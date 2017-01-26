package org.openremote.controller.protocol.velbus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.TcpSocketPort;
import org.openremote.controller.utils.Strings;
/**
 * This class provides support for Velbus using a Velbus
 * TCP/IP network server
 * @author Richard Turner
 *
 */
public class VelbusIpConnection implements VelbusConnection, TcpSocketPort.PacketProcessor {
  private static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  private static final int TIME_INJECTION_INTERVAL = 3600000*6; // 6hrs
  private ConnectionStatus status = ConnectionStatus.DISCONNECTED;
  public static final int WAIT_BETWEEN_COMMANDS = 60;
  private Port port;
  private BusListener busListener;
  private Timer timerInjector;
  private List<VelbusConnectionStatusCallback> handlers = new ArrayList<VelbusConnectionStatusCallback>();
  private List<VelbusDevicePacketCallback> packetCallbacks = new ArrayList<VelbusDevicePacketCallback>();
  
  @Override
  public ConnectionStatus getStatus() {
    return status;
  }
  
  @Override
  public void start(Port port) throws ConnectionException {
    if (this.port != null) {
      try {
        this.port.stop();
      } catch (Exception e) {
        throw new ConnectionException(e);
      }
    }
    
    // Check port is set
    if (port == null) {
      throw new ConnectionException("Connection requires a port implementation");
    }
    
    this.port = port;
    
    // Initialise the port
    try {
      port.start();
     
      // Start bus listener
      this.busListener = new BusListener("Bus Listener");

      synchronized (this.busListener) {
           this.busListener.start();
      }
      
      // Start the time injection task
      timerInjector = new Timer("Time Injector");
      timerInjector.schedule(new TimeInjectionTask(), 120000, TIME_INJECTION_INTERVAL);
      
      updateStatus(ConnectionStatus.CONNECTED);
      Thread.sleep(3000);
    } catch (Exception e) {
      throw new ConnectionException(e);
    }
  }
  
  @Override
  public void stop() throws ConnectionException {
    if (this.busListener != null) {
      try {
        updateStatus(ConnectionStatus.DISCONNECTED);
        
        // Stop the port which will cause the bus listener to terminate
        // port cannot be interrupted
        this.port.stop();
        
        this.busListener.join();
        timerInjector.cancel();
      } catch (Exception e) {
        throw new ConnectionException(e);
      }
    }
  }

  @Override
  public void send(VelbusPacket sendPacket) throws ConnectionException {
    Message sendMessage = new Message(sendPacket.pack());
    log.debug("Sending: " + Strings.bytesToHex(sendMessage.getContent()));
    
    try {
      this.port.send(sendMessage);
      Thread.sleep(WAIT_BETWEEN_COMMANDS);
    } catch (Exception e) {
      log.info(e);
      
      // Ensure port is stopped
      try {
        VelbusIpConnection.this.port.stop();
      } catch (Exception e2) {
        log.error(e2);
      } finally {
        VelbusIpConnection.this.updateStatus(ConnectionStatus.DISCONNECTED);
      }
      throw new ConnectionException();
    }
  }

  @Override
  public void sendTimeUpdate() throws ConnectionException {
    injectTime();
  }
  
  @Override
  public synchronized void registerConnectionCallback(VelbusConnectionStatusCallback handler) {
    handlers.add(handler);    
  }

  @Override
  public synchronized void unregisterConnectionCallback(VelbusConnectionStatusCallback handler) {
    handlers.remove(handler);
  }
   
  private synchronized void updateStatus(ConnectionStatus status) {
    if (this.status == status) {
      return;
    }
    
    this.status = status;
    
    for (VelbusConnectionStatusCallback handler : handlers) {
      handler.onConnectionStatusChanged();
    }
  }

  @Override
  public synchronized void addDevicePacketHandler(VelbusDevicePacketCallback handler) {
    if (!packetCallbacks.contains(handler)) {
      packetCallbacks.add(handler);
    }
  }

  @Override
  public synchronized void removeDevicePacketHandler(VelbusDevicePacketCallback handler) {
    packetCallbacks.remove(handler);   
  }
  
  @Override
  public synchronized void removeAllDevicePacketHandlers() {
    packetCallbacks.clear();
  }  

  @Override
  public boolean processByte(ByteArrayOutputStream packet, byte newByte) {
    if (packet.size() == 0 && (newByte & 0xFF) != 0x0F) {
      // Start of packet not found
      return false;
    }

    packet.write(newByte & 0xFF);
    
    if (packet.size() > 14) {
      // Packet is bigger than max packet size for velbus, must have been a
      // problem somewhere so discard this packet (pick it up in the is valid method)
      return true;
    }

    if (packet.size() < 4) {
      return false;
    }
    
    // Check data size
    byte[] packetBytes = packet.toByteArray();
    int dataSize = (packetBytes[3] & 0x0F);
    
    // Total packet size is dataSize + 6
    if (packet.size() == dataSize + 6 && (newByte & 0xFF) == 0x04) {
      return true;
    }
    
    return false;
  }

