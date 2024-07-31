/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.tiles.ContextTileComponent;
import com.top_logic.mig.html.layout.tiles.TileInfo;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLResKeyUtil;

/**
 * Utilities for the layout export.
 * 
 * @see ExportLayoutCommandHandler
 * @see LayoutExportContext
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutExportUtils {

	/**
	 * Returns the local scope (only respecting tabs and dialogs) for the given component.
	 */
	public static String getLocalScope(LayoutComponent component) {
		StringBuilder layoutScopeBuilder = new StringBuilder();

		appendTabLayoutScope(layoutScopeBuilder, component);
		appendTileLayoutScope(layoutScopeBuilder, component);
		appendDialogLayoutScope(layoutScopeBuilder, component);

		return layoutScopeBuilder.toString();
	}

	/**
	 * Layout resource name for a {@link LayoutComponent component}.
	 */
	public static String getLayoutName(LayoutComponent component) {
		return getBasename(component) + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
	}

	private static String getBasename(LayoutComponent component) {
		StringBuilder builder = new StringBuilder();

		builder.append(getTitleName(component));
		builder.append(" ");
		builder.append(getLocalName(component));

		return getCompatibleFilenamePart(builder.toString());
	}

	private static String getLocalName(LayoutComponent component) {
		return component.getName().localName();
	}

	private static String getTitleName(LayoutComponent component) {
		Optional<ResKey> titleKey = findFirstInnerTabTitleKey(component);

		return titleKey.map(key -> getText(key)).orElse(StringServices.EMPTY_STRING);
	}

	private static Optional<ResKey> findFirstInnerTabTitleKey(LayoutComponent component) {
		while (component != null) {
			if (isTabComponent(component)
				|| LayoutUtils.isRootDialog(component)
				|| getTileInfo(component) != null) {
				break;
			}

			ResKey titleKey = component.getTitleKey();

			if (TLResKeyUtil.existsResource(titleKey) && (component.getConfig().getTabInfo() == null)) {
				return Optional.of(titleKey);
			}

			component = component.getParent();
		}

		return Optional.empty();
	}

	private static boolean isTabComponent(LayoutComponent component) {
		return component instanceof TabComponent;
	}

	/**
	 * Returns the complete layout scope.
	 */
	public static String getFullLayoutScope(String parentScope, String childScope) {
		if (StringServices.isEmpty(childScope)) {
			return parentScope;
		}

		return parentScope + childScope + FileUtilities.PATH_SEPARATOR;
	}

	/**
	 * Whether a resource with the given name exists in the application layout directory.
	 * 
	 * @param name
	 *        Resource name
	 * 
	 * @see FileManager#exists(String)
	 * @see LayoutConstants#LAYOUT_BASE_DIRECTORY
	 */
	public static boolean existsLayoutInFilesystem(String name) {
		return FileManager.getInstance().exists(LayoutConstants.LAYOUT_BASE_RESOURCE + "/" + name);
	}

	private static void appendTabLayoutScope(StringBuilder layoutScopeBuilder, LayoutComponent component) {
		TabConfig tabInfo = component.getConfig().getTabInfo();

		if (tabInfo != null) {
			appendFilenamePart(layoutScopeBuilder, tabInfo.getLabel());
		}
	}

	private static void appendTileLayoutScope(StringBuilder layoutScopeBuilder, LayoutComponent component) {
		TileInfo tileInfo = getTileInfo(component);
		if (tileInfo != null) {
			appendFilenamePart(layoutScopeBuilder, tileInfo.getLabel());
		} else {
			LayoutComponent parent = component.getParent();
			if (parent instanceof ContextTileComponent) {
				String parentScope = parent.getName().scope();
				LayoutComponent parentRoot = component.getMainLayout().getComponentForLayoutKey(parentScope);
				appendFilenamePart(layoutScopeBuilder, getText(parentRoot.getTitleKey()));
			}
		}
	}

	private static TileInfo getTileInfo(LayoutComponent component) {
		return component.getConfig().getTileInfo();
	}

	private static void appendDialogLayoutScope(StringBuilder layoutScopeBuilder, LayoutComponent component) {
		if (LayoutUtils.isRootDialog(component)) {
			appendFilenamePart(layoutScopeBuilder, LayoutUtils.getDialogTitle(component));
		}
	}

	private static void appendFilenamePart(StringBuilder layoutScopeBuilder, ResKey filenameKey) {
		appendFilenamePart(layoutScopeBuilder, getText(filenameKey));
	}

	private static void appendFilenamePart(StringBuilder builder, String filenamePart) {
		if (!StringServices.isEmpty(filenamePart)) {
			builder.append(getCompatibleFilenamePart(filenamePart));
		}
	}

	private static String getText(ResKey key) {
		Locale locale = ResourcesModule.localeFromString("en");

		return Resources.getInstance(locale).getString(key);
	}

	private static String getCompatibleFilenamePart(String filenamePart) {
		String camelCaseFilenamePart = StringServices.toCamelCase(filenamePart.trim(), "\\s+");

		return LayoutUtils.getCompatibleLayoutFilenamePart(camelCaseFilenamePart);
	}

	/**
	 * Deletes the layout by the given layout key from the database.
	 * 
	 * @see LayoutExportUtils#deletePersistentLayoutTemplates(Set)
	 */
	public static void deletePersistentLayoutTemplates(String layoutKey) {
		deletePersistentLayoutTemplates(Collections.singleton(layoutKey));
	}

	/**
	 * Deletes the layouts by the given layout keys from the database.
	 */
	public static void deletePersistentLayoutTemplates(Set<String> layoutKeys) {
		Person person = TLContext.getContext().getPerson();

		for (String layoutKey : layoutKeys) {
			LayoutStorage.deleteLayoutFromDatabase(person, layoutKey);
			LayoutStorage.deleteLayoutFromDatabase(null, layoutKey);
		}
	}

	/**
	 * Deletes all layouts from the database.
	 */
	public static void deleteAllPersistentLayoutTemplates() {
		Person person = TLContext.getContext().getPerson();

		LayoutStorage.deleteLayoutsFromDatabase(person);
		LayoutStorage.deleteLayoutsFromDatabase(null);
	}

}
