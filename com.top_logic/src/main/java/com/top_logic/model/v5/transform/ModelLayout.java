/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.v5.transform;

import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLModule;

/**
 * Conventions for an imported <i>TopLogic</i> 5 runtime model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelLayout {

	/**
	 * Name of the {@link TLModule} that contains the {@link FastList} enumeration types.
	 */
	public static final String TL5_ENUM_MODULE = "tl5.enum";
	
}
