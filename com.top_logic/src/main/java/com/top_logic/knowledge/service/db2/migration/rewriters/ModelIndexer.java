/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import static com.top_logic.knowledge.service.db2.migration.TLModelMigrationUtil.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;

import com.google.inject.Inject;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link RewritingEventVisitor} changing the annotations for a certain {@link TLTypePart}.
 * 
 * <p>
 * The rewrites modifies all versions of affected model parts.
 * </p>
 */
public class ModelIndexer<C extends ModelIndexer.Config<?>> extends ConfiguredRewritingEventVisitor<C> {

	private final BidiMap<String, ObjectKey> _partKeyByName = new BidiHashMap<>();

	private Log _log = new LogProtocol(ModelIndexer.class);

	/**
	 * Create a {@link ModelIndexer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelIndexer(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Sets the {@link Log} to write messages to.
	 */
	@Inject
	public void setLog(Log log) {
		_log = log;
	}

	/**
	 * The current {@link Log}.
	 */
	public Log getLog() {
		return _log;
	}

	/**
	 * The {@link TLModelPart} name of the element with the given ID.
	 */
	public String getName(ObjectKey key) {
		return _partKeyByName.getKey(key);
	}

	/**
	 * The {@link TLModelPart} ID of the element with the given qualified name.
	 */
	public ObjectKey getKey(String qualifiedPartName) {
		return _partKeyByName.get(qualifiedPartName);
	}

	@Override
	protected void processCreations(ChangeSet cs) {
		Map<String, List<ObjectCreation>> byType =
			cs.getCreations().stream().collect(Collectors.groupingBy(change -> change.getObjectType().getName()));

		byType.getOrDefault(MODULE_MO, Collections.emptyList())
			.forEach(this::processModuleCreation);
		byType.getOrDefault(STRUCTURED_TYPE_MO, Collections.emptyList())
			.forEach(this::processStructuredTypeCreation);
		byType.getOrDefault(FastList.OBJECT_NAME, Collections.emptyList())
			.forEach(this::processEnumCreation);
		byType.getOrDefault(STRUCTURED_TYPE_PART_MO, Collections.emptyList())
			.forEach(this::processStructuredTypePartCreation);
		byType.getOrDefault(FastListElement.OBJECT_NAME, Collections.emptyList())
			.forEach(this::processClassifierCreation);
	}

	/**
	 * Called with all changes creating a {@link TLModule}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processModuleCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String moduleName = (String) values.get(MODULE_NAME_ATTRIBUTE);
		processModelPartCreation(creation, moduleName);
	}

	/**
	 * Called with all changes creating a {@link TLStructuredType}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processStructuredTypeCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String typeName = (String) values.get(STRUCTURED_TYPE_NAME_ATTRIBUTE);
		ObjectKey moduleKey = (ObjectKey) values.get(STRUCTURED_TYPE_MODULE_ATTRIBUTE);
		String moduleName = _partKeyByName.getKey(moduleKey);
		if (moduleName != null) {
			processModelPartCreation(creation, moduleName + TLModelUtil.QUALIFIED_NAME_SEPARATOR + typeName);
		}
	}

	/**
	 * Called with all changes creating a {@link TLEnumeration}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processEnumCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String typeName = (String) values.get(FastList.NAME_ATTRIBUTE);
		ObjectKey moduleKey = (ObjectKey) values.get(STRUCTURED_TYPE_MODULE_ATTRIBUTE);
		String moduleName = _partKeyByName.getKey(moduleKey);
		if (moduleName != null) {
			processModelPartCreation(creation, moduleName + TLModelUtil.QUALIFIED_NAME_SEPARATOR + typeName);
		}
	}

	/**
	 * Called with all changes creating a {@link TLStructuredTypePart}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processStructuredTypePartCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String partName = (String) values.get(TLTypePart.NAME_ATTRIBUTE);
		ObjectKey typeKey = (ObjectKey) values.get(STRUCTURED_TYPE_PART_OWNER_ATTRIBUTE);
		String typeName = _partKeyByName.getKey(typeKey);
		if (typeName != null) {
			processModelPartCreation(creation, typeName + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + partName);
		}
	}

	/**
	 * Called with all changes creating a {@link TLClassifier}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processClassifierCreation(ObjectCreation creation) {
		Map<String, Object> values = creation.getValues();
		String partName = (String) values.get(FastListElement.NAME_ATTRIBUTE);
		ObjectKey typeKey = (ObjectKey) values.get(FastListElement.OWNER_ATTRIBUTE);
		String typeName = _partKeyByName.getKey(typeKey);
		if (typeName != null) {
			processModelPartCreation(creation, typeName + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + partName);
		}
	}

	/**
	 * Called with all changes creating a {@link TLModelPart}.
	 *
	 * @param creation
	 *        The change.
	 */
	protected void processModelPartCreation(ObjectCreation creation, String name) {
		ObjectKey moduleKey = creation.getObjectId().toCurrentObjectKey();
		_partKeyByName.put(name, moduleKey);
	}

