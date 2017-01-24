package org.openremote.controller.protocol.velbus;

import java.util.HashMap;
import java.util.Map;

import org.openremote.controller.protocol.velbus.processor.*;

public enum VelbusDeviceType {
  UNKNOWN(0x00, false, null),
  VMB4RYNO(0x11, false, RelayDeviceProcessor.class),
  VMB4RYLD(0x10, false, RelayDeviceProcessor.class),
  VMB1RYNO(0x1B, false, RelayDeviceProcessor.class),
  VMB1RYNOS(0x29, false, RelayDeviceProcessor.class),
  VMB1RY(0x02, false, RelayDeviceProcessor.class),
  VMBGP1(0x1E, true, InputDeviceProcessor.class),
  VMBGP2(0x1F, true, InputDeviceProcessor.class),
  VMBGP4(0x20, true, InputDeviceProcessor.class),
  VMBGPO(0x21, true, InputDeviceProcessor.class),
  VMBGPOD(0x28, true, InputDeviceProcessor.class),
  VMB7IN(0x22, false, InputDeviceProcessor.class),
  VMB8PBU(0x16, false, InputDeviceProcessor.class),
  VMB6PBN(0x17, false, InputDeviceProcessor.class),
  VMBDMI(0x15, false, DimmerDeviceProcessor.class),
  VMBDMIR(0x2F, false, DimmerDeviceProcessor.class),
  VMBDME(0x14, false, DimmerDeviceProcessor.class),
  VMB4DC(0x12, false, DimmerDeviceProcessor.class),
  VMB2BLE(0x1D, false, BlindDeviceProcessor.class),
  VMB1BL(0x03, false, BlindDeviceProcessor.class),
  VMBGP4PIR(0x2D, true, InputDeviceProcessor.class),
  VMBPIRM(0x2A, false, InputDeviceProcessor.class),
  VMBPIRO(0x2C, false, InputDeviceProcessor.class),
  VMBPIRC(0x2B, false, InputDeviceProcessor.class)
  ;
  
  private static final Map<Class<? extends VelbusDeviceProcessor>, VelbusDeviceProcessor> processors = new HashMap<Class<? extends VelbusDeviceProcessor>, VelbusDeviceProcessor>();
  private int code;
  private boolean hasSubtype;
  private Class<? extends VelbusDeviceProcessor> processorClazz;  
  
  private VelbusDeviceType(int code, boolean hasSubtype, Class<? extends VelbusDeviceProcessor> processorClazz) {
    this.code = code;
    this.hasSubtype = hasSubtype;
    this.processorClazz = processorClazz;
  }
  
  public int getCode() {
    return this.code;
  }
  
  public VelbusDeviceProcessor getProcessor() throws InstantiationException, IllegalAccessException {
    VelbusDeviceProcessor processor = null;
    if (processorClazz != null) {
      processor = processors.get(processorClazz);
      
      if (processor == null) {
        processor = processorClazz.newInstance();
        processors.put(processorClazz, processor);
      }
    }
    return processor;
  }
  
  public boolean hasSubtype() {
    return hasSubtype;
  }
  
  public static VelbusDeviceType fromCode(int code) {
    for (VelbusDeviceType type : VelbusDeviceType.values()) {
      if (type.getCode() == code) {
        return type;
      }
    }
    
    return UNKNOWN;
  }
}
