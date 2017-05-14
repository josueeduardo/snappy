package io.joshworks.snappy.metric;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Josh Gontijo on 5/14/17.
 */
public class RingBuffer<T> extends ConcurrentLinkedQueue<T> {

    private int pos = 0;
    private final int size;

    private RingBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than greater than zero");
        }
        this.size = size;
    }

    public static <T> RingBuffer<T> ofSize(int size) {
        return new RingBuffer<>(size);
    }

    private void checkPosInsert() {
        if (pos++ >= size) {
            poll();
            pos = size;
        }
    }

    private void checkPosRemove() {
        if (pos > 0) {
           pos--;
        }
    }

    @Override
    public boolean offer(T t) {
        checkPosInsert();
        return super.offer(t);
    }

    @Override
    public T poll() {
        T poll = super.poll();
        checkPosRemove();
        return poll;
    }

    @Override
    public T peek() {
        T peek = super.peek();
        checkPosRemove();
        return peek;
    }

    @Override
    public void clear() {
        pos = 0;
        super.clear();
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = super.remove(o);
        checkPosRemove();
        return remove;
    }

    @Override
    public T remove() {
        T remove = super.remove();
        checkPosRemove();
        return remove;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported");
    }
}
