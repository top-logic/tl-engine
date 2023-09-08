/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeConfig;

/**
 * A resource (file or directory) provided by a {@link ThemeConfig}.
 * 
 * @see #getResources(ThemeConfig, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeResource {

	/**
	 * The root resource of the given {@link ThemeConfig}.
	 */
	public static ThemeResource getResources(ThemeConfig theme, boolean loadInheritedResources) throws IOException {
		ThemeResource result = new ThemeResource(theme, null, null).loadFiles(theme, loadInheritedResources);

		if (result._files.size() == 0) {
			return null;
		}

		return result;
	}

	private ThemeResource loadFiles(ThemeConfig theme, boolean loadInheritedResources) throws IOException {
		addThemeFiles(theme);

		if (loadInheritedResources) {
			loadInheritedFiles(theme, loadInheritedResources);
		}

		return this;
	}

	private void loadInheritedFiles(ThemeConfig theme, boolean loadInheritedResources) throws IOException {
		List<String> baseIDs = theme.getExtends();

		if (baseIDs != null) {
			for (String baseId : baseIDs) {
				ThemeConfig parent = ((MultiThemeFactory) ThemeFactory.getInstance()).getThemeConfig(baseId);

				if (parent != null) {
					loadFiles(parent, loadInheritedResources);
				}
			}
		}
	}

	private void addThemeFiles(ThemeConfig theme) {
		addThemeFile(theme, theme.getPathEffective() + "/");
	}

	/**
	 * Adds a theme file to the current theme resource.
	 */
	public boolean addThemeFile(ThemeConfig theme, String resourcePath) {
		return addThemeFile(new ThemeFile(theme, resourcePath));
	}

	/**
	 * Adds a theme file to the current theme resource.
	 */
	public boolean addThemeFile(ThemeFile themeFile) {
		return _files.add(themeFile);
	}

	private final ThemeConfig _theme;

	private final ThemeResource _parent;

	private final List<ThemeFile> _files;

	private ResourceType _type;

	/**
	 * Creates a root {@link ThemeResource}.
	 * 
	 * @see #getResources(ThemeConfig, boolean)
	 */
	public ThemeResource(ThemeConfig theme, ThemeResource parent, ResourceType type) {
		_theme = theme;
		_parent = parent;
		_type = type;
		_files = new ArrayList<>();
	}

	/**
	 * The {@link ThemeConfig} this resource is part of.
	 */
	public ThemeConfig getTheme() {
		return _theme;
	}

	/**
	 * Files for the given theme resources.
	 */
	public List<ThemeFile> getThemeFiles() {
		return _files;
	}

	/**
	 * The ID of the {@link ThemeConfig} this resource lives in.
	 */
	public String getDefiningThemeID() {
		return getDefiningTheme().getId();
	}

	/**
	 * The {@link ThemeConfig} this resource lives in.
	 */
	public ThemeConfig getDefiningTheme() {
		return lead().getDefiningTheme();
	}

	/**
	 * The base {@link ThemeConfig} this resource overrides.
	 */
	public String getBaseThemeID() {
		return _files.get(_files.size() - 1).getDefiningTheme().getId();
	}

	/**
	 * The file name of this resource.
	 */
	public String getName() {
		return FileUtilities.getFilenameOfResource(lead().getResourcePath());
	}

	/**
	 * Whether this resource may have content resources.
	 */
	public boolean isDirectory() {
		return FileManager.getInstance().isDirectory(lead().getResourcePath());
	}

	/**
	 * The parent (directory) of this resource.
	 */
	public ThemeResource getParent() {
		return _parent;
	}

	/**
	 * The type of this resource.
	 */
	public ResourceType getType() {
		return _type;
	}


	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Lists all contents of this (directory) resource.
	 *
	 * @return Sorted list of content resources, or an empty list, if this resource
	 *         {@link #isDirectory() is not a directory}.
	 */
	public List<ThemeResource> list(Predicate<String> resourceFilter) {
		if (!isDirectory()) {
			return Collections.emptyList();
		}

		Map<String, ThemeResource> index = new HashMap<>();
		FileManager fileManager = FileManager.getInstance();

		for (ThemeFile file : _files) {
			Set<String> resourcePaths = fileManager.getResourcePaths(file.getResourcePath()).stream()
				.filter(resourceFilter).collect(Collectors.toSet());
			if (resourcePaths.isEmpty()) {
				continue;
			}

			for (String resourcePath : resourcePaths) {
				String resourceName = FileUtilities.getFilenameOfResource(resourcePath);
				ThemeResource overlay = index.get(resourceName);

				ThemeFile content = new ThemeFile(file.getDefiningTheme(), resourcePath);
				if (overlay == null) {
					ResourceType type = calcResourceType(resourcePath);
					index.put(resourceName, new ThemeResource(getTheme(), this, type).add(content));
				} else {
					overlay.add(content);
				}
			}
		}

		ArrayList<ThemeResource> result = new ArrayList<>(index.values());
		Collections.sort(result, (r1, r2) -> r1.getName().compareTo(r2.getName()));
		return result;
	}

	/**
	 * Lists all contents of this (directory) resource. Filter for all {@link ResourceType}s.
	 * 
	 * @return Sorted list of content resources, or an empty list, if this resource
	 *         {@link #isDirectory() is not a directory}.
	 */
	public List<ThemeResource> list() {
		return list(createThemeResourcePathFilter());
	}

	private Predicate<String> createThemeResourcePathFilter() {
		return resourcePath -> {
			for (ResourceType resourceFilter : ResourceType.values()) {
				boolean isAccepted = resourceFilter.accept(resourcePath);
				if (isAccepted) {
					return true;
				}
			}

			return false;
		};
	}

	private ResourceType calcResourceType(String resourcePath) {
		ResourceType type = null;

		for (ResourceType typeToTest : ResourceType.values()) {
			if (typeToTest.accept(resourcePath)) {
				type = typeToTest;
			}
		}

		return type;
	}

	private ThemeResource add(ThemeFile file) {
		addThemeFile(file);
		return this;
	}

	private ThemeFile lead() {
		return _files.get(0);
	}

	/**
	 * Relative path in the {@link ThemeConfig} that would resolve to the resource represented by
	 * this {@link ThemeResource} when resolved relative to the {@link ThemeConfig} from which this
	 * resource was built.
	 */
	public String getThemeKey() {
		StringBuilder buffer = new StringBuilder();
		buildKey(buffer, this);
		return buffer.toString();
	}

	private static void buildKey(StringBuilder buffer, ThemeResource resource) {
		if (isRoot(resource)) {
			// The folder with the theme ID is not part of the theme key.
			return;
		}

		buildKey(buffer, resource.getParent());
		buffer.append('/');
		buffer.append(resource.getName());
	}

	private static boolean isRoot(ThemeResource resource) {
		return resource.getParent() == null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_files == null) ? 0 : _files.hashCode());
		result = prime * result + ((_parent == null) ? 0 : _parent.hashCode());
		result = prime * result + ((_theme == null) ? 0 : _theme.hashCode());
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThemeResource other = (ThemeResource) obj;
		if (_files == null) {
			if (other._files != null)
				return false;
		} else if (!_files.equals(other._files))
			return false;
		if (_theme == null) {
			if (other._theme != null)
				return false;
		} else if (!_theme.equals(other._theme))
			return false;
		if (_type != other._type)
			return false;
		if (_parent == null) {
			if (other._parent != null)
				return false;
		} else if (!_parent.equals(other._parent))
			return false;
		return true;
	}

	/**
	 * A single resource defined in a {@link ThemeConfig}.
	 */
	public static class ThemeFile {

		private ThemeConfig _definingTheme;

		private String _resourcePath;

		/**
		 * Creates a {@link ThemeFile}.
		 */
		public ThemeFile(ThemeConfig definingTheme, String resourcePath) {
			_definingTheme = definingTheme;
			_resourcePath = resourcePath;
		}

		/**
		 * The {@link ThemeConfig} that defines this {@link #getResourcePath()}.
		 */
		public ThemeConfig getDefiningTheme() {
			return _definingTheme;
		}

		/**
		 * Pointer to the resource.
		 */
		public String getResourcePath() {
			return _resourcePath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_definingTheme == null) ? 0 : _definingTheme.hashCode());
			result = prime * result + ((_resourcePath == null) ? 0 : _resourcePath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ThemeFile other = (ThemeFile) obj;
			if (_definingTheme == null) {
				if (other._definingTheme != null)
					return false;
			} else if (!_definingTheme.equals(other._definingTheme))
				return false;
			if (_resourcePath == null) {
				if (other._resourcePath != null)
					return false;
			} else if (!_resourcePath.equals(other._resourcePath))
				return false;
			return true;
		}

	}

}
