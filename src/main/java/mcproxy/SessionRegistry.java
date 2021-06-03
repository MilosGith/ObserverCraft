package mcproxy;

import mcproxy.Spectator.SpectatorSession;
import science.atlarge.opencraft.packetlib.Session;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SessionRegistry {

    private final ConcurrentHashMap<SpectatorSession, Boolean> sessions = new ConcurrentHashMap<>();

    public void add(SpectatorSession session) {
        sessions.put(session, true);
    }

    public void remove(SpectatorSession session) {
        sessions.remove(session);
    }

    public void removeBySession(Session session) {
        SpectatorSession s = findBySession(session);
        this.remove(s);
    }

    public SpectatorSession findBySession(Session session) {
        SpectatorSession obsSession = null;
        for (SpectatorSession f : sessions.keySet()) {
            if (f.getSession() == session)
                return f;
        }
        return obsSession;
    }

    public ConcurrentHashMap<SpectatorSession, Boolean> getSessions() {
        return sessions;
    }

//    private ArrayList<SpectatorSession> getSessionsCopy() {
//        return new ArrayList<>(sessions);
//    }

    public void pulse() {
        sessions.keySet().forEach(SpectatorSession::pulse);
    }
}
