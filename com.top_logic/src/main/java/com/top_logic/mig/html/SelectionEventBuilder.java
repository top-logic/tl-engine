/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.layout.component.model.SelectionEvent;

/**
 * Builder for {@link SelectionEvent}s.
 */
public interface SelectionEventBuilder<T> {

	/**
	 * A {@link SelectionEventBuilder} that ignores updates and builds no event.
	 */
	public static final SelectionEventBuilder<Object> NONE = new SelectionEventBuilder<>() {
		@Override
		public SelectionEvent<Object> build() {
			return null;
		}

		@Override
		public void recordUpdate(Object obj) {
			// Ignore.
		}
	};

	/**
	 * Creates the {@link SelectionEvent} from recorded updates.
	 */
	public SelectionEvent<T> build();

	/**
	 * Marks the given object as touched.
	 * 
	 * @see SelectionEvent#getUpdatedObjects()
	 */
	public void recordUpdate(Object obj);

	/**
	 * Creates a {@link SelectionEventBuilder}.
	 */
	public static <T> SelectionEventBuilder<T> create(SelectionModel<T> model) {
		Set<? extends T> old = model.getSelection();

		return new SelectionEventBuilder<>() {
			private Set<Object> _updated = new HashSet<>();

			@Override
			public void recordUpdate(Object obj) {
				_updated.add(obj);
			}

			@Override
			public SelectionEvent<T> build() {
				if (_updated.isEmpty()) {
					return null;
				}

				return new SelectionEvent<>() {
					private Set<? extends T> _current;

					@Override
					public Set<?> getUpdatedObjects() {
						return _updated;
					}

					@Override
					public SelectionModel<T> getSender() {
						return model;
					}

					@Override
					public Set<? extends T> getOldSelection() {
						return old;
					}

					@Override
					public Set<? extends T> getNewSelection() {
						if (_current == null) {
							_current = model.getSelection();
						}
						return _current;
					}
				};
			}
		};
	}

	/**
	 * A {@link SelectionEventBuilder} that creates no events.
	 */
	@SuppressWarnings("unchecked")
	public static <T> SelectionEventBuilder<T> none() {
		return (SelectionEventBuilder<T>) NONE;
	}
}