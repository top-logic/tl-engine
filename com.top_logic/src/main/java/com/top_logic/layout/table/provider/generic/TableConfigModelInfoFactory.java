/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import java.util.Set;

import com.top_logic.model.TLClass;

/**
 * Factory for {@link TableConfigModelInfo}s.
 * <p>
 * All implementations have to be thread-safe and stateless.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TableConfigModelInfoFactory {

	/**
	 * Create a {@link TableConfigModelInfo} for the given {@link Set} of content classes.
	 */
	TableConfigModelInfo create(Set<? extends TLClass> contentTypes);

}
