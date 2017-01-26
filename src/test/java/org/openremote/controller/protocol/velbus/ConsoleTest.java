package org.openremote.controller.protocol.velbus;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class ConsoleTest {
  private static final Logger log = Logger.getLogger(VelbusCommandBuilder.VELBUS_PROTOCOL_LOG_CATEGORY);
  
  public static void main(String[] args) throws InterruptedException {
    TestRunner runner = new TestRunner();
    runner.Run();
  }



  
//  private static void loopRead(VelbusReadCommand command, int loops, int pause) {
//    int count = 0;
//
//    while (count < loops) {
//      String inputResult = command.read();
//      System.out.println("STATUS '" + command.getAction() + "': " + inputResult);
//      count++;
//      try {
//        Thread.sleep(pause);
//      } catch (InterruptedException e1) {
//        // TODO Auto-generated catch block
//        e1.printStackTrace();
//      }
//    }
//  }
//  
//  private static String singleRead(VelbusReadCommand command) {
//    String result = command.read();
//    System.out.println("STATUS '" + command.getAction() + "': " + result);
//    return result;
//  }
  

}
