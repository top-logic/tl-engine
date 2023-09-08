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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	 * Creates the application layouts, CSS and JavaScript.
	 * 
	 * @param resourcePaths
	 *        Root paths to collect all layouts from.
	 * @param targetWebappDirectory
	 *        Web application directory to write the generation results to.
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
		CompileTimeApplication application = CompileTimeApplication.createApplicationModule(targetWebappDirectory);

		startXMLProperties();

		try (ModuleContext ctx = ModuleUtil.beginContext()) {
			startLayoutDependantModules();

			File targetThemeDirectory = new File(targetWebappDirectory, MultiThemeFactory.THEMES_FOLDER_PATH);
			MergeThemeConfigs.writeMergedThemesTo(targetThemeDirectory);

			Map<String, List<BinaryData>> overlaysByLayoutKey = LayoutStorage.readLayouts();

			createLayoutsForThemes(targetWebappDirectory, application, overlaysByLayoutKey);
		}
		
		// Copy generated scripts and styles.
		copy(targetWebappDirectory, appOverlay, "script");
		copy(targetWebappDirectory, appOverlay, "style");

		FileManager.setInstance(oldFileManager);
	}

	private static void copy(File targetWebappDirectory, Path appOverlay, String name) {
		FileUtilities.copyR(appOverlay.resolve(name).toFile(), new File(targetWebappDirectory, name));
	}

	private static void createLayoutsForThemes(File target, CompileTimeApplication application,
			Map<String, List<BinaryData>> overlaysByLayoutKey) {
		ThreadContextManager.inSystemInteraction(LayoutCreator.class, () -> {
			for (Theme theme : ThemeFactory.getInstance().getChoosableThemes()) {
				createLayoutsForTheme(theme, target, application, overlaysByLayoutKey);
			}
		});
	}

	private static void createLayoutsForTheme(Theme theme, File target, CompileTimeApplication application,
			Map<String, List<BinaryData>> overlaysByLayoutKey) {
		ThemeFactory.getInstance().withTheme(theme, new Computation<Void>() {

			@Override
			public Void run() {
				for (String layout : overlaysByLayoutKey.keySet()) {
					try {
						mergeLayout(target, application, theme, layout, overlaysByLayoutKey);
					} catch (IOException exception) {
						Logger.error("Merging of layout " + layout + " failed.", exception);
					}
				}

				return null;
			}
		});
	}

	private static void startLayoutDependantModules() {
		try {
			ModuleUtil.INSTANCE.startUp(ThreadContextManager.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(ThemeFactory.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(JSFileCompiler.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(CommandGroupRegistry.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(DynamicComponentService.Module.INSTANCE);
		} catch (IllegalArgumentException | ModuleException exception) {
			Logger.error("Unable to load layout dependant modules.", exception);
		}
	}

	private static void startXMLProperties() {
		try {
			XMLProperties.startWithMetaConf(ModuleLayoutConstants.META_CONF_RESOURCE);
		} catch (ModuleException exception) {
			Logger.error("Unable to load application configuration.", exception);
		}
	}

	static void mergeLayout(File targetBase, CompileTimeApplication application, Theme theme, String layoutKey,
			Map<String, List<BinaryData>> overlays) throws IOException {
		Path target = targetBase.toPath().resolve(ModuleLayoutConstants.LAYOUT_PATH + "/" + layoutKey);
		Files.createDirectories(target.getParent());
		List<BinaryData> layoutOverlays = overlays.getOrDefault(layoutKey, Collections.emptyList());
		if (layoutOverlays.isEmpty()) {
			writeLayout(layoutKey, target);
		} else {
			TLLayout layout = createLayout(application, theme, layoutKey, layoutOverlays);

			if (layout != null) {
				try (OutputStream output = new FileOutputStream(target.toFile())) {
					layout.writeTo(output, true);
				}
			}
		}
	}

	private static TLLayout createLayout(CompileTimeApplication application, Theme theme, String layoutKey,
			List<BinaryData> overlays) {
		Protocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		CreateComponentParameter params = CreateComponentParameter.newParameter(layoutKey);
		params.setLayoutResolver(application.createLayoutResolver(log, theme));
		TLLayout layout;
		try {
			layout = LayoutStorage.createLayout(context, params, overlays);
			log.checkErrors();
		} catch (Exception exception) {
			Logger.error("Creating layout failed: " + params.getLayoutName(), exception);

			return null;
		}
		return layout;
	}

	private static void writeLayout(String layoutKey, Path target) throws IOException {
		BinaryData layoutData =
			FileManager.getInstance().getData(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + layoutKey);
		Files.copy(layoutData.getStream(), target, StandardCopyOption.REPLACE_EXISTING);
	}
}
