
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.internet;

/**
 * Simple interface for basic information of an iternet link delivered by 
 * search engines.
 *
 * @author  Marco Perra
 */
public interface Link extends java.io.Serializable {

	/**
	 * Returns the title of this link.
	 */
	public String getTitle();

	/**
	 * Returns an description of this link.
	 */
	public String getDescription();

	/**
	 * Returns the URL of this link.
	 */
	public String getURLString();

	/**
	 * Returns a number in the range from 0.0 to 1.0
	 */
	public double getRanking();
}

