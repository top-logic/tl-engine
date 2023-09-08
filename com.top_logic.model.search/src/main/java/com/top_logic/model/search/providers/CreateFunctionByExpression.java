/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.element.meta.gui.FormObjectCreation;
import com.top_logic.layout.form.component.CreateFunction;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link CreateFunction} initializing the created object with a TL-Script operation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CreateFunctionByExpression extends FormObjectCreation {

	private final QueryExecutor _initOperation;

	/**
	 * Creates a {@link CreateFunctionByExpression}.
	 */
	public CreateFunctionByExpression(QueryExecutor initOperation) {
		_initOperation = initOperation;
	}

	@Override
	protected void initContainer(TLObject container, TLObject newObject, Object createContext) {
		super.initContainer(container, newObject, createContext);
		_initOperation.execute(container, newObject, createContext);
	}
}