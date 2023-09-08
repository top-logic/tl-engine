/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.knowledge.wrap.person.TransientPersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.DefaultHandlerResult;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Reset the Personal Configuration assuming the underlying model is a Person.
 * 
 * In case the current Person is the model the {@link TransientPersonalConfiguration} from the
 * TLContext will be removed. The {@link PersonalConfigurationWrapper} will be deleted from the
 * KBase in any case.
 * 
 * @author <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class ResetPersonalConfiguration extends AbstractCommandHandler {
    
	/** ID of Command as registered with CommandHandler Factory */
	public static final String COMMAND_ID = "resetPersonalConfiguration";

	/**
	 * Configuration for {@link ResetPersonalConfiguration}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		@BooleanDefault(true)
		@Override
		boolean getConfirm();

	}

	/** Create the Command with {@link SimpleBoundCommandGroup#RESTRICTED_WRITE} */
	public ResetPersonalConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext,
			LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		try {
			Person thePerson = getPersonFromComponenet(aComponent);
			TLContext theContext = TLContext.getContext();
			if (thePerson != null && theContext != null) {
				try (Transaction tx = thePerson.getKnowledgeBase().beginTransaction()) {
					if (thePerson == theContext.getCurrentPersonWrapper()) {
						forgetTransientConfiguration(theContext);
					}
					dropPersonalConfigurationWrapper(thePerson);
					tx.commit();
				}
			}
		} catch (Exception any) {
			Logger.error("Failed to ResetPersonalConfiguration", any);
			throw new TopLogicException(ResetPersonalConfiguration.class, "save.failed", any);
		}
        
		aComponent.invalidate();
		return DefaultHandlerResult.DEFAULT_RESULT;
	}

	/**
	 * {@link TLContext#resetPersonalConfiguration()} and eventually clear it.
	 */
	protected void forgetTransientConfiguration(TLContext aContext) {
		aContext.resetPersonalConfiguration();
	}

	/**
	 * Call {@link PersonalConfigurationWrapper#tDelete()} for now.
	 */
	protected void dropPersonalConfigurationWrapper(Person aPerson) {
		PersonalConfigurationWrapper theConf = PersonalConfigurationWrapper.getPersonalConfiguration(aPerson);
		if (theConf != null) {
			theConf.tDelete();
		}
	}

	/**
	 * Extract a Person as model from aComponent.
	 * 
	 * This can be used to derive the Person from some other Model (e.g. a PersonContact).
	 * 
	 * @return null in case no Person can be resolved from aComponent
	 */
	private Person getPersonFromComponenet(LayoutComponent aComponent) {
		Object theModel =  aComponent.getModel();
		if (theModel instanceof Person) {
			return (Person) theModel;
		}
		if (theModel != null) {
			Logger.warn("Could not resolve a Person from " + theModel, ResetPersonalConfiguration.class);
		}
		return null;
	}
	
	/**
	 * Allow in View-Mode via {@link InViewModeExecutable}.
	 */
	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return InViewModeExecutable.INSTANCE;
	}
}