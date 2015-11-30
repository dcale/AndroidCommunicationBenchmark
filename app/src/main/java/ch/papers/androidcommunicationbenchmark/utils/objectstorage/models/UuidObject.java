package ch.papers.androidcommunicationbenchmark.utils.objectstorage.models;

import java.util.UUID;


/**
 * Created by Alessandro De Carli (@a_d_c_) on 21/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public abstract class UuidObject {
    protected UUID uuid = UUID.randomUUID();

    public String getUuidString() {
        return uuid.toString();
    }

    public UUID getUuid() {
        return uuid;
    }
}
