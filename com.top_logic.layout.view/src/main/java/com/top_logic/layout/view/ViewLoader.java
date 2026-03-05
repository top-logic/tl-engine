/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Shared utility for loading and caching parsed {@link ViewElement} instances.
 *
 * <p>
 * View XML files are parsed via {@link TypedConfiguration} into a shared {@link ViewElement} tree
 * (Phase 1). The cache checks the source file's modification timestamp and re-parses when the file
 * has changed.
 * </p>
 */
public class ViewLoader {

	/** Base path for view XML files within the webapp. */
	public static final String VIEW_BASE_PATH = "/WEB-INF/views/";

	private static final ConcurrentHashMap<String, CachedView> CACHE = new ConcurrentHashMap<>();

	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 *
	 * @param viewPath
	 *        Full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The parsed {@link ViewElement}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		File file = FileManager.getInstance().getIDEFileOrNull(viewPath);
		long currentModified = file != null ? file.lastModified() : 0L;

		CachedView cached = CACHE.get(viewPath);
		if (cached != null && cached._lastModified == currentModified) {
			return cached._view;
		}

		ViewElement view = loadView(viewPath);
		CACHE.put(viewPath, new CachedView(view, currentModified));
		return view;
	}

	/**
	 * Parses a {@code .view.xml} file into a {@link ViewElement}.
	 *
	 * @param viewPath
	 *        Full path to the view file.
	 * @return The parsed {@link ViewElement}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement loadView(String viewPath) throws ConfigurationException {
		BinaryData source = FileManager.getInstance().getDataOrNull(viewPath);
		if (source == null) {
			throw new ConfigurationException("View file not found: " + viewPath);
		}

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewLoader.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource((Content) source);

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		UIElement uiElement = context.getInstance(config);
		if (!(uiElement instanceof ViewElement)) {
			throw new ConfigurationException(
				"Expected ViewElement but got: " + uiElement.getClass().getName());
		}
		return (ViewElement) uiElement;
	}

	private static final class CachedView {

		final ViewElement _view;

		final long _lastModified;

		CachedView(ViewElement view, long lastModified) {
			_view = view;
			_lastModified = lastModified;
		}
	}
}
