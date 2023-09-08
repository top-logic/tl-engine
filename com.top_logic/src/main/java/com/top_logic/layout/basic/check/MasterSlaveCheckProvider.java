/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.form.FormHandlerUtil;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link CheckScopeProvider} delivering all components that are affected by a change of the current
 * selection in the component using the {@link MasterSlaveCheckProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MasterSlaveCheckProvider implements CheckScopeProvider {

	/**
	 * Singleton {@link MasterSlaveCheckProvider} instance.
	 */
	public static final MasterSlaveCheckProvider INSTANCE = new MasterSlaveCheckProvider();

	private MasterSlaveCheckProvider() {
		// Singleton constructor.
	}

	/**
	 * {@link CheckScope} containing all {@link LayoutComponent} that are affected by a selection
	 * change in the given component.
	 * 
	 * @see CheckScopeProvider#getCheckScope(LayoutComponent)
	 */
	@Override
	public CheckScope getCheckScope(final LayoutComponent component) {
		return new MasterSlaveCheck(component);
	}

	private static class MasterSlaveCheck implements CheckScope {

		private final LayoutComponent _component;

		public MasterSlaveCheck(LayoutComponent component) {
			this._component = component;
		}

		@Override
		public Collection<? extends ChangeHandler> getAffectedFormHandlers() {
			Set<LayoutComponent> allRelated = new HashSet<>();
			Set<ComponentChannel> seenChannels = new HashSet<>();
			List<LayoutComponent> pending = new ArrayList<>();
			pending.add(_component);
			for (int i = 0; i < pending.size(); i++) {
				LayoutComponent pendingComponent = pending.get(i);
				for (ComponentChannel related : pendingComponent.getRelatedChannels(SelectionChannel.NAME)) {
					LayoutComponent relatedComponent = related.getComponent();
					allRelated.add(relatedComponent);
					if (seenChannels.add(related)) {
						// not seen now
						if (related.equals(relatedComponent.modelChannel())) {
							/* related component is in some kind a "slave" of the pending component.
							 * When the model of the component affected, the selection may also be
							 * affected. */
							pending.add(relatedComponent);
						}
					}
				}
			}

			return allRelated.stream()
				.filter(c -> c != _component)
				.filter(FormHandlerUtil.getNeedsChangeCheckFilter())
				.map(c -> ((ChangeHandler) c))
				.collect(Collectors.toList());
		}
	}

}
