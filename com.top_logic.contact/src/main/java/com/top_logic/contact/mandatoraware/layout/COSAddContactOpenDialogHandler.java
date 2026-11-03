/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.layout;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * Opens the dialog for creating a contact, if the selected mandator allows contacts.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class COSAddContactOpenDialogHandler extends OpenModalDialogCommandHandler {

	/**
	 * Configuration of the {@link COSAddContactOpenDialogHandler}.
	 */
	public interface Config extends OpenModalDialogCommandHandler.Config {

		@Override
		@ListDefault({
			InViewModeExecutable.class,
			MandatorAllowsCreateExecutable.class
		})
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

	public COSAddContactOpenDialogHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	
	/**
	 * Rule allowing the creation of contacts only for mandators that allow contacts.
	 */
	public static class MandatorAllowsCreateExecutable implements ExecutabilityRule {
	    /**
	     * Singleton instance of this class.
	     */
	    public static final ExecutabilityRule INSTANCE = new MandatorAllowsCreateExecutable();
	    
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Selectable theSelMaster = ((LayoutComponent) aComponent.getSelectableMaster()).getSelectableMaster();
			if (theSelMaster != null) {
				Object theSel = theSelMaster.getSelected();
				if (theSel instanceof Mandator) {
					Mandator theMandator = (Mandator) theSel;
					if (!theMandator.allowType(ContactFactory.STRUCTURE_NAME)) {
						return ExecutableState.createDisabledState(I18NConstants.CREATE_CONTACTS_NOT_ALLOWED);
					}
				}
			}
			return ExecutableState.EXECUTABLE;
		}
	}
}
