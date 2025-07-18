/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.layout.component.model.SelectionEvent;

interface SelectionEventBuilder {

	public static final SelectionEventBuilder NONE = new SelectionEventBuilder() {
		@Override
		public SelectionEvent build() {
			return null;
		}

		@Override
		public void recordUpdate(Object obj) {
			// Ignore.
		}
	};

	public SelectionEvent build();

	public void recordUpdate(Object obj);

	public static SelectionEventBuilder create(SelectionModel<?> model) {
		Set<?> old = model.getSelection();

		return new SelectionEventBuilder() {
			private Set<Object> _updated = new HashSet<>();

			@Override
			public void recordUpdate(Object obj) {
				_updated.add(obj);
			}

			@Override
			public SelectionEvent build() {
				return new SelectionEvent() {
					private Set<?> _current;

					@Override
					public Set<?> getUpdatedObjects() {
						return _updated;
					}

					@Override
					public SelectionModel<?> getSender() {
						return model;
					}

					@Override
					public Set<?> getOldSelection() {
						return old;
					}

					@Override
					public Set<?> getNewSelection() {
						if (_current == null) {
							_current = model.getSelection();
						}
						return _current;
					}
				};
			}
		};
	}
}