/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.LabelPositioning;
import com.top_logic.model.annotate.LabelPosition;

/**
 * What every agent run is told about the situation it runs in.
 *
 * <p>
 * The harness composes one environment block per run and puts it into the system prompt. This
 * section of {@code AgentApplicationService} is where an installation changes its wording and names the
 * application the model is told it is working in.
 * </p>
 *
 * @see EnvironmentTemplate
 * @see RunEnvironment
 */
@TagName("environment")
@DisplayOrder({
	EnvironmentConfig.APPLICATION,
	EnvironmentConfig.TEMPLATE,
})
public interface EnvironmentConfig extends ConfigPart {

	/** Configuration property name for {@link #getTemplate()}. */
	String TEMPLATE = "template";

	/** Configuration property name for {@link #getApplication()}. */
	String APPLICATION = "application";

	/**
	 * The text of the environment block, in the language of the account the run acts for.
	 *
	 * <p>
	 * It may use every placeholder a system prompt may use, except {@code {{environment}}} - the
	 * block cannot contain itself. Left empty, the shipped English default is used, which is the
	 * constant {@code EnvironmentTemplate.DEFAULT}.
	 * </p>
	 *
	 * @see #TEMPLATE
	 */
	@Name(TEMPLATE)
	@Label("Template")
	@Nullable
	@LabelPositioning(LabelPosition.ABOVE)
	@ControlProvider(MultiLineText.class)
	ResKey getTemplate();

	/**
	 * What to call this application in front of the model.
	 *
	 * <p>
	 * Left empty, the application's own name is used - the one the title bar shows. Set it when the
	 * model should be told something the title bar does not say, for example a product name a
	 * customer knows the installation by.
	 * </p>
	 *
	 * @see #APPLICATION
	 */
	@Name(APPLICATION)
	@Label("Application")
	@Nullable
	ResKey getApplication();
}
