/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;

/**
 * Model representing a view that can be maximized and minimized.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Expandable extends PropertyObservable {

	/**
	 * Property key for observing the {@link #getExpansionState()} property.
	 * 
	 * @see #addListener(EventType, PropertyListener)
	 */
	public static final EventType<ExpandableListener, Expandable, ExpansionState> STATE =
		new EventType<>("state") {
			@Override
			public com.top_logic.basic.listener.EventType.Bubble dispatch(ExpandableListener listener,
					Expandable sender,
					ExpansionState oldValue, ExpansionState newValue) {
				return listener.notifyExpansionStateChanged(sender, oldValue, newValue);
			}
		};

	/**
	 * The current display {@link ExpansionState}.
	 * 
	 * <p>
	 * This property is observable using the {@link #STATE} property key.
	 * </p>
	 * 
	 * @see #addListener(EventType, PropertyListener)
	 */
	ExpansionState getExpansionState();

	/**
	 * @see #getExpansionState()
	 */
	void setExpansionState(ExpansionState newState);

	/**
	 * The states a {@link CollapsibleControl} can have.
	 */
	public enum ExpansionState {
		/**
		 * The content is displayed regularly.
		 */
		NORMALIZED,
	
		/**
		 * The content is taken out of normal flow and displayed minimized.
		 */
		MINIMIZED,
	
		/**
		 * The content is taken out of normal flow and displayed maximized.
		 */
		MAXIMIZED,

		/**
		 * The content is not displayed.
		 */
		HIDDEN;

		/**
		 * Throws an {@link UnreachableAssertion} that no such {@link Expandable.ExpansionState}
		 * exists.
		 */
		public static UnreachableAssertion noSuchExpansionState(Expandable.ExpansionState obj) {
			throw new UnreachableAssertion("Unknown " + Expandable.ExpansionState.class.getName() + ": " + obj);
		}

		/**
		 * The toggled state {@link #NORMALIZED} <-> {@link #MINIMIZED}.
		 */
		public ExpansionState toggleMinimized() {
			switch (this) {
				case HIDDEN:
				case MAXIMIZED:
					return NORMALIZED;
				case MINIMIZED:
					return NORMALIZED;
				case NORMALIZED:
					return MINIMIZED;
			}
			throw new UnreachableAssertion("No such state: " + this);
		}
	}

}
