package org.openremote.controller.protocol.velbus.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.protocol.velbus.VelbusCommand;
import org.openremote.controller.protocol.velbus.VelbusCommandBuilder;
import org.openremote.controller.protocol.velbus.VelbusDevice;
import org.openremote.controller.protocol.velbus.VelbusDeviceProcessor;
import org.openremote.controller.protocol.velbus.VelbusReadCommand;
import org.openremote.controller.protocol.velbus.VelbusWriteCommand;

/**
 * Base class for device processors; deals with updating device cache and
 * raising property change notification
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public abstract class VelbusDeviceProcessorImpl implements VelbusDeviceProcessor {
  enum LedStatus {
    OFF(0x00),
    ON(0x80),
    SLOW(0x40),
    FAST(0x20),
    VERYFAST(0x10);
     
    private int code;
    
    private LedStatus(int code) {
      this.code = code;
    }
    
    public int getCode() {
      return this.code;
    }
    
    public static LedStatus fromCode(int code) {
      for (LedStatus type : LedStatus.values()) {
        if (type.getCode() == code) {
          return type;
        }
      }
      
      return OFF;
    }
  }
  
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  
  protected String getOnOff(int value) {
    return value == 1 ? "ON" : "OFF";
  }
  
  protected String getOnOff(boolean value) {
    return value ? "ON" : "OFF";
  }
   
//  public void processReadCommand(VelbusDevice device, VelbusReadCommand command, Sensor sensor) {
//    // Determine device cache property name
//    String propertyName = getDeviceCachePropertyName(command);
//    
//    if (propertyName != null && !propertyName.isEmpty()) {
//      propertyName = propertyName.toUpperCase();
//      synchronized (device) {
//        Map<String, List<Sensor>> sensorMap = device.getSensorMap();
//        List<Sensor> sensors = sensorMap.get(propertyName);
//        
//        if (sensors == null) {
//          sensors = new ArrayList<Sensor>();
//          sensorMap.put(propertyName, sensors);
//        }
//        
//        sensors.add(sensor);
//        
//        // Update the value of the sensor with value from cache
//        Object currentValue = device.getDeviceCache().get(propertyName);
//        updateSensor(sensor, currentValue != null ? currentValue.toString() : "N/A");
//      } 
//    }
//  }

  /**
   * Get the property name that the processor will use to store the value for this command
   * @param command
   * @return
   */
  public abstract String getDeviceCachePropertyName(VelbusReadCommand command);
}
