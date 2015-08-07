package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;

public class VelbusNetwork {
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  private int id;
  private String interfaceAddress;
  private int interfacePort;
  private VelbusConnection connection;
  private VelbusConnectionManager connectionManager;
  private boolean initialised;
  
  VelbusNetwork(int id, String interfaceAddress, int interfacePort, VelbusConnection connection) {
    this.id = id;
    this.interfaceAddress = interfaceAddress;
    this.interfacePort = interfacePort;
    this.connection = connection;
  }
  
  String getInterfaceAddress() {
    return interfaceAddress;
  }
  int getInterfacePort() {
    return interfacePort;
  }
  VelbusConnection getConnection() {
    return connection;
  }
  VelbusConnectionManager getConnectionManager() {
    return connectionManager;
  }

  boolean isInitialised() {
    return initialised;
  }
  
  int getId() {
    return id;
  }
  
  void initialise() {
    if (!initialised) {
      log.debug("Initialising network ID " + getId());
      connectionManager = new VelbusConnectionManager();
      connectionManager.setAddress(interfaceAddress);
      connectionManager.setPort(interfacePort);
      connectionManager.setConnection(connection);
      connectionManager.start();
      
      this.initialised = true;
    }
  }
  
  void stop() {
    if (initialised) {
      connectionManager.stop();
      this.initialised = false;
    }
  }
}
