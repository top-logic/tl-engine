/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Sink;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateContainer.Handle;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;

/**
 * Serves as a listener that listens for changes to a list of model references. Based on the changes
 * in the models, the class computes the {@link FormVisibility} dynamically.
 * 
 * <p>
 * The listener uses the {@link ModeSelector} to determine how a given element should be displayed
 * or hidden, depending on the state or changes of the models being referenced. Each time a model is
 * updated, the listener recalculates the visibility of the form elements based on the selected
 * mode.
 * </p>
 * 
 * <p>
 * The class maintains a list of model references, which are monitored for changes. Upon detecting
 * any updates, the listener triggers a re-evaluation of the visibility conditions.
 * </p>
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class ModeObserver implements ValueListener, Sink<Pointer> {

	private final List<AttributeUpdateContainer.Handle> _handles = new ArrayList<>();

	private AttributeUpdateContainer _updateContainer;

	private ModeSelector _modeSelector;

	private TLObject _object;

	private TLStructuredTypePart _attribute;

	/**
	 * Creates a {@link ModeObserver}.
	 * 
	 * @param updateContainer
	 *        Container to get the values of.
	 * @param modeSelector
	 *        Calculates the actual {@link FormVisibility}.
	 * @param object
	 *        Model to observe.
	 * @param attribute
	 *        The attribute to observe. Maybe <code>null</code>.
	 */
	public ModeObserver(AttributeUpdateContainer updateContainer, ModeSelector modeSelector, TLObject object,
			TLStructuredTypePart attribute) {
		_updateContainer = updateContainer;
		_modeSelector = modeSelector;
		_object = object;
		_attribute = attribute;
	}

	@Override
	public void valueChanged(FormField changedField, Object oldValue, Object newValue) {
		FormVisibility mode = _modeSelector.getMode(_object, _attribute);
		valueChanged(mode);

		removeListeners();
		_modeSelector.traceDependencies(_object, _attribute, this, _updateContainer.getFormContext());
	}

	/**
	 * Handles the changes of the visibility depending on the given {@link FormVisibility}.
	 */
	public abstract void valueChanged(FormVisibility mode);

	private void removeListeners() {
		for (Handle handle : _handles) {
			handle.release();
		}
		_handles.clear();
	}

	@Override
	public void add(Pointer p) {
		_handles.add(_updateContainer.addValueListener(p.object(), p.attribute(), this));
	}
}
