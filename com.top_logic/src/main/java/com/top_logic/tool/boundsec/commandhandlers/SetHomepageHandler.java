/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.knowledge.wrap.person.Homepage;
import com.top_logic.knowledge.wrap.person.Homepage.ChannelValue;
import com.top_logic.knowledge.wrap.person.Homepage.Path;
import com.top_logic.knowledge.wrap.person.NoStartPageAutomatismExecutability;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link AbstractCommandHandler} setting the personal homepage.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
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
		RelevantComponentFinder channelFinder = new RelevantComponentFinder(config().isStoreSelection());
		mainLayout.acceptVisitorRecursively(channelFinder);
		Map<LayoutComponent, Set<String>> relevantChannels = channelFinder.relevantComponents();

		List<LayoutComponent> sortedComponents =
			CollectionUtil.topsort(this::componentDependencies, relevantChannels.keySet(), false);

		Homepage homepage = TypedConfiguration.newConfigItem(Homepage.class);
		homepage.setMainLayout(mainLayout.getLocation());

		for (LayoutComponent component : sortedComponents) {
			addPath(homepage, component, relevantChannels.get(component));
		}

		return homepage;
	}

	private void addPath(Homepage homepage, LayoutComponent component, Set<String> channels) {
		homepage.getComponentPaths().add(newPath(component, channels));
	}

	private Path newPath(LayoutComponent component, Set<String> channels) {
		Path path = TypedConfiguration.newConfigItem(Path.class);
		path.setComponent(component.getName());
		for (String channel : channels) {
			addChannel(path, component, channel);
		}
		return path;
	}

	private void addChannel(Path path, LayoutComponent component, String channelName) {
		// Channel must exist.
		ComponentChannel channel = component.getChannel(channelName);
		
		ModelName value = extractTargetObjectFromModel(channel.get());
		if (value == null) {
			return;
		}
		ChannelValue channelValue = TypedConfiguration.newConfigItem(ChannelValue.class);
		channelValue.setName(channelName);
		channelValue.setValue(value);
		path.getChannelValues().put(channelName, channelValue);
	}

	private Set<LayoutComponent> componentDependencies(LayoutComponent component) {
		LayoutComponent parent = component.getParent();
		if (parent == null) {
			return Collections.emptySet();
		}
		if (parent instanceof LayoutContainer) {
			Set<LayoutComponent> dependencies = new HashSet<>();
			dependencies.add(parent);
			/* Component which are displayed "left" of the components are also dependent */
			List<LayoutComponent> childList = ((LayoutContainer) parent).getChildList();
			int componentIdx = childList.indexOf(component);
			if (componentIdx != -1) {
				dependencies.addAll(childList.subList(0, componentIdx));
			}
			return dependencies;
		}
		return Collections.singleton(parent);
	}

	/**
	 * Extracts the wrapper from the model if available.
	 */
	protected ModelName extractTargetObjectFromModel(Object model) {
		return ModelResolver.buildModelNameIfAvailable(model).getElse(null);
	}

	/**
	 * Visitor searching for the components to include in the homepage.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class RelevantComponentFinder extends DefaultDescendingLayoutVisitor {

		private final Map<LayoutComponent, Set<String>> _relevantComponents = new HashMap<>();

		private final boolean _storeSelection;

		/**
		 * Creates a new {@link RelevantComponentFinder}.
		 * 
		 * @param storeSelection
		 *        Whether the selection of the leaf components is relevant.
		 */
		public RelevantComponentFinder(boolean storeSelection) {
			_storeSelection = storeSelection;
		}

		@Override
		public boolean visitLayoutComponent(LayoutComponent component) {
			if (!component.isVisible()) {
				return false;
			}
			addRelevantSourceChannels(component, component.getChannel(ModelChannel.NAME).sources());
			addTabComponentChild(component);
			if (_storeSelection) {
				addAdditionalSelection(component);
			}
			return super.visitLayoutComponent(component);
		}

		private void addAdditionalSelection(LayoutComponent component) {
			if (component instanceof LayoutContainer) {
				return;
			}
			String selectionChannelName = SelectionChannel.NAME;
			ComponentChannel selectionChannel = component.getChannelOrNull(selectionChannelName);
			if (selectionChannel == null) {
				return;
			}
			MultiMaps.add(relevantComponents(), component, selectionChannelName);
		}

		private void addTabComponentChild(LayoutComponent component) {
			LayoutComponent parent = component.getParent();
			if (parent instanceof TabComponent) {
				/* Mark component as relevant, also if no channel is relevant to ensure correct tab
				 * structure. */
				relevantComponents().computeIfAbsent(component, unused -> new HashSet<>());
			}
		}

		private void addRelevantSourceChannels(LayoutComponent component, Collection<ComponentChannel> sources) {
			for (ComponentChannel source : sources) {
				LayoutComponent sourceComponent = source.getComponent();
				if (sourceComponent == component) {
					addRelevantSourceChannels(component, source.sources());
				} else {
					String sourceChannelName = source.name();
					ComponentChannel existingChannel = sourceComponent.getChannelOrNull(sourceChannelName);
					if (existingChannel != null) {
						// Channel can be resolved later
						MultiMaps.add(relevantComponents(), sourceComponent, sourceChannelName);
					} else {
						// Channel may be a transforming channel with generated name.
						addRelevantSourceChannels(component, source.sources());
					}
				}
			}
		}

		/**
		 * The found relevant channels.
		 */
		public Map<LayoutComponent, Set<String>> relevantComponents() {
			return _relevantComponents;
		}

	}

}
