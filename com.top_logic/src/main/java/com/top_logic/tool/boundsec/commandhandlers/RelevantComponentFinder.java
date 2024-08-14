/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * Visitor searching for the components to include in the homepage.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RelevantComponentFinder extends DefaultDescendingLayoutVisitor {

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
		if (component.getDialogParent() != null) {
			/* Skip dialogs. */
			return false;
		}
		addRelevantSourceChannels(component, component.getChannel(ModelChannel.NAME).sources());
		addTabComponentChild(component);
		if (isStoreSelection()) {
			addAdditionalSelection(component);
		}
		return super.visitLayoutComponent(component);
	}

	/**
	 * Adds the selection of the given component to {@link #relevantComponents()} if possible.
	 */
	protected void addAdditionalSelection(LayoutComponent component) {
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

	/**
	 * Adds the direct children of a {@link TabComponent} to {@link #relevantComponents()}.
	 */
	protected void addTabComponentChild(LayoutComponent component) {
		LayoutComponent parent = component.getParent();
		if (parent instanceof TabComponent) {
			/* Mark component as relevant, also if no channel is relevant to ensure correct tab
			 * structure. */
			relevantComponents().computeIfAbsent(component, unused -> new HashSet<>());
		}
	}

	/**
	 * Adds the the given component and a source channel to {@link #relevantComponents()} if
	 * necessary.
	 */
	protected void addRelevantSourceChannels(LayoutComponent component, Collection<ComponentChannel> sourceChannels) {
		for (ComponentChannel source : sourceChannels) {
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

	/**
	 * Whether the selection must be stored for leaf components.
	 */
	public boolean isStoreSelection() {
		return _storeSelection;
	}

}