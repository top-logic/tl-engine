/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import java.io.File;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * A {@link ConfigurableFilter} that filters {@link File} objects by the ending of their name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FileEndingFilter extends StringEndingFilter<File> {

	/** {@link TypedConfiguration} constructor for {@link FileEndingFilter}. */
	public FileEndingFilter(InstantiationContext context, Config config) {
		super(context, config, File.class);
	}

	@Override
	protected String toString(File file) {
		return file.getName();
	}

}
