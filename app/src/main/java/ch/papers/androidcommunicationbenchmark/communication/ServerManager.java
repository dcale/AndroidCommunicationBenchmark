package ch.papers.androidcommunicationbenchmark.communication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 21/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class ServerManager {

    private static final ServerManager INSTANCE = new ServerManager();

    private final List<Thread> serverThreads = new ArrayList<Thread>();

    private ServerManager() {

    }

    public static ServerManager getInstance() {
        return INSTANCE;
    }

    public void start(final Server server) {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.start();
            }
        });

        serverThread.start();
        serverThreads.add(serverThread);
    }

}
