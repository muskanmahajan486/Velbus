package org.openremote.controller.protocol.velbus;

public class ConnectionException extends Exception
{
  public ConnectionException()
  {
    super();
  }
  
  public ConnectionException(String message)
  {
    super(message);
  }
  
  public ConnectionException(String message, Throwable rootCause)
  {
    super(message, rootCause);
  }
  
  public ConnectionException(Throwable rootCause)
  {
    super(rootCause);
  }
}
