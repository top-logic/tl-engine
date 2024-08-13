/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.HomepageImpl;
import com.top_logic.knowledge.wrap.person.NoStartPageAutomatismExecutability;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link AbstractCommandHandler} setting the personal homepage.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
@Label("Homepage handler")
public class SetHomepageHandler extends AbstractCommandHandler {

	/** Default command name for {@link SetHomepageHandler}. */
	public final static String COMMAND_ID = "setHomepage";

	/**
	 * Configuration of the {@link SetHomepageHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Whether the selection of the last component must be stored.
		 */
		boolean isStoreSelection();

		@Override
		@ListDefault(NoStartPageAutomatismExecutability.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		/**
		 * The implementation class for the homepage.
		 */
		@ClassDefault(HomepageImpl.class)
		Class<? extends HomepageImpl> getHomepageClass();

	}

	/**
	 * Creates a new {@link SetHomepageHandler}.
	 */
    public SetHomepageHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
	private Config config() {
		return (Config) getConfig();
	}

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		setHomepage(aComponent.getMainLayout());

		return HandlerResult.DEFAULT_RESULT;
    }

	/**
	 * Set the business component currently active as homepage (with its model) for the current
	 * user.
	 * 
	 * The method will store the homepage settings in the {@link PersonalConfiguration} which is (at
	 * this moment) a transient one. On logout the information will be transformed into a persistent
	 * variant.
	 * 
	 * @param mainLayout
	 *        The main layout containing all layout components, must not be <code>null</code>.
	 * @see #getHomepage(MainLayout)
	 */
	public void setHomepage(MainLayout mainLayout) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration != null) {
			personalConfiguration.setHomepage(mainLayout, getHomepage(mainLayout));
		}
	}

	/**
	 * Get the homepage information from the given main layout.
	 * 
	 * 
	 * @param mainLayout
	 *        The main layout containing all layout components, must not be <code>null</code>.
	 * @return The requested {@link Homepage} information. May be <code>null</code>.
	 */
	public Homepage getHomepage(MainLayout mainLayout) {
		HomepageImpl homepage = TypedConfigUtil.newConfiguredInstance(config().getHomepageClass());

		homepage.fillConfigFrom(mainLayout, config().isStoreSelection());
		return homepage.getConfig();
	}

}
