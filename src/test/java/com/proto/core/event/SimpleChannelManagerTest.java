package com.proto.core.event;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author wfrancis
 */
public class SimpleChannelManagerTest {

    @Test
    public void testExtractWildRoot() {
        assertThat(SimpleChannelManager.extractWildRoot("1.2.3.4.>"), is("1.2.3.4."));
    }

    @Test
    public void testRegularRegistration() throws Exception {
        SimpleChannelManager manager = new SimpleChannelManager(new SimpleEventChannel());
        Listener listener1 = mock(Listener.class);
        manager.registerListener(new StringEventKey("1"), listener1);
        Listener listener2 = mock(Listener.class);
        manager.registerListener(new StringEventKey("2"), listener2);
        Listener listener3 = mock(Listener.class);
        manager.registerListener(new StringEventKey("2"), listener3);

        Object event = new Object();
        EventKey key = new StringEventKey("2");
        manager.consume(key, event);

        Thread.sleep(100);

        verify(listener1, times(0)).consume(key, event);
        verify(listener2, times(1)).consume(key, event);
        verify(listener3, times(1)).consume(key, event);
    }

    @Test
    public void testWildcardRegistration1() throws Exception {
        SimpleChannelManager manager = new SimpleChannelManager(new SimpleEventChannel());

        Listener listener1 = mock(Listener.class);
        manager.registerListener(new StringEventKey("11"), listener1);

        Listener listener2 = mock(Listener.class);
        manager.registerListener(new StringEventKey("22"), listener2);

        Listener listener3 = mock(Listener.class);
        manager.registerListener(new StringEventKey("2>"), listener3);

        Object event = new Object();
        EventKey key = new StringEventKey("22");
        manager.consume(key, event);

        Thread.sleep(100);

        verify(listener1, times(0)).consume(key, event);
        verify(listener2, times(1)).consume(key, event);
        verify(listener3, times(1)).consume(key, event);
        verifyNoMoreInteractions(listener1, listener2, listener3);
    }

    @Test
    public void testWildcardRegistration2() throws Exception {
        SimpleChannelManager manager = new SimpleChannelManager(new SimpleEventChannel());

        Listener listener1 = mock(Listener.class);
        manager.registerListener(new StringEventKey("11"), listener1);

        Listener listener2 = mock(Listener.class);
        manager.registerListener(new StringEventKey("2>"), listener2);

        Object event1 = new Object();
        EventKey key1 = new StringEventKey("22");
        manager.consume(key1, event1);

        Object event2 = new Object();
        EventKey key2 = new StringEventKey("11");
        manager.consume(key2, event2);

        Thread.sleep(100);

        verify(listener1, times(1)).consume(key2, event2);
        verify(listener2, times(1)).consume(key1, event1);
        verifyNoMoreInteractions(listener1, listener2);
    }
}
