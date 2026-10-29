/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.listen.impl;

import static java.util.Objects.*;

import java.util.Map;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeItemUtil;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.mig.html.layout.AssociationEndRelevance;

/**
 * Settings for synthesizing model events.
 */
public final class ModelEventSettings {

	private final KnowledgeBase _kb;

	private final MetaObject _objectType;

	private final MetaObject _associationType;

	private final AssociationEndRelevance _relevanceByType;

	/**
	 * Creates a {@link ModelEventSettings}.
	 */
	public ModelEventSettings(KnowledgeBase kb, MetaObject objectType, MetaObject associationType,
			AssociationEndRelevance relevanceByType) {
		_kb = requireNonNull(kb);
		_objectType = requireNonNull(objectType);
		_associationType = requireNonNull(associationType);
		_relevanceByType = requireNonNull(relevanceByType);
	}

	/**
	 * Creates {@link ModelEventSettings} for the given {@link KnowledgeBase} with its
	 * {@link BasicTypes#getObjectType(MORepository) object table} and
	 * {@link BasicTypes#getKnowledgeAssociationType(MORepository) association table}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} that is the source of events.
	 * @param relevanceByType
	 *        Specification of association end relevance by type and reference attribute.
	 */
	public static ModelEventSettings forKnowledgeBase(KnowledgeBase kb, AssociationEndRelevance relevanceByType) {
		MORepository typeSystem = kb.getMORepository();
		return new ModelEventSettings(kb,
			BasicTypes.getObjectType(typeSystem),
			BasicTypes.getKnowledgeAssociationType(typeSystem),
			relevanceByType);
	}

	/**
	 * The {@link KnowledgeBase} that is the source of events.
	 */
	public KnowledgeBase kb() {
		return _kb;
	}

	/**
	 * The top-level object table.
	 */
	public MetaObject objectType() {
		return _objectType;
	}

	/**
	 * The top-level plain association table.
	 */
	public MetaObject associationType() {
		return _associationType;
	}

	/**
	 * The {@link AssociationEndRelevance} configuration.
	 */
	public AssociationEndRelevance relevanceByType() {
		return _relevanceByType;
	}

	/**
	 * Whether the given table stores plain associations.
	 */
	public boolean isAssociationType(MetaObject type) {
		return !KnowledgeItemUtil.isPureObjectType(type, _objectType, _associationType);
	}

	/**
	 * The event relevance of references stored in the given table.
	 */
	public Map<MOReference, Boolean> relevanceByReference(MetaObject linkType) {
		return _relevanceByType.relevanceByReference(linkType);
	}

}
