/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link TreeModelBuilder} building the directory tree of theme resources of the
 * {@link ThemeConfig} given as business model.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeDirectoryTreeBuilder implements TreeModelBuilder<ThemeResource> {

	private static final String SHOW_INHERITED_FOLDERS_NAME = "showInheritedFolders";

	private static final Property<Boolean> SHOW_INHERITED_FOLDERS = createInheritedFoldersProperty();

	/**
	 * Singleton {@link ThemeDirectoryTreeBuilder} instance.
	 */
	public static final ThemeDirectoryTreeBuilder INSTANCE = new ThemeDirectoryTreeBuilder();

	private ThemeDirectoryTreeBuilder() {
		// Singleton constructor.
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof ThemeConfig;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		if (businessModel == null) {
			return null;
		}
		try {
			return ThemeResource.getResources((ThemeConfig) businessModel, showInheritedFolders(component));
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		return node instanceof ThemeResource;
	}

	@Override
	public Iterator<? extends ThemeResource> getChildIterator(LayoutComponent contextComponent, ThemeResource node) {
		if (node == null) {
			return Collections.<ThemeResource> emptyList().iterator();
		}

		return node.list(resourcePath -> FileManager.getInstance().isDirectory(resourcePath)).iterator();
	}

	@Override
	public Collection<? extends ThemeResource> getParents(LayoutComponent contextComponent, ThemeResource node) {
		return CollectionUtilShared.singletonOrEmptyList(node.getParent());
	}

	@Override
	public boolean canExpandAll() {
		return true;
	}

	@Override
	public boolean isLeaf(LayoutComponent contextComponent, ThemeResource node) {
		return !getChildIterator(contextComponent, node).hasNext();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, ThemeResource node) {
		return node.getTheme();
	}

	@Override
	public PreloadOperation loadForExpansion() {
		return NoPreload.INSTANCE;
	}

	private static Property<Boolean> createInheritedFoldersProperty() {
		return TypedAnnotatable.property(Boolean.class, SHOW_INHERITED_FOLDERS_NAME, Boolean.FALSE);
	}

	static boolean showInheritedFolders(LayoutComponent component) {
		return component.get(SHOW_INHERITED_FOLDERS).booleanValue();
	}

	static Object setShowInheritedFolders(LayoutComponent component, boolean newValue) {
		return component.set(SHOW_INHERITED_FOLDERS, Boolean.valueOf(newValue));
	}

	@Override
	public Collection<? extends ThemeResource> getNodesToUpdate(LayoutComponent contextComponent,
			Object businessObject) {
		return Collections.emptySet();
	}

	@Override
	public Set<TLStructuredType> getTypesToObserve() {
		return Collections.emptySet();
	}

}
