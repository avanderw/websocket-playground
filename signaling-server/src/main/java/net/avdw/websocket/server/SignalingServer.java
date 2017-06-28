package net.avdw.websocket.server;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/signaling-server")
public class SignalingServer {

    private static Queue<Session> queue = new ConcurrentLinkedQueue();
    private static final Thread rateThread;

    static {
        rateThread = new Thread() {
            @Override
            public void run() {
                DecimalFormat df = new DecimalFormat("#.####");
                while (true) {
                    double d = 2 + Math.random();
                    if (queue != null) {
                        sendAll("USD Rate: " + df.format(d));
                    }
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                    }
                }
            }

            ;
        };
        rateThread.start();
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            System.out.println("received msg " + msg + " from " + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void open(Session session) {
        queue.add(session);
        System.out.println("New session opened: " + session.getId());
    }

    @OnError
    public void error(Session session, Throwable t) {
        queue.remove(session);
        System.err.println("Error on session " + session.getId());
    }

    @OnClose
    public void closedConnection(Session session) {
        queue.remove(session);
        System.out.println("session closed: " + session.getId());
    }

    private static void sendAll(String msg) {
        try {
            /* Send the new rate to all open WebSocket sessions */
            ArrayList<Session> closedSessions = new ArrayList();
            for (Session session : queue) {
                if (!session.isOpen()) {
                    System.err.println("Closed session: " + session.getId());
                    closedSessions.add(session);
                } else {
                    session.getBasicRemote().sendText(msg);
                }
            }
            queue.removeAll(closedSessions);
            System.out.println("Sending " + msg + " to " + queue.size() + " clients");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
