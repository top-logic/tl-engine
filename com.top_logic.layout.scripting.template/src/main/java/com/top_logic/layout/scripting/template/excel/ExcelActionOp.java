/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.template.excel.ExcelActionOp.ExcelAction;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;



/**
 * Processing instance for one {@link ExcelRow} and base class to the different excel based actions
 * (doing some administration stuff).
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class ExcelActionOp<C extends ExcelAction> extends AbstractApplicationActionOp<C> {

	/**
	 * Base configuration information for the {@link ExcelActionOp}s.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface ExcelAction extends ApplicationAction {

		/** The variable name in which the result of this {@link ExcelAction} should be stored. */
		@Name(ExcelRowParser.RESULT_PROPERTY)
		String getResultName();

		/** @see #getResultName() */
		void setResultName(String newValue);

		/**
		 * The value of the "Kontext" excel column, which contains the name of the variable (in the
		 * {@link GlobalVariableStore}) in which the target element is stored.
		 */
		@Name(ExcelRowParser.CONTEXT_PROPERTY)
		String getContext();

		/** @see #setContext(String) */
		void setContext(String newValue);

		/**
		 * The values from the "Parameter" and the following excel columns.
		 * <p>
		 * If an {@link ExcelAction} is excepting values in the parameter columns, it has to refine
		 * the return type of this property with the concrete config interface describing the
		 * expected parameters.
		 * </p>
		 */
		@Name(ExcelRowParser.PARAM_PROPERTY)
		@Subtypes({})
		ConfigurationItem getParameters();

		// Debug information:

		/** The name of the excel file from which this action is parsed. */
		String getFileName();

		/** @see #getFileName() */
		void setFileName(String newValue);

		/** The name of the excel sheet from which this action is parsed. */
		String getSheetName();

		/** @see #getSheetName() */
		void setSheetName(String newValue);

		/** The number of the row from which this action is parsed. */
		int getRowNumber();

		/** @see #getRowNumber() */
		void setRowNumber(int newValue);

		/** The name of the business model, this {@link ExcelAction} is about. */
		String getBusinessModel();

		/** @see #getBusinessModel() */
		void setBusinessModel(String newValue);

		/** The name of the business action, this {@link ExcelAction} is about. */
		String getBusinessAction();

		/** @see #getBusinessAction() */
		void setBusinessAction(String newValue);

	}

	/**
	 * Creates a {@link ExcelActionOp} from a {@link ExcelAction}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent configured objects.
	 * @param config
	 *        The {@link ExcelAction} of this {@link ExcelActionOp}.
	 */
	@CalledByReflection
	public ExcelActionOp(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with Config: " + getConfig();
	}

	@Override
	public Object processInternal(ActionContext actionContext, Object anArgument) throws Exception {
		Object result = execute(actionContext);
		String resultName = getConfig().getResultName();

		if (!StringServices.isEmpty(resultName)) {
			actionContext.getGlobalVariableStore().set(resultName, result);
		}

		validateResult(result, resultName, getResultMap(actionContext));

		Logger.info("Processed '" + getConfig().location() + "': " + this + '!', ExcelActionOp.class);

		return result;
	}

	/**
	 * Returns the mappings from the {@link GlobalVariableStore} as unmodifiable {@link Map}.
	 * 
	 * @param actionContext
	 *        The {@link ActionContext} in which the current {@link ApplicationAction} is executed.
	 *        It holds the {@link GlobalVariableStore} from which the result {@link Map} is
	 *        constructed. Must not be <code>null</code>.
	 * @return <b>Unmodifiable</b>, as changes don't write through to the
	 *         {@link ActionContext#getGlobalVariableStore()} and would therefore be lost. Never
	 *         <code>null</code>.
	 */
	protected Map<String, Object> getResultMap(ActionContext actionContext) {
		return Collections.unmodifiableMap(actionContext.getGlobalVariableStore().getMappings());
	}

	/**
	 * Return the command handler identified by the given unique ID.
	 * 
	 * @param    aName    The name of the requested command handler, must not be <code>null</code>.
	 * @return The requested command handler, never <code>null</code> (when handler registered).
	 */
	@SuppressWarnings("unchecked")
	protected <T extends CommandHandler> T getCommandHandler(String aName) {
		return (T) CommandHandlerFactory.getInstance().getHandler(aName);
	}

	/**
	 * Hook for performing additional checks during {@link #process(ActionContext, Object)}.
	 * 
	 * @param aResultValue
	 *        The value returned from the {@link ExcelActionOp#execute(ActionContext)}.
	 * @param resultName
	 *        The key of the expected object (to be read from the given map.
	 * @param aResultMap
	 *        The map of objects provided by other actions.
	 */
	protected void validateResult(Object aResultValue, String resultName, Map<String, Object> aResultMap) {
		// Hook for subclasses
	}

	/**
	 * Contains the actual code of this action.
	 * 
	 * @param actionContext
	 *        The action context contains the {@link GlobalVariableStore} and other useful
	 *        information.
	 * @return The resulting object of the operation, may be <code>null</code>.
	 * @throws Exception
	 *         When execution fails.
	 */
	public abstract Object execute(ActionContext actionContext) throws Exception;

}
