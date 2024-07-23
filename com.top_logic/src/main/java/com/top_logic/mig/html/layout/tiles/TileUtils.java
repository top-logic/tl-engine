/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.InlineList;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.ResolveFailure;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.tiles.component.ComponentParameters;
import com.top_logic.mig.html.layout.tiles.component.TileContainerComponent;
import com.top_logic.mig.html.layout.tiles.component.TileDefinitionContainer;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;

/**
 * Utility methods for {@link TileLayout}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileUtils {

	/**
	 * Recursively adds the {@link TileDefinition} and the {@link TileDefinition#getTiles() inner
	 * definitions} into the given {@link Map}.
	 * 
	 * <p>
	 * When there are clashes, an error is logged.
	 * </p>
	 * 
	 * @param log
	 *        A {@link Log} to inform about name clashes.
	 * @param definitions
	 *        The {@link TileDefinition}s to add.
	 * @param out
	 *        The output {@link Map}.
	 * @return The given output {@link Map} for chaining.
	 */
	public static Map<String, TileDefinition> addDefinitionsRecursive(Log log, Map<String, TileDefinition> definitions,
			Map<String, TileDefinition> out) {
		for (Entry<String, TileDefinition> entry : definitions.entrySet()) {
			TileDefinition tile = entry.getValue();
			TileDefinition clash = out.put(entry.getKey(), tile);
			if (clash != null) {
				log.error("Duplicate tile with name '" + entry.getKey() + "': " + tile + ", " + clash);
			}
			out = addDefinitionsRecursive(log, tile.getTiles(), out);
		}
		return out;
	}

	/**
	 * Searches for references to unknown {@link TileDefinition}.
	 * 
	 * <p>
	 * This method descends into the visited {@link TileLayout} and searches for {@link TileRef}
	 * which reference to unknown {@link TileDefinition}.
	 * </p>
	 * 
	 * @param value
	 *        The {@link TileLayout} to inspect.
	 * @param knownTiles
	 *        The {@link TileDefinition#getName() names} of all known {@link TileDefinition}.
	 * @return Either an error {@link ResKey} with all unknown {@link TileRef} names, or
	 *         {@link ResKey#NONE}, when there are no unknown {@link TileRef} names.
	 */
	public static ResKey hasUnkownTile(TileLayout value, Set<String> knownTiles) {
		ResKey problem;
		List<String> result = getUnkownTiles(value, knownTiles);
		if (result.isEmpty()) {
			problem = ResKey.NONE;
		} else {
			problem = I18NConstants.UNKNOWN_TILE_REF__REF_NAMES__AVAILABLE.fill(result, knownTiles);
		}
		return problem;
	}

	private static List<String> getUnkownTiles(TileLayout tile, Set<String> knownTiles) {
		Object unknownTiles = tile.visit(new TileLayoutVisitor<>() {
	
			@Override
			public Object visitTileRef(TileRef value, Object arg) {
				if (!knownTiles.contains(value.getName())) {
					arg = InlineList.add(String.class, arg, value.getName());
				}
				return value.getContentTile().visit(this, arg);
			}
	
			@Override
			public Object visitTileGroup(TileGroup value, Object arg) {
				return visitCompositeTile(value, arg);
			}
	
			@Override
			public Object visitContextTileGroup(ContextTileGroup value, Object arg) {
				return visitCompositeTile(value, arg);
			}
	
			@Override
			public Object visitCompositeTile(CompositeTile value, Object arg) {
				for (TileLayout part : value.getTiles()) {
					arg = part.visit(this, arg);
				}
				return arg;
			}

			@Override
			public Object visitInlinedTile(InlinedTile value, Object arg) {
				/* Layout is defined inline. This is never unknown. */
				return arg;
			}
	
		}, InlineList.newInlineList());
		List<String> result = InlineList.toList(String.class, unknownTiles);
		return result;
	}

	/**
	 * Fetches all {@link TileRef} that can be selected in the current layout.
	 */
	public static Map<String, TileRef> allTilesByName(TileContainerComponent tileContainer) {
		List<String> tileRefStack = getTileRefStack(tileContainer);

		TileContainerComponent.Config context = tileContainer.getConfig();
		String contextName = tileContainer.getName().qualifiedName();
		Map<String, TileRef> result = new HashMap<>();
	
		addSelectableTiles(tileContainer, tileRefStack.iterator(), context, contextName, result, new HashSet<>());
		return result;
	}

	private static void addSelectableTiles(TileContainerComponent component, Iterator<String> contexts,
			TileDefinitionContainer context, String contextName, Map<String, TileRef> out, Set<String> ignoredContext) {
		Map<String, TileDefinition> contextTiles = context.getTiles();
		String nextContextName;
		if (contexts.hasNext()) {
			nextContextName = contexts.next();
			TileDefinition nextContext = contextTiles.get(nextContextName);
			// first descend before add sibling
			ignoredContext.addAll(nextContext.getIgnoreContexts());
			addSelectableTiles(component, contexts, nextContext, nextContext.getName(), out, ignoredContext);
		} else {
			nextContextName = null;
		}

		if (ignoredContext.contains(contextName)) {
			return;
		}

		for (Entry<String, TileDefinition> entry : contextTiles.entrySet()) {
			String tileName = entry.getKey();
			if (tileName.equals(nextContextName)) {
				// this is a context, therefore it must not be available (already selected).
				continue;
			}
			out.put(tileName, newTileRef(component, tileName));
		}
	}

	/**
	 * Determines all {@link TileBuilder} that can be used in the current layout to build new
	 * {@link TileLayout}.
	 */
	public static List<TileBuilder> allBuildersByName(TileContainerComponent tileContainer) {
		List<String> tileRefStack = getTileRefStack(tileContainer);
		TileContainerComponent.Config context = tileContainer.getConfig();
		String contextName = tileContainer.getName().qualifiedName();
		List<TileBuilder> result = new ArrayList<>();

		addSelectableBuilders(tileContainer, tileRefStack.iterator(), context, contextName, result, new HashSet<>());
		return result;
	}

	private static void addSelectableBuilders(TileContainerComponent component, Iterator<String> contexts,
			TileDefinitionContainer context, String contextName, List<TileBuilder> result, Set<String> ignoredContext) {
		if (contexts.hasNext()) {
			TileDefinition nextContext = context.getTiles().get(contexts.next());
			// first descend before builders
			ignoredContext.addAll(nextContext.getIgnoreContexts());
			addSelectableBuilders(component, contexts, nextContext, nextContext.getName(), result, ignoredContext);
		}

		if (ignoredContext.contains(contextName)) {
			return;
		}

		result.addAll(context.getBuilders());
	}

	/**
	 * Creates a {@link TileRef} with the given name.
	 */
	private static TileRef newTileRef(TileContainerComponent container, String tileName) {
		TileRef reference = TypedConfiguration.newConfigItem(TileRef.class);
		reference.setName(tileName);
		boolean allowed;
		LayoutComponent tileComponent = container.getTileComponent(reference);
		if (tileComponent == null) {
			// component not yet loaded
			allowed = true;
		} else {
			allowed = TileUtils.componentAllowed(tileComponent);
		}
		container.setTileAllowed(reference, allowed);
		return reference;
	}

	/**
	 * Determines the stack of context tiles in the current layout.
	 * 
	 * <p>
	 * The first element in the list is a context free tile. The n+1^th tile is a context tile which
	 * needs the n^th element to be displayed.
	 * </p>
	 */
	private static List<String> getTileRefStack(TileContainerComponent tileContainer) {
		ArrayList<String> tileRefStack = new ArrayList<>();
		TileLayout layout = tileContainer.displayedLayout();
		while (true) {
			if (layout instanceof TileRef) {
				tileRefStack.add(((TileRef) layout).getName());
			}
			ConfigurationItem container = layout.container();
			if (!(container instanceof TileLayout)) {
				// reached end of layout hierarchy
				break;
			}
			layout = (TileLayout) container;
		}
		
		Collections.reverse(tileRefStack);
		return tileRefStack;
	}

	/**
	 * Whether the given {@link LayoutComponent} is allowed.
	 * 
	 * @param component
	 *        The component to check.
	 * @return <code>true</code> iff the {@link LayoutComponent} is a {@link BoundChecker} and
	 *         {@link BoundCheckerComponent#allow()}, or it is not a {@link BoundChecker}.
	 */
	public static boolean componentAllowed(LayoutComponent component) {
		boolean childAllowed;
		if (component instanceof BoundCheckerComponent) {
			childAllowed = ((BoundCheckerComponent) component).allow();
		} else {
			childAllowed = true;
		}
		return childAllowed;
	}

	/**
	 * The {@link TileLayout} container of the given tile, or <code>null</code>, if there is
	 *         none.
	 */
	public static TileLayout getContainer(TileLayout tile) {
		ConfigurationItem container = tile.container();
		if (container instanceof TileLayout) {
			return (TileLayout) container;
		}
		/* Tile has either no container or is part of a ConfigurationItem that is not a TileLayout.
		 * This happens for the root tile. */
		return null;
	}

	/**
	 * Whether the given <code>potentialAncestor</code> is an ancestor of the given
	 * <code>tile</code>, i.e. whether the iterated call to {@link #getContainer(TileLayout)}
	 * starting from <code>tile</code> leads to the <code>potentialAncestor</code>.
	 * 
	 */
	public static boolean isAncestor(TileLayout tile, TileLayout potentialAncestor) {
		while (true) {
			tile = getContainer(tile);
			if (tile == null) {
				return false;
			}
			if (potentialAncestor == tile) {
				return true;
			}
		}
	}

	/**
	 * Loads the {@link TileLayout} stored under the given key from the personal configuration.
	 * 
	 * @param expectedType
	 *        The expected {@link TileLayout} type of the stored item.
	 * @param key
	 *        Key to lookup in the personal configuration.
	 * @return The stored {@link TileLayout}, or <code>null</code> when there is no personal
	 *         configuration, or nothing was stored.
	 * @throws ConfigurationException
	 *         When the stored configuration could not be parsed to an {@link TileLayout} of the
	 *         expected type.
	 */
	public static <T extends ConfigurationItem> T getPersonalLayout(Class<T> expectedType, String key)
			throws ConfigurationException {
		PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfig == null) {
			// Happens when layout is loading first time.
			return null;
		}
		Object storedLayoutConfig = personalConfig.getValue(key);
		if (storedLayoutConfig == null) {
			return null;
		}
		ConfigurationItem storedConfigItem =
			TypedConfiguration.parse(CharacterContents.newContent((String) storedLayoutConfig));
		if (!expectedType.isInstance(storedConfigItem)) {
			String expected = expectedType.getName();
			String actual = storedConfigItem.descriptor().getConfigurationInterface().getName();
			ResKey error = I18NConstants.ILLEGAL_STORED_LAYOUT_TYPE__EXPECTED__ACTUAL.fill(expected, actual);
			throw new ConfigurationException(error, key, (String) storedLayoutConfig);
		}
		return expectedType.cast(storedConfigItem);
	}

	/**
	 * Stores the given {@link TileLayout} under the given key.
	 * 
	 * @param layout
	 *        The {@link TileLayout} to store. May be <code>null</code> to remove the stored
	 *        configuartion.
	 * @param key
	 *        The key used to store the given {@link TileLayout}
	 */
	public static void store(TileLayout layout, String key) {
		PersonalConfiguration personalConfig = PersonalConfiguration.getPersonalConfiguration();
		personalConfig.setValue(key, TypedConfiguration.toString(layout));
	}

	/**
	 * Searches for the next {@link TileContainerComponent} in the
	 * {@link LayoutComponent#getParent() parent hierarchy} of the given {@link LayoutComponent}.
	 */
	public static TileContainerComponent enclosingTileContainer(LayoutComponent component) {
		while (component != null) {
			if (component instanceof TileContainerComponent) {
				return (TileContainerComponent) component;
			}
			component = component.getParent();
		}
		return null;
	}

	/**
	 * Creates a new {@link Config} from the given {@link ComponentParameters parameters}.
	 * 
	 * @param log
	 *        {@link Protocol} to log problems.
	 * @param nameScope
	 *        Scope for the local component names in the resulting {@link Config}. May be
	 *        <code>null</code> to not automatically qualify component names.
	 * @param definition
	 *        The {@link ComponentParameters} containing the necessary parameters.
	 * @return May be <code>null</code>, when errors occurred.
	 */
	public static Config resolveComponentConfiguration(Protocol log, String nameScope, ComponentParameters definition) {
		LayoutResolver layoutResolver = LayoutResolver.newRuntimeResolver(log);
		Document layoutDocument = loadLayout(log, definition);
		if (layoutDocument == null) {
			return null;
		}
		BinaryData data = FileManager.getInstance()
			.getDataOrNull(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + definition.getTemplate());
		if (data == null) {
			log.error("No data found for '" + definition.getTemplate() + "'.");
			return null;
		}
		ConstantLayout layout = new ConstantLayout(layoutResolver, "dynamicLoaded", data, layoutDocument);
		LayoutInline inliner = new LayoutInline(layoutResolver);
		try {
			inliner.inline(layout);
		} catch (ResolveFailure ex) {
			log.error("Unable to inline layout", ex);
			return null;

		}

		BinaryContent content = DOMUtil.toBinaryContent(layout.getLayoutDocument(), "layout:" + layout.getLayoutName());
		Config config;
		try {
			config = LayoutUtils.createLayoutConfiguration(new DefaultInstantiationContext(log),
				ThemeFactory.getTheme(), layout.getLayoutName(), content, nameScope);
		} catch (ConfigurationException ex) {
			log.error("Problem creating configuration.", ex);
			return null;
		}
		return config;

	}

	private static Document loadLayout(Protocol log, ComponentParameters definition) {
		StringWriter out = new StringWriter();
		try {
			try (ConfigurationWriter configurationWriter = new ConfigurationWriter(out)) {
				configurationWriter.write(LayoutModelConstants.INCLUDE_ELEMENT, definition.descriptor(), definition);
			}
		} catch (XMLStreamException ex) {
			log.error("Unable to serialize layout definition", ex);
			return null;
		}
		return DOMUtil.parse(out.getBuffer().toString());
	}

	/**
	 * Whether the given {@link LayoutComponent} is displayed (directly or as descendant) in the
	 * given {@link TileLayout}.
	 */
	public static boolean displayedInTile(LayoutComponent component, TileContainerComponent container,
			TileLayout layout) {
		LayoutComponent tileComponent = container.getTileComponent(layout);
		if (tileComponent == null) {
			return false;
		}
		while (component != null && component != container) {
			if (component == tileComponent) {
				return true;
			}
			component = component.getParent();
		}
		return false;
	}

}

