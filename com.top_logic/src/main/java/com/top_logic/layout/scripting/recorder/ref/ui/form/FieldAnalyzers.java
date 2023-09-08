/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;

/**
 * Global configuration that lists all available {@link FieldAnalyzer}s in the system.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldAnalyzers extends ConfigurationItem {

	/**
	 * List of {@link FieldAnalyzer}s to use for identifying fields during script recording.
	 * 
	 * <p>
	 * Note: The list is ordered in reverse order of priority so that custom modules are able to
	 * easily add high-priority {@link FieldAnalyzer}s.
	 * </p>
	 */
	@InstanceFormat
	@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
	List<FieldAnalyzer> getAnalyzers();

}
