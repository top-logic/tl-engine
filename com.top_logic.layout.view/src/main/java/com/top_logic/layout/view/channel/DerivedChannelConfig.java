/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Configuration for a derived (computed, read-only) channel.
 *
 * <p>
 * A derived channel computes its value from other channels using a TL-Script expression. The
 * {@link #getInputs() inputs} are channel references whose current values become positional
 * arguments to the {@link #getExpr() expression}.
 * </p>
 */
@TagName("derived-channel")
public interface DerivedChannelConfig extends ChannelConfig {

	@Override
	@ClassDefault(DerivedChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();

	/** Configuration name for {@link #getInputs()}. */
	String INPUTS = "inputs";

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/** Configuration name for {@link #getReverse()}. */
	String REVERSE = "reverse";

	/**
	 * Comma-separated references to channels whose current values become positional arguments to the
	 * expression.
	 */
	@Name(INPUTS)
	@Format(CommaSeparatedChannelRefs.class)
	List<ChannelRef> getInputs();

	/**
	 * TL-Script expression computing the derived value.
	 *
	 * <p>
	 * The expression receives the input channel values as positional arguments. For example, with
	 * {@code inputs="a,b"} and {@code expr="x -> y -> $x + $y"}, the expression is called with the
	 * current values of channels {@code a} and {@code b}.
	 * </p>
	 */
	@Name(EXPR)
	@Mandatory
	@NonNullable
	Expr getExpr();

	/**
	 * Optional TL-Script expression for reverse (write-back) propagation.
	 *
	 * <p>
	 * When present, the derived channel becomes bidirectional: setting a value on it applies this
	 * expression to compute the source value, which is then written to the first input channel.
	 * </p>
	 *
	 * <p>
	 * The expression receives the derived value as its single argument and must return the
	 * corresponding value for the first input channel. For example, with
	 * {@code expr="id -> 'id-' + $id"} and {@code reverse="s -> $s.substring(3)"}, writing
	 * {@code "id-42"} to the derived channel sets {@code "42"} on the source.
	 * </p>
	 */
	@Name(REVERSE)
	Expr getReverse();
}
