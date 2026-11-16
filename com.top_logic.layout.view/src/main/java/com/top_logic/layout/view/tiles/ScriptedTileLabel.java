/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ObservedTypes;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link TileLabelProvider} that computes the label from current channel values using a TL-Script
 * expression.
 *
 * <p>
 * Resolves all {@link Config#getInputs() inputs} to channels at compute time and evaluates the
 * expression over their current values, which it sees as variables named after the channels or by
 * their {@code as} names. The expression's result is converted to a {@link ResKey} according to its
 * runtime type - intended for labels derived from business object names.
 * </p>
 *
 * <p>
 * Where the label is kept up to date, it follows the objects on its inputs. An expression reading
 * an object beyond them - {@code $project.get(`my.app:Project#customer`).get(`my.app:Customer#name`)}
 * - names that object's type in {@link Config#getObservedTypes() observed-types}.
 * </p>
 *
 * @implNote The result is turned into a {@link ResKey} by
 *           {@link TileLabelProvider#toLabel(Object)}.
 */
public class ScriptedTileLabel implements TileLabelProvider {

	/**
	 * Configuration for {@link ScriptedTileLabel}.
	 */
	@TagName("scripted")
	public interface Config extends TileLabelProvider.Config<ScriptedTileLabel>, Inputs {

		@Override
		@ClassDefault(ScriptedTileLabel.class)
		Class<? extends ScriptedTileLabel> getImplementationClass();

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * TL-Script expression returning the label string.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names.
		 * </p>
		 */
		@Name(EXPR)
		@Mandatory
		@NonNullable
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getExpr();

		/** Configuration name for {@link #getObservedTypes()}. */
		String OBSERVED_TYPES = "observed-types";

		/**
		 * Types whose object changes (create / update / delete) compute the label again, in
		 * addition to the objects on the {@link #getInputs() inputs}, which are always observed.
		 *
		 * <p>
		 * Configure this for a label reading an object it reaches only through the object on an
		 * input - the name of the customer of the displayed project, for instance.
		 * </p>
		 */
		@Name(OBSERVED_TYPES)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getObservedTypes();
	}

	private final List<InputRef> _inputs;

	private final QueryExecutor _executor;

	private final List<TLModelPartRef> _observedTypeRefs;

	/**
	 * Creates a new {@link ScriptedTileLabel}.
	 */
	@CalledByReflection
	public ScriptedTileLabel(InstantiationContext context, Config config) {
		_inputs = config.getInputs();
		_executor = InputScript.compile(config, Config.EXPR);
		_observedTypeRefs = config.getObservedTypes();
	}

	@Override
	public ResKey compute(ViewContext context) {
		Object[] args = ChannelInputs.arguments(ChannelInputs.resolve(context, _inputs));
		return TileLabelProvider.toLabel(_executor.execute(args));
	}

	/**
	 * The {@link Config#getInputs() inputs} the expression reads.
	 */
	@Override
	public List<ViewChannel> observedChannels(ViewContext context) {
		return ChannelInputs.resolve(context, _inputs);
	}

	/**
	 * The {@link Config#getObservedTypes() observed types} of the label.
	 */
	@Override
	public Set<TLStructuredType> observedTypes() {
		return ObservedTypes.resolve(_observedTypeRefs);
	}
}
