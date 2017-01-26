/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class SensorValueConverter {
    private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
    protected Sensor sensor;
    protected Double conversion;

    public SensorValueConverter(Sensor sensor) {
        this.sensor = sensor;
    }

    public SensorValueConverter(Sensor sensor, Double conversion) {
        this.sensor = sensor;
        this.conversion = conversion;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Double getConversion() {
        return conversion;
    }

    public void updateSensor(String value) {
        if (sensor == null) {
            return;
        }

        String sensorValue = value;

        if (getConversion() != null) {
            try {
                double val = Double.parseDouble(value);
                val = val * getConversion();
                sensorValue = new DecimalFormat("#.###").format(val);
            } catch (NumberFormatException e) {
                log.error("Value conversion requested for sensor '" + sensor.getName() + "' but value is not a number");
            }
        }

        if (sensor instanceof StateSensor) {
            // State sensors are case sensitive and expect lower case
            sensorValue = sensorValue.toLowerCase();
        } else if (sensor instanceof RangeSensor) {
            try {
                // Value must be an integer
                BigDecimal parsedValue = new BigDecimal(sensorValue);

                if (sensor instanceof LevelSensor) {
                    sensorValue = Integer.toString(Math.min(100, Math.max(0, parsedValue.intValue())));
                } else {
                    sensorValue = Integer.toString(parsedValue.intValue());
                }
            } catch (NumberFormatException e) {
                log.warn("Received value (" + sensorValue + ") invalid, cannot be converted to integer");
                sensorValue = "0";
            }
        }

        sensor.update(sensorValue);
    }
}
