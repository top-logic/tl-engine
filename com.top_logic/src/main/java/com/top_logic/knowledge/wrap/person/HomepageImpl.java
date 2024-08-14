/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.gui.layout.I18NConstants;
import com.top_logic.knowledge.wrap.person.Homepage.ChannelValue;
import com.top_logic.knowledge.wrap.person.Homepage.Path;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.LayoutComponentResolver;
import com.top_logic.layout.scripting.runtime.LiveActionContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.commandhandlers.RelevantComponentFinder;

/**
 * Definition of the homepage of the user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HomepageImpl extends AbstractConfiguredInstance<Homepage> {

	/**
	 * Creates a {@link HomepageImpl}.
	 */
	public HomepageImpl(InstantiationContext context, Homepage config) {
		super(context, config);
	}

	/**
	 * Restores this homepage for the given {@link PersonalConfiguration} and {@link MainLayout}.
	 * 
	 * @param personalConfig
	 *        The {@link PersonalConfiguration} of the current user.
	 * @param mainLayout
	 *        {@link MainLayout} in which the homepage path is stored. Must correspond to
	 *        {@link Homepage#getMainLayout()}.
	 * 
	 * @see #fillConfigFrom(MainLayout, boolean)
	 */
	public void restore(PersonalConfiguration personalConfig, MainLayout mainLayout) {
		boolean anythingChanged = installComponentPath(personalConfig, mainLayout);
		if (anythingChanged) {
			mainLayout.invalidate();
		}

	}

	/**
	 * Installs the stored models and switches the stored component trees to visible.
	 *
	 * @param personalConfig
	 *        The {@link PersonalConfiguration} of the current user.
	 * @param mainLayout
	 *        {@link MainLayout} in which the homepage path is stored.
	 * 
	 * @return Whether anything in the component tree was changed (e.g. changed visibility, or
	 *         component model).
	 */
	protected boolean installComponentPath(PersonalConfiguration personalConfig, MainLayout mainLayout) {
		boolean anythingChanged = false;
		LiveActionContext context = new LiveActionContext(DefaultDisplayContext.getDisplayContext(), mainLayout);
		for (Path step : getConfig().getComponentPaths()) {
			ComponentName targetComponentName = step.getComponent();
			LayoutComponent component = mainLayout.getComponentByName(targetComponentName);
			if (component == null) {
				handleTargetComponentUnresolvable(personalConfig, mainLayout);
				return anythingChanged;
			}
			ChannelValue modelChannel = step.getChannelValues().get(ModelChannel.NAME);
			if (modelChannel != null) {
				// Value of model channel must be set, before the component is made visible.
				Object value;
				try {
					value = locateModel(context, modelChannel.getValue());
				} catch (RuntimeException | AssertionError ex) {
					handleTargetObjectUnresolvable(personalConfig, mainLayout);
					continue;
				}
				component.setModel(value);
			}

			boolean visible = component.makeVisible();
			if (!visible) {
				return anythingChanged;
			}
			for (ChannelValue channelValue : step.getChannelValues().values()) {
				if (ModelChannel.NAME.equals(channelValue.getName())){
					continue;
				}
				Object value;
				try {
					value = locateModel(context, channelValue.getValue());
				} catch (RuntimeException | AssertionError ex) {
					handleTargetObjectUnresolvable(personalConfig, mainLayout);
					continue;
				}
				component.getChannel(channelValue.getName()).set(value);
			}
		}
		return anythingChanged;
	}

	/**
	 * Resolves a model of the {@link Homepage}.
	 */
	protected Object locateModel(LiveActionContext context, ModelName name) {
		/* Ensure that eventually hidden components are found. Such a component is made visible when
		 * displaying model. */
		boolean before = LayoutComponentResolver.allowResolvingHiddenComponents(context, true);
		try {
			return ModelResolver.locateModel(context, name);
		} finally {
			LayoutComponentResolver.allowResolvingHiddenComponents(context, before);
		}
	}

	private void handleTargetComponentUnresolvable(PersonalConfiguration personalConfig, MainLayout mainLayout) {
		resetHomepage(personalConfig, I18NConstants.HOMEPAGE_RESTORE_PROBLEM_COMPONENT_INVALID, mainLayout);
	}

	private void handleTargetObjectUnresolvable(PersonalConfiguration personalConfig, MainLayout mainLayout) {
		resetHomepage(personalConfig, I18NConstants.HOMEPAGE_RESTORE_PROBLEM_MODEL_INVALID, mainLayout);
	}

	private void resetHomepage(PersonalConfiguration personalConfig, ResKey detail, MainLayout mainLayout) {
		InfoService.showInfo(I18NConstants.HOMEPAGE_RESTORE_PROBLEM, detail);
		personalConfig.removeHomepage(mainLayout);
	}

	/**
	 * Fills {@link #getConfig()} from the given {@link MainLayout}.
	 *
	 * @param mainLayout
	 *        The {@link MainLayout} for this {@link HomepageImpl}.
	 * @param storeSelection
	 *        Whether the selection of the "leaf component" should be stored.
	 * 
	 * @see #restore(PersonalConfiguration, MainLayout)
	 */
	public void fillConfigFrom(MainLayout mainLayout, boolean storeSelection) {
		RelevantComponentFinder channelFinder = new RelevantComponentFinder(storeSelection);
		mainLayout.acceptVisitorRecursively(channelFinder);

		Map<LayoutComponent, Set<ComponentChannel>> relevantChannels = channelFinder.relevantComponents();
		Map<LayoutComponent, Set<LayoutComponent>> dependentComponents = channelFinder.dependentComponents();

		List<LayoutComponent> sortedComponents =
			CollectionUtil.topsort(c -> componentDependencies(dependentComponents, c), relevantChannels.keySet(), false);

		Homepage config = getConfig();
		config.setMainLayout(mainLayout.getLocation());

		for (LayoutComponent component : sortedComponents) {
			addPath(relevantChannels, config, component);
		}
	}

	private void addPath(Map<LayoutComponent, Set<ComponentChannel>> relevantChannels, Homepage config,
			LayoutComponent component) {
		Path newPath = newPath(component, relevantChannels.get(component));
		if (newPath != null) {
			config.getComponentPaths().add(newPath);
		}
	}

	/**
	 * Creates a {@link Homepage#getComponentPaths() component path} entry for the given component
	 * and the given channels.
	 *
	 * @param component
	 *        Element in the component tree.
	 * @param channels
	 *        The channels whose values must be stored. May be empty.
	 * 
	 * @return May be <code>null</code> when no entry is stored for any reason.
	 */
	protected Path newPath(LayoutComponent component, Set<ComponentChannel> channels) {
		Path path = TypedConfiguration.newConfigItem(Path.class);
		path.setComponent(component.getName());
		for (ComponentChannel channel : channels) {
			addChannel(path, component, channel);
		}
		return path;
	}

	private void addChannel(Path path, LayoutComponent component, ComponentChannel channel) {
		ChannelValue channelValue = newChannelValue(component, channel);
		if (channelValue != null) {
			path.getChannelValues().put(channelValue.getName(), channelValue);
		}
	}

	/**
	 * Creates a {@link Path#getChannelValues() channel} entry for the given component and the given
	 * channel.
	 *
	 * @param component
	 *        Element in the component tree.
	 * @param channel
	 *        The channel whose value must be stored.
	 * 
	 * @return May be <code>null</code> when no entry is stored for any reason.
	 */
	protected ChannelValue newChannelValue(LayoutComponent component, ComponentChannel channel) {
		ModelName value = extractTargetObjectFromModel(channel.get());
		if (value == null) {
			return null;
		}
		ChannelValue channelValue = TypedConfiguration.newConfigItem(ChannelValue.class);
		channelValue.setName(channel.name());
		channelValue.setValue(value);
		return channelValue;
	}

	private Set<LayoutComponent> componentDependencies(Map<LayoutComponent, Set<LayoutComponent>> dependentComponents,
			LayoutComponent component) {

		Set<LayoutComponent> channelDependencies = dependentComponents.getOrDefault(component, Collections.emptySet());

		LayoutComponent parent = component.getParent();
		if (parent == null) {
			return channelDependencies;
		}
		if (channelDependencies.isEmpty()) {
			return Collections.singleton(parent);
		}
		Set<LayoutComponent> dependencies = new HashSet<>(channelDependencies);
		dependencies.add(parent);
		return dependencies;
	}

	/**
	 * Extracts the wrapper from the model if available.
	 */
	protected ModelName extractTargetObjectFromModel(Object model) {
		return ModelResolver.buildModelNameIfAvailable(model).getElse(null);
	}

}
