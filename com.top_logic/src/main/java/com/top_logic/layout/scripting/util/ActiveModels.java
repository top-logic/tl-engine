/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.Control;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;

/**
 * Utilities for finding all {@link Control}s and models currently active in the UI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ActiveModels {

	/**
	 * Iteration of all models currently visible in the given scope.
	 */
	public static Iterable<Object> visibleModels(LayoutComponent scope) {
		return visibleModels(scope(scope));
	}

	/**
	 * Iteration of all models currently visible in the given scope.
	 */
	public static Iterable<Object> visibleModels(CommandListenerRegistry scope) {
		return activeModels(scope, true);
	}

	/**
	 * Iteration of all models currently active (maybe not be visible to the user) in the given
	 * scope.
	 */
	public static Iterable<Object> allModels(LayoutComponent scope) {
		return allModels(scope(scope));
	}

	/**
	 * Iteration of all models currently active (maybe not be visible to the user) in the given
	 * scope.
	 */
	public static Iterable<Object> allModels(CommandListenerRegistry scope) {
		return activeModels(scope, false);
	}

	/**
	 * All active models in the UI.
	 * 
	 * @param scope
	 *        The search scope.
	 * @param onlyVisible
	 *        Whether to report only visible items.
	 */
	public static Iterable<Object> activeModels(final CommandListenerRegistry scope, final boolean onlyVisible) {
		return new Iterable<>() {
			@Override
			public Iterator<Object> iterator() {
				return new FilteringIteratorBase<>(iterateControls(scope, onlyVisible)) {
					@Override
					protected Object process(Control item) {
						return item.getModel();
					}
				};
			}
		};
	}

	/**
	 * All {@link Control} in the UI including hidden ones.
	 * 
	 * @param scope
	 *        The search scope.
	 */
	public static Iterable<Control> allControls(LayoutComponent scope) {
		return allControls(scope(scope));
	}

	/**
	 * All {@link Control} in the UI including hidden ones.
	 * 
	 * @param scope
	 *        The search scope.
	 */
	public static Iterable<Control> allControls(CommandListenerRegistry scope) {
		return activeControls(scope, false);
	}

	/**
	 * All visible {@link Control} in the UI.
	 * 
	 * @param scope
	 *        The search scope.
	 */
	public static Iterable<Control> visibleControls(final LayoutComponent scope) {
		return visibleControls(scope(scope));
	}

	/**
	 * All visible {@link Control} in the UI.
	 * 
	 * @param scope
	 *        The search scope.
	 */
	public static Iterable<Control> visibleControls(LayoutComponentScope scope) {
		return activeControls(scope, true);
	}

	/**
	 * Active {@link Control} in the UI.
	 * 
	 * @param scope
	 *        The search scope.
	 * @param onlyVisible
	 *        Whether to report only visible items.
	 */
	public static Iterable<Control> activeControls(final CommandListenerRegistry scope, final boolean onlyVisible) {
		return new Iterable<>() {
			@Override
			public Iterator<Control> iterator() {
				return iterateControls(scope, onlyVisible);
			}
		};
	}

	static Iterator<CommandListener> iterateListeners(final CommandListenerRegistry scope) {
		return scope.getCommandListener().iterator();
	}

	static Iterator<Control> iterateControls(CommandListenerRegistry scope, final boolean onlyVisible) {
		return new FilteringIteratorBase<>(iterateListeners(scope)) {
			@Override
			protected Control process(CommandListener listener) {
				if (listener instanceof Control) {
					Control control = (Control) listener;

					if (control.isViewDisabled()) {
						// In background.
						return null;
					}

					if (onlyVisible && !control.isVisible()) {
						// Not displayed.
						return null;
					}

					return control;
				} else {
					return null;
				}
			}
		};
	}

	private static LayoutComponentScope scope(LayoutComponent scope) {
		return scope.getEnclosingFrameScope();
	}

	static abstract class FilteringIteratorBase<S, T> implements Iterator<T> {

		private final Iterator<? extends S> _source;

		private T _next = null;

		public FilteringIteratorBase(Iterator<? extends S> source) {
			_source = source;
		}

		@Override
		public boolean hasNext() {
			prepareNext();
			return _next != null;
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			T result = _next;
			_next = null;
			return result;
		}

		private void prepareNext() {
			if (_next == null) {
				_next = findNext();
			}
		}

		private T findNext() {
			while (_source.hasNext()) {
				S item = _source.next();
				T result = process(item);
				if (result != null) {
					return result;
				}
			}
			return null;
		}

		protected abstract T process(S item);

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}