/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.action.ModelAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Base class for {@link ApplicationActionOp}s that target a single {@link FormMember}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormActionOp<S extends ModelAction> extends AbstractApplicationActionOp<S> {

	/**
	 * Creates a {@link AbstractFormActionOp} form configuration.
	 */
	public AbstractFormActionOp(InstantiationContext context, S config) {
		super(context, config);
	}

	/**
	 * The {@link FormMember} this action is about.
	 */
	protected final FormMember findMember(ActionContext actionContext) {
		ModelName modelName = config.getModelName();
		return (FormMember) ModelResolver.locateModel(actionContext, modelName);
	}

}
