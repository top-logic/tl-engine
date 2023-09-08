/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;

/**
 * {@link MOAnnotation} marking a {@link MOClass} using full-load during search.
 * 
 * @see #getFullLoad()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FullLoadAnnotation extends MOAnnotation {

	/**
	 * @see #getFullLoad()
	 */
	String FULL_LOAD = "full-load";

	/**
	 * Whether this class uses the full-load strategy for searches by default.
	 * 
	 * <p>
	 * In a search, either a two-stage load (identifiers of matched objects first and data only for
	 * those objects that are not already in cache), or a full load (identifier together with data)
	 * is performed. The full-load strategy optimizes the number of database requests, while the
	 * identifiers-first load minimizes temporary memory usage (at least if some of the result
	 * objects are already in the local cache).
	 * </p>
	 */
	@Name(FULL_LOAD)
	boolean getFullLoad();

}
