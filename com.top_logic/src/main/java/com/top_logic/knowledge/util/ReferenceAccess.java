/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.util;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.model.TLObject;

/**
 * {@link Mapping} of base object to the value of a reference attribute.
 * 
 * @param <T>
 *        The type of the base object.
 * @param <R>
 *        The target type of the reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ReferenceAccess<T extends TLObject, R extends TLObject> implements Mapping<T, R> {

	/**
	 * Access to the destination reference of {@link KnowledgeAssociation}s.
	 */
	public static <L extends KnowledgeAssociation, T extends TLObject> ReferenceAccess<L, T> outgoingAccess(
			Class<T> targetType) {
		return linkAccess(DBKnowledgeAssociation.REFERENCE_DEST_NAME, targetType);
	}

	/**
	 * Access to the source reference of {@link KnowledgeAssociation}s.
	 */
	public static <L extends KnowledgeAssociation, T extends TLObject> ReferenceAccess<L, T> incomingAccess(
			Class<T> targetType) {
		return linkAccess(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, targetType);
	}

	/**
	 * Access to an arbitrary reference attribute in any type.
	 * 
	 * @param referenceName
	 *        The name of the reference attribute to access.
	 * @param targetType
	 *        The expected target type of the reference.
	 */
	public static <L extends TLObject, T extends TLObject> ReferenceAccess<L, T> linkAccess(String referenceName,
			Class<T> targetType) {
		return new ReferenceAccess<>(referenceName, targetType);
	}

	private final String _referenceName;

	private final Class<R> _targetType;

	private ReferenceAccess(String referenceName, Class<R> targetType) {
		_referenceName = referenceName;
		_targetType = targetType;
	}

	@Override
	public R map(T input) {
		try {
			KnowledgeItem value = (KnowledgeItem) input.tHandle().getAttributeValue(_referenceName);
			return value == null ? null : _targetType.cast(value.getWrapper());
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
}