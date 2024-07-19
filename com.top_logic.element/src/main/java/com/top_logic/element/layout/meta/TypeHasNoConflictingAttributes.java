/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.constraint.algorithm.GenericValueDependency;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ValueConstraint} that checks that a {@link TLStructuredType types} generalizations and
 * specializations do not have attributes with the same name and different definitions.
 *
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven Förster</a>
 */
public class TypeHasNoConflictingAttributes extends GenericValueDependency<List<TLClass>, TLStructuredType> {

	/**
	 * Creates a {@link TypeHasNoConflictingAttributes}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TypeHasNoConflictingAttributes() {
		super((Class) List.class, TLStructuredType.class);
	}

	@Override
	protected void checkValue(PropertyModel<List<TLClass>> typesModel, PropertyModel<TLStructuredType> typeModel) {
		Set<TLClass> classes = new HashSet<>();

		TLStructuredType type = typeModel.getValue();
		if (type instanceof TLClass) {
			classes.add((TLClass) type);
		}

		List<TLClass> types = typesModel.getValue();
		if (!CollectionUtilShared.isEmptyOrNull(types)) {
			classes.addAll(types);
		}

		if (classes.size() > 1) {
			try {
				checkClasses(classes);
			} catch (TopLogicException exception) {
				typesModel.setProblemDescription(exception.getErrorKey());
			}
		}
	}
	
	/**
	 * Checks if the given {@link TLClass classes} have attributes with the same name but different
	 * definitions.
	 * 
	 * <p>
	 * If a conflicting attribute is found then a {@link TopLogicException} is thrown.
	 * </p>
	 */
	public static void checkClasses(Collection<TLClass> classes) {
		Map<String, TLStructuredTypePart> attributeByName = new HashMap<>();
		
		for (TLClass clazz : classes) {
			List<? extends TLStructuredTypePart> attributes = clazz.getAllParts();

			for (TLStructuredTypePart attribute1 : attributes) {
				String name = attribute1.getName();

				TLStructuredTypePart attribute2 = attributeByName.get(name);

				if (attribute2 == null) {
					attributeByName.put(name, attribute1);
				} else {
					TLStructuredTypePart definition1 = attribute1.getDefinition();
					TLStructuredTypePart definition2 = attribute2.getDefinition();

					if (!definition2.equals(definition1)) {
						throw new TopLogicException(errorKey(name, attribute1, definition1, attribute2, definition2));
					}
				}
			}
		}
	}

	private static ResKey errorKey(String name, TLTypePart attribute1, TLTypePart definition1, TLTypePart attribute2, TLTypePart definition2) {
		ResKey5 key = I18NConstants.ERROR_CONFLICTING_ATTRIBUTE__NAME_TYPE1_DEFINITION1_TYPE2_DEFINITION2;

		String type1Name = getOwnerName(attribute1);
		String definition1Name = getOwnerName(definition1);
		String type2Name = getOwnerName(attribute2);
		String definition2Name = getOwnerName(definition2);

		return key.fill(name, type1Name, definition1Name, type2Name, definition2Name);
	}

	private static String getOwnerName(TLTypePart attribute1) {
		return TLModelUtil.qualifiedName(attribute1.getOwner());
	}

}
