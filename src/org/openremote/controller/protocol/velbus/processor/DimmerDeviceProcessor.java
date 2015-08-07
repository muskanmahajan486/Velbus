package org.openremote.controller.protocol.velbus.processor;

import java.util.ArrayList;
import java.util.EnumMap;
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
import org.openremote.controller.protocol.velbus.processor.InputDeviceProcessor.ChannelStatus;
import org.openremote.controller.protocol.velbus.processor.RelayDeviceProcessor.ChannelSetting;
import org.openremote.controller.protocol.velbus.processor.VelbusDeviceProcessorImpl.LedStatus;

public class DimmerDeviceProcessor extends VelbusDeviceProcessorImpl {
  enum ChannelStatus {
    ON,
    OFF
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
    byte channelRequest = device.getDeviceType() == VelbusDeviceType.VMB4DC ? (byte)0x0F : (byte)0x01;
    packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.MODULE_STATUS.getCode(), channelRequest));
    return packets;
  }

  @Override
  public String getDeviceCachePropertyName(VelbusReadCommand command) {
    String propertyName = null;
    
    try {
      // Check which channel they want
      String commandValue = command.getValue().toUpperCase();
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
      } else if (command.getAction() == Action.DIMMER_STATUS) {
        prefix = "channelLevel";
      }
            
      if (prefix != null) {
        propertyName = prefix + channelNumber;
      }
    } catch (NumberFormatException e) {
      log.error("Invalid Command value '" + command.getValue() + "' for '" + command.getAction() + "' command");
    }
    
    return propertyName;
  }

  @Override
  public Iterable<VelbusPacket> processWriteCommand(VelbusDevice device, VelbusWriteCommand command) {
    List<VelbusPacket> packets = new ArrayList<VelbusPacket>();
    
    try {
      // Check which channel they want
      String[] params = command.getValue().split(":");
      int channelNumber = Integer.parseInt(params[0]);

      byte channelByte = (byte)(1 << (channelNumber - 1));
      
      if (command.getAction() == Action.ON) {
        // Look for time value
        int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.DIM_ON_TIMER.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)(time)));
      } else if (command.getAction() == Action.OFF) {
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.SET_DIM.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)0x00, (byte)0x00, (byte)0x00));
      } else if (command.getAction() == Action.DIMMER_LEVEL) {
        int level = Integer.parseInt(params[1].trim());
        int duration = params.length == 3 && params[2] != null && !params[2].isEmpty() ? Integer.parseInt(params[2]) : 0x01;
        
        if (command.getParameter() != null) {
          // Use command parameter value for new target
          try {
            level = Integer.parseInt(command.getParameter());
          } catch (NumberFormatException e) {
            log.error("Invalid dynamic value supplied", e);
          }
        }
        
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.SET_DIM.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)level, (byte)(duration >> 8), (byte)duration));        
      } else if (command.getAction() == Action.HALT) {
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.STOP_DIM.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
      } else if (command.getAction() == Action.LOCK) {
        // Look for time value
        int time = params.length == 2 && params[1] != null && !params[1].isEmpty() ? Integer.parseInt(params[1]) : 0xFFFFFF; // FFFFFF locks it permanently
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.LOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte, (byte)(time >> 16), (byte)(time >> 8), (byte)time));
      } else if (command.getAction() == Action.UNLOCK) {
        packets.add(new VelbusPacket(device.getAddresses()[0], PacketRequestCommand.UNLOCK_CHANNEL.getCode(), VelbusPacket.PacketPriority.HIGH, channelByte));
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
      case DIMMER_STATUS:
      case DIMMER_STATUS2:
      {
        // Extract channel info
        // DIMMER_STATUS command is only used on 1 channel dimmer modules
        int channelNumber = command == PacketResponseCommand.DIMMER_STATUS ? 1 : (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1; 
        
        ChannelSetting setting = command == PacketResponseCommand.DIMMER_STATUS ? ChannelSetting.NORMAL : ChannelSetting.fromCode(packet.getByte(2) & 0x03);
        int value = command == PacketResponseCommand.DIMMER_STATUS ? packet.getByte(2) & 0xFF : packet.getByte(3) & 0xFF;
        ChannelStatus status = value > 0 ? ChannelStatus.ON : ChannelStatus.OFF;
        LedStatus ledStatus = command == PacketResponseCommand.DIMMER_STATUS ? LedStatus.fromCode(packet.getByte(3) & 0xFF) : LedStatus.fromCode(packet.getByte(4) & 0xFF);
        String lock = getOnOff(setting == ChannelSetting.DISABLED);
        
        // Push to device cache
        device.updatePropertyValue("channelStatus" + channelNumber, status);
        device.updatePropertyValue("channelLevel" + channelNumber, value);
        device.updatePropertyValue("channelSetting" + channelNumber, setting);
        device.updatePropertyValue("channelLocked" + channelNumber, lock);
        device.updatePropertyValue("channelLed" + channelNumber, ledStatus);
        break;
      }
      case DIMMER_SLIDER_STATUS:
      {
        int channelNumber = (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1;
        device.updatePropertyValue("channelLevel" + channelNumber, packet.getByte(2) & 0xFF);
        break;
      }
      case PUSH_BUTTON_STATUS:
      {
        // Update each of the dimmer channels
        int onByte = packet.getByte(1) & 0xFF;
        int offByte = packet.getByte(2) & 0xFF;
       
        for (int i=1; i<5; i++) {
          if ((onByte & 0x01) == 1) {
            device.updatePropertyValue("channelStatus" + i, ChannelStatus.ON);
          } else if ((offByte & 0x01) == 1) {
            device.updatePropertyValue("channelStatus" + i, ChannelStatus.OFF);
          }

          onByte = onByte >>> 1;
          offByte = offByte >>> 1;
        }
        break;
      }
      default:
        log.warn("Unkown command '" + packet.getCommand() + "' will be ignored");
        break;
    }
  }

  @Override
  public boolean isInitialised(VelbusDevice device) {
    int channelCount = device.getDeviceType() == VelbusDeviceType.VMB4DC ? 4 : 1;
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
