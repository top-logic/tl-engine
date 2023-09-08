/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.tabbar.TabInfo.TabConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
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
	 * Layout scope of the given key. If no scope exists then use the project root directory as
	 * fallback scope.
	 */
	public static String getRootLayoutScope(String layoutKey) {
		int lastIndexOf = layoutKey.lastIndexOf(FileUtilities.PATH_SEPARATOR);

		if (lastIndexOf != -1) {
			return layoutKey.substring(0, lastIndexOf + 1);
		} else {
			return getProjectRootDirectory();
		}
	}

	private static String getProjectRootDirectory() {
		return Workspace.topLevelProjectDirectory().getName() + FileUtilities.PATH_SEPARATOR;
	}

	/**
	 * Returns the direct scope (only respecting tabs and dialogs) for the given component.
	 */
	public static String getLayoutScope(LayoutComponent component) {
		StringBuilder layoutScopeBuilder = new StringBuilder();

		appendTabLayoutScope(layoutScopeBuilder, component);
		appendTileLayoutScope(layoutScopeBuilder, component);
		appendDialogLayoutScope(layoutScopeBuilder, component);

		return layoutScopeBuilder.toString();
	}

	/**
	 * Similar to {@link #getLayoutScope(LayoutComponent)} but returns an unique scope.
	 */
	public static String getUniqueLayoutScope(Collection<String> existingScopes, String parentScope, String scope) {
		if (StringServices.isEmpty(scope)) {
			return StringServices.EMPTY_STRING;
		} else {
			return getUniqueScopeInternal(existingScopes, parentScope, scope);
		}
	}

	/**
	 * Returns an unique filename for the given scope.
	 */
	public static String getUniqueFilename(Collection<String> existingFilenames, String scope, String fullScope,
			LayoutComponent component) {
		if (!StringServices.isEmpty(scope)) {
			return getFilename(component) + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;
		} else {
			return getUniqueFilename(existingFilenames, getFilename(component), fullScope);
		}
	}

	private static String getUniqueFilename(Collection<String> filenames, String filename, String fullScope) {
		String uniqueFilename = filename + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;

		int counter = 2;

		while (isUniqueFilename(filenames, fullScope, uniqueFilename)) {
			uniqueFilename = filename + counter + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX;

			counter++;
		}

		return uniqueFilename;
	}

	private static String getFilename(LayoutComponent component) {
		String filename = getTitleName(component) + " " + getLocalName(component);

		return getCompatibleFilenamePart(filename);
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

	private static boolean isUniqueFilename(Collection<String> filenames, String layoutScope, String filename) {
		String path = layoutScope + filename;

		return filenames.contains(path) || isInvalidFile(path);
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

	private static boolean isInvalidFile(String layoutPath) {
		File layoutsDirectory = LayoutUtils.getCurrentTopLayoutBaseDirectory();

		File file = new File(layoutsDirectory, layoutPath);

		return file.exists();
	}

	private static String getUniqueScopeInternal(Collection<String> existingScopes, String parentScope, String scope) {
		String uniqueChildScope = scope;

		int counter = 2;

		while (existingScopes.contains(uniqueChildScope) || isInvalidFile(parentScope + uniqueChildScope)) {
			uniqueChildScope = scope + counter;

			counter++;
		}

		return uniqueChildScope;
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
