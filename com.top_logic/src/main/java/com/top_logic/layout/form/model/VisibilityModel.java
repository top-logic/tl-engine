/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * Observable visibility property.
 * 
 * @see #isVisible()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface VisibilityModel extends PropertyObservable {

	/**
	 * Type of the <code>visible</code> property.
	 * 
	 * @see #isVisible()
	 */
	public static final EventType<VisibilityListener, Object, Boolean> VISIBLE_PROPERTY =
		LayoutComponent.VISIBILITY_EVENT;

	/**
	 * Whether this model is visible.
	 * 
	 * <p>
	 * The <code>visible</code> property decides, whether a view corresponding to this model is
	 * currently displayed on the user interface.
	 * </p>
	 * 
	 * <p>
	 * The <code>visible</code> property is observable through the {@link #VISIBLE_PROPERTY} event
	 * key and the {@link VisibilityListener} interface.
	 * </p>
	 */
	public boolean isVisible();

	/**
	 * Set the <code>visible</code> property of this form member.
	 * 
	 * <p>
	 * Optional operation: A read-only implementation may throw an
	 * {@link UnsupportedOperationException}.
	 * </p>
	 * 
	 * <p>
	 * If the property changes, a {@link #VISIBLE_PROPERTY} event is fired.
	 * </p>
	 * 
	 * @see #isVisible()
	 * @see #VISIBLE_PROPERTY
	 */
	public void setVisible(boolean isVisible);

	/**
	 * Constant {@link VisibilityModel} that is always visible.
	 */
	class AlwaysVisible implements VisibilityModel {
	
		/**
		 * Singleton {@link VisibilityModel.AlwaysVisible} instance.
		 */
		public static final AlwaysVisible INSTANCE = new AlwaysVisible();
	
		private AlwaysVisible() {
			// Singleton constructor.
		}
	
		@Override
		public <L extends PropertyListener, S, V> boolean addListener(EventType<L, S, V> type, L listener) {
			return false;
		}
	
		@Override
		public <L extends PropertyListener, S, V> boolean removeListener(EventType<L, S, V> type, L listener) {
			return false;
		}
	
		@Override
		public boolean isVisible() {
			return true;
		}
	
		@Override
		public void setVisible(boolean isVisible) {
			throw new UnsupportedOperationException("Cannot change constant visibility model.");
		}

		/**
		 * Use {@link AlwaysVisible}, if the given model is <code>null</code>.
		 */
		public static VisibilityModel ifNull(VisibilityModel visibility) {
			return visibility == null ? INSTANCE : visibility;
		}
	}

	/**
	 * {@link VisibilityModel} that can be updated.
	 */
	class Default extends PropertyObservableBase implements VisibilityModel {

		private boolean _visible;

		/**
		 * Creates a {@link Default} that is visible by default.
		 */
		public Default() {
			this(true);
		}

		/**
		 * Creates a {@link Default} with the given default visibility.
		 * 
		 * @param defaultVisibility
		 *        See {@link #isVisible()}.
		 */
		public Default(boolean defaultVisibility) {
			_visible = defaultVisibility;
		}

		@Override
		public boolean isVisible() {
			return _visible;
		}

		@Override
		public void setVisible(boolean newValue) {
			if (newValue == _visible) {
				return;
			}
			Boolean oldValue = Boolean.valueOf(_visible);
			_visible = newValue;
			notifyListeners(VISIBLE_PROPERTY, self(), oldValue, Boolean.valueOf(newValue));
		}

		/**
		 * The event sender.
		 * 
		 * <p>
		 * Subclasses that use this implementation as delegate may override this to adjust the event
		 * sender to the proxy model.
		 * </p>
		 */
		protected Object self() {
			return this;
		}

		/**
		 * Use {@link AlwaysVisible}, if the given model is <code>null</code>.
		 */
		public static VisibilityModel ifNull(VisibilityModel visibility) {
			return visibility == null ? new Default() : visibility;
		}
	}

}
