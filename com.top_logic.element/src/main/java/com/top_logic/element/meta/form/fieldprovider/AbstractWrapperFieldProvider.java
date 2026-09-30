/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.util.FormUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.selection.SelectDialogProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.DefaultTableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.ui.OptionsDisplay;
import com.top_logic.model.annotate.ui.OptionsPresentation;

/**
 * Base class for {@link FieldProvider}s displaying persistent objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractWrapperFieldProvider extends AbstractSelectFieldProvider {

	/**
	 * Common configuration of {@link AbstractWrapperFieldProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractSelectFieldProvider> {

		/**
		 * Configuration name of property, that decides, whether the popup select dialog of the
		 * created {@link SelectField} shall display a selectio table, or not
		 */
		public static final String SHOW_TABLE_DIALOG = "show-table-dialog";

		/**
		 * @see #SHOW_TABLE_DIALOG
		 */
		@BooleanDefault(false)
		@Name(SHOW_TABLE_DIALOG)
		boolean shouldShowTableDialog();
	}

	protected SelectField newSelectWrapperSetField(
			EditContext editContext,
			String name,
			boolean isMultiple,
			boolean isOrdered,
			boolean isMandatoryDefault,
			boolean isSearch,
			Constraint mandatoryCheckerDefault,
			boolean isDisabled,
			boolean isForList) {
		// Type-cast outside the lazy list (see below) to prevent
		// deferring failures.
		final LabelProvider theProv = MetaResourceProvider.INSTANCE;
		final SelectField selectField = newSelectField(
			name,
			Collections.EMPTY_LIST,
			isMultiple,
			isOrdered,
			isSearch,
			isMandatoryDefault,
			mandatoryCheckerDefault,
			isDisabled,
			theProv,
			isForList);
		OptionModel<?> options =
			DefaultAttributeFormFactory.getOptionList(editContext, theProv, selectField);
		selectField.setOptionModel(options);
		selectField.setTableConfigurationProvider(getTableConfigurationProvider(editContext));
		initSelectDialogProvider(selectField, editContext);
		return selectField;
	}

	/**
	 * Initializes the {@link SelectField#getSelectDialogProvider() select dialog} based on the
	 * given attribute.
	 */
	protected void initSelectDialogProvider(SelectField selectField, EditContext editContext) {
		if (!editContext.isOrdered()) {
			OptionsPresentation optionsPresentation =
				AttributeOperations.getOptionsPresentation(
					editContext.getAnnotation(OptionsDisplay.class),
					editContext.getValueType());
			if (optionsPresentation == null) {
				if (getConfig().shouldShowTableDialog()) {
					selectField.setSelectDialogProvider(SelectDialogProvider.newTableInstance());
				}
			} else {
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
	}

	private Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * {@link TableConfigurationProvider} for a table to display potential options for the given
	 * {@link EditContext} in a table select dialog.
	 * 
	 * @param editContext
	 *        Description of the values that are potentially displayed in the table.
	 * @return {@link TableConfigurationProvider} for a table displaying values compatible with the
	 *         given context.
	 */
	public static TableConfigurationProvider getTableConfigurationProvider(final EditContext editContext) {
		TLType baseType = editContext.getValueType();
		if (baseType instanceof TLClass) {
				Set<TLClass> contentTypes = Collections.singleton((TLClass) baseType);
				List<TableConfigurationProvider> tableConfigProviders = new ArrayList<>();
				tableConfigProviders.add(new GenericTableConfigurationProvider(contentTypes) {
					@Override
					public void adaptConfigurationTo(TableConfiguration table) {
						super.adaptConfigurationTo(table);

						if (table.getTitleKey() == null) {
							table.setTitleKey(editContext.getTableTitleKey());
						}
					}
				});
				FormUtil.addProvidersFromAnnotations(tableConfigProviders, editContext);
				return TableConfigurationFactory.combine(tableConfigProviders);
		}

		// Fall back, when no MetaAttributeDescription or TLClass found.
		return DefaultTableConfigurationProvider.INSTANCE;
	}

}
