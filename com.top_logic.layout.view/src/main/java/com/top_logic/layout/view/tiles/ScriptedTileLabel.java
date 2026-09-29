/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.CommaSeparatedChannelRefs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link TileLabelProvider} that computes the label from current channel values using a TL-Script
 * expression.
 *
 * <p>
 * Resolves all {@link Config#getInputs() inputs} to channels at compute time, reads their current
 * values and passes them as positional arguments to the compiled expression. The expression's
 * result is converted to a {@link ResKey} according to its runtime type - intended for labels
 * derived from business object names.
 * </p>
 *
 * @implNote A {@link ResKey} result (e.g. an {@code I18NString} attribute value) is returned as-is
 *           so it stays localizable; a {@link String} is wrapped via {@link ResKey#text(String)};
 *           any other object is labeled via {@link MetaLabelProvider}.
 */
public class ScriptedTileLabel implements TileLabelProvider {

	/**
	 * Configuration for {@link ScriptedTileLabel}.
	 */
	@TagName("scripted")
	public interface Config extends TileLabelProvider.Config<ScriptedTileLabel> {

		@Override
		@ClassDefault(ScriptedTileLabel.class)
		Class<? extends ScriptedTileLabel> getImplementationClass();

		/** Configuration name for {@link #getInputs()}. */
		String INPUTS = "inputs";

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * Comma-separated channel references whose current values are passed as positional
		 * arguments to the expression.
		 */
		@Name(INPUTS)
		@Format(CommaSeparatedChannelRefs.class)
		List<ChannelRef> getInputs();

		/**
		 * TL-Script expression returning the label string.
		 */
		@Name(EXPR)
		@Mandatory
		@NonNullable
		Expr getExpr();
	}

	private final List<ChannelRef> _inputs;

	private final QueryExecutor _executor;

	/**
	 * Creates a new {@link ScriptedTileLabel}.
	 */
	@CalledByReflection
	public ScriptedTileLabel(InstantiationContext context, Config config) {
		_inputs = config.getInputs();
		_executor = QueryExecutor.compile(config.getExpr());
	}

	@Override
	public ResKey compute(ViewContext context) {
		Object[] args = _inputs.stream()
			.map(context::resolveChannel)
			.map(ViewChannel::get)
			.toArray();
		Object result = _executor.execute(args);
		if (result == null) {
			return null;
		}
		if (result instanceof ResKey) {
			// An I18NString attribute value (or an explicit ResKey) is already localizable - keep it
			// so the breadcrumb renders the translation, not its debug toString().
			return (ResKey) result;
		}
		if (result instanceof String) {
			return ResKey.text((String) result);
		}
		// Any other business object: derive its display label rather than its toString().
		return ResKey.text(MetaLabelProvider.INSTANCE.getLabel(result));
	}
}
