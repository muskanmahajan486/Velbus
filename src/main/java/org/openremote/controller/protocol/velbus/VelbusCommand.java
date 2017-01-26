package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;

/**
 * Velbus base command 
 * @author Richard Turner
 *
 */
public abstract class VelbusCommand {
  public enum Action {
    STATUS(true),
    SETTING_STATUS(true),
    LED_STATUS(true),
    LOCK_STATUS(true),
    ENABLED_STATUS(true),
    ALARM_STATUS(true),
    ALARM_TYPE_STATUS(true),
    ALARM_TIME_STATUS(true),
    SUNRISE_STATUS(true),
    SUNSET_STATUS(true),
    PROGRAM_STATUS(true),
    TEMP_STATUS(true),
    TEMP_TARGET_STATUS(true),
    TEMP_MODE_STATUS(true),
    TEMP_STATE_STATUS(true),
    COUNTER_STATUS(true),
    COUNTER_INSTANT_STATUS(true),
    DIMMER_STATUS(true),
    POSITION_STATUS(true),
    ON(false),
    OFF(false),
    LOCK(false),
    UNLOCK(false),
    LED(false),
    TEMP_TARGET(false),
    TEMP_TARGET_RELATIVE(false),
    TEMP_MODE(false),
    ALARM(false),
    ALARM_TIME(false),
    ALARM_TIME_RELATIVE(false),
    DIMMER_LEVEL(false),
    HALT(false),
    UP(false),
    DOWN(false),
    BLIND_POSITION(false),
    MEMO_TEXT(false),
    PRESS(false),
    LONG_PRESS(false),
    RELEASE(false),
    LANGUAGE(false),
    PROGRAM(false),
    TIME_UPDATE(false)
    ;
    
    private boolean isRead;
    
    private Action(boolean isRead)
    {
      this.isRead = isRead;
    }
    
    public boolean isRead()
    {
      return isRead;
    }
  }
  
  protected static Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  private int address;
  private Action action;
  private String value;
  protected VelbusConnectionManager connectionManager;
  
  
  VelbusCommand(VelbusConnectionManager connectionManager, int address, Action action, String value) {
    this.connectionManager = connectionManager;
    this.address = address;
    this.action = action;
    this.value = value;
  }  

  public int getAddress() {
    return address;
  }

  public Action getAction() {
    return action;
  }

  public String getValue() {
    return value;
  }
}
