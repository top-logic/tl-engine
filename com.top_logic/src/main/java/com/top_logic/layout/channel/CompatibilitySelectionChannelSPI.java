/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link SelectionChannelSPI} that creates {@link ComponentChannel} which unwraps collection values
 * for single-selection components and wrap non-collection values for multi-selection components.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompatibilitySelectionChannelSPI extends SelectionChannelSPI {

	/**
	 * Creates a {@link CompatibilitySelectionChannelSPI}.
	 *
	 * @param name
	 *        See {@link ChannelSPI#ChannelSPI(String)}.
	 * @param initialSingleSelection
	 *        See {@link #getInitialSingleSelection()}.
	 * @param initialMultiSelection
	 *        See {@link #getInitialMultiSelection()}.
	 */
	public CompatibilitySelectionChannelSPI(String name, Object initialSingleSelection, Object initialMultiSelection) {
		super(name, initialSingleSelection, initialMultiSelection);
	}

	@Override
	protected ComponentChannel createSingleSelectionChannel(LayoutComponent component) {
		return new SingleSelectionChannel(component, getName(), getInitialSingleSelection());
	}

	@Override
	protected ComponentChannel createMultiSelectionChannel(LayoutComponent component) {
		return new MultiSelectionChannel(component, getName(), getInitialMultiSelection());
	}

	/**
	 * {@link DefaultChannel} that unwraps empty or single-element collections that are given as new
	 * value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SingleSelectionChannel extends DefaultChannel {

		/**
		 * Creates a {@link SingleSelectionChannel}.
		 */
		public SingleSelectionChannel(LayoutComponent component, String name, Object initialValue) {
			super(component, name, initialValue);
		}

		@Override
		protected Object tranformInput(Object newValue) {
			if (newValue instanceof Collection<?>) {
				Collection<?> collectionValue = (Collection<?>) newValue;
				switch (collectionValue.size()) {
					case 0:
						return null;
					case 1:
						return CollectionUtil.getFirst(collectionValue);
					default:
						throw new IllegalArgumentException(
							"Multiple selection " + newValue + " for single selection component: " + getComponent());
				}
			}
			return newValue;
		}

	}

	/**
	 * {@link DefaultChannel} that wraps non-collection values that are given as new value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class MultiSelectionChannel extends DefaultChannel {

		/**
		 * Creates a {@link MultiSelectionChannel}.
		 */
		public MultiSelectionChannel(LayoutComponent component, String name, Object initialValue) {
			super(component, name, initialValue);
		}

		@Override
		protected Object tranformInput(Object newValue) {
			if (newValue instanceof Collection<?>) {
				return newValue;
			}
			return CollectionUtil.singletonOrEmptySet(newValue);
		}

	}

}
