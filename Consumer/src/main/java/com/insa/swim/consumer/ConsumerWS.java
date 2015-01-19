/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.insa.swim.consumer;

import compositeapp1.CompositeApp1Service1;
import com.insa.swim.consumer.scenario.Scenario;
import com.insa.swim.consumer.scenario.Scenario.Request;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
import compositeapp1.CompositeApp1Service2;
import compositeapp1.CompositeApp1Service3;
import compositeapp1.CompositeApp1Service4;
import java.io.IOException;
import java.util.Date;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author pdlsoa
 */
@WebService()
public class ConsumerWS {

//    public static final int NB_PROVIDERS = 4;
    static protected ConsumerAMQPHandler amqp;
    // req format : ConsID|ProvID|ReqSize|RespSize|ProcessingTime|SendingDateCons|PayloadCons
//    private String[] requests = {"1|1|3|4|6000|SendingDateConsumer|payloadConsumer",
//                                            "1|3|100|2|2000|SendingDateConsumer3|payloadConsumer3",
//                                            "1|4|10|10|4000|SendingDateConsumer4|payloadConsumer4"};

    /*
     * These are the referenes of the services provided by the bus to join the controller
     * */
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_9080/CompositeApp1Service1/casaPort1.wsdl")
    private CompositeApp1Service4 service4;
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_9080/CompositeApp1Service1/casaPort1.wsdl")
    private CompositeApp1Service3 service3;
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_9080/CompositeApp1Service1/casaPort1.wsdl")
    private CompositeApp1Service2 service2;
    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_9080/CompositeApp1Service1/casaPort1.wsdl")
    private CompositeApp1Service1 service1;
    private static final Logger logger = LogManager.getLogger("Consumer");
//    private Scenario scenario = null;
     private Scenario scenario = new Scenario("INFO|0|name|0|10|CONSUMER|2|C2"
                                                            + "|REQUEST|1|0021|4|0|0|0|2000|5"
                                                            + "|REQUEST|3|0023|2|0|0|0|5000|10"
                                                            + "|REQUEST|4|0024|10|0|0|0|10000|2");


    /**
     * Deprecated
     * This method tests the communication with the providers P1 throurgh the bus
     * @param txt (ping)
     * @return pong if param = ping, else erro, Gros fail if exception
     */
    @WebMethod(operationName = "sendPing")
    public String sendPing(@WebParam(name = "start") String txt) {
         // Call Web Service Operation
            compositeapp1.P1WebService port = service1.getCasaPort1();
            // TODO initialize WS operation arguments here
            java.lang.String ping = txt;
            // TODO process result here
            java.lang.String result = port.pingpong(ping);
            logger.debug("message received : " + result);
            return result;        
    }

    /**
     * This method tests the communication with a specifit provider throurgh the bus
     * @param txt : ping
     * @param provider : number of the provider
     * @return pong if param = ping, else erro, Gros fail if exception
     */
    public String sendPing(String txt, int provider) {
        java.lang.String result = "BigFail";
        switch (provider) {
            case 1:
                compositeapp1.P1WebService port1 = service1.getCasaPort1();
                result = port1.pingpong("ping");
                break;
            case 2:
                compositeapp1.P2WebService port2 = service2.getCasaPort2();
                result = port2.pingpong("ping");
                break;
            case 3:
                compositeapp1.P3WebService port3 = service3.getCasaPort3();
                result = port3.pingpong("ping");
                break;
            case 4:
                compositeapp1.P4WebService port4 = service4.getCasaPort4();
                result = port4.pingpong("ping");
                break;
            default:
                ;
        }
        logger.debug("message received : " + result);
        return result;
    }

