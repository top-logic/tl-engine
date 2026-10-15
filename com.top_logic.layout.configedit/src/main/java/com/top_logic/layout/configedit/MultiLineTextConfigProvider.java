/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;

/**
 * {@link ConfigControlProvider} editing a text property in a multi-line text area.
 *
 * <p>
 * The {@code impl} for a {@link ConfigControlService.ControlProviderMapping} entry that maps the
 * declarative form's {@link MultiLineText} to this service: a property annotated with it is edited
 * in a {@link ReactTextInputControl} spanning several rows. The input behaves like the single-line
 * text input in every other respect - a formatted value is sent when the field is left, a plain
 * text while it is typed.
 * </p>
 */
public class MultiLineTextConfigProvider extends AbstractConfiguredInstance<MultiLineTextConfigProvider.Config>
		implements ConfigControlProvider {

	/**
	 * Configuration options for {@link MultiLineTextConfigProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<MultiLineTextConfigProvider> {

		/** Property name of {@link #getRows()}. */
		String ROWS = "rows";

		/**
		 * The number of visible rows of the text area.
		 *
		 * <p>
		 * The annotation of the declarative form names a control provider class only, which that
		 * form instantiates with its default number of rows; this is the same default.
		 * </p>
		 */
		@Name(ROWS)
		@IntDefault(MultiLineText.DEFAULT_ROWS)
		int getRows();

	}

	/**
	 * Creates a {@link MultiLineTextConfigProvider}.
	 */
	@CalledByReflection
	public MultiLineTextConfigProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		ReactTextInputControl input = ConfigControlService.createTextInput(context, model);
		input.setMultiline(getConfig().getRows());
		return input;
	}

}
