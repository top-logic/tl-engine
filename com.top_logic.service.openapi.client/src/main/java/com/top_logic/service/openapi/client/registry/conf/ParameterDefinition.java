/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import java.util.regex.Pattern;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.service.openapi.client.registry.impl.call.CallBuilderFactory;
import com.top_logic.service.openapi.common.document.Described;

/**
 * Definition of a parameter of a configured TL-Script function.
 * 
 * @see MethodDefinition#getParameters()
 * @see CallBuilderFactory
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	ParameterDefinition.NAME_ATTRIBUTE,
	ParameterDefinition.TYPE,
	ParameterDefinition.REQUIRED,
	ParameterDefinition.MULTIPLE,
	ParameterDefinition.DEFAULT_VALUE,
	ParameterDefinition.DESCRIPTION,
})
@Label("Function parameter")
public interface ParameterDefinition extends NamedConfigMandatory, Described {

	/**
	 * {@link Pattern} of the {@link #getName()} property.
	 */
	String NAME_PATTERN = "[a-zA-Z][a-zA-Z0-9_]*";

	/**
	 * @see #getType()
	 */
	String TYPE = "type";

	/**
	 * @see #isRequired()
	 */
	String REQUIRED = "required";

	/**
	 * @see #getDefaultValue()
	 */
	String DEFAULT_VALUE = "default-value";

	/** @see #isMultiple() */
	String MULTIPLE = "multiple";

	/**
	 * Name of the parameter.
	 * 
	 * <p>
	 * The name of the parameter is used to reference its value e.g. in a service's
	 * {@link MethodDefinition#getBaseUrl()}.
	 * </p>
	 */
	@Override
	@RegexpConstraint(NAME_PATTERN)
	String getName();

	/**
	 * The expected type of the argument value passed to the TL-Script function that invokes the
	 * external API.
	 */
	@Name(TYPE)
	@Options(fun = AllTypes.class, mapping = TLModelPartRef.PartMapping.class)
	TLModelPartRef getType();

	/**
	 * Setter for {@link #getType()}.
	 */
	void setType(TLModelPartRef value);

	/**
	 * Whether a value for this {@link ParameterDefinition} must be given, when invoking the
	 * TL-Script function.
	 */
	@Name(REQUIRED)
	boolean isRequired();

	/**
	 * Setter for {@link #isRequired()}.
	 */
	void setRequired(boolean value);

	/**
	 * Expression to evaluate if no argument is passed for this parameter in an invocation of the
	 * TL-Script function.
	 * 
	 * <p>
	 * Only relevant, if the parameter is not {@link #isRequired()}.
	 * </p>
	 */
	@Name(DEFAULT_VALUE)
	@DynamicMode(fun = HiddenIf.class, args = @Ref(REQUIRED))
	Expr getDefaultValue();

	/**
	 * Setter for {@link #getDefaultValue()}.
	 */
	void setDefaultValue(Expr value);

	/**
	 * Whether this parameter can occur multiple times.
	 */
	@Name(MULTIPLE)
	boolean isMultiple();

	/**
	 * Setter for {@link #isMultiple()}.
	 */
	void setMultiple(boolean value);

	/**
	 * Dynamic field mode that displays a field only if a referenced checkbox is checked.
	 */
	class HiddenIf extends Function1<FieldMode, Boolean> {
		@Override
		public FieldMode apply(Boolean arg) {
			return arg != null && arg.booleanValue() ? FieldMode.INVISIBLE : FieldMode.ACTIVE;
		}
	}

}
