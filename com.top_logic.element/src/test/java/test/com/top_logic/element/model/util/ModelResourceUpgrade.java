/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Settings;
import com.top_logic.basic.XMain;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.tools.resources.ResourceFile;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Tool for upgrading legacy model I18N according to Ticket #23170.
 * 
 * <p>
 * The tool must be run in the application's module that should be upgraded (using the application's
 * class path). It then processes all I18N in this module and all its dependencies.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ModelResourceUpgrade extends XMain implements ComputationEx2<Void, ConfigurationException, IOException> {

	public static void main(String[] args) throws Exception {
		new ModelResourceUpgrade().runMainCommandLine(args);
	}

	private Map<String, List<String>> _resMapping = new HashMap<>();

	private Map<String, List<String>> _themeMapping = new HashMap<>();

	@Override
	protected boolean argumentsRequired() {
		return false;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		initFileManager();
		setupModuleContext(
			ModelService.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE,
			AttributeValueLocatorFactory.Module.INSTANCE,
			Settings.Module.INSTANCE,
			ThreadContextManager.Module.INSTANCE,
			DataAccessService.Module.INSTANCE,
			CompatibilityService.Module.INSTANCE);

		ThreadContextManager.inSystemInteraction(ModelResourceUpgrade.class, this);
	}

	@Override
	public Void run() throws ConfigurationException, IOException {
		loadModelMapping(ModelService.getApplicationModel());
		loadDBMapping();

		for (File baseDir : FileManager.getInstance().getIDEPaths()) {
			for (File resourceFile : files(baseDir, "WEB-INF/conf/resources/*.properties")) {
				upgradeResourceFile(_resMapping, resourceFile);
			}
			for (File resourceFile : files(baseDir, "../src/META-INF/themes/*/theme-settings.properties")) {
				upgradeResourceFile(_themeMapping, resourceFile);
			}
		}
		return null;
	}

	private static void upgradeResourceFile(Map<String, List<String>> mapping, File resourceFile) throws IOException {
		ResourceFile resources = new ResourceFile(resourceFile);

		for (String oldKey : new ArrayList<>(resources.getKeys())) {
			int suffixIndex = oldKey.indexOf('@');
			String oldKeyPrefix = suffixIndex >= 0 ? oldKey.substring(suffixIndex) : oldKey;
			List<String> newKeyPrefixes = mapping.get(oldKeyPrefix);
			if (newKeyPrefixes != null) {
				String oldValue = resources.removeProperty(oldKey);
				for (String newKeyPrefix : newKeyPrefixes) {
					String newKey = suffixIndex >= 0 ? newKeyPrefix + oldKey.substring(suffixIndex) : newKeyPrefix;
					resources.setProperty(newKey, oldValue);
				}
			}
		}

		System.out.println("Upgrading: " + resourceFile.getPath());
		resources.save();
	}

	private void loadModelMapping(TLModel model) {
		for (TLModule module : model.getModules()) {
			// Various ways how the module was internationalized.
			addResMapping(
				"element.typename." + module.getName(),
				"model." + module.getName());
			addResMapping(
				"element.typename." + module.getName() + ".name",
				"model." + module.getName());
			addResMapping(
				"class.com.top_logic.element.model.I18NConstants.MODULE_NAME." + module.getName(),
				"model." + module.getName());
			if (module.getName().equals(ApplicationObjectUtil.LEGACY_TABLE_TYPES_MODULE)) {
				for (TLType type : module.getTypes()) {
					String tableName = ApplicationObjectUtil.tableName(type);
					addThemeMapping(
						"mime." + tableName,
						"mime." + module.getName() + "." + type.getName());
					addThemeMapping(
						"mime." + tableName + ".large",
						"mime." + module.getName() + "." + type.getName() + ".large");
				}
			}

			for (TLType type : module.getTypes()) {
				addResMapping(
					"element.typename." + type.getName(),
					"model." + module.getName() + "." + type.getName());
				addResMapping(
					"element.typename." + module.getName() + "." + type.getName(),
					"model." + module.getName() + "." + type.getName());
				addResMapping(
					type.getName(),
					"model." + module.getName() + "." + type.getName());
				// The type's self column in tables.
				addResMapping(type.getName() + "._self", "model." + module.getName() + "." + type.getName() + "._self");

				if (type instanceof TLEnumeration) {
					loadEnumMapping(module.getName(), (TLEnumeration) type);
				} else if (type instanceof TLStructuredType) {
					for (TLStructuredTypePart part : ((TLStructuredType) type).getAllParts()) {
						addResMapping(
							type.getName() + "." + part.getName(),
							"model." + module.getName() + "." + type.getName() + "." + part.getName());
						// Attribute tool-tips.
						addResMapping(
							type.getName() + "." + part.getName() + ResKey.TOOLTIP,
							"model." + module.getName() + "." + type.getName() + "." + part.getName() + ResKey.TOOLTIP);
						// The table title for attributes displayed as table.
						addResMapping(
							type.getName() + "." + part.getName() + ".title",
							"model." + module.getName() + "." + type.getName() + "." + part.getName() + ".title");
					}
				}
			}
		}
	}

	private void loadEnumMapping(String moduleName, TLEnumeration type) {
		addResMapping(
			type.getName(),
			"model." + moduleName + "." + type.getName());
		for (TLClassifier part : type.getClassifiers()) {
			addResMapping(
				part.getName(),
				"model." + moduleName + "." + type.getName() + "." + part.getName());
		}
	}

	private void loadDBMapping() throws ConfigurationException {
		List<ResourceDeclaration> dbDeclarations = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class)
			.getTypeSystem("Default").getConfig().getDeclarations();
		MetaObjectsConfig db = MORepositoryBuilder
			.readTypeSystem(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, dbDeclarations);
		for (MetaObjectName table : db.getTypes().values()) {
			addResMapping(
				"element.typename." + table.getObjectName(),
				"model." + ApplicationObjectUtil.LEGACY_TABLE_TYPES_MODULE + "." + table.getObjectName() + "Table");
		}
	}

	private void addResMapping(String oldKey, String newKey) {
		addMapping(_resMapping, oldKey, newKey);
	}

	private void addThemeMapping(String oldKey, String newKey) {
		addMapping(_themeMapping, oldKey, newKey);
	}

	private static void addMapping(Map<String, List<String>> mapping, String oldKey, String newKey) {
		List<String> newKeys = mapping.get(oldKey);
		if (newKeys == null) {
			newKeys = new ArrayList<>();
			mapping.put(oldKey, newKeys);
		}
		newKeys.add(newKey);
	}

	interface FileResolver {

		Iterable<File> files(File baseDir);

	}

	private static Iterable<File> files(File baseDir, String pattern) {
		String[] parts = pattern.split("/");
		List<FileResolver> resolvers =
			Arrays.asList(parts).stream().map(ModelResourceUpgrade::resolver).collect(Collectors.toList());
		List<File> result = new ArrayList<>();
		addFiles(baseDir, resolvers, 0, result);
		return result;
	}

	static FileResolver resolver(String glob) {
		if (glob.equals("..")) {
			return new FileResolver() {
				@Override
				public Iterable<File> files(File baseDir) {
					return Collections.singletonList(baseDir.getParentFile());
				}
			};
		}
		if (glob.equals(".")) {
			return new FileResolver() {
				@Override
				public Iterable<File> files(File baseDir) {
					return Collections.singletonList(baseDir);
				}
			};
		}

		Pattern pattern = Pattern.compile(glob.replace(".", "\\.").replace("*", ".*").replace("?", "."));
		return new FileResolver() {
			@Override
			public Iterable<File> files(File baseDir) {
				return Arrays.asList(baseDir.listFiles(file -> pattern.matcher(file.getName()).matches()));
			}
		};
	}

	private static void addFiles(File baseDir, List<FileResolver> resolvers, int index, List<File> result) {
		if (index >= resolvers.size()) {
			return;
		}

		FileResolver resolver = resolvers.get(index);
		if (index == resolvers.size() - 1) {
			// Last section.
			for (File file : resolver.files(baseDir)) {
				result.add(file);
			}
		} else {
			for (File dir : resolver.files(baseDir)) {
				addFiles(dir, resolvers, index + 1, result);
			}
		}
	}

}
