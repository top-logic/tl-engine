/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge.layout;

/**
 * Constants for merge trees.
 * 
 * @see MergeTreeComponenet
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MergeTreeConstants {

	/** All Checkboxes of this rendere use the same name */
	public static final String CHECKBOX_NAME = "approve";
	/** IDs of error messages are concatenated by this String */
	public static final String ERROR_CONC = ".E.";
	/** IDs of info messages are concatenated by this String */
	public static final String INFO_CONC = ".M.";
	/** CSS Styles to be used for the different message levels */
	public static final String[] LEVEL_STYLES = {
	    "mergeDebug", "mergeInfo", "mereWarn", "mergeError", "mergeFatal"};
	/** Images in TODO KH to use for the differentlevels */
	public static final String[] LEVEL_IMAGES = {
	    "mergeDebug", "mergeInfo", "mereWarn", "mergeError", "mergeFatal"};

}

