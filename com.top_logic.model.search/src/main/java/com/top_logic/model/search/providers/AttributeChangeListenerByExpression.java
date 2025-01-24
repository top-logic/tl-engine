/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.meta.AttributeChangeListener;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link AttributeChangeListener} implementation in TL-Script.
 */
@InApp
@Label("TL-Script change listener")
public class AttributeChangeListenerByExpression
		extends AbstractConfiguredInstance<AttributeChangeListenerByExpression.Config>
		implements AttributeChangeListener {

	/**
	 * Configuration options for {@link AttributeChangeListenerByExpression}.
	 */
	public interface Config extends PolymorphicConfiguration<AttributeChangeListenerByExpression> {
		/**
		 * Operation that is called when an attribute is changed.
		 * 
		 * <p>
		 * The operation expects three arguments:
		 * </p>
		 * 
		 * <pre>
		 * obj -> attr -> value -> ...
		 * </pre>
		 * 
		 * <table>
		 * <tr>
		 * <td>obj</td>
		 * <td>The object whose attribute was updated.</td>
		 * <td>attr</td>
		 * <td>The attribute that got a new value.</td>
		 * <td>value</td>
		 * <td>The new value set to the object's attribute.</td>
		 * </tr>
		 * </table>
		 * 
		 * <p>
		 * The result of the operation is ignored. The operation is invoked in a transaction and is
		 * allowed to modify other objects reachable from the given one.
		 * </p>
		 */
		@Mandatory
		Expr getOperation();
	}

	private QueryExecutor _operation;

	/**
	 * Creates a {@link AttributeChangeListenerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeChangeListenerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		
		_operation = QueryExecutor.compile(config.getOperation());
	}

	@Override
	public void notifyChange(TLObject obj, TLStructuredTypePart attr, Object newValue) {
		_operation.execute(obj, attr, newValue);
	}

}
