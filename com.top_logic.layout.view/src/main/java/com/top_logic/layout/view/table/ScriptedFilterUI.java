/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.table.ColumnFilterUI;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.table.FilterState;
import com.top_logic.util.Resources;

/**
 * The {@link ColumnFilterUI} of a {@link ScriptedFilter}: a form over a transient instance of the
 * filter's parameter type, rendered with the same {@link FieldControlService} controls as any other
 * form. The transient instance lives across dialog openings (so the form remembers its values); each
 * {@link #read()} snapshots it (and the configured input channel values) into a
 * {@link ScriptedFilterState}.
 */
public class ScriptedFilterUI implements ColumnFilterUI {

	private final ScriptedFilter _filter;

	private final List<ViewChannel> _inputChannels;

	private final TLObject _instance;

	/**
	 * Creates a {@link ScriptedFilterUI}.
	 *
	 * @param filter
	 *        The filter providing the parameter type.
	 * @param inputChannels
	 *        The resolved extra input channels, snapshotted on {@link #read()}.
	 */
	public ScriptedFilterUI(ScriptedFilter filter, List<ViewChannel> inputChannels) {
		_filter = filter;
		_inputChannels = inputChannels;
		_instance = TransientModelFactory.createTransientObject((TLClass) filter.filterType());
	}

	@Override
	public ReactControl buildForm(ReactContext context) {
		ReactFormBuilder form = new ReactFormBuilder(context);
		FieldControlService controls = FieldControlService.getInstance();
		Resources resources = Resources.getInstance();
		for (TLStructuredTypePart part : _filter.filterType().getAllParts()) {
			if (DisplayAnnotations.isHidden(part)) {
				continue;
			}
			AttributeFieldModel model = new AttributeFieldModel(_instance, part);
			ReactControl input = controls.createFieldControl(context, part, model);
			form.addField(resources.getString(TLModelNamingConvention.resourceKey(part)), input);
		}
		return form.build();
	}

	@Override
	public FilterState read() {
		Object[] inputs = new Object[_inputChannels.size()];
		for (int n = 0; n < _inputChannels.size(); n++) {
			inputs[n] = _inputChannels.get(n).get();
		}
		return new ScriptedFilterState(snapshot(), inputs);
	}

	/**
	 * A transient copy of the current parameter values, so a later edit (or cancel) of the form does
	 * not retroactively change an already-applied filter.
	 */
	private TLObject snapshot() {
		TLObject copy = TransientModelFactory.createTransientObject((TLClass) _filter.filterType());
		for (TLStructuredTypePart part : _filter.filterType().getAllParts()) {
			if (!part.isDerived()) {
				copy.tUpdate(part, _instance.tValue(part));
			}
		}
		return copy;
	}

}
