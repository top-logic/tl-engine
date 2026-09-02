/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

		Map<TLStructuredType, List<? extends TLStructuredTypePart>> attributesByType = new LinkedHashMap<>();

		TLStructuredType type = typeModel.getValue();
		if (type != null) {
			/* Only the local attributes of the edited type must be checked. Its inherited
			 * attributes are contributed by the generalizations below, since the generalizations of
			 * the persistent type may already have been removed from the selection being
			 * checked. */
			attributesByType.put(type, type.getLocalParts());
		}

		for (TLClass generalization : generalizations) {
			attributesByType.put(generalization, generalization.getAllParts());
		}

		ResKey problem = checkAttributes(attributesByType);
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
		Map<TLStructuredType, List<? extends TLStructuredTypePart>> attributesByType = new LinkedHashMap<>();
		for (TLClass clazz : classes) {
			attributesByType.put(clazz, clazz.getAllParts());
		}

		ResKey problem = checkAttributes(attributesByType);
		if (problem != null) {
			throw new TopLogicException(problem);
		}
	}

	/**
	 * Searches for attributes with the same name but different
	 * {@link TLStructuredTypePart#getDefinition() definitions}.
	 *
	 * @param attributesByType
	 *        The attributes to check by the type they were found in.
	 * @return The problem description of the first conflict found, or <code>null</code>, if there is
	 *         no conflicting attribute.
	 */
	private static ResKey checkAttributes(
			Map<TLStructuredType, List<? extends TLStructuredTypePart>> attributesByType) {
		Map<String, TLStructuredTypePart> attributeByName = new HashMap<>();
		Map<String, TLStructuredType> typeByName = new HashMap<>();

		for (Entry<TLStructuredType, List<? extends TLStructuredTypePart>> entry : attributesByType.entrySet()) {
			TLStructuredType type = entry.getKey();

			for (TLStructuredTypePart attribute : entry.getValue()) {
				String name = attribute.getName();

				TLStructuredTypePart clash = attributeByName.get(name);

				if (clash == null) {
					attributeByName.put(name, attribute);
					typeByName.put(name, type);
				} else {
					TLStructuredTypePart definition = attribute.getDefinition();
					TLStructuredTypePart clashDefinition = clash.getDefinition();

					if (!clashDefinition.equals(definition)) {
						return errorKey(name, typeByName.get(name), clashDefinition, type, definition);
					}
				}
			}
		}

		return null;
	}

	/**
	 * The problem description for an attribute that is declared in two conflicting ways.
	 *
	 * @param name
	 *        The name of the conflicting attribute.
	 * @param type1
	 *        The type the attribute was found in.
	 * @param definition1
	 *        The definition of the attribute found in {@code type1}.
	 * @param type2
	 *        The other type declaring an attribute with the same {@code name}.
	 * @param definition2
	 *        The definition of the attribute found in {@code type2}.
	 */
	private static ResKey errorKey(String name, TLStructuredType type1, TLTypePart definition1,
			TLStructuredType type2, TLTypePart definition2) {
		ResKey5 key = I18NConstants.ERROR_CONFLICTING_ATTRIBUTE__NAME_TYPE1_DEFINITION1_TYPE2_DEFINITION2;

		return key.fill(name, TLModelUtil.qualifiedName(type1), getOwnerName(definition1),
			TLModelUtil.qualifiedName(type2), getOwnerName(definition2));
	}

	private static String getOwnerName(TLTypePart part) {
		return TLModelUtil.qualifiedName(part.getOwner());
	}

}
