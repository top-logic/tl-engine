package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: EventAwareTestCase.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2004, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import junit.framework.TestCase;
import junit.framework.Assert;

import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class EventAwareTestCase extends TestCase {

    private static ArrayList _events;


    public EventAwareTestCase( String name ) {
        super( name );
    }


    protected static void expectEvent( String eventName, Class listenerClass ) {
        _events.add( new EventData( eventName, listenerClass ) );
    }


    protected static void expectEvent( String eventName, Class listenerClass, EventVerifier verifier ) {
        _events.add( new EventData( eventName, listenerClass, verifier ) );
    }


    protected static void sendEvent( String eventName, Object listener, Object eventObject ) {
        assertFalse( "Unexpected event: " + EventData.toEventString( eventName, listener.getClass() ), _events.isEmpty() );
        ((EventData) _events.remove( 0 )).verifyEvent( eventName, listener, eventObject );
    }


    protected static void verifyEvents() {
        if (!_events.isEmpty()) fail( "Did not receive event " + _events.get(0) );
    }


    protected static void clearEvents() {
        _events = new ArrayList();
    }


    interface EventVerifier {
        public void verifyEvent( String eventLabel, Object eventObject );
    }

}




class EventData {
    private String _eventName;
    private Class _listenerClass;
    private EventAwareTestCase.EventVerifier _verifier;

    static String toEventString( String eventName, Class listenerClass ) {
        return eventName + " from " + listenerClass.getName();
    }


    EventData( String eventName, Class listenerClass ) {
        this( eventName, listenerClass, null );
    }


    EventData( String eventName, Class listenerClass, EventAwareTestCase.EventVerifier verifier ) {
        _eventName = eventName;
        _listenerClass = listenerClass;
        _verifier = verifier;
    }


    void verifyEvent( String eventName, Object listener, Object event ) {
        Assert.assertEquals( "Event", toString(),  toEventString( eventName, listener.getClass() ) );
        if (_verifier != null) _verifier.verifyEvent( toString(), event );
    }


    public String toString() {
        return toEventString( _eventName, _listenerClass );
    }

}
