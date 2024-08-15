/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.table.CellClassProvider;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.CssClassProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Computation of dynamic CSS classes for table cells in a TL-Script function.
 */
@InApp
@Label("CSS class provider in TL-Script")
public class CellClassProviderByExpression extends AbstractConfiguredInstance<CellClassProviderByExpression.Config<?>>
		implements CellClassProvider, CssClassProvider {

	/**
	 * Configuration options for {@link CellClassProviderByExpression}.
	 */
	@TagName("cell-class-by-expression")
	public interface Config<I extends CellClassProviderByExpression> extends PolymorphicConfiguration<I> {
		/**
		 * Function computing the CSS class for a certain cell.
		 * 
		 * <p>
		 * The function expects the following three arguments:
		 * </p>
		 * 
		 * <dl>
		 * <dt><code>value</code></dt>
		 * <dd>The value of the cell that should receive a CSS class. If the column represents an
		 * attribute of an object, then the value is the value of this attribute for the row
		 * object.</dd>
		 * </dl>
		 * 
		 * <dl>
		 * <dt><code>row</code></dt>
		 * <dd>The row object of the current table row. If the column represents an attribute of an
		 * object, then the row is the object from which attributes are displayed in the current
		 * table row.</dd>
		 * </dl>
		 * 
		 * <dl>
		 * <dt><code>model</code></dt>
		 * <dd>The model of the underlying component.</dd>
		 * </dl>
		 * 
		 * <p>
		 * The function is expected to return a string representing the CSS class to be set to the
		 * current cell. Multiple CSS classes may be separated with white space. If the function
		 * returns <code>null</code>, no CSS classes are added to the current cell.
		 * </p>
		 * 
		 * <p>
		 * To add the CSS class "<code>tl-warning</code>" to cells with values greater than
		 * <code>99</code>, you may use the following expression:
		 * </p>
		 * 
		 * <pre>
		 * <code>value -> $value &gt; 99 ? "tl-warning" : null</code>
		 * </pre>
		 * 
		 * <p>
		 * There are a number of pre-defined CSS classes for highlighting:
		 * </p>
		 * 
		 * <ul>
		 * <li><code>tl-info</code></li>
		 * <li><code>tl-success</code></li>
		 * <li><code>tl-warning</code></li>
		 * <li><code>tl-danger</code></li>
		 * <li><code>tl-debug</code></li>
		 * <li><code>tl-accent-1</code></li>
		 * <li><code>tl-accent-2</code></li>
		 * <li><code>tl-accent-3</code></li>
		 * </ul>
		 * 
		 * <p>
		 * All these classes can be combined with the class <code>tl-lighter</code> to make the
		 * highlighting less prominent.
		 * </p>
		 */
		@Mandatory
		@Label("CSS classes")
		Expr getCssClasses();
	}

	private LayoutComponent _component;

	private QueryExecutor _cssClasses;

	/**
	 * Creates a {@link CellClassProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CellClassProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);
		if (!(context instanceof SimpleInstantiationContext)) {
			context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
		}
		_cssClasses = QueryExecutor.compile(config.getCssClasses());
	}

	@Override
	public String getCellClass(Cell cell) {
		Object value = cell.getValue();
		Object row = cell.getRowObject();
		Object componentModel = _component == null ? null : _component.getModel();
		Object result = _cssClasses.execute(value, row, componentModel);
		return toCssClass(result);
	}

	@Override
	public String getCssClass(TLObject model, TLStructuredTypePart attribute, Object value) {
		Object componentModel = _component == null ? null : _component.getModel();
		Object result = _cssClasses.execute(value, model, componentModel);
		return toCssClass(result);
	}

	private static String toCssClass(Object result) {
		if (StringServices.isEmpty(result)) {
			return null;
		}
		if (result instanceof Collection<?> collection) {
			return collection.stream()
				.map(CellClassProviderByExpression::toCssClass)
				.filter(Objects::nonNull)
				.collect(Collectors.joining(" "));
		}
		return result.toString();
	}

}
