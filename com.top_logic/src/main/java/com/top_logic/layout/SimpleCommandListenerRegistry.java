/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The class {@link SimpleCommandListenerRegistry} is a simple implementation of
 * {@link CommandListenerRegistry}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleCommandListenerRegistry implements CommandListenerRegistry {

	/**
	 * Lazily initialized map of all {@link CommandListener} for executing
	 * commands. The map maps {@link CommandListener#getID() listener
	 * identifiers} to {@link CommandListener}.
	 */
	private HashMap<String, CommandListener> lazyCommandListener;

	/**
	 * flag that indicates whether currently a {@link #clear()} is running
	 * 
	 * @see #clear()
	 */
	private boolean clearing;

	/**
	 * Registers the given {@link CommandListener} for executing commands.
	 * 
	 * @throws IllegalArgumentException
	 *         if another {@link CommandListener} is already registered under
	 *         the ID of <code>aCommandListener</code>.
	 */
	@Override
	public void addCommandListener(CommandListener listener) {
		if (clearing) {
			throw new IllegalStateException(SimpleCommandListenerRegistry.class.getSimpleName() + " is currently clearing its listener.");
		}

		if (lazyCommandListener == null) {
			lazyCommandListener = new HashMap<>();
		}

		CommandListener formerlyRegisteredCommandListener = lazyCommandListener.put(listener.getID(), listener);
		if (formerlyRegisteredCommandListener != null) {
			if (listener != formerlyRegisteredCommandListener) {
				// Put back formerly registered control and throw an exception.
				// This is an optimization that avoids checking for existence
				// before adding the new control.
				lazyCommandListener.put(formerlyRegisteredCommandListener.getID(), formerlyRegisteredCommandListener);
				throw new IllegalArgumentException("Different listener with the same ID '" + listener.getID() + "'is already registered.");
			}
		} else {
			listener.notifyAttachedTo(this);
		}
	}

	/**
	 * Removes the given {@link CommandListener}.
	 * 
	 * @throws IllegalArgumentException
	 *         if another {@link CommandListener} was registered under the ID of
	 *         <code>aCommandListener</code>.
	 */
	@Override
	public boolean removeCommandListener(CommandListener listener) {
		if (lazyCommandListener == null) {
			return false;
		}

		if (clearing) {
			return lazyCommandListener.get(listener.getID()) == listener;
		}

		String id = listener.getID();
		CommandListener deregisteredListener = lazyCommandListener.remove(id);
		if (deregisteredListener == null) {
			return false;
		}
		if (deregisteredListener != listener) {
			lazyCommandListener.put(id, deregisteredListener);
			throw new IllegalArgumentException("Different listener with the same ID '" + listener.getID() + "' was registered.");
		}

		deregisteredListener.notifyDetachedFrom(this);
		return true;
	}

	/**
	 * Returns the listener registered under the given ID or <code>null</code>
	 * if there is none.
	 * 
	 * @param id
	 *        the id of the required {@link CommandListener}
	 */
	@Override
	public CommandListener getCommandListener(String id) {
		if (lazyCommandListener != null) {
			return lazyCommandListener.get(id);
		} else {
			return null;
		}
	}

	/**
	 * @see com.top_logic.layout.CommandListenerRegistry#getCommandListener()
	 */
	@Override
	public Collection<CommandListener> getCommandListener() {
		if (lazyCommandListener == null) {
			return new ArrayList<>(0);
		} else {
			return new ArrayList<>(lazyCommandListener.values());
		}
	}

	/**
	 * Removes all currently registered {@link CommandListener} from this
	 * registry.
	 */
	@Override
	public void clear() {
		if (lazyCommandListener == null || lazyCommandListener.isEmpty()) {
			return;
		}
		if (clearing) {
			throw new IllegalStateException(SimpleCommandListenerRegistry.class.getSimpleName() + " is currently clearing its listener.");
		}
		clearing = true;
		try {
			for (Iterator<CommandListener> it = lazyCommandListener.values().iterator(); it.hasNext();) {
				CommandListener nextListener = it.next();
				nextListener.notifyDetachedFrom(this);
			}
			lazyCommandListener.clear();
		} finally {
			clearing = false;
		}
	}

}
