/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.DBTypeFormat;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLScope;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.impl.util.TLPrimitiveColumns;

/**
 * Persistent variant of a {@link TLPrimitive}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentDatatype extends DynamicType implements TLPrimitive, TLPrimitiveColumns {

	/**
	 * Column that stores {@link #getKind()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#KIND_ATTR} instead
	 */
	public static final String KIND_ATTR = TLPrimitiveColumns.KIND_ATTR;

	/**
	 * The table to store {@link PersistentDatatype}s in.
	 * @deprecated Use {@link TLPrimitiveColumns#OBJECT_TYPE} instead
	 */
	public static final String OBJECT_TYPE = TLPrimitiveColumns.OBJECT_TYPE;

	/**
	 * Column that stores {@link #getDBType()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#DB_TYPE_ATTR} instead
	 */
	public static final String DB_TYPE_ATTR = TLPrimitiveColumns.DB_TYPE_ATTR;

	/**
	 * Column that stores {@link #getDBSize()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#DB_SIZE_ATTR} instead
	 */
	public static final String DB_SIZE_ATTR = TLPrimitiveColumns.DB_SIZE_ATTR;

	/**
	 * Column that stores {@link #getDBPrecision()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#DB_PRECISION_ATTR} instead
	 */
	public static final String DB_PRECISION_ATTR = TLPrimitiveColumns.DB_PRECISION_ATTR;

	/**
	 * Column that stores {@link #isBinary()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#BINARY_ATTR} instead
	 */
	public static final String BINARY_ATTR = TLPrimitiveColumns.BINARY_ATTR;

	/**
	 * Column that stores {@link #getStorageMapping()}.
	 * 
	 * @deprecated Use {@link TLPrimitiveColumns#STORAGE_MAPPING} instead
	 */
	public static final String STORAGE_MAPPING = TLPrimitiveColumns.STORAGE_MAPPING;

	/**
	 * Creates a {@link PersistentDatatype} for a given {@link KnowledgeObject}.
	 */
	public PersistentDatatype(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public StorageMapping<?> getStorageMapping() {
		return (StorageMapping<?>) tGetData(TLPrimitiveColumns.STORAGE_MAPPING);
	}

	@Override
	public void setStorageMapping(StorageMapping<?> value) {
		tSetData(TLPrimitiveColumns.STORAGE_MAPPING, value);
	}

	@Override
	public DBType getDBType() {
		try {
			return DBTypeFormat.INSTANCE.getValue(TLPrimitiveColumns.DB_TYPE_ATTR,
				tGetDataString(TLPrimitiveColumns.DB_TYPE_ATTR));
		} catch (ConfigurationException ex) {
			throw new WrapperRuntimeException(ex);
		}
	}

	@Override
	public void setDBType(DBType value) {
		tSetDataString(TLPrimitiveColumns.DB_TYPE_ATTR, DBTypeFormat.INSTANCE.getSpecification(value));
	}

	@Override
	public Integer getDBSize() {
		return tGetDataInteger(TLPrimitiveColumns.DB_SIZE_ATTR);
	}

	@Override
	public void setDBSize(Integer value) {
		tSetDataInteger(TLPrimitiveColumns.DB_SIZE_ATTR, value);
	}

	@Override
	public Integer getDBPrecision() {
		return tGetDataInteger(TLPrimitiveColumns.DB_PRECISION_ATTR);
	}

	@Override
	public void setDBPrecision(Integer value) {
		tSetDataInteger(TLPrimitiveColumns.DB_PRECISION_ATTR, value);
	}

	@Override
	public Boolean isBinary() {
		return tGetDataBoolean(TLPrimitiveColumns.BINARY_ATTR);
	}

	@Override
	public void setBinary(Boolean value) {
		tSetDataBoolean(TLPrimitiveColumns.BINARY_ATTR, value);
	}

	/**
	 * Lookup or create a {@link PersistentDatatype} with the given name.
	 */
	public static TLPrimitive mkDatatype(TLModuleBase module, String name, Kind kind, StorageMapping<?> mapping) {
		TLPrimitive existingType = getDatatype(module, name);
		if (existingType != null) {
			return existingType;
		}

		return createDatatype(module, module, name, kind, mapping);
	}

	/**
	 * Create a {@link PersistentDatatype} with the given name.
	 */
	public static TLPrimitive createDatatype(TLModule module, TLScope scope, String name, Kind kind,
			StorageMapping<?> mapping) {
		try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			PersistentDatatype newType =
				(PersistentDatatype) WrapperFactory.getWrapper(
					kb.createKnowledgeObject(TLPrimitiveColumns.OBJECT_TYPE, new NameValueBuffer()
						.put(SCOPE_REF, scope.tHandle())
						.put(MODULE_REF, module.tHandle())
						.put(NAME_ATTR, name)
						.put(TLPrimitiveColumns.KIND_ATTR, kind)
						.put(TLPrimitiveColumns.STORAGE_MAPPING, mapping)));
			return newType;
		} catch (DataObjectException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	/**
	 * Lookup {@link PersistentDatatype} with the given name.
	 * 
	 * @param module
	 *        The module to search in.
	 * @param name
	 *        The type name to find.
	 * @return The resolved {@link PersistentDatatype} or <code>null</code> if no such type is
	 *         found.
	 */
	public static TLPrimitive getDatatype(TLModuleBase module, String name) {
		return module.getDatatype(name);
	}

	@Override
	public Kind getKind() {
		return (Kind) tGetData(TLPrimitiveColumns.KIND_ATTR);
	}

}
