package mcproxy.measurements;

import mcproxy.ObserverServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class EventLogger implements AutoCloseable {

    private final Map<String, Long> timings = new HashMap<>();

    private ObserverServer server = null;

    public EventLogger(ObserverServer serv) {
        this.server = serv;
    }

    abstract public void init() throws IOException;

    abstract public void log(String key, String value, int pcount);

    public void log(String key, int value) {
        log(key, String.valueOf(value), server.getSpectatorCount());
    }

    public void log(String key, double value) {
        log(key, String.valueOf(value), server.getSpectatorCount());
    }

    public void start(String key) {
        timings.put(key, System.currentTimeMillis());
    }

    public void stop(String key) {
        if (timings.containsKey(key)) {
            log(key, System.currentTimeMillis() - timings.remove(key));
        }
    }

    abstract public void flush();
}
