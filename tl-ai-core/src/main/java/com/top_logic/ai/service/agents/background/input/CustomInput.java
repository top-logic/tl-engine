/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.model.annotate.LabelPosition;

/**
 * Input configuration that uses a fixed custom message.
 *
 * <p>
 * The background agent is invoked with a static message defined at configuration time. This is
 * useful for autonomous agents that perform a predefined task without requiring dynamic context.
 * </p>
 */
@Label("Custom message")
public interface CustomInput extends InputConfig {

	/** @see #getCustomMessage() */
	String CUSTOM_MESSAGE = "custom-message";

	/**
	 * The fixed message that is passed to the background agent as input.
	 *
	 * @return The custom message, never {@code null}.
	 */
	@Name(CUSTOM_MESSAGE)
	@Label("Custom message")
	@Mandatory
	@RenderWholeLine
	@ControlProvider(MultiLineText.class)
	@LabelPositioning(LabelPosition.ABOVE)
	String getCustomMessage();
}
