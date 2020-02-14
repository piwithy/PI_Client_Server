package fr.piwithy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Launcher {
    public static final Logger LOGGER = LogManager.getLogger(Launcher.class);

    public static void main(String[] ars){
        LOGGER.info("Launching new PiServer");
        PiServer server = new PiServer();
        server.run();
        LOGGER.info("PiServer Stopped");
        LOGGER.info("Good Bye!");
    }
}
