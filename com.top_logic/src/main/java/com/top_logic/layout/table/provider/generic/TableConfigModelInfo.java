/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import java.util.Map;
import java.util.Set;

import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.model.TLClass;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Derived information about the {@link ModelService#getApplicationModel() application model}, that
 * is especially relevant for configuring {@link TableModel}s.
 * 
 * <p>
 * Instances of this type are only valid during the current interaction and must therefore not be
 * cached for later usage.
 * </p>
 * 
 * <p>
 * Instances of this type have to be immutable.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface TableConfigModelInfo {

	/**
	 * The {@link Set} of {@link TLClass}es this {@link TableConfigModelInfo}-object is about.
	 * 
	 * @return An immutable {@link Set}. Never null.
	 */
	Set<TLClass> getContentTypes();

	/**
	 * The common super-classes of the {@link #getContentTypes}.
	 * <p>
	 * See {@link TLModelUtil#getCommonGeneralizations(Iterable)} for the exact semantics.
	 * </p>
	 * 
	 * @return An immutable {@link Set}. Never null.
	 */
	Set<TLClass> getCommonSuperClasses();

	/**
	 * The local names of the {@link MainProperties} of the {@link #getContentTypes}.
	 * 
	 * @return An ordered unmodifiable set of column names.
	 */
	Set<String> getMainColumns();

	/**
	 * The {@link ColumnInfo}s for the {@link Set} of {@link #getContentTypes}, indexed by the
	 * technical name of the column.
	 * 
	 * @return {@link ColumnInfo}s indexed in an ordered unmodifiable map.
	 */
	Map<String, ColumnInfo> getColumnInfos();

}
