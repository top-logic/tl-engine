/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.exporter;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;

/**
 * {@link InstanceResolver} that identifies an object by a single attribute that is mapped to a DB
 * column.
 */
public final class ResolverByColumnAttribute implements InstanceResolver {

	private final TLStructuredTypePart _idAttribute;

	private final TLPrimitive _idValueType;

	private final String _tableName;

	private final String _idColumn;

	/** 
	 * Creates a {@link ResolverByColumnAttribute}.
	 */
	public ResolverByColumnAttribute(TLStructuredTypePart idAttribute, TLPrimitive idType, String tableName,
			String idColumn) {
		_idAttribute = idAttribute;
		_idValueType = idType;
		_tableName = tableName;
		_idColumn = idColumn;
	}

	@Override
	public TLObject resolve(I18NLog log, Object context, String kind, String id) {
		Object value = XMLInstanceImporter.parse(log, _idValueType, id);
		KnowledgeObject item = (KnowledgeObject) PersistencyLayer.getKnowledgeBase()
			.getObjectByAttribute(_tableName, _idColumn, value);
		if (item == null) {
			return null;
		}
		return item.getWrapper();
	}

	@Override
	public String buildId(TLObject obj) {
		Object id = obj.tValue(_idAttribute);
		return XMLInstanceExporter.serialize(_idValueType, id);
	}
}