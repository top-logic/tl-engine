/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ContainerControl;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.ContextMenuLayoutControlProvider;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutFactory;
import com.top_logic.layout.structure.WrappingControl;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.AbstractTileLayoutVisitor;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.ContextTileGroup;
import com.top_logic.mig.html.layout.tiles.InlinedTile;
import com.top_logic.mig.html.layout.tiles.TileGroup;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileLayoutVisitor;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.component.ContainerComponentTile;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TileLayoutListener;

/**
 * {@link DecoratingLayoutControlProvider} for a {@link TileContainerComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileContainerControlProvider extends ContextMenuLayoutControlProvider<TileContainerControlProvider> {

	private static final Property<LayoutControl> EXISTING_CONTROL =
		TypedAnnotatable.property(LayoutControl.class, "root tile control");

	/**
	 * {@link TileLayoutVisitor} dropping all {@link ToolBar} in the whole {@link LayoutComponent}
	 * tree in the visited {@link TileLayout}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static final class DropToolbarVisitor extends DefaultDescendingLayoutVisitor
			implements TileLayoutVisitor<Void, TileContainerComponent> {

		private static final Property<ToolBar> CONTAINER_TOOLBAR = TypedAnnotatable.property(ToolBar.class, "containerToolbar");

		@Override
		public Void visitTileRef(TileRef value, TileContainerComponent arg) {
			LayoutComponent tileComponent = arg.getTileComponent(value);

			ThreadContextManager.getInteraction().set(CONTAINER_TOOLBAR, arg.getToolBar());
			tileComponent.acceptVisitorRecursively(this);
			return value.getContentTile().visit(this, arg);
		}

		@Override
		public Void visitInlinedTile(InlinedTile value, TileContainerComponent arg) {
			LayoutComponent tileComponent = arg.getTileComponent(value);

			ThreadContextManager.getInteraction().set(CONTAINER_TOOLBAR, arg.getToolBar());
			tileComponent.acceptVisitorRecursively(this);
			return null;
		}

		@Override
		public Void visitTileGroup(TileGroup value, TileContainerComponent arg) {
			return visitCompositeTile(value, arg);
		}

		@Override
		public Void visitContextTileGroup(ContextTileGroup value, TileContainerComponent arg) {
			return visitCompositeTile(value, arg);
		}

		@Override
		public Void visitCompositeTile(CompositeTile value, TileContainerComponent arg) {
			value.getTiles().forEach(tile -> tile.visit(this, arg));
			return null;
		}

		@Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
			ToolBar formerToolbar = aComponent.getToolBar();
			if (formerToolbar == null) {
				// nothing to do here
				return true;
			}
			if (formerToolbar == ThreadContextManager.getInteraction().get(CONTAINER_TOOLBAR)) {
				/* Remove toolbar, because component becomes invisible and the commands must
				 * disappear from the toolbar. The toolbar is reinstalled later. */
				aComponent.setToolBar(null);
				return true;
			} else {
				/* Component (and all children) have own toolbar. No need to reset it or descend. */
				return false;
			}
		}

	}

	static final TileLayoutVisitor<Control, TileContainerComponent> CREATE_CHILD =
		new AbstractTileLayoutVisitor<>() {

			@Override
			public Control visitTileRef(TileRef value, TileContainerComponent arg) {
				LayoutComponent tileComponent = arg.getTileComponent(value);
				LayoutFactory layoutFactory = tileComponent.getMainLayout().getLayoutFactory();
				return layoutFactory.createLayout(tileComponent, arg.getToolBar());
			}

			@Override
			public Control visitInlinedTile(InlinedTile value, TileContainerComponent arg) {
				LayoutComponent tileComponent = arg.getTileComponent(value);
				LayoutFactory layoutFactory = tileComponent.getMainLayout().getLayoutFactory();
				return layoutFactory.createLayout(tileComponent, arg.getToolBar());
			}

			@Override
			public Control visitCompositeTile(CompositeTile value, TileContainerComponent arg) {
				LayoutControlAdapter control =
					new LayoutControlAdapter(
						new CompositeTileControl(new ContainerComponentTile(arg, value)));
				/* Ensure that control creates scrollbars, when not all tiles can be displayed on
				 * the screen. */
				control.setConstraint(DefaultLayoutData.DEFAULT_CONSTRAINT);
				return control;
			}
		};

	static final TileLayoutVisitor<Void, TileContainerComponent> DROP_TOOLBAR = new DropToolbarVisitor();

	/**
	 * Creates a new {@link TileContainerControlProvider}.
	 */
	public TileContainerControlProvider(InstantiationContext context, Config<TileContainerControlProvider> config) {
		super(context, config);
	}

	@Override
	public Control mkLayout(Strategy strategy, LayoutComponent component) {
		/* When the control is created then an listener is registered at the given component. There
		 * is no time at which the listener can be removed, therefore the control must be cached and
		 * reused.
		 * 
		 * The first attempt was that the control adds a listener to the component when it is
		 * attached, and removes the listener when it is detached. The problem is, that attaching
		 * happens in rendering phase in which no events must be sent. But when the correct view is
		 * created, the corresponding component will install the surrounding toolbar which is
		 * already rendered. installing the toolbar would trigger events which leads to errors. */
		LayoutControl existingControl = component.get(EXISTING_CONTROL);
		if (existingControl != null) {
			removeParent(existingControl);
			return existingControl;
		}
		LayoutControl control = createContainerControl(component);
		component.set(EXISTING_CONTROL, control);
		return control;
	}

	/**
	 * Must remove parent of existing control manually, because setting a different parent leads to
	 * errors.
	 */
	private void removeParent(LayoutControl existingControl) {
		LayoutControl parent = existingControl.getParent();
		if (parent instanceof WrappingControl<?>) {
			((WrappingControl<?>) parent).setChildControl(null);
		} else if (parent instanceof ContainerControl<?>) {
			List<LayoutControl> newChildren = new ArrayList<>(parent.getChildren());
			newChildren.remove(existingControl);
			((ContainerControl<?>) parent).setChildren(newChildren);
		}
	}

	private LayoutControl createContainerControl(LayoutComponent component) {
		ContentControl control = createContentWithMenu(component);
		TileContainerComponent container = (TileContainerComponent) component;
		container.addListener(TileLayoutListener.TILE_LAYOUT_CHANGED, new TileLayoutListener() {

			@Override
			public Bubble layoutChanged(TileContainerComponent sender, TileLayout oldValue, TileLayout newValue) {
				oldValue.visit(DROP_TOOLBAR, sender);
				control.setView(newValue.visit(CREATE_CHILD, sender));
				control.requestRepaint();
				return Bubble.BUBBLE;
			}
		});

		control.setView(container.displayedLayout().visit(CREATE_CHILD, container));
		return control;
	}

}

