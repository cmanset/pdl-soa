/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cons;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author pdlsoa
 */
public class Consumer1 {
    
    private static final Logger LOGGER = LogManager.getLogger(Consumer1.class);

    public static void main(String args[]) {


        try { // Call Web Service Operation
            compositeapp1.CompositeApp1Service1 service = new compositeapp1.CompositeApp1Service1();
            compositeapp1.P1WebService port = service.getCasaPort1();
            // TODO initialize WS operation arguments here
            java.lang.String ping = "ping";
            // TODO process result here
            java.lang.String result = port.pingpong(ping);
            System.out.println("Result = "+result);
        } catch (Exception ex) {
            LOGGER.error("Failed to call casa: " + ex.getMessage());
        }


    }
}
