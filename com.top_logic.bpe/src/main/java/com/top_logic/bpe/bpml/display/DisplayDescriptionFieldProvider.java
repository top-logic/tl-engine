/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.fieldprovider.FormDefinitionFieldProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link FormDefinitionFieldProvider} for attributes of type {@link #DISPLAY_DESCRIPTION_TYPE}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayDescriptionFieldProvider extends FormDefinitionFieldProvider {

	private static final String DISPLAY_DESCRIPTION_TYPE = "tl.bpe.bpml:DisplayDescription";

	/**
	 * {@link TLAttributeAnnotation} to annotate the {@link Expr} to compute the type to create GUI
	 * for from the model.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("gui-type")
	@TargetType(value = TLTypeKind.CUSTOM, name = DISPLAY_DESCRIPTION_TYPE)
	public interface TypeComputation extends TLAttributeAnnotation {

		/**
		 * Expression to use to compute the type to create GUI for.
		 * <p>
		 * It is expected that the expression accepts one argument; the model of the component.
		 * </p>
		 */
		Expr getExpr();

	}

	/**
	 * {@link TLAttributeAnnotation} to annotate the {@link Function} to create
	 * {@link FormDefinitionTemplate}s.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("template-builder")
	@TargetType(value = TLTypeKind.CUSTOM, name = DISPLAY_DESCRIPTION_TYPE)
	public interface TemplateBuilder extends TLAttributeAnnotation {

		/**
		 * The returned function returns a {@link Supplier} for the {@link FormDefinitionTemplate}
		 * to offer the user as base templates.
		 */
		@InstanceFormat
		@Mandatory
		Function<Object, Supplier<? extends List<FormDefinitionTemplate>>> getFunction();
	}

	@Override
	protected Supplier<? extends List<FormDefinitionTemplate>> templateProvider(EditContext editContext) {
		TLObject attributed = editContext.getObject();
		if (attributed == null) {
			return () -> Collections.emptyList();
		}
		TemplateBuilder builderAnnotation = editContext.getAnnotation(TemplateBuilder.class);
		if (builderAnnotation != null) {
			return builderAnnotation.getFunction().apply(attributed);
		} else {
			return () -> Collections.emptyList();
		}
	}

	@Override
	protected TLStructuredType guiType(EditContext editContext) {
		TLObject attributed = editContext.getObject();
		if (attributed == null) {
			return null;
		}
		TypeComputation typeAnnotation = editContext.getAnnotation(TypeComputation.class);
		if (typeAnnotation == null) {
			throw new TopLogicException(
				I18NConstants.MISSING_TYPE_COMPUTATION__ATTRIBUTE.fill(editContext.getDescriptionKey()));
		}
		return (TLStructuredType) QueryExecutor.compile(typeAnnotation.getExpr()).execute(attributed);
	}

}

