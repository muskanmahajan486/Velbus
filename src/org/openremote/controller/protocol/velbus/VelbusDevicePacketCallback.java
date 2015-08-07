package org.openremote.controller.protocol.velbus;

/**
 * Callback to be used for returning device specific packets
 * to the device
 * @author Richard Turner
 *
 */
public interface VelbusDevicePacketCallback {
  int[] getAddresses();
  void onPacketReceived(VelbusPacket packet);
}
