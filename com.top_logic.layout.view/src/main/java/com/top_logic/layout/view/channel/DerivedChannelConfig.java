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
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Configuration for a derived (computed, read-only) channel.
 *
 * <p>
 * A derived channel computes its value from other channels using a TL-Script expression. The
 * {@link #getInputs() inputs} are channel references whose current values the
 * {@link #getExpr() expression} sees as variables, see {@link InputScript}.
 * </p>
 */
@TagName("derived-channel")
public interface DerivedChannelConfig extends ChannelConfig, Inputs {

	@Override
	@ClassDefault(DerivedChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();

	/** Configuration name for {@link #getExpr()}. */
	String EXPR = "expr";

	/** Configuration name for {@link #getReverse()}. */
	String REVERSE = "reverse";

	/** Configuration name for {@link #getObservedTypes()}. */
	String OBSERVED_TYPES = "observed-types";

	/**
	 * Name of the variable holding the written value in the {@link #getReverse() reverse
	 * expression}.
	 */
	String VALUE = "value";

	/**
	 * TL-Script expression computing the derived value.
	 *
	 * <p>
	 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
	 * their channels, without declaring them. For example, with {@code inputs="a, b"} and
	 * {@code expr="$a + $b"}, the expression computes the sum of the current values of the
	 * channels {@code a} and {@code b}. An input renamed with {@code as} is seen under its new name:
	 * {@code inputs="selection as ticket"}, {@code expr="$ticket.get(`my.module:Ticket#name`)"}.
	 * </p>
	 */
	@Name(EXPR)
	@Mandatory
	@NonNullable
	@ScriptContextVariables(InputScript.Variables.class)
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
	 * The expression sees the value written to the derived channel as the variable
	 * {@value #VALUE}, behind the values of the {@link #getInputs() inputs}, and must return the
	 * corresponding value for the first input channel. For example, with {@code inputs="id"},
	 * {@code expr="'id-' + $id"} and {@code reverse="$value.substring(3)"}, writing {@code "id-42"}
	 * to the derived channel sets {@code "42"} on the channel {@code id}.
	 * </p>
	 */
	@Name(REVERSE)
	@Supplies(VALUE)
	@ScriptContextVariables(InputScript.Variables.class)
	Expr getReverse();

	/**
	 * Types whose object changes (create / update / delete) recompute the {@link #getExpr()
	 * expression}, in addition to the objects the {@link #getInputs() inputs} hold, which are
	 * always observed.
	 *
	 * <p>
	 * Configure this only for an expression that navigates beyond the input objects, e.g. one
	 * reading an attribute of the input's container: a change of that other object is invisible to
	 * the inputs' own observation. Empty (default) observes just the input objects.
	 * </p>
	 */
	@Name(OBSERVED_TYPES)
	@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
	List<TLModelPartRef> getObservedTypes();
}
