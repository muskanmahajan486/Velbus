package org.openremote.controller.protocol.velbus;

public class VelbusPacket {
  /*
   * Packet priority. This is important for the Velbus network only,
   * the dispatcher does not yet keep track of priorities.
   */
  public enum PacketPriority
  {
      /// <summary>
      /// High priority. Usually used by real-time command like
      /// pressing a button etc.
      /// </summary>
      HIGH(0xF8),

      /// <summary>
      /// Low priority.
      /// </summary>
      LOW(0xFB);
      
      private int value;
      
      PacketPriority(int value) {
        this.value = value;
      }
      
      public int getValue() {
        return value;
      }
  }

  /*
   * Maximum packet size used by the Velbus protocol.
   */
  public static final int MAX_PACKET_SIZE = 14;

  /*
   * Start of packet.
   */
  public static final byte STX = 0x0F;

  /*
   * End of packet.
   */
  public static final byte ETX = 0x04;

  /*
   * Raw representation of this packet.
   */
  private byte[] rawPacket = new byte[MAX_PACKET_SIZE];


  public VelbusPacket(byte[] content) {
    rawPacket = content;
  }
  
  /// <summary>
  /// Initialises a new instance of the Packet class.
  /// </summary>
  public VelbusPacket() {
    this(0x00);
  }

  /*
   * Initialises a new instance of the Packet class using the
   * specified address.
   */
  public VelbusPacket(int address) {
      this(address, PacketPriority.LOW, 0, false);
  }
  
  public VelbusPacket(int address, int command, byte... dataBytes) {
    this(address, command, PacketPriority.LOW, dataBytes);
  }
  
  public VelbusPacket(int address, int command, PacketPriority priority, byte... dataBytes) {
    this(address);
    setCommand(command);
    setDataSize(dataBytes.length + 1);
    setPriority(priority);
    
    for (byte i = 0; i < dataBytes.length; i++) {
      setByte(i+1, dataBytes[i]);
    }
  }

  /*
   * Initialises a new instance of the Packet class using the
   * specified address, priority, data size and rtr state.
   */
  public VelbusPacket(int address, PacketPriority priority, int dataSize, boolean rtr) {
      setAddress(address);
      setPriority(PacketPriority.LOW);
      setDataSize(dataSize);
      setRtr(rtr);
  }

  /*
   * Get Address
   */
  public int getAddress() {
    return rawPacket[2] & 0xFF;
  }
  
  /*
   * Set address
   */
  public void setAddress(int address) {
    rawPacket[2] = (byte)address;
  }

  /*
   * Get data size
   */
  public int getDataSize() {
    return (rawPacket[3] & 0x0F);
  }
  
  /*
   * Set data size
   */
  public void setDataSize(int dataSize) {
    rawPacket[3] = (byte)((rawPacket[3] & 0xF0) + dataSize);
  }
  
  /*
   * Get total packet size
   */
  public int getSize() {
    return getDataSize() + 6;
  }

  /*
   * Get packet priority
   */
  public PacketPriority getPriority() {
    return (rawPacket[1] == 0xF8 ? PacketPriority.HIGH : PacketPriority.LOW);
  }
  
  /*
   * Set packet priority
   */
  public void setPriority(PacketPriority priority) {
    rawPacket[1] = (byte)(priority.getValue());
  }

  /*
   * Gets the request to reply state of the packet.
   */
  public boolean getRtr() {
    return ((rawPacket[3] & 0x40) == 0x40);
  }
  
  /*
   *  Sets the request to reply state of the packet.
   */
  public void setRtr(boolean rtr) {
          if (rtr)
              rawPacket[3] |= 0x40;
          else
              rawPacket[3] &= 0x0F;
  }

  /*
   * Get packet byte by index
   */
  public byte getByte(int index) {
    return rawPacket[4 + index];
  }
      
  /*
   * Set packet byte at specified index
   */
  public void setByte(int index, byte value) {
    rawPacket[4 + index] = value;
  }

  /*
   * Gets the command byte of the packet. Since the command
   * byte is the first databyte, the datasize needs to be greater or 
   * equal to one.
   */
  public int getCommand() {
    if (getDataSize() <= 0) {
      return -1;  
    }
    
    return getByte(0) & 0xFF;
  }
  
  /*
   * Gets or sets the command byte of the packet. Since the command
   * byte is the first databyte, the datasize needs to be greater or 
   * equal to one.
   */
  public void setCommand(int command) {
    setByte(0, (byte)command);
  }

  /*
   * Checks if the packet has a command byte (eg. if DataSize >= 1).
   */
  public boolean hasCommand()
  {
      return (getDataSize() >= 1);
  }

  /*
   * Packs the byte so it is ready for sending.
   * Packing involves adding a checksum and the frame delimiters.
   */
  public byte[] pack()
  {
      rawPacket[0] = VelbusPacket.STX;
      rawPacket[getSize() - 1] = VelbusPacket.ETX;
      rawPacket[getSize() - 2] = checksum(rawPacket, getSize() - 3);

      return rawPacket;
  }

  /*
   * Clones this packet.
   */
  public VelbusPacket clone() {
      VelbusPacket packet;
      try {
        packet = new VelbusPacket();
        for(int i=0; i<getSize(); i++) {
          packet.rawPacket[i] = rawPacket[i];
        }
        return packet;
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      return null;
  }
  
  /*
   * Calculates checksum byte
   */
  public static byte checksum(byte[] data, int size)
  {
    byte checksum = 0;
    for (int i=0; i<=size; i++) {
      checksum += data[i];
    }
    return (byte)(-checksum);
  } 
}
