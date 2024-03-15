/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.builder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.element.layout.formeditor.definition.OtherAttributes;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * A utility class for {@link FormDefinition}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class FormDefinitionUtil {

	static FormDefinition createAllPartsFormDefinition() {
		FormDefinition definition = TypedConfiguration.newConfigItem(FormDefinition.class);
		definition.getContent().add(TypedConfiguration.newConfigItem(OtherAttributes.class));
		return definition;
	}

	/**
	 * Computes the {@link TLFormDefinition} for the given {@link TLStructuredType}.
	 */
	public static TLFormDefinition getFormAnnotation(TLStructuredType structuredType) {
		return structuredType.getAnnotation(TLFormDefinition.class);
	}

	/**
	 * All {@link FormDefinitionTemplate}s annotated to parents of the given type.
	 */
	public static List<FormDefinitionTemplate> getInheritedFormDefinitionTemplates(TLStructuredType type) {
		if (type instanceof TLClass) {
			return TLModelUtil.getReflexiveTransitiveGeneralizations((TLClass) type)
				.stream()
				.map(FormDefinitionUtil::getFormDefinitionTemplate)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	private static FormDefinitionTemplate getFormDefinitionTemplate(TLClass clazz) {
		TLFormDefinition formAnnotation = getFormAnnotation(clazz);
		if (formAnnotation == null) {
			return null;
		}

		FormDefinition form = formAnnotation.getForm();
		if (form == null) {
			return null;
		}
		return new FormDefinitionTemplate(MetaLabelProvider.INSTANCE.getLabel(clazz), form);
	}

	/**
	 * Loads all forms of the configuration and resolves the name of the type to a {@link TLType}.
	 * 
	 * <p>
	 * If a name of a type cannot be resolved, the associated form will not be added to the result
	 * map and an error is logged.
	 * </p>
	 */
	public static Map<TLType, FormDefinition> createTypedFormMapping(Map<TLModelPartRef, TypedFormDefinition> forms) {
		Map<TLType, FormDefinition> typedForms = new LinkedHashMap<>();
		for (Entry<TLModelPartRef, TypedFormDefinition> entry : forms.entrySet()) {
			TLType type = entry.getKey().resolveType();

			typedForms.put(type, entry.getValue().getFormDefinition());
		}

		return typedForms;
	}

}

