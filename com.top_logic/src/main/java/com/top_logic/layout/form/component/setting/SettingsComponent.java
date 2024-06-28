/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.setting;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link FormComponent} for transient settings or value display.
 * 
 * <p>
 * The component displays and/or edits some transient model. Additionally, it comes with an
 * <code>output</code> channel, that allows to communicate (a copy of) the edited object to
 * dependent components.
 * </p>
 */
public class SettingsComponent extends FormComponent implements EditMode, WithOutputChannel {

	/**
	 * Configuration options for {@link SettingsComponent}.
	 */
	@TagName("settings")
	public interface Config<I extends SettingsComponent>
			extends FormComponent.Config, EditMode.Config, WithOutputChannel.Config {

		@Override
		@ClassDefault(SettingsComponent.class)
		Class<? extends I> getImplementationClass();

	}

	/**
	 * Creates a {@link SettingsComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SettingsComponent(InstantiationContext context, Config<?> config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * @see #channels()
	 */
	protected static final Map<String, ChannelSPI> CHANNELS =
		channels(LayoutComponent.MODEL_CHANNEL, EditMode.EDIT_MODE_SPI, WithOutputChannel.OUTPUT_SPI);

	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		EditMode.super.linkChannels(log);
		WithOutputChannel.super.linkChannels(log);
	}

	@Override
	public void handleComponentModeChange(boolean editMode) {
		if (!hasFormContext()) {
			return;
		}
		if (editMode) {
			getFormContext().setImmutable(false);
		} else {
			getFormContext().setImmutable(true);
		}
	}

}
