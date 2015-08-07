package org.openremote.controller.protocol.velbus.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.velbus.PacketRequestCommand;
import org.openremote.controller.protocol.velbus.PacketResponseCommand;
import org.openremote.controller.protocol.velbus.VelbusCommand;
import org.openremote.controller.protocol.velbus.VelbusCommandBuilder;
import org.openremote.controller.protocol.velbus.VelbusDevice;
import org.openremote.controller.protocol.velbus.VelbusDeviceProcessor;
import org.openremote.controller.protocol.velbus.VelbusDeviceType;
import org.openremote.controller.protocol.velbus.VelbusPacket;
import org.openremote.controller.protocol.velbus.VelbusReadCommand;
import org.openremote.controller.protocol.velbus.VelbusWriteCommand;
import org.openremote.controller.protocol.velbus.VelbusCommand.Action;
import org.openremote.controller.protocol.velbus.processor.DimmerDeviceProcessor.ChannelSetting;
import org.openremote.controller.protocol.velbus.processor.VelbusDeviceProcessorImpl.LedStatus;

public class BlindDeviceProcessor extends VelbusDeviceProcessorImpl {

  enum ChannelStatus {
    HALT,
    UP,
    DOWN
  }
  
  enum ChannelSetting {
    NORMAL(0x00),
    INHIBITED(0x01),
    INHIBITED_DOWN(0x02),
    INHIBITED_UP(0x03),
    FORCED_DOWN(0x04),
    FORCED_UP(0x05),
    LOCKED(0x06);
     
    private int code;
    
    private ChannelSetting(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    public static ChannelSetting fromCode(int code) {
      for (ChannelSetting type : ChannelSetting.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return NORMAL;
    }
  }
  
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  
  @Override
  public Iterable<VelbusPacket> getStatusRequestPackets(VelbusDevice device) {
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MODULE_STATUS.getCode(), (byte)0x03));
    return packets;
  }

