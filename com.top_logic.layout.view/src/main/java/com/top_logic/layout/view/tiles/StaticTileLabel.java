/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link TileLabelProvider} that always returns a configured {@link ResKey}.
 */
public class StaticTileLabel implements TileLabelProvider {

	/**
	 * Configuration for {@link StaticTileLabel}.
	 */
	@TagName("static")
	public interface Config extends TileLabelProvider.Config<StaticTileLabel> {

		@Override
		@ClassDefault(StaticTileLabel.class)
		Class<? extends StaticTileLabel> getImplementationClass();

		/** Configuration name for {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The label to use for the pushed frame.
		 */
		@Name(VALUE)
		@Mandatory
		ResKey getValue();
	}

	private final ResKey _value;

	/**
	 * Creates a new {@link StaticTileLabel}.
	 */
	@CalledByReflection
	public StaticTileLabel(InstantiationContext context, Config config) {
		_value = config.getValue();
	}

	@Override
	public ResKey compute(ViewContext context) {
		return _value;
	}
}
