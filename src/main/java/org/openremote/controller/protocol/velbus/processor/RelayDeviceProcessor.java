package org.openremote.controller.protocol.velbus.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.velbus.PacketRequestCommand;
import org.openremote.controller.protocol.velbus.PacketResponseCommand;
import org.openremote.controller.protocol.velbus.VelbusCommand.Action;
import org.openremote.controller.protocol.velbus.VelbusCommandBuilder;
import org.openremote.controller.protocol.velbus.VelbusDevice;
import org.openremote.controller.protocol.velbus.VelbusDeviceType;
import org.openremote.controller.protocol.velbus.VelbusPacket;
import org.openremote.controller.protocol.velbus.VelbusReadCommand;
import org.openremote.controller.protocol.velbus.VelbusWriteCommand;

public class RelayDeviceProcessor extends VelbusDeviceProcessorImpl {
  enum ChannelStatus {
    OFF(0x00),
    ON(0x01),
    TIMER(0x03);
     
    private int code;
    
    private ChannelStatus(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    public static ChannelStatus fromCode(int code) {
      for (ChannelStatus type : ChannelStatus.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return OFF;
    }
  }
  
  enum ChannelSetting {
    NORMAL(0x00),
    INHIBITED(0x01),
    FORCED(0x02),
    DISABLED(0x03);
     
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
    
    // Relay Status Request message
    packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MODULE_STATUS.getCode(), (byte)0x1F));
    return packets;
  }

  @Override
  public String getDeviceCachePropertyName(VelbusReadCommand command) {
    String propertyName = null;
    String commandValue = command.getValue() != null ? command.getValue().toUpperCase() : null;
    
    try {
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
      }
      
      if (prefix != null) {
        propertyName = prefix + channelNumber;
      }
    } catch (NumberFormatException e) {
      log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
    }
    
    return propertyName;
  }

  @Override
  public Iterable<VelbusPacket> processWriteCommand(VelbusDevice device, VelbusWriteCommand command) {
    int channelCount = device.getDeviceType() == VelbusDeviceType.VMB1RY ? 1 : 5;
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    String commandValue = command.getValue() != null ? command.getValue().toUpperCase() : null;
    
    try {
      // Check which channel they want
      String[] params = commandValue.split(":");
      int channelNumber = Integer.parseInt(params[0]);
      
      // Check index is in bounds
      if (channelNumber < 1 || channelNumber > channelCount) {
        log.error("Invalid channel number requested");
      } else {
        byte channelByte = (byte)Math.pow(2, channelNumber - 1);
        
        if (command.getAction() == Action.ON) {
          // Look for time value
          int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.RELAY_ON_TIMER.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)(time)));
        } else if (command.getAction() == Action.OFF) {
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.RELAY_OFF.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
        } else if (command.getAction() == Action.LOCK) {
          // Look for time value
          int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.LOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)time));
        } else if (command.getAction() == Action.UNLOCK) {
          packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.UNLOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
        }
      }
    } catch (Exception e) {
      log.error("Invalid Command value '" + commandValue + "' for '" + command.getAction() + "' command");
    }
    
    return packets;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processResponse(VelbusDevice device, VelbusPacket packet) {
    PacketResponseCommand command = PacketResponseCommand.fromCode(packet.getCommand());
    switch (command)
    {
      case RELAY_STATUS:
      {
        // Extract channel info
        int channelNumber = (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1;

        ChannelStatus status = device.getDeviceType() == VelbusDeviceType.VMB1RY ? (packet.getByte(3) & 0xFF) == 0x11 ? ChannelStatus.TIMER : ChannelStatus.fromCode(packet.getByte(3) & 0xFF) : ChannelStatus.fromCode(packet.getByte(3) & 0xFF);
        ChannelSetting setting = device.getDeviceType() == VelbusDeviceType.VMB1RY ? ChannelSetting.NORMAL : ChannelSetting.fromCode(packet.getByte(2) & 0xFF);
        String lock = getOnOff(setting == ChannelSetting.DISABLED);
        LedStatus ledStatus = device.getDeviceType() == VelbusDeviceType.VMB1RY ? null : LedStatus.fromCode(packet.getByte(4) & 0xFF);
        
        // Push to device cache
        device.updatePropertyValue("channelStatus" + channelNumber, status);
        device.updatePropertyValue("channelSetting" + channelNumber, setting);
        device.updatePropertyValue("channelLocked" + channelNumber, lock);
        device.updatePropertyValue("channelLed" + channelNumber, ledStatus);        
        break;
      }
      case PUSH_BUTTON_STATUS:
      {
        // Don't need to react to this because a relay status command is always sent 
        break;
      }
    default:
      log.warn("Unkown command '" + packet.getCommand() + "' will be ignored");
      break;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isInitialised(VelbusDevice device) {
    int channelCount = device.getDeviceType() == VelbusDeviceType.VMB1RY ? 1 : 5;
    boolean initialised = true;
    
    for (int i=1; i<=channelCount; i++) {
      if (!device.propertyExists("CHANNELSTATUS" + i)) {
        initialised = false;
        break;
      }
    }

    return initialised;
  }
}
