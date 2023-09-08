/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import java.util.Set;

import com.top_logic.model.TLClass;

/**
 * Source for getting {@link TableConfigModelInfo}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TableConfigModelInfoProvider {

	/**
	 * The {@link TableConfigModelInfo} for the given content types.
	 * <p>
	 * The cache must not be stored beyond the current interaction.
	 * </p>
	 * <p>
	 * The cache is filled automatically on-demand.
	 * </p>
	 */
	TableConfigModelInfo getModelInfo(Set<? extends TLClass> classes);

}
