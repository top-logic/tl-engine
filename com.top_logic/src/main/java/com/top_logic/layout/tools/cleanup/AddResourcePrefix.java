/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.cleanup;

import static com.top_logic.basic.core.xml.DOMUtil.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.top_logic.basic.Main;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;

/**
 * Tool to process layout XML files and add/normalize resource keys and prefixes defined in those
 * files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AddResourcePrefix extends Main implements FileHandler {

	private final Map<String, Set<String>> _prefixMapping = new HashMap<>();

	private boolean _updateAll = true;

	private boolean _verbose;

	private boolean _masterFrame;

	private static final Set<String> IGNORED = new HashSet<>(
		Arrays.asList(
			"com.top_logic.mig.html.layout.FragmentComponent",
			"com.top_logic.layout.component.TabComponent",
			"com.top_logic.knowledge.gui.layout.ButtonComponent",
			"com.top_logic.tool.boundsec.CockpitLayout",
			"com.top_logic.tool.boundsec.WindowLayout",
			"com.top_logic.knowledge.gui.layout.TLMainLayout",
			"com.top_logic.tool.boundsec.BoundMainLayout",
			"com.top_logic.tool.boundsec.BoundLayout",
			"%INFO_COMPONENT%",
			"com.top_logic.tool.boundsec.compound.CompoundSecurityInfoComponent",
			"com.top_logic.extensions.ewe.layout.inbox.DynamicWrapperLayout",
			"%MAIN_LAYOUT_CLASS%"
			));

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *        {@link File#pathSeparator}-separated paths with files to process.
	 */
	public static void main(String[] args) throws Exception {
		AddResourcePrefix tool = new AddResourcePrefix();
		tool.runMainCommandLine(args);
		tool.storeMapping();
	}


	@Override
	protected boolean argumentsRequired() {
		return true;
	}

	@Override
	protected int longOption(String option, String[] args, int i) {
		if ("add-only".equals(option)) {
			_updateAll = false;
			return i;
		}
		if ("update-existing".equals(option)) {
			_updateAll = true;
			return i;
		}
		if ("verbose".equals(option)) {
			_verbose = true;
			return i;
		}
		if ("quiet".equals(option)) {
			_verbose = false;
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		FileUtil.handleFile(args[i++], this);
		return i;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		// Nothing more.
	}

	private void storeMapping() throws FileNotFoundException, IOException {
		ResourceMappingUtil.storeMappings(_prefixMapping);
	}

	@Override
	public void handleFile(String fileName) throws IOException, SAXException {
		File file = new File(fileName);
		if (file.isDirectory()) {
			scan(file);
		} else {
			update(file);
		}
	}

	private void scan(File dir) throws SAXException, IOException {
		for (File file : FileUtilities.listFiles(dir)) {
			if (file.getName().startsWith(".")) {
				continue;
			}
			if (file.isDirectory()) {
				scan(file);
			} else {
				if (file.getName().endsWith("_alias.xml")) {
					continue;
				}
				if (!file.getName().endsWith(".xml")) {
					continue;
				}
				update(file);
			}
		}
	}

	private void update(File file) throws SAXException, IOException, FileNotFoundException {
		String packageName = packageName(file);
		Document document = parse(file);
		_masterFrame = file.getName().matches("(?i).*masterframe.xml");
		boolean hasUpdate = update(packageName, document);
		if (hasUpdate) {
			System.out.println("Updating: " + file);
			dump(document, file);
		} else {
			if (_verbose) {
				System.out.println("Up to date: " + file);
			}
		}
	}

	private String packageName(File file) throws IOException {
		File canonicalFile = file.getCanonicalFile();
	
		String path = canonicalFile.getPath().replace(File.separatorChar, '/');
		String base = "/WEB-INF/layouts/";
		int baseIndex = path.indexOf(base);
		if (baseIndex < 0) {
			throw new IllegalArgumentException("File not within a web application's layouts directory: " + path);
		}
		String packageName = path.substring(baseIndex + base.length()).replaceFirst(".xml$", "").replace('/', '.');
		return packageName;
	}

	private static Document parse(File file) throws SAXException, IOException {
		Document document = DOMUtil.getDocumentBuilder().parse(file);
		return document;
	}

	private boolean update(String packageName, Node node) {
		boolean hasUpdate = false;
		for (Element element : elements(node)) {
			hasUpdate |= updateLocal(packageName, element);

			// Descend.
			hasUpdate |= update(packageName, element);
		}
		return hasUpdate;
	}

	private boolean updateLocal(String packageName, Element element) {
		if (isElement(null, "component", element)) {
			return updateComponent(packageName, element);
		}
		if (isElement(null, "open-handler", element)) {
			return updateHandler(packageName, element);
		}
		if (isElement(null, "handler", element)) {
			return updateHandler(packageName, element);
		}
		if (isElement(null, "button", element)) {
			return updateHandler(packageName, element);
		}
		if (isElement(null, "tab", element)) {
			return updateTab(packageName, element);
		}
		return false;
	}

	private boolean updateTab(String packageName, Element element) {
		Element info = childElement(element, null, "info");
		if (info == null) {
			return false;
		}

		String legacyKey = property(info, "TabInfo.name");

		boolean hasName = !StringServices.isEmpty(legacyKey);
		if (!hasName) {
			// Cannot generate a name.
			return false;
		}

		if (_masterFrame) {
			// Just record mapping to prevent rewriting a globally defined key.
			addMapping(legacyKey, legacyKey);
			return false;
		}

		if (addOnly()) {
			return false;
		}

		if (containsVariable(legacyKey)) {
			return false;
		}

		String canonicalName = legacyKey;
		if (canonicalName.endsWith(".tabber")) {
			canonicalName = canonicalName.substring(0, canonicalName.length() - ".tabber".length());
		}
		else if (canonicalName.endsWith(".tab")) {
			canonicalName = canonicalName.substring(0, canonicalName.length() - ".tab".length());
		}

		String canonicalKey = canonicalKey(packageName, camelCase(canonicalName)) + ".tab";
		info.setAttributeNS(null, "TabInfo.name", canonicalKey);

		addMapping(legacyKey, canonicalKey);
		return true;
	}

	private boolean updateHandler(String packageName, Element element) {
		String resourceKey = property(element, "resourceKey");
		boolean hasResourceKey = !StringServices.isEmpty(resourceKey);
		if (addOnly()) {
			if (hasResourceKey || _masterFrame) {
				return false;
			}
		}

		if (containsVariable(resourceKey)) {
			return false;
		}

		String id = property(element, "id");
		if (StringServices.isEmpty(id)) {
			return false;
		}
		String camelCaseName = camelCase(id);

		String canonicalKey = canonicalKey(packageName, camelCaseName);
		element.setAttributeNS(null, "resourceKey", canonicalKey);

		String legacyKey = hasResourceKey ? resourceKey : "tl.command." + id;

		addMapping(legacyKey, canonicalKey);
		return true;
	}

	private boolean containsVariable(String value) {
		return value != null && value.contains("$");
	}

	private boolean updateComponent(String packageName, Element element) {
		String resPrefix = property(element, "resPrefix");
		boolean hasResourcePrefix = !StringServices.isEmpty(resPrefix);
		if (addOnly()) {
			if (hasResourcePrefix || _masterFrame) {
				return false;
			}
		}

		if (containsVariable(resPrefix)) {
			return false;
		}

		String name = property(element, "name");
		if (StringServices.isEmpty(name)) {
			if (hasResourcePrefix) {
				name = "";
			} else {
				return false;
			}
		}

		String implClass = property(element, "class");
		if (implClass == null || implClass.endsWith("ReferenceComponent") || IGNORED.contains(implClass)) {
			if (hasResourcePrefix) {
				if (implClass == null) {
					implClass = "";
				}
			} else {
				return false;
			}
		}

		String localName = removeVariables(name);

		String camelCaseName = camelCase(localName);
		if (camelCaseName.isEmpty()) {
			// In case the complete component name has been aliased.
			camelCaseName = "main";
		}

		String legacyResPrefix = hasResourcePrefix ? resPrefix :
			(implClass.indexOf('.') < 0) ? implClass : I18NConstantsBase.getLegacyResPrefix(implClass) + ".";
		String canonicalResPrefix = canonicalPrefix(packageName, camelCaseName);
		element.setAttributeNS(null, "resPrefix", canonicalResPrefix);

		addMapping(legacyResPrefix, canonicalResPrefix);
		return true;
	}


	private boolean addOnly() {
		return !_updateAll;
	}

	private void addMapping(String legacyResPrefix, String canonicalResPrefix) {
		MultiMaps.add(_prefixMapping, legacyResPrefix, canonicalResPrefix);
	}

	private String canonicalKey(String packageName, String camelCaseName) {
		return "layouts." + packageName + "." + camelCaseName;
	}

	private String canonicalPrefix(String packageName, String camelCaseName) {
		return "layouts." + packageName + "." + camelCaseName + ".";
	}

	private String property(Element element, String name) {
		String result = element.getAttributeNS(null, name);
		if (StringServices.isEmpty(result)) {
			// Check for subtag configuration.
			Element propertyTag = childElement(element, null, name);
			if (propertyTag != null) {
				result = propertyTag.getTextContent();

				// Normalize for further processing.
				element.removeChild(propertyTag);
				element.setAttributeNS(null, name, result);
			}
		}
		return result;
	}

	private static Element childElement(Element element, String ns, String name) {
		for (Element child : elements(element)) {
			if (isElement(ns, name, child)) {
				return child;
			}
		}
		return null;
	}

	private String camelCase(String localName) {
		StringBuilder buffer = new StringBuilder();
		for (String part : localName.split("[^A-Za-z0-9]+")) {
			if (part.isEmpty()) {
				continue;
			}
			if (buffer.length() == 0) {
				buffer.append(part);
			} else {
				buffer.append(Character.toUpperCase(part.charAt(0)));
				buffer.append(part.substring(1));
			}
		}
		String camelCaseName = buffer.toString();
		return camelCaseName;
	}

	private String removeVariables(String name) {
		return name.replaceAll("(?:\\$\\{[^\\}]*\\})|(?:\\$[^\\$]+\\$)", "");
	}

	private static void dump(Document document, File file) throws FileNotFoundException, IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			XMLPrettyPrinter.dump(out, document);
		} finally {
			out.close();
		}
	}

}
