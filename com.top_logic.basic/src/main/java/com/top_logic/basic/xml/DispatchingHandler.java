/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** Handler for XML that will Disptach to other DefaultHandlers on startElement().
 * 
 * Namespaces are not well supported, just because the need was not there, yet.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DispatchingHandler extends DefaultHandler
{
    /** Stack of ContentHandler currently in use */
    private Stack<ContentHandler>  handlers;
    
    /** Stack of Names currently in use */
    private Stack<String>   names;

    /** Handler currently used to dispatch the Content events */
    private ContentHandler  current;

    /** Map of ContentHandler used to dispatch to in case a tag is found */
    private Map<String, ContentHandler>  dispatch;
    
    /** Name of tag for handler currently used to dispatch the Content events */
    protected String          currentName;

    /** Send character Events to current, too */
    protected final boolean needCharEvent;
    
    /**
     * @see #isCaptureMode()
     */
    private boolean _captureEvents;

    /**
     * Empty Ctor to set up the DispatchingHandler (without character events).
     */
    public DispatchingHandler() {
        this(false);
    }

    /** 
     * Create a new DispatchingHandler.
     * 
     * @param    needCharacterEvents    Flag, send character Events to current handler. 
     */
    public DispatchingHandler(boolean needCharacterEvents) {
        this.dispatch      = new HashMap<>();
        this.handlers      = new Stack<>();
        this.names         = new Stack<>();
        this.current       = new DefaultHandler();  // Avoids extra checking for empty Stacks
        this.currentName   = null;                  // No tag is current
        this.needCharEvent = needCharacterEvents;
    }

    protected int getStackSize(){
        return handlers.size(); 
    }
    
    protected ContentHandler getCurrent() {
        return current;
    }

	/**
	 * Starts capturing events.
	 * 
	 * @see #isCaptureMode()
	 * @see #captureStop()
	 */
    public final void captureStart() {
		setCaptureEvents(true);
	}
    
    /**
     * Stops capturing events. 
     * 
     * @see #captureStart()
     */
    public final void captureStop() {
    	setCaptureEvents(false);
    }
    
    private void setCaptureEvents(boolean captureEvents) {
    	if (this._captureEvents == captureEvents) {
    		throw new IllegalStateException("Already capturing events.");
    	}
		this._captureEvents = captureEvents;
	}
    
    /**
     * Whether event capturing is turned on.
     * 
	 * <p>
	 * In capture mode, all events are dispatched to the {@link #getCurrent()} handler. No new
	 * handlers are selected for a consumed tag name.
	 * </p>
     * 
     * @see #captureStart()
     * @see #captureStop()
     */
    public final boolean isCaptureMode() {
		return _captureEvents;
	}

    /** Register a ContentHandler that will be used as soon as the given name is parsed.
     *
     * @param name    local name of tag that will trigger dispatching to the given handler.
     * @param handler Will be activated when tag is found.
     * @return The {@link ContentHandler} formerly registered under the given name.
     */
    protected ContentHandler registerHandler(String name, ContentHandler handler) {
        return dispatch.put(name, handler);
    }
    
    /** Register a map of ContentHandlers
     *
     * @param someHandlers Map indexed by name containing ContentHandlers
     */
    protected void registerHandlers(Map<String, ContentHandler> someHandlers) {
        dispatch.putAll(someHandlers);
    }

    /** Unregister a ContentHandler that was used for the given name.
     *
     * @param name local name of tag that will trigge dispatching to the given handler.
     * @return The {@link ContentHandler} formerly registered under the given name.
     */
    protected ContentHandler unregisterHandler(String name) {
        return dispatch.remove(name);
    }

	/**
	 * Lookup the {@link ContentHandler} registered under the given element name.
	 * 
	 * @param name
	 *        The element name.
	 * @return The specialized {@link ContentHandler} that is responsible for parsing the given
	 *         element name. <code>null</code>, if no special handler is registerd.
	 */
	protected ContentHandler getHandler(String name) {
		return dispatch.get(name);
	}

    /** Look up dispatcher via qName and when found install it.
     *
     * See Documentation of superclass for parameters.
     */
    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
            throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (! isCaptureMode()) {
        	ContentHandler newHandler = getHandler(qName);
        	if (newHandler != null) { // Push current handler for later reuse
        		handlers.push(current);
        		names.push(currentName);
        		
        		current     = newHandler;
        		currentName = qName;
        		
        	}
        }
        current.startElement(uri, localName, qName, attributes); 
    }

    /**
     * Dispatch characters events to {@link #current} when needCharEvent is set.
     */
    @Override
	public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.needCharEvent) {
            this.getCurrent().characters(ch, start, length);
        }
        /* Super is a no-op anyway ... */
    }

    /** Reset the String Buffer for the next usage.
     *  See (incomplete :-) Documentation of superclass 
     *  for parameters (ignored here).
     */
    @Override
	public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        current.endElement(namespaceURI, localName, qName); 

        if (! isCaptureMode()) {
        	// Check if previous handler should be reinstalled                
        	if (qName.equals(currentName)) {
        		current     = handlers.pop();
        		currentName = names.pop();
        	}
        }
    }
    
    /** Not forwarded to current since it is usually not used in TL. */
    /*
    public void characters(char[] ch, int start, int length) throws SAXException {
        getCurrent().characters(ch, start, length);
    }
    */

    // Not forwarded we ignore ignorableWhitespaces
    // void ignorableWhitespace(char[] ch, int start, int length) 

    // Not forwarded since this should be handled by subclasses of this class.
    // public void setDocumentLocator(Locator locator) 

    /** Forward startDocument() to all handlers. */
    @Override
	public void startDocument() throws SAXException {
        // reset handler for the case that an exception was thrown before
        currentName = null;
        handlers.clear();
        for (Iterator<ContentHandler> iter = dispatch.values().iterator(); iter.hasNext();) {
            ContentHandler handler = iter.next();
            handler.startDocument();
        }
        super.startDocument();
    }
    
    /** Cleanup Stack (after Exceptions) and forward endDocument() to all handlers. */
    @Override
	public void endDocument() throws SAXException {
        assert currentName == null && handlers.size() == 0
             : "Handler Stack out of order ?";
        /*
        while (currentName != null)
            current     = (ContentHandler) handlers.pop();
            currentName = (String)         names.pop();
        }
        */
        // Call endDocument() for all registered Handlers, too
        for (Iterator<ContentHandler> iter = dispatch.values().iterator(); iter.hasNext();) {
            ContentHandler handler = iter.next();
            handler.endDocument();
        }
        super.endDocument();
    }
    
    // All other events of ContentHandler are forwarded

    /** Just forward the Events to the current Handler. */
    @Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
        current.startPrefixMapping(prefix, uri);
    }

    /** Just forward the Events to the current Handler. */
    @Override
	public void endPrefixMapping(String prefix) throws SAXException {
        current.endPrefixMapping(prefix);
    }

    /** Just forward the Events to the current Handler. */
    @Override
	public void processingInstruction(String target, String data) throws SAXException {
        current.processingInstruction(target, data);
    } 
    
    /** Just forward the Events to the current Handler. */
    @Override
	public void skippedEntity(String name) throws SAXException {
        current.skippedEntity(name);
    }


}
