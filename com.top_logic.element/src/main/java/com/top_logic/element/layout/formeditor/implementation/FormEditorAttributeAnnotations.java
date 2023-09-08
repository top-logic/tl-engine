/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import java.util.function.Predicate;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.meta.AttributeAnnotationOptions;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.util.TLModelUtil;

/**
 * Option provider for attribute annotations based on the {@link TLTypeKind} of type part given by
 * the qualified name argument when using the form editor.
 * 
 * @see FieldDefinition
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class FormEditorAttributeAnnotations extends Function1<OptionModel<Class<?>>, String> {

	private DeclarativeFormOptions _options;

	/**
	 * Creates a {@link FormEditorAttributeAnnotations}.
	 */
	public FormEditorAttributeAnnotations(DeclarativeFormOptions options) {
		_options = options;
	}

	@Override
	public OptionModel<Class<?>> apply(String qualifiedName) {
		try {
			return new AttributeAnnotationOptions(_options, getTypePart(qualifiedName), classifierPredicate()).apply();
		} catch (ConfigurationException exception) {
			throw new RuntimeException(exception);
		}
	}

	private Predicate<Class<?>> classifierPredicate() {
		return TLModelUtil.classifierPredicate(new String[] { InAppClassifierConstants.FORM_RELEVANT }, false);
	}

	private TLTypePart getTypePart(String qualifiedName) throws ConfigurationException {
		return (TLTypePart) TLModelUtil.resolveQualifiedName(qualifiedName);
	}

}
