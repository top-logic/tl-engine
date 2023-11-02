/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.util.model.ModelService;

/**
 * {@link TLTypeContext} that represents a mix of multiple other contexts.
 */
public class SumTypeContext implements TLTypeContext {

	/**
	 * Creates a new {@link TLTypeContext} from other {@link TLTypeContext}s displayed at the same
	 * location.
	 */
	public static TLTypeContext joinContexts(TLTypeContext self, TLTypeContext other) {
		Set<TLType> types = self.getConcreteTypes();
		Set<TLType> otherTypes = other.getConcreteTypes();
		if (types.equals(otherTypes)) {
			return self;
		}
		boolean multiple = self.isMultiple() || other.isMultiple();
		boolean mandatory = self.isMandatory() && other.isMandatory();
		boolean composite = self.isCompositionContext() && other.isCompositionContext();
		return SumTypeContext.createSumTypeContext(self, types, otherTypes, multiple, mandatory, composite);
	}

	/**
	 * Creates a new {@link TLTypeContext} from concrete types of other {@link TLTypeContext}s
	 * displayed at the same location.
	 */
	private static SumTypeContext createSumTypeContext(TLTypeContext lead, Set<TLType> types, Set<TLType> otherTypes,
			boolean multiple, boolean mandatory, boolean composite) {
		Set<TLType> allTypes = new HashSet<>();
		allTypes.addAll(types);
		allTypes.addAll(otherTypes);
	
		TLType commonSuperType = commonSuperType(allTypes);
	
		return new SumTypeContext(lead, commonSuperType, allTypes, multiple, mandatory, composite);
	}

	private final Set<TLType> _concreteTypes;

	private final TLTypeContext _lead;

	private final TLType _commonType;

	private final boolean _multiple;

	private final boolean _mandatory;

	private final boolean _composite;
	/**
	 * Creates a {@link SumTypeContext}.
	 */
	private SumTypeContext(TLTypeContext lead, TLType commonType, Set<TLType> concreteTypes, boolean multiple,
			boolean mandatory, boolean composite) {
		_lead = lead;
		_commonType = commonType;
		_concreteTypes = concreteTypes;
		_multiple = multiple;
		_mandatory = mandatory;
		_composite = composite;
	}

	private static TLType commonSuperType(Set<TLType> allTypes) {
		List<TLClass> allClasses = new ArrayList<>();
		for (TLType type : allTypes) {
			if (type instanceof TLClass) {
				allClasses.add((TLClass) type);
			} else {
				return objectType();
			}
		}

		Set<TLClass> generalizations = TLModelUtil.getCommonGeneralizations(allClasses);
		if (generalizations.isEmpty()) {
			return objectType();
		}
		return generalizations.iterator().next();
	}

	private static TLType objectType() {
		return TLModelUtil.tlObjectType(ModelService.getApplicationModel());
	}

	@Override
	public Set<TLType> getConcreteTypes() {
		return _concreteTypes;
	}

	@Override
	public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
		return _lead.getAnnotation(annotationType);
	}

	@Override
	public TLType getType() {
		return _commonType;
	}

	@Override
	public boolean isMultiple() {
		return _multiple;
	}

	@Override
	public boolean isMandatory() {
		return _mandatory;
	}

	@Override
	public boolean isCompositionContext() {
		return _composite;
	}

	@Override
	public TLTypePart getTypePart() {
		return null;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("lead", _lead)
			.add("common-type", _commonType)
			.add("concrete-types", _concreteTypes)
			.build();
	}

}
