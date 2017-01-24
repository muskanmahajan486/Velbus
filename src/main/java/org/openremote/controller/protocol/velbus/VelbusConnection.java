package org.openremote.controller.protocol.velbus;

import org.openremote.controller.protocol.port.Port;

public interface VelbusConnection {
  /**
   * Send data to the network; returns instantly and packet
   * handler callback should be used to receive the response
   *
   * @return Packet
   */
  void send(VelbusPacket sendPacket) throws ConnectionException;

  /**
   * Add a device specific packet handler
   * @param handler
   */
  void addDevicePacketHandler(VelbusDevicePacketCallback handler);
  
  /**
   * Remove a device specific packet handler
   * @param handler
   */
  void removeDevicePacketHandler(VelbusDevicePacketCallback handler);
  
  /**
   * Remove all device specific packet handlers
   */
  void removeAllDevicePacketHandlers();
  
  /**
   * Get connection status.
   */
   ConnectionStatus getStatus();
   
   /**
    * Subscribe to connection status change notification
    * @param handler
    */
   void registerConnectionCallback(VelbusConnectionStatusCallback handler);
   
   /**
    * Un-subscribe from connection status change notification
    * @param handler
    */
   void unregisterConnectionCallback(VelbusConnectionStatusCallback handler);
   
   /**
    * Start the connection to the physical bus
    * @throws ConnectionException Connection failed
    */
   void start(Port port) throws ConnectionException;
   
   /**
    * Terminates the connection to the physical bus
    * @throws ConnectionException
    */
   void stop() throws ConnectionException;
   
   /**
    * Inject the current time into the VELBUS network
    * @throws ConnectionException
    */
   void sendTimeUpdate() throws ConnectionException;
}
