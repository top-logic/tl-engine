/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.db2.DBTypeRepository;

/**
 * {@link MORepository} for migration, that creates {@link MetaObject} on demand.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MigrationRepository implements MORepository {

	private MORepository _impl;

	private Map<String, MetaObject> _createdTypes = new HashMap<>();

	/**
	 * Creates a new {@link MigrationRepository} from the given {@link KnowledgeBaseConfiguration}.
	 * 
	 * @param sqlDialect
	 *        Dialect for the database of the {@link KnowledgeBase}.
	 * @param kbConfig
	 *        The configuration describing the types in the {@link MORepository}.
	 */
	public MigrationRepository(DBHelper sqlDialect, KnowledgeBaseConfiguration kbConfig)
			throws ConfigurationException, DataObjectException {
		SchemaSetup schemaSetup = KBUtils.getSchemaConfigResolved(kbConfig);
		_impl = DBTypeRepository.newRepository(sqlDialect, schemaSetup, !kbConfig.getDisableVersioning());
		installSchema(schemaSetup.buildSchema());

	}
	/**
	 * Creates a new {@link MigrationRepository}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} to import data to.
	 */
	@CalledByReflection
	public MigrationRepository(KnowledgeBase kb) {
		_impl = kb.getMORepository();
		DBSchema dbSchema = buildSchema(kb);
		installSchema(dbSchema);
	}

	private DBSchema buildSchema(KnowledgeBase kb) {
		SchemaSetup schemaSetup = KBUtils.getSchemaConfig(kb.getConfiguration());
		return schemaSetup.buildSchema();
	}

	private void installSchema(DBSchema dbSchema) {
		for (DBTable table : dbSchema.getTables()) {
			MOStructure tableType = SchemaSetup.createTableType(table, DefaultMOFactory.INSTANCE);
			addNewType(tableType);
		}
		for (Entry<String, MetaObject> entry : _createdTypes.entrySet()) {
			MetaObject resolvedType;
			resolvedType = entry.getValue().resolve(this);
			entry.setValue(resolvedType);
		}
		for (MetaObject type : _createdTypes.values()) {
			type.freeze();
		}
	}

	@Override
	public boolean multipleBranches() {
		return _impl.multipleBranches();
	}

	@Override
	public MetaObject getTypeOrNull(String typeName) {
		MetaObject result = _impl.getTypeOrNull(typeName);
		if (result != null) {
			return result;
		}

		MetaObject type = _createdTypes.get(typeName);
		if (type == null) {
			type = new MOClassImpl(typeName);
			addNewType(type);
		}
		return type;
	}

	private void addNewType(MetaObject type) {
		_createdTypes.put(type.getName(), type);
	}

	@Override
	public void addMetaObject(MetaObject aMetaObject) throws DuplicateTypeException {
		throw new UnsupportedOperationException("Immutable during migration.");
	}

	@Override
	public MetaObject getMOCollection(String rawType, String elementTypeName) throws UnknownTypeException {
		return _impl.getMOCollection(rawType, elementTypeName);
	}

	@Override
	public boolean containsMetaObject(MetaObject aMetaObject) {
		return _impl.containsMetaObject(aMetaObject) || _createdTypes.containsValue(aMetaObject);
	}

	@Override
	public List<String> getMetaObjectNames() {
		if (_createdTypes.isEmpty()) {
			return _impl.getMetaObjectNames();
		}
		ArrayList<String> result = new ArrayList<>(_createdTypes.keySet());
		result.addAll(_impl.getMetaObjectNames());
		return result;
	}

	@Override
	public Collection<? extends MetaObject> getMetaObjects() {
		if (_createdTypes.isEmpty()) {
			return _impl.getMetaObjects();
		}
		ArrayList<MetaObject> result = new ArrayList<>(_createdTypes.values());
		result.addAll(_impl.getMetaObjects());
		return Collections.unmodifiableCollection(result);
	}

	@Override
	public void resolveReferences() throws DataObjectException {
		_impl.resolveReferences();
	}

}

