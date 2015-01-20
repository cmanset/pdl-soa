package com.insa.swim.orchestrator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class. Orchestrate the different components.
 */
public class Application {


    private static final Logger logger = LogManager.getLogger(Application.class);

    private final Controller controller;

    public Application() {
        controller = new Controller();
    }
    
    public void start() {
        controller.start();
    }

    public static void main(String[] args) {
        logger.error("Program starts");
        Application app = new Application();
        app.start();

        logger.debug("Program ends");
    }
}
