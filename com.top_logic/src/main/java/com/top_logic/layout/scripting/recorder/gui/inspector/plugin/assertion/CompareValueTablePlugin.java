/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.Collections;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.decorator.ChangeInfo;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.assertion.ValueAssertion;
import com.top_logic.layout.scripting.recorder.gui.TableCell;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorUtil;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.CellChangeInfo;
import com.top_logic.layout.scripting.recorder.ref.value.CellCompareValue;

/**
 * {@link CompareValueAssertionPlugin} for {@link TableCell}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareValueTablePlugin extends CompareValueAssertionPlugin<TableCell> {

	/**
	 * Creates a new {@link CompareValueTablePlugin}.
	 * 
	 * @param cell
	 *        {@link TableCell} to retrieve initial compare values from.
	 */
	public CompareValueTablePlugin(TableCell cell) {
		super(cell, (CompareInfo) cell.getValue());
	}

	@Override
	protected FormField createCompareValueField(String fieldCompareName) {
		Object oldValue = getCompareValue();
		FormField newFieldForValueType;
		if (oldValue instanceof Wrapper) {
			newFieldForValueType = FormFactory.newSelectField(fieldCompareName, Collections.emptyList());
			oldValue = Collections.singletonList(oldValue);
		} else {
			newFieldForValueType = GuiInspectorUtil.newFieldForValueType(oldValue, fieldCompareName);
		}
		newFieldForValueType.initializeField(oldValue);
		return newFieldForValueType;
	}

	private Object getCompareValue() {
		if (_compareInfo.getChangeInfo() == ChangeInfo.REMOVED) {
			return _compareInfo.getChangeValue();
		} else {
			return _compareInfo.getBaseValue();
		}
	}

	@Override
	protected ValueAssertion assertSameChangeInfo() {
		ModelName actualValue = ReferenceInstantiator.tableValue(CellChangeInfo.class, getModel());
		return ActionFactory.valueAssertion(ModelResolver.buildModelName(expectedChangeInfo()), actualValue,
			getComment());
	}

	@Override
	protected ValueAssertion assertSameCompareObject() {
		ModelName actualValue = ReferenceInstantiator.tableValue(CellCompareValue.class, getModel());
		Object expectedCompareValue;
		if (_compareField instanceof SelectField) {
			expectedCompareValue = ((SelectField) _compareField).getSingleSelection();
		} else {
			expectedCompareValue = _compareField.getValue();
		}
		return ActionFactory.valueAssertion(ModelResolver.buildModelName(expectedCompareValue), actualValue,
			getComment());
	}

}

