/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * Selection of the tables a {@link MigrationProcessor} works on.
 *
 * <p>
 * The tables are given by the names of their types: explicitly by {@link #getTables()}, or as all
 * concrete subtypes of the types in {@link #getSubtypesOf()}, so that tables of types defined by an
 * application are selected without naming them. Types in {@link #getExclude()} and their subtypes
 * are removed from the selection.
 * </p>
 *
 * @see #resolve(Log, TableSelection, TypeContext)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableSelection extends ConfigurationItem {

	/**
	 * Configuration name of {@link #getTables()}.
	 */
	String TABLES = "tables";

	/**
	 * Configuration name of {@link #getSubtypesOf()}.
	 */
	String SUBTYPES_OF = "subtypes-of";

	/**
	 * Configuration name of {@link #getExclude()}.
	 */
	String EXCLUDE = "exclude";

	/**
	 * Names of the types whose tables are selected.
	 */
	@Name(TABLES)
	@Format(CommaSeparatedStrings.class)
	List<String> getTables();

	/**
	 * @see #getTables()
	 */
	void setTables(List<String> value);

	/**
	 * Names of the types whose concrete subtypes are selected.
	 *
	 * <p>
	 * A type itself belongs to its subtypes. Abstract types have no table and are not selected.
	 * </p>
	 */
	@Name(SUBTYPES_OF)
	@Format(CommaSeparatedStrings.class)
	List<String> getSubtypesOf();

	/**
	 * @see #getSubtypesOf()
	 */
	void setSubtypesOf(List<String> value);

	/**
	 * Names of the types that are removed from the selection together with their subtypes.
	 */
	@Name(EXCLUDE)
	@Format(CommaSeparatedStrings.class)
	List<String> getExclude();

	/**
	 * @see #getExclude()
	 */
	void setExclude(List<String> value);

	/**
	 * Whether the given selection names neither {@link #getTables() tables} nor
	 * {@link #getSubtypesOf() types} to select.
	 */
	static boolean isEmpty(TableSelection selection) {
		return selection.getTables().isEmpty() && selection.getSubtypesOf().isEmpty();
	}

	/**
	 * Resolves the types selected by the given selection.
	 *
	 * @param log
	 *        The {@link Log} to report explicitly selected types to, that are unknown or have no
	 *        table.
	 * @param selection
	 *        The selection to resolve.
	 * @param types
	 *        The types to select from.
	 * @return The selected concrete types: the {@link #getTables() explicitly selected} ones in
	 *         their configured order, followed by the {@link #getSubtypesOf() subtypes} ordered by
	 *         name, each type only once.
	 */
	static List<MOClass> resolve(Log log, TableSelection selection, TypeContext types) {
		Map<String, MOClass> result = new LinkedHashMap<>();
		for (String name : selection.getTables()) {
			MOClass type = tableType(types.getTypeOrNull(name));
			if (type == null) {
				log.error("Type '" + name + "' does not exist or has no table.");
				continue;
			}
			result.put(type.getName(), type);
		}

		if (!selection.getSubtypesOf().isEmpty()) {
			List<MOClass> subtypes = new ArrayList<>();
			for (MetaObject candidate : types.getMetaObjects()) {
				MOClass type = tableType(candidate);
				if (type == null) {
					continue;
				}
				for (String superName : selection.getSubtypesOf()) {
					if (type.isSubtypeOf(superName)) {
						subtypes.add(type);
						break;
					}
				}
			}
			subtypes.sort(Comparator.comparing(MOClass::getName));
			for (MOClass type : subtypes) {
				result.putIfAbsent(type.getName(), type);
			}
		}

		List<String> excludes = selection.getExclude();
		if (!excludes.isEmpty()) {
			result.values().removeIf(type -> excludes.stream().anyMatch(type::isSubtypeOf));
		}
		return new ArrayList<>(result.values());
	}

	/**
	 * The given type, if it is stored in a table of its own.
	 */
	private static MOClass tableType(MetaObject type) {
		if (type == null || type.getKind() != MetaObject.Kind.item) {
			return null;
		}
		MOClass result = (MOClass) type;
		return result.isAbstract() ? null : result;
	}

}
