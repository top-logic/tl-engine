/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.security;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.structured.AdminElementComponent;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * Component for security demonstrations. Shows the parent of the Master-Selection.
 * 
 * @author <a href="mailto:cca@top-logic.com>cca</a>
 */
public class EditParentComponent extends AdminElementComponent {

	public EditParentComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	/**
	 * Opens the parent of the selected model in a certain dialog. Security is checked on the
	 * target-component. If the target-component can not display the parent-object, the command can
	 * not be executed.
	 * 
	 * @author <a href="mailto:cca@top-logic.com>cca</a>
	 */
	public static class GotoParentHandler extends GotoHandler {

		public GotoParentHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues) {
			if (model instanceof StructuredElement) {
				BoundObject parent = (BoundObject) ((StructuredElement) model).getParent();
				BoundChecker boundChecker = getBoundChecker((BoundChecker) component, parent, someValues);
				return boundChecker.allow(getCommandGroup(), parent);
			}
			return false;
		}

		@Override
		protected ComponentName getComponentName(Map<String, Object> someArguments) {
			return ComponentName.newName(
				"com.top_logic.demo/security/demoSecurity/demoSecurityTypesView.layout.xml", "DialogGotoParent");
		}

		@Override
		protected Object getObject(Map<String, Object> someArguments) {
			if (someArguments.containsKey(AbstractCommandHandler.BOUND_OBJECT)) {
				return someArguments.get(AbstractCommandHandler.BOUND_OBJECT);
			}
			return super.getObject(someArguments);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			if (model instanceof StructuredElement) {
				someArguments.put(AbstractCommandHandler.BOUND_OBJECT, ((StructuredElement) model).getParent());
			}
			return super.handleCommand(aContext, aComponent, model, someArguments);
		}

	}

}
