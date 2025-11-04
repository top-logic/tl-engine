/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link RowClassProvider} that can be parameterized by TL-Script.
 * 
 * @implNote This implementation can directly be attached to a {@link TableConfig} by also
 *           implementing {@link TableConfigurationProvider}.
 */
@InApp
public class RowClassProviderByExpression
		extends DefaultRowClassProvider<RowClassProviderByExpression.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration options for {@link RowClassProviderByExpression}.
	 */
	public interface Config<I extends RowClassProviderByExpression> extends DefaultRowClassProvider.Config<I> {
		/**
		 * Function computing the CSS class for a certain cell.
		 * 
		 * <p>
		 * The function expects the following three arguments:
		 * </p>
		 * 
		 * <dl>
		 * <dt><code>row</code></dt>
		 * <dd>The row object of the current table row. If the column represents an attribute of an
		 * object, then the row is the object from which attributes are displayed in the current
		 * table row.</dd>
		 * 
		 * <dt><code>model</code></dt>
		 * <dd>The model of the underlying component.</dd>
		 * </dl>
		 * 
		 * <p>
		 * The function is expected to return a string representing the CSS class to be set to the
		 * current row. Multiple CSS classes may be separated with white space. If the function
		 * returns <code>null</code>, no CSS classes are added to the current row.
		 * </p>
		 * 
		 * <p>
		 * To add the CSS class "<code>tl-warning</code>" to rows that display objects with some
		 * propety greater than <code>99</code>, you may use the following expression:
		 * </p>
		 * 
		 * <pre>
		 * <code>row -> model -> $row.get(`my.moduel:MyClass#myProp`) &gt; 99 ? "tl-warning" : null</code>
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
	 * Creates a {@link RowClassProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RowClassProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		if (!(context instanceof SimpleInstantiationContext)) {
			context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
		}
		_cssClasses = QueryExecutor.compile(config.getCssClasses());
	}

	@Override
	protected void buildRowClass(StringBuilder buffer, TableControl view, int rowOptions, int displayedRow, int row) {
		// Note: It is important to always set the intrinsic CSS classes for table rows, since they
		// are required for functionality.
		super.buildRowClass(buffer, view, rowOptions, displayedRow, row);

		// See also CellClassProviderByExpression
		Object rowObj = view.getViewModel().getRowObject(row);
		if (rowObj instanceof TLTreeNode<?> node) {
			// This establishes consistency with tree grids, where the row object is just the
			// business object of the node.
			rowObj = node.getBusinessObject();
		}
		Object componentModel = _component == null ? null : _component.getModel();
		Object result = _cssClasses.execute(rowObj, componentModel);

		String customClasses = CellClassProviderByExpression.toCssClass(result);

		appendCssClass(buffer, customClasses);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		table.setRowClassProvider(this);
	}

}
