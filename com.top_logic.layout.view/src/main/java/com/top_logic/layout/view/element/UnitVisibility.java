/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelCondition;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The compiled {@link VisibilityCondition} of a UI unit.
 *
 * <p>
 * Instances are created once per loaded view and shared by the sessions displaying it; the
 * per-session part is the {@link ChannelCondition} {@link #bind(ViewContext) bound} to the channels
 * of a session.
 * </p>
 */
public final class UnitVisibility {

	private final QueryExecutor _expr;

	private final List<InputRef> _inputs;

	private UnitVisibility(VisibilityCondition config) {
		_expr = InputScript.compile(config, VisibilityCondition.EXPR);
		_inputs = config.getInputs();
	}

	/**
	 * Creates the {@link UnitVisibility} of the given configuration.
	 *
	 * @param config
	 *        The visibility condition of a unit, {@code null} for a unit without one.
	 * @return {@code null} for a unit without visibility condition.
	 */
	public static UnitVisibility create(VisibilityCondition config) {
		return config == null ? null : new UnitVisibility(config);
	}

	/**
	 * The condition of this unit for the session the given context belongs to.
	 *
	 * @param context
	 *        The context the input channels are resolved in.
	 * @see ChannelCondition#of(QueryExecutor, List, Object...)
	 */
	public ChannelCondition bind(ViewContext context) {
		return ChannelCondition.of(_expr, ChannelInputs.resolve(context, _inputs));
	}

	/**
	 * The condition of a unit with the given visibility for the session the given context belongs
	 * to.
	 *
	 * @param visibility
	 *        The visibility condition of the unit, {@code null} for a unit without one.
	 * @param context
	 *        The context the input channels are resolved in.
	 * @return {@code null} for a unit without visibility condition.
	 */
	public static ChannelCondition bind(UnitVisibility visibility, ViewContext context) {
		return visibility == null ? null : visibility.bind(context);
	}

}
