/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.window;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.StringValue;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.Layout;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.util.error.TopLogicException;

/**
 * This is a special SubstitutionReferenceComponnent that references separate
 * windows. It defines some substitutions that are needed for handling of
 * multiple separate windows displaying the same components.
 * 
 * {@link WindowTemplate}s are registered at the {@link WindowManager}. The final
 * Components will be resolve on opening the Window.
 * 
 * @author <a href="mailto:twi@top-logic.com">twi</a>
 */
public class WindowTemplate extends LayoutComponent {

	/**
	 * Configuration for component that are opened in an external window.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface WindowConfig extends ConfigurationItem {

		/** Configuration name for the value of {@link #getWindowInfo()}. */
		String WINDOW_INFO_NAME = "windowInfo";

		/**
		 * {@link WindowConfig} holding the necessary informations for opening the window.
		 */
		@Name(WINDOW_INFO_NAME)
		WindowInfo getWindowInfo();

		/**
		 * Setter for {@link #getWindowInfo()}.
		 */
		void setWindowInfo(WindowInfo info);

	}

	public interface Config extends LayoutComponent.Config, WindowConfig {

		/** @see #getTemplate() */
		String TEMPLATE = "template";

		@ClassDefault(WindowTemplate.class)
		@Override
		Class<? extends LayoutComponent> getImplementationClass();

		@Name(TEMPLATE)
		String getTemplate();

		@MapBinding
		Map<String, String> getSubstitutions();

	}

	/**
	 * This variable should be used in the referenced layout-xml to make names
	 * of LayoutComponents unique and enable opening of multiple separate
	 * windows.
	 */
	public static final String NAME_PREFIX_ARGUMENT = "namePrefix";

	/**
	 * The {@link LayoutComponent} on which the template was defined.
	 */
	private LayoutComponent owner;

	private Map<String, StringValue> _arguments = new HashMap<>();
	{
		setNamePrefix("");
	}

	public WindowTemplate(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		for (Entry<String, String> substitution : config.getSubstitutions().entrySet()) {
			addArgument(substitution.getKey(), substitution.getValue());
		}
	}

	public LayoutComponent getOwner() {
		return owner;
	}

	public void setOwner(LayoutComponent owner) {
		this.owner = owner;
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
	   // defer resolving of the window content
	}
	
	/**
	 * Instantiates this template.
	 * 
	 * This is the deferred variant of resolveComponent, triggered by the {@link WindowManager}.
	 * 
	 * @return The new window with the contents described by this template.
	 */
	public WindowComponent instantiate(InstantiationContext context, Layout parent, LayoutComponent opener) {
		try {
			if (getWindowInfo().getMultipleWindows()) {
				// ensure that the name of the new WindowComponent and all
				// children have different names to eventually already existing
				// windows.
				setNamePrefix(getEnclosingFrameScope().createNewID());
			}
			// Load the window contents.


			// Instantiate the new window.
			WindowComponent.Config config = TypedConfiguration.newConfigItem(WindowComponent.Config.class);
			config.setName(getWindowName());
			config.setWindowInfo(getWindowInfo());
			WindowComponent window = new WindowComponent(context, config);
			window.setOpener(opener);

			CreateComponentParameter createArgs = CreateComponentParameter.newParameter(getTemplate());
			createArgs.setTemplateArguments(_arguments);
			createArgs.setLayoutResolver(LayoutResolver.newRuntimeResolver(new LogProtocol(WindowTemplate.class)));
			LayoutComponent.Config configuration = LayoutStorage.getInstance().installLayout(context, createArgs);
			if (configuration == null) {
				throw new TopLogicException(getWindowInstantiationErrorMessage(null));
			}
			LayoutComponent contents =
				LayoutUtils.createComponentFromXML(context, window, getTemplate(), false, configuration);
			window.addComponent(contents);
			return window;
		} catch (ConfigurationException | IOException exception) {
			throw new TopLogicException(getWindowInstantiationErrorMessage(exception), exception);
		}
	}

	private ResKey getWindowInstantiationErrorMessage(Exception exception) {
		Logger.error("Failed to instantiate window '" + getTemplate() + "'.", exception, WindowTemplate.class);
		return I18NConstants.WINDOW_TEMPLATE_INSTANTIATION_FAILED__TEMPLATE.fill(getTemplate());
	}

	private void setNamePrefix(String namePrefix) {
		_arguments.put(NAME_PREFIX_ARGUMENT, new StringValue(NAME_PREFIX_ARGUMENT, namePrefix));
	}

	public ComponentName getWindowName() {
		StringBuilder name = new StringBuilder();
		name.append("win_");
		name.append(getTemplate().replace('/', '_'));
		name.append(_arguments.get(NAME_PREFIX_ARGUMENT).get());
		return ComponentName.newName(name.toString());
	}

	public String getTemplate() {
		return config().getTemplate();
	}

	private Config config() {
		return (Config) getConfig();
	}

	/**
	 * The {@link WindowInfo} for this component.
	 * 
	 * @see WindowTemplate.Config#getWindowInfo()
	 */
    public WindowInfo getWindowInfo() {
		return config().getWindowInfo();
    }

	private void addArgument(String name, String value) {
		_arguments.put(name, new StringValue(name, value));
	}
}
