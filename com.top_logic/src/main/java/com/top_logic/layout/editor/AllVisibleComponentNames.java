/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.layout.basic.component.BreadcrumbComponent;
import com.top_logic.layout.editor.components.ComponentPlaceholder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.xml.LayoutControlComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.LayoutList;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.PageComponent;
import com.top_logic.mig.html.layout.tiles.DisplayedTilesVisitor;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;

/**
 * Computes all names of possible component that are visible in the current {@link MainLayout}.
 * 
 * A set of given components are excluded:
 * 
 * <ul>
 * <li>{@link BreadcrumbComponent}</li>
 * <li>{@link LayoutControlComponent}</li>
 * <li>{@link ComponentPlaceholder}</li>
 * <li>{@link LayoutList}</li>
 * </ul>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllVisibleComponentNames extends AbstractComponentNameCollector {

	/**
	 * Creates a {@link AllVisibleComponentNames}.
	 */
	@CalledByReflection
	public AllVisibleComponentNames(DeclarativeFormOptions options) {
		super(options);
	}

	@Override
	protected void collect(List<LayoutComponent> result) {
		LayoutComponent dialog = getContextComponent().getDialogTopLayout();
		if (dialog != null) {
			// Unconditionally add the dialog top layout, since this component is used for
			// communicating the model to the outside.
			result.add(dialog);
			collectChildren(result, dialog);
		}

		collect(result, getMainLayout(), true);
	}

	private void collect(List<LayoutComponent> result, LayoutComponent component, boolean onlyVisible) {
		if (isChoosableComponent(component, onlyVisible)) {
			result.add(component);
		}
		collectChildren(result, component);
	}

	private void collectChildren(List<LayoutComponent> result, LayoutComponent component) {
		if (component instanceof RootTileComponent) {
			RootTileComponent rootTile = (RootTileComponent) component;
			new DisplayedTilesVisitor() {
				@Override
				public boolean visitLayoutComponent(LayoutComponent aComponent) {
					if (isChoosableComponent(aComponent, false)) {
						result.add(aComponent);
					}
					return super.visitLayoutComponent(aComponent);
				}
			}.visitRootTile(rootTile);
		} else if (component instanceof LayoutContainer) {
			LayoutContainer container = (LayoutContainer) component;
			for (LayoutComponent child : container.getVisibleChildren()) {
				collect(result, child, true);
			}
		}
	}

	/**
	 * Whether the given component should be present in the result list.
	 * 
	 * @param onlyVisible
	 *        Whether only visible components are regarded.
	 */
	protected boolean isChoosableComponent(LayoutComponent component, boolean onlyVisible) {
		if (component == getContextComponent()
			|| (onlyVisible && !component.isVisible())
			|| component.getChannelNames().isEmpty()) {
			return false;
		}

		return !(component instanceof BreadcrumbComponent)
			&& !(component instanceof LayoutControlComponent)
			&& !(component instanceof LayoutContainer && !(component instanceof InlinedTileComponent))
			&& !(component instanceof ComponentPlaceholder)
			&& !(component instanceof PageComponent);
	}

}
