package mcproxy;


//import mcproxy.Connection.McServerConnection;
import mcproxy.logging.GlobalLogger;
import mcproxy.logging.SimpleTimeFormatter;
import org.bukkit.Server;

import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String[] args) throws InterruptedException {
        ObserverServer server = new ObserverServer();
        server.run();
    }
}


