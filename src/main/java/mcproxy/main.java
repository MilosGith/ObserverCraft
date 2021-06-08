package mcproxy;


//import mcproxy.Connection.McServerConnection;
import mcproxy.logging.GlobalLogger;
import mcproxy.logging.SimpleTimeFormatter;
import org.bukkit.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String ip = null;
        String logFile = null;
        String host = null;
        if (args.length > 0) {
             ip = args[1];
             logFile = args[2];
             host = args[3];
             System.out.println("WE RECEIVED ARG: " + ip);
             System.out.println("WE RECEIVED ARG: " + logFile);
             System.out.println("WE RECEIVED ARG: " + host);
        } else {
            host = "127.0.0.1";
            ip = "127.0.0.1";
        }
        ObserverServer server = new ObserverServer(ip, logFile, host);
        server.run();
    }
}


