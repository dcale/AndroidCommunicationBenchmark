package ch.papers.androidcommunicationbenchmark.utils.objectstorage;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 23/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */

import android.content.Context;

import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.papers.androidcommunicationbenchmark.communication.Constants;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnStorageChangeListener;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.models.UuidObject;

public class UuidObjectStorage {

    private static UuidObjectStorage INSTANCE;

    public static UuidObjectStorage getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UuidObjectStorage();

        }
        return INSTANCE;
    }

    private UuidObjectStorage() {
    }

    private Context context;
    private final Map<Class<? extends UuidObject>, Map<UUID, ? extends UuidObject>> uuidObjectCache = new LinkedHashMap<Class<? extends UuidObject>, Map<UUID, ? extends UuidObject>>();
    private final Map<Class<? extends UuidObject>, List<OnStorageChangeListener>> listeners = new HashMap<Class<? extends UuidObject>, List<OnStorageChangeListener>>();

    public synchronized void init(Context context) {
        this.context = context;
    }

    public synchronized <T extends UuidObject> void addEntry(final T entry, final OnResultListener<T> resultCallback, final Class<T> clazz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UuidObjectStorage.this.getOrCreateClassCache(clazz).put(entry.getUuid(), entry);
                    persistEntries(clazz);
                    if(resultCallback!=null) {
                        resultCallback.onSuccess(entry);
                    }
                    UuidObjectStorage.this.notifyListeners(clazz);
                } catch (Throwable e) {
                    if(resultCallback!=null) {
                        resultCallback.onError(e.getMessage());
                    }
                }
            }
        }).start();
    }

    public synchronized <T extends UuidObject> void deleteEntry(final T entry, final OnResultListener<T> resultCallback, final Class<T> clazz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UuidObjectStorage.this.getOrCreateClassCache(clazz).remove(entry.getUuid());
                    persistEntries(clazz);
                    if(resultCallback!=null) {
                        resultCallback.onSuccess(entry);
                    }
                    UuidObjectStorage.this.notifyListeners(clazz);
                } catch (Throwable e) {
                    if(resultCallback!=null) {
                        resultCallback.onError(e.getMessage());
                    }
                }
            }
        }).start();
    }

    public <T extends UuidObject> void getEntries(final OnResultListener<Map<UUID, T>> resultCallback, final Class<T> clazz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resultCallback.onSuccess(UuidObjectStorage.this.getOrCreateClassCache(clazz));
                } catch (Throwable e) {
                    resultCallback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public <T extends UuidObject> void getEntriesAsList(final OnResultListener<List<T>> resultCallback, final Class<T> clazz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resultCallback.onSuccess(new ArrayList<T>(UuidObjectStorage.this.getOrCreateClassCache(clazz).values()));
                } catch (Throwable e) {
                    resultCallback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public <T extends UuidObject> void getEntry(final UUID uuid, final OnResultListener<T> resultCallback, final Class<T> clazz) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    T entry = (T) UuidObjectStorage.this.getOrCreateClassCache(clazz).get(uuid);
                    if (entry != null) {
                        resultCallback.onSuccess(entry);
                    } else {
                        resultCallback.onError("could not find entry for uuid " + uuid);
                    }
                } catch (Throwable e) {
                    resultCallback.onError(e.getMessage());
                }
            }
        }).start();
    }

    public <T extends UuidObject> void registerOnChangeListener(OnStorageChangeListener onStorageChangeListener, final Class<T> clazz) {
        final List<OnStorageChangeListener> listeners = this.getOrCreateListenerList(clazz);
        listeners.add(onStorageChangeListener);
    }

    public <T extends UuidObject> void unRegisterOnChangeListener(OnStorageChangeListener onStorageChangeListener, final Class<T> clazz) {
        final List<OnStorageChangeListener> listeners = this.getOrCreateListenerList(clazz);
        listeners.remove(onStorageChangeListener);
    }

    private <T extends UuidObject> void notifyListeners(final Class<T> clazz) {
        final List<OnStorageChangeListener> listeners = this.getOrCreateListenerList(clazz);
        for (OnStorageChangeListener listener : listeners) {
            listener.onChange();
        }
    }

    private <T extends UuidObject> List<OnStorageChangeListener> getOrCreateListenerList(final Class<T> clazz) {
        List<OnStorageChangeListener> listeners = this.listeners.get(clazz);
        if (listeners == null) {
            listeners = new ArrayList<OnStorageChangeListener>();
        }
        return listeners;
    }

    private synchronized <T extends UuidObject> Map<UUID, T> getOrCreateClassCache(final Class<T> clazz) throws FileNotFoundException {
        try {
            if (!this.uuidObjectCache.containsKey(clazz)) {
                this.loadEntries(clazz);
            }
        } catch (FileNotFoundException e) {

        }

        Map<UUID, T> entries = (Map<UUID, T>) UuidObjectStorage.this.uuidObjectCache.get(clazz);
        if (entries == null) {
            entries = new HashMap<UUID, T>();
            this.uuidObjectCache.put(clazz, entries);
            this.persistEntries(clazz);
        }

        return entries;
    }

    private synchronized void persistEntries(Class<? extends UuidObject> clazz) throws FileNotFoundException {
        Appendable fileOutputStreamAppendable = new OutputStreamWriter(this.context.openFileOutput(clazz.getSimpleName() + ".json", Context.MODE_PRIVATE));
        Constants.GSON.toJson(this.uuidObjectCache.get(clazz), fileOutputStreamAppendable);
    }

    private synchronized <T extends UuidObject> void loadEntries(Class<T> clazz) throws FileNotFoundException {
        this.uuidObjectCache.remove(clazz);
        Reader fileInputStreamReader = new InputStreamReader(this.context.openFileInput(clazz.getSimpleName() + ".json"));
        Constants.GSON.fromJson("bla",String.class);

        final Map<UUID, T> entriesMap = Constants.GSON.fromJson(fileInputStreamReader, new TypeToken<Map<UUID, T>>() {
        }.getType());
        this.uuidObjectCache.put(clazz, entriesMap);
    }

}
