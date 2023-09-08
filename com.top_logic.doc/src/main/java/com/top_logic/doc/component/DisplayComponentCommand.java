/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import java.util.Map;

import com.top_logic.basic.col.ObjectFlag;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.doc.model.Page;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.help.HelpFinder;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelHidden;

/**
 * Command that displays the component with the {@link Page#getUuid() unique page identifier} as
 * {@link HelpFinder#getHelpID(LayoutComponent) help id}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayComponentCommand extends AbstractCommandHandler {

	/**
	 * Creates a new {@link DisplayComponentCommand}.
	 */
	public DisplayComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		Page page = (Page) model;
		String helpID = page.getUuid();
		LayoutComponent target = componentWithHelpId(aComponent.getMainLayout(), helpID);
		if (target == null) {
			InfoService.showInfo(I18NConstants.HELP_ID_NOT_A_COMPONENT__HELP_ID.fill(helpID));
			return HandlerResult.DEFAULT_RESULT;
		}
		boolean success = target.makeVisible();
		if (!success) {
			return HandlerResult.error(I18NConstants.COMPONENT_COULD_NOT_BE_MADE_VISIBLE);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private LayoutComponent componentWithHelpId(MainLayout mainLayout, String componentHelpID) {
		ObjectFlag<LayoutComponent> target = new ObjectFlag<>(null);
		mainLayout.acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (target.get() != null) {
					// Stop traversing when any component is found.
					return false;
				}
				if (componentHelpID.equals(helpID(aComponent))) {
					target.set(aComponent);
					return false;
				}
				return true;
			}

			private String helpID(LayoutComponent component) {
				if (HelpFinder.hasHelpId(component)) {
					return HelpFinder.getHelpID(component);
				}
				DialogInfo dialogInfo = component.getConfig().getDialogInfo();
				if (dialogInfo != null && dialogInfo.getHelpId() != null) {
					return dialogInfo.getHelpId();
				}
				return null;
			}
		});
		return target.get();
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelHidden.INSTANCE);
	}

}

