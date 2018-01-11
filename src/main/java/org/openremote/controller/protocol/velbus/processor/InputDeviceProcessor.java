package org.openremote.controller.protocol.velbus.processor;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.velbus.PacketRequestCommand;
import org.openremote.controller.protocol.velbus.PacketResponseCommand;
import org.openremote.controller.protocol.velbus.VelbusCommand;
import org.openremote.controller.protocol.velbus.VelbusCommandBuilder;
import org.openremote.controller.protocol.velbus.VelbusDevice;
import org.openremote.controller.protocol.velbus.VelbusDeviceType;
import org.openremote.controller.protocol.velbus.VelbusPacket;
import org.openremote.controller.protocol.velbus.VelbusPacket.PacketPriority;
import org.openremote.controller.protocol.velbus.VelbusReadCommand;
import org.openremote.controller.protocol.velbus.VelbusWriteCommand;
import org.openremote.controller.protocol.velbus.VelbusCommand.Action;

public class InputDeviceProcessor extends VelbusDeviceProcessorImpl {
  enum Program {
    NONE(0x00),
    SUMMER(0x01),
    WINTER(0x02),
    HOLIDAY(0x03);
     
    private int code;
    
    private Program(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    @Override
    public String toString() {
      return super.toString() + "(" + code + ")";
    }
    
    public static Program fromCode(int code) {
      for (Program type : Program.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return NONE;
    }
  }
   
  enum ChannelStatus {
    RELEASED,
    PRESSED,
    LONG_PRESSED
  }
  
  enum TemperatureMode {
    CURRENT(0),
    COOL_COMFORT(7),
    COOL_DAY(8),
    COOL_NIGHT(9),
    COOL_SAFE(10),
    HEAT_COMFORT(1),
    HEAT_DAY(2),
    HEAT_NIGHT(3),
    HEAT_SAFE(4);
    
    private int pointerIndex;
    
    private TemperatureMode(int pointerIndex) {
      this.pointerIndex = pointerIndex;
    }
    
    public int getPointerIndex() {
      return this.pointerIndex;
    }
  }
  
  enum TemperatureState {
    DISABLED,
    MANUAL,
    TIMER,
    NORMAL
  }
  
  enum Language {
    ENGLISH(0x00),
    FRANCAIS(0x01),
    NEDERLANDS(0x02),
    ESPANOL(0x03),
    DEUTSCH(0x04),
    ITALIANO(0x05);
     
    private int code;
    
    private Language(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    public static Language fromCode(int code) {
      for (Language type : Language.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return ENGLISH;
    }
  }
  
  enum CounterUnits {
    RESERVED(0x00),
    LITRES(0x01),
    CUBICMETRES(0x02),
    KILOWATTS(0x03);
     
    private int code;
    
    private CounterUnits(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    public static CounterUnits fromCode(int code) {
      for (CounterUnits type : CounterUnits.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return RESERVED;
    }
  }
  
  private static final Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  
  @Override
  public Iterable<VelbusPacket> getStatusRequestPackets(VelbusDevice device) {
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MODULE_STATUS.getCode(), (byte)0x00));
    packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.TEMP_SENSOR_STATUS.getCode(), (byte)0x00));
    
    // Add alarm memory read requests
    Integer alarm1 = getAlarmMemoryLocation(1, device.getDeviceType());
    Integer alarm2 = getAlarmMemoryLocation(2, device.getDeviceType());
    if (alarm1 != null && alarm2 != null) {
      packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.READ_MEMORY_BLOCK.getCode(), (byte)(alarm1 >> 8), (byte)(alarm1 >> 0)));
      packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.READ_MEMORY_BLOCK.getCode(), (byte)(alarm2 >> 8), (byte)(alarm2 >> 0)));
    }
    
    // Add energy counter requests
    if (device.getDeviceType() == VelbusDeviceType.VMB7IN) {
      packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.COUNTER_STATUS.getCode(), (byte)0x0F, (byte)0x00));
      packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.READ_MEMORY.getCode(), (byte)0x03, (byte)0xFE));      
    }

    // Add light, wind and rain request
    if (device.getDeviceType() == VelbusDeviceType.VMBMETEO) {
      packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.SENSOR_READOUT.getCode(), (byte)0x0F, (byte)0x00));
    }
    
    return packets;
  }

  @Override
  public String getDeviceCachePropertyName(VelbusReadCommand command) {
    String propertyName = null;
    String commandValue = command.getValue() == null ? null : command.getValue().trim().toUpperCase();
    
    switch(command.getAction()) {
      case STATUS:
      {
        try {
          // Check which channel they want
          if ("HEATER".equals(commandValue)) {
            return "heater";
          }
          if ("COOLER".equals(commandValue)) {
            return "cooler";
          }
          if ("PUMP".equals(commandValue)) {
            return "pump";
          }
          if ("BOOST".equals(commandValue)) {
            return "boost";
          }
          if ("ALARM1".equals(commandValue)) {
            return "tempAlarm1";
          }
          if ("ALARM2".equals(commandValue)) {
            return "tempAlarm2";
          }
          if ("ALARM3".equals(commandValue)) {
            return "tempAlarm3";
          }
          if ("ALARM4".equals(commandValue)) {
            return "tempAlarm4";
          }
          int channelNumber = Integer.parseInt(commandValue);
          propertyName = "channelStatus" + channelNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case ENABLED_STATUS:
      {
        try {
          // Check which channel they want
          int channelNumber = Integer.parseInt(commandValue);
          propertyName = "channelEnabled" + channelNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case LOCK_STATUS:
      {
        try {
          // Check which channel they want
          int channelNumber = Integer.parseInt(commandValue);
          propertyName =  "channelLock" + channelNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case LED_STATUS:
      {
        try {
          // Check which channel they want
          int channelNumber = Integer.parseInt(commandValue);
          propertyName = "channelLed" + channelNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case ALARM_STATUS:
      {
        try {
          // Check which alarm they want
          int alarmNumber = Integer.parseInt(commandValue);
          propertyName = "alarmStatus" + alarmNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case ALARM_TYPE_STATUS:
      {
        try {
          // Check which alarm they want
          int alarmNumber = Integer.parseInt(commandValue);
          propertyName = "alarmType" + alarmNumber;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case ALARM_TIME_STATUS:
      {
        try {
          // Check which alarm they want
          String[] params = commandValue.split(":");
          int number = Integer.parseInt(params[0].trim());
          String type = params[1];
          propertyName = "alarmTime" + type + number;
        } catch (NumberFormatException e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case SUNRISE_STATUS:
      {
        propertyName = "sunrise";
        break;
      }
      case SUNSET_STATUS:
      {
        propertyName = "sunset";
        break;
      }
      case TEMP_STATUS:
      {
        propertyName = "tempCurrent";
        break;
      }
      case TEMP_TARGET_STATUS:
      {
        TemperatureMode mode = null;
        try {
          mode = commandValue != null && !commandValue.isEmpty() ? TemperatureMode.valueOf(commandValue.trim()) : null;
        } catch (IllegalArgumentException e) {
          log.warn("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command will default to CURRENT");
        }
        if (mode == null) {
          mode = TemperatureMode.CURRENT;
        }
        
        propertyName = "tempTarget_" + mode;
        break;
      }
      case TEMP_MODE_STATUS:
      {
        propertyName = "tempMode";
        break;
      }
      case TEMP_STATE_STATUS:
      {
        propertyName = "tempState";
        break;
      }
      case PROGRAM_STATUS:
      {
        propertyName = "program";
        break;
      }
      case COUNTER_STATUS:
      case COUNTER_INSTANT_STATUS:
      {
        // Check which channel they want and look for any conversion multiplier
        String[] params = commandValue.split(":");
        String prefix = command.getAction() == Action.COUNTER_STATUS ? "counter" : "counterInstant";
        propertyName = prefix + params[0];
        break;
      }
    }
    
    return propertyName;
  }

  @Override
  public Iterable<VelbusPacket> processWriteCommand(final VelbusDevice device, VelbusWriteCommand command) {
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    String commandValue = command.getValue() != null ? command.getValue().toUpperCase() : null;
    
    switch (command.getAction())
    {
      case LOCK:
      {
        try {
          // Check which channel they want
          String[] params = commandValue.split(":");
          int channelNumber = Integer.parseInt(params[0].trim());

          // Look for time value
          int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
          
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.LOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, (byte)channelNumber, (byte)(time >> 16), (byte)(time >> 8), (byte)time));
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case UNLOCK:
      {
        try {
          int channelNumber = Integer.parseInt(commandValue);

          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.UNLOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, (byte)channelNumber));
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case LED:
      {
        try {
          String[] params = commandValue.split(":");
          if (params.length == 2) {
            int channelNumber = Integer.parseInt(params[0]);
            byte channelByte = (byte)(Math.pow(2, channelNumber - 1));
            
            // Figure out address to use
            int addressIndex = Math.min(0, Math.max(4, (int)Math.floor((double)channelNumber / 8)));
            int address = device.getAddresses()[addressIndex];
            
            if (address > 0) {
              // Add command
              PacketRequestCommand rCommand = PacketRequestCommand.valueOf("LED_" + params[1].trim());
              LedStatus ledStatus = LedStatus.valueOf(params[1].trim());
              packets.add(new VelbusPacket(address, rCommand.getCode(), VelbusPacket.PacketPriority.LOW, channelByte));
              
              // Update the device cache as well as the module won't broadcast an LED change
              device.updatePropertyValue("channelLed" + channelNumber, ledStatus);
            }
          }
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case TEMP_TARGET:
      case TEMP_TARGET_RELATIVE:
      {
        try {
          String[] params = commandValue.split(":");
          TemperatureMode mode = TemperatureMode.valueOf(params[0].trim());
          double tempValue = params.length == 2 && !"${param}".equalsIgnoreCase(params[1]) ? Double.parseDouble(params[1]) : 0d;
          
          if (command.getAction() == Action.TEMP_TARGET_RELATIVE) {
            // Lookup current temp
            String propertyName = "tempTarget_" + mode;
            Object obj = device.getPropertyValue(propertyName);
            double currentTemp = Double.parseDouble((String)obj);
            tempValue = currentTemp + tempValue;
            
            // Push new value back to cache otherwise it might not update before someone sends another incremental change
            device.updatePropertyValue(propertyName, new DecimalFormat("##.#").format(tempValue));
          } else if (command.getParameter() != null) {
            // Use command parameter value for new target
            try {
              tempValue = Double.parseDouble(command.getParameter());
            } catch (NumberFormatException e) {
              log.error("Invalid dynamic value supplied for temperature", e);
            }
          }
          
          tempValue = (int)Math.round((tempValue * 10) / 5);
          if (tempValue >= -110 && tempValue <= 125) {
            packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.SET_TEMP.getCode(), VelbusPacket.PacketPriority.LOW, (byte)mode.getPointerIndex(), (byte)tempValue));
          }
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case TEMP_MODE:
      {
        try {
          String[] params = commandValue.split(":");
          TemperatureMode mode = TemperatureMode.valueOf(params[0].trim()); // Just to validate temp mode input
          String[] modes = params[0].split("_");
          PacketRequestCommand mode1Command = PacketRequestCommand.valueOf("SET_MODE_" + modes[0]);
          PacketRequestCommand mode2Command = PacketRequestCommand.valueOf("SET_MODE_" + modes[1]);
  
          // Look for time value
          int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0x0000;
          time = time == -1 ? 0xFFFF : time;
          
          // Add two packets one to set cool heat mode and one to set comfort etc.
          packets.add(new VelbusPacket(device.getAddresses()[0], mode1Command.getCode(), VelbusPacket.PacketPriority.LOW, (byte)0x00));
          packets.add(new VelbusPacket(device.getAddresses()[0], mode2Command.getCode(), VelbusPacket.PacketPriority.LOW, (byte)(time >> 8), (byte)time));
        
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case ALARM:
      case ALARM_TIME:
      case ALARM_TIME_RELATIVE:
      {
        try {
          String[] params = commandValue.split(":");
          if (params.length == 3) {
            int number = Integer.parseInt(params[0].trim());
            boolean isGlobal = command.getAction() == Action.ALARM ? params[1].trim().equals("MASTER") : device.getPropertyValue("ALARMTYPE" + number).equals("MASTER");
            boolean isEnabled = command.getAction() == Action.ALARM ? params[2].trim().equals("ON") : device.getPropertyValue("ALARMSTATUS" + number).equals("ON");
            
            // Find this alarm in the cache
            if (getAlarmMemoryLocation(number, device.getDeviceType()) == null) {
              log.debug("Alarms not supported by device type '" + device.getDeviceType() + "'");
              return packets;
            }
            
            int wakeHours = (Integer)device.getPropertyValue("ALARMHOURSWAKE" + number);
            int wakeMins = (Integer)device.getPropertyValue("ALARMMINUTESWAKE" + number);            
            int bedHours = (Integer)device.getPropertyValue("ALARMHOURSBED" + number);
            int bedMins = (Integer)device.getPropertyValue("ALARMMINUTESBED" + number);
            
            // Adjust times if required
            if (command.getAction() != Action.ALARM) {
              String alarmTimeType = params[1];
              Calendar time = Calendar.getInstance();
              if (command.getAction() == Action.ALARM_TIME) {
                int dayMins = Integer.parseInt(params[2].trim());
                
                if (command.getParameter() != null) {
                  // Use command parameter value for new target
                  try {
                    dayMins = Integer.parseInt(command.getParameter());
                  } catch (NumberFormatException e) {
                    log.error("Invalid dynamic value supplied", e);
                  }
                }
                
                int hours = (int)Math.floor((double)dayMins/60);
                int mins = dayMins % 60;
                time.set(Calendar.HOUR_OF_DAY, hours);
                time.set(Calendar.MINUTE, mins);
              } else {
                time.set(Calendar.HOUR_OF_DAY, alarmTimeType.equals("WAKE") ? wakeHours : bedHours);
                time.set(Calendar.MINUTE, alarmTimeType.equals("WAKE") ? wakeMins : bedMins);
                int addMins = Integer.parseInt(params[2].replace('+', ' ').trim());
                time.add(Calendar.MINUTE, addMins);
              }
              
              if (alarmTimeType.equals("WAKE")) {
                wakeHours = time.get(Calendar.HOUR_OF_DAY);
                wakeMins = time.get(Calendar.MINUTE);
              } else {
                bedHours = time.get(Calendar.HOUR_OF_DAY);
                bedMins = time.get(Calendar.MINUTE);
              }
            }
            
            // Update alarm settings
            int address = isGlobal ? 0 : device.getAddresses()[0];
            
            packets.add(new VelbusPacket(address, PacketRequestCommand.SET_ALARM.getCode(), PacketPriority.LOW, (byte)number, (byte)wakeHours, (byte)wakeMins, (byte)bedHours, (byte)bedMins, isEnabled ? (byte)0x01 : (byte)0));
            
            // Add packets to refresh alarm time
            Integer alarmMemoryLocation = getAlarmMemoryLocation(number, device.getDeviceType());
            //Integer alarm2 = getAlarmMemoryLocation(2, device.getDeviceType());
            if (alarmMemoryLocation != null) {
              packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.READ_MEMORY_BLOCK.getCode(), PacketPriority.LOW, (byte)(alarmMemoryLocation >> 8), (byte)(alarmMemoryLocation >> 0)));
//              packets.add(new VelbusPacket(isGlobal ? 0 : device.getAddresses()[0], PacketRequestCommand.READ_MEMORY_BLOCK.getCode(), (byte)(alarm2 >> 8), (byte)(alarm2 >> 0)));
            }
            
            // Push values back into cash to provide rapid change of relative time
            device.updatePropertyValue("ALARMHOURSWAKE" + number, wakeHours);
            device.updatePropertyValue("ALARMMINUTESWAKE" + number, wakeMins);
            device.updatePropertyValue("ALARMHOURSBED" + number, bedHours);
            device.updatePropertyValue("ALARMMINUTESBED" + number, bedMins);
            device.updatePropertyValue("ALARMTIMEWAKE" + number, String.format("%02d:%02d", wakeHours, wakeMins));
            device.updatePropertyValue("ALARMTIMEBED" + number, String.format("%02d:%02d", bedHours, bedMins));
            device.updatePropertyValue("ALARMSTATUS" + number, isEnabled ? "ON" : "OFF");
          }
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command", e);
        }
        break;
      }
      case MEMO_TEXT:
      {
        String[] vals = command.getValue().trim().split(":");
        int timeout = 5;
        int strEnd = vals.length;
        
        if (vals.length > 1) {
          try {
            timeout = Integer.parseInt(vals[vals.length - 1]);
            strEnd--;
          } catch (NumberFormatException e) {
            log.debug("Last section of command value '" + vals[vals.length - 1] + "' is not a number so using default value");
          }
        }
        
        StringBuilder strBuilder = new StringBuilder();
        for (int i=0; i < strEnd; i++) {
          strBuilder.append(vals[i]);
          if( i != strEnd-1) {
            strBuilder.append(":");
          }
        }
                
        String memoStr = strBuilder.toString();
        
        if (command.getParameter() != null) {
          memoStr = command.getParameter();
        }
        
        memoStr = memoStr.substring(0, Math.min(memoStr.length(), 62));
        try {
          byte[] bytes = memoStr.getBytes("UTF8");
          int counter = 0;
          byte[] packetBytes = new byte[7];
          packetBytes[0] = (byte)0x00; // Doesn't matter what this byte is
          packetBytes[1] = (byte)0x00; // startPos
          
          for (int i=0; i<=memoStr.length(); i++) {
            packetBytes[counter + 2] = i==memoStr.length() ? 0x00 : bytes[i];
            counter++;
            
            if (counter > 4 || i==memoStr.length()) {
              // Can only send 5 characters at a time
              byte[] pBytes = Arrays.copyOf(packetBytes, counter + 2);
              packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MEMO_TEXT.getCode(), VelbusPacket.PacketPriority.LOW, pBytes));
              packetBytes[1] = (byte)((packetBytes[1] & 0xFF) + 5);
              counter = 0;
            }
          }
        } catch (UnsupportedEncodingException e) {
          log.error("Failed to set memo text, couldn't convert string to UTF8");
        }
        
        // Create timer task to clear memo text
        Timer clearTimer = new Timer("Memo Text Clear Timer");
        clearTimer.schedule(new TimerTask() {          
          @Override
          public void run() {
            device.sendPacket(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MEMO_TEXT.getCode(), VelbusPacket.PacketPriority.LOW, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00));
            cancel();
          }
        }, timeout * 1000);
        break;
      }
      case PRESS:
      case LONG_PRESS:
      case RELEASE:
      {
        VelbusCommand.Action action = command.getAction();
        byte[] packetBytes = new byte[3];
        int byteIndex = action == Action.PRESS ? 0 : action == Action.RELEASE ? 1 : 2;
        
        try {
          int channelNumber = Integer.parseInt(commandValue);
          int addressIndex = Math.max(0, Math.min(4, (int)Math.floor((double)channelNumber / 8)));
          int channelIndex = channelNumber % 8;
          packetBytes[byteIndex] = (byte)(Math.pow(2, channelIndex - 1));
          packets.add(new VelbusPacket(device.getAddresses()[addressIndex], PacketRequestCommand.BUTTON_STATUS.getCode(), VelbusPacket.PacketPriority.HIGH, packetBytes));
          
          // Update cache as bus doesn't return a response
          device.updatePropertyValue("channelStatus" + channelNumber, action == Action.PRESS ? ChannelStatus.PRESSED : action == Action.LONG_PRESS ? ChannelStatus.LONG_PRESSED : ChannelStatus.RELEASED);
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case LANGUAGE:
      {
        if (device.getDeviceType() != VelbusDeviceType.VMBGPO && device.getDeviceType() != VelbusDeviceType.VMBGPOD) {
          break;
        }
        
        try {
          Language lang = Language.valueOf(commandValue);
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.WRITE_MEMORY.getCode(), VelbusPacket.PacketPriority.LOW, (byte)0x02, (byte)0xF2, (byte)lang.getCode()));
        } catch (Exception e) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        }
        break;
      }
      case PROGRAM:
      {
        Program prog = null;
        
        try {
          // Check which channel they want
          int progNumber = Integer.parseInt(commandValue);
          prog = Program.fromCode(progNumber);
        } catch (NumberFormatException e) {
          try {          
            prog = Program.valueOf(commandValue);
          } catch (Exception ex) {}
        }
        
        if (prog == null) {
          log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
        } else {
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.SELECT_PROGRAM.getCode(), VelbusPacket.PacketPriority.LOW, (byte)prog.getCode()));          
        }
        break;
      }
    }
    
    return packets;
  }

  @Override
  public void processResponse(VelbusDevice device, VelbusPacket packet) {
    PacketResponseCommand command = PacketResponseCommand.fromCode(packet.getCommand());
    switch (command)
    {
      case INPUT_STATUS:
      {
        // Get start channel
        int startChannel = 1;
        
        for (int i=0; i<5; i++) {
          if (device.getAddresses()[i] == packet.getAddress()) {
            startChannel = (i*8) + 1;
            break;
          }
        }
        
        // Extract channel info        
        // Update each of the 8 channels for this address
        int statusByte = 0;
        int enabledByte = 0;
        int invertedByte = 0;
        int lockByte = 0;
        int programByte = 0;

        if (device.getDeviceType() == VelbusDeviceType.VMBMETEO) {
          statusByte = packet.getByte(1) & 0xFF;
          lockByte = packet.getByte(2) & 0xFF;
          programByte = packet.getByte(3) & 0xFF;
        } else {
          statusByte = packet.getByte(1) & 0xFF;
          enabledByte = packet.getByte(2) & 0xFF;
          invertedByte = packet.getByte(3) & 0xFF;
          lockByte = packet.getByte(4) & 0xFF;
          programByte = packet.getByte(5) & 0xFF;
        }

        for (int i=startChannel; i<startChannel+8; i++) {
          ChannelStatus status = (statusByte & 0x01) == 1 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED;
          boolean enabled = (enabledByte & 0x01) == 1;
          boolean inverted = (invertedByte & 0x01) == 0;
          String lock = getOnOff((lockByte & 0x01));
          boolean programDisabled = (programByte & 0x01) == 1;
          statusByte = statusByte >>> 1;
          enabledByte = enabledByte >>> 1;
          invertedByte = invertedByte >>> 1;
          lockByte = lockByte >>> 1;
          programByte = programByte >>> 1;

          // Push to device cache
          device.updatePropertyValue("CHANNELSTATUS" + i, status);
          device.updatePropertyValue("CHANNELENABLED" + i, enabled);
          device.updatePropertyValue("CHANNELLOCK" + i, lock);
          device.updatePropertyValue("CHANNELINVERTED" + i, inverted);
          device.updatePropertyValue("CHANNELPROGRAMDISABLED" + i, programDisabled);
      }

      if (device.getDeviceType() == VelbusDeviceType.VMBGP4PIR) {
          // Read PIR sensor data
          
        }
               
        if (startChannel == 1) {
          byte alarmByte = device.getDeviceType() == VelbusDeviceType.VMBMETEO ? packet.getByte(4) : packet.getByte(6);
          device.updatePropertyValue("PROGRAM", Program.fromCode(alarmByte & 0x03));
          device.updatePropertyValue("ALARMSTATUS1", getOnOff((alarmByte & 0x04) == 0x04));
          device.updatePropertyValue("ALARMSTATUS2", getOnOff((alarmByte & 0x10) == 0x10));
          device.updatePropertyValue("ALARMTYPE1", (alarmByte & 0x08) == 0x08 ? "MASTER" : "LOCAL");
          device.updatePropertyValue("ALARMTYPE2", (alarmByte & 0x20) == 0x20 ? "MASTER" : "LOCAL");
          device.updatePropertyValue("SUNRISE", getOnOff((alarmByte & 0x40) == 0x40));
          device.updatePropertyValue("SUNSET", getOnOff((alarmByte & 0x80) == 0x80));
        }

        break;
      }
      case TEMP_STATUS:
      {
        // Extract State
        int stateInt = (packet.getByte(1) & 0x06);
        String stateStr = "NORMAL";
        
        if(stateInt == 0x06) {
          stateStr = "DISABLED";
        } else if(stateInt == 0x02) {
          stateStr ="MANUAL";
        } else if(stateInt == 0x04) {
          stateStr ="TIMER";
        }
        device.updatePropertyValue("TEMPSTATE",  TemperatureState.valueOf(stateStr));
        
        // Extract mode
        String modeStr = (packet.getByte(1) & 0x80) == 0x80 ? "COOL_" : "HEAT_";
        int modeInt = (packet.getByte(1) & 0x70);
        if(modeInt == 0x40) {
          modeStr += "COMFORT";
        } else if(modeInt == 0x20) {
          modeStr += "DAY";
        } else if(modeInt == 0x10) {
          modeStr += "NIGHT";
        } else {
          modeStr += "SAFE";
        }

        device.updatePropertyValue("TEMPMODE",  TemperatureMode.valueOf(modeStr));
        device.updatePropertyValue("HEATER", (packet.getByte(3) & 0x01) == 0x01 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("BOOST", (packet.getByte(3) & 0x02) == 0x02 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("PUMP", (packet.getByte(3) & 0x04) == 0x04 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("COOLER", (packet.getByte(3) & 0x08) == 0x08 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("TEMPALARM1", (packet.getByte(3) & 0x10) == 0x10 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("TEMPALARM2", (packet.getByte(3) & 0x20) == 0x20 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("TEMPALARM3", (packet.getByte(3) & 0x40) == 0x40 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("TEMPALARM4", (packet.getByte(3) & 0x80) == 0x80 ? ChannelStatus.PRESSED : ChannelStatus.RELEASED);
        device.updatePropertyValue("TEMPCURRENT",  new DecimalFormat("##.#").format((double)packet.getByte(4) / 2));
        device.updatePropertyValue("TEMPTARGET_CURRENT",  new DecimalFormat("##.#").format((double)packet.getByte(5) / 2));
        break;
      }
      case CURRENT_TEMP_STATUS:
      {
        short value = (short)(packet.getByte(1) << 8 | (short)packet.getByte(2) & 0xFF);
        byte msb = (byte)(value >> 15);
        if (msb > 0) {
          value -= 1;
          value = (short)~value;
          value*=-1;
        }
        
        value = (short)(value >> 5);
        double result = 0.0625 * value;
        String valueStr = new DecimalFormat("#0.0").format(result);        
        device.updatePropertyValue("TEMPCURRENT",  valueStr);
        break;
      }
      case TEMP_SETTINGS1:
      {
        device.updatePropertyValue("TEMPTARGET_CURRENT",  new DecimalFormat("##.#").format((double)packet.getByte(1) / 2));
        device.updatePropertyValue("TEMPTARGET_HEAT_COMFORT",  new DecimalFormat("##.#").format((double)packet.getByte(2) / 2));
        device.updatePropertyValue("TEMPTARGET_HEAT_DAY",  new DecimalFormat("##.#").format((double)packet.getByte(3) / 2));
        device.updatePropertyValue("TEMPTARGET_HEAT_NIGHT",  new DecimalFormat("##.#").format((double)packet.getByte(4) / 2));
        device.updatePropertyValue("TEMPTARGET_HEAT_SAFE",  new DecimalFormat("##.#").format((double)packet.getByte(5) / 2));
        break;      
      }
      case TEMP_SETTINGS2:
      {
        device.updatePropertyValue("TEMPTARGET_COOL_COMFORT",  new DecimalFormat("##.#").format((double)packet.getByte(1) / 2));
        device.updatePropertyValue("TEMPTARGET_COOL_DAY",  new DecimalFormat("##.#").format((double)packet.getByte(2) / 2));
        device.updatePropertyValue("TEMPTARGET_COOL_NIGHT",  new DecimalFormat("##.#").format((double)packet.getByte(3) / 2));
        device.updatePropertyValue("TEMPTARGET_COOL_SAFE",  new DecimalFormat("##.#").format((double)packet.getByte(4) / 2));
        break;
      }
      case PUSH_BUTTON_STATUS:
      {
        // Check if this is temp status
        if (isTempPacket(device, packet.getAddress())) {
          // Update temp status
          ChannelStatus[] status = new ChannelStatus[] {
            ChannelStatus.PRESSED,
            ChannelStatus.RELEASED
          };
          
          for (int i=0; i<2; i++) {
            byte stateByte = packet.getByte(i+1);
            
            if ((stateByte & 0x01) == 0x01) {
              device.updatePropertyValue("HEATER", status[i]); 
            }
            if ((stateByte & 0x02) == 0x02) {
              device.updatePropertyValue("BOOST", status[i]);
            }
            if ((stateByte & 0x04) == 0x04) {
              device.updatePropertyValue("PUMP", status[i]);
            }
            if ((stateByte & 0x08) == 0x08) {
              device.updatePropertyValue("COOLER", status[i]);
            }
            if ((stateByte & 0x10) == 0x10) {
              device.updatePropertyValue("TEMPALARM1", status[i]);
            }
            if ((stateByte & 0x20) == 0x20) {
              device.updatePropertyValue("TEMPALARM2", status[i]);
            }
            if ((stateByte & 0x40) == 0x40) {
              device.updatePropertyValue("TEMPALARM3", status[i]);
            }
            if ((stateByte & 0x80) == 0x80) {
              device.updatePropertyValue("TEMPALARM4", status[i]);
            }
          }
        } else {
          // Get start channel
          int startChannel = 1;
          
          for (int i=0; i<5; i++) {
            if (device.getAddresses()[i] == packet.getAddress()) {
              startChannel = (i*8) + 1;
              break;
            }
          }
          
          // Update each of the 8 input channels for this address
          int pressedByte = packet.getByte(1) & 0xFF;
          int releasedByte = packet.getByte(2) & 0xFF;
          int longPressedByte = packet.getByte(3) & 0xFF;
          
          for (int i=startChannel; i<startChannel+8; i++) {
            if ((pressedByte & 0x01) == 1) {
              device.updatePropertyValue("CHANNELSTATUS" + i, ChannelStatus.PRESSED);
            } else if ((releasedByte & 0x01) == 1) {
              device.updatePropertyValue("CHANNELSTATUS" + i, ChannelStatus.RELEASED);
            } else if ((longPressedByte & 0x01) == 1) {
              device.updatePropertyValue("CHANNELSTATUS" + i, ChannelStatus.LONG_PRESSED);
            }

            pressedByte = pressedByte >>> 1;
            releasedByte = releasedByte >>> 1;
            longPressedByte = longPressedByte >>> 1;
          }
        }
        break;
      }
      case MEMORY_BLOCK_DUMP:
      {
        Integer alarm1 = getAlarmMemoryLocation(1, device.getDeviceType());
        Integer alarm2 = getAlarmMemoryLocation(2, device.getDeviceType());
        int memoryLocation = ((packet.getByte(1) & 0xFF) << 8) | (packet.getByte(2) & 0xFF);
        int alarmNumber = 0;

        if (alarm1 != null && alarm1.intValue() == memoryLocation) {
          alarmNumber = 1;
        } else if (alarm2 != null && alarm2.intValue() == memoryLocation) {
          alarmNumber = 2;
        }

        if (alarmNumber > 0) {
          int wakeHours = packet.getByte(3) & 0xFF;
          int wakeMins = packet.getByte(4) & 0xFF;
          int bedHours = packet.getByte(5) & 0xFF;
          int bedMins = packet.getByte(6) & 0xFF;

          device.updatePropertyValue("ALARMHOURSWAKE" + alarmNumber, wakeHours);
          device.updatePropertyValue("ALARMMINUTESWAKE" + alarmNumber, wakeMins);
          device.updatePropertyValue("ALARMHOURSBED" + alarmNumber, bedHours);
          device.updatePropertyValue("ALARMMINUTESBED" + alarmNumber, bedMins);
          device.updatePropertyValue("ALARMTIMEWAKE" + alarmNumber, String.format("%02d:%02d", wakeHours, wakeMins));
          device.updatePropertyValue("ALARMTIMEBED" + alarmNumber, String.format("%02d:%02d", bedHours, bedMins));
        }
        break;
      }
      case LED_OFF:
      case LED_ON:
      case LED_FAST:
      case LED_SLOW:
      case LED_VERYFAST:
      {
        // Get start channel
        int startChannel = 1;
        
        for (int i=0; i<5; i++) {
          if (device.getAddresses()[i] == packet.getAddress()) {
            startChannel = (i*8) + 1;
            break;
          }
        }
        
        LedStatus ledStatus = LedStatus.valueOf(command.toString().substring(4));
        byte ledByte = packet.getByte(1);
        
        for (int i=0; i<8; i++) {
          if (((ledByte >> i) & 0x01) == 1) {
            device.updatePropertyValue("CHANNELLED" + startChannel + i, ledStatus);
          }
        }
        break;
      }
      case LED_STATUS:
      {
        // Get start channel
        int startChannel = 1;
        
        for (int i=0; i<5; i++) {
          if (device.getAddresses()[i] == packet.getAddress()) {
            startChannel = (i*8) + 1;
            break;
          }
        }
        
        byte onByte = packet.getByte(1);
        byte slowByte = packet.getByte(2);
        byte fastByte = packet.getByte(3);
        
        for (int i=0; i<8; i++) {
          boolean on = ((onByte >> i) & 0x01) == 1;
          boolean slow = ((slowByte >> i) & 0x01) == 1;
          boolean fast = ((fastByte >> i) & 0x01) == 1;
          if (on) {
            device.updatePropertyValue("CHANNELLED" + startChannel + i, LedStatus.ON);
          } else if (slow && fast) {
            device.updatePropertyValue("CHANNELLED" + startChannel + i, LedStatus.VERYFAST);
          } else if (slow) {
            device.updatePropertyValue("CHANNELLED" + startChannel + i, LedStatus.SLOW);
          } else if (fast) {
            device.updatePropertyValue("CHANNELLED" + startChannel + i, LedStatus.FAST);
          }
        }
        
        break;
      }
      case COUNTER_STATUS:
      {
        int channelNumber = (packet.getByte(1) & 0x03) + 1;
        int pulses = (packet.getByte(1) >> 2) * 100;
        int counter = ((packet.getByte(2) & 0xFF) << 24) + ((packet.getByte(3) & 0xFF) << 16) + ((packet.getByte(4) & 0xFF) << 8) + ((packet.getByte(5) & 0xFF) << 0);
        int period = ((packet.getByte(6) & 0xFF) << 8) + ((packet.getByte(7) & 0xFF) << 0);
        
        Object unitsObj = device.getPropertyValue("COUNTERUNITS" + channelNumber);
        CounterUnits units = null;
        if (unitsObj != null) {
          units = (CounterUnits)unitsObj;
        }
        
        boolean isElectric = units != null && units == CounterUnits.KILOWATTS;
        
        double value = ((double)counter / pulses);
        value = (double)Math.round(value*100) / 100;
        device.updatePropertyValue("COUNTER" + channelNumber, new DecimalFormat("#.###").format(value));
        
        double instant = (double)1000 * 3600 * (isElectric ? 1000 : 1);
        instant = instant / (period * pulses);
        instant = (double)Math.round(instant * 100) / 100;
        device.updatePropertyValue("COUNTERINSTANT" + channelNumber, new DecimalFormat(isElectric ? "#" : "#.###").format(instant));
        break;
      }
      case MEMORY_DATA:
      {
        if ((packet.getByte(1) & 0xFF) == 0x03 && (packet.getByte(2) & 0xFF) == 0xFE) {
          
          // Read Counter units
          int counterUnits = packet.getByte(3);
          CounterUnits[] counters = new CounterUnits[4];
          counters[0] = CounterUnits.fromCode(counterUnits & 0x03);
          counters[1] = CounterUnits.fromCode((counterUnits & 0x0C) >> 2);
          counters[2] = CounterUnits.fromCode((counterUnits & 0x30) >> 4);
          counters[3] = CounterUnits.fromCode((counterUnits & 0xC0) >> 6);
          
          // Put values directly into cache no sensors will be linked to these values
          device.updatePropertyValue("COUNTERUNITS1", counters[0]);
          device.updatePropertyValue("COUNTERUNITS2", counters[1]);
          device.updatePropertyValue("COUNTERUNITS3", counters[2]);
          device.updatePropertyValue("COUNTERUNITS4", counters[3]);

          // Try and update the counter instant values if any counter is a kilowatt counter
          for (int i=0; i<4; i++) {
            if (counters[i] == CounterUnits.KILOWATTS) {
              Object obj = device.getPropertyValue("COUNTERINSTANT" + (i+1));
              if (obj != null) {
                double val = Double.parseDouble((String)obj);
                val = val * 1000;
                device.updatePropertyValue("COUNTERINSTANT"+ (i+1), new DecimalFormat("#").format(val));
              }
            }
          }
        }
        break;
      }
      case METEO_STATUS:
      {
        int rainValue = Math.abs((packet.getByte(1) << 8) + packet.getByte(2));
        int light = Math.abs((packet.getByte(3) << 8)  + packet.getByte(4));
        int windValue = Math.abs((packet.getByte(5) << 8) + packet.getByte(6));
        double wind = 0.1 * windValue;
        double rain = 0.1 * rainValue;
        device.updatePropertyValue("COUNTERINSTANTRAIN", new DecimalFormat("#.#").format(rain));
        device.updatePropertyValue("COUNTERINSTANTLIGHT", new DecimalFormat("#").format(light));
        device.updatePropertyValue("COUNTERINSTANTWIND", new DecimalFormat("#.#").format(wind));
        break;
      }
      default:
        log.warn("Unknown command '" + packet.getCommand() + "' will be ignored");
        break;
    }
  }

  private int getMaxSubAddresses(VelbusDevice device) {
    VelbusDeviceType deviceType = device.getDeviceType();
    if (deviceType == VelbusDeviceType.VMBGPO || deviceType == VelbusDeviceType.VMBGPOD) {
      return 4;
    } else if (deviceType == VelbusDeviceType.VMBGP1 ||
        deviceType == VelbusDeviceType.VMBGP2 ||
        deviceType == VelbusDeviceType.VMBGP4) {
      return 1;
    }

    return 0;
  }

  private int getChannelCount(VelbusDevice device) {
    if (device.getDeviceType() == VelbusDeviceType.VMBGPO || device.getDeviceType() == VelbusDeviceType.VMBGPOD) {
      
      if (device.getAddresses().length == 5) {
        int channelCount = 0;
        for (int i=0; i<4; i++) {
          int address = device.getAddresses()[i];
          
          if(address != 255) {
            channelCount += 8;
          }
        }
        return channelCount;
      } else {
        // Assume all 32 channels are enabled
        return 32;
      }
    } else if (device.getDeviceType() == VelbusDeviceType.VMB1TS) {
      return 0;
    } else {
      return 8;
    }
  }
  
  @Override
  public boolean isInitialised(VelbusDevice device) {
    int channelCount = getChannelCount(device);

    // Check all sub addresses have been received
    int subAddressCount = getMaxSubAddresses(device);
    int[] addresses = device.getAddresses();

    for (int i=1; i<1+subAddressCount; i++) {
      if (addresses[i] == 0) {
        return false;
      }
    }

    // Check temperatures exist
    if (deviceSupportsTemperature(device) &&
            (!device.propertyExists("HEATER") ||
                    !device.propertyExists("TEMPMODE") ||
                    !device.propertyExists("TEMPCURRENT") ||
                    !device.propertyExists("TEMPTARGET_HEAT_COMFORT") ||
                    !device.propertyExists("TEMPTARGET_COOL_COMFORT"))) {
      return false;
    }
        
    // Check program properties exist in cache
    if (deviceSupportsPrograms(device)) {
      if (!device.propertyExists("PROGRAM")) {
        return false;
      }

      // Check alarm times exist
      if (getAlarmMemoryLocation(1, device.getDeviceType()) != null && (!device.propertyExists("ALARMHOURSWAKE1") || !device.propertyExists("ALARMHOURSBED1"))) {
        return false;
      }
      if (getAlarmMemoryLocation(2, device.getDeviceType()) != null && (!device.propertyExists("ALARMHOURSWAKE2") || !device.propertyExists("ALARMHOURSBED2"))) {
        return false;
      }
    }

    // Check meteo light, wind & rain
    if (device.getDeviceType() == VelbusDeviceType.VMBMETEO) {
      if (!device.propertyExists("COUNTERINSTANTRAIN") || !device.propertyExists("COUNTERINSTANTWIND") || !device.propertyExists("COUNTERINSTANTLIGHT")) {
        return false;
      }
    }

    // Can't check the following because if channel counter disabled we get no response
//    if (device.getDeviceType() == VelbusDeviceType.VMB7IN) {
//      // Check counters for each channel
//      for (int i=1; i<5; i++) {
//        if (!cache.containsKey("counter" + i)) {
//          return false;
//        }
//      }
//    }

    return true;
  }

  private boolean deviceSupportsPrograms(VelbusDevice device) {
    return device.getDeviceType() != VelbusDeviceType.VMB1TS;
  }

  private boolean deviceSupportsTemperature(VelbusDevice device) {
    VelbusDeviceType deviceType = device.getDeviceType();
    
    if (deviceType != VelbusDeviceType.VMBGP1 && 
        deviceType != VelbusDeviceType.VMBGP2 &&
        deviceType != VelbusDeviceType.VMBGP4 &&
        deviceType != VelbusDeviceType.VMBGPO &&
        deviceType != VelbusDeviceType.VMBGPOD &&
        deviceType != VelbusDeviceType.VMB1TS) {
      return false;
    }
    
    if ((deviceType == VelbusDeviceType.VMBGPO || deviceType == VelbusDeviceType.VMBGPOD) && device.getAddresses().length == 5) {
      return device.getAddresses()[4] != 255;
    }
    
    if ((deviceType == VelbusDeviceType.VMBGP1 ||
        deviceType == VelbusDeviceType.VMBGP2 ||
        deviceType == VelbusDeviceType.VMBGP4)) {
      return device.getAddresses()[1] != 255;
    }
    
    return true;
  }
  
  private boolean isTempPacket(VelbusDevice device, int address) {
    VelbusDeviceType deviceType = device.getDeviceType();
    
    return ((deviceType == VelbusDeviceType.VMBGP1 || 
            deviceType == VelbusDeviceType.VMBGP2 ||
            deviceType == VelbusDeviceType.VMBGP4 || 
            deviceType == VelbusDeviceType.VMBGP4PIR) &&
            address == device.getAddresses()[1]) ||
            ((deviceType == VelbusDeviceType.VMBGPO || deviceType == VelbusDeviceType.VMBGPOD) &&
            address == device.getAddresses()[4]);      
  }
  
  private Integer getAlarmMemoryLocation(int alarmNumber, VelbusDeviceType deviceType) {
    Integer location = null;
    
    if (deviceType == VelbusDeviceType.VMBGPO || deviceType == VelbusDeviceType.VMBGPOD) {
      location = alarmNumber == 2 ? 0x0289 : 0x0285;
    } else if (deviceType == VelbusDeviceType.VMB6PBN || deviceType == VelbusDeviceType.VMB8PBU || deviceType == VelbusDeviceType.VMB7IN) {
      location = alarmNumber == 2 ? 0x0098 : 0x0094;
    } else if (deviceType == VelbusDeviceType.VMBGP1 ||
            deviceType == VelbusDeviceType.VMBGP2 || 
            deviceType == VelbusDeviceType.VMBGP4 || 
            deviceType == VelbusDeviceType.VMBGP4PIR) {
      location = alarmNumber == 2 ? 0x00A9 : 0x00A5;
    } else if (deviceType == VelbusDeviceType.VMBPIRC || 
            deviceType == VelbusDeviceType.VMBPIRO ||
            deviceType == VelbusDeviceType.VMBPIRM) {
      location = alarmNumber == 2 ? 0x0036 : 0x0032;
    } else if (deviceType == VelbusDeviceType.VMBMETEO) {
      location = alarmNumber == 2 ? 0x0088 : 0x0084;
    }
    
    return location;
  }
}
