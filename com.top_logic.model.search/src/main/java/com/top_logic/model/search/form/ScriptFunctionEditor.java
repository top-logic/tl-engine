/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.SimpleProxyConstraint;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * {@link PlainEditor} to create configurations based on {@link Expr}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ScriptFunctionEditor extends PlainEditor {

	private static final Constraint SCRIPT_FUNCTION_CONSTRAINT =
		new SimpleProxyConstraint(ModelReferenceChecker.INSTANCE) {

			@Override
			public boolean check(Object value) throws CheckException {
				if (value != null) {
					value = ((WithExpression) value).getExpression();
				}
				return super.check(value);
			}
		};

	@Override
	protected void init(EditorFactory editorFactory, ValueModel model, FormField field,
			Mapping<Object, Object> uiConversion, Mapping<Object, Object> storageConversion) {
		super.init(editorFactory, model, field, uiConversion, storageConversion);

		field.addConstraint(SCRIPT_FUNCTION_CONSTRAINT);

	}

	@Override
	protected ConfigurationValueProvider<?> getValueProvider(PropertyDescriptor property) {
		return new AbstractConfigurationValueProvider<>(scriptFunctionType()) {
			@Override
			protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				Expr expr = ExprFormat.INSTANCE.getValue(propertyName, propertyValue);
				WithExpression newConfigItem = TypedConfiguration.newConfigItem(scriptFunctionType());
				newConfigItem.setExpression(expr);
				return newConfigItem;
			}

			@Override
			protected String getSpecificationNonNull(Object configValue) {
				WithExpression functionConfig = (WithExpression) configValue;
				return ExprFormat.INSTANCE.getSpecification(functionConfig.getExpression());
			}
		};
	}

	/**
	 * The concrete {@link WithExpression} configuration to instantiate.
	 */
	protected abstract Class<? extends WithExpression> scriptFunctionType();

}
