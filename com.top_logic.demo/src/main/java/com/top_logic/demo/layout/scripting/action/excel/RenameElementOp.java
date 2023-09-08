/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.scripting.action.excel;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.demo.layout.scripting.action.excel.RenameElementOp.RenameElement;
import com.top_logic.demo.layout.scripting.action.excel.params.RenameParameters;
import com.top_logic.demo.model.types.wrap.DemoTypesAllBase;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.GlobalVariableStore;
import com.top_logic.layout.scripting.template.excel.CommittingExcelActionOp;
import com.top_logic.layout.scripting.template.excel.ExcelActionOp;

/**
 * An example of a {@link CommittingExcelActionOp} that renames the object given as context.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class RenameElementOp extends CommittingExcelActionOp<RenameElement> {

	/** The config interface for the {@link RenameElementOp}. */
	public interface RenameElement extends ExcelActionOp.ExcelAction {

		@Override
		@ClassDefault(RenameElement.class)
		Class<RenameElementOp> getImplementationClass();

		@Override
		RenameParameters getParameters();

	}

	/**
	 * Creates a {@link RenameElementOp} from a configuration.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} for instantiation of dependent configured objects.
	 * @param config
	 *        The configuration of this {@link RenameElementOp}.
	 */
	@CalledByReflection
	public RenameElementOp(InstantiationContext context, RenameElement config) {
		super(context, config);
	}

	@Override
	public Object executeWithCommit(ActionContext actionContext) {
		GlobalVariableStore variableStore = actionContext.getGlobalVariableStore();
		DemoTypesAllBase objectToRename = (DemoTypesAllBase) variableStore.get(getConfig().getContext());
		objectToRename.setName(getConfig().getParameters().getNewName());
		return objectToRename;
	}

}
