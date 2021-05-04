package mcproxy;

import science.atlarge.opencraft.packetlib.Session;

import java.util.ArrayList;

/**
 * A list of all the sessions which provides a convenient {@link #pulse()} method to pulse every
 * session in one operation.
 *
 * @author Graham Edgecombe
 */
public final class SessionRegistry {

    /**
     * A list of the sessions.
     */
    private final ArrayList<ObserverSession> sessions = new ArrayList<>();

    /**
     * Pulses all the sessions.
     */

    /**
     * Adds a new session.
     *
     * @param session The session to add.
     */
    public void add(ObserverSession session) {
        sessions.add(session);
    }

    /**
     * Removes a session.
     *
     * @param session The session to remove.
     */
    public void remove(ObserverSession session) {
        sessions.remove(session);
    }

    public void removeBySession(Session session) {
//        sessions.forEach(s -> {
//            if (s.getSession() == session) {
//                this.remove(s);
//            }
//        });

        ObserverSession s = findBySession(session);
        this.remove(s);
    }

    public ObserverSession findBySession(Session session) {
        ObserverSession obsSession = null;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getSession() == session) {
                obsSession = sessions.get(i);
            }
        }
        return obsSession;
    }

    public ArrayList<ObserverSession> getSessions() {
        return sessions;
    }

    public void Pulse() {
        sessions.forEach(s -> {
            s.getMessageQueue().forEach(m -> {
                s.getSession().send(m);
            });
        });
    }
}
