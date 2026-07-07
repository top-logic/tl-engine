/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Build-time pre-merge of same-path view overlays for a deployed (flattened) application.
 *
 * <p>
 * At runtime in a development workspace, {@link ViewLoader} merges the stacked module copies of a
 * view on the fly (the resource path still has one root per module). A deployed WAR flattens all
 * module resources into a single tree, so that multiplicity is lost: only the highest-priority copy
 * of a view survives. This merger runs during application assembly - while the dependency-ordered
 * {@link FileManager} still exposes every module copy - and writes the merged view into the target
 * web application, so the deployed application serves the same result the workspace does.
 * </p>
 *
 * @implNote Invoked reflectively from the application-WAR build (LayoutCreator) so that the core
 *           layer need not depend on this module. Reuses {@link ViewLoader#loadConfig(String)} for
 *           the actual overlay fold, so build and runtime share one merge implementation.
 */
public class ViewOverlayMerger {

	/** Resource prefix of the view files that are subject to overlay merging. */
	private static final String VIEWS_PREFIX = ViewLoader.VIEW_BASE_PATH;

	/** Suffix identifying a view file. */
	private static final String VIEW_SUFFIX = ".view.xml";

	/**
	 * Merges every view that has more than one same-path copy across the modules on the current
	 * {@link FileManager} and writes the merged result into the given target web application.
	 *
	 * @param targetWebappDirectory
	 *        The expanded web application the deployed WAR is built from.
	 */
	@CalledByReflection
	public static void mergeOverlaidViews(File targetWebappDirectory) {
		FileManager fileManager = FileManager.getInstance();

		List<String> viewPaths = new ArrayList<>();
		collectViews(fileManager, VIEWS_PREFIX, viewPaths);

		for (String viewPath : viewPaths) {
			List<BinaryData> overlays;
			try {
				overlays = fileManager.getDataOverlays(viewPath);
			} catch (IOException ex) {
				Logger.error("Cannot resolve view overlays: " + viewPath, ex, ViewOverlayMerger.class);
				continue;
			}
			if (overlays.size() <= 1) {
				// Single copy: the WAR overlay already placed the file; nothing to merge.
				continue;
			}
			try {
				writeMerged(targetWebappDirectory, viewPath, ViewLoader.loadConfig(viewPath));
				Logger.info("Merged " + overlays.size() + " overlays for view: " + viewPath,
					ViewOverlayMerger.class);
			} catch (Exception ex) {
				Logger.error("Failed to merge view overlays: " + viewPath, ex, ViewOverlayMerger.class);
			}
		}
	}

	private static void collectViews(FileManager fileManager, String dir, List<String> result) {
		for (String resource : fileManager.getResourcePaths(dir)) {
			if (fileManager.isDirectory(resource)) {
				collectViews(fileManager, resource, result);
			} else if (resource.endsWith(VIEW_SUFFIX)) {
				result.add(resource);
			}
		}
	}

	private static void writeMerged(File targetWebappDirectory, String viewPath, ViewElement.Config config)
			throws IOException, XMLStreamException {
		String relative = viewPath.startsWith("/") ? viewPath.substring(1) : viewPath;
		File target = new File(targetWebappDirectory, relative);
		File parent = target.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
			try (ConfigurationWriter configWriter = new ConfigurationWriter(writer)) {
				configWriter.write("view", ViewElement.Config.class, config);
			}
		}
	}

}
