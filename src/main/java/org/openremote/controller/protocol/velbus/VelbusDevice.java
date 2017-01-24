package org.openremote.controller.protocol.velbus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;

public class VelbusDevice implements VelbusDevicePacketCallback {
  private class ReadCommandSensorPair
  {
    ReadCommandSensorPair(VelbusReadCommand command, Sensor sensor) {
      this.command = command;
      this.sensor = sensor;
    }
    
    VelbusReadCommand command;
    Sensor sensor;
  }
  
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
  public static final String DEFAULT_RESPONSE = "N/A";
  private boolean initialising;
  private boolean timedout = false;
  private VelbusConnection connection;
  private int[] addresses = new int[5]; // Main address plus up to 4 sub addresses
  private VelbusDeviceType deviceType = null;
  private List<ReadCommandSensorPair> readQueue = new ArrayList<ReadCommandSensorPair>();
  private Map<String, Object> deviceCache = new HashMap<String, Object>();
  private VelbusDeviceProcessor processor;
  private Map<String, List<Sensor>> sensorMap = new HashMap<String, List<Sensor>>();
  
  VelbusDevice(int address, VelbusConnection connection) {
    this.addresses[0] = address;
    this.connection = connection;
    initialising = true;
    timedout = false;
    
    if (this.connection != null) {
      // Add packet handler
      this.connection.addDevicePacketHandler(this);
    }
  }
  
  public boolean isInitialised() {
    return initialising == false;
  }
  
  void setTimedout() {
    timedout = true;
  }
  
  synchronized void addSensor(VelbusReadCommand command, Sensor sensor) {
    if (timedout) {
      log.info("Device is not reachable so not adding sensor");
      return;
    }

    if (!initialising && deviceType == VelbusDeviceType.UNKNOWN) {
      log.debug("Cannot add sensor as device type is unknown");
      return;
    }
    
    if (!initialising) {
      processReadCommand(command, sensor);
    } else {
      // Queue the sensor
      readQueue.add(new ReadCommandSensorPair(command, sensor));
    }   
  }
  
  synchronized void removeSensor(VelbusReadCommand command, Sensor sensor) {
    for(Entry<String, List<Sensor>> entry : sensorMap.entrySet())
    {
      if (entry.getValue().remove(sensor))
      {
        break;
      }
    }
  }
  
  void write(VelbusWriteCommand command) {
    if (timedout) {
      log.info("Device is not reachable so cannot process command");
      return;
    }
    
    if (!initialising) {
      processWriteCommand(command);
    }
  }
   
  public VelbusDeviceType getDeviceType() {
    return deviceType;
  }
  
  public int[] getAddresses() {
    return addresses;
  }
  
  private Map<String, Object> getDeviceCache() {
    return deviceCache;
  }
  
  public Map<String, List<Sensor>> getSensorMap() {
    return sensorMap;
  }
  
  @Override
  public void onPacketReceived(VelbusPacket packet) {
    if (timedout) {
      return;
    }
    
    processReceivedPacket(packet);
  }
    
  private VelbusDeviceProcessor getProcessor() {
    if (processor == null) {
      try {
        processor = deviceType != null ? deviceType.getProcessor() : null;
      } catch (Exception e) {
        log.error(e);
      }
    }
    
    return processor;
  }
  
  private void processReadCommand(VelbusReadCommand command, Sensor sensor) {
    VelbusDeviceProcessor processor = getProcessor();
    if (processor != null) {
      String propertyName = processor.getDeviceCachePropertyName(command);
      if (propertyName != null) {
        propertyName = propertyName.toUpperCase();

        Map<String, List<Sensor>> sensorMap = getSensorMap();
        List<Sensor> sensors = sensorMap.get(propertyName);
        
        if (sensors == null) {
          sensors = new ArrayList<Sensor>();
          sensorMap.put(propertyName, sensors);
        }
        
        sensors.add(sensor);
        
        // Update the value of the sensor with value from cache
        Object currentValue = getPropertyValue(propertyName);
        updateSensor(sensor, currentValue != null ? currentValue.toString() : "N/A"); 
      }
    }
  }
  
  private void processWriteCommand(VelbusWriteCommand command) {
    VelbusDeviceProcessor processor = getProcessor();
    if (processor != null) {
      Iterable<VelbusPacket> packets = processor.processWriteCommand(this, (VelbusWriteCommand)command);
      if (packets != null) {
        try {
          for (VelbusPacket packet : packets) {
            connection.send(packet);
          }
        } catch (ConnectionException e) {
          // Something wrong with the connection
          log.error(e);
        }
      }
    }
  }
  
