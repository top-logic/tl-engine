/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.util.Resources;

/**
 * Static utilities for creating labels for properties in declarative forms.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Labels {

	/**
	 * A label derived from the given {@link ValueModel} with the given suffix.
	 * 
	 * @param valueModel
	 *        The identifier for the property.
	 * @param keySuffix
	 *        A suffix starting with <code>@</code>.
	 * @return The label derived from the given property.
	 */
	public static String propertyLabel(ValueModel valueModel, String keySuffix) {
		return propertyLabel(valueModel.getProperty(), keySuffix, false);
	}

	/**
	 * A label for the property described by the given {@link ValueModel}.
	 * 
	 * @see #propertyLabel(PropertyDescriptor, boolean)
	 */
	public static String propertyLabel(ValueModel setting) {
		return propertyLabel(setting.getProperty(), false);
	}

	/**
	 * A label for the property described by the given {@link ValueModel}.
	 * 
	 * @see #propertyLabel(PropertyDescriptor, boolean)
	 */
	public static ResKey propertyLabelKey(ValueModel setting) {
		return propertyLabelKey(setting.getProperty());
	}

	/**
	 * Installs the {@link SelectField#setEmptyLabel(String) empty label} for the given field based
	 * on the given property.
	 * 
	 * @param field
	 *        The field to set the empty label.
	 * @param valueModel
	 *        The property whose choices are displayed by the given field.
	 */
	public static void emptyLabel(SelectField field, ValueModel valueModel) {
		emptyLabel(field, valueModel.getProperty());
	}

	private static void emptyLabel(SelectField field, PropertyDescriptor property) {
		String label = propertyLabel(property, "@add", true);
		if (label == null) {
			String propertyName = propertyLabel(property, false);
			label = Resources.getInstance().getString(I18NConstants.EMPTY_LABEL__PROPERTY.fill(propertyName));
		}
		field.setEmptyLabel(label);
	}

	/**
	 * Label for the given property.
	 */
	public static String propertyLabel(PropertyDescriptor property, boolean optional) {
		return resolve(propertyLabelKey(property), optional);
	}

	private static String resolve(ResKey key, boolean optional) {
		if (optional) {
			return Resources.getInstance().getString(key, null);
		} else {
			return Resources.getInstance().getString(key);
		}
	}

	/**
	 * Resource key for the label of the given property.
	 * 
	 * @see #propertyLabelKey(PropertyDescriptor, String)
	 */
	public static ResKey propertyLabelKey(PropertyDescriptor property) {
		return propertyLabelKey(property, null);
	}

	/**
	 * Label-derived resource for the given property.
	 */
	public static String propertyLabel(PropertyDescriptor property, String keySuffix, boolean optional) {
		return resolve(propertyLabelKey(property, keySuffix), optional);
	}

	/**
	 * Alternative label resource for the given property.
	 * 
	 * @param property
	 *        The property to resolve the label key for.
	 * @param keySuffix
	 *        The suffix to the key that allows to customize the label for a certain special
	 *        situation. <code>null</code> to retrieve the standard label.
	 * @return The {@link ResKey} for the label.
	 * 
	 * @see PropertyDescriptor#labelKey(String)
	 */
	public static ResKey propertyLabelKey(PropertyDescriptor property, String keySuffix) {
		return property.labelKey(keySuffix);
	}

}
