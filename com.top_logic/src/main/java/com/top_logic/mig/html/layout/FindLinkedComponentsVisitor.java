/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Visitor to find the linked {@link LayoutComponent}s by a given component.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class FindLinkedComponentsVisitor extends DefaultDescendingLayoutVisitor {

	private Map<LayoutComponent, Collection<LayoutComponent>> _linkedComponentsByComponent = new HashMap<>();

	@Override
	public boolean visitLayoutComponent(LayoutComponent component) {
		_linkedComponentsByComponent.put(component, getLinkedComponents(component));

		return super.visitLayoutComponent(component);
	}

	/**
	 * Computes all linked {@link LayoutComponent}s by a given component.
	 */
	public Map<LayoutComponent, Collection<LayoutComponent>> getLinkedComponentsByComponent() {
		return _linkedComponentsByComponent;
	}

	private Collection<LayoutComponent> getLinkedComponents(LayoutComponent component) {
		return component.getChannelNames()
			.stream()
			.map(channelName -> component.getChannel(channelName))
			.flatMap(channel -> channel.destinations().stream())
			.map(channel -> channel.getComponent())
			.collect(Collectors.toSet());
	}

}
