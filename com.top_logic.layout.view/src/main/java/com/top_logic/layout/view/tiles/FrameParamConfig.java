/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Conversion of one parameter of a {@link FrameRouteConfig frame route} between the value the frame
 * holds and the text the URL carries.
 *
 * <p>
 * A URL carries text, a frame parameter typically a business object. The two expressions convert
 * between them: {@link #getExpr() expr} maps the value onto the text that names it,
 * {@link #getReverse() reverse} maps the text back onto the value. For an object identified by its
 * technical identifier this is {@code expr="p -> objectId($p)"} and
 * {@code reverse="id -> objectResolve(`tl.accounts:Person`, $id)"}; a readable URL uses a business
 * key instead, at the price of the uniqueness the key then has to have.
 * </p>
 *
 * <p>
 * A parameter of the route without an entry of its own is carried as text: the value names itself in
 * the URL and the text of the URL is the value, which is what a parameter holding a string needs.
 * </p>
 */
@TagName(FrameParamConfig.TAG_NAME)
@TitleProperty(name = FrameParamConfig.NAME)
public interface FrameParamConfig extends ConfigurationItem {

	/** Configuration tag of a {@link FrameParamConfig}. */
	String TAG_NAME = "param";

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/** Configuration name for {@link #getReverse()}. */
	String REVERSE = "reverse";

	/**
	 * The parameter this conversion applies to.
	 *
	 * <p>
	 * The name of a parameter placeholder of the {@link FrameRouteConfig#getRoute() route}, which is
	 * at the same time the name of the channel the frame receives the value on.
	 * </p>
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * Expression mapping the value of the parameter onto the text the URL carries.
	 *
	 * <p>
	 * Receives the value the frame holds and returns the text naming it. An expression yielding
	 * nothing means the value has no name in a URL - a transient object, for instance - and the
	 * frame then contributes no segment: an address that cannot be opened again is better left
	 * unwritten.
	 * </p>
	 */
	@Name(EXPR)
	@Nullable
	Expr getExpr();

	/**
	 * Expression mapping the text the URL carries back onto the value of the parameter.
	 *
	 * <p>
	 * Receives the text of the URL and returns the value the frame is mounted with. An expression
	 * yielding nothing means the URL names something the display cannot show - an object that is
	 * deleted, an identifier that was mistyped - and the frame is then not restored at all.
	 * </p>
	 */
	@Name(REVERSE)
	@Nullable
	Expr getReverse();
}
