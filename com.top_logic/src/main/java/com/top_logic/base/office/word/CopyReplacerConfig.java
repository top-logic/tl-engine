/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.word;

import java.util.Map;

/**
 * Hold the number of replacements and the token replacements for a CopyReplacer
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class CopyReplacerConfig {
	
	/** The number of copies. */
	public int copyNo;
	
	/** The tag replacements (old tag name - new tag name). */
	public Map tagReplacements;
	
	public final String endToken;
	
	/** 
	 * Create a new CopyReplacerConfig without tag replacements
	 * 
	 * @param aCopyNo the number of copies
	 */
	public CopyReplacerConfig(int aCopyNo) {
		this(aCopyNo, null);
	}
	
	/** 
	 * Create a new CopyReplacerConfig
	 * 
	 * @param aCopyNo			the number of copies
	 * @param aTagReplacements	the tag replacements
	 */
	public CopyReplacerConfig(int aCopyNo, Map aTagReplacements) {
		this(aCopyNo, aTagReplacements, null);
	}
	
	public CopyReplacerConfig(int aCopyNo, Map aTagReplacements, String endToken) {
		this.copyNo          = aCopyNo;
		this.tagReplacements = aTagReplacements;
		this.endToken = endToken;
	}
}