  @Override
  public Iterable<VelbusPacket> processWriteCommand(VelbusDevice device, VelbusWriteCommand command) {
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    
    try {
      // Check which channel they want
      String[] params = command.getValue().split(":");
      int channelNumber = Integer.parseInt(params[0]);

      byte channelByte = (byte)(1 << (channelNumber - 1));
      
      if (command.getAction() == Action.BLIND_POSITION) {
        // Look for pos value
        int pos = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : -1;
        
        if (command.getParameter() != null) {
          // Use command parameter value for new target
          try {
            pos = Integer.parseInt(command.getParameter());
          } catch (NumberFormatException e) {
            log.error("Invalid dynamic value supplied", e);
          }
        }
        
        if (pos >= 0) {
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_POSITION.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)pos));
        }
      } else if (command.getAction() == Action.UP) {
        int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0x000000; // FFFFFF locks it permanently
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_UP.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)time));
      } else if (command.getAction() == Action.DOWN) {
        int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0x000000; // FFFFFF locks it permanently
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_DOWN.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)time));        
      } else if (command.getAction() == Action.HALT) {
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_HALT.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
      } else if (command.getAction() == Action.LOCK) {
        // Look for time value
        int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_CHANNEL_LOCK.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)time));
      } else if (command.getAction() == Action.UNLOCK) {
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.BLIND_CHANNEL_UNLOCK.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
      }
    } catch (Exception e) {
      log.error("Invalid Command value '" + command.getValue() + "' for '" + command.getAction() + "' command");
    }
    
    return packets;
  }

  @Override
  public void processResponse(VelbusDevice device, VelbusPacket packet) {
    PacketResponseCommand command = PacketResponseCommand.fromCode(packet.getCommand());
    
    switch(command)
    {
      case BLIND_STATUS:
      {
        // Extract channel info
        int channelNumber = (int)(packet.getByte(1) & 0xFF);
 
        int blindPos = packet.getByte(5) & 0xFF;
        int statusValue = packet.getByte(3) & 0xFF;
        ChannelStatus status = statusValue == 0 ? ChannelStatus.HALT : statusValue == 1 ? ChannelStatus.UP : ChannelStatus.DOWN;
        LedStatus ledStatus = LedStatus.fromCode(packet.getByte(4) & 0xFF);
        ChannelSetting setting = ChannelSetting.fromCode(packet.getByte(6) & 0xFF);
        String lock = getOnOff(setting == ChannelSetting.LOCKED);
        
        // Push to device cache
        device.updatePropertyValue("channelStatus" + channelNumber, status);
        device.updatePropertyValue("channelSetting" + channelNumber, setting);
        device.updatePropertyValue("channelLocked" + channelNumber, lock);
        device.updatePropertyValue("channelLed" + channelNumber, ledStatus);
        
        if (device.getDeviceType() == VelbusDeviceType.VMB2BLE) {
          device.updatePropertyValue("channelPosition" + channelNumber, blindPos);
        }
        break;
      }
      case PUSH_BUTTON_STATUS:
      {
        // Don't need to react to this because a relay status command is always sent 
//        // Update each of the blind channels
//        int byte1 = packet.getByte(1) & 0xFF;
//        int byte2 = packet.getByte(2) & 0xFF;
//        
//        if (device.getDeviceType() == VelbusDeviceType.VMB1BL) {
//          if ((byte1 & 0x03) == 1 && (byte2 & 0x03) == 0) {
//            updateValue(device, "channelStatus1", ChannelStatus.UP);
//          } else if ((byte1 & 0x03) == 0 && (byte2 & 0x03) >= 1) {
//            updateValue(device, "channelStatus1", ChannelStatus.OFF);
//          } else if ((byte1 & 0x03) == 2 && (byte2 & 0x03) == 0) {
//            updateValue(device, "channelStatus1", ChannelStatus.DOWN);
//          }
//        } else {
//          if ((byte1 & 0x03) == 1 && (byte2 & 0x03) == 0) {
//            updateValue(device, "channelStatus1", ChannelStatus.UP);
//          } else if ((byte1 & 0x03) == 0 && (byte2 & 0x03) >= 1) {
//            updateValue(device, "channelStatus1", ChannelStatus.OFF);
//          } else if ((byte1 & 0x03) == 2 && (byte2 & 0x03) == 0) {
//            updateValue(device, "channelStatus1", ChannelStatus.DOWN);
//          }
//          
//          if ((byte1 & 0x12) == 4 && (byte2 & 0x12) == 0) {
//            updateValue(device, "channelStatus2", ChannelStatus.UP);
//          } else if ((byte1 & 0x12) == 0 && (byte2 & 0x12) >= 4) {
//            updateValue(device, "channelStatus2", ChannelStatus.OFF);
//          } else if ((byte1 & 0x12) == 8 && (byte2 & 0x12) == 0) {
//            updateValue(device, "channelStatus2", ChannelStatus.DOWN);
//          }
        break;
      }
      default:
        log.warn("Unkown command '" + packet.getCommand() + "' will be ignored");
        break;
    }
  }

  @Override
  public boolean isInitialised(VelbusDevice device) {
    int[] channels = device.getDeviceType() == VelbusDeviceType.VMB1BL ? new int[] {3} : new int[] {1,2};
    boolean initialised = true;

    for (int i : channels) {
      if (!device.propertyExists("CHANNELSTATUS" + i)) {
        initialised = false;
        break;
      }
    }
    
    return initialised;
  }

  @Override
  public String getDeviceCachePropertyName(VelbusReadCommand command) {
    String propertyName = null;
    
    try {
      String commandValue = command.getValue().toUpperCase();
      
      // Check which channel they want
      int channelNumber = Integer.parseInt(commandValue);
      String prefix = null;
      
      if (command.getAction() == Action.STATUS) {
        prefix = "channelStatus";
      } else if (command.getAction() == Action.SETTING_STATUS) {
        prefix = "channelSetting";
      } else if (command.getAction() == Action.LOCK_STATUS) {
        prefix = "channelLocked";
      } else if (command.getAction() == Action.LED_STATUS) {
        prefix = "channelLed";
      } else if (command.getAction() == Action.POSITION_STATUS) {
        prefix = "channelPosition";
      }
            
      if (prefix != null) {
        propertyName = prefix + channelNumber;
      }
    } catch (NumberFormatException e) {
      log.error("Invalid Command value '" + command.getValue() + "' for '" + command.getAction() + "' command");
    }
    
    return propertyName;
  }
}
