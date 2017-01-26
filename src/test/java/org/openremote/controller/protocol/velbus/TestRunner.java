package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.protocol.velbus.VelbusCommand.Action;


class TestRunner {
  private static final Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  String sensorValue = "N/A";

  void Run() {
    // Build commands and sensors
    // TEST DIMMER MODULES
//      int[] dimmerAddresses = new int[] {
//              186, // VMB4DC
//              //192, // VMBDMI
//               // VMBDME
//      };
//
//      for (int addr : dimmerAddresses) {
//        VelbusReadCommand command = (VelbusReadCommand)cmdBuilder.build(createCommandElement(1, 97, VelbusCommand.Action.LOCK_STATUS, "1", null));
//        TestSensor sensor = new TestSensor("TEST", 0, 0, null, command, null, EnumSensorType.CUSTOM);
//        command.setSensor(sensor);
//        loopRead(new TestSensor[] {sensor}, 3, 1000);
//
//        VelbusReadCommand command2 = (VelbusReadCommand)cmdBuilder.build(createCommandElement(2, 97, VelbusCommand.Action.LOCK_STATUS, "1", null));
//        TestSensor sensor2 = new TestSensor("TEST", 0, 0, null, command, null, EnumSensorType.CUSTOM);
//        command2.setSensor(sensor2);
//        loopRead(new TestSensor[] {sensor2}, 3, 1000);
//
//      VelbusWriteCommand onCommand = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(2, 97, VelbusCommand.Action.LOCK, "1:5", 50));
//      onCommand.send();
//
//      loopRead(new TestSensor[] {sensor}, 30, 1000);

//        VelbusWriteCommand onCommand = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(186, VelbusCommand.Action.ON, "1"));
//        onCommand.send();
//        
//        loopRead(new TestSensor[] {sensor}, 20, 1000);
//        
//        VelbusWriteCommand offCommand = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(1, 186, VelbusCommand.Action.OFF, "1", null));
//        offCommand.send();
//
//        loopRead(new TestSensor[] {sensor}, 20, 1000);

    //    // Read all status info from channel 1
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
    //    
    //    // Read all status info from channel 3 (Only the VMB4DC)
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "3"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "3"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "3"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "3"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "3"));
    //
    //    // Lock channel 1 and try and turn it on
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "1"));
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "1"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UNLOCK, "1"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
    //    
    //    // Turn channel 1 on and then off checking status changes
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "1"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    Thread.sleep(2000);
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_LEVEL, "1:75:10"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "1"), 10,1000);
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_LEVEL, "1:15:10"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "1"), 5,1000);
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.STOP, "1"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "1"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.OFF, "1"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
    //
    //    // Lock channel 2 for 10s and try and turn it on
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "2:10"));
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "2"));
    //    Thread.sleep(5000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "2"));
    //    Thread.sleep(7000);
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "2"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
    //    // Set dim level
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_LEVEL, "2:50:2"));
    //    Thread.sleep(2000);
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
    //    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.DIMMER_STATUS, "2"));
    //    Thread.sleep(2000);
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.OFF, "2"));
    //    
    //    // Turn channel 1 on for 10 seconds and check status changes back to off
    //    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "1:10"));
    //    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"),16,1000);
  }


  // Energy Monitor Test
//    VelbusReadCommand[] commands = new VelbusReadCommand[6];
//    TestSensor[] sensors = new TestSensor[6];
//    Integer counter = 1;
//    for (int i=0;i<6;i++) {
//      boolean isEven = i % 2 == 0;
//      Action action = isEven ? Action.COUNTER_STATUS : Action.COUNTER_INSTANT_STATUS;
//      String name = (isEven ? "COUNTER" : "COUNTERINSTANT") + counter;
//      commands[i] = (VelbusReadCommand)cmdBuilder.build(createCommandElement(1, 10, action, counter.toString(), null));
//      sensors[i] = new TestSensor(name, 0, 0, null, commands[i], null, EnumSensorType.CUSTOM);
//      commands[i].setSensor(sensors[i]);
//
//      if (!isEven) {
//        counter++;
//      }
//    }
//
//    loopRead(sensors, 300, 1000);

