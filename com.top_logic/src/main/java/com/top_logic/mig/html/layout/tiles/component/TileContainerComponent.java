/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.RandomAccess;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.menu.CommandItem;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.toolbar.ToolBarChangeListener;
import com.top_logic.layout.toolbar.ToolBarOwner;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.tiles.AbstractTileLayoutVisitor;
import com.top_logic.mig.html.layout.tiles.CompositeTile;
import com.top_logic.mig.html.layout.tiles.ContextTileGroup;
import com.top_logic.mig.html.layout.tiles.InlinedTile;
import com.top_logic.mig.html.layout.tiles.TileDefinition;
import com.top_logic.mig.html.layout.tiles.TileGroup;
import com.top_logic.mig.html.layout.tiles.TileLayout;
import com.top_logic.mig.html.layout.tiles.TileLayoutVisitor;
import com.top_logic.mig.html.layout.tiles.TileRef;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.mig.html.layout.tiles.breadcrumb.TileBreadcrumbControlProvider;
import com.top_logic.mig.html.layout.tiles.control.TileContainerControlProvider;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;
import com.top_logic.tool.boundsec.BoundCheckerLayoutConfig;
import com.top_logic.tool.boundsec.simple.AllowAllChecker;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LayoutContainer} displaying {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileContainerComponent extends LayoutContainer implements BoundCheckerDelegate {

	/**
	 * Configuration of a {@link TileContainerComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LayoutContainer.Config, BoundCheckerLayoutConfig, TileDefinitionContainer {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Property name of {@link #getBreadcrumb()}. */
		String BREADCRUMB = "breadcrumb";

		/**
		 * Configuration of the default layout. Used unless the user haas changed the layout, e.g.
		 * by adding a group or changing displayed {@link TileRef}s in a group.
		 */
		@Mandatory
		@Constraint(value = OnlyAvailableTiles.class, args = @Ref(TileDefinitionContainer.TILES_ATTRIBUTE_NAME))
		DefaultLayoutConfig getDefaultLayout();

		@Override
		@Constraint(value = NoDuplicateTileNames.class)
		Map<String, TileDefinition> getTiles();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		/**
		 * The breadcrumb that should be displayed in the title of the toolbar.
		 * <p>
		 * Null means: No breadcrumb.
		 * </p>
		 */
		@Name(BREADCRUMB)
		PolymorphicConfiguration<TileBreadcrumbControlProvider> getBreadcrumb();

		@Override
		@ItemDefault(TileContainerControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@ClassDefault(TileContainerComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@Derived(fun = ChildLayoutConfigComputation.class, args = {
			@Ref(TileDefinitionContainer.TILES_ATTRIBUTE_NAME) })
		List<? extends LayoutComponent.Config> getChildConfigurations();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			LayoutContainer.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(ConfigureGroupCommand.COMMAND_ID);
			registry.registerButton(StepIntoContextTile.COMMAND_ID);
			registry.registerButton(StepOutTileCommand.COMMAND_ID);
			registry.registerButton(AddGroupCommand.COMMAND_ID);
			registry.registerButton(AddComponentCommand.COMMAND_ID);
		}

	}

	/**
	 * Configuration holding the {@link TileLayout} which is displayed when the user has made no
	 * changes in the layout hierarchy.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface DefaultLayoutConfig extends ConfigurationItem {

		/**
		 * The root {@link TileLayout} of the tiles within the {@link TileContainerComponent}.
		 */
		@DefaultContainer
		@Mandatory
		TileLayout getRootTile();

	}

	private static final TileLayoutVisitor<Provider<Menu>, TileContainerComponent> GET_TILE_COMMANDS =
		new AbstractTileLayoutVisitor<>() {

			@Override
			public Provider<Menu> visitTileRef(TileRef value, TileContainerComponent arg) {
				PolymorphicConfiguration<Provider<Menu>> tileCommands =
					arg.getReferencedDefinition(value).getTileCommands();
				if (tileCommands == null) {
					return () -> Menu.create(hideCommand(arg, value));
				}
				BufferingProtocol protocol = new BufferingProtocol();
				Provider<Menu> result =
					arg.toInstantiationContext(protocol).getInstance(tileCommands);
				protocol.checkErrors();
				return () -> {
					Menu menu = result.get();
					menu.insert(0, CommandItem.create(hideCommand(arg, value)));
					return menu;
				};
			}

			@Override
			public Provider<Menu> visitTileGroup(TileGroup value, TileContainerComponent arg) {
				return () -> Menu.create(
					hideCommand(arg, value),
					removeCommand(arg, value),
					new EditTilePreviewCommand<>(arg, value));
			}

			@Override
			public Provider<Menu> visitInlinedTile(InlinedTile value, TileContainerComponent arg) {
				return () -> Menu.create(
					hideCommand(arg, value),
					removeCommand(arg, value),
					new EditTilePreviewCommand<>(arg, value));
			}

			private CommandModel removeCommand(TileContainerComponent container, TileLayout tile) {
				return new RemoveTileCommand(container, tile);
			}

			private CommandModel hideCommand(TileContainerComponent container, TileLayout tile) {
				return new HideTileCommand(container, tile);
			}

			@Override
			public Provider<Menu> visitCompositeTile(CompositeTile value,
					TileContainerComponent arg) {
				return ComponentTile.NO_MENU;
			}

		};

	/** Finds the (depth) first {@link TileRef} with the given name. */
	private static final TileLayoutVisitor<TileLayout, String> FIRST_LAYOUT_FOR_NAME =
		new AbstractTileLayoutVisitor<>() {

			@Override
			public TileLayout visitTileRef(TileRef value, String arg) {
				if (arg.equals(value.getName())) {
					return value;
				}
				return value.getContentTile().visit(this, arg);
			}

			@Override
			public TileLayout visitInlinedTile(InlinedTile value, String arg) {
				if (arg.equals(value.getId())) {
					return value;
				}
				return null;
			}

			@Override
			public TileLayout visitCompositeTile(CompositeTile value, String arg) {
				Optional<TileLayout> findFirst =
					value.getTiles().stream().map(part -> part.visit(this, arg)).filter(Objects::nonNull).findFirst();
				return findFirst.orElse(null);
			}
		};

	/**
	 * Returns the component that belongs to the given {@link TileLayout}.
	 * 
	 * @see #_componentByTileName
	 */
	private static final TileLayoutVisitor<LayoutComponent, Map<String, LayoutComponent>> COMPONENT_BY_TILE =
		new AbstractTileLayoutVisitor<>() {

			@Override
			public LayoutComponent visitTileRef(TileRef value, Map<String, LayoutComponent> arg) {
				return arg.get(value.getName());
			}

			@Override
			public LayoutComponent visitInlinedTile(InlinedTile value, Map<String, LayoutComponent> arg) {
				return arg.get(value.getId());
			}

			@Override
			public LayoutComponent visitCompositeTile(CompositeTile value, Map<String, LayoutComponent> arg) {
				return null;
			}

		};

	private static final TileLayoutVisitor<List<? extends ComponentTile>, TileContainerComponent> COMPONENT_TILE_CREATOR =
		new TileLayoutVisitor<>() {

			@Override
			public List<? extends ComponentTile> visitTileRef(TileRef value, TileContainerComponent arg) {
				TileDefinition tileDefinition = arg.getReferencedDefinition(value);
				LogProtocol log = new LogProtocol(TileContainerComponent.class);
				TileProvider provider = arg.toInstantiationContext(log).getInstance(tileDefinition.getTileProvider());
				log.checkErrors();

				return createChildren(provider, arg, value);
			}

			@Override
			public List<? extends ComponentTile> visitCompositeTile(CompositeTile value, TileContainerComponent arg) {
				return singletonTile(arg, value);
			}

			@Override
			public List<? extends ComponentTile> visitTileGroup(TileGroup value, TileContainerComponent arg) {
				return singletonTile(arg, value);
			}

			@Override
			public List<? extends ComponentTile> visitContextTileGroup(ContextTileGroup value,
					TileContainerComponent arg) {
				return singletonTile(arg, value);
			}

			@Override
			public List<? extends ComponentTile> visitInlinedTile(InlinedTile value, TileContainerComponent arg) {
				return singletonTile(arg, value);
			}

			private List<? extends ComponentTile> singletonTile(TileContainerComponent container, TileLayout tile) {
				return createChildren(SingleTileProvider.INSTANCE, container, tile);
			}

			private List<? extends ComponentTile> createChildren(TileProvider provider,
					TileContainerComponent container, TileLayout tile) {
				return provider.getDisplayTiles(new ContainerComponentTile(container, tile));
			}
		};

	private static final TileLayoutVisitor<TilePreview, TileContainerComponent> PREVIEW_CREATOR =
		new TileLayoutVisitor<>() {
			@Override
			public TilePreview visitTileRef(TileRef value, TileContainerComponent arg) {
				return instantiate(arg.getReferencedDefinition(value));
			}

			@Override
			public TilePreview visitCompositeTile(CompositeTile value, TileContainerComponent arg) {
				return TilePreview.EMPTY;
			}

			@Override
			public TilePreview visitTileGroup(TileGroup value, TileContainerComponent arg) {
				return instantiate(value);
			}

			@Override
			public TilePreview visitContextTileGroup(ContextTileGroup value, TileContainerComponent arg) {
				return TilePreview.EMPTY;
			}

			@Override
			public TilePreview visitInlinedTile(InlinedTile value, TileContainerComponent arg) {
				return instantiate(value);
			}

			private TilePreview instantiate(PreviewedTile value) {
				return instantiate(value.getPreview());
			}

			private TilePreview instantiate(PolymorphicConfiguration<? extends TilePreview> preview) {
				return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(nonNull(preview));
			}

			private PolymorphicConfiguration<? extends TilePreview> nonNull(
					PolymorphicConfiguration<? extends TilePreview> preview) {
				if (preview == null) {
					preview = StaticPreview.newStaticPreview(null, null);
				}
				return preview;
			}

		};

	private static final String LAYOUT_KEY = "layoutKey";

	/**
	 * All children in this component.
	 * 
	 * @see #getChildList()
	 */
	private List<LayoutComponent> _children = Collections.emptyList();

	/**
	 * Currently visible children.
	 * 
	 * @see #getVisibleChildren()
	 */
	private final Iterable<LayoutComponent> _visibleChildren = new Iterable<>() {

		@Override
		public Iterator<LayoutComponent> iterator() {
			return new FilterIterator<>(getChildList().iterator(),
				(LayoutComponent component) -> component.isVisible());
		}
	};

	/** Maps the name of a {@link TileDefinition} to the corresponding {@link LayoutComponent}. */
	BidiMap<String, LayoutComponent> _componentByTileName = MapUtil.emptyBidiMap();

	/** "allowed" state by child component. */
	Map<LayoutComponent, Boolean> _allownessByComponent = Collections.emptyMap();

	private final Map<TileLayout, Boolean> _allownessByTile = new IdentityHashMap<>();

	/** The complete {@link TileLayout} of this component. */
	private TileLayout _layout;

	/** @see #displayedLayout() */
	private final List<TileLayout> _displayedLayout = new ArrayList<>();

	/**
	 * All {@link TileDefinition}. Especially it contains also the {@link TileDefinition} within a
	 * {@link TileDefinition}.
	 * 
	 * @see TileDefinition#getTiles()
	 */
	Map<String, TileDefinition> _flatDefinitions;

	/**
	 * {@link BoundChecker} to delegate all {@link BoundChecker} methods to.
	 */
	private final BoundChecker _boundCheckerDelegate;

	private final TileBreadcrumbControlProvider _breadcrumb;

	/**
	 * Creates a new {@link TileContainerComponent}.
	 */
	public TileContainerComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		_flatDefinitions = TileUtils.addDefinitionsRecursive(context, atts.getTiles(), new HashMap<>());
		_layout = findLayout();
		_displayedLayout.add(_layout);
		_boundCheckerDelegate = new AllowAllChecker(getName());
		_breadcrumb = context.getInstance(getConfig().getBreadcrumb());
		if (_breadcrumb != null) {
			addListener(TOOLBAR_PROPERTY, this::setBreadcrumbIntoToolbar);
		}
	}

	/**
	 * Ignore the unused parameters: This method has to match the signature of the
	 * {@link ToolBarChangeListener}.
	 */
	@SuppressWarnings("unused")
	private void setBreadcrumbIntoToolbar(ToolBarOwner owner, ToolBar oldToolbar, ToolBar newToolbar) {
		if (newToolbar == null) {
			return;
		}
		newToolbar.setTitle(_breadcrumb.createFragment(this));
	}

	private String personalStructureKey() {
		return LAYOUT_KEY + "." + getName();
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);
		// Do not resolve components, because me and my children are resolved later.
		initChildren(context, false);
	}

	private void initChildren(InstantiationContext context, boolean resolve) {
		Map<String, LayoutComponent> compByTileName = new HashMap<>(_componentByTileName);
		Map<String, LayoutComponent.Config> referencedTiles = getLayoutConfigs(context, completeLayout());
		List<LayoutComponent> newChildList = new ArrayList<>(referencedTiles.size());
		BidiMap<String, LayoutComponent> newComponentByTileName = new BidiHashMap<>(referencedTiles.size());
		Map<LayoutComponent, Boolean> newAllownessByComponent = new HashMap<>();
		List<LayoutComponent> createdChildren = new ArrayList<>();
		for (Entry<String, LayoutComponent.Config> config : referencedTiles.entrySet()) {
			LayoutComponent existingComponent = compByTileName.remove(config.getKey());
			if (existingComponent != null) {
				newChildList.add(existingComponent);
				newComponentByTileName.put(config.getKey(), existingComponent);
				newAllownessByComponent.put(existingComponent, _allownessByComponent.get(existingComponent));
				continue;
			}
			LayoutComponent formerlyNewChild = newComponentByTileName.get(config.getKey());
			if (formerlyNewChild != null) {
				/* Tile could be referenced in more than one tile groups. */
				continue;
			}

			LayoutComponent newChild = instantiateChild(context, config.getValue());
			newChildList.add(newChild);
			newComponentByTileName.put(config.getKey(), newChild);
			createdChildren.add(newChild);
		}

		List<LayoutComponent> oldChildren = _children;
		_children = newChildList;
		_componentByTileName = newComponentByTileName;
		handleChildrenAdded(context, createdChildren, resolve, newAllownessByComponent);
		handleRemoveChildren(context, compByTileName.values());
		_allownessByComponent = newAllownessByComponent;
		if (isVisible()) {
			updateChildVisibility();
		}
		fireChildrenChanged(oldChildren);
	}

	private void handleChildrenAdded(InstantiationContext context, Collection<LayoutComponent> children,
			boolean resolve, Map<LayoutComponent, Boolean> allownessByComponent) {
		for (LayoutComponent child : children) {
			handleChildAdded(context, child, resolve, allownessByComponent);
		}
	}

	private void handleChildAdded(InstantiationContext context, LayoutComponent child, boolean resolve,
			Map<LayoutComponent, Boolean> allownessByComponent) {
		updateRelations(context, child);
		boolean componentAllowed;
		if (resolve) {
			resolveComponent(context, child);
			componentAllowed = TileUtils.componentAllowed(child);
		} else {
			/* Can not compute allowness when components are not resolved. */
			componentAllowed = true;
		}
		allownessByComponent.put(child, componentAllowed);
	}

	private void handleRemoveChildren(Log log, Collection<LayoutComponent> children) {
		for (LayoutComponent child : children) {
			handleRemoveChild(log, child);
		}
	}

	private void handleRemoveChild(Log log, LayoutComponent child) {
		updateRelations(log, child);
		child.setParent(null);
	}

	private LayoutComponent instantiateChild(InstantiationContext context, LayoutComponent.Config component) {
		LayoutComponent child = context.getInstance(component);
		child.setParent(this);
		ComponentInstantiationContext.createSubComponents(context, child);
		return child;
	}

	private void updateRelations(Log log, LayoutComponent component) {
		MainLayout main = getMainLayout();
		if (main != null) {
			main.setupRelations(log);
		}
	}

	/**
	 * The names of all referenced {@link TileDefinition}s in the given {@link TileLayout}.
	 */
	private Map<String, LayoutComponent.Config> getLayoutConfigs(InstantiationContext context,
			TileLayout layout) {
		Map<String, LayoutComponent.Config> layoutConfigs = new HashMap<>();
		layout.visit(new AbstractTileLayoutVisitor<Void, Void>() {

			@Override
			public Void visitTileRef(TileRef value, Void arg) {
				String tileName = value.getName();
				TileDefinition tileConfig = _flatDefinitions.get(tileName);
				if (tileConfig == null) {
					// Reference to a tile which does not exist.
					ResKey error =
						com.top_logic.mig.html.layout.tiles.I18NConstants.UNKNOWN_TILE_REF__REF_NAMES__AVAILABLE
							.fill(tileName, _flatDefinitions.keySet());
					context.error(Resources.getInstance().getString(error));
					return null;
				}
				layoutConfigs.put(tileName, tileConfig.getComponent());
				return value.getContentTile().visit(this, arg);
			}

			@Override
			public Void visitInlinedTile(InlinedTile value, Void arg) {
				layoutConfigs.put(value.getId(), value.getComponent());
				return null;
			}

			@Override
			public Void visitCompositeTile(CompositeTile value, Void arg) {
				value.getTiles().forEach(tile -> tile.visit(this, arg));
				return null;
			}

		}, null);
		return layoutConfigs;
	}

	/**
	 * The currently displayed {@link TileLayout}.
	 */
	public final TileLayout displayedLayout() {
		return displayedLayoutPath().get(displayedLayoutPath().size() - 1);
	}

	/**
	 * The currently displayed path of {@link TileLayout}.
	 * 
	 * <p>
	 * <b>Attention:</b> Change of this list may result in unexpected behaviour of the component.
	 * </p>
	 * 
	 * @return A {@link RandomAccess} list with the {@link TileLayout} from the root to the actual
	 *         displayed {@link TileLayout}.
	 */
	@FrameworkInternal
	public List<TileLayout> displayedLayoutPath() {
		return _displayedLayout;
	}

	private TileLayout completeLayout() {
		return _layout;
	}

	/**
	 * Sets the given tiles as new content of the {@link #displayedLayout() currently displayed
	 * layout}.
	 * 
	 * @param newTiles
	 *        The new value of {@link CompositeTile#getTiles()}.
	 */
	public void updateDisplayedLayout(List<TileLayout> newTiles) {
		TileLayout displayedLayout = displayedLayout();
		checkCompositeTile(displayedLayout);
		CompositeTile composite = (CompositeTile) displayedLayout;
		// current layout is modified inline. Therefore create a copy for notification.
		TileLayout formerLayout = TypedConfiguration.copy(composite);
		composite.setTiles(newTiles);

		handleCurrentLayoutUpdated(formerLayout);
	}

	private static void checkCompositeTile(TileLayout displayedLayout) {
		if (!(displayedLayout instanceof CompositeTile)) {
			throw new TopLogicException(I18NConstants.NOT_COMPOSITE_TILE);
		}
	}

	/**
	 * Determines whether the given {@link TileLayout} is allowed to be displayed.
	 * 
	 * @param tileLayout
	 *        A {@link TileLayout} that belongs to this {@link TileContainerComponent}.
	 * 
	 * @return Whether the given {@link TileLayout} is allowed to be displayed.
	 */
	public boolean isTileAllowed(TileLayout tileLayout) {
		Boolean isAllowed = _allownessByTile.get(tileLayout);
		if (isAllowed != null) {
			return isAllowed.booleanValue();
		}
		return true;
	}

	/**
	 * Updates the "allowed" state of the given {@link TileLayout}.
	 * 
	 * @param tileLayout
	 *        A {@link TileLayout} that belongs to this {@link TileContainerComponent}.
	 * @param allowed
	 *        Whether the given tileLayout is allowed.
	 */
	public void setTileAllowed(TileLayout tileLayout, boolean allowed) {
		_allownessByTile.put(tileLayout, Boolean.valueOf(allowed));
	}

	private void handleCurrentLayoutUpdated(TileLayout formerLayout) {
		updateDisplayedLayout(formerLayout, displayedLayout());
		TileUtils.store(completeLayout(), personalStructureKey());

		BufferingProtocol protocol = new BufferingProtocol();
		InstantiationContext instantiationContext = toInstantiationContext(protocol);
		instantiationContext.deferredReferenceCheck(() -> {
			try {
				initChildren(instantiationContext, true);
			} catch (ConfigurationError err) {
				protocol.error("Error initiatalizing children.", err);
			}
			return null;
		});
		protocol.checkErrors();

		updateTileSecurity();
	}

	InstantiationContext toInstantiationContext(Protocol protocol) {
		/* Note: Do not use SimpleInstantiationContext, because instantiation may use feature of
		 * resolving references which is not supported by SimpleInstantiationContext. */
		return new ComponentInstantiationContext(new DefaultInstantiationContext(protocol), getMainLayout());
	}

	/**
	 * Displays the given {@link TileLayout}.
	 * 
	 * @param layout
	 *        The newly displayed {@link TileLayout}. Must be a part of the tile hierarchy of this
	 *        component.
	 */
	public void displayTileLayout(TileLayout layout) {
		if (displayedLayout() == layout) {
			// already displayed.
			return;
		}
		if (!isTileAllowed(layout)) {
			/* Display an ancestor is always allowed. Otherwise it is not possible to step out, when
			 * security of component switches spontaneous. */
			boolean shouldDisplayAncestor = TileUtils.isAncestor(displayedLayout(), layout);
			if (!shouldDisplayAncestor) {
				String tile = TypedConfiguration.toString(layout);
				ResKey error = I18NConstants.TILE_NOT_ALLOWED__TILE__COMPONENT.fill(tile, componentName());
				throw new TopLogicException(error);
			}
		}
		// Check part of hierarchy
		TileLayout ancestorOrSelf = layout;
		while (true) {
			if (ancestorOrSelf == completeLayout()) {
				break;
			}
			ancestorOrSelf = TileUtils.getContainer(ancestorOrSelf);
			if (ancestorOrSelf == null) {
				String tile = TypedConfiguration.toString(layout);
				ResKey error = I18NConstants.TILE_NOT_PART_OF_HIERARCHY__TILE__COMPONENT.fill(tile, componentName());
				throw new TopLogicException(error);
			}
		}
		updateDisplayedLayout(displayedLayout(), layout);
	}

	private String componentName() {
		return getName().qualifiedName();
	}

	private void updateDisplayedLayout(TileLayout oldLayout, TileLayout newLayout) {
		adaptLayoutList(newLayout);
		if (isVisible()) {
			updateChildVisibility();
		}
		firePropertyChanged(TileLayoutListener.TILE_LAYOUT_CHANGED, this, oldLayout, displayedLayout());
	}

	private void adaptLayoutList(TileLayout newLastLayout) {
		List<TileLayout> layoutPath = displayedLayoutPath();
		for (int i = layoutPath.size(); i > 0; i--) {
			if (layoutPath.get(i - 1).equals(newLastLayout)) {
				layoutPath.subList(i, layoutPath.size()).clear();
				return;
			}
		}
		layoutPath.add(newLastLayout);
	}

	private TileLayout findLayout() {
		TileLayout layout;
		try {
			layout = TileUtils.getPersonalLayout(TileLayout.class, personalStructureKey());
		} catch (ConfigurationException ex) {
			canNotLoadLayout();
			layout = null;
		}
		if (layout != null) {
			ResKey problem = TileUtils.hasUnkownTile(layout, _flatDefinitions.keySet());
			if (problem != ResKey.NONE) {
				canNotLoadLayout();
				layout = null;
			}
		}
		if (layout == null) {
			TileLayout rootTile = getConfig().getDefaultLayout().getRootTile();
			/* Layout is modified. As the layout configuration is cached, the item from the config
			 * must be copied. */
			rootTile = TypedConfiguration.copy(rootTile);
			layout = rootTile;
		}
		return layout;
	}

	private void canNotLoadLayout() {
		InfoService.showInfo(I18NConstants.UNABLE_LOAD_PERSONALIZED_LAYOUT,
			I18NConstants.UNABLE_LOAD_PERSONALIZED_LAYOUT_DETAIL__COMPONENT.fill(componentName()));
		TileUtils.store(null, personalStructureKey());
	}

	/**
	 * Returns the {@link TileDefinition} that is referenced by the given {@link TileRef}.
	 * 
	 * @param ref
	 *        The {@link TileRef} to get the {@link TileDefinition} for.
	 * 
	 * @return The {@link TileDefinition} for the given {@link TileRef}, or <code>null</code>, when
	 *         there is no such definition.
	 */
	public TileDefinition getReferencedDefinition(TileRef ref) {
		return getTileDefinition(ref.getName());
	}

	/**
	 * Returns the {@link TileDefinition} with the given name.
	 * 
	 * @param name
	 *        The {@link TileDefinition#getName()} of the {@link TileDefinition} to return.
	 * 
	 * @return The {@link TileDefinition} with the given name, or <code>null</code>, when there is
	 *         no such definition.
	 */
	public TileDefinition getTileDefinition(String name) {
		return _flatDefinitions.get(name);
	}

	@Override
	protected void setChildVisibility(boolean visible) {
		if (visible) {
			updateChildVisibility();
		} else {
			getChildList()
				.stream()
				.filter(LayoutComponent::isVisible)
				.forEach(child -> child.setVisible(false));
		}
	}

	private void updateChildVisibility() {
		HashSet<String> visibleTiles = new HashSet<>();
		displayedLayout().visit(new AbstractTileLayoutVisitor<Void, Collection<String>>() {

			@Override
			public Void visitTileRef(TileRef value, Collection<String> arg) {
				arg.add(value.getName());
				return null;
			}

			@Override
			public Void visitInlinedTile(InlinedTile value, Collection<String> arg) {
				arg.add(value.getId());
				return null;
			}

			@Override
			public Void visitCompositeTile(CompositeTile value, Collection<String> arg) {
				value.getTiles().stream()
					.filter(TileRef.class::isInstance)
					.filter(ref -> TileContainerComponent.this.getReferencedDefinition((TileRef) ref).getTileProvider()
						.isComponentDisplayedInline())
					.map(ref -> ((TileRef) ref).getName())
					.forEach(name -> arg.add(name));
				return null;
			}

		}, visibleTiles);
		HashMap<String, LayoutComponent> knownComponents = new HashMap<>(_componentByTileName);
		visibleTiles.forEach(visibleTile -> knownComponents.remove(visibleTile).setVisible(true));
		knownComponents.values().forEach(component -> component.setVisible(false));
	}

	@Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return _visibleChildren;
	}

	@Override
	public boolean makeVisible(LayoutComponent child) {
		String tileName = _componentByTileName.getKey(child);
		TileLayout layout = _layout.visit(FIRST_LAYOUT_FOR_NAME, tileName);
		if (layout == null) {
			// Not a part of mine.
			return false;
		}
		boolean visible = makeVisible();
		if (!visible) {
			return false;
		}
		displayTileLayout(layout);
		return true;
	}

	@Override
	public List<LayoutComponent> getChildList() {
		return _children;
	}

	@Override
	public void setChildren(List<LayoutComponent> newChildren) {
		// Can not be updated directly. Children are taken from tiles.
	}

	@Override
	public boolean isOuterFrameset() {
		return true;
	}

	@Override
	public int getChildCount() {
		return getChildList().size();
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	/**
	 * Returns the {@link LayoutComponent} child of this component which belongs to the
	 * {@link TileLayout}.
	 * 
	 * @param tile
	 *        The {@link TileLayout} to get component for.
	 * @return The component represented by the given {@link TileLayout}. This may be
	 *         <code>null</code>, e.g. when the given {@link TileLayout} does not represent a
	 *         component, or it is not loaded yet.
	 */
	public LayoutComponent getTileComponent(TileLayout tile) {
		return tile.visit(COMPONENT_BY_TILE, _componentByTileName);
	}

	/**
	 * Adds the given {@link TileLayout} at the end of the tiles of the currently displayed
	 * {@link CompositeTile}.
	 */
	public void addNewTile(TileLayout newTile) {
		TileLayout displayedLayout = displayedLayout();
		checkCompositeTile(displayedLayout);
		CompositeTile currentLayout = (CompositeTile) displayedLayout;

		CompositeTile formerLayout = TypedConfiguration.copy(currentLayout);
		currentLayout.getTiles().add(newTile);

		handleCurrentLayoutUpdated(formerLayout);
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		boolean superResult = super.validateModel(context);

		boolean validationWasNecessary = false;
		for (LayoutComponent child : getChildList()) {
			Boolean knownValue = _allownessByComponent.get(child);
			boolean componentAllowed = TileUtils.componentAllowed(child);
			if (knownValue != null && knownValue.booleanValue() == componentAllowed) {
				continue;
			}
			_allownessByComponent.put(child, componentAllowed);
			validationWasNecessary = true;
		}
		if (validationWasNecessary) {
			updateTileSecurity();
			return false;
		}
		return superResult;
	}

	@Override
	protected boolean receiveModelSecurityChangedEvent(Object changedBy) {
		boolean superResult = super.receiveModelSecurityChangedEvent(changedBy);
		LayoutComponent changedChild = getPotentialChild(changedBy);
		if (changedChild == null) {
			return superResult;
		}
		Boolean knownValue = _allownessByComponent.get(changedChild);
		if (knownValue == null) {
			/* This happens when a component is newly instantiated. When the allowness has been
			 * changed during first access to component if fires a securityChanged event. as we
			 * currently instantiate the component and compute the allowness in that method, nothing
			 * is to do here. */
			return superResult;
		}
		boolean childAllowed = TileUtils.componentAllowed(changedChild);
		if (childAllowed == knownValue.booleanValue()) {
			return superResult;
		}

		_allownessByComponent.put(changedChild, childAllowed);

		updateTileSecurity();

		if (!isTileAllowed(displayedLayout())) {
			List<TileLayout> displayedPath = displayedLayoutPath();
			for (int i = displayedPath.size() - 2; i >= 0; i--) {
				TileLayout displayedLayout = displayedPath.get(i);
				if (isTileAllowed(displayedLayout) && !(displayedLayout instanceof ContextTileGroup)) {
					displayTileLayout(displayedLayout);
					break;
				}
			}
		}

		return superResult;
	}

	private void updateTileSecurity() {
		completeLayout().visit(new AbstractTileLayoutVisitor<Void, Void>() {

			@Override
			public Void visitTileRef(TileRef value, Void arg) {
				LayoutComponent tileComponent = getTileComponent(value);
				boolean allowed = _allownessByComponent.get(tileComponent).booleanValue();
				TileContainerComponent.this.setTileAllowed(value, allowed);
				TileLayout content = value.getContentTile();
				content.visit(this, arg);
				return null;
			}

			@Override
			public Void visitInlinedTile(InlinedTile value, Void arg) {
				LayoutComponent tileComponent = getTileComponent(value);
				boolean allowed = _allownessByComponent.get(tileComponent).booleanValue();
				TileContainerComponent.this.setTileAllowed(value, allowed);
				return null;
			}

			@Override
			public Void visitContextTileGroup(ContextTileGroup value, Void arg) {
				visitCompositeTile(value, arg);
				/* Allow the content iff the TileRef is allowed. Selection in the TileRef displays
				 * the content. If the content is not allowed, it would never be possible to
				 * configured a tile under the TileRef which is allowed for the selected object. */
				TileContainerComponent.this.setTileAllowed(value,
					TileContainerComponent.this.isTileAllowed(TileUtils.getContainer(value)));
				return null;
			}

			@Override
			public Void visitCompositeTile(CompositeTile value, Void arg) {
				/* Let the CompositeTile always be allowed. If not, it would not be possible to
				 * display it to be able to select an (potentially) allowed tile to be displayed. */
				TileContainerComponent.this.setTileAllowed(value, true);
				descend(value, arg);
				return null;
			}

			private void descend(CompositeTile value, Void arg) {
				for (TileLayout tile : value.getTiles()) {
					tile.visit(this, arg);
				}
			}
		}, null);
	}

	private LayoutComponent getPotentialChild(Object aChangedBy) {
		if (aChangedBy instanceof LayoutComponent) {
			LayoutComponent thePotentialChild = (LayoutComponent) aChangedBy;
			while (true) {
				LayoutComponent parent = thePotentialChild.getParent();
				if (parent == this) {
					return thePotentialChild;
				}
				if (parent == null) {
					return null;
				}
				thePotentialChild = parent;
			}
		} else {
			return null;
		}
	}

	/**
	 * Creates a sequence of strings identifying the given {@link TileLayout} within this component.
	 * 
	 * @return A path navigating through the layout of this component identifying a
	 *         {@link TileLayout}.
	 * 
	 * @see #resolveTilePath(List)
	 */
	public List<String> getTilePath(TileLayout tile) {
		List<String> result = new ArrayList<>();
		tile.visit(new TileLayoutVisitor<Void, List<String>>() {

			private Resources _resources = Resources.getInstance();

			@Override
			public Void visitTileRef(TileRef value, List<String> arg) {
				TileLayout container = TileUtils.getContainer(value);
				if (container != null) {
					container.visit(this, arg);
					arg.add(_resources.getString(getLabel(value)));
				}
				return null;
			}

			@Override
			public Void visitTileGroup(TileGroup value, List<String> arg) {
				TileLayout container = TileUtils.getContainer(value);
				if (container != null) {
					TileUtils.getContainer(value).visit(this, arg);
					arg.add(_resources.getString(getLabel(value)));
				}
				return null;
			}

			@Override
			public Void visitContextTileGroup(ContextTileGroup value, List<String> arg) {
				Void val = visitCompositeTile(value, arg);
				arg.add(_resources.getString(getLabel(value)));
				return val;
			}

			@Override
			public Void visitCompositeTile(CompositeTile value, List<String> arg) {
				TileLayout container = TileUtils.getContainer(value);
				if (container != null) {
					TileUtils.getContainer(value).visit(this, arg);
				}
				return null;
			}

			@Override
			public Void visitInlinedTile(InlinedTile value, List<String> arg) {
				TileLayout container = TileUtils.getContainer(value);
				if (container != null) {
					TileUtils.getContainer(value).visit(this, arg);
					arg.add(_resources.getString(getLabel(value)));
				}
				return null;
			}
		}, result);
		return result;
	}

	/**
	 * Searches for a label for the given {@link TileLayout}.
	 * 
	 * @param layout
	 *        The {@link TileLayout} to get {@link ResKey label} for.
	 * 
	 * @return {@link ResKey label} for the given {@link TileLayout}. May be <code>null</code>.
	 */
	public ResKey getLabel(TileLayout layout) {
		return layout.visit(new TileLayoutVisitor<ResKey, Void>() {

			@Override
			public ResKey visitTileRef(TileRef value, Void arg) {
				return TileContainerComponent.this.getReferencedDefinition(value).getLabel();
			}

			@Override
			public ResKey visitTileGroup(TileGroup value, Void arg) {
				return value.getLabel();
			}

			@Override
			public ResKey visitContextTileGroup(ContextTileGroup value, Void arg) {
				return I18NConstants.CONTENT_TILE_NAME;
			}

			@Override
			public ResKey visitCompositeTile(CompositeTile value, Void arg) {
				return null;
			}

			@Override
			public ResKey visitInlinedTile(InlinedTile value, Void arg) {
				return value.getLabel();
			}

		}, null);
	}

	/**
	 * Tries to resolve a {@link TileLayout} identified by the given path.
	 * 
	 * @param tilePath
	 *        The path to resolve within the component.
	 * @return The {@link TileLayout} identified by the given path.
	 * 
	 * @throws ParseException
	 *         when path could not be resolved to a valid {@link TileLayout}. The
	 *         {@link ParseException#getErrorOffset() error offsets} points to the first index in
	 *         the given list which could not be resolved.
	 */
	public TileLayout resolveTilePath(List<String> tilePath) throws ParseException {
		TileLayout layout = completeLayout();
		int pathSize = tilePath.size();
		if (pathSize == 0) {
			return layout;
		}
		MutableInteger index = new MutableInteger(0);
		TileLayoutVisitor<TileLayout, Iterator<String>> visitor =
			new TileLayoutVisitor<>() {

				Resources _resources = Resources.getInstance();

				@Override
				public TileLayout visitCompositeTile(CompositeTile value, Iterator<String> arg) {
					if (index.intValue() == pathSize) {
						return value;
					}
					return descendPart(value, arg);
				}

				private TileLayout descendPart(CompositeTile value, Iterator<String> arg) {
					String name = tilePath.get(index.intValue());
					for (TileLayout tile : value.getTiles()) {
						String label = _resources.getString(getLabel(tile));
						if (name.equals(label)) {
							index.inc();
							return tile.visit(this, arg);
						}
					}
					return null;
				}

				@Override
				public TileLayout visitTileRef(TileRef value, Iterator<String> arg) {
					if (index.intValue() == pathSize) {
						return value;
					}
					index.inc();
					return value.getContentTile().visit(this, arg);
				}

				@Override
				public TileLayout visitTileGroup(TileGroup value, Iterator<String> arg) {
					return visitCompositeTile(value, arg);
				}

				@Override
				public TileLayout visitContextTileGroup(ContextTileGroup value, Iterator<String> arg) {
					return visitCompositeTile(value, arg);
				}

				@Override
				public TileLayout visitInlinedTile(InlinedTile value, Iterator<String> arg) {
					if (index.intValue() == pathSize) {
						return value;
					}
					return null;
				}

			};
		TileLayout resolvedTile = layout.visit(visitor, tilePath.iterator());
		if (resolvedTile == null) {
			throw new ParseException("No tile found with path " + BreadcrumbStrings.INSTANCE.getSpecification(tilePath),
				index.intValue());
		}
		return resolvedTile;
	}

	/**
	 * Returns a {@link TilePreview} for the given {@link TileLayout}.
	 * 
	 * @param layout
	 *        The {@link TileLayout} to create a preview for.
	 * @return a {@link TilePreview} for the given {@link TileLayout}.
	 */
	public TilePreview getPreview(TileLayout layout) {
		return layout.visit(PREVIEW_CREATOR, this);
	}

	/**
	 * Returns the commands that are displayed for the given {@link TileLayout}.
	 * 
	 * @param tile
	 *        A {@link TileLayout} that belongs to this {@link TileContainerComponent}.
	 * @return The commands to display for the given {@link TileLayout}.
	 */
	public Provider<Menu> getCommandsProvider(TileLayout tile) {
		return tile.visit(GET_TILE_COMMANDS, this);
	}

	/**
	 * Returns the list of {@link ComponentTile} to display for the given {@link TileLayout}.
	 * 
	 * @param layout
	 *        A {@link TileLayout} that belongs to this {@link TileContainerComponent}.
	 * @return The {@link ComponentTile} to display for the given {@link TileLayout}.
	 */
	public List<? extends ComponentTile> getComponentTiles(TileLayout layout) {
		return layout.visit(COMPONENT_TILE_CREATOR, this);
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}
