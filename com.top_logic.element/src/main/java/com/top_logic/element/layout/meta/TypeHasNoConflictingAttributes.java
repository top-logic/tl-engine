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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
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
		Map<String, TLStructuredTypePart> definitionByPartName = new HashMap<>();
		
		for (TLClass clazz : classes) {
			List<? extends TLStructuredTypePart> attributes = clazz.getAllParts();

			for (TLStructuredTypePart attribute : attributes) {
				TLStructuredTypePart attributeDefinition = attribute.getDefinition();
				String attributeName = attribute.getName();

				TLStructuredTypePart definition = definitionByPartName.get(attributeName);

				if (definition == null) {
					definitionByPartName.put(attributeName, attributeDefinition);
				} else {
					if (!definition.equals(attributeDefinition)) {
						throw new TopLogicException(errorKey(attributeName, definition.getOwner(), attributeDefinition.getOwner()));
					}
				}
			}
		}
	}

	private static ResKey errorKey(String attributeName, TLType type1, TLType type2) {
		String type1Name = TLModelUtil.qualifiedName(type1);
		String type2Name = TLModelUtil.qualifiedName(type2);

		return I18NConstants.ERROR_CONFLICTING_ATTRIBUTE__NAME_TYPE1_TYPE2.fill(attributeName, type1Name, type2Name);
	}

}
