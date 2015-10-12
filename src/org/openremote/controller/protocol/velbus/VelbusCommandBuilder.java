package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.exception.CommandBuildException;
import org.openremote.controller.service.Deployer;
import org.openremote.controller.service.ServiceContext;

import java.util.ArrayList;
import java.util.List;

public class VelbusCommandBuilder implements CommandBuilder {
  public final static String VELBUS_PROTOCOL_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "velbus";
  private final static String STR_ATTRIBUTE_NAME_ADDRESS = "address";
  private final static String STR_ATTRIBUTE_NAME_ACTION = "command";
  private final static String STR_ATTRIBUTE_NAME_VALUE = "value";
  private final static String STR_ATTRIBUTE_NAME_NETWORK_ID = "networkId";
  private final static String DELIMITER = ";";
  private static Logger log = Logger.getLogger(VELBUS_PROTOCOL_LOG_CATEGORY);
  private static final int DEVICE_LIMIT = 5;
  private static final List<Integer> DEVICE_IDS = new ArrayList<Integer>();
  private static final boolean IS_LIMITED = false;

  private Deployer deployer;

  private List<VelbusNetwork> networks = new ArrayList<VelbusNetwork>(); 
  private boolean isValid = false;
  
  public VelbusCommandBuilder(Deployer deployer) {
    this.deployer = deployer;

    // Get actual connection class
    try {
      // Split addresses and ports by delimiter and create networks
      String[] interfaceAddressesArr = ServiceContext.getVelbusConfiguration().getServerHostnames().split(DELIMITER);
      String[] interfacePortsArr = ServiceContext.getVelbusConfiguration().getServerPorts().split(DELIMITER);

      if (interfaceAddressesArr.length != interfacePortsArr.length) {
        log.error("Number of addresses provided doesn't match the number of ports");
        return;
      }

      for (int i=0; i<interfaceAddressesArr.length; i++) {
        String address = interfaceAddressesArr[i];
        String port = interfacePortsArr[i];

        if (!address.isEmpty() && !port.isEmpty()) {
          VelbusNetwork network = new VelbusNetwork(i+1, address, Integer.parseInt(port), new VelbusIpConnection());
          networks.add(network);
        }
      }

      isValid = true;
    } catch (Exception e) {
      log.error("Couldn't configure the VelbusConnection class to use - Protocol will not work.", e);
    }
  }

  public Command build(Element element) {
    int networkId = 1;
    int address = 0;
    VelbusCommand.Action action = null;    
    String commandValue = null;
    
    if (!isValid) {
      throw new CommandBuildException("Protocol didn't initialise correctly");
    }
    
    List<Element> propertyElements = element.getChildren(XML_ELEMENT_PROPERTY, element.getNamespace());
    
    for (Element ele : propertyElements)
    {
      String elementName = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_NAME);
      String elementValue = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_VALUE);
      
      if (STR_ATTRIBUTE_NAME_NETWORK_ID.equals(elementName))
      {
        try {
          networkId = Integer.parseInt(elementValue);
        } catch(NumberFormatException e) {
          log.warn("Invalid network ID specified", e);
        }
      }
      else if (STR_ATTRIBUTE_NAME_ADDRESS.equals(elementName))
      {
        try {
          address = Integer.parseInt(elementValue);
        } catch(NumberFormatException e) {
          log.warn("Invalid address specified", e);
        }
      }
      else if (STR_ATTRIBUTE_NAME_ACTION.equals(elementName))
      {
        try {
          action = VelbusCommand.Action.valueOf(elementValue);
        } catch(Exception e) {
          log.warn("Invalid command action specified", e);
        }
      }
      else if (STR_ATTRIBUTE_NAME_VALUE.equals(elementName))
      {
          commandValue = elementValue;
      }
    }
    
    // Sanity check values
    networkId = Math.max(1, networkId);
    VelbusNetwork network;
    
    if (networkId > networks.size() || (network = networks.get(networkId-1)) == null)
    {
      throw new CommandBuildException("Network ID '" + networkId  + "' is invalid");
    }    
    if (address <= 0 || address > 255)
    {
      throw new CommandBuildException("Command address must be an integer and in the range of 1-255");
    }
    if (action == null)
    {
      throw new CommandBuildException("Command action is not valid");
    }
    
    // Check device limit
    if (IS_LIMITED) {
      
      int deviceId = (networkId*1000) + address;
      if (!DEVICE_IDS.contains(deviceId)) {
        if (DEVICE_IDS.size() >= DEVICE_LIMIT) {
          log.warn("Device limit of " + DEVICE_LIMIT + " reached.");
          throw new CommandBuildException("Device limit reached");
        }
        
        DEVICE_IDS.add(deviceId);        
      }
    }
    
    // Initialise the network
    if (!network.isInitialised()) {
      network.initialise();
    }

    if (action.isRead())
    {
      return new VelbusReadCommand(network.getConnectionManager(), address, action, commandValue);
    }
    else
    {
      String paramValue = element.getAttributeValue(Command.DYNAMIC_VALUE_ATTR_NAME);
     
      return new VelbusWriteCommand(network.getConnectionManager(), address, action, commandValue, paramValue);
    }
  }
}
