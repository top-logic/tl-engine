/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.template.action.op.BusinessOperationTemplateActionOp;

/**
 * An {@link TemplateAction} that locates the template via the business action and the business
 * object the business action is about.
 * 
 * @see BusinessOperationTemplateActionOp
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface BusinessOperationTemplateAction extends TemplateAction {

	@Override
	@ClassDefault(BusinessOperationTemplateActionOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/** The name of the business object the template is about. */
	public String getBusinessObject();

	/** @see #getBusinessObject() */
	public void setBusinessObject(String businessObject);

	/** The name of the business action the template is about. */
	public String getBusinessAction();

	/** @see #getBusinessAction() */
	public void setBusinessAction(String businessAction);

}
