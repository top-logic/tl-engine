/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.exception.NotYetImplementedException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.model.search.ui.model.options.structure.RightHandSideOptions;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TLTypeVisitor} that calculates the {@link InstanceOptions}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class InstanceOptionsVisitor implements TLTypeVisitor<List<? extends Object>, Void> {

	/** Returns the {@link InstanceOptionsVisitor} instance. */
	public static final InstanceOptionsVisitor INSTANCE = new InstanceOptionsVisitor();

	@Override
	public List<? extends Object> visitPrimitive(TLPrimitive primitive, Void arg) {
		throw new UnsupportedOperationException("Cannot create a list of all instances of a primitive type."
			+ " Use " + RightHandSideOptions.class + " instead.");
	}

	@Override
	public List<TLClassifier> visitEnumeration(TLEnumeration enumeration, Void arg) {
		return enumeration.getClassifiers();
	}

	@Override
	public List<? extends Object> visitClass(TLClass tlClass, Void arg) {
		return getInstances(tlClass);
	}

	private List<Wrapper> getInstances(TLClass requestedType) {
		Set<String> tableNames = TLModelUtil.potentialTables(requestedType, false).keySet();
		List<KnowledgeObject> tableContents = new ArrayList<>();
		for (String tableName : tableNames) {
			tableContents.addAll(PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(tableName));
		}
		/* The tables can contain objects of different types. Therefore, the objects
		 * have to be filtered by the requested type. */
		List<Wrapper> result = new ArrayList<>();
		for (Wrapper wrapper : resolveToWrappers(tableContents)) {
			TLType actualType = wrapper.tType();
			if (TLModelUtil.isCompatibleType(requestedType, actualType)) {
				result.add(wrapper);
			}
		}
		return result;
	}

	private List<Wrapper> resolveToWrappers(List<? extends KnowledgeObject> knowledgeObjects) {
		List<Wrapper> result = new ArrayList<>();
		for (KnowledgeObject knowledgeObject : knowledgeObjects) {
			result.add(WrapperFactory.getWrapper(knowledgeObject));
		}
		return result;
	}

	@Override
	public List<? extends Object> visitAssociation(TLAssociation association, Void arg) {
		throw new NotYetImplementedException("Searching for TLAssociations is not yet implemeted.");
	}

}
