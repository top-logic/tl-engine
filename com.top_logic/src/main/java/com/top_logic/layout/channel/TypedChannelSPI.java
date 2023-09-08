/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelSPI} creating a {@link ComponentChannel} with values limited to a certain
 * {@link Class} type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypedChannelSPI<T> extends DefaultChannelSPI {

	private Class<T> _type;

	/**
	 * Creates a {@link TypedChannelSPI}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param type
	 *        See {@link #getType()}.
	 * @param initialValue
	 *        See {@link #getInitialValue()}.
	 */
	public TypedChannelSPI(String name, Class<T> type, T initialValue) {
		super(name, initialValue);
		_type = type;
	}

	/**
	 * The value type that {@link ComponentChannel}s are limited to.
	 */
	public Class<T> getType() {
		return _type;
	}

	@Override
	protected ComponentChannel createImpl(LayoutComponent component) {
		return new Impl(component, "typed(" + getName() + ", " + _type.getName() + ")", _type, getInitialValue());
	}

	/**
	 * {@link ComponentChannel} with values limited by a certain {@link Class} type.
	 */
	protected static class Impl extends DefaultChannel {

		private Class<?> _type;

		/**
		 * Creates a {@link Impl}.
		 *
		 * @param component
		 *        See {@link #getComponent()}.
		 * @param name
		 *        Channel name for debugging.
		 * @param type
		 *        See {@link #getType()}.
		 * @param initialValue
		 *        The initial value, see {@link #get()}.
		 */
		public Impl(LayoutComponent component, String name, Class<?> type, Object initialValue) {
			super(component, name, initialValue);
			_type = type;
		}

		/**
		 * The type all values of this channel must be compatible with (<code>null</code> is
		 * excluded).
		 */
		public Class<?> getType() {
			return _type;
		}

		@Override
		protected void storeValue(Object newValue, Object oldValue) {
			if (!_type.isInstance(newValue)) {
				return;
			}
			super.storeValue(newValue, oldValue);
		}

	}

}
