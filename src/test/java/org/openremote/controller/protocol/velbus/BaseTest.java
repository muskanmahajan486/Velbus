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

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.component.EnumSensorType;

public abstract class BaseTest {
    abstract VelbusCommandBuilder getCommandBuilder();
//
//    protected static void loopRead(TestSensor[] sensors, int loops, int pause) {
//        int count = 0;
//
//        while (count < loops) {
//
//            String output = "";
//            String header = "";
//
//            for (TestSensor sensor : sensors) {
//                header += sensor.getName() + "    ";
//                output += sensor.value + "    ";
//            }
//
//            System.out.println(header);
//            System.out.println(output);
//
//            count++;
//            try {
//                Thread.sleep(pause);
//            } catch (InterruptedException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
//        }
//    }
//
//    protected static void singleRead(TestSensor[] sensors) {
//        String output = "";
//        String header = "";
//
//        for (TestSensor sensor : sensors) {
//            header += sensor.getName() + "    ";
//            output += sensor.value + "    ";
//        }
//
//        System.out.println(header);
//        System.out.println(output);
//    }

    protected static boolean sensorHasValue(TestSensor sensor, String expectedValue, int waitSeconds) throws InterruptedException {
        int counter = 0;
        String value = sensor.value;

        while (counter < waitSeconds * 10 && !expectedValue.equalsIgnoreCase(value)) {
            value = sensor.value;
            counter++;
            Thread.sleep(100);
        }

        return expectedValue.equalsIgnoreCase(value);
    }

    protected static boolean sensorHasValue(TestSensor sensor, int waitSeconds) throws InterruptedException {
        int counter = 0;
        String value = sensor.value;

        while (counter < waitSeconds * 10 && (value == null || "N/A".equalsIgnoreCase(value))) {
            value = sensor.value;
            counter++;
            Thread.sleep(100);
        }

        return value != null && !"N/A".equalsIgnoreCase(value);
    }

    protected static TestSensor createSensor(String name, VelbusReadCommand readCommand) {
        TestSensor sensor = new TestSensor(name, 0, 0, null, readCommand, null, EnumSensorType.CUSTOM);
        readCommand.setSensor(sensor);
        return sensor;
    }

    protected static Element createCommandElement(int networkId, int address, VelbusCommand.Action action) {
        return createCommandElement(networkId, address, action, "", null);
    }

    protected static Element createCommandElement(int networkId, int address, VelbusCommand.Action action, String value) {
        return createCommandElement(networkId, address, action, value, null);
    }

    protected static Element createCommandElement(int networkId, int address, VelbusCommand.Action action, String value, Integer dynamicValue) {
        Element elem = new Element("command");

        if (dynamicValue != null) {
            elem.setAttribute(Command.DYNAMIC_VALUE_ATTR_NAME, dynamicValue.toString());
        }

        Element propNetworkId = new Element("property");
        propNetworkId.setAttribute("name", "networkId");
        propNetworkId.setAttribute("value", new Integer(networkId).toString());

        Element propAddress = new Element("property");
        propAddress.setAttribute("name", "address");
        propAddress.setAttribute("value", new Integer(address).toString());

        Element propAction = new Element("property");
        propAction.setAttribute("name", "command");
        propAction.setAttribute("value", action.toString());

        Element propValue = new Element("property");
        propValue.setAttribute("name", "value");
        propValue.setAttribute("value", value);

        elem.addContent(propNetworkId);
        elem.addContent(propAddress);
        elem.addContent(propAction);
        elem.addContent(propValue);
        return elem;
    }
}
