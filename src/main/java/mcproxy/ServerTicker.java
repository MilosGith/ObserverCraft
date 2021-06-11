package mcproxy;

import com.atlarge.yscollector.YSCollector;
import mcproxy.ObserverServer;
import mcproxy.util.Scheduler;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerTicker implements Runnable {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ObserverServer server;

    public ServerTicker(ObserverServer server) {
        this.server = server;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Already ticking");
        }
        Thread t = new Thread(this);
        t.setName("Observer Ticker");
        t.start();
    }

    public void stop() {
      //  System.out.println("CALLED SOTP ON THE  OBSERVER TICKER");
        running.set(false);
    }
    private void startMeasurement(String key, String help) {
        server.getEventLogger().start(key);
    }

    private void stopMeasurement(String key) {
        server.getEventLogger().stop(key);
    }

    @Override
    public void run() {
        Scheduler sched = new Scheduler(50);
        sched.start();


        while (running.get()) {
            startMeasurement("tick", "The duration of a tick");

            startMeasurement("tick_server", "duration of how long it takes to process server packets");
            Packet[] toHandle = server.getConnection().getToHandle().toArray(new Packet[0]);
            for (Packet packet : toHandle) {
                server.getServerMessageHandler().handlePacket(packet);
            }
            server.getConnection().getToHandle().removeAll(Arrays.asList(toHandle));
            stopMeasurement("tick_server");

            startMeasurement("tick_client", "duration of how long it takes to process client packets");
            server.getSessionRegistry().pulse();
            stopMeasurement("tick_client");

            startMeasurement("tick_update", "duration of how long it takes for proxy to perform its tasks (forwarding, updating inrange  players");
            server.getSessionRegistry().update();
            stopMeasurement("tick_update");

            stopMeasurement("tick");
            sched.sleepTick();
        }
    }
}