  @Override
  public boolean packetIsValid(ByteArrayOutputStream packet) {
    if (packet.size() > 14) {
      return false;
    }
    
    byte[] packetBytes = packet.toByteArray();
    
    // Check start and end bytes
    if ((packetBytes[0] & 0xFF) != 0x0F && (packetBytes[packetBytes.length - 1] & 0xFF) != 0x04) {
      return false;
    }
    
    // Check checksum
    byte checksumByte = packetBytes[packetBytes.length - 2];
    byte calculatedChecksum = VelbusPacket.checksum(packetBytes, packetBytes.length - 3);

    return calculatedChecksum == checksumByte;
  }
  
  /**
   * Thread for listening to messages coming from the physical bus
   * @author Richard Turner
   *
   */
  private class BusListener extends Thread {
    public BusListener(String name) {
      super(name);
    }
    
    @Override
    public void run() {
      synchronized (this) {
        this.notify();
      }
      
      while (!this.isInterrupted()) {
        try {
          // Wait to receive data
          Message b = VelbusIpConnection.this.port.receive();
          
          // TODO: Validate packet checksum
          if (b.getContent().length < 3) {
            continue;
          }
                   
          // Check address and route to packet handlers
          int address = b.getContent()[2] & 0xFF;
          VelbusPacket packet = new VelbusPacket(b.getContent());
          
          // If this is a time injection request just action it
          if (PacketResponseCommand.fromCode(packet.getCommand()) == PacketResponseCommand.TIME_REQUEST) {
            injectTime();
            continue;
          }
          
          synchronized(VelbusIpConnection.this) {
            VelbusDevicePacketCallback[] callbackArr = new VelbusDevicePacketCallback[packetCallbacks.size()];
            packetCallbacks.toArray(callbackArr);
            
            for (VelbusDevicePacketCallback callback : callbackArr) {
              for (int moduleAddress : callback.getAddresses()) {
                if (moduleAddress == address) {
                  try {
                    log.debug("Received: " + Strings.bytesToHex(packet.pack()));
                    callback.onPacketReceived(packet);
                  } catch (Exception e) {
                    log.error("Packet Callback threw an exception: " + e.getMessage(), e);
                  }
                  break;
                }
              }
            }
          }
        } catch (IOException e2) {
          // Problem with the socket - could be a network issue
          // or the socket could have been closed by the connection
          // outer class
          log.info(e2);
          
          // Ensure port is stopped
          try {
            VelbusIpConnection.this.port.stop();
          } catch (Exception e3) {
            log.error(e3);
          } finally {
            VelbusIpConnection.this.updateStatus(ConnectionStatus.DISCONNECTED);
          }
          
          break;
        }
      }
    }
  }
  
  private synchronized void injectTime() {
    if (VelbusIpConnection.this.getStatus() == ConnectionStatus.CONNECTED) {
      Calendar c = Calendar.getInstance();
      int dst = c.get(Calendar.DST_OFFSET) > 0 ? 1 : 0;
      int dow = (c.get(Calendar.DAY_OF_WEEK)+5) % 7;
      int dom = c.get(Calendar.DAY_OF_MONTH);
      int month = c.get(Calendar.MONTH) + 1;
      int hours = c.get(Calendar.HOUR_OF_DAY);
      int mins = c.get(Calendar.MINUTE);
      int year = c.get(Calendar.YEAR);
      
      
      VelbusPacket timePacket = new VelbusPacket(0x00, PacketRequestCommand.SET_REALTIME_CLOCK.getCode(), (byte)dow, (byte)hours, (byte)mins);
      VelbusPacket datePacket = new VelbusPacket(0x00, PacketRequestCommand.SET_REALTIME_DATE.getCode(), (byte)dom, (byte)month, (byte)(year >>> 8), (byte)year);
      VelbusPacket dstPacket = new VelbusPacket(0x00, PacketRequestCommand.SET_DAYLIGHT_SAVING.getCode(), (byte)dst);
      try {
        VelbusIpConnection.this.send(timePacket);
        VelbusIpConnection.this.send(datePacket);
        VelbusIpConnection.this.send(dstPacket);
      } catch (ConnectionException e) {
       log.error(e);
      }
    }
  }
  
  /**
   * Class for injecting the current time into the bus
   * @author Richard Turner
   *
   */
  private class TimeInjectionTask extends TimerTask {

    @Override
    public void run() {
      injectTime();
    }
    
  }
}
