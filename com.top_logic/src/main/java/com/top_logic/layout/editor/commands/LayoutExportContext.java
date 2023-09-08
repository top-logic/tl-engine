/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.FileManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Context used by the {@link ExportLayoutCommandHandler}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutExportContext {

	private Map<String, TLLayout> _templatesByLayoutKey = new HashMap<>();

	private Map<String, String> _exportNameByLayoutKey = new HashMap<>();

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
			String rootLayoutKey = getLayoutKey(root);

			_exportNameByLayoutKey.put(rootLayoutKey, rootLayoutKey);

			putExportNameByLayoutKeys(root, LayoutExportUtils.getRootLayoutScope(rootLayoutKey));
		}
	}

	private void putExportNameByLayoutKeys(LayoutComponent parent, String parentScope) {
		Collection<LayoutComponent> children = _childrenByComponent.get(parent);

		if (children != null) {
			putExportNameByLayoutKeysInternal(parentScope, children);
		}
	}

	private void putExportNameByLayoutKeysInternal(String parentScope, Collection<LayoutComponent> children) {
		Collection<String> childScopes = new HashSet<>();

		for (LayoutComponent child : children) {
			if (isNewCreatedComponent(child)) {
				String childLayoutScope = LayoutExportUtils.getLayoutScope(child);
				String uniqueChildLayoutScope =	LayoutExportUtils.getUniqueLayoutScope(childScopes, parentScope, childLayoutScope);
				String layoutScope = LayoutExportUtils.getFullLayoutScope(parentScope, uniqueChildLayoutScope);
				String uniqueFilename = getUniqueFilename(child, childLayoutScope, layoutScope);

				_exportNameByLayoutKey.put(getLayoutKey(child), layoutScope + uniqueFilename);

				putExportNameByLayoutKeys(child, layoutScope);

				childScopes.add(uniqueChildLayoutScope);
			}
		}
	}

	private String getUniqueFilename(LayoutComponent child, String childLayoutScope, String layoutScope) {
		Collection<String> layoutKeys = _exportNameByLayoutKey.values();

		return LayoutExportUtils.getUniqueFilename(layoutKeys, childLayoutScope, layoutScope, child);
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

	private void initExistingComponents() {
		MainLayout mainLayout = MainLayout.getDefaultMainLayout();

		for (String layoutKey : _templatesByLayoutKey.keySet()) {
			if (isExistingComponent(layoutKey)) {
				LayoutComponent component = mainLayout.getComponentForLayoutKey(layoutKey);

				if (component != null) {
					_existingComponents.add(component);
				}
			}
		}
	}

	private boolean isExistingComponent(String key) {
		String path = LayoutConstants.LAYOUT_BASE_RESOURCE + "/" + key;

		return FileManager.getInstance().getIDEFileOrNull(path) != null;
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
