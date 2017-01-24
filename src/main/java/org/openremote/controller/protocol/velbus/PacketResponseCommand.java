package org.openremote.controller.protocol.velbus;

public enum PacketResponseCommand {
  UNKNOWN(-1),
  PUSH_BUTTON_STATUS(0x00),
  RELAY_STATUS(0xFB),
  MODULE_TYPE(0xFF),
  MODULE_SUBTYPE(0xB0),
  INPUT_STATUS(0xED),
  TEMP_STATUS(0xEA),
  CURRENT_TEMP_STATUS(0xE6),
  TEMP_SETTINGS1(0xE8),
  TEMP_SETTINGS2(0xE9),
  TIME_REQUEST(0xD7),
  MEMORY_DATA(0xFE),
  MEMORY_BLOCK_DUMP(0xCC),
  DIMMER_SLIDER_STATUS(0x0F),
  DIMMER_STATUS(0xEE),
  DIMMER_STATUS2(0xB8),
  BLIND_STATUS(0xEC),
  LED_OFF(0xF5),
  LED_ON(0xF6),
  LED_SLOW(0xF7),
  LED_FAST(0xF8),
  LED_VERYFAST(0xF9),
  LED_STATUS(0xF4),
  COUNTER_STATUS(0xBE)
  ;
   
  private int code;
  
  private PacketResponseCommand(int code) {
    this.code = code;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public static PacketResponseCommand fromCode(int code) {
    for (PacketResponseCommand type : PacketResponseCommand.values()) {
      if (type.getCode() == code) {
        return type;
      }
    }
    
    return UNKNOWN;
  }
}
