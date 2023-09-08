/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LayoutComponent} for creating new templates.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateCreateBuilder extends AbstractTemplateFormBuilder<TLTreeNode<?>> {

	/**
	 * Creates a {@link TemplateCreateBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateCreateBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void fillFormModel(TemplateEditModel formModel, TLTreeNode<?> businessModel) {
		if (businessModel == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_SELECTION);
		}
		ApplicationAction action = (ApplicationAction) businessModel.getBusinessObject();
		formModel.setContent(ActionWriter.INSTANCE.writeAction(action));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<? extends TLTreeNode<?>> getModelType() {
		return (Class) TLTreeNode.class;
	}

}
