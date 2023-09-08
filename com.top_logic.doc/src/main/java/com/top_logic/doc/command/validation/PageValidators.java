/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command.validation;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;

/**
 * {@link ConfigurationItem} holding a sequence of {@link PageValidator}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageValidators extends ConfigurationItem {

	/**
	 * Configuration of the required {@link PageValidator}s.
	 */
	@DefaultContainer
	List<PolymorphicConfiguration<PageValidator>> getValidators();

}

