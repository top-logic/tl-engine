/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.List;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.DefaultAttributeFormFactory;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Base class for {@link FieldProvider}s creating {@link SelectField}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSelectFieldProvider extends AbstractFieldProvider {

	/**
	 * Creates a new {@link SelectField} with default option labels.
	 * 
	 * @see #newSelectField(String, OptionModel, boolean, boolean, boolean, boolean, Constraint,
	 *      boolean, LabelProvider, boolean)
	 */
	protected SelectField newSelectField(String name, OptionModel<?> options, boolean isMultipleDefault,
			boolean isOrdered,
			boolean isSearch,
			boolean isMandatoryDefault, Constraint mandatoryCheckerDefault, boolean isDisabled, boolean isForList) {
		return newSelectField(name, options, isMultipleDefault, isOrdered, isSearch, isMandatoryDefault,
			mandatoryCheckerDefault,
			isDisabled, null, isForList);
	}

	/**
	 * Creates a new {@link SelectField} with default option labels.
	 * 
	 * @see #newSelectField(String, List, boolean, boolean, boolean, boolean, Constraint, boolean,
	 *      LabelProvider, boolean)
	 */
	protected SelectField newSelectField(String name, List<?> options, boolean isMultipleDefault, boolean isOrdered,
			boolean isSearch,
			boolean isMandatoryDefault, Constraint mandatoryCheckerDefault, boolean isDisabled, boolean isForList) {
		return newSelectField(name, options, isMultipleDefault, isOrdered, isSearch, isMandatoryDefault,
			mandatoryCheckerDefault,
			isDisabled, null, isForList);
	}

	/**
	 * Creates a new {@link SelectField} where the {@link OptionModel} is created from a list of
	 * options.
	 * 
	 * @see #newSelectField(String, OptionModel, boolean, boolean, boolean, boolean, Constraint,
	 *      boolean, LabelProvider, boolean)
	 */
	protected SelectField newSelectField(String name, List<?> options, boolean isMultipleDefault, boolean isOrdered,
			boolean isSearch,
			boolean isMandatoryDefault, Constraint mandatoryCheckerDefault, boolean isDisabled,
			LabelProvider aLabelProvider, boolean isForList) {
		return newSelectField(name, new DefaultListOptionModel(options), isMultipleDefault, isOrdered, isSearch,
			isMandatoryDefault, mandatoryCheckerDefault, isDisabled, aLabelProvider, isForList);
	}

	protected SelectField newSelectField(String name, OptionModel<?> options, boolean isMultipleDefault,
			boolean isOrdered,
			boolean isSearch,
			boolean isMandatoryDefault, Constraint mandatoryCheckerDefault, boolean isDisabled,
			LabelProvider aLabelProvider, boolean isForList) {
		// Use multi selection even for single object fields, if this is
		// a search to allow to search for an object with an attribute
		// value from the set of selected objects.
		// TODO FSC MERGE: check if isMultipleDefault is used correclty
		// OLD Version: boolean isMultiple = isSearch ? (isForList) ? isMultipleDefault &&
		// getSearchMultiForLists() : true : isMultipleDefault;
		boolean isMultiple =
			isSearch ? (isForList) ? DefaultAttributeFormFactory.getSearchMultiForLists() : true : isMultipleDefault;
		boolean isMandatory = isSearch ? false : isMandatoryDefault;
		Constraint mandatoryChecker = isSearch ? null : mandatoryCheckerDefault;
		boolean isWriteProtected = false;

		SelectField selectField =
			FormFactory.newSelectField(name, options, isMultiple, isMandatory, isWriteProtected, mandatoryChecker);

		if (isMultiple && !isSearch) {
			selectField.setCustomOrder(isOrdered);
		}

		selectField.setConfigNameMapping(DefaultAttributeFormFactory.ATTRIBUTED_CONFIG_NAME_MAPPING);

		// Would be fine, but is unfortunately wrong:
		//
		// selectField.setOptionLabelProvider(
		// MetaLabelProviderRegistry.getLabelProvider(metaAttribute.getMetaElement()));
		//
		// The meta element of a meta attribute is the element on which
		// the attribute is defined (not the type of the attribute
		// value it defines). Even worse, a meta attribute seems not to
		// know the the type of value that can be assigned to it???
		//
		if (aLabelProvider == null) {
			aLabelProvider = MetaResourceProvider.INSTANCE;
		}
		selectField.setOptionLabelProvider(aLabelProvider);
		selectField.setImmutable(isDisabled);
		return selectField;
	}

}
