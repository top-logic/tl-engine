/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.SelectionUpdater;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Updates the selection using a <i>TL-Script</i> computes the new selection from the model and the
 * current selection of the component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("New selection with TL-Script")
@InApp
public class SelectionUpdaterByExpression extends AbstractConfiguredInstance<SelectionUpdaterByExpression.Config>
		implements SelectionUpdater {

	/**
	 * Typed configuration interface definition for {@link SelectionUpdaterByExpression}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<SelectionUpdaterByExpression> {

		/**
		 * The algorithm that computes the new selection.
		 * 
		 * The algorithm is called with the model of the component and the current selection. The
		 * result is the new selection of the component.
		 */
		@Mandatory
		Expr getAlgorithm();

	}

	private final QueryExecutor _algorithm;

	/**
	 * Create a {@link SelectionUpdaterByExpression}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SelectionUpdaterByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_algorithm = QueryExecutor.compile(config.getAlgorithm());
	}

	@Override
	public void updateSelection(Selectable selectable) {
		Object currentSelection = selectable.getSelected();
		Object newSelection = _algorithm.execute(selectable.getModel(), currentSelection);
		selectable.setSelected(newSelection);

	}

}
