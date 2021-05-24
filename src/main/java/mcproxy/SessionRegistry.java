package mcproxy;

import mcproxy.Spectator.SpectatorSession;
import science.atlarge.opencraft.packetlib.Session;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SessionRegistry {

    private final ArrayList<SpectatorSession> sessions = new ArrayList<>();

    public void add(SpectatorSession session) {
        sessions.add(session);
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
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getSession() == session) {
               // System.out.println("FOUND SESSION TO REMOVE FROM SESSIONREGISTRY");
                obsSession = sessions.get(i);
            }
        }
        return obsSession;
    }

    public ArrayList<SpectatorSession> getSessions() {
        return sessions;
    }

    private ArrayList<SpectatorSession> getSessionsCopy() {
        return new ArrayList<>(sessions);
    }

    public void pulse() {
        getSessionsCopy().forEach(SpectatorSession::pulse);
    }
}
