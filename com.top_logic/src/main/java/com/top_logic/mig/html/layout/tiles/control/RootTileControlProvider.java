/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.control;

import java.util.List;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.structure.ComponentWrappingControl;
import com.top_logic.layout.structure.ContextMenuLayoutControlProvider;
import com.top_logic.layout.structure.DecoratingLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutFactory;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.ChildrenChangedListener;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent.DisplayPathListener;

/**
 * {@link DecoratingLayoutControlProvider} for a {@link RootTileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RootTileControlProvider extends ContextMenuLayoutControlProvider<RootTileControlProvider> {

	private static final Property<Runnable> CLEANUP_ACTION =
		TypedAnnotatable.property(Runnable.class, "cleanup action", () -> {

			// Nothing to do

		});

	/**
	 * {@link DefaultDescendingLayoutVisitor} dropping a given {@link ToolBar} from all
	 * {@link LayoutComponent}s in the visited tree.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	static final class DropToolbarVisitor extends DefaultDescendingLayoutVisitor {

		/** Singleton {@link DropToolbarVisitor} instance. */
		public static final DropToolbarVisitor INSTANCE = new DropToolbarVisitor();

		/**
		 * Creates a new {@link DropToolbarVisitor}.
		 */
		protected DropToolbarVisitor() {
			// singleton instance
		}

		private static final Property<ToolBar> TOOLBAR = TypedAnnotatable.property(ToolBar.class, "toolbar");

		@Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
			ToolBar formerToolbar = aComponent.getToolBar();
			if (formerToolbar == null) {
				// nothing to do here
				return true;
			}
			if (formerToolbar == ThreadContextManager.getInteraction().get(TOOLBAR)) {
				/* Remove toolbar, because component becomes invisible and the commands must
				 * disappear from the toolbar. The toolbar is reinstalled later. */
				aComponent.setToolBar(null);
				return true;
			} else {
				/* Component (and all children) have own toolbar. No need to reset it or descend. */
				return false;
			}
		}

		public void drop(LayoutComponent root, ToolBar toolBar) {
			ThreadContextManager.getInteraction().set(TOOLBAR, toolBar);
			root.acceptVisitorRecursively(this);
		}

	}

	/**
	 * Creates a new {@link RootTileControlProvider}.
	 */
	public RootTileControlProvider(InstantiationContext context, Config<RootTileControlProvider> config) {
		super(context, config);
	}

	@Override
	public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
		/* When the control is created then an listener is registered at the given component. There
		 * is no time at which the listener can be removed, therefore cleanup actions must be
		 * created and cached.
		 * 
		 * The first attempt was that the control adds a listener to the component when it is
		 * attached, and removes the listener when it is detached. The problem is, that attaching
		 * happens in rendering phase in which no events must be sent. But when the correct view is
		 * created, the corresponding component will install the surrounding toolbar which is
		 * already rendered. installing the toolbar would trigger events which leads to errors. */
		component.get(CLEANUP_ACTION).run();
		
		return createContainerControl(component);
	}

	private LayoutControl createContainerControl(LayoutComponent component) {
		RootTileComponent rootComponent = (RootTileComponent) component;
		ComponentWrappingControl control =
			ComponentWrappingControl.create(component, contextMenu().createContextMenuProvider(component));
		DisplayPathListener displayPathListener = new DisplayPathListener() {

			@Override
			public void handleDisplayedPathChanged(RootTileComponent sender, List<LayoutComponent> oldPath,
					List<LayoutComponent> newPath) {
				ToolBar toolBar = sender.getToolBar();
				if (!oldPath.isEmpty()) {
					LayoutComponent displayedComponent = oldPath.get(oldPath.size() - 1);
					if (displayedComponent != null) {
						DropToolbarVisitor.INSTANCE.drop(displayedComponent, toolBar);
					} else {
						// Component may already be dropped from layout tree.
					}
				}
				setView(control, sender, newPath);
			}
		};
		ChildrenChangedListener childrenChangeListener = new ChildrenChangedListener() {

			@Override
			public Bubble notifyChildrenChanged(LayoutContainer sender, List<LayoutComponent> oldChildren,
					List<LayoutComponent> newValue) {
				List<LayoutComponent> displayedPath = rootComponent.displayedPath();
				if (displayedPath.isEmpty()) {
					return Bubble.BUBBLE;
				}
				LayoutComponent displayedComponent = displayedPath.get(displayedPath.size() - 1);
				LayoutComponent changed = sender;
				do {
					/* A child of the displayed component has be been changed. As all children gets
					 * the toolbar of the "root tile control", the view must be updated. */
					if (changed == displayedComponent) {
						/* Create view deferred: If the children have changed due to a
						 * re-configuration of the parent, it is possible that the new children are
						 * not resolved yet. In this case, the commands would not be created and no
						 * commands would be added to the control's toolbar. */
						displayedComponent.getMainLayout().getLayoutContext().notifyInvalid(context -> {
							setView(control, rootComponent, displayedPath);
						});
						break;
					}
					changed = changed.getParent();
				} while (changed != null);
				return Bubble.BUBBLE;
			}

		};
		rootComponent.addListener(RootTileComponent.DISPLAYED_PATH_PROPERTY, displayPathListener);
		rootComponent.addListener(RootTileComponent.CHILDREN_PROPERTY, childrenChangeListener);
		rootComponent.set(CLEANUP_ACTION, () -> {
			rootComponent.removeListener(RootTileComponent.CHILDREN_PROPERTY, childrenChangeListener);
			rootComponent.removeListener(RootTileComponent.DISPLAYED_PATH_PROPERTY, displayPathListener);
		});

		setView(control, rootComponent, rootComponent.displayedPath());
		return control;
	}

	void setView(ComponentWrappingControl control, RootTileComponent rootTile, List<LayoutComponent> displayPath) {
		control.setChildControl(createView(rootTile, displayPath));
		control.requestRepaint();
	}

	private LayoutControl createView(RootTileComponent sender, List<LayoutComponent> displayedPath) {
		MainLayout mainLayout = sender.getMainLayout();
		LayoutFactory layoutFactory = mainLayout.getLayoutFactory();

		if (displayedPath.isEmpty()) {
			LayoutComponent child = sender.getChild();
			if (child == null) {
				return new LayoutControlAdapter(Fragments.empty());
			}
			return layoutFactory.createLayout(child, sender.getToolBar());
		}
		LayoutComponent displayedComponent = displayedPath.get(displayedPath.size() - 1);
		return layoutFactory.createLayout(displayedComponent, sender.getToolBar());
	}

}

