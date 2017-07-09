package io.joshworks.snappy.extensions.dashboard.metrics;

import io.joshworks.snappy.extensions.dashboard.RingBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 5/14/17.
 */
public class RingBufferTest {

    @Test(expected = IllegalArgumentException.class)
    public void empty() throws Exception {
        RingBuffer.ofSize(0);
    }

    @Test
    public void noOverflow() throws Exception {

        RingBuffer<String> buffer = RingBuffer.ofSize(2);
        buffer.add("a");
        buffer.add("b");

        assertEquals(2, buffer.size());
    }

    @Test
    public void overflow() throws Exception {
        int size = 2;
        RingBuffer<String> buffer = RingBuffer.ofSize(size);
        buffer.add("a");
        buffer.add("b");
        buffer.add("c");

        assertEquals(size, buffer.size());
        assertEquals("b", buffer.poll());
        assertEquals("c", buffer.poll());
    }

    @Test
    public void overflows() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(1);
        buffer.add("a");
        buffer.add("b");
        buffer.add("c");
        buffer.add("d");

        assertEquals(1, buffer.size());
        assertEquals("d", buffer.poll());
    }

    @Test
    public void multipleCicles() throws Exception {
        int size = 2;
        int iterations = 1000000;
        RingBuffer<Integer> buffer = RingBuffer.ofSize(size);

        for (int i = 0; i <= iterations; i++) {
            buffer.add(i);
        }

        assertEquals(size, buffer.size());

        assertEquals((iterations - 1), (int) buffer.poll());
        assertEquals(iterations, (int) buffer.poll());
    }

    @Test
    public void clear() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(1);
        buffer.add("a");
        buffer.add("b");

        buffer.clear();
        assertEquals(0, buffer.size());

        buffer.add("a");
        buffer.add("b");
        assertEquals(1, buffer.size());
        assertEquals("b", buffer.poll());

    }

    @Test
    public void remove_noElementleft() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(1);
        buffer.add("a");
        buffer.add("b");

        buffer.remove();
        assertEquals(0, buffer.size());

    }

    @Test
    public void remove_singleElementLeft() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(2);
        buffer.add("a");
        buffer.add("b");
        buffer.add("c");

        assertEquals("b", buffer.remove());
        assertEquals(1, buffer.size());
    }

    @Test
    public void remove_add() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(2);
        buffer.add("a");
        buffer.remove();
        buffer.add("c");
        buffer.remove();
        buffer.add("d");

        assertEquals("d", buffer.remove());
        assertTrue(buffer.isEmpty());
    }

    @Test
    public void peek() throws Exception {
        RingBuffer<String> buffer = RingBuffer.ofSize(1);
        buffer.add("a");
        buffer.add("b");
        buffer.add("c");
        buffer.add("d");

        assertEquals(1, buffer.size());
        assertEquals("d", buffer.peek());
    }
}