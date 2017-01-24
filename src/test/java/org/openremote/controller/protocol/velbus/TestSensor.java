package org.openremote.controller.protocol.velbus;

import java.util.Map;

import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.Event;
import org.openremote.controller.protocol.EventProducer;
import org.openremote.controller.statuscache.StatusCache;

public class TestSensor extends Sensor {
  protected TestSensor(String name, int sensorID, StatusCache cache, EventProducer eventProducer,
          Map<String, String> sensorProperties, EnumSensorType sensorType) {
    super(name, sensorID, cache, eventProducer, sensorProperties, sensorType);
    
  }

  public String value;
  
  @Override
  protected Event processEvent(String value) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public void update(String state) {
    value = state;
  }
}
