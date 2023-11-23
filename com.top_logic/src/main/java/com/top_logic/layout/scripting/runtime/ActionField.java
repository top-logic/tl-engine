/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.model.AbstractSingleValueField;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;

/**
 * {@link FormField} that parses {@link ApplicationAction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ActionField extends AbstractSingleValueField {

	/**
	 * Creates an {@link ActionField}.
	 * 
	 * @see AbstractSingleValueField#AbstractSingleValueField(String, boolean, boolean, Constraint)
	 */
	public static FormField newActionField(String name, boolean mandatory, boolean immutable, Constraint constraint,
			XMLPrettyPrinter.Config config) {
		return new ActionField(name, mandatory, immutable, constraint, config);
	}

	private final XMLPrettyPrinter.Config _config;

	private ActionField(String name, boolean mandatory, boolean immutable, Constraint constraint,
			XMLPrettyPrinter.Config config) {
		super(name, mandatory, immutable, constraint);
		_config = config;
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFormField(this, arg);
	}

	@Override
	protected Object parseString(String aRawValue) throws CheckException {
		if (aRawValue == null || aRawValue.trim().length() == 0) {
			throw new CheckException("Action must not be empty.");
		}

		ApplicationAction newValue;
		try {
			newValue = ActionReader.INSTANCE.readAction(aRawValue);
		} catch (ConfigurationException ex) {
			throw new CheckException(ex.getMessage());
		}

		return newValue;
	}

	@Override
	protected String unparseString(Object aValue) {
		if (aValue == null) {
			return "";
		}

		return ActionWriter.INSTANCE.writeAction((ApplicationAction) aValue, _config);
	}

	@Override
	protected Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException {
		return value;
	}

}