  public void sendPacket(VelbusPacket packet) {
    if (packet != null) {
      try {
          connection.send(packet);
      } catch (ConnectionException e) {
        // Something wrong with the connection
        log.error(e);
      }
    }
  }
  
  /**
   * Takes a packet received from the velbus network linked to this specific
   * device; this method processes the packet and updates the state cache
   * @param packet
   */
  private void processReceivedPacket(VelbusPacket packet) {
    PacketResponseCommand packetCommand = PacketResponseCommand.fromCode(packet.getCommand());
    
    switch (packetCommand)
    {
      case UNKNOWN:
      {
        //log.debug("Unkown command received from bus '" + packet.getCommand() + "'");
        break;
      }
      case MODULE_TYPE:
      {
        synchronized(this) {
          log.debug("Module type received");
          int typeCode = packet.getByte(1) & 0xFF;
          deviceType = VelbusDeviceType.fromCode(typeCode);
          log.debug("Device type '" + hexArray[typeCode >>> 4] + hexArray[typeCode & 0x0F] + "' is '" + deviceType.name() + "'");
        
          if (deviceType == VelbusDeviceType.UNKNOWN) {
            connection.removeDevicePacketHandler(this);
            return;
          }
        }
        
        if (!deviceType.hasSubtype()) {
          sendDeviceStatusRequest();
        }
        break;
      }        
      case MODULE_SUBTYPE:
      {
        // Extract sub addresses
        for (int i=4; i<packet.getDataSize() && i<8; i++) {
          addresses[i-3] = packet.getByte(i) & 0xFF;
        }
        sendDeviceStatusRequest();
        break;
      }
      default:
      {
        // Let device processor handle the packet
        VelbusDeviceProcessor processor = getProcessor();
        if (processor != null) {
          processor.processResponse(this, packet);
          
          if (initialising) {
            // Check if device is initialised now
            initialising = !processor.isInitialised(this);
            
            if (!initialising) {
              log.debug("Device '" + this.getAddresses()[0] + "' is initialised");
              
              // Process queued read commands
              synchronized(this) {
                for (ReadCommandSensorPair commandSensorPair : readQueue) {
                  processReadCommand(commandSensorPair.command, commandSensorPair.sensor);
                }              
              }
            }
          } 
        }
      }
    }
  }
  
  /** 
   * Requests the status of the device; packet to send
   * is device type specific
   */
  private void sendDeviceStatusRequest() {
    VelbusDeviceProcessor processor = getProcessor();
    
    if (processor != null) {    
      Iterable<VelbusPacket> packets = processor.getStatusRequestPackets(this);
      
      if (packets == null) {
        log.debug("Device Processor '" + processor.getClass() + "' returned no status request packets");
        return;
      }
      
      try {
        for (VelbusPacket packet : packets) {
          connection.send(packet);
        }
      } catch (ConnectionException e) {
        // Something wrong with the connection
        log.error(e);
      }
    }
  }
  
  public synchronized void updatePropertyValue(String propertyName, Object value) {
    propertyName = propertyName.toUpperCase();
    Map<String,Object> cache = getDeviceCache();
    cache.put(propertyName, value);

    // Update sensors
    updateSensors(propertyName, value);
  }
  
  public Object getPropertyValue(String propertyName) {
    if (propertyName != null && !propertyName.isEmpty()) {
      propertyName = propertyName.toUpperCase();
      Map<String,Object> cache = getDeviceCache();
      return cache.get(propertyName);
    }
    
    return null;
  }
  
  public boolean propertyExists(String propertyName) {
    if (propertyName != null) {
      propertyName = propertyName.toUpperCase();
      return deviceCache.containsKey(propertyName);
    }
    
    return false;
  }
  
  private void updateSensors(String propertyName, Object value) {
    Map<String, List<Sensor>> sensorMap = getSensorMap();
    List<Sensor> sensors = sensorMap.get(propertyName);
    String sensorValue = value != null ? value.toString() : "N/A";
    
    if (sensors != null) {
      for (Sensor sensor : sensors) {
        updateSensor(sensor, sensorValue);
      }
    }
  }
  
  private void updateSensor(Sensor sensor, String sensorValue) {
    if (sensor == null) {
      return;
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
