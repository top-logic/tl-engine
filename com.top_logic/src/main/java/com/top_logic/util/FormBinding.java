/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.StringField;

/**
 * Utilities to build forms based on the persistent model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormBinding {

	/**
	 * Adds a {@link StringLengthConstraint} based on the size of a static type attribute.
	 * 
	 * @param field
	 *        The {@link FormField} to modify.
	 * @param type
	 *        Name of the static {@link MOClass} type.
	 * @param attribute
	 *        Name of the attribute of the given type.
	 */
	public static void addMOSizeConstraint(FormField field, String type, String attribute) {
		addSizeConstraint(field, lookupMOAttribute(type, attribute));
	}

	/**
	 * Adds a {@link StringLengthConstraint} based on the size of a static type attribute.
	 * 
	 * @param field
	 *        The {@link FormField} to modify.
	 * @param type
	 *        The static {@link MOClass} type.
	 * @param attribute
	 *        Name of the attribute of the given type.
	 */
	public static void addSizeConstraint(StringField field, MOClass type, String attribute) {
		addSizeConstraint(field, lookupAttribute(type, attribute));
	}

	/**
	 * Adds a {@link StringLengthConstraint} based on the size of a static type attribute.
	 * 
	 * @param field
	 *        The {@link FormField} to modify.
	 * @param attribute
	 *        The attribute of a static type.
	 */
	public static void addSizeConstraint(FormField field, MOAttribute attribute) {
		field.addConstraint(constraint(attribute));
	}

	/**
	 * Creates a {@link StringLengthConstraint} based on the size of a static type attribute.
	 * 
	 * @param attribute
	 *        The attribute of a static type.
	 */
	public static StringLengthConstraint constraint(MOAttribute attribute) {
		return new StringLengthConstraint(attribute.isMandatory() ? 1 : 0, size(attribute));
	}

	/**
	 * Looks up a static type.
	 * 
	 * @param name
	 *        The name of the static type.
	 */
	public static MOClass lookupMOType(String name) {
		try {
			return (MOClass) PersistencyLayer.getKnowledgeBase().getMORepository().getType(name);
		} catch (UnknownTypeException ex) {
			throw new AssertionError("Missing persistent type '" + name + "'.");
		}
	}

	/**
	 * Looks up a static type attribute.
	 * 
	 * @param type
	 *        The name of the static type.
	 * @param attribute
	 *        The name of the attribute.
	 */
	public static MOAttribute lookupMOAttribute(String type, String attribute) {
		return lookupAttribute(lookupMOType(type), attribute);
	}

	/**
	 * Looks up a static type attribute.
	 * 
	 * @param type
	 *        The static type.
	 * @param attribute
	 *        The name of the attribute.
	 */
	public static MOAttribute lookupAttribute(MOClass type, String attribute) {
		MOAttribute result = type.getAttributeOrNull(attribute);
		if (result == null) {
			throw new AssertionError("Missing attribute '' of persistent type '" + type.getName() + "'.");
		}
		return result;
	}

	/**
	 * Computes the maximum length of a value for the given string attribute.
	 * 
	 * @param attribute
	 *        The attribute to retrieves its size.
	 */
	public static int size(MOAttribute attribute) {
		return attribute.getDbMapping()[0].getSQLSize();
	}

}
