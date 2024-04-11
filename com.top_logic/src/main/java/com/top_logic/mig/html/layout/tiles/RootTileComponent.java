/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.title.TitleProvider;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.ChildrenChangedListener;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.SingleLayoutContainer;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent.ContentComponentChangedListener;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent.ContentDisplayedListener;
import com.top_logic.mig.html.layout.tiles.component.InlinedTileComponent;
import com.top_logic.mig.html.layout.tiles.control.RootTileControlProvider;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;

/**
 * A {@link RootTileComponent} is the main entry point to configure tiles in app.
 * 
 * <p>
 * The main components that can be configured in a {@link RootTileComponent} are the
 * {@link GroupTileComponent} to display a group of components as tiles, and the
 * {@link ContextTileComponent} to connect a {@link Selectable} component and a detail component. In
 * this case, when an object is selected in the selection component a step in the tile hierarchy is
 * descended and the detail component is displayed. This detail component can use the selection of
 * the selectable component as a model.
 * </p>
 * 
 * <p>
 * The {@link RootTileComponent} displays a breadbrumb showing the path to the currently visible
 * tile. The breadcrumb can also be used to navigate back to parent components.
 * </p>
 * 
 * @see GroupTileComponent
 * @see ContextTileComponent
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RootTileComponent extends SingleLayoutContainer implements BoundCheckerDelegate {

	/**
	 * Property annotated to the components that are displayed in the path of the
	 * {@link RootTileComponent}.
	 */
	private static final TypedAnnotatable.Property<RootTileComponent> ROOT_TILE =
		TypedAnnotatable.property(RootTileComponent.class, "belongs to root tile");

	/**
	 * {@link PropertyListener} to handle change of the displayed path in a
	 * {@link RootTileComponent}.
	 * 
	 * @see RootTileComponent#displayedPath()
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface DisplayPathListener extends PropertyListener {

		/**
		 * Handles the change of the displayed component.
		 * 
		 * @param sender
		 *        The {@link RootTileComponent} whose displayed path has changed.
		 * @param oldPath
		 *        Former path of the displayed components.
		 * @param newPath
		 *        Current path of the displayed component. The last component with is the actually
		 *        visible component.
		 */
		void handleDisplayedPathChanged(RootTileComponent sender, List<LayoutComponent> oldPath,
				List<LayoutComponent> newPath);

	}

	/**
	 * {@link EventType} to be informed about a change of the value {@link #displayedPath()}.
	 * 
	 * @see #addListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * @see #removeListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * @see DisplayPathListener
	 */
	public static final EventType<DisplayPathListener, RootTileComponent, List<LayoutComponent>> DISPLAYED_PATH_PROPERTY =
		new EventType<>("Displayed path") {
			@Override
			public Bubble dispatch(DisplayPathListener listener, RootTileComponent sender,
					List<LayoutComponent> oldValue, List<LayoutComponent> newValue) {
				listener.handleDisplayedPathChanged(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}
		};

	/**
	 * Configuration of a {@link RootTileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends SingleLayoutContainer.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Property name of {@link #getTitle()}. */
		String TITLE = "title";

		@Override
		@ItemDefault(RootTileControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/**
		 * The title is displayed in the toolbar of the configured component.
		 * 
		 * <p>
		 * Null means, that no title is displayed.
		 * </p>
		 */
		@Name(TITLE)
		PolymorphicConfiguration<TitleProvider> getTitle();

		/**
		 * {@link RootTileComponent} needs a {@link ToolBar} to render the title.
		 */
		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		@Override
		@FormattedDefault("false")
		Decision getShowMaximize();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			SingleLayoutContainer.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(StepOutCommand.COMMAND_ID);
		}

	}

	private final List<LayoutComponent> _displayedPath = new ArrayList<>();

	final ContentDisplayedListener _contentDisplayedListener = this::handleContentDisplayChanged;

	final ContentComponentChangedListener _contentComponentListener = this::handleContentComponentChanged;

	final ChannelListener _groupTileSelectionListener = this::handleGroupTileSelectionChanged;

	final ChannelListener _inlinedTileSelectionListener = this::handleInlinedTileSelectionChanged;

	private TitleProvider _titleProvider;

	private final BoundChecker _boundCheckerDelegate = new LayoutContainerBoundChecker<>(this);

	/**
	 * Creates a new {@link RootTileComponent}.
	 */
	public RootTileComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		_titleProvider = context.getInstance(getConfig().getTitle());
	}

	@Override
	protected void onSetToolBar(ToolBar oldValue, ToolBar newValue) {
		super.onSetToolBar(oldValue, newValue);
		if (_titleProvider != null && newValue != null) {
			newValue.setTitle(_titleProvider.createTitle(this));
		}
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Tile path to the currently displayed component.
	 * 
	 * <p>
	 * The returned path represents the currently displayed path in the tile hierarchy. The last
	 * component is the actual visible component. The first component is {@link #getChild()}.
	 * </p>
	 */
	public List<LayoutComponent> displayedPath() {
		return _displayedPath;
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		addListener(LayoutContainer.CHILDREN_PROPERTY, this::handleDescendantChildrenChanged);
	}

	/**
	 * Handles the children change of a {@link LayoutContainer} descendant.
	 * 
	 * @param sender
	 *        See
	 *        {@link ChildrenChangedListener#notifyChildrenChanged(LayoutContainer, List, List)}.
	 * @param oldChildren
	 *        See
	 *        {@link ChildrenChangedListener#notifyChildrenChanged(LayoutContainer, List, List)}.
	 * @param newValue
	 *        See
	 *        {@link ChildrenChangedListener#notifyChildrenChanged(LayoutContainer, List, List)}.
	 * @return See
	 *         {@link ChildrenChangedListener#notifyChildrenChanged(LayoutContainer, List, List)}.
	 */
	private Bubble handleDescendantChildrenChanged(LayoutContainer sender, List<LayoutComponent> oldChildren,
			List<LayoutComponent> newValue) {
		Set<LayoutComponent> added = new HashSet<>(newValue);
		added.removeAll(oldChildren);
		Set<LayoutComponent> removed = new HashSet<>(oldChildren);
		removed.removeAll(newValue);
		added.forEach(this::addSelectionListeners);
		removed.forEach(this::removeSelectionListeners);

		int firstRemovedIndex = 0;
		while (firstRemovedIndex < _displayedPath.size()) {
			LayoutComponent removeComponent = _displayedPath.get(firstRemovedIndex);
			if (removeComponent.getMainLayout() == null) {
				/* Displayed component was removed from layout tree. */
				break;
			}
			firstRemovedIndex++;
		}
		if (firstRemovedIndex == _displayedPath.size()) {
			// Removal of components does not affect this tile root.
			return Bubble.BUBBLE;
		}

		List<LayoutComponent> oldPath = copyDisplayedPath();
		clearDisplayedPathFrom(true, firstRemovedIndex);
		fireDisplayPathChanged(oldPath);
		return Bubble.BUBBLE;
	}

	private void clearDisplayedPathFrom(boolean deselectDisplayedComponent, int firstRemovedIndex) {
		assert firstRemovedIndex <= _displayedPath.size();
		if (deselectDisplayedComponent) {
			deselect(displayedPathSubList(firstRemovedIndex - 1));
		} else {
			// Deselect the now displayed component to ensure possibility of re-select.
			deselect(displayedPathSubList(firstRemovedIndex));
		}
		// De-selecting may drop elements from display path.
		if (firstRemovedIndex == _displayedPath.size()) {
			return;
		}

		List<LayoutComponent> componentsToRemove = displayedPathSubList(firstRemovedIndex);
		componentsToRemove.forEach(this::removeRootTile);
		componentsToRemove.clear();
	}

	private void setRootTile(LayoutComponent component) {
		component.set(ROOT_TILE, this);
	}

	private void removeRootTile(LayoutComponent component) {
		component.reset(ROOT_TILE);
	}

	private static RootTileComponent correspondingRoot(LayoutComponent component) {
		return component.get(ROOT_TILE);
	}

	private List<LayoutComponent> displayedPathSubList(int index) {
		return _displayedPath.subList(index, _displayedPath.size());
	}

	void addToDisplayPathAndFire(LayoutComponent newEndComponent) {
		List<LayoutComponent> oldPath = copyDisplayedPath();
		addToDisplayPath(newEndComponent);
		fireDisplayPathChanged(oldPath);
	}

	private void addToDisplayPath(LayoutComponent newEndComponent) {
		_displayedPath.add(newEndComponent);
		setRootTile(newEndComponent);
	}

	void fireDisplayPathChanged(List<LayoutComponent> oldPath) {
		if (equalsStartingFromEnd(oldPath, _displayedPath)) {
			return;
		}
		firePropertyChanged(DISPLAYED_PATH_PROPERTY, this, oldPath, _displayedPath);
	}

	private boolean equalsStartingFromEnd(List<?> l1, List<?> l2) {
		int size = l1.size();
		if (size != l2.size()) {
			return false;
		}
		assert l1 instanceof RandomAccess && l2 instanceof RandomAccess;
		for (int i = size-1; i >= 0; i--) {
			if (!Utils.equals(l1.get(i), l2.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Handles the change of {@link ContextTileComponent#isContentDisplayed()} of a
	 * {@link ContextTileComponent context tile child}.
	 * 
	 * @param sender
	 *        See
	 *        {@link ContentDisplayedListener#handleDisplayedChanged(ContextTileComponent, Boolean, Boolean)}.
	 * @param oldValue
	 *        See
	 *        {@link ContentDisplayedListener#handleDisplayedChanged(ContextTileComponent, Boolean, Boolean)}.
	 * @param newValue
	 *        See
	 *        {@link ContentDisplayedListener#handleDisplayedChanged(ContextTileComponent, Boolean, Boolean)}.
	 */
	private void handleContentDisplayChanged(ContextTileComponent sender, Boolean oldValue, Boolean newValue) {
		int indexOfChangedComponent = _displayedPath.lastIndexOf(getDisplayedComponent(sender));
		if (indexOfChangedComponent == -1) {
			// Display detail of invisible component has changed. Ignore.
			return;
		}
		LayoutComponent detailComponent = sender.getContent();
		if (indexOfChangedComponent == _displayedPath.size() - 1) {
			if (newValue.booleanValue()) {
				addToDisplayPathAndFire(detailComponent);
			}
		} else {
			int indexOfOldSelection = indexOfChangedComponent + 1;
			if (newValue.booleanValue()) {
				if (_displayedPath.get(indexOfOldSelection).equals(detailComponent)) {
					// Actually no change.
				} else {
					List<LayoutComponent> oldPath = copyDisplayedPath();
					clearDisplayedPathFrom(false, indexOfOldSelection);
					addRecursivelyToDisplayedPath(detailComponent);
					fireDisplayPathChanged(oldPath);
				}

			} else {
				List<LayoutComponent> oldPath = copyDisplayedPath();
				clearDisplayedPathFrom(true, indexOfOldSelection);
				fireDisplayPathChanged(oldPath);
			}
		}
	}

	/**
	 * Handles the change of {@link ContextTileComponent#getContent()} of a
	 * {@link ContextTileComponent context tile child}.
	 * 
	 * @param sender
	 *        See
	 *        {@link ContentComponentChangedListener#handleContentChanged(ContextTileComponent, LayoutComponent, LayoutComponent)}
	 * @param oldValue
	 *        See
	 *        {@link ContentComponentChangedListener#handleContentChanged(ContextTileComponent, LayoutComponent, LayoutComponent)}
	 * @param newValue
	 *        See
	 *        {@link ContentComponentChangedListener#handleContentChanged(ContextTileComponent, LayoutComponent, LayoutComponent)}
	 */
	private void handleContentComponentChanged(ContextTileComponent sender, LayoutComponent oldValue,
			LayoutComponent newValue) {
		int indexOfContext = _displayedPath.lastIndexOf(getDisplayedComponent(sender));
		if (indexOfContext == -1) {
			// Invisible content component has changed. Ignore.
			return;
		}
		if (indexOfContext == _displayedPath.size() - 1) {
			// selector is currently displayed. Ignore change of content.
			return;
		}

		List<LayoutComponent> oldPath = copyDisplayedPath();
		if (newValue != null) {
			clearDisplayedPathFrom(false, indexOfContext + 1);
			addToDisplayPath(newValue);
		} else {
			// clear also selection of newly displayed context component
			clearDisplayedPathFrom(true, indexOfContext + 1);
		}
		fireDisplayPathChanged(oldPath);
	}

	/**
	 * Handles the change of {@link InlinedTileComponent#getSelected()}.
	 * 
	 * @param sender
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}
	 * @param oldSelection
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}
	 * @param newSelection
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}
	 */
	private void handleInlinedTileSelectionChanged(ComponentChannel sender, Object oldSelection,
			Object newSelection) {
		LayoutComponent groupChild = getGroupAncestorChild(sender.getComponent());
		if (groupChild == null) {
			return;
		}
		GroupTileComponent group = (GroupTileComponent) groupChild.getParent();
		if (newSelection != null) {
			group.setSelected(groupChild);
		} else {
			group.setSelected(null);
		}
		return;

	}

	/**
	 * The direct child of the next {@link GroupTileComponent} ancestor.
	 * 
	 * @return <code>null</code> when there is no such component.
	 */
	private LayoutComponent getGroupAncestorChild(LayoutComponent sender) {
		while (true) {
			LayoutComponent parent = sender.getParent();
			if (parent == null) {
				// layoutComponent not part in a tile environment.
				return null;
			}
			if (parent instanceof RootTileComponent) {
				// layoutComponent not part of a group.
				return null;
			}
			if (parent instanceof GroupTileComponent) {
				// sender is direct child of a group.
				return sender;
			}
			sender = parent;
		}
	}

	/**
	 * Implementation of {@link ChannelListener} for {@link GroupTileComponent} descendant.
	 * 
	 * @param sender
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param oldSelection
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param newSelection
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 */
	void handleGroupTileSelectionChanged(ComponentChannel sender, Object oldSelection, Object newSelection) {
		int indexOfChangedComponent =
			_displayedPath.indexOf(((GroupTileComponent) sender.getComponent()).getDisplayedComponent());
		if (indexOfChangedComponent > -1) {
			LayoutComponent newSelectedComponent = (LayoutComponent) newSelection;
			if (indexOfChangedComponent == _displayedPath.size()-1) {
				if (newSelectedComponent != null) {
					addToDisplayPathAndFire(newSelectedComponent);
				}
			} else {
				int indexOfOldSelection = indexOfChangedComponent + 1;
				if (newSelectedComponent != null) {
					if (_displayedPath.get(indexOfOldSelection).equals(newSelectedComponent)) {
						// Actually no change.
					} else {
						List<LayoutComponent> oldPath = copyDisplayedPath();
						clearDisplayedPathFrom(false, indexOfOldSelection);
						addRecursivelyToDisplayedPath(newSelectedComponent);
						fireDisplayPathChanged(oldPath);
					}
				} else {
					List<LayoutComponent> oldPath = copyDisplayedPath();
					clearDisplayedPathFrom(true, indexOfOldSelection);
					fireDisplayPathChanged(oldPath);
				}
			}
		} else {
			// Selection of invisible component has changed. Ignore.
		}

	}

	private List<LayoutComponent> copyDisplayedPath() {
		int numberSections = _displayedPath.size();
		switch (numberSections) {
			case 0:
				return Collections.emptyList();
			case 1:
				return Collections.singletonList(_displayedPath.get(0));
			default:
				return Arrays.asList(_displayedPath.toArray(new LayoutComponent[numberSections]));
		}
	}

	private void addRecursivelyToDisplayedPath(LayoutComponent component) {
		addToDisplayPath(component);
		if (component instanceof GroupTileComponent) {
			LayoutComponent selectedComponent = ((GroupTileComponent) component).selectedComponent();
			if (selectedComponent != null) {
				addRecursivelyToDisplayedPath(selectedComponent);
			}
		} else {
			ContextTileComponent contextTile =
				TileComponentFinder.getFirstOfType(ContextTileComponent.class, component);
			if (contextTile != null && contextTile.isContentDisplayed()) {
				addRecursivelyToDisplayedPath(contextTile.getContent());
			}
		}
	}

	@Override
	protected void onSet(LayoutComponent oldChild) {
		super.onSet(oldChild);
		if (oldChild != null) {
			removeSelectionListeners(oldChild);
		}
		LayoutComponent newChild = getChild();
		if (newChild != null) {
			addSelectionListeners(newChild);

			List<LayoutComponent> oldPath = copyDisplayedPath();
			clearDisplayedPathFrom(false, 0);
			addRecursivelyToDisplayedPath(newChild);
			fireDisplayPathChanged(oldPath);
		}
	}

	private void removeSelectionListeners(LayoutComponent child) {
		child.acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent component) {
				if (component instanceof GroupTileComponent) {
					((GroupTileComponent) component).selectionChannel().removeListener(_groupTileSelectionListener);
				}
				if (component instanceof InlinedTileComponent) {
					((InlinedTileComponent) component).selectionChannel().removeListener(_inlinedTileSelectionListener);
				}
				if (component instanceof ContextTileComponent) {
					component.removeListener(ContextTileComponent.DETAIL_DISPLAYED_PROPERTY, _contentDisplayedListener);
					component.removeListener(ContextTileComponent.CONTENT_COMPONENT_PROPERTY,
						_contentComponentListener);
				}
				return super.visitLayoutComponent(component);
			}

		});
	}

	private void addSelectionListeners(LayoutComponent child) {
		child.acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent component) {
				if (component instanceof GroupTileComponent) {
					((GroupTileComponent) component).selectionChannel().addListener(_groupTileSelectionListener);
				}
				if (component instanceof InlinedTileComponent) {
					((InlinedTileComponent) component).selectionChannel().addListener(_inlinedTileSelectionListener);
				}
				if (component instanceof ContextTileComponent) {
					component.addListener(ContextTileComponent.DETAIL_DISPLAYED_PROPERTY, _contentDisplayedListener);
					component.addListener(ContextTileComponent.CONTENT_COMPONENT_PROPERTY,
						_contentComponentListener);
				}
				return super.visitLayoutComponent(component);
			}

		});
	}

	/**
	 * Forces this {@link RootTileComponent} to display a component on the {@link #displayedPath()}.
	 */
	public void displayTileLayout(LayoutComponent component) {
		int indexOf = _displayedPath.indexOf(component);
		if (indexOf == -1) {
			throw new IllegalArgumentException("Not part of the displayed part.");
		}
		if (indexOf == _displayedPath.size() - 1) {
			// Last element is currently displayed. Nothing to do.
			return;
		}

		List<LayoutComponent> oldPath = copyDisplayedPath();
		clearDisplayedPathFrom(true, indexOf + 1);
		fireDisplayPathChanged(oldPath);

	}

	private void deselect(List<LayoutComponent> displayedComponents) {
		List<Selectable> componentsToDeselect = new ArrayList<>();
		DefaultDescendingLayoutVisitor selectableCollector = new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (aComponent instanceof InlinedTileComponent) {
					componentsToDeselect.add((InlinedTileComponent) aComponent);
					return false;
				}
				if (aComponent instanceof GroupTileComponent) {
					componentsToDeselect.add((GroupTileComponent) aComponent);
					return false;
				}
				if (aComponent instanceof ContextTileComponent) {
					Selectable contextSelection = ((ContextTileComponent) aComponent).getContextSelection();
					if (contextSelection != null) {
						componentsToDeselect.add(contextSelection);
					} else {
						Logger.error(aComponent + " without context selector.", RootTileComponent.class);
					}
					return false;
				}
				return super.visitLayoutComponent(aComponent);
			}
		};
		for (int index = displayedComponents.size() - 1; index >= 0; index--) {
			displayedComponents.get(index).acceptVisitorRecursively(selectableCollector);
		}
		componentsToDeselect.forEach(Selectable::clearSelection);
	}

	/**
	 * Determines the {@link RootTileComponent} in which the component is displayed.
	 * 
	 * @return May be <code>null</code>, when the given component is not part of a
	 *         {@link RootTileComponent}.
	 * @see #getDisplayedComponent(LayoutComponent)
	 */
	public static RootTileComponent getRootTile(LayoutComponent component) {
		LayoutComponent displayedComponent = getDisplayedComponent(component);
		if (displayedComponent != null) {
			return correspondingRoot(displayedComponent);
		}
		return null;
	}

	/**
	 * When the given component is displayed in a tile, this method returns the top level
	 * {@link LayoutComponent} that is actually displayed, i.e. part of
	 * {@link RootTileComponent#displayedPath()}.
	 * 
	 * @return May be <code>null</code>, when the given component is not part of a
	 *         {@link RootTileComponent}.
	 * 
	 * @see #getRootTile(LayoutComponent)
	 */
	public static LayoutComponent getDisplayedComponent(LayoutComponent component) {
		do {
			if (correspondingRoot(component) != null) {
				return component;
			}
			component = component.getParent();
		} while (component != null);
		return null;
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}

