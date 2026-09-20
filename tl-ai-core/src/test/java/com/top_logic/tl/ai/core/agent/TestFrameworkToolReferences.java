/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * Guards that every tool the shipped agents and skills refer to actually exists.
 *
 * <p>
 * A {@code <tool name="factory.method"/>} is a string in a configuration file pointing at a Java
 * method, and <b>nothing else checks that the two still agree</b>: the registry resolves the name at
 * boot, an entry that resolves to nothing is dropped, and the only symptom is an agent that quietly
 * lacks a capability. That is precisely the kind of defect a unit test should be catching, and it is
 * cheap to catch - the factory declares its tool class, the tool class declares its methods.
 * </p>
 *
 * <p>
 * The file is parsed rather than the booted registry queried, for the reason
 * {@link TestFrameworkSkillReferences} gives: what is under test is the shipped framework
 * configuration and nothing an application adds to it. Booting the registry would also need the whole
 * service stack for a question that is answered by two files.
 * </p>
 *
 * @see TestFrameworkSkillReferences
 */
public class TestFrameworkToolReferences extends TestCase {

	/** Relative to the repository root. */
	private static final String CONFIG_FILE =
		"tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.conf.config.xml";

	/** The factory that builds its tools by scanning a class for {@code @ToolMethod} methods. */
	private static final String REFLECTIVE =
		"com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory";

	/** Every tool an agent or a skill names is declared by a factory of this configuration. */
	public void testEveryReferencedToolExists() throws Exception {
		Document config = config();
		Map<String, String> factories = reflectiveFactories(config);
		Set<String> declared = declaredFactories(config);

		List<String> failures = new ArrayList<>();
		Set<String> references = toolReferences(config);
		for (String reference : references) {
			int dot = reference.indexOf('.');
			if (dot < 0) {
				failures.add("'" + reference + "' names no factory. A tool is referred to as"
					+ " '{factory}.{tool}'.");
				continue;
			}
			String factory = reference.substring(0, dot);
			String tool = reference.substring(dot + 1);
			if (!declared.contains(factory)) {
				failures.add("'" + reference + "' names the factory '" + factory + "', which this"
					+ " configuration does not declare.");
				continue;
			}
			String toolClass = factories.get(factory);
			if (toolClass == null) {
				// A factory that builds its tools some other way - the TL-Script tools of 'custom',
				// the generated subagent tools. What they offer is not decidable from a file.
				continue;
			}
			if (!toolsOf(toolClass).contains(tool)) {
				failures.add("'" + reference + "' names no @ToolMethod of " + toolClass + ", which"
					+ " offers " + toolsOf(toolClass) + ".");
			}
		}

		assertTrue(String.join("\n", failures), failures.isEmpty());
		assertTrue("The framework ships agents with tools; a run that found no reference at all is"
			+ " not testing the rule.", references.size() > 20);
	}

	/** The positive control: a reference that does not resolve is actually reported. */
	public void testAMisspelledToolWouldBeCaught() throws Exception {
		assertFalse("readText is the one this guard was written for.",
			toolsOf("com.top_logic.tl.ai.core.tools.text.TextEditTool").contains("readTexts"));
		assertTrue(toolsOf("com.top_logic.tl.ai.core.tools.text.TextEditTool").contains("readText"));
	}

	// ===== Reading the two sides =====

	/** The tool names the given class offers, as the registry will discover them. */
	private static Set<String> toolsOf(String toolClass) throws ReflectiveOperationException {
		Object provider = Class.forName(toolClass).getDeclaredConstructor().newInstance();
		Set<String> names = new TreeSet<>();
		for (Tool tool : AnnotatedTools.from(provider)) {
			names.add(tool.name());
		}
		return names;
	}

	/** Factory name to tool class, for the factories that declare one. */
	private static Map<String, String> reflectiveFactories(Document config) {
		Map<String, String> factories = new LinkedHashMap<>();
		for (Element factory : factoryElements(config)) {
			for (Element implementation : children(factory, "implementation")) {
				if (REFLECTIVE.equals(implementation.getAttribute("class"))) {
					factories.put(factory.getAttribute("name"),
						implementation.getAttribute("tool-class"));
				}
			}
		}
		return factories;
	}

	/** Every factory name this configuration declares, however the factory builds its tools. */
	private static Set<String> declaredFactories(Document config) {
		Set<String> names = new TreeSet<>();
		for (Element factory : factoryElements(config)) {
			names.add(factory.getAttribute("name"));
		}
		return names;
	}

	private static List<Element> factoryElements(Document config) {
		List<Element> factories = new ArrayList<>();
		NodeList candidates = config.getElementsByTagName("factory");
		for (int n = 0; n < candidates.getLength(); n++) {
			factories.add((Element) candidates.item(n));
		}
		return factories;
	}

	/** Every {@code <tool name="..."/>} an agent or a skill lists. */
	private static Set<String> toolReferences(Document config) {
		Set<String> references = new TreeSet<>();
		NodeList candidates = config.getElementsByTagName("tool");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element candidate = (Element) candidates.item(n);
			if ("tools".equals(((Element) candidate.getParentNode()).getTagName())) {
				references.add(candidate.getAttribute("name"));
			}
		}
		return references;
	}

	private static List<Element> children(Element parent, String tagName) {
		List<Element> children = new ArrayList<>();
		NodeList nodes = parent.getChildNodes();
		for (int n = 0; n < nodes.getLength(); n++) {
			Node node = nodes.item(n);
			if (node instanceof Element child && tagName.equals(child.getTagName())) {
				children.add(child);
			}
		}
		return children;
	}

	private static Document config() throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(configFile().toFile());
	}

	/**
	 * Locates the shipped configuration by walking up from the working directory.
	 *
	 * @see TestFrameworkSkillReferences
	 */
	private static Path configFile() {
		Path candidate = Paths.get("").toAbsolutePath();
		while (candidate != null) {
			Path config = candidate.resolve(CONFIG_FILE);
			if (Files.isRegularFile(config)) {
				return config;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("Cannot find '" + CONFIG_FILE + "' from "
			+ Paths.get("").toAbsolutePath() + ".");
	}
}