	@Override
	protected void processDeletions(ChangeSet cs) {
		for (ItemDeletion deletion : cs.getDeletions()) {
			String tableName = deletion.getObjectType().getName();
			if (MODULE_MO.equals(tableName)) {
				processModuleDeletion(deletion);
			} else if (STRUCTURED_TYPE_MO.equals(tableName)) {
				processStructuredTypeDeletion(deletion);
			} else if (FastList.OBJECT_NAME.equals(tableName)) {
				processEnumDeletion(deletion);
			} else if (STRUCTURED_TYPE_PART_MO.equals(tableName)) {
				processStructuredTypePartDeletion(deletion);
			} else if (FastListElement.OBJECT_NAME.equals(tableName)) {
				processClassifierDeletion(deletion);
			}
		}
	}

	/**
	 * Called with all changes deleting a {@link TLModule}.
	 *
	 * @param deletion
	 *        The change.
	 */
	protected void processModuleDeletion(ItemDeletion deletion) {
		processModelPartDeletion(deletion);
	}

	/**
	 * Called with all changes deleting a {@link TLStructuredType}.
	 *
	 * @param deletion
	 *        The change.
	 */
	protected void processStructuredTypeDeletion(ItemDeletion deletion) {
		processModelPartDeletion(deletion);
	}

	/**
	 * Called with all changes deleting a {@link TLEnumeration}.
	 *
	 * @param deletion
	 *        The change.
	 */
	protected void processEnumDeletion(ItemDeletion deletion) {
		processModelPartDeletion(deletion);
	}

	/**
	 * Called with all changes deleting a {@link TLStructuredTypePart}.
	 *
	 * @param deletion
	 *        The change.
	 */
	protected void processStructuredTypePartDeletion(ItemDeletion deletion) {
		processModelPartDeletion(deletion);
	}

	/**
	 * Called with all changes deleting a {@link TLClassifier}.
	 *
	 * @param deletion
	 *        The change.
	 */
	protected void processClassifierDeletion(ItemDeletion deletion) {
		processModelPartDeletion(deletion);
	}

	/**
	 * Called with all changes deleting a {@link TLModelPart}.
	 *
	 * @param deletion
	 *        The change.
	 */
	public void processModelPartDeletion(ItemDeletion deletion) {
		_partKeyByName.removeValue(deletion.getObjectId().toCurrentObjectKey());
	}

	@Override
	protected void processUpdates(ChangeSet cs) {
		for (ItemUpdate update : cs.getUpdates()) {
			String tableName = update.getObjectType().getName();
			if (MODULE_MO.equals(tableName)) {
				processModuleUpdate(update);
			} else if (STRUCTURED_TYPE_MO.equals(tableName)) {
				processStructuredTypeUpdate(update);
			} else if (FastList.OBJECT_NAME.equals(tableName)) {
				processEnumUpdate(update);
			} else if (STRUCTURED_TYPE_PART_MO.equals(tableName)) {
				processStructuredTypePartUpdate(update);
			} else if (FastListElement.OBJECT_NAME.equals(tableName)) {
				processClassifierUpdate(update);
			}
		}
	}

	/**
	 * Called with all changes updating a {@link TLModule}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processModuleUpdate(ItemUpdate update) {
		processModelPartUpdate(update);
	}

	/**
	 * Called with all changes updating a {@link TLStructuredType}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processStructuredTypeUpdate(ItemUpdate update) {
		processModelPartUpdate(update);
	}

	/**
	 * Called with all changes updating a {@link TLEnumeration}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processEnumUpdate(ItemUpdate update) {
		processModelPartUpdate(update);
	}

	/**
	 * Called with all changes updating a {@link TLStructuredTypePart}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processStructuredTypePartUpdate(ItemUpdate update) {
		processModelPartUpdate(update);
	}

	/**
	 * Called with all changes updating a {@link TLClassifier}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processClassifierUpdate(ItemUpdate update) {
		processModelPartUpdate(update);
	}

	/**
	 * Called with all changes updating a {@link TLModelPart}.
	 *
	 * @param update
	 *        The change.
	 */
	protected void processModelPartUpdate(ItemUpdate update) {
		// Hook for subclasses.
	}

}
