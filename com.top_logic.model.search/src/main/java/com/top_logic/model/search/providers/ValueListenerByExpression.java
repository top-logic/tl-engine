/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ValueListener} that executes a TL-Script expression.
 */
@InApp
@Label("Value listener with TL-Script")
public class ValueListenerByExpression extends AbstractConfiguredInstance<ValueListenerByExpression.Config>
		implements ValueListener {

	/**
	 * Typed configuration interface definition for {@link ValueListenerByExpression}.
	 */
	@TagName("script")
	public interface Config extends PolymorphicConfiguration<ValueListenerByExpression> {

		/**
		 * TL-Script that is executed when the value of the field for the annotated attribute
		 * changes.
		 * 
		 * <p>
		 * The operation gets four arguments: the object, the new field value, the old field value,
		 * and the annotated attribute.
		 * </p>
		 */
		@Mandatory
		Expr getOperation();

	}

	private final QueryExecutor _operation;

	/**
	 * Create a {@link ValueListenerByExpression}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ValueListenerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_operation = QueryExecutor.compile(config.getOperation());
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(field);
		TLFormObject overlay = update.getOverlay();
		_operation.execute(overlay, newValue, oldValue, update.getAttribute());
	}

}

