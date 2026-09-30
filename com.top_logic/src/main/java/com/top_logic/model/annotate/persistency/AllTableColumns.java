/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.func.Function1;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Function delivering all columns of a given table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllTableColumns extends Function1<List<String>, String> {

	@Override
	public List<String> apply(String tableName) {
		if (StringServices.isEmpty(tableName)) {
			return Collections.emptyList();
		}
		MORepository repository = PersistencyLayer.getKnowledgeBase().getMORepository();
		MetaObject table;
		try {
			table = repository.getMetaObject(tableName);
		} catch (UnknownTypeException ex) {
			return Collections.emptyList();
		}

		List<String> columnsNames = getColumnsNames(table);
		Collections.sort(columnsNames);
		return columnsNames;
	}

	/**
	 * Determines the table names of the given column.
	 * 
	 * @implNote This method returns the names of the {@link MOAttribute}s returned by
	 *           {@link #getColumns(MetaObject)}.
	 */
	protected List<String> getColumnsNames(MetaObject table) {
		return getColumns(table).map(MOAttribute::getName).collect(Collectors.toList());
	}

	/**
	 * Determines the sequence of {@link MOAttribute}s that are used to get the column names.
	 * 
	 * @see #getColumnsNames(MetaObject)
	 */
	protected Stream<? extends MOAttribute> getColumns(MetaObject table) {
		return MetaObjectUtils.getAttributes(table).stream();
	}

}
