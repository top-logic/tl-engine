/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * {@link GenericFunction} listing all table names of tables derived from a given
 * {@link #getBaseTable()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AllTables extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		MORepository repository = PersistencyLayer.getKnowledgeBase().getMORepository();
		MetaObject baseTable;
		try {
			baseTable = repository.getMetaObject(getBaseTable());
		} catch (UnknownTypeException ex) {
			return Collections.emptyList();
		}
		ArrayList<String> result = new ArrayList<>();
		for (MetaObject type : repository.getMetaObjects()) {
			if (!(type instanceof MOClass)) {
				continue;
			}
			if (((MOClass) type).isAbstract()) {
				continue;
			}
			if (type.isSubtypeOf(baseTable)) {
				result.add(type.getName());
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * The name of the base table to retrieve all derived tables of.
	 */
	protected abstract String getBaseTable();

}
