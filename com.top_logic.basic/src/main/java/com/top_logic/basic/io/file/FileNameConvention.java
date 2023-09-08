/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.file;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * The conventions for file names in <i>TopLogic</i> based projects.
 * <p>
 * These conventions are tested by <code>TestFileNames</code>. But as that is a test class, it
 * cannot be linked here. And this configuration cannot be moved to the test package, as the test
 * configurations are not inherited: The same configuration had to be copied to each single project.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FileNameConvention extends ConfigurationItem {

	/** Property name of {@link #getRules()}. */
	String RULES = "rules";

	/** The rules that this test checks. */
	@Name(RULES)
	@DefaultContainer
	@Key(FileNameRule.NAME_ATTRIBUTE)
	Map<String, FileNameRule> getRules();

}