     /**
     * This method tests the communication with a specifit provider throurgh the bus
     * @param request format : ConsID|ProvID|ReqSize|RespSize|ProcessingTime|SendingDateCons|PayloadCons
     * @param provider : id of the provider
     * @return response format : ConsID|ProvID|ReqSize|RespSize|ProcessingTime|SendingDateCons|ReceptionDateProv|PayloadProv
     */
    @WebMethod(operationName = "sendRequest")
    public String sendRequest(String request, int provider) {
        String response = "No response from provider " + provider;

        try {
             switch (provider) {
                case 1:
                    compositeapp1.P1WebService port1 = service1.getCasaPort1();
                    response = port1.processRequest(request);
                    break;
                case 2:
                    compositeapp1.P2WebService port2 = service2.getCasaPort2();
                    response = port2.processRequest(request);
                    break;
                case 3:
                    compositeapp1.P3WebService port3 = service3.getCasaPort3();
                    response = port3.processRequest(request);
                    break;
                case 4:
                    compositeapp1.P4WebService port4 = service4.getCasaPort4();
                    response = port4.processRequest(request);
                    break;
                default:
                    ;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.debug(ex.getStackTrace());
        }
        logger.debug("message received : " + response);

        // étrange : affichage des requêtes s'arrête au pipe
        // TODO : prendre les requêtes du scénario et les exécuter une par une + test

        return response;
    }

    /**
     * Obsolete method to configure the consumer : use AMQP instead
     * @param conf
     * @return
     */
//    @WebMethod(operationName = "configConsumer")
//    public String configConsumer(
//            @WebParam(name = "conf") String conf) {
//        logger.debug("message received : " + conf);
//        scenario = new Scenario(conf);
//        return "done";
//    }

    // format a request from the scenario to further be sent to the provider
    private String constructRequest(Request req) {
        String consumerID = Integer.toString(scenario.getConsumerId());
        String providerID = Integer.toString(req.getProviderId());
        String requestSize = Integer.toString(req.getSize());
        String responseSize = Integer.toString(req.getResponseSize());
        String processingTime = Integer.toString(req.getProcessingTime());
        String sendingDate = Integer.toString(req.getSendingTime());
        char[] payloadConsumer = new char[req.getSize()];

        // construct payload
        for (int i = 0 ; i < payloadConsumer.length ; i++) {
            payloadConsumer[i] = 'x';
        }

        // req format : ConsID|ProvID|ReqSize|RespSize|ProcessingTime|SendingDateCons|PayloadCons
        return consumerID + "|" + providerID  + "|" + requestSize  + "|" + responseSize + "|" + processingTime + "|" + sendingDate + "|" + new String(payloadConsumer);
    }

    /**
     * Test all the consumers with a ping request
     */
    @WebMethod(operationName = "startSendingRequests")
    public void startSendingRequests() {
        int providerNumber;

        for (Request req : scenario.getRequestList()) {
            providerNumber = req.getProviderId();

        
            logger.debug("Consumer " + this.getClass() + " starts sending requests");

            // Create a thread that handles the request sending to provider i (send, wait for response and send it to app)
            Thread thread = new Thread(new ConsumerThread(providerNumber, constructRequest(req)), this.getClass().toString());
            thread.start();         
        }
    }

    // Thread class to handle sending requests in parallel
    private class ConsumerThread implements Runnable {
        int providerNumber; // Provider to send requests to
        String request;

        public ConsumerThread(int providerNumber, String request) {
            this.providerNumber = providerNumber;
            this.request = request;
        }

        public void run() {
            System.out.println("-------------------------------------------------------------------Request sent : " + request);
            String response = sendRequest(request.toString(), providerNumber);
            System.out.println("-------------------------------------------------------------------Response : " + response);
            logger.debug("Response from P" + providerNumber + " to " + Thread.currentThread().getName() + " : " + response);

            //TODO envoyer les résultats à l'application par AMQP
            String startMsg = "ping";
            String pingResponse = "";

            pingResponse = sendPing(startMsg, providerNumber);
            Date receptionDateConsumer = new Date();
            logger.debug("Response from P" + providerNumber + " to " + Thread.currentThread().getName() + " : " + pingResponse);
            

            // envoyer les résultats à l'application par AMQP        
            String[] responseParts = pingResponse.split("|");
            String result = "";
            int i;
            for (i=0; i<8; i++){
                result += responseParts[i] + "|";
            }
            result += "|" + receptionDateConsumer;
            try {
                logger.debug("result : " + result);
                amqp.sendResult(result);
            } catch (IOException ex) {
                logger.error("[Consumer thread] Unable to send result to application" + ex.getMessage());
            }
        }
    }

    /**
     * Must be invoked at the begining
     * @param name used to be identified by AMQP (e.g "C2")
     * @return "done"
     */
    @WebMethod(operationName = "initialiseConsumer")
    public String initialiseConsumer(String name) {
        try {
            amqp = new ConsumerAMQPHandler(name);
            logger.debug("wait configuration...");
            String received = amqp.receiveConfigurationMessage();
            logger.debug("message config : " + received);
            scenario = new Scenario();
            scenario.init(received);
            logger.debug("wait start message...");
            String start = amqp.receiveStartMessage();

            logger.debug("message start : " + start);
            sendRequests();

            amqp.closeConnection();
        } catch (IOException ex) {
            logger.error("error initialisation" + this.getClass());
            ex.printStackTrace();
        } catch (ShutdownSignalException ex) {
            logger.error("error initialisation" + this.getClass());
            ex.printStackTrace();
        } catch (ConsumerCancelledException ex) {
            logger.error("error initialisation" + this.getClass());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            logger.error("error initialisation" + this.getClass());
            ex.printStackTrace();
        }
        return "done";
    }

    // TODO use multitrhead
    protected void sendRequests() {
        try {
            // STEP 3 : Send requests to providers and results to application
            amqp.sendResult("this is a result from " + this.getClass());
            System.out.println("end");
        } catch (Exception ex) {
            logger.debug("exception sendRequest");
        }
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
    
}
