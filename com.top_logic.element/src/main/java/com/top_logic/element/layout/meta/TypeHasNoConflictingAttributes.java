/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * {@link ValueConstraint} that checks that the {@link TLClass generalizations} selected for a
 * {@link TLStructuredType type} do not introduce attributes with the same name but different
 * definitions.
 *
 * <p>
 * The constraint must not be used for the specializations of a type: Two specializations of the
 * same type are unrelated to each other, therefore both may declare an attribute with the same
 * name.
 * </p>
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
	protected void checkValue(PropertyModel<List<TLClass>> generalizationsModel,
			PropertyModel<TLStructuredType> typeModel) {
		List<TLClass> generalizations = generalizationsModel.getValue();
		if (CollectionUtilShared.isEmptyOrNull(generalizations)) {
			return;
		}

		List<List<? extends TLStructuredTypePart>> attributeGroups = new ArrayList<>();

		TLStructuredType type = typeModel.getValue();
		if (type != null) {
			/* Only the local attributes of the edited type must be checked. Its inherited
			 * attributes are contributed by the generalizations below, since the generalizations of
			 * the persistent type may already have been removed from the selection being
			 * checked. */
			attributeGroups.add(type.getLocalParts());
		}

		for (TLClass generalization : generalizations) {
			attributeGroups.add(generalization.getAllParts());
		}

		ResKey problem = checkAttributes(attributeGroups);
		if (problem != null) {
			generalizationsModel.setProblemDescription(problem);
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
		List<List<? extends TLStructuredTypePart>> attributeGroups = new ArrayList<>();
		for (TLClass clazz : classes) {
			attributeGroups.add(clazz.getAllParts());
		}

		ResKey problem = checkAttributes(attributeGroups);
		if (problem != null) {
			throw new TopLogicException(problem);
		}
	}

	/**
	 * Searches for attributes with the same name but different
	 * {@link TLStructuredTypePart#getDefinition() definitions}.
	 *
	 * @param attributeGroups
	 *        The attributes to check, each entry holding the attributes of a single type.
	 * @return The problem description of the first conflict found, or <code>null</code>, if there is
	 *         no conflicting attribute.
	 */
	private static ResKey checkAttributes(List<List<? extends TLStructuredTypePart>> attributeGroups) {
		Map<String, TLStructuredTypePart> attributeByName = new HashMap<>();

		for (List<? extends TLStructuredTypePart> attributes : attributeGroups) {
			for (TLStructuredTypePart attribute1 : attributes) {
				String name = attribute1.getName();

				TLStructuredTypePart attribute2 = attributeByName.get(name);

				if (attribute2 == null) {
					attributeByName.put(name, attribute1);
				} else {
					TLStructuredTypePart definition1 = attribute1.getDefinition();
					TLStructuredTypePart definition2 = attribute2.getDefinition();

					if (!definition2.equals(definition1)) {
						return errorKey(name, attribute1, definition1, attribute2, definition2);
					}
				}
			}
		}

		return null;
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
