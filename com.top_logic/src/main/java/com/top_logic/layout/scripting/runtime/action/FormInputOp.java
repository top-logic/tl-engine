/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static com.top_logic.basic.StringServices.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.action.FormInput;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * {@link FieldActionOp} setting a value to a {@link FormField}.
 * 
 * @deprecated Use {@link FormInput}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public class FormInputOp extends FieldActionOp {

	public FormInputOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processField(FormField field, Object value) {
		Object adaptedValue = checkAndAdaptValue(field, value);
		try {
			FormFieldInternals.setValue((AbstractFormField) field, adaptedValue);
		} catch (VetoException ex) {
			ApplicationAssertions.fail(getConfig(), "Unexpected veto received.", ex);
		}
	}

	private Object checkAndAdaptValue(FormField field, Object value) {
		if (field instanceof SelectField) {
			SelectField selectField = (SelectField) field;
			Collection<?> valueCollection;
			if (value instanceof Collection<?>) {
				valueCollection = (Collection<?>) value;
			} else if (value == null) {
				valueCollection = Collections.emptyList();
			} else {
				valueCollection = Collections.singletonList(value);
			}
			if (selectField.isOptionsList()) {
				isInOptionList(selectField, valueCollection);
			} else if (selectField.isOptionsTree()) {
				isInOptionTree(selectField, valueCollection);
			}

			return valueCollection;
		} else {
			return value;
		}
	}

	private void isInOptionList(SelectField selectField, Collection<?> valueCollection) {
		List<?> optionList = selectField.getOptions();
		for (Object value : valueCollection) {
			if (!optionList.contains(value)) {
				// Note: An inconsistent selection is not required to be fixed when only changing
				// other values, see Ticket #17315.
				if (!selectField.getSelection().contains(value)) {
					String errorMessage =
						"Tried to select something that is neither one of the options nor currently selected. Description of that object: "
							+ debug(valueCollection) + "; Options: " + debug(optionList);
					throw new RuntimeException(errorMessage);
				}
			}
		}
	}

	private void isInOptionTree(SelectField selectField, Collection<?> valueCollection) {
		TLTreeModel optionTree = selectField.getOptionsAsTree().getBaseModel();
		for (Object valueNode : valueCollection) {
			if (!optionTree.containsNode(valueNode)) {
				String errorMessage =
					"Tried to select something that is not one of the options. Description of that object: "
						+ debug(valueCollection);
				throw new RuntimeException(errorMessage);
			}
		}
	}

}
