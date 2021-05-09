package mcproxy;

import mcproxy.util.Scheduler;
import science.atlarge.opencraft.packetlib.packet.Packet;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ObserverTicker implements Runnable {

    private final ObserverSession session;
    private final ObserverServer server;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ObserverTicker(ObserverSession ses, ObserverServer server) {
        this.session = ses;
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

    /**
     * Stops the BotTicker.
     */
    public void stop() {
        System.out.println("CALLED SOTP ON THE FUCKING OBSERVER TICKER");
        running.set(false);
    }

    @Override
    public void run() {
        Scheduler sched = new Scheduler(50);
        sched.start();

        while (running.get()) {
             server.getSessionRegistry().getSessions().forEach(ObserverSession::pulse);
             //System.out.println("TICKING OBSERVER " + running.get() + "\n");
            sched.sleepTick();
        }
    }
}


