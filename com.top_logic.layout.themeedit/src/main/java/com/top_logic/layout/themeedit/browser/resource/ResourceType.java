/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.filefilter.IOFileFilter;

import com.top_logic.basic.FileManager;
import com.top_logic.layout.themeedit.browser.dialogs.UploadForm;

/**
 * Types of {@link ThemeResource}s for the theme editor. If a new type or extension is added, it
 * need to be added in {@link UploadForm} as accepted type too.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public enum ResourceType implements IOFileFilter {
	/** Font resource to describe the appearance of a text. */
	FONT(Arrays.asList("ttf", "eot", "woff", "woff2", "fnt", "otf", "afm", "pfm", "inf", "pfb")),
	/** Icon resource. */
	ICON(Arrays.asList("png", "gif", "svg", "jpg"));

	private List<String> _extensions;

	private static final List<String> EXTENSIONS = new ArrayList<>();

	private ResourceType(List<String> extensions) {
		_extensions = extensions;
	}

	/**
	 * Returns a list of all extensions of this type.
	 */
	public List<String> getExtensions() {
		return _extensions;
	}

	/**
	 * Returns a list of all extensions of all types.
	 */
	public static List<String> getAllExtensions() {
		if (EXTENSIONS.isEmpty()) {
			createExtensionsList();
		}

		return EXTENSIONS;
	}

	private static void createExtensionsList() {
		for (ResourceType type : values()) {
			EXTENSIONS.addAll(type.getExtensions());
		}
	}

	private boolean hasExtension(String name) {
		boolean result = false;

		for (String extension : getExtensions()) {
			result = result || name.endsWith(extension);
		}

		return result;
	}

	/**
	 * Checks if the resource by the given path is a normal file and ends with one of its configured
	 * extensions.
	 */
	public boolean accept(String resourcePath) {
		return !FileManager.getInstance().isDirectory(resourcePath) && hasExtension(resourcePath);
	}

	@Override
	public boolean accept(File file) {
		return file.isFile() && hasExtension(file.getName());
	}

	@Override
	public boolean accept(File dir, String name) {
		return hasExtension(name);
	}
}