/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.basic.sql.DBType;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link TLPrimitive} type implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLPrimitiveImpl extends AbstractTLType implements TLPrimitive {

	private final Kind kind;

	private StorageMapping<?> _storageMapping;

	private DBType _dbType;

	private Integer _size;

	private Integer _precision;

	private Boolean _binary;

	/* package protected */TLPrimitiveImpl(TLModelImpl model, String name, Kind kind, StorageMapping<?> mapping) {
		super(model, name);

		this.kind = kind;
		_storageMapping = mapping;
	}

	@Override
	public Kind getKind() {
		return kind;
	}
	
	@Override
	public StorageMapping<?> getStorageMapping() {
		return _storageMapping;
	}

	@Override
	public void setStorageMapping(StorageMapping<?> value) {
		_storageMapping = value;
	}

	@Override
	public DBType getDBType() {
		return _dbType;
	}

	@Override
	public void setDBType(DBType value) {
		_dbType = value;
	}

	@Override
	public Integer getDBSize() {
		return _size;
	}

	@Override
	public void setDBSize(Integer value) {
		_size = value;
	}

	@Override
	public Integer getDBPrecision() {
		return _precision;
	}

	@Override
	public void setDBPrecision(Integer value) {
		_precision = value;
	}

	@Override
	public Boolean isBinary() {
		return _binary;
	}

	@Override
	public void setBinary(Boolean value) {
		_binary = value;
	}

}
