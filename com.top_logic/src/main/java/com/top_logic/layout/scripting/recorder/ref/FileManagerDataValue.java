/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.io.File;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.FileManager;

/**
 * {@link DataItemValue} that uses the {@link FileManager} to find the requested {@link File}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FileManagerDataValue extends DataItemValue {

	/**
	 * The path to the {@link File}.
	 * <p>
	 * Resolved with the {@link FileManager}, after the {@link AliasManager} replaced aliases.
	 * </p>
	 */
	@Override
	String getName();

}
