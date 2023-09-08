/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.TileDefinition;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;

/**
 * {@link ComponentResolver} adding a {@link TileLayoutListener} to the resolved component.
 * 
 * <p>
 * When the displayed {@link TileLayout} is changed from a inner {@link TileLayout} to a
 * {@link TileRef}, then the selection of the component of that {@link TileRef} is cleared, to be
 * able to select the former selection again.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UnselectSelectable extends ComponentResolver {

	private static class UnselectListener implements TileLayoutListener {

		public UnselectListener() {
		}

		@Override
		public Bubble layoutChanged(TileContainerComponent sender, TileLayout oldValue, TileLayout newValue) {
			Object l = InlineList.newInlineList();
			TileLayout tmp = oldValue;
			while (true) {
				tmp = TileUtils.getContainer(tmp);
				if (tmp == null) {
					/* New value is not an ancestor of old value. Not stepping out of tile. */
					return Bubble.BUBBLE;
				}
				if (tmp instanceof TileRef) {
					l = InlineList.add(TileRef.class, l, (TileRef) tmp);
				}
				if (tmp == newValue) {
					break;
				}
			}
			InlineList.forEach(TileRef.class, l, tile -> unselect(sender, tile));

			return Bubble.BUBBLE;
		}

		private void unselect(TileContainerComponent sender, TileRef tileRef) {
			TileDefinition definition = sender.getReferencedDefinition(tileRef);
			if (definition.getTiles().isEmpty()) {
				/* No inner tiles possible. No need to reset something */
				return;
			}

			LayoutComponent tileComponent = sender.getTileComponent(tileRef);
			Selectable firstSelectable = getFirstSelectable(tileComponent);
			if (firstSelectable != null) {
				firstSelectable.clearSelection();
			}

		}

		private Selectable getFirstSelectable(LayoutComponent tileComponent) {
			if (tileComponent instanceof Selectable) {
				return (Selectable) tileComponent;
			}
			class Visitor extends DefaultDescendingLayoutVisitor {

				Selectable _firstSelectable = null;
				
				@Override
				public boolean visitLayoutComponent(LayoutComponent aComponent) {
					if (_firstSelectable != null) {
						return false;
					}
					if (aComponent.openedAsDialog()) {
						// Clearing Selectable's in dialog seems to make no sense.
						return false;
					}
					if (aComponent instanceof Selectable) {
						_firstSelectable = (Selectable) aComponent;
						return false;
					}
					return super.visitLayoutComponent(aComponent);
				}

			}
			Visitor v = new Visitor();
			tileComponent.acceptVisitorRecursively(v);
			return v._firstSelectable;
		}

	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		component.addListener(TileLayoutListener.TILE_LAYOUT_CHANGED, new UnselectListener());
	}

}

