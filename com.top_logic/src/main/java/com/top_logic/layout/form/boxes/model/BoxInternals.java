/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import static com.top_logic.layout.form.boxes.model.Box.*;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Common management methods of {@link Box} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class BoxInternals extends PropertyObservableBase {

	private AbstractBox _parent;

	private boolean _locallyAttached;

	private boolean _parentAttached;

	public final AbstractBox getParent() {
		return _parent;
	}

	/**
	 * This {@link Box} is added or removed from a hierarchy.
	 * 
	 * @param parent
	 *        The new parent {@link Box}, or <code>null</code> if this is called in response to a
	 *        remove operation.
	 */
	public final void setParent(AbstractBox parent) {
		_parent = parent;
		parentChanged();
	}

	public void notifyLayoutChange(Box sender) {
		Bubble bubble = notifyListeners(LAYOUT_CHANGE, sender, null, null);
		if (bubble == Bubble.BUBBLE) {
			if (_parent != null) {
				_parent.internals().notifyLayoutChange(sender);
			}
		}
	}

	@Override
	protected void firstListenerAdded(EventType<?, ?, ?> type) {
		super.firstListenerAdded(type);

		if (type == LAYOUT_CHANGE) {
			if (!hasListeners(PropertyObservableBase.GLOBAL_LISTENER_TYPE)) {
				attachLocal();
			}
		} else if (type == PropertyObservableBase.GLOBAL_LISTENER_TYPE) {
			if (!hasListeners(LAYOUT_CHANGE)) {
				attachLocal();
			}
		}
	}

	@Override
	protected void lastListenerRemoved(EventType<?, ?, ?> type) {
		super.lastListenerRemoved(type);

		if (type == LAYOUT_CHANGE) {
			if (!hasListeners(PropertyObservableBase.GLOBAL_LISTENER_TYPE)) {
				detachLocal();
			}
		} else if (type == PropertyObservableBase.GLOBAL_LISTENER_TYPE) {
			if (!hasListeners(LAYOUT_CHANGE)) {
				detachLocal();
			}
		}
	}

	/**
	 * Whether this {@link Box} is currently observed for {@link Box#LAYOUT_CHANGE} events.
	 */
	public final boolean isAttached() {
		return _parentAttached || _locallyAttached;
	}

	private void attachLocal() {
		_locallyAttached = true;
		if (_parentAttached) {
			return;
		}
		attach();
	}

	private void detachLocal() {
		_locallyAttached = false;
		if (_parentAttached) {
			return;
		}
		detach();
	}

	private void attach() {
		onAttach();
		for (Box child : self().getChildren()) {
			child.internals().parentAttached();
		}
	}

	private void detach() {
		onDetach();
		for (Box child : self().getChildren()) {
			child.internals().parentDetached();
		}
	}

	/**
	 * Hook called when the first {@link Box#LAYOUT_CHANGE} listener is added.
	 */
	protected abstract void onAttach();

	/**
	 * Hook called when the last {@link Box#LAYOUT_CHANGE} listener is removed.
	 */
	protected abstract void onDetach();

	private void parentChanged() {
		boolean parentAttached = _parent != null && _parent.internals().isAttached();

		if (parentAttached) {
			if (!_parentAttached) {
				parentAttached();
			}
		} else {
			if (_parentAttached) {
				parentDetached();
			}
		}
	}

	/**
	 * The parent {@link Box} is now being observed.
	 */
	private void parentAttached() {
		_parentAttached = true;
		if (_locallyAttached) {
			return;
		}
		attach();
	}

	/**
	 * The parent {@link Box} is no longer being observed.
	 */
	private void parentDetached() {
		_parentAttached = false;
		if (_locallyAttached) {
			return;
		}
		detach();
	}

	protected abstract Box self();

}
