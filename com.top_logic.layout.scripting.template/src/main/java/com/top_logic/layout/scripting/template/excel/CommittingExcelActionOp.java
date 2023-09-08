/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.template.excel.ExcelActionOp.ExcelAction;

/**
 * Action to be executed in a correct context environment.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class CommittingExcelActionOp<C extends ExcelAction> extends ExcelActionOp<C> {

	/**
	 * Creates a {@link CommittingExcelActionOp} from a {@link ExcelAction}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent configured objects.
	 * @param config
	 *        The {@link ExcelAction} of this {@link CommittingExcelActionOp}.
	 */
	@CalledByReflection
	public CommittingExcelActionOp(InstantiationContext context, C config) {
		super(context, config);
    }

	/**
	 * Operation to be executed, changes will be committed by the {@link CommittingExcelActionOp}.
	 * 
	 * @param actionContext
	 *        The action context contains the {@link GlobalVariableStore} and other useful
	 *        information.
	 * @return Object provided by this action (to be placed in the given map, may be
	 *         <code>null</code>.
	 * @throws Exception
	 *         When executing the operation fails.
	 */
	public abstract Object executeWithCommit(ActionContext actionContext) throws Exception;

	@Override
	public Object execute(ActionContext actionContext) throws Exception {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();

		try {
			Object theObject = this.executeWithCommit(actionContext);

			transaction.commit();

			return theObject;
		}
		finally {
			transaction.rollback();
		}
	}

}
