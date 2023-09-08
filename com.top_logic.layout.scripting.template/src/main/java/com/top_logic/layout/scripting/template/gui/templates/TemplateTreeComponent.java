/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.model.TLObject;

/**
 * {@link TreeComponent} displaying scripting templates.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateTreeComponent extends TreeComponent {

	/**
	 * Creates a {@link TemplateTreeComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean receiveModelChangedEvent(Object model, Object someChangedBy) {
		handleModelUpdate(model);
		return super.receiveModelChangedEvent(model, someChangedBy);
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		for (TLObject model : models) {
			if (handleModelUpdate(model)) {
				break;
			}
		}
		return super.receiveModelDeletedEvent(models, changedBy);
	}

	private boolean handleModelUpdate(Object model) {
		if (model instanceof TemplateLocation) {
			resetTreeModel();
			return true;
		}
		return false;
	}

}
