/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.component;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CheckingProtocol;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.col.equal.EqualitySpecification;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.component.configuration.ComponentConfigEquality;
import com.top_logic.layout.editor.DynamicComponentService;
import com.top_logic.layout.processor.Application;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.layout.processor.ConstantLayout;
import com.top_logic.layout.processor.Expansion;
import com.top_logic.layout.processor.Expansion.NodeBuffer;
import com.top_logic.layout.processor.LayoutInline;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.processor.LayoutResolver;
import com.top_logic.layout.processor.NodeProcessor;
import com.top_logic.layout.processor.ParameterValue;
import com.top_logic.layout.processor.TemplateLayout;
import com.top_logic.mig.html.layout.CreateComponentParameter;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.LayoutVariables;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Test case for reading {@link com.top_logic.mig.html.layout.LayoutComponent.Config} descriptors
 * from XML.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestComponentConfiguration extends BasicTestCase {
	
	private static final FileFilter LAYOUT_XML_FILE = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			if (!pathname.isFile()) {
				return false;
			}
			String name = pathname.getName();
			if (!name.endsWith(".xml")) {
				return false;
			}
			if (name.endsWith("_alias.xml")) {
				return false;
			}
			if (name.endsWith("_shared.xml")) {
				return false;
			}
			if (name.endsWith("_relations.xml")) {
				return false;
			}
			if (name.endsWith(LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX)) {
				return false;
			}
			if (name.endsWith(LayoutModelConstants.DYNAMIC_LAYOUT_TEMPLATE_FILE_SUFFIX)) {
				return false;
			}
			return true;
		}
	};

	private static final FileFilter DIRECTORY = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};

	Log _log;

	private EqualitySpecification<ConfigurationItem> _equality = new ComponentConfigEquality() {
		@Override
		protected boolean equalsPropertyValue(Object valueLeft, Object valueRight, PropertyDescriptor property,
				Set<Set<ConfigurationItem>> stack) {
			boolean result = super.equalsPropertyValue(valueLeft, valueRight, property, stack);
			if (!result && property.isInstanceValued()) {
				_log.error("Values of property '" + property + "' not equal: " + id(valueLeft) + ", vs. "
					+ valueRight.getClass());
			}
			return result;
		}

		private Class<? extends Object> id(Object valueLeft) {
			return valueLeft == null ? null : valueLeft.getClass();
		}
	};

	private static final Class<?> ABSTRACT_CONFIGURED_CLASS_FACTORY = ReflectionUtils
		.getClass("com.top_logic.basic.config.DefaultConfigConstructorScheme$AbstractConfiguredClassFactory");

	private void checkComponentConfiguration(String layoutName, List<BinaryData> overlays) {
		ConfigurationItem componentConfig = checkReadComponent(layoutName, overlays);
		if (componentConfig != null) {
			checkSerializeComponent(layoutName, componentConfig);
			checkConfiguredInstance(layoutName, componentConfig);

			ConfigurationItem equalConfig = checkReadComponent(layoutName, overlays);
			if (!_equality.equals(componentConfig, equalConfig)) {
				_log.error(layoutName + ": Configuration is not equal to configuration read from the same file.");
			}
		}
	}

	private void checkConfiguredInstance(String layoutName, ConfigurationItem config) {
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM: {
					checkValueIsConfigured(layoutName, property, config.value(property));
					break;
				}
				case ARRAY: {
					Object arrayValue = config.value(property);
					for (int i = 0, size = Array.getLength(arrayValue); i < size; i++) {
						checkValueIsConfigured(layoutName, property, Array.get(arrayValue, i));
					}
					break;
				}
				case LIST: {
					for (Object value : (List<?>) config.value(property)) {
						checkValueIsConfigured(layoutName, property, value);
					}
					break;
				}
				case MAP: {
					for (Object value : ((Map<?, ?>) config.value(property)).values()) {
						checkValueIsConfigured(layoutName, property, value);
					}
					break;
				}
				default: {
					continue;
				}
			}
		}
	}

	private void checkValueIsConfigured(String layoutName, PropertyDescriptor property, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof ConfiguredInstance<?>) {
			checkConfiguredInstance(layoutName, ((ConfiguredInstance<?>) value).getConfig());
			return;
		}
		if (value instanceof ConfigurationItem) {
			checkConfiguredInstance(layoutName, (ConfigurationItem) value);
			return;
		}
		Class<?> valueClass = value.getClass();
		try {
			Factory factory = DefaultConfigConstructorScheme.getFactory(valueClass);
			if (ABSTRACT_CONFIGURED_CLASS_FACTORY.isInstance(factory)) {
				_log.error(layoutName + ": Property '" + property + "' has value with class '" + valueClass.getName()
				+ "' that uses configuration, but the value is not a ConfiguredInstance.");
			}
		} catch (ConfigurationException ex) {
			_log.error(layoutName + ": Cannot resolve factory for '" + valueClass.getName() + "'.");
		}
	}

	private void checkSerializeComponent(String layoutName, ConfigurationItem componentConfig) {
		try {
			try (Writer out = new StringWriter()) {
				new ConfigurationWriter(out).write("component", ConfigurationItem.class, componentConfig);
			}
		} catch (Throwable ex) {
			_log.error(layoutName + ": Unable to serialize config.", ex);
		}
	}

	private ConfigurationItem checkReadComponent(String layoutName, List<BinaryData> overlays) {
		try {
			return doCheckReadComponent(layoutName, overlays);
		} catch (Throwable ex) {
			_log.error(layoutName + ": Error reading configuration: " + ex.getMessage(), ex);
			return null;
		}
	}

	private ConfigurationItem doCheckReadComponent(String layoutName, List<BinaryData> overlays)
			throws Exception {
		CheckingProtocol protocol = new CheckingProtocol();
		LayoutResolver resolver = createLayoutResolver(protocol);
		DefaultInstantiationContext context = new DefaultInstantiationContext(protocol);

		if (layoutName.endsWith(LayoutModelConstants.LAYOUT_XML_FILE_SUFFIX)) {
			CreateComponentParameter parameters = CreateComponentParameter.newParameter(layoutName);
			parameters.setLayoutResolver(resolver);

			Config createLayoutConfiguration = LayoutStorage.createLayout(context, parameters, overlays).get();

			/* Check for problems */
			protocol.checkErrors();
			return createLayoutConfiguration;
		}

		ConstantLayout layout = resolver.getLayout(layoutName);

		TemplateLayout template = new TemplateLayout(layout);
		for (ParameterValue param : template.getDeclaredParameters().values()) {
			if (!param.isDefined()) {
				// Cannot parse, since there is a mandatory parameter.
				return null;
			}
		}

		NodeProcessor callback = new LayoutInline(resolver);
		Expansion expander =
			template.createContentExpander(null, null, Collections.<String, ParameterValue> emptyMap(), callback);
		Document document = DOMUtil.newDocument();
		Element layoutElement = document.createElement("layout");
		document.appendChild(layoutElement);

		expander.expandTemplate(new NodeBuffer(layoutElement, null), template.getContent());
		
		Iterable<Element> contentElements = DOMUtil.elements(layoutElement);
		if (nonUnique(contentElements)) {
			// Range template that cannot be parsed stand-alone.
			return null;
		}

		Element node = contentElements.iterator().next();
		if (!MainLayout.COMPONENT_DESCRIPTORS.keySet().contains(node.getLocalName())) {
			// Not a LayoutComponent
			return null;
		}

		// Check inlining successful
		protocol.checkErrors();

		BinaryContent binaryContent = toBinaryContent(node, layoutName);

		ConfigurationItem config = new ConfigurationReader(context, MainLayout.COMPONENT_DESCRIPTORS)
				.setSource(binaryContent)
			.setVariableExpander(LayoutVariables.layoutExpander(resolver.getTheme(), layoutName))
				.read();
		
		protocol.checkErrors();
		assertNotNull(config);
		return config;
	}

	private boolean nonUnique(Iterable<Element> elements) {
		Iterator<Element> iterator = elements.iterator();
		if (iterator.hasNext()) {
			iterator.next();
			return iterator.hasNext();
		} else {
			// No elements at all.
			return true;
		}
	}

	private static BinaryContent toBinaryContent(Node node, String layoutName) throws IOException {
		File tmpFile =
			File.createTempFile("layout_" + layoutName.replace('/', '_'), ".xml", Settings.getInstance().getTempDir());
		try (FileOutputStream fos = new FileOutputStream(tmpFile)) {
			transform(node, fos);
		}
		return FileBasedBinaryContent.createBinaryContent(tmpFile);
	}

	private static void transform(Node node, OutputStream os) throws IOException {
		DOMUtil.serializeXMLDocument(os, true, node);
	}

	private static LayoutResolver createLayoutResolver(Protocol log) throws IOException {
		Application application =
			CompileTimeApplication.createCompileTimeApplication(new File(".."),
				new File(ModuleLayoutConstants.WEBAPP_DIR));
		Theme theme = ThemeFactory.getTheme();
		return application.createLayoutResolver(log, theme);
	}

	/**
	 * Check that loading all component configuration succeeds.
	 */
	public void testComponentConfigurations() throws IOException {
		BufferingProtocol log = new BufferingProtocol();
		_log = log;
		File layoutsDir = AbstractBasicTestAll.potentiallyNotExistingLayoutDir();
		if (layoutsDir.exists()) {
			checkConfigsInDir(layoutsDir);
		}
		if (log.hasErrors()) {
			fail(log.getErrors().stream().collect(Collectors.joining("\n")), log.getFirstProblem());
		}
	}

	private void checkConfigsInDir(File layoutsDir) throws IOException {
		checkConfigsInPath(layoutsDir, "");
		checkOverlays(layoutsDir);
	}

	private void checkOverlays(File layoutsDir) throws IOException {
		Map<String, List<BinaryData>> overlays = findOverlays();
		for (Entry<String, List<BinaryData>> entry : overlays.entrySet()) {
			checkComponentConfiguration(entry.getKey(), entry.getValue());
		}
	}

	private static Map<String, List<BinaryData>> findOverlays() throws IOException {
		Map<String, List<BinaryData>> overlays = new HashMap<>();
		for (File webapp : FileManager.getInstance().getIDEPaths()) {
			if (!webapp.getName().equals(ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME)) {
				throw new IllegalArgumentException(
					"Expected " + webapp.getCanonicalPath() + " to be a web application.");
			}
			Path layoutFile = new File(webapp, ModuleLayoutConstants.LAYOUT_PATH).toPath();
			if (!Files.isDirectory(layoutFile)) {
				// No layouts
				continue;
			}
			FileUtilities.walkFileTree(layoutFile, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					FileVisitResult result = super.visitFile(file, attrs);
					if (file.toString().endsWith(LayoutModelConstants.LAYOUT_XML_OVERLAY_FILE_SUFFIX)) {
						String componentName = LayoutUtils.getOverlayedComponentPath(layoutFile.relativize(file));
						MultiMaps.add(overlays, componentName,
							BinaryDataFactory.createBinaryDataWithName(file, componentName), ArrayList::new);
					}
					return result;
				}
			});
		}
		overlays.values().forEach(Collections::reverse);
		return overlays;
	}

	private void checkConfigsInPath(File directory, String path) throws IOException {
		File[] subDirectories = FileUtilities.listFiles(directory, DIRECTORY);
		for (int n = 0, cnt = subDirectories.length; n < cnt; n++) {
			File subDir = subDirectories[n];
			checkConfigsInPath(subDir, path(path, subDir));
		}
		
		File[] configFiles = directory.listFiles(LAYOUT_XML_FILE);
		for (int n = 0, cnt = configFiles.length; n < cnt; n++) {
			File configFile = configFiles[n];
			String layoutName = path(path, configFile);
			/* Only layout files in the top level directory are tested. In the top level directory
			 * are no overlays for layouts defined in the top level directory. */
			checkComponentConfiguration(layoutName, Collections.emptyList());
		}
	}

	private static String path(String path, File next) {
		return path.isEmpty() ? next.getName() : path + "/" + next.getName();
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		/* Don't start the model service or the persistency layer: The layout files, especially all
		 * singletons referenced from those files, must not contain any persistent object: If the
		 * persistency layer is restarted, the persistent objects are no longer valid but would
		 * remain in the configuration or singleton instances. It is therefore a failure to use
		 * persistent objects in the configuration or singletons referenced from there. Starting the
		 * persistency layer would hide these problems. */
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestComponentConfiguration.class,
				// Layouts may refer to other typed layout definitions.
				DynamicComponentService.Module.INSTANCE,
				// Layouts may contain macros which try to ensure safe HTML.
				SafeHTML.Module.INSTANCE,
				CommandGroupRegistry.Module.INSTANCE));
	}

}
