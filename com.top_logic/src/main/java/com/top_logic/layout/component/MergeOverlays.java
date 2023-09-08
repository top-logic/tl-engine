/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.Operation;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Tool that applies all {@link LayoutModelConstants#LAYOUT_XML_OVERLAY_FILE_SUFFIX overlays} to the
 * {@link LayoutModelConstants#LAYOUT_XML_FILE_SUFFIX}, and stores them to a given theme folder.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MergeOverlays extends Operation {

	private static String WORKSPACE = "-workspace";

	private static String WEBAPP = "-app";

	private static String OUTPUT_DIR = "-output";

	private File _outputLayoutsDir;

	private boolean _prettyPrint;

	/**
	 * Creates a new {@link MergeOverlays}.
	 * 
	 * @param protocol
	 *        See {@link #getProtocol()}.
	 * @param application
	 *        See {@link #getApplication()}.
	 * @param outputLayoutDir
	 *        See {@link #getOutputLayoutsDir()}.
	 */
	public MergeOverlays(Protocol protocol, CompileTimeApplication application, File outputLayoutDir) {
		super(protocol, application);
		setOutputLayoutsDir(outputLayoutDir);
	}

	/**
	 * Creates a new {@link MergeOverlays} without {@link #getApplication() application} and
	 * {@link #getOutputLayoutsDir() output directory}.
	 */
	public MergeOverlays(Protocol protocol) {
		this(protocol, null, null);
	}

	/**
	 * The output directory to generate files to.
	 */
	public File getOutputLayoutsDir() {
		return _outputLayoutsDir;
	}

	/**
	 * Setter for {@link #getOutputLayoutsDir()}.
	 */
	public void setOutputLayoutsDir(File outputLayoutsDir) {
		_outputLayoutsDir = outputLayoutsDir;
	}

	/**
	 * Whether the XML files must be pretty printed.
	 */
	public boolean isPrettyPrint() {
		return _prettyPrint;
	}

	/**
	 * Setter for {@link #isPrettyPrint()}.
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		_prettyPrint = prettyPrint;

	}

	void run(String[] args) {
		File ws = null;
		String webapp = null;
		File output = null;
		for (int i = 0; i < args.length; i++) {
			String param = args[i];
			i++;
			if (i >= args.length) {
				getProtocol().error("No value for parameter " + param + " given.");
				return;
			}
			String value = args[i];
			if (WEBAPP.equals(param)) {
				webapp = value;
			} else if (WORKSPACE.equals(param)) {
				ws = new File(value);
			} else if (OUTPUT_DIR.equals(param)) {
				output = new File(value);
			}
		}
		if (output == null) {
			getProtocol().error("Missing attribute value " + OUTPUT_DIR + ": No output layout directors set.");
		} else {
			setOutputLayoutsDir(output);
		}
		if (webapp == null) {
			getProtocol().error("Missing attribute value " + WEBAPP + ": No source web application set.");
		} else {
			File applicationRoot;
			if (ws == null) {
				applicationRoot = new File(webapp);
			} else {
				applicationRoot = new File(ws, webapp);
			}
			if (applicationRoot.isDirectory()) {
				try {
					setApplication(CompileTimeApplication.createApplicationModule(applicationRoot));
				} catch (IOException ex) {
					getProtocol().error("Unable to create application from " + applicationRoot.getAbsolutePath(), ex);
				}
			} else {
				getProtocol().error(
					"Illegal web application: " + applicationRoot.getAbsolutePath() + " is not an existing directory.");
			}
		}
		if (getProtocol().hasErrors()) {
			return;
		}
		run();
	}

	/**
	 * Runs this {@link MergeOverlays}.
	 */
	public void run() {
		if (getApplication() == null) {
			throw new IllegalStateException("No application set.");
		}
		if (getOutputLayoutsDir() == null) {
			throw new IllegalStateException("No output layout directory set.");
		}
		try {
			init();
		} catch (ModuleException ex) {
			getProtocol().error("Unable to load configuration.", ex);
			return;
		}

		Map<String, List<BinaryData>> overlaysByLayoutKey = LayoutStorage.readLayouts();

		try (ModuleContext ctx = ModuleUtil.beginContext()) {
			try {
				ModuleUtil.INSTANCE.startUp(ThreadContextManager.Module.INSTANCE);
				ModuleUtil.INSTANCE.startUp(ThemeFactory.Module.INSTANCE);
				ModuleUtil.INSTANCE.startUp(CommandGroupRegistry.Module.INSTANCE);
				ModuleUtil.INSTANCE.startUp(DynamicComponentService.Module.INSTANCE);
			} catch (IllegalArgumentException | ModuleException ex) {
				getProtocol().error("Unable to start ThemeFactory", ex);
				return;
			}
			ThreadContextManager.inSystemInteraction(MergeOverlays.class, () -> {
				for (Theme theme : ThemeFactory.getInstance().getChoosableThemes()) {
					ThemeFactory.getInstance().withTheme(theme, new Computation<Void>() {

						@Override
						public Void run() {
							for (String layout : overlaysByLayoutKey.keySet()) {
								mergeLayout(theme, layout, overlaysByLayoutKey);
							}
							return null;
						}
					});
				}
			});
		}
	}

	private void init() throws ModuleException {
		CompileTimeApplication application = (CompileTimeApplication) getApplication();
		FileManager fm =
			MultiFileManager.createForModularApplication();
		FileManager.setInstance(fm);

		XMLProperties.startWithMetaConf(application.getMetaConfResource());
	}

	private void mergeLayout(Theme theme, String layoutKey, Map<String, List<BinaryData>> overlays) {
		List<BinaryData> layoutOverlays = overlays.getOrDefault(layoutKey, Collections.emptyList());
		if (layoutOverlays.isEmpty()) {
			return;
		}
		Protocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		CreateComponentParameter params = CreateComponentParameter.newParameter(layoutKey);
		params.setLayoutResolver(getApplication().createLayoutResolver(log, theme));
		TLLayout layout;
		try {
			layout = LayoutStorage.createLayout(context, params, layoutOverlays);
			log.checkErrors();
		} catch (Exception ex) {
			getProtocol().error("Unable to process '" + layoutKey + "' in theme '" + theme.getName() + "'.", ex);
			return;
		}

		writeLayout(theme, layoutKey, layout);
	}

	private File outputFile(Theme theme, String layoutName) {
		File themesFolder = new File(getOutputLayoutsDir(), Theme.LAYOUT_PREFIX);
		File themeFolder = new File(themesFolder, theme.getThemeID());
		File layoutFile = new File(themeFolder, layoutName);
		layoutFile.getParentFile().mkdirs();
		return layoutFile;
	}

	private void writeLayout(Theme theme, String layoutKey, TLLayout layout) {
		File layoutFile = outputFile(theme, layoutKey);
		try (FileOutputStream fout = new FileOutputStream(layoutFile)) {
			layout.writeTo(fout, true);
		} catch (IOException exception) {
			getProtocol().error("Unable to write to file " + layoutFile.getAbsolutePath(), exception);
			return;
		}
		if (isPrettyPrint()) {
			try {
				XMLPrettyPrinter.normalizeFile(layoutFile);
			} catch (IOException | SAXException ex) {
				getProtocol().error("Unable to pretty print file " + layoutFile.getAbsolutePath(), ex);
			}
		}
	}

	/**
	 * Runs the {@link MergeOverlays} from command line.
	 */
	public static void main(String[] args) {
		SyserrProtocol protocol = new SyserrProtocol();
		new MergeOverlays(protocol).run(args);
		protocol.checkErrors();
	}

}
