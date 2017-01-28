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

public class Vmb1tsTest extends BaseTest {
    VelbusCommandBuilder builder;

    @Override
    VelbusCommandBuilder getCommandBuilder() {
        if (builder == null) {
            builder = new VelbusCommandBuilder(new String[] {"mdar-model.no-ip.biz"}, new String[] {"6006"});
        }
        return builder;
    }

    @Test
    public void tempRead() throws InterruptedException {
        VelbusReadCommand tempCommand = (VelbusReadCommand) getCommandBuilder().build(createCommandElement(1, 5, VelbusCommand.Action.TEMP_STATUS));
        TestSensor tempSensor = createSensor("Temp", tempCommand);

        // Check sensor has some value (allow some time for device to initialise)
        Assert.assertTrue(sensorHasValue(tempSensor, 30));

        System.out.println("Temp sensor value is: " + tempSensor.value);
    }
}