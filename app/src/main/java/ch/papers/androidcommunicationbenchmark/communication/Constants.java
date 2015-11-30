package ch.papers.androidcommunicationbenchmark.communication;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class Constants {
    public final static UUID DEVICE_UUID = UUID.randomUUID();
    public final static UUID SERVICE_UUID = UUID.fromString("f36681f8-c73b-4a02-94a6-a87a8a351dc2");
    public final static String BROADCAST_NAME = "ACB";
    public final static int SERVER_PORT = 51337;
    public final static int CYCLE_COUNT = 10;
    public final static int DEFAULT_BUFFER_SIZE = 4096;
    public final static int DISCOVERABLE_DURATION = 300;
    public static final Gson GSON = new Gson();
}
