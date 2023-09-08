/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.MacroControlProvider;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.search.expr.config.MacroFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link FormElement} using an expression to produce HTML.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	MacroPart.EXPR,
	MacroPart.WHOLE_LINE,
})
@TagName("macro")
public interface MacroPart extends FormElement<MacroTemplateProvider> {

	/** Configuration option for {@link #getWholeLine()}. */
	String WHOLE_LINE = "wholeLine";

	/**
	 * Name of the configuration property {@link #getExpr()}.
	 */
	String EXPR = "expr";

	/**
	 * The {@link Expr} computing the HTML.
	 * 
	 * <p>
	 * The expression is expected to be a function that takes the context object as single argument.
	 * </p>
	 * 
	 * <p>
	 * The name of the sole variable in {@link #getExpr()} is {@link MacroFormat#MODEL}.
	 * </p>
	 */
	@Mandatory
	@Name(EXPR)
	@ControlProvider(MacroControlProvider.class)
	@Format(MacroFormat.class)
	Expr getExpr();

	/**
	 * Whether the content must be rendered using the whole line.
	 */
	@Name(WHOLE_LINE)
	boolean getWholeLine();
}

