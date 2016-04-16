package org.openremote.controller.protocol.velbus;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.TcpSocketPort;
import org.openremote.controller.protocol.velbus.VelbusPacket.PacketPriority;

public class VelbusConnectionManager implements VelbusConnectionStatusCallback {
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  private static final int MAX_CONNECTION_ATTEMPTS = 30;
  private static final int RECONNECTION_DELAY = 20000;
  private static final int CONNECTED_DELAY = 3000;
  private final ExecutorService deviceInitialiserQueue = Executors.newSingleThreadExecutor();
  private String address;
  private int port;
  private Timer connectionTimer;
  private VelbusConnection connection;
  private int connectionAttempts;
  private Port busPort;
  private Map<Integer, VelbusDevice> deviceCache = new HashMap<Integer, VelbusDevice>();
  private static final int INIT_TIMEOUT = 30000;
  
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
  
  public synchronized VelbusConnection getConnection() {
    return connection;
  }
  
  public synchronized void setConnection(VelbusConnection connection) {
    this.connection = connection;
  }
  
  public ConnectionStatus getConnectionStatus() {
    return connection != null ? connection.getStatus() : ConnectionStatus.DISCONNECTED;
  }
  
  /**
   * Try and make the connection to the bus
   */
  synchronized void start() {
    if (this.connection == null) {
      log.error("No connection defined so cannot start connection manager");
      return;
    }
    
    // Verify connection is supported
    if (connection instanceof VelbusIpConnection) {
    } else {
      log.error("Unsupported connection");
      return;
    }
    
    // Register callback
    if (this.connection != null) {
      this.connection.registerConnectionCallback(this);
    }
       
    startConnectionTimer(RECONNECTION_DELAY);
  }
  
  synchronized void stop() {
    cancelConnectionTimer();
    
    if (this.connection != null) {
      this.connection.unregisterConnectionCallback(this);
    }
    
    try {
      connection.stop();
    } catch (ConnectionException e) {
      log.error(e);
    }
  }
   
  void addSensor(VelbusReadCommand command, Sensor sensor) {
    // Get the device for this command
    VelbusDevice device = getDevice(command);
    
    if (device != null)
    {
      log.debug("Adding sensor '" + sensor.getName() + "' from device '" + device.getAddresses() + "'");
      device.addSensor(command, sensor);
    }
  }
  
  void removeSensor(VelbusReadCommand command, Sensor sensor) {
    // Get the device for this command
    VelbusDevice device = getDevice(command);
    
    if (device != null)
    {
      log.debug("Removing sensor '" + sensor.getName() + "' from device '" + device.getAddresses() + "'");
      device.removeSensor(command, sensor);
    }
  }
  
  /**
   * Abstraction layer for sending openremote commands to the
   * velbus network; deals with translation of data.
   * This is a fire and forget method (i.e. no acknowledgement)  
   * @param command
   */
  void send(VelbusWriteCommand command) {
    if (command.getAction() == VelbusCommand.Action.TIME_UPDATE && connection != null) {
      try {
        connection.sendTimeUpdate();
      } catch (ConnectionException e) {
        log.error("Failed to inject time", e);
      }
      
      return;
    }
    
    VelbusDevice device = getDevice(command);
    
    if (device != null) {
      device.write(command);
    }
  }

  
  /**
   * Get the device associated with this command; if it doesn't exist
   * in the device cache then create it 
   * @param address
   * @return
   */
  private synchronized VelbusDevice getDevice(VelbusCommand command) {
    int addr = command.getAddress();
    
    if (deviceCache.get(addr-1) != null) {
      return deviceCache.get(addr-1);
    }
    
    final VelbusDevice device = new VelbusDevice(addr, getConnection());
    deviceCache.put(addr-1, device);

    if (getConnectionStatus() == ConnectionStatus.CONNECTED) {
      deviceInitialiserQueue.submit(new Runnable() {        
        @Override
        public void run() {
          VelbusConnectionManager.this.initialiseDevice(device);
        }
      });
    }
    
    return device;
  }
  
