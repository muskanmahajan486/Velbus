package org.openremote.controller.protocol.velbus;

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;

/**
 * Velbus command for reading data from the bus and updating associated sensors
 * @author Richard Turner
 *
 */
public class VelbusReadCommand extends VelbusCommand implements EventListener {

  VelbusReadCommand(VelbusConnectionManager connectionManager, int address, Action action, String value) {
    super(connectionManager, address, action, value);
  }
  
//  /*
//   * Only here for testing
//   */
//  String read() {
//    String response = "N/A";
//    
//    if (connectionManager.getConnectionStatus() != ConnectionStatus.CONNECTED)
//    {
//      log.info("No Velbus connection available, cannot read from bus");
//    }
//    else
//    {
//      response = connectionManager.read(this);  
//    }
//    
//    return response;    
//  }

  @Override
  public void setSensor(Sensor sensor) {
    connectionManager.addSensor(this, sensor);
  }

  @Override
  public void stop(Sensor sensor) {
    connectionManager.removeSensor(this, sensor);
  }
}
