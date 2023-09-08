/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.layout.form.FormMember;

/**
 * Model that supports focus requests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Focusable extends PropertyObservable {

	/**
	 * {@link PropertyListener} for {@link Focusable#focus()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	interface FocusRequestedListener extends PropertyListener {

		/**
		 * Handles focus request.
		 * 
		 * @param sender
		 *        The object to focus.
		 * 
		 * @return Whether this event shall bubble.
		 */
		Bubble handleFocusRequested(Focusable sender);

	}

	/**
	 * Whether this {@link FormMember} should receive the client-side focus.
	 * 
	 * <p>
	 * This event is only sent, if this member is an atomic member. Containers dispatch to some of
	 * its contents.
	 * </p>
	 */
	EventType<FocusRequestedListener, Focusable, Boolean> FOCUS_PROPERTY =
		new EventType<>("focus") {

		@Override
		public Bubble dispatch(FocusRequestedListener listener, Focusable sender, Boolean oldValue, Boolean newValue) {
			return listener.handleFocusRequested(sender);
		}

	};

	/**
	 * Sets the client-side focus to the first input element.
	 * 
	 * @return Whether the focus request was accepted, <code>false</code>, if no input element was
	 *         found inside this member that was able to accept the focus request. The result is
	 *         <code>true</code>, even if the found input element already has a pending focus
	 *         request.
	 * 
	 * @see FocusHandling#shouldFocus(com.top_logic.layout.DisplayContext, Focusable)
	 */
	public boolean focus();

}
