/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMain;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.state.ViewStateManager;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.SecurityConfig;

/**
 * Tool for updating selected xml file (or recursive in a selected directory), by replacing all
 * component names by its qualified variant.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateComponentName extends XMain {

	private static final String TEMPLATE_NS = "http://www.top-logic.com/ns/template/1.0";

	private static final String ACTION = "action";

	private static final String SECURITY = "security";

	private static final String VIEW_STATE_MANAGER_TAG = "root";

	private static final String FILE_PROPERTY = "file";

	private static final String MODULE_DIR_PROPERTY = "module";

	private static final String ONLY_UNTYPED_REPLACEMENT_PROPERTY = "only-untyped-replacement";

	private boolean _onlyUntyped = false;

	private File _file;

	private File _tmpFile;

	Map<String, ConfigurationDescriptor> _globalDescriptorsByName = new HashMap<>();

	Map<String, Collection<ComponentName>> _nameMapping = Collections.emptyMap();

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (FILE_PROPERTY.equals(option)) {
			_file = new File(args[i++]);
			return i;
		}
		if (MODULE_DIR_PROPERTY.equals(option)) {
			setWebapp(FileUtilities.canonicalize(new File(new File(args[i++]), ModuleLayoutConstants.WEBAPP_DIR))
				.getAbsolutePath());
			return i;
		}
		if (ONLY_UNTYPED_REPLACEMENT_PROPERTY.equals(option)) {
			_onlyUntyped = true;
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void setUp(String[] args) throws Exception {
		super.setUp(args);
		if (_file == null) {
			throw new IllegalStateException("Property '" + FILE_PROPERTY + "' needed.");
		}
	}

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();
		createComponentNameMapping();
		_globalDescriptorsByName.put(ACTION, TypedConfiguration.getConfigurationDescriptor(ApplicationAction.class));
		_globalDescriptorsByName.put(SECURITY, TypedConfiguration.getConfigurationDescriptor(SecurityConfig.class));
		doPerformanceRecursive(_file);
	}

	private void createComponentNameMapping() throws ModuleException, IOException {
		getProtocol().info("Fetching component name mapping.");
		_nameMapping = new HashMap<>();
		CreateComponentNameMapping.createMapping(getProtocol(), new File(getWebapp())).entrySet()
			.forEach(entry -> _nameMapping.put(entry.getKey().localName(), entry.getValue()));
	}

	private void doPerformanceRecursive(File file) throws IOException {
		if (file.getName().endsWith(FileUtilities.XML_FILE_ENDING)) {
			try {
				migrate(file);
			} catch (XMLStreamException | SAXException | ParserConfigurationException ex) {
				getProtocol().error("Unable to process " + file.getCanonicalPath(), ex);
			}
			return;
		}
		if (FileUtilities.IS_DIRECTORY_FILTER.accept(file)) {
			for (File f : FileUtilities.listFiles(file)) {
				doPerformanceRecursive(f);
			}
		}
	}

	private void migrate(File file) throws IOException, XMLStreamException, SAXException, ParserConfigurationException {
		ReadResult result = readFile(file);
		if (result.noConfiguration()) {
			return;
		}

		boolean modified = qualifyComponentNames(result.getConfig(), file.getCanonicalPath());
		if (!modified) {
			// nothing modified
			return;
		}

		_tmpFile = File.createTempFile("updateCompName", FileUtilities.XML_FILE_ENDING);
		_tmpFile.deleteOnExit();
		try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(_tmpFile), StringServices.UTF8)) {
			result.writeBack(out);
		}
		XMLPrettyPrinter.normalizeFile(_tmpFile);
		file.delete();
		_tmpFile.renameTo(file);
		logFileQualified(file);
	}

	private void logFileQualified(File file) throws IOException {
		getProtocol().info(file.getCanonicalPath() + " qualified.");
	}

	@SuppressWarnings("unused")
	private ReadResult readFile(File file) throws IOException, SAXException {
		if (_onlyUntyped) {
			return updateComponentNamesUntyped(file);
		}
		try {
			return readConfigurationItem(file);
		} catch (Exception ex) {
			if (false) {
				return updateComponentNamesTyped(file);
			}
			return updateComponentNamesUntyped(file);
		}
	}

	private ReadResult readConfigurationItem(File file) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		ConfigurationItem config = ConfigurationReader.readFile(log, _globalDescriptorsByName, file);
		log.checkErrors();
		return new ReadResult(config) {

			@Override
			public void writeBack(OutputStreamWriter out) throws XMLStreamException, IOException {
				ConfigurationWriter configWriter = new ConfigurationWriter(out);
				if (_config instanceof ApplicationAction) {
					configWriter.write(ACTION, _globalDescriptorsByName.get(ACTION), _config);
				} else if (_config instanceof SecurityConfig) {
					configWriter.write(SECURITY, _globalDescriptorsByName.get(SECURITY), _config);
				} else if (_config instanceof ViewStateManager.Config) {
					configWriter.write(VIEW_STATE_MANAGER_TAG, ConfigurationItem.class, _config);
				} else {
					getProtocol().info("Unknown configuration type in file " + file.getCanonicalPath() + ".",
						Protocol.WARN);
				}

			}

		};
	}

	private ReadResult updateComponentNamesUntyped(File file) throws SAXException, IOException {
		Document doc = DOMUtil.getDocumentBuilder().parse(file);
		Element element = doc.getDocumentElement();
		boolean anyModified = replaceAttributeValuesRecursive(file, element);
		if (!anyModified) {
			return ReadResult.empty();
		}
		try (OutputStream stream = new FileOutputStream(file)) {
			XMLPrettyPrinter.dump(stream, element);
		}
		logFileQualified(file);
		// return dummy to break computation.
		return ReadResult.empty();
	}

	private boolean replaceAttributeValuesRecursive(File file, Element element) throws IOException {
		boolean result = replaceAttributeValues(file, element);
		for (Element child : DOMUtil.elements(element)) {
			boolean childResult = replaceAttributeValuesRecursive(file, child);
			result |= childResult;
		}
		return result;
	}

	private boolean replaceAttributeValues(File file, Element element) throws IOException {
		boolean modified = false;
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			Collection<ComponentName> value = _nameMapping.get(attribute.getNodeValue());
			if (value == null) {
				continue;
			}
			if (value.size() == 1) {
				modified = true;
				attribute.setNodeValue(value.iterator().next().qualifiedName());
			} else {
				getProtocol()
					.error("Multiple qualified names for component name " + attribute.getNodeValue() + " in file "
						+ file.getCanonicalPath() + ": " + value);
			}
		}
		return modified;
	}

	private ReadResult updateComponentNamesTyped(File file)
			throws SAXException, IOException {
		Document doc = DOMUtil.getDocumentBuilder().parse(file);
		if (!TEMPLATE_NS.equals(doc.getDocumentElement().getNamespaceURI())) {
			return ReadResult.empty();
		}
		String bodyTagName = "body";
		Node bodyElement;
		NodeList elementsByTagNameNS = doc.getElementsByTagNameNS(TEMPLATE_NS, bodyTagName);
		switch (elementsByTagNameNS.getLength()) {
			case 0: {
				// No body
				getProtocol().info("No '" + bodyTagName + "' tag in file " + file.getCanonicalPath());
				return ReadResult.empty();
			}
			case 1: {
				bodyElement = elementsByTagNameNS.item(0);
				break;
			}
			default: {
				getProtocol().info("Multiple '" + bodyTagName + "' tags in file " + file.getCanonicalPath());
				return ReadResult.empty();
			}
		}
		Node configChild = singleElementChild(bodyElement, file);
		if (configChild == null) {
			getProtocol().error("No action in '" + bodyTagName + "' tag of file " + file.getCanonicalPath());
			return ReadResult.empty();
		}
		BinaryContent configContent = DOMUtil.toBinaryContent(configChild, file.getCanonicalPath());
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, _globalDescriptorsByName);
		reader.setSource(configContent);
		ConfigurationItem config;
		try {
			config = reader.read();
		} catch (ConfigurationException ex) {
			getProtocol().error("Unable to read action in of file " + file.getCanonicalPath(), ex);
			return ReadResult.empty();
		}
		return new ReadResult(config) {

			@Override
			public void writeBack(OutputStreamWriter out)
					throws XMLStreamException, IOException, SAXException, ParserConfigurationException {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				XMLStreamWriter streamWriter =
					XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(outStream, StringServices.UTF8);
				try {
					ConfigurationWriter configWriter = new ConfigurationWriter(streamWriter);
					if (_config instanceof ApplicationAction) {
						configWriter.write(ACTION, _globalDescriptorsByName.get(ACTION), _config);
					} else if (_config instanceof SecurityConfig) {
						configWriter.write(SECURITY, _globalDescriptorsByName.get(SECURITY), _config);
					} else {
						getProtocol().info("Unknown configuration type in file " + file.getCanonicalPath() + ".",
							Protocol.WARN);
						return;
					}
				} finally {
					streamWriter.close();
				}
				InputSource inputSource = new InputSource(
					new InputStreamReader(new ByteArrayInputStream(outStream.toByteArray()), StringServices.UTF8));
				Document document = DOMUtil.newDocumentBuilderNamespaceAware().parse(inputSource);
				Node configAsNode = bodyElement.getOwnerDocument().importNode(document.getDocumentElement(), true);
				bodyElement.replaceChild(configAsNode, configChild);
				try (XMLPrettyPrinter prettyPrinter =
					new XMLPrettyPrinter(out, XMLPrettyPrinter.newConfiguration())) {
					prettyPrinter.write(doc.getDocumentElement());
				}
			}

		};
	}

	private Node singleElementChild(Node bodyElement, File file) throws IOException {
		NodeList children = bodyElement.getChildNodes();

		Node singleElementChild = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			short childType = child.getNodeType();
			if (childType != Node.ELEMENT_NODE) {
				continue;
			}
			if (singleElementChild == null) {
				singleElementChild = child;
				continue;
			}
			getProtocol()
				.error("More than one child in '" + bodyElement.getLocalName() + "' tag of file "
					+ file.getCanonicalPath());
		}
		return singleElementChild;
	}

	private boolean qualifyComponentNames(ConfigurationItem config, String filePath) {
		Set<ComponentName> unknownLocalNames = new HashSet<>();
		Map<ComponentName, Collection<ComponentName>> multipleQualifiedNames = new HashMap<>();
		BooleanFlag modified = new BooleanFlag(false);
		LayoutUtils.qualifyComponentNames(localName -> {
			Collection<ComponentName> qualifiedNames = _nameMapping.get(localName.localName());
			if (qualifiedNames == null) {
				unknownLocalNames.add(localName);
				return localName;
			}
			if (qualifiedNames.size() == 1) {
				modified.set(true);
				return qualifiedNames.iterator().next();
			}
			multipleQualifiedNames.put(localName, qualifiedNames);
			return localName;
		}, config);
		if (!unknownLocalNames.isEmpty()) {
			getProtocol().error("Unknown local names '" + unknownLocalNames + "' in " + filePath + ".");
		}
		multipleQualifiedNames.entrySet().forEach( entry -> 
		getProtocol().error(
			"Multiple qualified names for local names '" + entry.getKey() + "' in " + filePath + ":"
				+ entry.getValue()));
		return modified.get();
	}

	public static void main(String[] args) throws Exception {
		new UpdateComponentName().runMain(args);
	}

	private abstract static class ReadResult {

		public static ReadResult empty() {
			return new ReadResult(null) {
				@Override
				public void writeBack(OutputStreamWriter out) throws XMLStreamException, IOException {
					throw new UnsupportedOperationException("empty result");
				}
			};
		}

		protected ConfigurationItem _config;

		public ReadResult(ConfigurationItem config) {
			_config = config;
		}

		public final ConfigurationItem getConfig() {
			return _config;
		}
		
		public final boolean noConfiguration() {
			return getConfig() == null;
		}

		public abstract void writeBack(OutputStreamWriter out)
				throws XMLStreamException, IOException, SAXException, ParserConfigurationException;
	}


}

