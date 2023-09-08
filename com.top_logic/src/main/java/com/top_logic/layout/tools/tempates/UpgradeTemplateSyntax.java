/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.tempates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.Main;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.processor.LayoutModelConstants;
import com.top_logic.layout.tools.FileHandler;
import com.top_logic.layout.tools.FileUtil;

/**
 * Tool to process layout XML files and add/normalize resource keys and prefixes defined in those
 * files.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpgradeTemplateSyntax extends Main implements FileHandler {

	private static final String COMPONENT_NAME_PARAM = "componentName";

	private static final String COMPONENT_ID_PARAM = "componentId";
	private static final String PROJECT_LAYOUT_NAME_PARAM = "ProjectLayoutName";
	private static final String NAVIGATION_TREE_NAME_PARAM = "NavigationTreeName";
	private static final String PROJECT_TREE_NAME_PARAM = "ProjectTreeName";
	private static final String NAVIGATION_TREE_SUFFIX = "_navigationTree";
	private static final String PARENT_NAME_PARAM = "parentName";
	private static final Map<String, String> MAPPING = new HashMap<>();

	private static final Pattern NEW_VAR_PATTERN = Pattern.compile(
		"(?:" + Pattern.quote(LayoutModelConstants.VARIABLE_PREFIX) + "[^\\}]*"
			+ Pattern.quote(LayoutModelConstants.VARIABLE_SUFFIX) + ")" + "|" +
		"(?:" + Pattern.quote(LayoutModelConstants.PARAMETER_PREFIX) + "[^\\}]*"
			+ Pattern.quote(LayoutModelConstants.PARAMETER_SUFFIX) + ")"
	);

	private File _currentLayout;

	private Set<String> _currentVariableNames;

	private Rewriter _rewriter;

	{
		// Reserved words within an include or fragment element.
		MAPPING.put(LayoutModelConstants.INCLUDE_NAME, COMPONENT_NAME_PARAM);
		MAPPING.put(LayoutModelConstants.INCLUDE_ID, COMPONENT_ID_PARAM);
		MAPPING.put(LayoutModelConstants.INJECT_ELEMENT, "injection");

		// Legacy param names that are wrong, misleading, or verbose.
		MAPPING.put("tab", "tabs");
		MAPPING.put("resourcekey", "resPrefix");
		MAPPING.put("layoutreferencename", "layout");
	}

	/**
	 * Main entry point.
	 * 
	 * @param args
	 *        {@link File#pathSeparator}-separated paths with files to process.
	 */
	public static void main(String[] args) throws Exception {
		UpgradeTemplateSyntax tool = new UpgradeTemplateSyntax();
		tool.runMainCommandLine(args);
	}

	@Override
	protected boolean argumentsRequired() {
		return true;
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
				if (!file.getName().endsWith(LayoutModelConstants.XML_SUFFIX)) {
					continue;
				}

				update(file);
			}
		}
	}

	private void update(File file) throws SAXException, IOException, FileNotFoundException {
		File aliasFile = new File(file.getParentFile(), aliasName(file.getName()));
		process(file, aliasFile);
	}

	private String aliasName(String name) {
		return name.substring(0, name.length() - LayoutModelConstants.XML_SUFFIX.length()) + "_alias.xml";
	}

	private void process(File file, File aliasFile) throws SAXException, IOException, FileNotFoundException {
		_currentLayout = file;
		System.out.println("Processing: " + file);

		Document layout = parse(file);
		if (aliasFile.exists()) {
			Document alias = parse(aliasFile);

			Element template = layout.createElementNS(null, LayoutModelConstants.TEMPLATE_ELEMENT);
			Element params = appendElement(template, LayoutModelConstants.PARAMS_ELEMENT);

			// Separate parameter declaration and content.
			appendText(template, "\n\n");

			List<String> names = createTemplateParams(params, alias);
			_currentVariableNames = getTemplateParams(alias);

			Element content = layout.getDocumentElement();
			replace(content, template);
			template.appendChild(content);

			if (names.isEmpty()) {
				remove(params);
			} else {
				_rewriter = Rewriter.createVariableRewriter(names, getPrefix(alias), getPostFix(alias));
				_rewriter.rewriteElement(template);
			}

			// Note: The old alias file must not be deleted in the same version because upgrading
			// depends on reading the old alias content.
			//
//			boolean ok = aliasFile.delete();
//			if (!ok) {
//				System.err.println("ERROR: Cannot delete alias file: " + aliasFile);
//			}
		} else {
			_currentVariableNames = Collections.emptySet();
		}

		rewriteReferences(layout.getDocumentElement());

		dump(layout, file);

		_currentVariableNames = null;
		_currentLayout = null;
		_rewriter = null;
	}

	private void rewriteReferences(Element element) {
		Node next;
		for (Node child = element.getFirstChild(); child != null; child = next) {
			next = child.getNextSibling();

			if (child instanceof Element) {
				rewriteReferences((Element) child);
			}
		}

		if (DOMUtil.isElement(null, "component", element) ||
			DOMUtil.isElement(null, "dialog", element)) {
			String classAttr = element.getAttributeNS(null, "class");
			if (classAttr.endsWith("ReferenceComponent")) {
				replaceWithInclude(element);
			} else if (classAttr.endsWith("FragmentComponent")) {
				System.err.println("WARNING: Must manually transform include target to a fragment template: "
					+ element.getAttributeNS(null, "name") + LayoutModelConstants.XML_SUFFIX);
				replaceWithInclude(element);
			}
		}
	}

	private void replaceWithInclude(Element reference) {
		Element include = createElement(reference, LayoutModelConstants.INCLUDE_ELEMENT);
		replace(reference, include);

		rewriteArguments(include, reference);
	}

	private List<String> createTemplateParams(Element params, Document alias) {
		ArrayList<String> origNames = new ArrayList<>();
		HashSet<String> newNames = new HashSet<>();
		Element aliases = getElement(alias, "aliases");
		Element mandatories = getElement(aliases, "mandatories");
		if (mandatories != null) {
			for (Element def : DOMUtil.elementsNS(mandatories, null, "mandatory")) {
				String name = paramName(def);
				origNames.add(name);

				String templateParam = getTemplateParamName(name);
				if (clash(newNames, templateParam)) {
					continue;
				}

				createTemplateParam(params, name, templateParam, null);
			}
		}
		Element optionals = getElement(aliases, "optionals");
		if (optionals != null) {
			for (Element def : DOMUtil.elementsNS(optionals, null, "optional")) {
				String name = paramName(def);
				origNames.add(name);

				String templateParam = getTemplateParamName(name);
				if (clash(newNames, templateParam)) {
					continue;
				}

				if (def.hasAttribute("defaultValue")) {
					createTemplateParam(params, name, templateParam, def.getAttributeNS(null, "defaultValue"));
				} else {
					Element param = createTemplateParam(params, name, templateParam, null);

					boolean hasChild = false;
					for (Node child : DOMUtil.children(def)) {
						param.appendChild(param.getOwnerDocument().importNode(child, true));
						hasChild = true;
					}

					if (!hasChild) {
						Node comment = param.getOwnerDocument().createComment("Empty by default.");
						param.appendChild(comment);
					}
				}
			}
		}

		return origNames;
	}

	private Set<String> getTemplateParams(Document alias) {
		Set<String> newNames = new HashSet<>();
		Element aliases = getElement(alias, "aliases");
		Element mandatories = getElement(aliases, "mandatories");
		if (mandatories != null) {
			for (Element def : DOMUtil.elementsNS(mandatories, null, "mandatory")) {
				String name = paramName(def);
				String templateParam = getTemplateParamName(name);
				if (clash(newNames, templateParam)) {
					continue;
				}
			}
		}
		Element optionals = getElement(aliases, "optionals");
		if (optionals != null) {
			for (Element def : DOMUtil.elementsNS(optionals, null, "optional")) {
				String name = paramName(def);

				String templateParam = getTemplateParamName(name);
				if (clash(newNames, templateParam)) {
					continue;
				}
			}
		}

		return newNames;
	}

	private Element createTemplateParam(Element params, String name, String templateParam, String defaultValue) {
		if (PROJECT_TREE_NAME_PARAM.equals(name)) {
			createTemplateParam(params, PARENT_NAME_PARAM, "");
		}

		Element param = createTemplateParam(params, templateParam, defaultValue);

		// Create default values for params completely filled by the layout generator.
		if (StringServices.isEmpty(defaultValue)) {
			if (PROJECT_TREE_NAME_PARAM.equals(name)) {
				param.setAttributeNS(null, "value", LayoutModelConstants.VARIABLE_PREFIX + PARENT_NAME_PARAM
					+ LayoutModelConstants.VARIABLE_SUFFIX + NAVIGATION_TREE_SUFFIX);
			}
			else if (NAVIGATION_TREE_NAME_PARAM.equals(name)) {
				param.setAttributeNS(null, "value", LayoutModelConstants.VARIABLE_PREFIX + COMPONENT_NAME_PARAM
					+ LayoutModelConstants.VARIABLE_SUFFIX + NAVIGATION_TREE_SUFFIX);
			}
			else if (PROJECT_LAYOUT_NAME_PARAM.equals(name)) {
				param.setAttributeNS(null, "value", LayoutModelConstants.VARIABLE_PREFIX + COMPONENT_NAME_PARAM
					+ LayoutModelConstants.VARIABLE_SUFFIX + "_PL");
			}
		}
		return param;
	}

	private Element createTemplateParam(Element params, String templateParam, String defaultValue) {
		Element param = createTemplateParam(params, templateParam);
		if (defaultValue != null) {
			param.setAttributeNS(null, LayoutModelConstants.PARAM_VALUE, defaultValue);
		}
		return param;
	}

	private boolean clash(Set<String> newNames, String templateParam) {
		boolean clash = !newNames.add(templateParam);
		if (clash) {
			System.err.println("ERROR: Duplicate parameter name: " + templateParam);
		}
		return clash;
	}

	private Element createTemplateParam(Element params, String name) {
		Element param = appendElement(params, LayoutModelConstants.PARAM_ELEMENT);
		setTemplateParamName(param, name);
		return param;
	}

	private void setTemplateParamName(Element param, String name) {
		param.setAttributeNS(null, LayoutModelConstants.PARAM_NAME, name);
	}

	private String getPostFix(Document alias) {
		String postfix = alias.getDocumentElement().getAttributeNS(null, "postfix");
		if (StringServices.isEmpty(postfix)) {
			postfix = "$";
		}
		return postfix;
	}

	private String getPrefix(Document alias) {
		String prefix = alias.getDocumentElement().getAttributeNS(null, "prefix");
		if (StringServices.isEmpty(prefix)) {
			prefix = "$";
		}
		return prefix;
	}


	private void rewriteArguments(Element include, Element referenceComponent) {
		String referenceName = referenceComponent.getAttributeNS(null, "name");
		String templateName = toIncludeName(referenceName);
		include.setAttributeNS(null, LayoutModelConstants.INCLUDE_NAME, templateName);

		Rewriter rewriter = createParamRewriter(templateName);

		Element inject = null;
		for (Element substitutions : DOMUtil.elements(referenceComponent)) {
			if (DOMUtil.isElement(null, "substitutions", substitutions)) {
				for (Element substitution : DOMUtil.elementsNS(substitutions, null, "substitution")) {
					String key = substitution.getAttributeNS(null, "key");
					if (StringServices.isEmpty(key)) {
						System.err.println("ERROR: Invalid empty 'key' argument.");
						continue;
					}

					String name = getTemplateParamName(key);
					Attr valueAttr = substitution.getAttributeNodeNS(null, "value");
					if (valueAttr != null) {
						String textValue = valueAttr.getTextContent();

						if (PROJECT_TREE_NAME_PARAM.equals(key) && textValue.endsWith(NAVIGATION_TREE_SUFFIX)) {
							include.setAttributeNS(null, PARENT_NAME_PARAM,
								textValue.substring(0, textValue.length() - NAVIGATION_TREE_SUFFIX.length()));
						}

						include.setAttributeNS(null, name, rewriter.rewriteString(textValue));
					} else {
						Element argumentElement = appendElement(include, name);
						rewriter.rewriteContent(argumentElement);
						while (true) {
							Node child = substitution.getFirstChild();
							if (child == null) {
								break;
							}

							remove(child);
							argumentElement.appendChild(child);
						}
					}
				}
			} else {
				if (inject == null) {
					inject = appendElement(include, LayoutModelConstants.INJECT_ELEMENT);
				}
				rewriter.rewriteElement(substitutions);
				remove(substitutions);
				inject.appendChild(substitutions);
			}
		}
	}

	private String toIncludeName(String referenceName) {
		if (_rewriter != null && endsWithVariable(referenceName)) {
			return referenceName;
		} else {
			return referenceName + LayoutModelConstants.XML_SUFFIX;
		}
	}

	private boolean endsWithVariable(String value) {
		Matcher matcher = NEW_VAR_PATTERN.matcher(value);
		int start = 0;
		while (matcher.find(start)) {
			if (matcher.end() == value.length()) {
				return true;
			}
			start = matcher.end();
		}
		return false;
	}

	private Rewriter createParamRewriter(String templateName) {
		File base = _currentLayout.getParentFile();
		while (base != null && !base.getName().equals("layouts")) {
			base = base.getParentFile();
		}
		if (base == null) {
			System.err.println("ERROR: Cannot resolve template '" + templateName + "' relative to '" + _currentLayout
				+ "'.");
			return Rewriter.createParamRewriter(Collections.<String> emptyList(), "$", "$", null);
		}
		File aliasFile = new File(base, aliasName(templateName));
		if (!aliasFile.exists()) {
			// Template does not declare parameters.
			return Rewriter.createParamRewriter(Collections.<String> emptyList(), "$", "$", null);
		}

		Document alias;
		try {
			alias = parse(aliasFile);
		} catch (Exception ex) {
			System.err.println("ERROR: Cannot parse alias file '" + aliasFile + "': " + ex.getMessage());
			return Rewriter.createParamRewriter(Collections.<String> emptyList(), "$", "$", null);
		}

		Set<String> templateParams = getTemplateParams(alias);

		// If the same name is used, the variable overwrites the parameter cross-reference.
		templateParams.removeAll(_currentVariableNames);

		return Rewriter.createParamRewriter(templateParams, getPrefix(alias), getPostFix(alias), null);
	}

	private Element getElement(Node parent, String name) {
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (name.equals(child.getLocalName())) {
				return (Element) child;
			}
		}
		return null;
	}

	private Element appendElement(Node parent, String name) {
		Element result = createElement(parent, name);
		parent.appendChild(result);
		return result;
	}

	private Text appendText(Node parent, String text) {
		Text result = createText(parent, text);
		parent.appendChild(result);
		return result;
	}

	private Element createElement(Node parent, String name) {
		Element result = parent.getOwnerDocument().createElementNS(null, name);
		return result;
	}

	private Text createText(Node parent, String text) {
		Text result = parent.getOwnerDocument().createTextNode(text);
		return result;
	}

	private void replace(Node orig, Node replacement) {
		orig.getParentNode().replaceChild(replacement, orig);
	}

	private Node remove(Node node) {
		node.getParentNode().removeChild(node);
		return node;
	}

	private String paramName(Element def) {
		return def.getAttributeNS(null, "key");
	}

	static String getTemplateParamName(String aliasParamName) {
		String mappedName = MAPPING.get(aliasParamName.toLowerCase());
		if (mappedName != null) {
			return mappedName;
		}

		return CodeUtil.toLowerCaseStart(CodeUtil.toCamelCase(aliasParamName));
	}

	private static Document parse(File file) throws SAXException, IOException {
		Document document = DOMUtil.getDocumentBuilder().parse(file);
		return document;
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
