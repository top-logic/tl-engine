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
 * Shared utility for loading and caching parsed {@link ViewElement} instances and their
 * configurations.
 *
 * <p>
 * View XML files are parsed via {@link TypedConfiguration} into {@link ViewElement.Config}
 * descriptors (config cache) and optionally instantiated into {@link ViewElement} trees (view
 * cache). Both caches check the source file's modification timestamp and re-parse when the file has
 * changed.
 * </p>
 */
public class ViewLoader {

	/** Base path for view XML files within the webapp. */
	public static final String VIEW_BASE_PATH = "/WEB-INF/views/";

	private static final ConcurrentHashMap<String, CachedConfig> CONFIG_CACHE = new ConcurrentHashMap<>();

	private static final ConcurrentHashMap<String, CachedView> CACHE = new ConcurrentHashMap<>();

	/**
	 * Retrieves a cached {@link ViewElement.Config} or loads and caches it from the view XML file.
	 *
	 * <p>
	 * This method provides access to the parsed configuration without instantiating a
	 * {@link ViewElement}. Useful for tooling (e.g. the View Designer) that needs to inspect or
	 * modify the configuration model.
	 * </p>
	 *
	 * @param viewPath
	 *        Full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The parsed {@link ViewElement.Config}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement.Config getOrLoadConfig(String viewPath) throws ConfigurationException {
		long currentModified = currentModified(viewPath);

		CachedConfig cached = CONFIG_CACHE.get(viewPath);
		if (cached != null && cached._lastModified == currentModified) {
			return cached._config;
		}

		ViewElement.Config config = loadConfig(viewPath);
		CONFIG_CACHE.put(viewPath, new CachedConfig(config, currentModified));
		return config;
	}

	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 *
	 * <p>
	 * Delegates to {@link #getOrLoadConfig(String)} for parsing, then instantiates the
	 * configuration into a {@link ViewElement}.
	 * </p>
	 *
	 * @param viewPath
	 *        Full path to the view file (e.g. {@code /WEB-INF/views/app.view.xml}).
	 * @return The parsed {@link ViewElement}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		long currentModified = currentModified(viewPath);

		CachedView cached = CACHE.get(viewPath);
		if (cached != null && cached._lastModified == currentModified) {
			return cached._view;
		}

		ViewElement.Config config = getOrLoadConfig(viewPath);
		ViewElement view = instantiateView(config);
		CACHE.put(viewPath, new CachedView(view, currentModified));
		return view;
	}

	/**
	 * Parses a {@code .view.xml} file into a {@link ViewElement.Config} without instantiation.
	 *
	 * @param viewPath
	 *        Full path to the view file.
	 * @return The parsed {@link ViewElement.Config}.
	 * @throws ConfigurationException
	 *         if the file cannot be found or parsed.
	 */
	public static ViewElement.Config loadConfig(String viewPath) throws ConfigurationException {
		BinaryData source = resolveSource(viewPath);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewLoader.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource((Content) source);

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		return config;
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
		ViewElement.Config config = loadConfig(viewPath);
		return instantiateView(config);
	}

	/**
	 * Instantiates a {@link ViewElement} from the given configuration.
	 */
	private static ViewElement instantiateView(ViewElement.Config config) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewLoader.class);
		UIElement uiElement = context.getInstance(config);
		context.checkErrors();

		if (!(uiElement instanceof ViewElement)) {
			throw new ConfigurationException(
				"Expected ViewElement but got: " + uiElement.getClass().getName());
		}
		return (ViewElement) uiElement;
	}

	/**
	 * Resolves the view source file, throwing if not found.
	 */
	private static BinaryData resolveSource(String viewPath) throws ConfigurationException {
		BinaryData source = FileManager.getInstance().getDataOrNull(viewPath);
		if (source == null) {
			throw new ConfigurationException("View file not found: " + viewPath);
		}
		return source;
	}

	/**
	 * Returns the current modification timestamp for the given view path.
	 */
	private static long currentModified(String viewPath) {
		File file = FileManager.getInstance().getIDEFileOrNull(viewPath);
		return file != null ? file.lastModified() : 0L;
	}

	private static final class CachedConfig {

		final ViewElement.Config _config;

		final long _lastModified;

		CachedConfig(ViewElement.Config config, long lastModified) {
			_config = config;
			_lastModified = lastModified;
		}
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
