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

import junit.framework.Assert;
import org.junit.Test;

public class VmbmeteoTest extends BaseTest {
    VelbusCommandBuilder builder;

    @Override
    VelbusCommandBuilder getCommandBuilder() {
        if (builder == null) {
            builder = new VelbusCommandBuilder(new String[] {"mdar-model.no-ip.biz"}, new String[] {"6066"});
        }
        return builder;
    }

    @Test
    public void windLightRainRead() throws InterruptedException {
        VelbusReadCommand lightCommand = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "LiGHT"));
        VelbusReadCommand rainCommand = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "RAIN"));
        VelbusReadCommand windCommand = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "WIND"));
        TestSensor lightSensor = createSensor("Light", lightCommand);
        TestSensor rainSensor = createSensor("Rain", rainCommand);
        TestSensor windSensor = createSensor("Wind", windCommand);

        // Check value conversion support
        VelbusReadCommand lightConvertedCommand = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "LIGHT:0.5"));
        TestSensor lightConvertedSensor = createSensor("LightConverted", lightConvertedCommand);

        // Check sensors have some value (allow some time for device to initialise)
        Assert.assertTrue(sensorHasValue(lightSensor, 30));
        Assert.assertTrue(sensorHasValue(rainSensor, 1));
        Assert.assertTrue(sensorHasValue(windSensor, 1));
        Assert.assertTrue(sensorHasValue(lightConvertedSensor, 1));

        System.out.println("Light sensor value is: " + lightSensor.value);
        System.out.println("Light converted sensor value is: " + lightConvertedSensor.value);
        System.out.println("Rain sensor value is: " + rainSensor.value);
        System.out.println("Wind sensor value is: " + windSensor.value);
    }

    @Test
    public void tempRead() throws InterruptedException {
        VelbusReadCommand tempCommand = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.TEMP_STATUS));
        TestSensor tempSensor = createSensor("Temp", tempCommand);

        // Check sensor has some value (allow some time for device to initialise)
        Assert.assertTrue(sensorHasValue(tempSensor, 20));

        System.out.println("Temp sensor value is: " + tempSensor.value);
    }

    @Test
    public void channelRead() throws InterruptedException {
        VelbusReadCommand channel1Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "1"));
        VelbusReadCommand channel2Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "2"));
        VelbusReadCommand channel3Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "3"));
        VelbusReadCommand channel4Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "4"));
        VelbusReadCommand channel5Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "5"));
        VelbusReadCommand channel6Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "6"));
        VelbusReadCommand channel7Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "7"));
        VelbusReadCommand channel8Command = (VelbusReadCommand)getCommandBuilder().build(createCommandElement(1, 254, VelbusCommand.Action.STATUS, "8"));
        TestSensor channel1Sensor = createSensor("Channel1", channel1Command);
        TestSensor channel2Sensor = createSensor("Channel2", channel2Command);
        TestSensor channel3Sensor = createSensor("Channel3", channel3Command);
        TestSensor channel4Sensor = createSensor("Channel4", channel4Command);
        TestSensor channel5Sensor = createSensor("Channel5", channel5Command);
        TestSensor channel6Sensor = createSensor("Channel6", channel6Command);
        TestSensor channel7Sensor = createSensor("Channel7", channel7Command);
        TestSensor channel8Sensor = createSensor("Channel8", channel8Command);

        // Check sensor has some value (allow some time for device to initialise)
        Assert.assertTrue(sensorHasValue(channel1Sensor, 20));
        Assert.assertTrue(sensorHasValue(channel2Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel3Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel4Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel5Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel6Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel7Sensor, 1));
        Assert.assertTrue(sensorHasValue(channel8Sensor, 1));

        System.out.println("Channel 1 sensor value is: " + channel1Sensor.value);
        System.out.println("Channel 2 sensor value is: " + channel2Sensor.value);
        System.out.println("Channel 3 sensor value is: " + channel3Sensor.value);
        System.out.println("Channel 4 sensor value is: " + channel4Sensor.value);
        System.out.println("Channel 5 sensor value is: " + channel5Sensor.value);
        System.out.println("Channel 6 sensor value is: " + channel6Sensor.value);
        System.out.println("Channel 7 sensor value is: " + channel7Sensor.value);
        System.out.println("Channel 8 sensor value is: " + channel8Sensor.value);
    }
}