/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.encryption.SecureRandomService;

/**
 * {@link AbstractCompositeContentHandler} is an abstract {@link CompositeContentHandler} which
 * handles registration of {@link CompositeContentHandler} and creation of id's.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCompositeContentHandler extends AbstractDispatchingContentHandler implements
		CompositeContentHandler, IdentifierSource {

	private BidiHashMap<String, ContentHandler> lazyContentHandlers;

	private int currentID;

	/**
	 * Creates a {@link AbstractCompositeContentHandler}.
	 */
	public AbstractCompositeContentHandler() {
		currentID = SecureRandomService.getInstance().getRandom().nextInt(10000);
	}

	/**
	 * Whether some {@link ContentHandler}s are registered.
	 * 
	 * @see #registerContentHandler(String, ContentHandler)
	 */
	public boolean hasContentHandlers() {
		return lazyContentHandlers != null && lazyContentHandlers.size() > 0;
	}

	/**
	 * Whether a {@link ContentHandler} is registered for the given ID.
	 * 
	 * @see #registerContentHandler(String, ContentHandler)
	 */
	public boolean hasContentHandler(String id) {
		return lazyContentHandlers != null && lazyContentHandlers.containsKey(id);
	}

	/**
	 * Returns an unmodifiable view of the sub-locations this {@link ContentHandler} can serve.
	 */
	public Set<String> getLocations() {
		if (lazyContentHandlers == null) {
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(lazyContentHandlers.keySet());
	}

	/**
	 * The number of registered {@link ContentHandler}s.
	 */
	public int getContentHandlerCount() {
		return lazyContentHandlers == null ? 0 : lazyContentHandlers.size();
	}

	@Override
	public boolean deregisterContentHandler(ContentHandler handler) {
		if (lazyContentHandlers == null) {
			return false;
		}
		final Object registeredProvider = lazyContentHandlers.removeValue(handler);
		if (registeredProvider == null) {
			return false;
		}
		return true;
	}

	/**
	 * Tries to register the handler under the given id.
	 * 
	 * @throws IllegalArgumentException
	 *         if a different handler was already registered under the given id.
	 * 
	 * @see CompositeContentHandler#registerContentHandler(String, ContentHandler)
	 */
	@Override
	public void registerContentHandler(String id, ContentHandler handler) {
		assert handler != this : "Try to add '" + handler + "' as content handler to itself!";
		if (lazyContentHandlers == null) {
			lazyContentHandlers = new BidiHashMap<>();
		}
		if (id != null) {
			final Object formerlyUsedKey = lazyContentHandlers.getKey(handler);
			if (formerlyUsedKey != null && !formerlyUsedKey.equals(id)) {
				throw new IllegalArgumentException("The given ContentHandler was already registered using a different id: '" + formerlyUsedKey + "'");
			}

			final ContentHandler formerlyRegistered = lazyContentHandlers.put(id, handler);
			if (formerlyRegistered != null && !formerlyRegistered.equals(handler)) {
				lazyContentHandlers.put(id, formerlyRegistered);
				throw new IllegalArgumentException("A different ContentHandler was registered for the same id: '" + id + "'");
			}
		} else {
			lazyContentHandlers.put(createNewID(), handler);
		}
	}

	@Override
	protected final ContentHandler getContentHandler(String id) {
		if (lazyContentHandlers == null) {
			return null;
		} else {
			return lazyContentHandlers.get(id);
		}
	}

	/**
	 * Returns the id which under which the given ContentHandler was registered,
	 * or <code>null</code> if the given Handler was not registered.
	 * 
	 * <p>
	 * Note: This method is not declared in the {@link CompositeContentHandler} interface, because
	 * in general, the same {@link ContentHandler} could be registered for different path elements.
	 * </p>
	 */
	public final String getID(ContentHandler handler) {
		if (lazyContentHandlers == null) {
			return null;
		} else {
			return lazyContentHandlers.getKey(handler);
		}
	}

	@Override
	public URLBuilder getURL(DisplayContext context, ContentHandler handler) {
		final URLBuilder url = getURL(context);
		
		// The following happens if some handler registers at one context, but
		// takes a URL from a different one. (see #3553)
		assert lazyContentHandlers.containsValue(handler) : "The given handler " + handler + " is not registered at this "
				+ CompositeContentHandler.class.getSimpleName() + ": " + this;

		url.addResource(lazyContentHandlers.getKey(handler));
		return url;
	}

	@Override
	public final String toString() {
		StringBuilder stringRepresentation = new StringBuilder(getClass().getSimpleName());
		stringRepresentation.append('[');
		appendToString(stringRepresentation);
		stringRepresentation.append(']');
		return stringRepresentation.toString();
	}

	/**
	 * Appends things to be displayed by {@link #toString()} to the given {@link StringBuilder}.
	 * 
	 * @param stringRepresentation
	 *        never <code>null</code>
	 */
	protected void appendToString(StringBuilder stringRepresentation) {
		if (lazyContentHandlers == null || lazyContentHandlers.isEmpty()) {
			stringRepresentation.append("No ContentHandlers");
		} else {
			stringRepresentation.append("ContentHandlers:'").append(lazyContentHandlers).append('\'');
		}
	}

	@Override
	public Collection<? extends ContentHandler> inspectSubHandlers() {
		if (lazyContentHandlers == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(lazyContentHandlers.values());
	}

	/**
	 * creates a new id of the form 'cZZZ' where ZZZ is some integer.
	 * 
	 * @see IdentifierSource#createNewID()
	 */
	@Override
	public final String createNewID() {
		return new StringBuilder(6).append('c').append(currentID++).toString();
	}

}
