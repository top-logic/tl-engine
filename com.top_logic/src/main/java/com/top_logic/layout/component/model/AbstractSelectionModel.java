/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Set;

import com.top_logic.basic.util.AbstractObservable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Base implementation for {@link SelectionModel}s that provides a listener
 * handling.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSelectionModel extends AbstractObservable<SelectionListener, MultiSelectionEvent> implements SelectionModel {

	private SelectionModelOwner _owner;

	public AbstractSelectionModel(SelectionModelOwner owner) {
		_owner = owner;
	}

	/**
	 * Initialises {@link #getOwner()}
	 * 
	 * @param owner
	 *        Value of {@link #getOwner()}.
	 * 
	 * @throws IllegalStateException
	 *         if this {@link AbstractSelectionModel} already has a valid owner.
	 */
	public void initOwner(SelectionModelOwner owner) {
		if (_owner == owner) {
			return;
		}
		if (_owner != SelectionModelOwner.NO_OWNER && _owner != null) {
			throw new IllegalStateException("Owner already set to " + _owner);
		}
		_owner = owner;
	}

	/**
	 * Notifies registered listeners about an atomic selection change.
	 * 
	 * @param formerlySelectedObjects
	 *            the set of objects which was selected before.
	 */
	protected final void fireSelectionChanged(Set formerlySelectedObjects) {
		if (!hasListeners()) {
			return;
		}
		Set currentSelection = getSelection();

		notifyListeners(new MultiSelectionEvent(this, formerlySelectedObjects, currentSelection));
	}

	@Override
	protected void sendEvent(SelectionListener listener, MultiSelectionEvent event) {
		{
			// Decompose event to keep original code with annotate information referencing #3910.
			SelectionListener currentListener = listener;
			Set formerlySelectedObjects = event.getFormerlySelectedObjects();
			Set currentSelection = event.getNewlySelectedObjects();

			// Quirks introduced in #3910 to fix #3936:

			// must check whether the current listener is still attached, as a
			// previous listener can force it to remove as listener. In that
			// case no notification must be happen.
			if (hasListener(SelectionListener.class, currentListener)) {
				currentListener.notifySelectionChanged(this, formerlySelectedObjects, currentSelection);
			}

		}
	}

	/**
	 * Keep original signature to not discard the annotate information to the quirks introduced in
	 * #3910
	 */
	private boolean hasListener(@SuppressWarnings("unused") Class<SelectionListener> listenerClass,
			SelectionListener listener) {
		return hasListener(listener);
	}

	@Override
	public boolean addSelectionListener(SelectionListener listener) {
		return addListener(listener);
	}
	
	@Override
	public boolean removeSelectionListener(SelectionListener listener) {
		return removeListener(listener);
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public SelectionModelOwner getOwner() {
		return _owner;
	}

}
