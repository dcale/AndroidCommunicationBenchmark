package ch.papers.androidcommunicationbenchmark.utils.objectstorage;

import ch.papers.androidcommunicationbenchmark.utils.objectstorage.models.UuidObject;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 23/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public interface Filter<T extends UuidObject> {
    public boolean matches(T object);
}
