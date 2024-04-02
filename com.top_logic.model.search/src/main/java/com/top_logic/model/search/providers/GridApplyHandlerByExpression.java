/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.layout.grid.DefaultGridApplyHandler;
import com.top_logic.element.layout.grid.GridApplyHandler;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link GridApplyHandler} that can be parameterized by TL-Script.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class GridApplyHandlerByExpression extends DefaultGridApplyHandler
		implements ConfiguredInstance<GridApplyHandlerByExpression.Config<?>> {

	private Config<?> _config;

	private QueryExecutor _canEdit;

	private QueryExecutor _script;

	/**
	 * Configuration options for {@link GridApplyHandlerByExpression}.
	 */
	public interface Config<I extends GridApplyHandlerByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Function deciding whether the selected row can be edited.
		 * 
		 * <p>
		 * The function expects two arguments. The first argument is the row object in question. The
		 * second argument is the the component's model.
		 * </p>
		 */
		Expr getCanEdit();

		/**
		 * Script that is executed after the edited model has been updated with new values from the
		 * form and before the apply transaction completes.
		 * 
		 * <p>
		 * The script expects the edited object as first argument and the components base model as
		 * second argument.
		 * </p>
		 */
		Expr getPostApplyScript();
	}

	/**
	 * Creates a {@link GridApplyHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GridApplyHandlerByExpression(InstantiationContext context, Config<?> config) {
		_config = config;

		_canEdit = QueryExecutor.compileOptional(config.getCanEdit());
		_script = QueryExecutor.compileOptional(config.getPostApplyScript());
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	@Override
	public boolean storeChanges(GridComponent component, Object rowObject, FormContainer container) {
		boolean changed = super.storeChanges(component, rowObject, container);
		if (changed) {
			if (_script != null) {
				_script.execute(rowObject, component.getModel());
			}
		}
		return changed;
	}

	@Override
	public boolean allowEdit(GridComponent component, Object rowObject) {
		if (_canEdit == null) {
			return super.allowEdit(component, rowObject);
		}

		return asBoolean(_canEdit.execute(rowObject, component.getModel()));
	}

	private static boolean asBoolean(Object result) {
		return ((Boolean) result).booleanValue();
	}

}
