/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.fieldprovider;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.NumberOfRows;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.fieldprovider.format.FormattedFieldProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.search.annotate.TLNumberOfEditorRows;

/**
 * {@link FieldProvider} that adds editor configuration options, for instance the maximal displayed
 * lines, to the fields properties.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SearchExpressionFieldProvider extends FormattedFieldProvider<SearchExpressionFieldProvider.Config<?>> {

	/**
	 * Configuration options for {@link SearchExpressionFieldProvider}.
	 */
	public interface Config<I extends FormattedFieldProvider<?>> extends FormattedFieldProvider.Config<I> {
		// Nothing to do.
	}

	/**
	 * Creates a {@link SearchExpressionFieldProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SearchExpressionFieldProvider(InstantiationContext context, SearchExpressionFieldProvider.Config<?> config) {
		super(context, config);
	}

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		FormMember formField = super.createFormField(editContext, fieldName);
		
		TLNumberOfEditorRows annotation = getMultiLineAnnotation(editContext);
		
		if (annotation != null) {
			formField.set(NumberOfRows.NUMBER_OF_ROWS, new Pair<>(annotation.min(), annotation.max()));
		}
		
		return formField;
	}

	private TLNumberOfEditorRows getMultiLineAnnotation(EditContext editContext) {
		return editContext.getAnnotation(TLNumberOfEditorRows.class);
	}

}
