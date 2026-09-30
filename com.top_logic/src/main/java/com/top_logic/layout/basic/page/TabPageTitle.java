/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.page;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent.DisplayPathListener;
import com.top_logic.mig.html.layout.tiles.breadcrumb.RootTileBreadcrumbControlProvider;
import com.top_logic.util.Resources;

/**
 * {@link PageTitleResolver} that derives the browser page title from the surrounding tile or tab.
 *
 * @implNote
 *           <p>
 *           For a component displayed inside a {@link RootTileComponent}, the title is the label of
 *           the {@link RootTileComponent#displayedPath() displayed} ancestor as it would appear in
 *           the tile breadcrumb (see
 *           {@link RootTileBreadcrumbControlProvider#tileBreadcrumbLabel(LayoutComponent)}).
 *           Otherwise, the title is the label of the tab card carrying the component.
 *           </p>
 *
 *           <p>
 *           The title only follows the surrounding context while the component is visible: when the
 *           component becomes invisible the {@link com.top_logic.mig.html.layout.MainLayout
 *           MainLayout} default title is restored. For tile navigation, the title is also updated
 *           on {@link RootTileComponent#DISPLAYED_PATH_PROPERTY displayed-path} changes so that
 *           drilling deeper or stepping back updates the browser title in sync with the breadcrumb.
 *           </p>
 */
public class TabPageTitle extends PageTitleResolver<PolymorphicConfiguration<?>> {

	/**
	 * Creates a {@link TabPageTitle} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TabPageTitle(InstantiationContext context, PolymorphicConfiguration<?> config) {
		super(context, config);
	}

	@Override
	public void resolveComponent(InstantiationContext context, final LayoutComponent component) {
		Claim claim = new Claim(component);
		component.addListener(LayoutComponent.VISIBILITY_EVENT, claim);
		RootTileComponent rootTile = RootTileComponent.getRootTile(component);
		if (rootTile != null) {
			rootTile.addListener(RootTileComponent.DISPLAYED_PATH_PROPERTY, claim);
		}
		if (component.isVisible()) {
			claim.applyTitle();
		}
	}

	/**
	 * Computes the title for the given component from its surrounding tile or tab.
	 *
	 * @param component
	 *        The {@link LayoutComponent} carrying the resolver.
	 * @return The label of the displayed tile ancestor, or of the surrounding tab card; or
	 *         <code>null</code> if neither applies.
	 */
	static String titleFor(LayoutComponent component) {
		LayoutComponent top = RootTileComponent.getDisplayedComponent(component);
		if (top != null) {
			return RootTileBreadcrumbControlProvider.tileBreadcrumbLabel(top);
		}
		LayoutComponent ancestor = component;
		while (true) {
			LayoutComponent parent = ancestor.getParent();
			if (parent == null) {
				return null;
			}
			if (parent instanceof RootTileComponent) {
				return null;
			} else if (parent instanceof TabComponent tab) {
				int index = tab.getIndexOfChild(ancestor);
				if (index >= 0) {
					TabbedLayoutComponent card = tab.getCard(index);
					return Resources.getInstance().getString(card.getCardInfo().getLabelKey());
				}
				return null;
			} else {
				ancestor = parent;
			}
		}
	}

	/**
	 * {@link PageTitleResolver.Claim} that recomputes the title on
	 * {@link RootTileComponent#DISPLAYED_PATH_PROPERTY displayed-path} changes in addition to
	 * visibility changes.
	 */
	private static class Claim extends PageTitleResolver.Claim implements DisplayPathListener {

		Claim(LayoutComponent component) {
			super(component);
		}

		@Override
		public void handleDisplayedPathChanged(RootTileComponent sender, List<LayoutComponent> oldPath,
				List<LayoutComponent> newPath) {
			if (_component.isVisible()) {
				applyTitle();
			}
		}

		@Override
		protected String computeTitle() {
			return titleFor(_component);
		}

	}

}
