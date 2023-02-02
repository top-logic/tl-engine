/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.ChildrenChangedListener;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;

/**
 * The class {@link DeckPaneControlProvider} creates a {@link DeckPaneControl} for the given {@link TabComponent}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckPaneControlProvider extends ConfiguredLayoutControlProvider<DeckPaneControlProvider.Config> {

	public interface Config extends PolymorphicConfiguration<DeckPaneControlProvider> {

	}

	public static final LayoutControlProvider INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
		.getInstance(TypedConfiguration.newConfigItem(Config.class));

	/**
	 * Creates a {@link DeckPaneControlProvider}.
	 */
	public DeckPaneControlProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
		assert component instanceof TabComponent;
		TabComponent tab = (TabComponent) component;
		DeckPaneControl deck = new DeckPaneControl(tab.getDeckPaneModel());
		deck.addListener(AbstractControlBase.ATTACHED_PROPERTY, new Listener(strategy, tab, deck));

		for (LayoutComponent child : tab.getChildList()) {
			deck.addChild(LayoutControlAdapter.wrap(strategy.createLayout(child)));
		}

		return deck;
	}

	private static class Listener implements ChildrenChangedListener, AttachedPropertyListener {

		private final TabComponent _tab;

		private final Strategy _strategy;

		private final DeckPaneControl _deck;

		Listener(Strategy strategy, TabComponent tab, DeckPaneControl deck) {
			_strategy = strategy;
			_tab = tab;
			_deck = deck;
		}

		@Override
		public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
			if (_deck != sender) {
				return;
			}
			if (newValue.booleanValue()) {
				_tab.addListener(LayoutContainer.CHILDREN_PROPERTY, this);
			} else {
				_tab.removeListener(LayoutContainer.CHILDREN_PROPERTY, this);
			}
		}

		@Override
		public Bubble notifyChildrenChanged(LayoutContainer sender, List<LayoutComponent> oldChildren,
				List<LayoutComponent> newValue) {
			if (sender != _tab) {
				return Bubble.BUBBLE;
			}
			/* It is currently not possible to reuse the existing controls of the deck, because
			 * removing a component from its parent causes the toolbar to be removed, which is just
			 * installed when a new control is created. */
			/* Null context toolbar causes the strategy to run the toolbar create heuristics */
			ToolBar noContextToolBar = null;
			List<LayoutControl> newControlChildren = newValue
				.stream()
				.map(component -> LayoutControlAdapter.wrap(_strategy.createLayout(component, noContextToolBar)))
				.collect(Collectors.toList());
			_deck.setChildren(newControlChildren);
			return Bubble.BUBBLE;
		}

	}

}
