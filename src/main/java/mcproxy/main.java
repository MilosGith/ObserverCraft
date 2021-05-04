package mcproxy;


//import mcproxy.Connection.McServerConnection;
import mcproxy.logging.GlobalLogger;
import mcproxy.logging.SimpleTimeFormatter;
import org.bukkit.Server;

import java.util.concurrent.TimeUnit;

public class main {
    public static final GlobalLogger LOGGER = GlobalLogger.setupGlobalLogger("proxy");

    public static void main(String[] args) throws InterruptedException {
        LOGGER.setupConsoleLogging(new SimpleTimeFormatter());
        ObserverServer server = new ObserverServer();
        server.run();
    }
}


