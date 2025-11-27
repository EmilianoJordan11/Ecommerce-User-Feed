package com.emijor.user_feed.utils.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Un Map con expiración automática de elementos.
 * Esta clase contiene un thread que periódicamente verifica si algún objeto
 * debe ser eliminado basándose en el tiempo de vida configurado.
 * 
 * Se utiliza para cachear tokens de usuario y evitar consultas repetitivas al servicio Auth.
 *
 * @param <K> Tipo de la clave (generalmente el token)
 * @param <V> Tipo del valor (generalmente User)
 */
public class ExpiringMap<K, V> implements Map<K, V> {

    public static final int DEFAULT_TIME_TO_LIVE = 60;
    public static final int DEFAULT_EXPIRATION_INTERVAL = 1;

    private static volatile int expirerCount = 1;

    private final ConcurrentHashMap<K, ExpiringObject> delegate;
    private final CopyOnWriteArrayList<ExpirationListener<V>> expirationListeners;
    private final Expirer expirer;

    public ExpiringMap() {
        this(DEFAULT_TIME_TO_LIVE, DEFAULT_EXPIRATION_INTERVAL);
    }

    public ExpiringMap(int timeToLive) {
        this(timeToLive, DEFAULT_EXPIRATION_INTERVAL);
    }

    /**
     * Crea una nueva instancia de ExpiringMap.
     *
     * @param timeToLive          Tiempo de vida en segundos
     * @param expirationInterval  Intervalo de verificación en segundos
     */
    public ExpiringMap(int timeToLive, int expirationInterval) {
        this.delegate = new ConcurrentHashMap<>();
        this.expirationListeners = new CopyOnWriteArrayList<>();
        this.expirer = new Expirer();
        expirer.setTimeToLive(timeToLive);
        expirer.setExpirationInterval(expirationInterval);
    }

    @Override
    public V put(K key, V value) {
        ExpiringObject answer = delegate.put(key, new ExpiringObject(key, value, System.currentTimeMillis()));
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override
    public V get(Object key) {
        ExpiringObject object = delegate.get(key);
        if (object != null) {
            object.setLastAccessTime(System.currentTimeMillis());
            return object.getValue();
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        ExpiringObject answer = delegate.remove(key);
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> inMap) {
        for (Entry<? extends K, ? extends V> e : inMap.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public void addExpirationListener(ExpirationListener<V> listener) {
        expirationListeners.add(listener);
    }

    public void removeExpirationListener(ExpirationListener<V> listener) {
        expirationListeners.remove(listener);
    }

    public Expirer getExpirer() {
        return expirer;
    }

    public int getExpirationInterval() {
        return expirer.getExpirationInterval();
    }

    public int getTimeToLive() {
        return expirer.getTimeToLive();
    }

    public void setExpirationInterval(int expirationInterval) {
        expirer.setExpirationInterval(expirationInterval);
    }

    public void setTimeToLive(int timeToLive) {
        expirer.setTimeToLive(timeToLive);
    }

    /**
     * Objeto interno que almacena el valor junto con su tiempo de último acceso.
     */
    private class ExpiringObject {
        private final K key;
        private final V value;
        private long lastAccessTime;
        private final ReadWriteLock lastAccessTimeLock = new ReentrantReadWriteLock();

        ExpiringObject(K key, V value, long lastAccessTime) {
            if (value == null) {
                throw new IllegalArgumentException("An expiring object cannot be null.");
            }
            this.key = key;
            this.value = value;
            this.lastAccessTime = lastAccessTime;
        }

        public long getLastAccessTime() {
            lastAccessTimeLock.readLock().lock();
            try {
                return lastAccessTime;
            } finally {
                lastAccessTimeLock.readLock().unlock();
            }
        }

        public void setLastAccessTime(long lastAccessTime) {
            lastAccessTimeLock.writeLock().lock();
            try {
                this.lastAccessTime = lastAccessTime;
            } finally {
                lastAccessTimeLock.writeLock().unlock();
            }
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return value.equals(obj);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    /**
     * Thread que monitorea el ExpiringMap y elimina elementos que han expirado.
     */
    public class Expirer implements Runnable {
        private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
        private long timeToLiveMillis;
        private long expirationIntervalMillis;
        private boolean running = false;
        private final Thread expirerThread;

        public Expirer() {
            expirerThread = new Thread(this, "ExpiringMapExpirer-" + expirerCount++);
            expirerThread.setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                processExpires();
                try {
                    Thread.sleep(expirationIntervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void processExpires() {
            long timeNow = System.currentTimeMillis();
            for (ExpiringObject o : delegate.values()) {
                if (timeToLiveMillis <= 0) {
                    continue;
                }
                long timeIdle = timeNow - o.getLastAccessTime();
                if (timeIdle >= timeToLiveMillis) {
                    delegate.remove(o.getKey());
                    for (ExpirationListener<V> listener : expirationListeners) {
                        listener.expired(o.getValue());
                    }
                }
            }
        }

        public void startExpiring() {
            stateLock.writeLock().lock();
            try {
                if (!running) {
                    running = true;
                    expirerThread.start();
                }
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        public void startExpiringIfNotStarted() {
            stateLock.readLock().lock();
            try {
                if (running) {
                    return;
                }
            } finally {
                stateLock.readLock().unlock();
            }

            stateLock.writeLock().lock();
            try {
                if (!running) {
                    running = true;
                    expirerThread.start();
                }
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        public void stopExpiring() {
            stateLock.writeLock().lock();
            try {
                if (running) {
                    running = false;
                    expirerThread.interrupt();
                }
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        public boolean isRunning() {
            stateLock.readLock().lock();
            try {
                return running;
            } finally {
                stateLock.readLock().unlock();
            }
        }

        public int getTimeToLive() {
            stateLock.readLock().lock();
            try {
                return (int) timeToLiveMillis / 1000;
            } finally {
                stateLock.readLock().unlock();
            }
        }

        public void setTimeToLive(long timeToLive) {
            stateLock.writeLock().lock();
            try {
                this.timeToLiveMillis = timeToLive * 1000;
            } finally {
                stateLock.writeLock().unlock();
            }
        }

        public int getExpirationInterval() {
            stateLock.readLock().lock();
            try {
                return (int) expirationIntervalMillis / 1000;
            } finally {
                stateLock.readLock().unlock();
            }
        }

        public void setExpirationInterval(long expirationInterval) {
            stateLock.writeLock().lock();
            try {
                this.expirationIntervalMillis = expirationInterval * 1000;
            } finally {
                stateLock.writeLock().unlock();
            }
        }
    }

    /**
     * Interfaz para escuchar eventos de expiración.
     */
    public interface ExpirationListener<E> {
        void expired(E expiredObject);
    }
}
