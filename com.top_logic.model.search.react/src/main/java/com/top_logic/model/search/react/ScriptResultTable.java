/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Table showing a script evaluation result, with columns derived from the result.
 *
 * <p>
 * App-specific console widget (referenced by {@code class=}, not a reusable {@code @TagName}
 * element). The {@link Config#getInput() input channel} holds the raw evaluation result; a written
 * result rebuilds the table through a {@link ScriptResultControl}, which derives the columns from
 * the row values (one column per object attribute, plus a label column for primitive values).
 * </p>
 */
public class ScriptResultTable implements UIElement {

	/**
	 * Configuration for {@link ScriptResultTable}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(ScriptResultTable.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the raw evaluation result to display.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link ScriptResultTable} from configuration.
	 */
	@CalledByReflection
	public ScriptResultTable(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel dataChannel = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		return new ScriptResultControl(context, dataChannel);
	}
}
