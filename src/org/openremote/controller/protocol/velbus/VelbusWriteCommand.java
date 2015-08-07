package org.openremote.controller.protocol.velbus;

import org.openremote.controller.command.ExecutableCommand;

public class VelbusWriteCommand extends VelbusCommand implements ExecutableCommand {
  private String parameter;
  
  VelbusWriteCommand(VelbusConnectionManager connectionManager, int address, Action action, String value, String parameterValue) {
    super(connectionManager, address, action, value);
    
    this.parameter = parameterValue;
  }

  public String getParameter() {
    return parameter;
  }
  
  public void send() {
    if (connectionManager.getConnectionStatus()!= ConnectionStatus.CONNECTED)
    {
      log.info("No KNX connection available, did not send command");
    }
    else
    {
      connectionManager.send(this);
    }
  }
}
