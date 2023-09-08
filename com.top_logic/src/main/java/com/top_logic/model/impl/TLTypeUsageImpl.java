/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLTypeUsage;

/**
 * Reference implementation of {@link TLTypeUsage}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLTypeUsageImpl implements TLTypeUsage {

	@Override
	public Collection<TLStructuredTypePart> getUsage(TLType targetType) {
		ArrayList<TLStructuredTypePart> result = new ArrayList<>();
		analyzeModel(result, targetType, targetType.getModel());
		return result;
	}

	private void analyzeModel(Collection<TLStructuredTypePart> result, TLType targetType, TLModel model) {
		for (TLModule module : model.getModules()) {
			analyzeScope(result, targetType, module);
		}
		for (TLModuleSingleton link : model.getQuery(TLModuleSingletons.class).getAllSingletons()) {
			TLObject singleton = link.getSingleton();
			if (singleton instanceof TLScope) {
				analyzeScope(result, targetType, (TLScope) singleton);
			}
		}
	}

	private void analyzeScope(Collection<TLStructuredTypePart> result, TLType targetType, TLScope scope) {
		for (TLClass clazz : scope.getClasses()) {
			analyzeStructuredType(result, targetType, clazz);
		}
		for (TLAssociation association : scope.getAssociations()) {
			analyzeStructuredType(result, targetType, association);
		}
	}

	private void analyzeStructuredType(Collection<TLStructuredTypePart> result, TLType targetType, TLStructuredType type) {
		for (TLStructuredTypePart part : type.getLocalParts()) {
			if (part.getType() == targetType) {
				result.add(part);
			}
		}
	}

}
