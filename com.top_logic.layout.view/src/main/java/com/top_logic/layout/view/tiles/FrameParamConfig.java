/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * One parameter of a {@link FrameRouteConfig frame route}: either converted between the value the
 * frame holds and the text the URL carries, or taken from the frames below it on the path.
 *
 * <p>
 * What kind of parameter an entry declares depends on its {@link #getName() name}. A
 * <em>placeholder parameter</em> is named like a placeholder of the
 * {@link FrameRouteConfig#getRoute() route} and is carried in the URL. A <em>path parameter</em>
 * is named like no placeholder: it never reaches the URL, and a frame restored from a URL takes its
 * value from the frame below on the path - the value a push from there hands on.
 * </p>
 *
 * <p>
 * A URL carries text, a frame parameter typically a business object. The two expressions convert
 * between them: {@link #getExpr() expr} maps the value onto the text that names it,
 * {@link #getReverse() reverse} maps the text back onto the value. For a parameter
 * <code>person</code> holding an object identified by its technical identifier this is
 * <code>expr="objectId($person)"</code> and
 * <code>reverse="$value.objectResolve(`tl.accounts:Person`)"</code>; a readable URL uses a business
 * key instead, at the price of the uniqueness the key then has to have.
 * </p>
 *
 * <p>
 * A parameter of the route without an entry of its own is carried as text: the value names itself in
 * the URL and the text of the URL is the value, which is what a parameter holding a string needs.
 * </p>
 *
 * <p>
 * A path parameter serves a frame whose content follows from the frame below it, the milestone list
 * of the project the frame below displays, for instance: the URL names the project once, in the
 * route of the project frame, and the list frame is declared with <code>route="milestones"</code>
 * and <code>&lt;param name="project"/&gt;</code>. A path parameter has no conversions: neither
 * {@link #getExpr() expr} nor {@link #getReverse() reverse} may be given for it.
 * </p>
 */
@TagName(FrameParamConfig.TAG_NAME)
@TitleProperty(name = FrameParamConfig.NAME)
public interface FrameParamConfig extends FrameContentConfig {

	/** Configuration tag of a {@link FrameParamConfig}. */
	String TAG_NAME = "param";

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/** Configuration name for {@link #getReverse()}. */
	String REVERSE = "reverse";

	/**
	 * Name of the variable holding the value of the parameter in the {@link #getExpr() expr} of a
	 * parameter without a {@link #getName() name}.
	 */
	String DEFAULT_PARAM_VARIABLE = "param";

	/** Name of the variable holding the text of the URL in the {@link #getReverse() reverse}. */
	String VALUE = "value";

	/**
	 * The parameter this entry declares, the name of the channel the frame receives the value on.
	 *
	 * <p>
	 * The name of a parameter placeholder of the {@link FrameRouteConfig#getRoute() route} declares
	 * the conversion of a parameter the URL carries. Any other name declares a path parameter: a
	 * frame restored from a URL gets the value of the nearest frame below it on the path that holds
	 * a parameter of this name. Without such a frame below, the path restored from the URL ends
	 * before this frame.
	 * </p>
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * Expression mapping the value of the parameter onto the text the URL carries.
	 *
	 * <p>
	 * The expression sees the value the frame holds as the variable named like the parameter, the
	 * value of the parameter <code>person</code> as <code>$person</code>, and returns the text
	 * naming it: <code>objectId($person)</code>. An expression yielding nothing means the value has
	 * no name in a URL - a transient object, for instance - and the frame then contributes no
	 * segment: an address that cannot be opened again is better left unwritten.
	 * </p>
	 *
	 * <p>
	 * Only for a parameter named like a placeholder of the route.
	 * </p>
	 */
	@Name(EXPR)
	@Nullable
	@Supplies(value = DEFAULT_PARAM_VARIABLE, renamedBy = NAME)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getExpr();

	/**
	 * Expression mapping the text the URL carries back onto the value of the parameter.
	 *
	 * <p>
	 * The expression sees the text of the URL as the variable {@value #VALUE} and returns the value
	 * the frame is mounted with: <code>$value.objectResolve(`tl.accounts:Person`)</code>. An
	 * expression yielding nothing means the URL names something the display cannot show - an
	 * object that is deleted, an identifier that was mistyped - and the frame is then not restored
	 * at all.
	 * </p>
	 *
	 * <p>
	 * Only for a parameter named like a placeholder of the route.
	 * </p>
	 */
	@Name(REVERSE)
	@Nullable
	@Supplies(VALUE)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getReverse();
}
