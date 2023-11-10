/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;

/**
 * {@link DisplayProvider} for {@link String} set attributes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringTagProvider extends AbstractConfiguredInstance<StringTagProvider.Config> implements DisplayProvider {

	/**
	 * Typed configuration interface definition for {@link StringTagProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<StringTagProvider> {

		/**
		 * Default numbers of columns.
		 * 
		 * @see TextInputTag#NO_COLUMNS
		 */
		@IntDefault(TextInputTag.NO_COLUMNS)
		int getColumns();

		/**
		 * Setter for {@link #getColumns()}.
		 */
		void setColumns(int columns);

	}

	/**
	 * Create a {@link StringTagProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StringTagProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		// Note: With an options annotation, a select field is created instead of a string field.
		if (member instanceof SelectField) {
			// A primitive type with an options annotation is somewhat like an enumeration.
			return EnumerationTagProvider.INSTANCE.createDisplay(editContext, member);
		}

		return textInput(editContext, member);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		// Note: With an options annotation, a select field is created instead of a string field.
		if (member instanceof SelectField) {
			// A primitive type with an options annotation is somewhat like an enumeration.
			return EnumerationTagProvider.INSTANCE.createDisplayFragment(editContext, member);
		}

		return textInput(editContext, member);
	}

	private Control textInput(EditContext editContext, FormMember member) {
		TextInputControl result = new TextInputControl((FormField) member);
		if (AttributeOperations.isMultiline(editContext) && !editContext.isSearchUpdate()) {
			result.setMultiLine(true);

			MultiLine annotation = editContext.getAnnotation(MultiLine.class);
			int rows;
			if (annotation != null) {
				rows = annotation.getRows();
			} else {
				rows = MultiLineText.DEFAULT_ROWS;
			}

			result.setRows(rows);
		}
		if (!editContext.isMultiple()) {
			result.setColumns(DisplayAnnotations.inputSize(editContext, getConfig().getColumns()));
		}
		return result;
	}

}
