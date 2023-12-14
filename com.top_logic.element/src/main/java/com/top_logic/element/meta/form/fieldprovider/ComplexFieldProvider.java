/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.OptionProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.OptionsDisplay;
import com.top_logic.model.annotate.ui.OptionsPresentation;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of custom data type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComplexFieldProvider extends AbstractSelectFieldProvider
		implements ConfiguredInstance<ComplexFieldProvider.Config> {

	/**
	 * Configuration options for {@link ComplexFieldProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<FieldProvider> {

		/**
		 * The {@link OptionProvider} that produces the options to choose from.
		 */
		@Mandatory
		@NonNullable
		@InstanceFormat
		OptionProvider getOptionProvider();

		/**
		 * @see #getOptionProvider()
		 */
		void setOptionProvider(OptionProvider value);

		/**
		 * Configuration for the select dialog (if it is used) of the result {@link SelectField}.
		 * 
		 * @return May be <code>null</code>. In this case the default select dialog is used.
		 */
		SelectDialogConfig getSelectDialog();

	}

	private final Config _config;

	private final SelectDialogProvider _selectDialog;

	/**
	 * Creates a {@link ComplexFieldProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComplexFieldProvider(InstantiationContext context, Config config) {
		_config = config;
		_selectDialog = context.getInstance(config.getSelectDialog());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		OptionModel<?> options;
		if (editContext.getOptions() != null) {
			options = editContext.getOptions().generate(editContext);
		} else {
			options = getConfig().getOptionProvider().getOptions(editContext);
		}
		options = filterOptions(options, editContext);
		SelectField selectField =
			newSelectField(fieldName, options, editContext.isMultiple(), false, isSearch, isMandatory,
				mandatoryChecker,
				isDisabled, true);
		if (!editContext.isOrdered()) {
			OptionsPresentation optionsPresentation = AttributeOperations.getOptionsPresentation(
				editContext.getAnnotation(OptionsDisplay.class), editContext.getValueType());
			if (optionsPresentation != null) {
				switch (optionsPresentation) {
					case PLAIN:
						// No table select dialog
						break;
					case TABLE:
						selectField.setSelectDialogProvider(SelectDialogProvider.newTableInstance());
						break;

					default:
						throw OptionsPresentation.unknownPresentation(optionsPresentation);
				}
			}
		}
		if (_selectDialog != null) {
			selectField.setSelectDialogProvider(_selectDialog);
		}
		return selectField;
	}

	private OptionModel<?> filterOptions(OptionModel<?> options, EditContext editContext) {
		if (!(options instanceof ListOptionModel)) {
			return options;
		}
		List<?> baseOptions = ((ListOptionModel<?>) options).getBaseModel();
		List<?> filteredOptions =
			AttributeOperations.adjustOptions(editContext, baseOptions);
		if (baseOptions == filteredOptions) {
			return options;
		}
		return new DefaultListOptionModel<>(filteredOptions);
	}

}
