package org.openremote.controller.protocol.velbus;

public interface VelbusDeviceProcessor {
  Iterable<VelbusPacket> getStatusRequestPackets(VelbusDevice device);

  /**
   * Get the name of the property in the device cache that matches this command
   * @param command
   * @return
   */
  String getDeviceCachePropertyName(VelbusReadCommand command);  
  
  /**
   * Converts the write command into a velbus packet that this
   * device will understand
   * @param device
   * @param command
   */
  Iterable<VelbusPacket> processWriteCommand(VelbusDevice device, VelbusWriteCommand command);
  
  /**
   * Processes the received packet and calls update value
   * on the appropriate device property
   * @param device
   * @param packet
   */
  void processResponse(VelbusDevice device, VelbusPacket packet);

  /**
   * Determines if the device cache is fully initialised for this
   * type of device
   * @param deviceCache
   * @return
   */
  boolean isInitialised(VelbusDevice device);
}