//    // Input Button Status Test
//    VelbusReadCommand[] commands = new VelbusReadCommand[8];
//    TestSensor[] sensors = new TestSensor[8];
//    Integer[] channels = new Integer[] { 1, 9, 18, 27};
//    
//    for (int i=0;i<4;i++) {
//      //Integer channelNumber = (int)Math.max(1d, Math.round(Math.random()*channelCount));
//      Integer channelNumber = channels[i];
//      String name = "BUTTON STATUS" + channelNumber;
//      commands[i] = (VelbusReadCommand)cmdBuilder.build(createCommandElement(48, Action.STATUS, channelNumber.toString()));
//      sensors[i] = new TestSensor(name, 0, null, commands[i], null, EnumSensorType.CUSTOM);
//      commands[i].setSensor(sensors[i]);
//    }
//    for (int i=4;i<8;i++) {
//      //Integer channelNumber = (int)Math.max(1d, Math.round(Math.random()*channelCount));
//      Integer channelNumber = channels[i-4];
//      String name = "LOCK STATUS" + channelNumber;
//      commands[i] = (VelbusReadCommand)cmdBuilder.build(createCommandElement(48, Action.LOCK_STATUS, channelNumber.toString()));
//      sensors[i] = new TestSensor(name, 0, null, commands[i], null, EnumSensorType.CUSTOM);
//      commands[i].setSensor(sensors[i]);
//    }
//    
//    loopRead(sensors, 1, 1000);
//    
//    // Set lock on 2 channels
//    VelbusWriteCommand lock1 = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(48, Action.LOCK, "9"));
//    VelbusWriteCommand lock2 = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(48, Action.LOCK, "27"));
//    lock1.send();
//    lock2.send();
//    loopRead(sensors, 15, 1000);
//
//    VelbusWriteCommand unlock1 = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(48, Action.UNLOCK, "9"));
//    VelbusWriteCommand unlock2 = (VelbusWriteCommand)cmdBuilder.build(createCommandElement(48, Action.UNLOCK, "27"));
//    unlock1.send();
//    unlock2.send();
//    
//    loopRead(sensors, 1, 1000);
  ////  sendCommand(new VelbusWriteCommand(connectionManager, 220, VelbusCommand.Action.MEMO_TEXT, "This a memo text example:10"));
  //
  ////  String energy = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_STATUS, "2"));
  ////  Thread.sleep(3000);
  ////  String energy1 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_STATUS, "1"));
  ////  String energyInstant1 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "1"));
  ////  String energy2 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_STATUS, "2"));
  ////  String energyInstant2 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "2"));
  ////  String energy3 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_STATUS, "3"));
  ////  String energyInstant3 = singleRead(new VelbusReadCommand(connectionManager, 10, VelbusCommand.Action.COUNTER_INSTANT_STATUS, "3"));
  ////
  ////  // TEST RELAY MODULES
  ////  int[] relayAddresses = new int[] {
  ////          81, // VMB4RYNO
  ////          82, // VMB4RYLD
  ////          84, // VMB1RYNOS
  ////          85  // VMB1RYNO
  ////  };
  ////
  ////  for (int addr : relayAddresses) {
  ////
  ////    // Read all status info from channel 3
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "3"));
  ////
  ////    // Lock channel 1 and try and turn it on
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UNLOCK, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////
  ////    // Turn channel 1 on and then off checking status changes
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    Thread.sleep(3000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.OFF, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////
  ////    // Lock channel 2 for 10s and try and turn it on
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "2:10"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "2"));
  ////    Thread.sleep(5000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "2"));
  ////    Thread.sleep(7000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "2"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.OFF, "2"));
  ////
  ////    // Turn channel 2 on for 10 seconds and check status changes back to off
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ON, "2:10"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"),16,1000);
  ////  }
  //
  //
  //  // TEST STANDARD INPUT MODULES
  //  int[] inputAddresses = new int[] {
  //          //52, // VMBGP2
  //          48,  // VMBGPOD
  //          10, // VMB7IN
  //          160 // VMB8PBU
  //              // VMB6PBN
  //  };
  //
  //  for (int addr : inputAddresses) {
  //
  //    // Read all status info from channel 3
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "3"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ENABLED_STATUS, "3"));
  ////
  ////    // Read all status info from a high channel (only exists on OLED panels)
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "23"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "23"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "23"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "23"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ENABLED_STATUS, "23"));
  ////
  ////    // Read alarm and temperature info
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TYPE_STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "BED1"));
  ////
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TYPE_STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "BED2"));
  ////
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SUNRISE_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SUNSET_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.PROGRAM_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, "HEAT_COMFORT"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_STATE_STATUS, null));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_MODE_STATUS, null));
  //
  //    // Set alarm time
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE1"));
  ////  sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME, "1:WAKE:360"));
  ////  Thread.sleep(2000);
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE1"));
  ////  sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_RELATIVE, "1:WAKE:-60"));
  ////  Thread.sleep(2000);
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE1"));
  ////  sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME, "1:WAKE:420"));
  ////  Thread.sleep(2000);
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "WAKE1"));
  ////
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "BED2"));
  ////  sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME, "2:BED:1260"));
  ////  Thread.sleep(2000);
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "BED2"));
  ////  sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME, "2:BED:1380"));
  ////  Thread.sleep(2000);
  ////  singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.ALARM_TIME_STATUS, "BED2"));
  //
  //    // Lock channel 1 and try and turn it on
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.PRESS, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UNLOCK, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////
  ////    // Change channel 2 status checking status changes
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.PRESS, "2"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LONG_PRESS, "2"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.RELEASE, "2"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////
  ////    // Change channel 26 - Only work on OLED
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.PRESS, "26"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "26"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LONG_PRESS, "26"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "26"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.RELEASE, "26"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "26"));
  ////
  ////    // Lock channel 4 for 10s and try and press it
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "4:10"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.PRESS, "4"));
  ////    Thread.sleep(5000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "4"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "4"));
  ////    Thread.sleep(7000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.PRESS, "4"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "4"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.RELEASE, "4"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "4"));
  ////
  ////    // Set channel 3 LED state
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LED, "1:SLOW"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
  ////    Thread.sleep(3000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LED, "1:FAST"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
  ////    Thread.sleep(3000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LED, "1:VERYFAST"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
  ////    Thread.sleep(3000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LED, "1:ON"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
  ////    Thread.sleep(3000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LED, "1:OFF"));
  ////
  ////    // Set target temp
  ////    String tTemp = singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, "HEAT_SAFE"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET, "HEAT_SAFE:7"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, "HEAT_SAFE"));
  ////    Thread.sleep(2000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_RELATIVE, "HEAT_SAFE:-5.0"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, "HEAT_SAFE"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET, "HEAT_SAFE:" + tTemp));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_TARGET_STATUS, "HEAT_SAFE"));
  ////
  ////    // Change temp mode for 1 minute
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_MODE_STATUS, null));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.TEMP_MODE, "HEAT_NIGHT:1"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.TEMP_MODE_STATUS, null),13,5000);
  ////
  ////    // Set memo text (OLED only)
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.MEMO_TEXT, "Hello Stuart!"));
  //
  //  }
  //
  //
  //  // TEST BLIND MODULES
  //  int[] blindAddresses = new int[] {
  //          97, // VMB2BLE
  //          106, // VMB1BL
  //  };
  //
  //  for (int addr : blindAddresses) {
  //
  //    // Read all status info from channel 1
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////
  ////    // Read all status info from channel 2 (Only the VMB2BLE)
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.SETTING_STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LED_STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "2"));
  ////
  ////    // Lock channel 1 and try and turn it on
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UP, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UNLOCK, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "1"));
  ////
  ////    // Turn channel 1 on and then off checking status changes
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.BLIND_POSITION, "1:50"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "1"), 10,1000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UP, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "1"), 10,1000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.HALT, "1"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "1"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.DOWN, "1"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "1"), 10,1000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "1"));
  ////
  ////    // Lock channel 2 for 10s and try and turn it on
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.LOCK, "2:10"));
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UP, "2"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "2"), 10, 1000);
  ////    Thread.sleep(5000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.LOCK_STATUS, "2"));
  ////    Thread.sleep(7000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.UP, "2"));
  ////    Thread.sleep(2000);
  ////    singleRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.STATUS, "2"));
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "2"), 10, 1000);
  ////    // Set position
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.BLIND_POSITION, "2:50"));
  ////    Thread.sleep(5000);
  ////    loopRead(new VelbusReadCommand(connectionManager, addr, VelbusCommand.Action.POSITION_STATUS, "2"), 10, 1000);
  ////    Thread.sleep(2000);
  ////    sendCommand(new VelbusWriteCommand(connectionManager, addr, VelbusCommand.Action.DOWN, "2"));
  //  }

  //
  //  Thread.sleep(10000);
  //  log.debug("Stopping Test");
  //  connectionManager.stop();
}