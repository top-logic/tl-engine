/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.generate.JavaGenerator;
import com.top_logic.basic.io.DirectoriesOnlyFilter;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.processor.Application;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.layout.processor.ComponentNameCheck;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutModelUtils;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.Operation;
import com.top_logic.layout.processor.ReferenceHandler;
import com.top_logic.layout.processor.ResolveFailure;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Utility to do various processings of application component configurations
 * (layout XML files).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutModelProcessor extends Operation {

	/**
	 * {@link Filter} that filters out objects from a given collection.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class NotInCollectionFilter implements Filter<Object> {
		private final Collection<?> objects;

		/**
		 * Creates a {@link NotInCollectionFilter}.
		 *
		 * @param objects All objects that are not accepted.
		 */
		public NotInCollectionFilter(Collection<?> objects) {
			this.objects = objects;
		}

		@Override
		public boolean accept(Object anObject) {
			return ! objects.contains(anObject);
		}
	}

	static final String DEFAULT_COMPONENT_PACKAGE_NAME = "com.top_logic.mig.html.layout";

	private static final String BINDING_COMMAND = "binding";

	/**
	 * No {@link #DEPRECATION_ARGUMENT}.
	 */
	private static final String NO_DEPRECATION_ARGUMENT = "-no-deprecation";

	/**
	 * Make {@link #deprecation} <code>true</code>.
	 */
	private static final String DEPRECATION_ARGUMENT = "-deprecation";

	private static final String INLINE_COMMAND = "inline";
	private static final String CHECK_COMMAND = "check";

	private static final String HELP_ARGUMENT = "-help";
	private static final String IN_ARGUMENT = "-in";

	private static final String WORKSPACE_ARGUMENT = "-workspace";
	private static final String APP_ARGUMENT = "-app";

	/**
	 * Argument passing the root of the application module to process.
	 * 
	 * <p>
	 * A value of {@link ModuleLayoutConstants#WEBAPP_DIR} is appended to find the actual web
	 * application root.
	 * </p>
	 * 
	 * @see #WEBAPP_ARGUMENT
	 */
	public static final String MODULE_ARGUMENT = "-mod";

	/**
	 * Argument passing the web application root folder to process.
	 */
	public static final String WEBAPP_ARGUMENT = "-webapp";
	private static final String FORCE_ARGUMENT = "-force";

	private static final String OUT_DIR_ARGUMENT = "-out";
	
	/**
	 * Activate {@link #pretty}.
	 */
	private static final String PRETTY_ARGUMENT = "-pretty";
	
	/**
	 * Not {@link #PRETTY_ARGUMENT}
	 */
	private static final String NO_PRETTY_ARGUMENT = "-no-pretty";

	/**
	 * Activate {@link #_strip}.
	 */
	private static final String STRIP_ARGUMENT = "-strip";
	
	/**
	 * Not {@link #STRIP_ARGUMENT}.
	 */
	private static final String NO_STRIP_ARGUMENT = "-no-strip";

	private boolean deprecation = false;

	/**
	 * Whether output should be pretty printed.
	 */
	private boolean pretty = true;

	private boolean force;

	private boolean _strip;

	private String _inputLayout;

	private File _outputDir;

	private Path _workspace;

	/**
	 * Creates a {@link LayoutModelProcessor}.
	 */
	public LayoutModelProcessor() {
		super(new SyserrProtocol(), null);
    }

	public void run(String[] args) throws IOException, XPathExpressionException {
		// Prevent trash on the console.
		Logger.configureStdout();
		
		// List of File
		List<File> applicationRoots = new ArrayList<>();
		
		_inputLayout = null;
		for (int n = 0, cnt = args.length; n < cnt; ) {
			String arg = args[n++];
			assert (arg != null) : "'arg' must not be null.";
			if (StringServices.startsWithChar(arg, '-')) {
				if (APP_ARGUMENT.equals(arg)) {
					applicationRoots.add(new File(args[n++]).getCanonicalFile());
				}
				
				else if (WORKSPACE_ARGUMENT.equals(arg)) {
					_workspace = Paths.get(args[n++]);
				}

				else if (MODULE_ARGUMENT.equals(arg)) {
					File moduleDir = new File(args[n++]);
					File webappDir = new File(moduleDir, ModuleLayoutConstants.WEBAPP_DIR);
					addWebapp(applicationRoots, webappDir);
				}
				
				else if (WEBAPP_ARGUMENT.equals(arg)) {
					File webappDir = new File(args[n++]);
					addWebapp(applicationRoots, webappDir);
				}

				else if (OUT_DIR_ARGUMENT.endsWith(arg)) {
					File outDir = new File(args[n++]);
					if (!outDir.exists()) {
						error("Output directory '" + outDir.toString() + "' does not exist.");
						continue;
					}
					if (!outDir.isDirectory()) {
						error("Output directory '" + outDir.toString() + "' is not a directory.");
						continue;
					}
					_outputDir = outDir;
				}

				else if (IN_ARGUMENT.equals(arg)) {
					_inputLayout = args[n++];
				}
				
				else if (DEPRECATION_ARGUMENT.equals(arg)) {
					deprecation = true;
				}
				
				else if (NO_DEPRECATION_ARGUMENT.equals(arg)) {
					deprecation = false;
				}
				
				else if (PRETTY_ARGUMENT.equals(arg)) {
					pretty = true;
				}
				
				else if (NO_PRETTY_ARGUMENT.equals(arg)) {
					pretty = false;
				}
				
				else if (STRIP_ARGUMENT.equals(arg)) {
					_strip = true;
				}
				
				else if (NO_STRIP_ARGUMENT.equals(arg)) {
					_strip = false;
				}
				
				else if (FORCE_ARGUMENT.equals(arg)) {
					force = false;
				}
				
				else if (HELP_ARGUMENT.equals(arg)) {
					help();
				}
				
				else {
					error("Unknown option '" + arg + "'.");
				}
			} else {
				if (INLINE_COMMAND.equals(arg)) {
					if (_inputLayout != null) {
						Path layoutPath = Paths.get(_inputLayout);
						String moduleName = layoutPath.getName(0).toString();

						File workspaceDir = _workspace.toFile();
						System.setProperty(Workspace.WORKSPACE_PROPERTY, workspaceDir.getCanonicalPath());
						Workspace workspace = Workspace.createWorkspace(workspaceDir);
						File applicationRoot =
							new File(workspace.resolveProjectDir(moduleName), ModuleLayoutConstants.WEBAPP_DIR);

						CompileTimeApplication application =
							CompileTimeApplication.createCompileTimeApplication(workspaceDir, applicationRoot);
						try {
							inlineSingleFile(application, layoutPath.subpath(6, layoutPath.getNameCount()));
						} catch (ResolveFailure ex) {
							error("Inlining application layout failed for '" + application.getName() + "'.", ex);
						} finally {
							shutDownApplication();
						}
					} else {
						ensureApplicationRoot(applicationRoots);
						for (File applicationRoot : applicationRoots) {
							CompileTimeApplication application =
								CompileTimeApplication.createApplicationModule(applicationRoot);
							try {
								inlineApplicationLayout(application);
							} catch (ResolveFailure ex) {
								error("Inlining application layout failed for '" + application.getName() + "'.", ex);
							} finally {
								shutDownApplication();
							}
						}
					}
				}

				else if (CHECK_COMMAND.equals(arg)) {
					checkUsedLayouts(applicationRoots);
					
					if (hasErrors() && (!force)) {
						// Report failure.
						System.exit(1);
					}
				}
				
				else if (BINDING_COMMAND.equals(arg)) {
					ensureApplicationRoot(applicationRoots);
					
					for (Iterator<File> it = applicationRoots.iterator(); it.hasNext(); ) {
						File applicationRoot = it.next();

						LayoutResolver resolver;
						CompileTimeApplication application =
							CompileTimeApplication.createApplicationModule(applicationRoot);
						try {
							resolver = setupApplication(application);
						} catch (ModuleException ex) {
							error("Application '" + application.getName() + "' could not be set up.", ex);
							continue;
						}
						try {
							resolver.setAddAnnotations(true);
							getRootLayouts().stream().forEach(rootLayout -> doBinding(application, resolver, rootLayout));
						} finally {
							shutDownApplication();
						}
					}
				} else {
					error("Command '" + arg + "' not supported.");
				}			
			}
		}
	}

	private void addWebapp(List<File> applicationRoots, File webappDir) throws IOException {
		if (!webappDir.exists()) {
			error("Webapp directory '" + webappDir.toString() + "' does not exist.");
			return;
		}
		if (!webappDir.isDirectory()) {
			error("Webapp directory '" + webappDir.toString() + "' is not a directory.");
			return;
		}
		applicationRoots.add(webappDir.getCanonicalFile());
	}

	private void doBinding(Application application, LayoutResolver resolver, String rootLayout) {
		ConstantLayout rootLayoutDefinition;
		try {
			rootLayoutDefinition = resolver.getGlobalLayout(rootLayout);
			new LayoutInline(resolver).inline(rootLayoutDefinition);
		} catch (ResolveFailure ex) {
			error("Resolving root layout in '" + application.getName() + "' failed.", ex);
			return;
		}

		File srcDir = application.getTestSourceDir();

		String applicationPackage = "test." + application.getModuleDir().getName() + ".layout";
		File packageDir = new File(srcDir, applicationPackage.replace('.', '/'));
		packageDir.mkdirs();

		String bindingClassName = CodeUtil.toCamelCase(application.getName()) + "Components";
		File sourceFile = new File(packageDir, bindingClassName + ".java");
		checkErrors();

		info("Generating component binding file '" + sourceFile.getAbsolutePath() + "'.");
		try {
			createComponentBinding(
				sourceFile, rootLayoutDefinition.getLayoutDocument(), applicationPackage,
				bindingClassName);
		} catch (IOException | XPathExpressionException ex) {
			error("Error creating component binding in '" + application.getName() + "'.", ex);
		}
	}

	private void inlineApplicationLayout(CompileTimeApplication application) throws IOException, ResolveFailure {
		LayoutResolver resolver;
		try {
			resolver = setupApplication(application);
		} catch (ModuleException ex) {
			error("Application '" + application.getName() + "' could not be set up.", ex);
			return;
		}
		try {
			resolver.setAddAnnotations(true);

			for (String layout : getRootLayouts()) {
				doInline(application, resolver, layout);
			}

			checkErrors();
		} finally {
			shutDownApplication();
		}
	}

	private void inlineSingleFile(CompileTimeApplication application, Path layoutResourcePath)
			throws IOException, ResolveFailure {
		LayoutResolver resolver;
		try {
			resolver = setupApplication(application);
		} catch (ModuleException ex) {
			error("Application '" + application.getName() + "' could not be set up.", ex);
			return;
		}
		try {
			resolver.setAddAnnotations(false);
			resolver.setSourceFile(false);

			String path = FileUtilities.replaceSeparator(layoutResourcePath.toString(), '/');
			ConstantLayout layoutDefinition = resolver.getGlobalLayout(path);
			LayoutInline inliner = new LayoutInline(resolver);
			inliner.setInlinedAnnotation(false);
			inliner.inline(layoutDefinition);
			LayoutModelUtils.stripComments(layoutDefinition.getLayoutDocument());
			String resourcePath =
				ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + LayoutUtils.normalizeLayoutName(path);
			File resourceFile = new File(application.getWebappDir(), resourcePath);
			DOMUtil.serializeXMLDocument(resourceFile, pretty, layoutDefinition.getLayoutDocument());
			System.out.println("Generated inlined layout definition: " + resourcePath);

			checkErrors();
		} finally {
			shutDownApplication();
		}
	}

	private void doInline(Application application, LayoutResolver resolver, String rootLayout) throws IOException, ResolveFailure {
		File layoutRoot;
		if (_outputDir == null) {
			layoutRoot = application.getLayoutDir();
		} else {
			layoutRoot = CompileTimeApplication.createApplicationModule(_outputDir).getLayoutDir();
		}

		Collection<Theme> allThemes = ThemeFactory.getInstance().getChoosableThemes();
		for (Theme theme : allThemes) {
			File inlinedRootLayout =
				new File(layoutRoot, Theme.LAYOUT_PREFIX + '/' + theme.getThemeID() + '/' + rootLayout);
			if (inlinedRootLayout.exists()) {
				// Clean old inlined contents to ensure that the old content is not found during
				// inlining.
				inlinedRootLayout.delete();
			}
		}

		List<Theme> orderedThemes = sortThemesTopologically(allThemes);
		for (Theme theme : orderedThemes) {
			resolver.setTheme(theme);

			File inlinedRootLayout =
				new File(layoutRoot, Theme.LAYOUT_PREFIX + '/' + theme.getThemeID() + '/' + rootLayout);

			inlinedRootLayout.getParentFile().mkdirs();

			long elapsed = -System.currentTimeMillis();

			/* Use global layout, because we just create the themed one. */
			ConstantLayout rootLayoutDefinition = resolver.getGlobalLayout(rootLayout);
			new LayoutInline(resolver).inline(rootLayoutDefinition);
			LayoutModelUtils.stripComments(rootLayoutDefinition.getLayoutDocument());

			elapsed += System.currentTimeMillis();

			writeLayout(inlinedRootLayout, rootLayoutDefinition.getLayoutDocument());
			System.out.println("Generated inlined layout definition for theme '" + theme.getThemeID() + "' ("
				+ elapsed + "ms): " + inlinedRootLayout.getAbsolutePath());
		}
	}

	/**
	 * Sorts the given {@link Theme}s such that a base theme is not reported before all dependent
	 * theme are reported.
	 */
	private List<Theme> sortThemesTopologically(Collection<Theme> allThemes) {
		// sort themes topological where the parent theme is dependency
		List<Theme> sorted = CollectionUtil.topsort(new Mapping<Theme, Iterable<? extends Theme>>() {

			@Override
			public Iterable<? extends Theme> map(Theme input) {
				List<Theme> parentThemes = input.getParentThemes();
				if (parentThemes == null) {
					return Collections.emptyList();
				}
				return parentThemes;
			}
		}, allThemes, false);
		
		// sorted has first the base themes and then the dependent
		Collections.reverse(sorted);

		return sorted;
	}

	protected Set<String> allAttributeValues(Document layout) {
		HashSet<String> attributeValues = new HashSet<>();
		
		descendAllAttributeValues(attributeValues, layout.getDocumentElement());
		
		return attributeValues;
	}

	private void descendAllAttributeValues(Collection<String> attributeValues, Element element) {
		NamedNodeMap attributes = element.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			attributeValues.add(attributes.item(n).getNodeValue());
		}
		
		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				descendAllAttributeValues(attributeValues, (Element) child);
			}
		}
	}

	private void checkUsedLayouts(List<File> applicationRoots) {
		ensureApplicationRoot(applicationRoots);
		Provider<ReferenceHandler> handlerProvider = new Provider<>() {
			@Override
			public ReferenceHandler get() {
				return new ComponentNameCheck(getProtocol());
			}
		};
		traverseAllUsedLayouts(handlerProvider, applicationRoots);
	}
		
	private void traverseAllUsedLayouts(Provider<ReferenceHandler> handlerProvider, List<File> applicationRoots) {
		for (Iterator<File> it = applicationRoots.iterator(); it.hasNext(); ) {
			File applicationRoot = it.next();

			try {
				info("Process application " + applicationRoot);
				CompileTimeApplication application = CompileTimeApplication.createApplicationModule(applicationRoot);
				inspectApplication(application, handlerProvider);
			} catch (IOException ex) {
				error("Application '" + applicationRoot + "' could not be set up.", ex);
			}
		}
	}

	private void inspectApplication(CompileTimeApplication application, Provider<ReferenceHandler> handlerProvider) {
		LayoutResolver resolver;
		try {
			resolver = setupApplication(application);
		} catch (ModuleException ex) {
			error("Application '" + application.getName() + "' could not be set up.", ex);
			return;
		}
		try {
			for (Theme theme : ThemeFactory.getInstance().getChoosableThemes()) {
				resolver.setTheme(theme);
				inspectCurrentApplication(handlerProvider, resolver);
				if (getProtocol().hasErrors()) {
					// With high probability, other themes suffer from common errors. Break to
					// prevent duplicate error reports.
					break;
				}
			}
		} finally {
			shutDownApplication();
		}
	}

	private void inspectCurrentApplication(Provider<ReferenceHandler> handlerProvider, LayoutResolver resolver) {
		getRootLayouts().stream().forEach(rootLayout -> {
			try {
				ConstantLayout layout = resolver.getGlobalLayout(rootLayout);
				new LayoutInline(resolver).inline(layout);
				handlerProvider.get().handle(layout);
			} catch (ResolveFailure ex) {
				error("Layout from '" + getApplication().getWebappDir() + "' cound not be loaded.", ex);
			}
		});
	}

	private void addLayoutFiles(final FileFilter fileFilter, final Filter<? super File> dirFilter, Collection<? super File> allLayoutFiles, File dir) throws IOException {
		
		File[] layoutFiles = dir.listFiles(fileFilter);
		
		if (layoutFiles != null) {
			for (File file : layoutFiles) {
				String name = file.getName();
				if (!name.endsWith(LayoutModelConstants.XML_SUFFIX)) {
					continue;
				}
				
				allLayoutFiles.add(file.getCanonicalFile());
			}
		}

		File[] subDirs = dir.listFiles(DirectoriesOnlyFilter.INSTANCE);
		if (subDirs != null) {
			for (File subDir : subDirs) {
				if (dirFilter.accept(subDir)) {
					addLayoutFiles(fileFilter, dirFilter, allLayoutFiles, subDir);
				}
			}
		}
	}

	private void createComponentBinding(File out, final Document applicationLayout, final String applicationPackage, final String bindingClassName) throws XPathExpressionException, IOException {
		final NodeList components = DOMUtil.selectNodeList(applicationLayout, "//component");
		
		final String outputPath = out.getCanonicalPath();
		
		JavaGenerator generator = new LayoutBindingGenerator(applicationPackage, getProtocol(), applicationLayout,
				components, bindingClassName, outputPath);
		
		generator.generate(out);
	}

	private LayoutResolver setupApplication(CompileTimeApplication application) throws ModuleException {
		this.setApplication(application);
		installFileManager();

		XMLProperties.startWithMetaConf(application.getMetaConfResource());

		ModuleUtil.INSTANCE.startModule(ThemeFactory.class);
		ModuleUtil.INSTANCE.startModule(CommandGroupRegistry.class);
		ModuleUtil.INSTANCE.startModule(DynamicComponentService.class);

		Theme theme = ThemeFactory.getInstance().getDefaultTheme();

		LayoutResolver resolver = getApplication().createLayoutResolver(getProtocol(), theme);
//		resolver.setExpandThemeVariables(false);
		return resolver;
	}

	/**
	 * Installs the {@link FileManager} for the {@link LayoutModelProcessor} run.
	 */
	protected void installFileManager() {
		FileManager.setInstance(MultiFileManager.createForModularApplication());
	}

	private void shutDownApplication() {
		ModuleUtil.INSTANCE.shutDownAll();
	}

	public static List<String> getRootLayouts() {
		return LayoutConfig.getAvailableLayouts();
	}

	private static void help() {
		System.err.println(LayoutModelProcessor.class.getSimpleName());
		System.err.println("    " + APP_ARGUMENT + " <web application root>");
		System.err.println("    " + IN_ARGUMENT + " <input layout name relative to layouts/ directory>");
		System.err.println("    " + DEPRECATION_ARGUMENT + " /*Report deprecations*/");
		System.err.println("    " + NO_DEPRECATION_ARGUMENT + " /*Report no deprecations*/");
		System.err.println("    " + INLINE_COMMAND + " /*Inline references in input layout and write to output file*/");
	}

	private void ensureApplicationRoot(List<File> applicationRoots) {
		if (applicationRoots.isEmpty()) {
			error("Missing application root(s).");
		}
	}
	
	private void writeLayout(File outputFile, Document document) throws IOException {
		try (FileOutputStream out = new FileOutputStream(outputFile)) {
			DOMUtil.serializeXMLDocument(out, pretty, document);
		}
	}

	public static void dumpLayout(ConstantLayout layout) {
		System.out.println("=== " + layout.getLayoutName() + " ===");
		try {
			DOMUtil.serializeXMLDocument(System.out, true, layout.getLayoutDocument());
		} catch (IOException ex) {
			throw LayoutModelUtils.error("Cannot dump layout.", ex);
		}
	}

	/**
	 * Main method of {@link LayoutModelProcessor}.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) throws IOException, XPathExpressionException {
        new LayoutModelProcessor().run(args);
    }

}
