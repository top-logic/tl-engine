/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Collections;
import java.util.Map.Entry;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;

/**
 * Implementation of convenience API of {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractKnowledgeBase implements KnowledgeBase {

	private static final Iterable<Entry<String, Object>> NO_VALUES = Collections.<String, Object> emptyMap().entrySet();

	@Override
	public final <T extends TLObject> T createObject(String typeName, Class<T> expectedType) throws DataObjectException {
		return createObject(typeName, NO_VALUES, expectedType);
	}

	@Override
	public final <T extends TLObject> T createObject(String typeName, Iterable<Entry<String, Object>> initialValues,
			Class<T> expectedType) throws DataObjectException {
		return createObject(contextBranch(), typeName, initialValues, expectedType);
	}

	@Override
	public final <T extends TLObject> T createObject(Branch branch, String typeName,
			Class<T> expectedType) throws DataObjectException {
		return createObject(branch, typeName, NO_VALUES, expectedType);
	}

	@Override
	public final <T extends TLObject> T createObject(Branch branch, String typeName, Iterable<Entry<String, Object>> initialValues,
			Class<T> expectedType) throws DataObjectException {
		return expectedType.cast(createObject(branch, typeName, initialValues));
	}

	@Override
	public final TLObject createObject(Branch branch, String typeName, Iterable<Entry<String, Object>> initialValues)
			throws DataObjectException {
		return createObject(branch, null, typeName, initialValues);
	}

	@Override
	public final TLObject createObject(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeItem(branch, objectName, typeName, initialValues).getWrapper();
	}

	@Deprecated
	@Override
	public final KnowledgeObject createKnowledgeObject(TLID customObjectName, String aTypeName)
			throws DataObjectException {
		return createKnowledgeObject(customObjectName, aTypeName, NO_VALUES);
	}

	@Deprecated
	@Override
	public final KnowledgeObject createKnowledgeObject(TLID customObjectName, String aTypeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeObject(contextBranch(), customObjectName, aTypeName, initialValues);
	}

	@Deprecated
	@Override
	public final KnowledgeObject createKnowledgeObject(Branch branch, TLID customObjectName, String aTypeName)
			throws DataObjectException {
		return createKnowledgeObject(branch, customObjectName, aTypeName, NO_VALUES);
	}

	@Override
	public final KnowledgeObject createKnowledgeObject(String aTypeName) throws DataObjectException {
		return createKnowledgeObject(contextBranch(), aTypeName);
	}

	@Override
	public final KnowledgeObject createKnowledgeObject(Branch branch, String aTypeName) throws DataObjectException {
		return createKnowledgeObject(branch, aTypeName, NO_VALUES);
	}

	@Override
	public final KnowledgeObject createKnowledgeObject(String aTypeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeObject(contextBranch(), aTypeName, initialValues);
	}

	@Deprecated
	@Override
	public final KnowledgeObject createKnowledgeObject(Branch branch, String aTypeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeItem(branch, null, aTypeName, initialValues, KnowledgeObject.class);
	}

	@Deprecated
	@Override
	public final KnowledgeObject createKnowledgeObject(Branch branch, TLID customObjectName, String aTypeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeItem(branch, customObjectName, aTypeName, initialValues, KnowledgeObject.class);
	}

	@Override
	public final KnowledgeAssociation createAssociation(KnowledgeItem src, KnowledgeItem dest, String typeName)
			throws DataObjectException {
		return createAssociation(contextBranch(), src, dest, typeName);
	}

	@Override
	public final KnowledgeAssociation createAssociation(Branch branch, KnowledgeItem src, KnowledgeItem dest,
			String typeName) throws DataObjectException {
		return createAssociation(branch, null, src, dest, typeName);
	}

	@Deprecated
	@Override
	public final KnowledgeAssociation createAssociation(TLID objectName, KnowledgeItem srcKO, KnowledgeItem destKO,
			String typeName) throws DataObjectException {
		return createAssociation(contextBranch(), objectName, srcKO, destKO, typeName);
	}

	@Override
	public final <T extends KnowledgeItem> T createKnowledgeItem(String typeName, Class<T> expectedType)
			throws DataObjectException {
		return createKnowledgeItem(typeName, NO_VALUES, expectedType);
	}

	@Override
	public final <T extends KnowledgeItem> T createKnowledgeItem(String typeName,
			Iterable<Entry<String, Object>> initialValues, Class<T> expectedType) throws DataObjectException {
		return createKnowledgeItem(contextBranch(), typeName, initialValues, expectedType);
	}

	@Override
	public final <T extends KnowledgeItem> T createKnowledgeItem(Branch branch, String typeName, Class<T> expectedType)
			throws DataObjectException {
		return createKnowledgeItem(branch, typeName, NO_VALUES, expectedType);
	}

	@Override
	public final KnowledgeItem createKnowledgeItem(Branch branch, String typeName) throws DataObjectException {
		return createKnowledgeItem(branch, typeName, NO_VALUES);
	}

	@Override
	public final KnowledgeItem createKnowledgeItem(Branch branch, String typeName,
			Iterable<Entry<String, Object>> initialValues) throws DataObjectException {
		return createKnowledgeItem(branch, null, typeName, initialValues);
	}

	@Override
	public final <T extends KnowledgeItem> T createKnowledgeItem(Branch branch, String typeName,
			Iterable<Entry<String, Object>> initialValues, Class<T> expectedType) throws DataObjectException {
		return createKnowledgeItem(branch, null, typeName, initialValues, expectedType);
	}

	@Override
	public final <T extends KnowledgeItem> T createKnowledgeItem(Branch branch, TLID objectName, String typeName,
			Iterable<Entry<String, Object>> initialValues, Class<T> expectedType) throws DataObjectException {
		return expectedType.cast(createKnowledgeItem(branch, objectName, typeName, initialValues));
	}

	private Branch contextBranch() {
		return getHistoryManager().getContextBranch();
	}

}
