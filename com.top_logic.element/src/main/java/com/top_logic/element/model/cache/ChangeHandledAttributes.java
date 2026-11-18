/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ChangeLocator;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.knowledge.service.event.PendingChanges;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLChangeHandler;
import com.top_logic.model.annotate.TLChangeHandlers;
import com.top_logic.model.util.TLModelUtil;

/**
 * Index of the attributes with {@link TLChangeHandler}s by the tables in which their values are
 * stored.
 *
 * <p>
 * For each table, the index lists the attributes whose values may be changed by creating,
 * updating, or removing a row of that table, together with the way to find the objects whose
 * values may have changed from the {@link PendingChanges} of a commit. A commit that changes only
 * tables without such attributes needs no further inspection.
 * </p>
 *
 * <p>
 * The locators are contributed by the storage of each attribute, see
 * {@link StorageImplementation#addChangeLocators(TLStructuredTypePart, java.util.function.BiConsumer)}.
 * An attribute whose storage contributes no locators (derived, transient, or computed values)
 * cannot have change handlers, see {@link TLChangeHandlers}.
 * </p>
 *
 * @see ModelTables#getChangeHandledAttributes()
 * @see TLChangeHandlers
 */
public class ChangeHandledAttributes {

	private final Map<MetaObject, List<HandledAttribute>> _attributesByTable;

	/**
	 * Creates a {@link ChangeHandledAttributes} index.
	 *
	 * @param classesByTable
	 *        The classes of the model by the table in which they store their instances.
	 */
	ChangeHandledAttributes(Map<MOStructure, List<TLClass>> classesByTable) {
		Map<MetaObject, List<HandledAttribute>> attributesByTable = new HashMap<>();
		Set<TLStructuredTypePart> indexed = new HashSet<>();
		for (List<TLClass> types : classesByTable.values()) {
			for (TLClass type : types) {
				if (type.isAbstract()) {
					continue;
				}
				for (TLStructuredTypePart part : type.getAllParts()) {
					if (!indexed.add(part)) {
						continue;
					}
					try {
						index(attributesByTable, part);
					} catch (RuntimeException ex) {
						// Safety. Do not fail when something is strange in the model.
						Logger.error("Unable to index change handlers of " + TLModelUtil.qualifiedName(part), ex,
							ChangeHandledAttributes.class);
					}
				}
			}
		}
		_attributesByTable = attributesByTable;
	}

	private static void index(Map<MetaObject, List<HandledAttribute>> attributesByTable, TLStructuredTypePart part) {
		List<? extends TLChangeHandler> handlers = AttributeOperations.getChangeHandlers(part);
		if (handlers.isEmpty()) {
			return;
		}
		StorageImplementation storage = AttributeOperations.getStorageImplementation(part);
		storage.addChangeLocators(part,
			(table, locator) -> attributesByTable.computeIfAbsent(table, x -> new ArrayList<>())
				.add(new HandledAttribute(part, handlers, locator)));
	}

	/**
	 * Whether no attribute of the model has {@link TLChangeHandler}s.
	 */
	public boolean isEmpty() {
		return _attributesByTable.isEmpty();
	}

	/**
	 * The attributes with {@link TLChangeHandler}s whose values may be changed by a created,
	 * updated, or removed row of the given table.
	 *
	 * @param table
	 *        The concrete table of a changed row.
	 * @return The attributes whose values may be stored in rows of the given table, empty if
	 *         there are none.
	 */
	public List<HandledAttribute> getAttributes(MetaObject table) {
		return _attributesByTable.getOrDefault(table, Collections.emptyList());
	}

	/**
	 * An attribute with {@link TLChangeHandler}s whose values are stored in a certain table.
	 *
	 * @param attribute
	 *        The attribute, as it is defined in the types of the objects that are stored in the
	 *        table. An object reported by the {@link #locator()} has this attribute, if
	 *        {@link TLClass#getPart(String)} of its type resolves this attribute.
	 * @param handlers
	 *        All {@link TLChangeHandler}s of the attribute, see
	 *        {@link AttributeOperations#getChangeHandlers(TLStructuredTypePart)}.
	 * @param locator
	 *        Finds the objects whose value of the attribute may have changed.
	 */
	public record HandledAttribute(TLStructuredTypePart attribute, List<? extends TLChangeHandler> handlers,
			ChangeLocator locator) {
		// Pure data.
	}

}
