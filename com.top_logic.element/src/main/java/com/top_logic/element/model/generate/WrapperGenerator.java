/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.model.util.TLModelNamingConvention.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.generate.FileGenerator;
import com.top_logic.basic.generate.JavaGenerator;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.tools.resources.ResourceFile;
import com.top_logic.util.model.CompatibilityService;

/**
 * Generator for Java bindings for {@link TLModule}s.
 * 
 * <p>
 * Note: The copyright of generated file header can be adjusted using the system property
 * {@value FileGenerator#COPYRIGHT_HOLDER_PROPERTY}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WrapperGenerator {

	/**
	 * Option to specify an additional application layer that is stacked on top on the default
	 * application.
	 * 
	 * <p>
	 * This can be used to generated wrappers for tests by adding the deploy aspect folder
	 * <code>src/test/webapp</code>.
	 * </p>
	 * 
	 * <p>
	 * This option can be given multiple times to add multiple deploy aspect folders.
	 * </p>
	 */
	public static final String DEPLOY_OPTION = "-deploy";

	/**
	 * Option to specify the directory where the resource files have to be stored. If not set, the
	 * default folder in the top level web application is used.
	 * 
	 * @see ModuleLayoutConstants#RESOURCES_PATH
	 */
	public static final String RESOURCES_OPTION = "-resources-path";

	/**
	 * Option to specify the module for which the binding should be generated.
	 */
	public static final String MODULE_OPTION = "-module";

	/**
	 * Option to specify multiple modules for which the binding should be generated.
	 */
	public static final String MODULES_OPTION = "-modules";

	/**
	 * Option to specify the output directory for the generated code.
	 */
	public static final String OUTPUT_DIR_OPTION = "-out";

	/**
	 * Option to specify that no classes from tl-element must be used.
	 */
	public static final String NO_ELEMENT_CLASSES_OPTION = "-no-element-classes";

	/**
	 * Option to specify the getter prefix for the method accessing the property values.
	 * 
	 * @see #SETTER_PREFIX_OPTION
	 */
	public static final String GETTER_PREFIX_OPTION = "-getter-prefix";

	/**
	 * Option to specify that no getter for properties must be generated.
	 * 
	 * @see #NO_PROPERTY_SETTER_OPTION
	 */
	public static final String NO_PROPERTY_GETTER_OPTION = "-no-property-getter";

	/**
	 * Option to specify the setter prefix for the method setting the property values.
	 * 
	 * @see #GETTER_PREFIX_OPTION
	 */
	public static final String SETTER_PREFIX_OPTION = "-setter-prefix";

	/**
	 * Option to specify that no setter for properties must be generated.
	 * 
	 * @see #NO_PROPERTY_GETTER_OPTION
	 */
	public static final String NO_PROPERTY_SETTER_OPTION = "-no-property-setter";

	/**
	 * Option to request describing the command usage.
	 */
	public static final String HELP_OPTION = "-help";

	/**
	 * Option to prevent wrapper generation.
	 */
	public static final String NO_WRAPPERS_OPTION = "-nowrap";

	/**
	 * Option to prevent interface generation.
	 */
	public static final String NO_INTERFACE_OPTION = "-nointf";

	/**
	 * Option to prevent template interface generation.
	 */
	public static final String NO_INTERFACE_TEMPLATE_OPTION = "-nointfacetemplate";

	/**
	 * Option to prevent template interface generation.
	 */
	public static final String NO_CONSTANTS_OPTION = "-noconst";

	/**
	 * Option to prevent factory generation.
	 */
	public static final String NO_FACTORY_OPTION = "-nofac";

	/**
	 * Option to give the languages for which resource files should be generated.
	 */
	public static final String LANG_OPTION = "-languages";

	/**
	 * Option to force updating templates.
	 */
	public static final String UPDATE_OPTION = "-update";

	private File _outDir;

	private List<String> _languages = null;

	private boolean _noInterfaceTemplate;

	private boolean _noConst;

	private boolean _noIntf;

	private boolean _noWrap;

	private boolean _noFactory;

	final Protocol _log = new SyserrProtocol();

	private String[] _moduleNames;

	private final List<File> _deployFolders = new ArrayList<>();

	private boolean _update = false;

	private boolean _noElementClasses = false;

	private String _getterPrefix;

	private String _setterPrefix;

	private boolean _noPropertySetter;

	private boolean _noPropertyGetter;

	private File _resourcesPath;

	private void setup(String[] args) {
		for (int n = 0; n < args.length;) {
			String option = args[n++];

			if (MODULE_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing module argument.");
					continue;
				}
				_moduleNames = new String[] { args[n++] };
			}

			else if (MODULES_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing modules argument.");
					continue;
				}
				_moduleNames = args[n++].trim().split("\\s*,\\s*");
			}

			else if (DEPLOY_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing deploy folder argument.");
				}
				_deployFolders.add(new File(args[n++]));
			}

			else if (OUTPUT_DIR_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing dir argument.");
				}
				_outDir = new File(args[n++]);
			}

			else if (NO_ELEMENT_CLASSES_OPTION.equals(option)) {
				_noElementClasses = true;
			}

			else if (LANG_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing languages argument.");
					continue;
				}
				String langSpec = args[n++];
				_languages =
					langSpec.isEmpty() ? Collections.emptyList() : Arrays.asList(StringServices.split(langSpec, ','));
			}

			else if (RESOURCES_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing resources path argument.");
					continue;
				}
				_resourcesPath = new File(args[n++]);
			}

			else if (NO_WRAPPERS_OPTION.equals(option)) {
				_noWrap = true;
			}

			else if (NO_CONSTANTS_OPTION.equals(option)) {
				_noConst = true;
			}

			else if (NO_INTERFACE_OPTION.equals(option)) {
				_noIntf = true;
			}

			else if (NO_INTERFACE_TEMPLATE_OPTION.equals(option)) {
				_noInterfaceTemplate = true;
			}

			else if (NO_FACTORY_OPTION.equals(option)) {
				_noFactory = true;
			}

			else if (UPDATE_OPTION.equals(option)) {
				_update = true;
			}
			else if (NO_PROPERTY_SETTER_OPTION.equals(option)) {
				_noPropertySetter = true;
			}

			else if (NO_PROPERTY_GETTER_OPTION.equals(option)) {
				_noPropertyGetter = true;
			}

			else if (GETTER_PREFIX_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing getter prefix argument.");
				}
				_getterPrefix = args[n++];
			}

			else if (SETTER_PREFIX_OPTION.equals(option)) {
				if (n == args.length) {
					_log.error("Missing setter prefix argument.");
				}
				_setterPrefix = args[n++];
			}

			else if (HELP_OPTION.equals(option)) {
				help();
				return;
			}
		}

		_log.checkErrors();

		if (_moduleNames == null || _moduleNames.length == 0) {
			_log.error("No module name given.");
		}

		if (_outDir == null) {
			_log.error("Missing output dir.");
		} else {
			if (!_outDir.exists()) {
				_log.error("Output directory '" + _outDir.getAbsolutePath() + "' does not exist.");
			}

			if (!_outDir.isDirectory()) {
				_log.error("Output directory '" + _outDir.getAbsolutePath() + "' is not a directory.");
			}
		}

		if (_noConst) {
			// base interfaces extends constants.
			_noIntf = true;
		}

		if (_noIntf) {
			// base implementation classes implements base interfaces
			_noFactory = true;
			// templates inherits extends base interfaces
			_noInterfaceTemplate = true;
		}

		if (_noInterfaceTemplate) {
			// template classes implements template interfaces
			_noWrap = true;
		}

		_log.checkErrors();
	}

	/**
	 * Executes generation of wrappers with given arguments.
	 * 
	 * @param args
	 *        The arguments to parameterise the {@link WrapperGenerator}.
	 */
	public void run(String[] args) throws IOException, ModuleException {
		try {
			setup(args);
		} catch (ConfigurationError ex) {
			System.err.println(ex.getMessage());
			help(System.err);
			System.exit(1);
			return;
		}
		initXMLProperties();
		try (ModuleContext context = ModuleUtil.beginContext()) {
			startModules();
			TLModel model = createModel();
			for (String moduleName : _moduleNames) {
				TLModule module = model.getModule(moduleName);
				if (module == null) {
					_log.error("No module with name '" + moduleName + "' defined.");
					continue;
				}

				checkConsistency(module);
				generate(module);
			}
		}
		_log.checkErrors();
	}

	/**
	 * Initializes the configuration and {@link FileManager}.
	 */
	protected void initXMLProperties() throws ModuleException {
		// The file manager is needed for the configuration to resolve files
		List<Path> appDirs = Workspace.applicationModules();
		int deployCnt = _deployFolders.size();
		if (deployCnt > 0) {
			List<Path> appDirsPlus = new ArrayList<>(appDirs.size() + deployCnt);
			for (int n = 0; n < deployCnt; n++) {
				appDirsPlus.add(_deployFolders.get(n).toPath());
			}
			appDirsPlus.addAll(appDirs);
			appDirs = appDirsPlus;
		}
		FileManager multiLoaderFileManager = MultiFileManager.createMultiFileManager(appDirs);
		FileManager.setInstance(multiLoaderFileManager);

		XMLProperties.startWithMetaConf(ModuleLayoutConstants.META_CONF_RESOURCE);
	}

	private void startModules() throws ModuleException {
		ModuleUtil.INSTANCE.startUp(SafeHTML.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(AttributeSettings.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(AttributeValueLocatorFactory.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(Settings.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(ThreadContextManager.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(DataAccessService.Module.INSTANCE);
		ModuleUtil.INSTANCE.startUp(CompatibilityService.Module.INSTANCE);
	}

	private void generate(TLModule module) throws IOException {
		for (TLClass type : module.getClasses()) {
			generateConstants(type);
			generateInterface(type);
			generateInterfaceTemplate(type);
			removeLegacyImplementation(type);
			generateImplementation(type);
		}

		generateFactory(module);
		generateMessages(module);
	}

	private void generateMessages(TLModule module) {
		for (String lang : computeLanguages()) {
			_log.info("Generate messages file for language '" + lang + "'.");
			try {
				generateMessages(module, lang);
			} catch (IOException ex) {
				StringBuilder msg = new StringBuilder();
				msg.append("Unable to generate messages file for language '");
				msg.append(lang);
				msg.append("' in module '");
				msg.append(module.getName());
				msg.append("'.");
				Logger.error(msg.toString(), ex, WrapperGenerator.class);
			}
		}
	}

	private List<String> computeLanguages() {
		if (_languages != null) {
			return _languages;
		}
		return ResourcesModule.getInstance().getConfig().getSupportedLocales();
	}

	private void generateMessages(TLModule module, String language) throws IOException {
		File resourcesDir = resourcesDir();
		MessagesGenerator generator = new MessagesGenerator(resourcesDir, language, module);
		File resourceFile = generate(resourcesDir, generator);
		normalizeResourceFile(resourceFile);
	}

	private File resourcesDir() {
		if (_resourcesPath != null) {
			return _resourcesPath;
		} else {
			return new File(Workspace.topLevelWebapp(), ModuleLayoutConstants.RESOURCES_PATH);
		}
	}

	private void normalizeResourceFile(File file) throws IOException {
		new ResourceFile(file).save();
	}

	private void checkConsistency(TLModule module) {
		if (javaImplementationPackage(module) == null && !_noWrap) {
			_log.error("No java implementation package found for module '" + module.getName() + "'.");
		}
		if (javaInterfacePackage(module) == null) {
			_log.error("No java interface package found for module '" + module.getName() + "'.");
		}
		_log.checkErrors();
	}

	private void generateImplementation(TLClass type) throws IOException {
		if (_noWrap) {
			return;
		}
		ImplementationTemplateGenerator generator = new ImplementationTemplateGenerator(type);
		generator.setNoElementClasses(_noElementClasses);
		if (type.isAbstract() && type.getAnnotation(JavaClass.class) == null) {
			// No need for empty implementations.
			File targetFile = targetFile(generator);
			if (targetFile.exists()) {
				targetFile.delete();
			}
			return;
		}
		_log.info("Generate implementation for type '" + type.getName() + "' in module '"
			+ type.getModule().getName() + "'.");
		generate(generator);
	}

	private void generateInterfaceTemplate(TLClass type) throws IOException {
		if (_noInterfaceTemplate) {
			return;
		}
		_log.info("Generate interface templates for type '" + type.getName() + "' in module '"
			+ type.getModule().getName() + "'.");
		TLTypeGenerator generator = new InterfaceTemplateGenerator(type);
		generator.setNoElementClasses(_noElementClasses);
		update(generator);
	}

	private void update(JavaGenerator generator) throws IOException {
		if (!_update && exists(generator)) {
			// Do create template, do not update
			return;
		}
		generate(generator);
	}

	private boolean exists(JavaGenerator generator) {
		return targetFile(generator).exists();
	}

	private File targetFile(JavaGenerator generator) {
		return targetFile(generator.packageName(), generator.className());
	}

	private File targetFile(String packageName, String className) {
		File packageFile = packageDir(packageName);
		File targetFile = new File(packageFile, className + ".java");
		return targetFile;
	}
	
	private void removeLegacyImplementation(TLClass type) {
		String legacyImplementationBaseName = legacyImplementationBaseName(type);
		if (legacyImplementationBaseName == null) {
			return;
		}
		String packageName = TLModelGenerator.packageName(legacyImplementationBaseName);
		String className = TLModelGenerator.simpleClassName(legacyImplementationBaseName);
		File targetFile = targetFile(packageName, className);
		boolean exists = targetFile.exists();
		if (exists) {
			// Remove legacy code.
			targetFile.delete();
		}
	}

	private void generateInterface(TLClass type) throws IOException {
		if (_noIntf) {
			return;
		}
		_log.info(
			"Generate interface for type '" + type.getName() + "' in module '" + type.getModule().getName() + "'.");
		BaseClassGenerator generator = new InterfaceGenerator(type);
		generator.setNoElementClasses(_noElementClasses);
		generator.setNoSetter(_noPropertySetter);
		generator.setNoGetter(_noPropertyGetter);
		if (_setterPrefix != null) {
			generator.setSetterPrefix(_setterPrefix);
		}
		if (_getterPrefix != null) {
			generator.setGetterPrefix(_getterPrefix);
		}
		generate(generator);
	}

	private void generateConstants(TLClass type) throws IOException {
		if (_noConst) {
			return;
		}

		TLTypeGenerator generator = new ConstantGenerator(type);
		generator.setNoElementClasses(_noElementClasses);
		File targetFile = targetFile(generator);
		boolean exists = targetFile.exists();
		if (exists) {
			// Since separate constants interfaces are deprecated, only generate them, if they
			// already exist.

			if (_update) {
				targetFile.delete();
			} else {
				_log.info(
					"Generate constants for type '" + type.getName() + "' in module '" + type.getModule().getName()
						+ "'.");
				generate(generator);
			}
		}
	}

	private void generateFactory(TLModule module) throws IOException {
		if (_noFactory) {
			return;
		}
		String factoryClassName = factoryClassName(module);
		_log.info("Generate factory '" + factoryClassName + "' for module '" + module.getName() + "'.");
		if (factoryClassName == null) {
			_log.error("No factory class name found for module '" + module.getName() + "'.");
			return;
		}
		FactoryGenerator generator = new FactoryGenerator(factoryClassName, module);
		generator.setNoElementClasses(_noElementClasses);
		generate(generator);
	}

	private void generate(JavaGenerator generator) throws IOException {
		generate(mkdir(packageDir(generator.packageName())), generator);
	}

	private File mkdir(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	private File generate(File dir, FileGenerator generator) throws IOException {
		File sourceFile = new File(dir, generator.fileName());

		generator.generate(sourceFile);
		return sourceFile;
	}

	private TLModel createModel() {
		final TLModel model = new TLModelImpl();

		ThreadContext.inSystemContext(WrapperGenerator.class, new Computation<Void>() {
			@Override
			public Void run() {
				TransientModelCreator transientModelCreator =
					new TransientModelCreator(_log, model, TransientObjectFactory.INSTANCE);
				transientModelCreator.fillModel();
				return null;
			}
		});

		_log.checkErrors();
		return model;
	}

	private File packageDir(String packageName) {
		if (packageName.length() == 0) {
			return new File(_outDir, ".");
		} else {
			File packageDir = new File(_outDir, packageName.replace('.', '/'));
			return packageDir;
		}
	}

	/**
	 * Main method to execute wrapper generation.
	 */
	public static void main(String[] args) throws Exception {
		new WrapperGenerator().run(args);
	}

	private static void help(PrintStream out) {
		out.println("Usage " + WrapperGenerator.class.getClass().getSimpleName() + " " +
			MODULE_OPTION + " <module-name> " +
			OUTPUT_DIR_OPTION + " <output-dir> " +
			"[" + LANG_OPTION + " <languages>]" +
			"[" + NO_FACTORY_OPTION + "]" +
			"[" + NO_CONSTANTS_OPTION + "]" +
			"[" + NO_WRAPPERS_OPTION + "]" +
			"[" + NO_INTERFACE_TEMPLATE_OPTION + "]" +
			"[" + NO_INTERFACE_OPTION + "]" +
			"[" + HELP_OPTION + "]");
	}

	private static void help() {
		help(System.out);
	}

}

