/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.template.action.op.ScriptTemplateActionOp;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * An {@link TemplateAction} that identifies the template by a resource name to be resolved by
 * {@link TemplateSourceFactory}.
 * 
 * @see ScriptTemplateActionOp
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ScriptTemplateAction extends TemplateAction {

	/**
	 * @see #getTemplate()
	 */
	String TEMPLATE = "template";

	@Override
	@ClassDefault(ScriptTemplateActionOp.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The name of the template resource to be resolved through {@link TemplateSourceFactory}.
	 * 
	 * <p>
	 * The default protocol is <code>script:</code>, if none is given in the resource name.
	 * </p>
	 */
	@Name(TEMPLATE)
	public String getTemplate();

	/** @see #getTemplate() */
	public void setTemplate(String value);

}