  private void initialiseDevice(VelbusDevice device) {
    if (getConnectionStatus() != ConnectionStatus.CONNECTED) {
      return;
    }
    
    int timer = 0;
    
    // Get device type from velbus network
    log.debug("Requesting module type information from bus for device '" + device.getAddresses()[0] + "'");
    VelbusPacket request = new VelbusPacket(device.getAddresses()[0], PacketPriority.LOW, 0, true);
    
    try {
      connection.send(request);
    } catch (ConnectionException e) {
      log.error(e);
    }
    
    // Sleep until this device is initialised or init timeout elapses
    while (getConnectionStatus() == ConnectionStatus.CONNECTED && !device.isInitialised() && timer < INIT_TIMEOUT) {
      try {
        Thread.sleep(100);
        timer += 100;
      } catch (InterruptedException e) {
        break;
      }
    }
    
    if (!device.isInitialised()) {
      log.error("Device '" + device.getAddresses()[0] + "' failed to initialise");
      device.setTimedout();
    } else {
      log.debug("Device '" + device.getAddresses()[0] + "' initialised");
    }
  }
  
  private void startConnectionTimer(int connectionDelay) {
    if(this.connectionTimer == null) {
      this.connectionTimer = new Timer("Velbus Bus connector");
      this.connectionTimer.schedule(new ConnectionTask(), 1, connectionDelay);
      log.info("Scheduled bus connection task");
    }
  }
  
  private void cancelConnectionTimer() {
    if (connectionTimer != null) {
      connectionTimer.cancel();
      connectionTimer = null;
    }
  }
  
  @Override
  public synchronized void onConnectionStatusChanged() {
    if (connection.getStatus() == ConnectionStatus.DISCONNECTED) {
      log.debug("Connection has been lost");
      
      // Restart the connection timer
      startConnectionTimer(RECONNECTION_DELAY);
    } else {
      // Stop the connection timer
      log.debug("Connection established");
      cancelConnectionTimer();
      
      // Reset connection attempt counter
      connectionAttempts = 0;
      
      // Wait to allow server to fully initialise
      try {
        log.debug("Waiting for server to fully initialise");
        Thread.sleep(CONNECTED_DELAY);
      } catch (InterruptedException e) {
        log.error(e);
      }
      
      if (connection.getStatus() == ConnectionStatus.DISCONNECTED) {
        return;
      }
      
      for (final VelbusDevice device : deviceCache.values()) {
        deviceInitialiserQueue.submit(new Runnable() {          
          @Override
          public void run() {
            initialiseDevice(device);
          }
        });
      }
    }
  }
  
  /**
   * Timer task to try and establish connection to the bus 
   * @author Richard Turner
   *
   */
  private class ConnectionTask extends TimerTask {

    @Override
    public void run() {
      if (connection != null) {
        try {
          connection.stop();
        } catch (Exception e) {
          log.debug("Exception thrown trying to stop old connection", e);
        }
      }
      
      // Try and initialise the port the connection will use
      try {
        log.debug("Trying to connect to '" + getAddress() + "' on port '" + getPort() + "'");
        if (connection instanceof VelbusIpConnection) {
          busPort = new TcpSocketPort();
          Socket socket = new Socket(getAddress(), getPort());
          Map<String, Object> cfg = new HashMap<String, Object>();
          cfg.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, socket);
          cfg.put(TcpSocketPort.TCP_PORT_CONFIGURATION_PROCESSOR, connection);
          busPort.configure(cfg);
          connection.start(busPort);
          log.info("Connection initialised");
          cancelTask();
        }
      } catch(Exception e) {
        log.warn(e.getMessage());
        connectionAttempts++;
        
        if (connectionAttempts > MAX_CONNECTION_ATTEMPTS) {
          log.warn("Max connection attempts reached, will reattempt once every hour");
          VelbusConnectionManager.this.cancelConnectionTimer();
          VelbusConnectionManager.this.startConnectionTimer(3600000);
        }
      }
    }
    
    private void cancelTask() {
      log.info("Stopping connection timer");
      cancel();
      VelbusConnectionManager.this.connectionTimer = null;
    }
  }
}
