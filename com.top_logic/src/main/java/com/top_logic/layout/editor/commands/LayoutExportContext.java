/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.util.error.TopLogicException;

/**
 * Context used by the {@link ExportLayoutCommandHandler}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutExportContext {

	private Map<String, TLLayout> _templatesByLayoutKey = new HashMap<>();

	private BidiMap<String, String> _exportNameByLayoutKey = new BidiHashMap<>();

	private Map<LayoutComponent, Collection<LayoutComponent>> _childrenByComponent = new HashMap<>();

	private Set<LayoutComponent> _existingComponents = new HashSet<>();

	/**
	 * Creates the layout export context for the given {@link Person}.
	 */
	public LayoutExportContext(Person person) {
		initTemplatesByLayoutKey(person);
		initExistingComponents();
		initChildrenByComponent();
		initExportNameByLayoutKey();
	}

	private void initExportNameByLayoutKey() {
		for (LayoutComponent root : _existingComponents) {
			String layoutKey = getLayoutKey(root);
			Path path = Path.of(layoutKey);

			putExportName(layoutKey, path, true);
			putChildrenExportNames(root, getLayoutScope(path));
		}
	}

	private Path getLayoutScope(Path path) {
		Path parent = path.getParent();

		return parent == null ? Path.of(Workspace.topLevelProjectDirectory().getName()) : parent;
	}

	private boolean isNewCreatedComponent(LayoutComponent component) {
		return !isExistingComponent(component);
	}

	private boolean isExistingComponent(LayoutComponent component) {
		return _existingComponents.contains(component);
	}

	private String getLayoutKey(LayoutComponent component) {
		return MainLayout.getDefaultMainLayout().getLayoutKey(component);
	}

	private void initTemplatesByLayoutKey(Person person) {
		_templatesByLayoutKey = getLayouts(person);
	}

	private void initChildrenByComponent() {
		for (String layoutKey : _templatesByLayoutKey.keySet()) {
			LayoutComponent child = MainLayout.getDefaultMainLayout().getComponentForLayoutKey(layoutKey);

			if (child != null) {
				LayoutComponent parent = child.getParent();

				if (parent != null) {
					putChildrenByParent(parent, child);
				}
			}

		}
	}

	private void putChildrenByParent(LayoutComponent parent, LayoutComponent child) {
		_childrenByComponent.compute(getParentRoot(parent), (k, v) -> v == null ? new HashSet<>() : v).add(child);
	}

	private LayoutComponent getParentRoot(LayoutComponent parent) {
		MainLayout mainLayout = MainLayout.getDefaultMainLayout();
		LayoutComponent root = mainLayout.getComponentForLayoutKey(LayoutTemplateUtils.getNonNullNameScope(parent));
		return Objects.requireNonNull(root);
	}

	private void putChildrenExportNames(LayoutComponent parent, Path scope) {
		Collection<LayoutComponent> children = _childrenByComponent.get(parent);

		if (children != null) {
			putExportPathByLayoutKeysInternal(scope, children);
		}
	}

	private void putExportPathByLayoutKeysInternal(Path scope, Collection<LayoutComponent> children) {
		for (LayoutComponent child : children) {
			if (isNewCreatedComponent(child)) {
				Path childScope = scope.resolve(LayoutExportUtils.getLayoutScope(child)).normalize();
				Path filename = Path.of(LayoutExportUtils.getLayoutName(child));

				putExportName(getLayoutKey(child), childScope.resolve(filename), false);
				putChildrenExportNames(child, childScope);
			}
		}
	}

	private void putExportName(String layoutKey, Path path, boolean override) {
		Path exportPath = path;

		if (!override) {
			exportPath = getUniqueLayoutPath(path);
		}

		_exportNameByLayoutKey.put(layoutKey, FileUtilities.getCombinedPath(exportPath));
	}

	private Path getUniqueLayoutPath(Path path) {
		Path scope = path.getParent();
		String filename = scope.relativize(path).toString();
		String basename = getLayoutBasename(filename);

		Path uniquePath = path;
		int suffixCounter = 1;

		while (isUnique(uniquePath)) {
			uniquePath = scope.resolve(basename + suffixCounter + LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX);

			suffixCounter++;
		}

		return uniquePath;
	}

	private String getLayoutBasename(String filename) {
		if (filename.endsWith(LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX)) {
			return filename.substring(0, filename.length() - LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX.length());
		} else {
			throw new TopLogicException(I18NConstants.NO_LAYOUT_FILENAME__NAME.fill(filename));
		}
	}

	private boolean isUnique(Path path) {
		String resourceName = FileUtilities.getCombinedPath(path);

		return isUsedByExport(resourceName) || LayoutExportUtils.existsLayoutInFilesystem(resourceName);
	}

	private boolean isUsedByExport(String resourceName) {
		return _exportNameByLayoutKey.containsValue(resourceName);
	}

	private void initExistingComponents() {
		MainLayout mainLayout = MainLayout.getDefaultMainLayout();

		for (String layoutKey : _templatesByLayoutKey.keySet()) {
			if (LayoutExportUtils.existsLayoutInFilesystem(layoutKey)) {
				LayoutComponent component = mainLayout.getComponentForLayoutKey(layoutKey);

				if (component != null) {
					_existingComponents.add(component);
				}
			}
		}
	}

	private Map<String, TLLayout> getLayouts(Person person) {
		return LayoutStorage.getInstance().getLayoutsFromDatabase(ThemeFactory.getTheme(), person);
	}

	/**
	 * Returns export names by key.
	 */
	public Map<String, String> getExportNameByLayoutKey() {
		return _exportNameByLayoutKey;
	}

	/**
	 * Returns layout templates by key.
	 */
	public Map<String, TLLayout> getTemplatesByLayoutKey() {
		return _templatesByLayoutKey;
	}

}
