/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.gui.JSFileCompiler;
import com.top_logic.gui.MergeThemeConfigs;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Tool called during deployment that applies all layout overlays and write the expanded layouts
 * into the {@link ModuleLayoutConstants#LAYOUT_RESOURCE_PREFIX layout directory} of the given
 * target webapp directory.
 * 
 * <p>
 * Additionally, the CSS and JavaScript files for the application are created.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutCreator {

	/**
	 * Name of the class in the React view module that merges the same-path view overlays, see
	 * {@link #MERGE_OVERLAID_VIEWS_METHOD}.
	 */
	private static final String VIEW_OVERLAY_MERGER_CLASS = "com.top_logic.layout.view.ViewOverlayMerger";

	/**
	 * Name of the static method of {@link #VIEW_OVERLAY_MERGER_CLASS} that merges the view
	 * overlays, taking the {@link Protocol} to report failures to and the target web application
	 * directory.
	 */
	private static final String MERGE_OVERLAID_VIEWS_METHOD = "mergeOverlaidViews";

	/**
	 * Creates the application layouts, CSS and JavaScript.
	 * 
	 * @param resourcePaths
	 *        Root paths to collect all layouts from.
	 * @param targetWebappDirectory
	 *        Web application directory to write the generation results to.
	 * @throws RuntimeException
	 *         If the application configuration cannot be loaded, or if a layout or a view cannot
	 *         be created. The message names each such layout and view and its cause, so that the
	 *         application build fails instead of deploying a defective application.
	 */
	@CalledByReflection
	public static void createLayouts(List<Path> resourcePaths, File targetWebappDirectory) throws IOException {
		List<Path> allResourcePaths = new ArrayList<>();

		Path appOverlay = Files.createTempDirectory("AppWarTmp");
		Path confDirOverlay = Files.createDirectories(appOverlay.resolve(ModuleLayoutConstants.CONF_PATH));
		Path configOverlay = Files.createTempFile(confDirOverlay, "config", ".config.xml");
		Path metaConfOverlay = Files.createFile(confDirOverlay.resolve(ModuleLayoutConstants.META_CONF_NAME));

		try (FileWriter out = new FileWriter(metaConfOverlay.toFile(), Charset.forName("utf-8"))) {
			out.write(configOverlay.getFileName().toString());
		}

		try (InputStream in = LayoutCreator.class.getResourceAsStream("LayoutCreator.config.xml")) {
			FileUtilities.copyToFile(in, configOverlay.toFile());
		}

		Logger.info("Using app overlay: " + appOverlay.toString(), LayoutCreator.class);

		allResourcePaths.add(appOverlay);
		allResourcePaths.addAll(resourcePaths);

		FileManager fileManager = MultiFileManager.createMultiFileManager(allResourcePaths);
		FileManager oldFileManager = FileManager.getInstance();
		FileManager.setInstance(fileManager);
		BufferingProtocol log = new BufferingProtocol();
		try {
			CompileTimeApplication application =
				CompileTimeApplication.createApplicationModule(targetWebappDirectory);

			startXMLProperties(log);
			log.checkErrors();

			try (ModuleContext ctx = ModuleUtil.beginContext()) {
				startLayoutDependantModules(log);
				log.checkErrors();

				File targetThemeDirectory = new File(targetWebappDirectory, MultiThemeFactory.THEMES_FOLDER_PATH);
				MergeThemeConfigs.writeMergedThemesTo(targetThemeDirectory);

				Map<String, List<BinaryData>> overlaysByLayoutKey = LayoutStorage.readLayouts(log);

				createLayoutsForThemes(log, targetWebappDirectory, application, overlaysByLayoutKey);
			}

			// Pre-merge same-path view overlays while the dependency-ordered FileManager still
			// exposes every module copy (a deployed WAR flattens them into one). Reflective, so the
			// core layer need not depend on the React view module.
			mergeViewOverlays(log, targetWebappDirectory);

			// One check for layouts and views, so that the failure lists every defect.
			log.checkErrors();

			// Copy generated scripts and styles.
			copy(targetWebappDirectory, appOverlay, "script");
			copy(targetWebappDirectory, appOverlay, "style");
		} finally {
			FileManager.setInstance(oldFileManager);
		}
	}

	/**
	 * Merges the same-path view overlays into the target web application.
	 *
	 * <p>
	 * Each view that cannot be merged is reported to the given {@link Protocol} with its cause.
	 * </p>
	 */
	private static void mergeViewOverlays(Protocol log, File targetWebappDirectory) {
		try {
			Class<?> merger = Class.forName(VIEW_OVERLAY_MERGER_CLASS, true,
				Thread.currentThread().getContextClassLoader());
			merger.getMethod(MERGE_OVERLAID_VIEWS_METHOD, Protocol.class, File.class)
				.invoke(null, log, targetWebappDirectory);
		} catch (ClassNotFoundException ex) {
			// Application does not include the React view layer; no view overlays to merge.
		} catch (InvocationTargetException ex) {
			log.error("Failed to merge view overlays.", ex.getCause());
		} catch (ReflectiveOperationException ex) {
			log.error("Failed to merge view overlays.", ex);
		}
	}

	private static void copy(File targetWebappDirectory, Path appOverlay, String name) {
		FileUtilities.copyR(appOverlay.resolve(name).toFile(), new File(targetWebappDirectory, name));
	}

	private static void createLayoutsForThemes(Protocol log, File target, CompileTimeApplication application,
			Map<String, List<BinaryData>> overlaysByLayoutKey) {
		ThreadContext.inSystemContext(LayoutCreator.class, () -> {
			for (Theme theme : ThemeFactory.getInstance().getChoosableThemes()) {
				createLayoutsForTheme(log, theme, target, application, overlaysByLayoutKey);
			}
		});
	}

	/**
	 * Writes the expanded layouts of the given {@link Theme} into the
	 * {@link ModuleLayoutConstants#LAYOUT_PATH layout directory} of the given target web
	 * application directory.
	 * 
	 * <p>
	 * A layout that cannot be created is not written but reported to the given {@link Protocol},
	 * naming the layout, the theme and the cause. The remaining layouts are written anyway, so that
	 * one run reports every defective layout.
	 * </p>
	 * 
	 * @param log
	 *        The {@link Protocol} to report layouts to that cannot be created.
	 * @param theme
	 *        The {@link Theme} to create the layouts for.
	 * @param target
	 *        The web application directory to write the layouts to.
	 * @param application
	 *        The application whose layouts are created.
	 * @param overlaysByLayoutKey
	 *        The overlays of each layout to create, indexed by the layout key, see
	 *        {@link LayoutStorage#readLayouts(Protocol)}. A layout without overlays is copied unchanged.
	 */
	public static void createLayoutsForTheme(Protocol log, Theme theme, File target,
			CompileTimeApplication application, Map<String, List<BinaryData>> overlaysByLayoutKey) {
		ThemeFactory.getInstance().withTheme(theme, new Computation<Void>() {

			@Override
			public Void run() {
				for (String layout : overlaysByLayoutKey.keySet()) {
					mergeLayout(log, target, application, theme, layout, overlaysByLayoutKey);
				}

				return null;
			}
		});
	}

	private static void startLayoutDependantModules(Protocol log) {
		try {
			ModuleUtil.INSTANCE.startUp(ThreadContextManager.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(ThemeFactory.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(JSFileCompiler.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(CommandGroupRegistry.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(DynamicComponentService.Module.INSTANCE);
		} catch (IllegalArgumentException | ModuleException exception) {
			log.error("Unable to load layout dependant modules.", exception);
		}
	}

	private static void startXMLProperties(Protocol log) {
		try {
			XMLProperties.startWithMetaConf(ModuleLayoutConstants.META_CONF_RESOURCE);
		} catch (ModuleException exception) {
			log.error("Unable to load application configuration.", exception);
		}
	}

	private static void mergeLayout(Protocol log, File targetBase, CompileTimeApplication application, Theme theme,
			String layoutKey, Map<String, List<BinaryData>> overlays) {
		try {
			Path target = targetBase.toPath().resolve(ModuleLayoutConstants.LAYOUT_PATH + "/" + layoutKey);
			Files.createDirectories(target.getParent());
			List<BinaryData> layoutOverlays = overlays.getOrDefault(layoutKey, Collections.emptyList());
			if (layoutOverlays.isEmpty()) {
				writeLayout(layoutKey, target);
			} else {
				TLLayout layout = createLayout(log, application, theme, layoutKey, layoutOverlays);

				if (layout != null) {
					try (OutputStream output = new FileOutputStream(target.toFile())) {
						layout.writeTo(output, true);
					}
				}
			}
		} catch (IOException | RuntimeException exception) {
			log.error(layoutFailure(theme, layoutKey), exception);
		}
	}

	/**
	 * Creates the given layout from its overlays.
	 * 
	 * @return The created layout, or <code>null</code>, if the layout cannot be created. In that
	 *         case, the problems have been reported to the given {@link Protocol}.
	 */
	private static TLLayout createLayout(Protocol log, CompileTimeApplication application, Theme theme,
			String layoutKey, List<BinaryData> overlays) {
		BufferingProtocol layoutLog = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(layoutLog);
		CreateComponentParameter params = CreateComponentParameter.newParameter(layoutKey);
		params.setLayoutResolver(application.createLayoutResolver(layoutLog, theme));
		TLLayout layout;
		try {
			layout = LayoutStorage.createLayout(context, params, overlays);
		} catch (Exception exception) {
			reportErrors(log, theme, layoutKey, layoutLog);

			// An abort of the layout protocol repeats the errors reported above.
			if (!(exception instanceof AbortExecutionException) || !layoutLog.hasErrors()) {
				log.error(layoutFailure(theme, layoutKey), exception);
			}
			return null;
		}
		if (layoutLog.hasErrors()) {
			reportErrors(log, theme, layoutKey, layoutLog);
			return null;
		}
		return layout;
	}

	private static void reportErrors(Protocol log, Theme theme, String layoutKey, BufferingProtocol layoutLog) {
		for (String error : layoutLog.getErrors()) {
			log.error(layoutFailure(theme, layoutKey) + ": " + error);
		}
	}

	private static String layoutFailure(Theme theme, String layoutKey) {
		return "Creating layout '" + layoutKey + "' for theme '" + theme.getThemeID() + "' failed";
	}

	private static void writeLayout(String layoutKey, Path target) throws IOException {
		BinaryData layoutData =
			FileManager.getInstance().getData(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + layoutKey);
		Files.copy(layoutData.getStream(), target, StandardCopyOption.REPLACE_EXISTING);
	}
}
