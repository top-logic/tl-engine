/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.component.model.SingleSelectionListener;

/**
 * The class {@link MappedSingleSelectionModelAdapter} wraps some
 * {@link SingleSelectionModel} and has two mappings which are used before
 * delegating something to the wrapped model, and accessing the wrapped model,
 * respectively.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MappedSingleSelectionModelAdapter extends AbstractSingleSelectionModel {

	private final SingleSelectionModel impl;
	private final Mapping<Object, ?> getterMapping;
	private final Mapping<Object, ?> setterMapping;

	/**
	 * listener to the wrapped model to inform listener of this model.
	 */
	private final SingleSelectionListener forwarder = new SingleSelectionListener() {

		@Override
		public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
			fireSingleSelectionChanged(getterMapping.map(formerlySelectedObject), getterMapping.map(selectedObject));
		}
	};

	/**
	 * @param setterMapping
	 *        each selection changing access to the wrapped model is done using
	 *        this {@link Mapping}.
	 * @param getterMapping
	 *        each selection getting access to the wrapped model is done using
	 *        this {@link Mapping}.
	 */
	public MappedSingleSelectionModelAdapter(SingleSelectionModel impl, Mapping<Object, ?> setterMapping, Mapping<Object, ?> getterMapping) {
		this.impl = impl;
		this.setterMapping = setterMapping;
		this.getterMapping = getterMapping;
	}

	@Override
	public Object getSingleSelection() {
		return getterMapping.map(impl.getSingleSelection());
	}

	@Override
	public boolean isSelectable(Object obj) {
		return impl.isSelectable(setterMapping.map(obj));
	}

	@Override
	public void setSingleSelection(Object obj) {
		impl.setSingleSelection(setterMapping.map(obj));
	}

	@Override
	public boolean addSingleSelectionListener(SingleSelectionListener listener) {
		if (!hasSingleSelectionListeners()) {
			this.impl.addSingleSelectionListener(forwarder);
		}
		return super.addSingleSelectionListener(listener);
	}

	@Override
	public boolean removeSingleSelectionListener(SingleSelectionListener listener) {
		boolean result = super.removeSingleSelectionListener(listener);
		if (result && !hasSingleSelectionListeners()) {
			this.impl.removeSingleSelectionListener(forwarder);
		}
		return result;
	}

}
