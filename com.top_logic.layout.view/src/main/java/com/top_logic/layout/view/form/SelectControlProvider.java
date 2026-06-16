/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Comparator;

import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link ReactFieldControlProvider} for attributes that are edited by selecting from a set of
 * options.
 *
 * <p>
 * Wraps the {@link SelectFieldModel} (an {@link AttributeSelectFieldModel}) in a
 * {@link ReactDropdownSelectControl}, using {@link MetaLabelProvider} so that options and the
 * current selection are rendered with their model labels in both edit and display mode.
 * </p>
 */
public class SelectControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		SelectFieldModel selectModel = (SelectFieldModel) model;
		LabelProvider labels = MetaLabelProvider.INSTANCE;
		Comparator<?> optionOrder = LabelComparator.newCachingInstance(labels);
		return new ReactDropdownSelectControl(context, selectModel, labels, optionOrder, false);
	}

}
