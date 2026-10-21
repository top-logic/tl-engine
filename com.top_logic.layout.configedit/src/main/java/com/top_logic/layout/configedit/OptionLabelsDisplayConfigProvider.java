/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;

/**
 * {@link ConfigAnnotationControlProvider} displaying the value of a property that cannot be set
 * with the labels of its {@link OptionLabels} annotation.
 *
 * <p>
 * A property that cannot be set - the configuration interface of an item, for instance - offers no
 * options to choose from. Its value is displayed read-only, labelled the same way as the options of
 * a property that can be set. A property that can be set is left to the control the
 * {@link ConfigControlService} chooses without the annotation; a select labels its options and its
 * selection itself.
 * </p>
 */
public class OptionLabelsDisplayConfigProvider implements ConfigAnnotationControlProvider<OptionLabels> {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model, OptionLabels annotation) {
		PropertyDescriptor property = model.getProperty();
		if (property.canHaveSetter()) {
			return null;
		}
		LabelProvider labels = ConfigPropertyOptions.optionLabels(model.getFormModel(), property);
		if (labels == null) {
			return null;
		}

		ReactTextControl display = new ReactTextControl(context, label(labels, model));
		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				display.setText(label(labels, model));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The value is displayed only, whether the form is editable or not.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// The label does not depend on the validity of the value.
			}
		};
		model.addListener(listener);
		display.addCleanupAction(() -> model.removeListener(listener));
		return display;
	}

	/**
	 * The label of the value the property holds.
	 *
	 * <p>
	 * Taken from the configuration, not from the model: the model may hold the value as the text
	 * its format makes of it, while the labels are made for the value itself.
	 * </p>
	 */
	static String label(LabelProvider labels, ConfigFieldModel model) {
		return labels.getLabel(model.getConfig().value(model.getProperty()));
	}

}
