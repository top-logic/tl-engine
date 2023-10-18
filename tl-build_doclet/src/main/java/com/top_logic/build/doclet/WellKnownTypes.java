/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.doclet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Well known types, methods and classes.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WellKnownTypes {

	private static final String LABEL_ANNOTATION = "com.top_logic.basic.config.annotation.Label";

	private static final String ABSTRACT_ANNOTATION = "com.top_logic.basic.config.annotation.Abstract";

	private static final String NAME_ANNOTATION = "com.top_logic.basic.config.annotation.Name";

	private static final String IN_APP_ANNOTATION = "com.top_logic.basic.annotation.InApp";

	private static final String TEMPLATE_VARIABLE_ANNOTATION = "com.top_logic.layout.basic.TemplateVariable";

	TypeMirror _configType;

	TypeMirror _errorType;

	TypeMirror _exceptionType;

	TypeMirror _polymorphicConfigurationType;

	TypeMirror _i18nConstantsType;

	TypeMirror _themeConstantsType;

	TypeMirror _withPropertiesType;

	List<TypeMirror> _reskeyTypes;

	TypeElement _objectType;

	TypeMirror _overridesAnnotation;

	TypeMirror _deprecatedAnnotation;

	TypeMirror _nameAnnotation;

	ExecutableElement _nameValue;

	TypeMirror _abstractAnnotation;

	TypeMirror _labelAnnotation;

	ExecutableElement _labelValue;

	TypeMirror _templateVariableAnnotation;

	ExecutableElement _templateVariableName;

	TypeMirror _instantiationContext;

	private final Elements _elements;

	private final Types _types;

	TypeMirror _inAppAnnotation;

	/**
	 * Creates a new {@link WellKnownTypes}.
	 */
	public WellKnownTypes(Elements elementUtils, Types types) {
		_elements = elementUtils;
		_types = types;
		_objectType = typeElement("java.lang.Object");
		_errorType = typeMirror("java.lang.Error");
		_exceptionType = typeMirror("java.lang.Exception");
		_overridesAnnotation = typeMirror("java.lang.Override");
		_deprecatedAnnotation = typeMirror("java.lang.Deprecated");
		_configType = typeMirror("com.top_logic.basic.config.ConfigurationItem");
		_polymorphicConfigurationType = typeMirror("com.top_logic.basic.config.PolymorphicConfiguration");
		_i18nConstantsType = typeMirror("com.top_logic.basic.i18n.I18NConstantsBase");
		_themeConstantsType = typeMirror("com.top_logic.layout.basic.IconsBase");
		_withPropertiesType = typeMirror("com.top_logic.layout.template.WithProperties");
		_reskeyTypes = Arrays.asList(
			typeMirror("com.top_logic.basic.util.ResKey"),
			typeMirror("com.top_logic.basic.util.ResKey1"),
			typeMirror("com.top_logic.basic.util.ResKey2"),
			typeMirror("com.top_logic.basic.util.ResKey3"),
			typeMirror("com.top_logic.basic.util.ResKey4"),
			typeMirror("com.top_logic.basic.util.ResKey5"),
			typeMirror("com.top_logic.basic.util.ResKeyN"));
		_instantiationContext = typeMirror("com.top_logic.basic.config.InstantiationContext");
		_inAppAnnotation = typeMirror(IN_APP_ANNOTATION);
		_abstractAnnotation = typeMirror(ABSTRACT_ANNOTATION);
		TypeElement name = typeElement(NAME_ANNOTATION);
		if (name != null) {
			_nameAnnotation = name.asType();
			_nameValue = methodByName(name, "value");
		} else {
			// May happen during tests.
		}
		TypeElement label = typeElement(LABEL_ANNOTATION);
		if (label != null) {
			_labelAnnotation = label.asType();
			_labelValue = methodByName(label, "value");
		} else {
			// May happen during tests.
		}
		TypeElement templateVariable = typeElement(TEMPLATE_VARIABLE_ANNOTATION);
		if (templateVariable != null) {
			_templateVariableAnnotation = templateVariable.asType();
			_templateVariableName = methodByName(templateVariable, "value");
		} else {
			// May happen during tests.
		}
	}

	private ExecutableElement methodByName(Element type, String methodName) {
		return type.getEnclosedElements().stream()
			.filter(elem -> elem.getKind() == ElementKind.METHOD)
			.filter(elem -> elem.getSimpleName().contentEquals(methodName))
			.map(ExecutableElement.class::cast)
			.findFirst().get();
	}

	private TypeMirror typeMirror(String className) {
		TypeElement typeElement = typeElement(className);
		if (typeElement == null) {
			return null;
		}
		return typeElement.asType();
	}

	private TypeElement typeElement(String className) {
		return _elements.getTypeElement(className);
	}

	/**
	 * Determines the {@value #TEMPLATE_VARIABLE_ANNOTATION} annotation.
	 */
	public Optional<String> getTemplateVariableName(Element element) {
		AnnotationMirror annotation = getAnnotation(element, _templateVariableAnnotation);
		if (annotation != null) {
			return Optional.ofNullable((String) annotation.getElementValues().get(_templateVariableName).getValue());
		}

		return Optional.empty();
	}

	/**
	 * Determines the {@value #LABEL_ANNOTATION} annotation.
	 */
	public Optional<String> getAnnotatedLabel(Element element) {
		AnnotationMirror annotation = getAnnotation(element, _labelAnnotation);
		if (annotation != null) {
			return Optional.ofNullable((String) annotation.getElementValues().get(_labelValue).getValue());
		}

		return Optional.empty();
	}

	/**
	 * Determines the {@value #NAME_ANNOTATION} annotation.
	 */
	public Optional<String> getAnnotatedName(Element element) {
		AnnotationMirror annotation = getAnnotation(element, _nameAnnotation);
		if (annotation != null) {
			return Optional.ofNullable((String) annotation.getElementValues().get(_nameValue).getValue());
		}

		return Optional.empty();
	}

	/**
	 * Whether the {@link Override} annotation is present.
	 */
	public boolean hasOverrideAnnotation(ExecutableElement methodDoc) {
		return hasAnnotation(methodDoc, _overridesAnnotation);
	}

	/**
	 * Whether the {@link Deprecated} annotation is present.
	 */
	public boolean hasDeprecatedAnnotation(Element methodDoc) {
		return hasAnnotation(methodDoc, _deprecatedAnnotation);
	}

	/**
	 * Whether the {@value #IN_APP_ANNOTATION} annotation is present.
	 */
	public boolean hasInAppAnnotation(Element elem) {
		return hasAnnotation(elem, _inAppAnnotation);
	}

	/**
	 * Whether the {@value #ABSTRACT_ANNOTATION} annotation is present.
	 */
	public boolean hasAbstractAnnotation(Element elem) {
		return hasAnnotation(elem, _abstractAnnotation);
	}

	private boolean hasAnnotation(Element doc, TypeMirror requiredAnnotation) {
		return getAnnotation(doc, requiredAnnotation) != null;
	}

	private AnnotationMirror getAnnotation(Element doc, TypeMirror requiredAnnotation) {
		if (requiredAnnotation == null) {
			return null;
		}
		for (AnnotationMirror annotation : doc.getAnnotationMirrors()) {
			if (_types.isSameType(annotation.getAnnotationType(), requiredAnnotation)) {
				return annotation;
			}
		}

		return null;
	}

}

