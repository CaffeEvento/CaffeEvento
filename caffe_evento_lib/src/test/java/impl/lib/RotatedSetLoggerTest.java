package impl.lib;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by chris on 7/15/16.
 */
public class RotatedSetLoggerTest {
    private RotatedSetLogger<Integer> instance;

    @Before
    public void setUp() {
        instance = new RotatedSetLogger<>(2);
    }

    @Test
    public void simpleTestContains() {
        instance.add(5);
        instance.add(3);
        assertTrue(instance.contains(5));
    }

    @Test
    public void testContainsAfterRotate() {
        instance.add(5);
        instance.add(3);
        instance.rotateLog();
        assertTrue(instance.contains(5));
        instance.add(6);
        assertTrue(instance.contains(6));
        assertTrue(instance.contains(5));
    }

    @Test
    public void testFlushAfterQueueRotation() {
        instance.add(5);
        instance.add(3);
        instance.rotateLog();
        assertTrue(instance.contains(5));
        instance.add(6);
        assertTrue(instance.contains(6));
        instance.rotateLog();
        instance.add(4);
        assertTrue(instance.contains(6));
        assertTrue(instance.contains(4));
        assertFalse(instance.contains(5));
        assertFalse(instance.contains(3));
    }

    @Test
    public void testSetContents() {
        Set<Integer> contents = Sets.newHashSet(5, 3);
        contents.forEach(instance::add);
        assertTrue(instance.contents().containsAll(contents));
    }

    @Test
    public void testRotatingContents() {
        Set<Integer> contents1 = Sets.newHashSet(5, 3);
        Set<Integer> contents2 = Sets.newHashSet(6, 7);
        Set<Integer> contents3 = Sets.newHashSet(9, 42);
        contents1.forEach(instance::add);
        assertEquals(2, instance.contents().size());
        assertTrue(instance.contents().containsAll(contents1));
        instance.rotateLog();
        assertEquals(2, instance.contents().size());
        assertTrue(instance.contents().containsAll(contents1));

        contents2.forEach(instance::add);
        assertEquals(4, instance.contents().size());
        assertTrue(instance.contents().containsAll(contents1));
        assertTrue(instance.contents().containsAll(contents2));
        instance.rotateLog();
        assertEquals(2, instance.contents().size());
        assertFalse(instance.contents().containsAll(contents1));
        assertTrue(instance.contents().containsAll(contents2));

        contents3.forEach(instance::add);
        assertEquals(4, instance.contents().size());
        assertFalse(instance.contents().containsAll(contents1));
        assertTrue(instance.contents().containsAll(contents2));
        assertTrue(instance.contents().containsAll(contents3));
    }


}
