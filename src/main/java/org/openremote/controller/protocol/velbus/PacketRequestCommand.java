package org.openremote.controller.protocol.velbus;

public enum PacketRequestCommand {  
  UNKNOWN(-1),
  MODULE_STATUS(0xFA),
  SET_REALTIME_CLOCK(0xD8),
  SET_REALTIME_DATE(0xB7),
  SET_DAYLIGHT_SAVING(0xAF),
  BUTTON_STATUS(0x00),
  RELAY_ON_TIMER(0x03),
  RELAY_OFF(0x01),
  LOCK_CHANNEL(0x12),
  UNLOCK_CHANNEL(0x13),
  LED_OFF(0xF5),
  LED_ON(0xF6),
  LED_SLOW(0xF7),
  LED_FAST(0xF8),
  LED_VERYFAST(0xF9),
  TEMP_SENSOR_STATUS(0xE7),
  READ_MEMORY(0xFD),
  READ_MEMORY_BLOCK(0xC9),
  WRITE_MEMORY(0xFC),
  SET_TEMP(0xE4),
  SET_MODE_HEAT(0xE0),
  SET_MODE_COOL(0xDF),
  SET_MODE_COMFORT(0xDB),
  SET_MODE_DAY(0xDC),
  SET_MODE_NIGHT(0xDD),
  SET_MODE_SAFE(0xDE),
  SET_ALARM(0xC3),
  SET_DIM(0x07),
  SET_LAST_DIM(0x11),
  STOP_DIM(0x10),
  DIM_ON_TIMER(0x08),
  BLIND_POSITION(0x1C),
  BLIND_UP(0x05),
  BLIND_DOWN(0x06),
  BLIND_HALT(0x04),
  BLIND_CHANNEL_LOCK(0x1A),
  BLIND_CHANNEL_UNLOCK(0x1B),
  MEMO_TEXT(0xAC),
  COUNTER_STATUS(0xBD),
  SELECT_PROGRAM(0xB3),
  SENSOR_READOUT(0xE5)
  ;
   
  private int code;
  
  private PacketRequestCommand(int code) {
    this.code = code;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public static PacketRequestCommand fromCode(int code) {
    for (PacketRequestCommand type : PacketRequestCommand.values()) {
      if (type.getCode() == code) {
        return type;
      }
    }
    
    return UNKNOWN;
  }
}
