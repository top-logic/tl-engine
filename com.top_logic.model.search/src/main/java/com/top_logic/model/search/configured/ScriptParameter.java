/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.base.config.i18n.InternationalizedDescription;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.HideActiveIf;
import com.top_logic.element.layout.meta.HideActiveIfNot;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Definition of a parameter in a {@link ScriptConfiguration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ScriptParameter.NAME_ATTRIBUTE,
	ScriptParameter.DESCRIPTION,
	ScriptParameter.TYPE,
	ScriptParameter.MANDATORY,
	ScriptParameter.MULTIPLE,
	ScriptParameter.DEFAULT_VALUE,
	ScriptParameter.ORDERED,
	ScriptParameter.BAG,
})
public interface ScriptParameter extends NamedConfigMandatory, InternationalizedDescription {

	/**
	 * Configuration name for {@link #getType()}.
	 */
	String TYPE = "type";

	/**
	 * Configuration name for {@link #isMandatory()}.
	 */
	String MANDATORY = "mandatory";

	/**
	 * Configuration name for {@link #getDefaultValue()}.
	 */
	String DEFAULT_VALUE = "default-value";

	/**
	 * Configuration name for {@link #isOrdered()}.
	 */
	String ORDERED = "ordered";

	/**
	 * Configuration name for {@link #isBag()}.
	 */
	String BAG = "bag";

	/**
	 * Configuration name for {@link #isMultiple()}.
	 */
	String MULTIPLE = "multiple";

	/**
	 * The name of the parameter. The name must be unique within all parameters for the script.
	 */
	@Override
	String getName();

	/**
	 * The description of this parameter.
	 */
	@Override
	ResKey getDescription();

	/**
	 * The type of the parameter.
	 * 
	 * <p>
	 * The value that is delivered to the script must be of this type.
	 * </p>
	 */
	@Name(TYPE)
	@Options(fun = AllTypes.class, mapping = TLModelPartRef.PartMapping.class)
	TLModelPartRef getType();

	/**
	 * Setter for {@link #getType()}.
	 */
	void setType(TLModelPartRef value);

	/**
	 * Whether this parameter is mandatory.
	 * 
	 * <p>
	 * If the parameter is not mandatory and not given by the user, the {@link #getDefaultValue()
	 * default value} is used.
	 * </p>
	 */
	@Name(MANDATORY)
	boolean isMandatory();

	/**
	 * Setter for {@link #isMandatory()}.
	 */
	void setMandatory(boolean value);

	/**
	 * The value for this parameter when it is not {@link #isMandatory()} and no value is given by
	 * the user.
	 */
	@Name(DEFAULT_VALUE)
	@DynamicMode(fun = HideActiveIf.class, args = @Ref(MANDATORY))
	Expr getDefaultValue();

	/**
	 * Setter for {@link #getDefaultValue()}.
	 */
	void setDefaultValue(Expr value);

	/**
	 * Whether this parameter is a single parameter or a collection parameter.
	 */
	@Name(MULTIPLE)
	boolean isMultiple();

	/**
	 * Setter for {@link #isMultiple()}.
	 */
	void setMultiple(boolean value);

	/**
	 * If {@link #isMultiple()} is set, whether the same value may appear more than once in the
	 * values of this parameter.
	 */
	@Name(BAG)
	@DynamicMode(fun = HideActiveIfNot.class, args = @Ref(MULTIPLE))
	@Label("Multiple occurrence")
	boolean isBag();

	/**
	 * Setter for {@link #isBag()}.
	 */
	void setBag(boolean value);

	/**
	 * If {@link #isMultiple()}, whether the order of values of this attribute is significant (list
	 * or ordered set valued).
	 */
	@Name(ORDERED)
	@DynamicMode(fun = HideActiveIfNot.class, args = @Ref(MULTIPLE))
	boolean isOrdered();

	/**
	 * Setter for {@link #isOrdered()}.
	 */
	void setOrdered(boolean value);

}
