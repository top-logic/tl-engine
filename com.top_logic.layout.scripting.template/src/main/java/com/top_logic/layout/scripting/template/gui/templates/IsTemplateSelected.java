/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Map;

import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateLocation;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that disables a command, if the component has not selected a template.
 * 
 * @see #isTemplate(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IsTemplateSelected implements ExecutabilityRule {

	/**
	 * Singleton {@link IsTemplateSelected} instance.
	 */
	public static final IsTemplateSelected INSTANCE = new IsTemplateSelected();

	private IsTemplateSelected() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		Object selection = ((Selectable) aComponent).getSelected();
		if (!isTemplate(selection)) {
			return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_TEMPLATE_SELECTED);
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Whether a given component model is a template.
	 */
	public static boolean isTemplate(Object value) {
		return template(value) != null;
	}

	/**
	 * Extracts {@link TemplateResource} from the given template node.
	 */
	public static TemplateResource template(Object value) {
		if (!(value instanceof TLTreeNode<?>)) {
			return null;
		}
		TLTreeNode<?> node = (TLTreeNode<?>) value;
		Object businessObject = node.getBusinessObject();
		if (!(businessObject instanceof TemplateLocation)) {
			return null;
		}
		TemplateLocation templateLocation = (TemplateLocation) businessObject;
		if (!templateLocation.isTemplate()) {
			return null;
		}

		return (TemplateResource) templateLocation;
	}

}
