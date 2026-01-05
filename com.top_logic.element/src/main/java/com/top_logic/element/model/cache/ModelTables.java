/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.SeparateTableStorage;
import com.top_logic.element.meta.kbbased.storage.ColumnStorage;
import com.top_logic.model.ModelKind;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Connection of {@link MOStructure KB tables} to model elements.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModelTables {

	/**
	 * The classes that store their instances in a certain table.
	 */
	private final Map<MOStructure, List<TLClass>> _classesByTable;

	/**
	 * For each table and column, the descriptor that describes how values for foreign objects are
	 * stored (if any).
	 */
	private final Map<MOStructure, Map<String, AssociationStorageDescriptor>> _descriptorsByTable;

	/**
	 * For each type, a mapping that assigns the {@link TLStructuredTypePart} that stores the column
	 * with a given name of the object's table.
	 */
	private final ConcurrentHashMap<TLStructuredType, Map<String, TLStructuredTypePart>> _columnBindingByType =
		new ConcurrentHashMap<>();

	/**
	 * This constructor creates a new {@link ModelTables} without exclusions.
	 */
	public ModelTables(TLModel model) {
		this(model, FilterFactory.falseFilter());
	}

	/**
	 * This constructor creates a new {@link ModelTables} with given exclusions.
	 * 
	 * @param exclusions
	 *        Determination of {@link TLModule}s to exclude from analysis.
	 */
	public ModelTables(TLModel model, Predicate<? super TLModule> exclusions) {
		_classesByTable = new HashMap<>();
		_descriptorsByTable = new HashMap<>();

		analyzeModel(model, exclusions);
	}

	/**
	 * Analyze the given model to map technical changes to model changes.
	 * 
	 * @param exclusions
	 *        Filter for {@link TLModule} to ignore.
	 */
	private void analyzeModel(TLModel model, Predicate<? super TLModule> exclusions) {
		for (TLModule module : model.getModules()) {
			if (exclusions.test(module)) {
				continue;
			}
			for (TLType type : module.getTypes()) {
				if (type.getModelKind() == ModelKind.CLASS) {
					TLClass classType = (TLClass) type;
					try {
						analyzeType(classType);
					} catch (RuntimeException ex) {
						// Safety. Do not fail when something is strange in the model.
						Logger.error("Unable to analyze type " + TLModelUtil.qualifiedName(type), ex);
					}
				}
			}
		}
	}

	private void analyzeType(TLClass classType) {
		MOStructure table = TLModelUtil.getTable(classType);
		_classesByTable.computeIfAbsent(table, x -> new ArrayList<>()).add(classType);

		for (TLStructuredTypePart part : classType.getLocalParts()) {
			try {
				analyzeTypePart(part);
			} catch (RuntimeException ex) {
				// Safety. Do not fail when something is strange in the model.
				Logger.error("Unable to analyze type part " + TLModelUtil.qualifiedName(part), ex);
			}
		}
	}

	private void analyzeTypePart(TLStructuredTypePart part) {
		StorageDetail storage = part.getStorageImplementation();
		if (storage.isReadOnly()) {
			return;
		}
		if (storage instanceof SeparateTableStorage associationStorage) {
			associationStorage.getStorageDescriptors().forEach(descriptor -> {
				String storageTable = descriptor.getTable();
				String storageColumn = descriptor.getStorageColumn();

				MOStructure storageType = (MOStructure) part.tKnowledgeBase().getMORepository().getType(storageTable);
				Map<String, AssociationStorageDescriptor> storageByColumn =
					_descriptorsByTable.computeIfAbsent(storageType, x -> new HashMap<>());

				/* Each descriptor that uses the same storage column must deliver same base object
				 * and part id. */
				storageByColumn.putIfAbsent(storageColumn, descriptor);
			});
		}
	}

	/**
	 * Returns the {@link TLClass}es that store their elements in the given table.
	 */
	public List<TLClass> getClassesForTable(MetaObject table) {
		return _classesByTable.getOrDefault(table, Collections.emptyList());
	}

	/**
	 * For each column the descriptor that describes how values for foreign objects are stored (if
	 * any).
	 */
	public Map<String, AssociationStorageDescriptor> getDescriptorsForTable(MetaObject table) {
		return _descriptorsByTable.getOrDefault(table, Collections.emptyMap());
	}

	/**
	 * Determines a mapping that maps the name of a column to the {@link TLStructuredTypePart part}
	 * that stores its value in the column.
	 */
	public Map<String, TLStructuredTypePart> lookupColumnBinding(TLStructuredType type) {
		Map<String, TLStructuredTypePart> partByColumn = _columnBindingByType.get(type);

		if (partByColumn == null) {
			partByColumn = new HashMap<>();
			List<? extends TLStructuredTypePart> parts = type.getAllParts();
			for (TLStructuredTypePart part : parts) {
				StorageDetail storage = part.getStorageImplementation();
				if (storage instanceof ColumnStorage columnStorage) {
					partByColumn.put(columnStorage.getStorageAttribute(), part);
				}
			}
			partByColumn = MapUtil.putIfAbsent(_columnBindingByType, type, partByColumn);
		}

		return partByColumn;
	}

}
