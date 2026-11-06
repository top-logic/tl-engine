/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AttributeValueListener;
import com.top_logic.model.annotate.TLValueListeners;

/**
 * Calls the {@link AttributeValueListener}s annotated to an attribute (see
 * {@link TLValueListeners}) when its value changes in an overlay edited in a form.
 *
 * <p>
 * One dispatch serves all overlays of a form: the overlay of the edited object and the overlays
 * of the rows of its composition tables. A listener writing another attribute of an overlay
 * triggers the listeners of that attribute in turn.
 * </p>
 *
 * <p>
 * The listeners run for a value the overlay holds as change: a discarded change (see
 * {@link TLObjectOverlay#revert(TLStructuredTypePart)}) calls no listener, neither do values an
 * overlay {@link TLObjectOverlay#isReceivingTransfer() receives} from an overlay over it - a
 * dialog storing into the buffer of a row - since the listeners already ran where the values were
 * entered. While the dispatch is {@link #suspended(Supplier) suspended}, e.g. while the form stores
 * its changes, no listener runs.
 * </p>
 *
 * <p>
 * Within the dispatch started by one change, the listeners of an attribute of an object run at
 * most once: a listener writing the attribute it listens to, directly or through a cycle of
 * listeners, records the value without calling the listeners of that attribute again.
 * </p>
 */
public class ValueListenerDispatch {

	/**
	 * The {@link AttributeValueListener}s of the attributes changed so far, by attribute.
	 */
	private final Map<TLStructuredTypePart, List<? extends AttributeValueListener>> _listeners = new HashMap<>();

	/**
	 * The attributes whose listeners have run in the current dispatch, by object.
	 */
	private final Map<TLObject, Set<TLStructuredTypePart>> _run = new IdentityHashMap<>();

	/**
	 * The nesting depth of {@link #valueChanged(TLObjectOverlay, TLStructuredTypePart, Object, Object)}
	 * calls running listeners.
	 */
	private int _depth;

	/**
	 * Whether no listener is called, see {@link #suspended(Supplier)}.
	 */
	private boolean _suspended;

	/**
	 * Calls the {@link AttributeValueListener}s of the given attribute for a change of its value in
	 * the given overlay.
	 *
	 * @param overlay
	 *        The overlay whose value changed.
	 * @param part
	 *        The changed attribute.
	 * @param oldValue
	 *        The value before the change.
	 * @param newValue
	 *        The value after the change.
	 */
	public void valueChanged(TLObjectOverlay overlay, TLStructuredTypePart part, Object oldValue, Object newValue) {
		if (_suspended || overlay.isReceivingTransfer() || !overlay.isChanged(part)) {
			return;
		}
		List<? extends AttributeValueListener> listeners =
			_listeners.computeIfAbsent(part, AttributeOperations::getValueListeners);
		if (listeners.isEmpty()) {
			return;
		}
		if (!_run.computeIfAbsent(overlay, x -> new HashSet<>()).add(part)) {
			return;
		}
		_depth++;
		try {
			for (AttributeValueListener listener : listeners) {
				listener.valueChanged(overlay, part, oldValue, newValue);
			}
		} finally {
			if (--_depth == 0) {
				_run.clear();
			}
		}
	}

	/**
	 * Runs the given computation without calling any listener for the changes it makes.
	 *
	 * @param computation
	 *        A computation transferring values rather than changing them, e.g. storing the changes
	 *        of a form.
	 * @return The result of the computation.
	 */
	public <T> T suspended(Supplier<T> computation) {
		boolean before = _suspended;
		_suspended = true;
		try {
			return computation.get();
		} finally {
			_suspended = before;
		}
	}

}
