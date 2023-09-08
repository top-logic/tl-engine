/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.values.CommonMarkText;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;

/**
 * Object that has a verbose description.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Described extends ConfigurationItem {

	/** Configuration name for the value of {@link #getDescription()}. */
	String DESCRIPTION = "description";

	/**
	 * A verbose explanation of the operation behavior.
	 * 
	 * <p>
	 * <i>CommonMark</i> syntax may be used for rich text representation.
	 * </p>
	 */
	@Name(DESCRIPTION)
	@Nullable
	@ControlProvider(CommonMarkText.class)
	@RenderWholeLine
	String getDescription();

	/**
	 * Setter for {@link #getDescription()}.
	 */
	void setDescription(String value);

}

