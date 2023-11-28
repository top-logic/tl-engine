/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.List;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor.WithTooltipConfiguration;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.constraint.CheckExprWithAdditionalParams;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration of TL-Script that delegates to a configured {@link Expr}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ScriptConfiguration.NAME_ATTRIBUTE,
	ScriptConfiguration.LABEL,
	ScriptConfiguration.DESCRIPTION,
	ScriptConfiguration.PARAMETERS,
	ScriptConfiguration.IMPLEMENTATION,
})
public interface ScriptConfiguration extends NamedConfigMandatory {

	/**
	 * Configuration name of {@link #getImplementation()}.
	 */
	String IMPLEMENTATION = "implementation";

	/**
	 * Configuration name of {@link #getParameters()}.
	 */
	String PARAMETERS = "parameters";

	/**
	 * Configuration name of {@link #getDescription()}.
	 */
	String DESCRIPTION = "description";

	/**
	 * Configuration name of {@link #getLabel()}.
	 */
	String LABEL = "label";

	/**
	 * The internationalized name of the script.
	 */
	@Name(LABEL)
	@Nullable
	@WithTooltipConfiguration(false)
	ResKey getLabel();

	/**
	 * Setter for {@link #getLabel()}
	 */
	void setLabel(ResKey value);

	/**
	 * The internationalized description of the script function.
	 */
	@Name(DESCRIPTION)
	@DisplayMinimized
	HtmlResKey getDescription();

	/**
	 * Setter for {@link #getDescription()}.
	 */
	void setDescription(HtmlResKey value);

	/**
	 * The expression that is executed when the script is called.
	 * 
	 * <p>
	 * The expression has access to all parameters in {@link #getParameters()}. The return value of
	 * the expression is the return value of the called script.
	 * </p>
	 */
	@Mandatory
	@PropertyEditor(PlainEditor.class)
	@Constraint(value = CheckExprWithAdditionalParams.class, args = { @Ref(PARAMETERS) })
	@Name(IMPLEMENTATION)
	Expr getImplementation();

	/** Setter for {@link #getImplementation()}. */
	void setImplementation(Expr value);

	/**
	 * Definition of the parameters that can be access by {@link #getImplementation()}.
	 */
	@Key(ScriptParameter.NAME_ATTRIBUTE)
	@Name(PARAMETERS)
	@CollapseEntries
	List<ScriptParameter> getParameters();

}
