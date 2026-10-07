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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Pins the shape of {@code TL_init}, the skill with which Albert writes the application context
 * (#667).
 *
 * <p>
 * The skill is the counterpart of {@code /init} in Claude Code: it reads the model, interviews the
 * user and returns a Markdown document an administrator installs in a {@link TextPromptContributor}.
 * Two properties of that shape are what keeps it honest, and neither is visible from the prompt text
 * alone:
 * </p>
 * <ul>
 * <li>Nothing it can reach <b>changes the application</b>. Neither its own tools nor those of the
 * skills its instructions load may write data, change the model or act on another system; keeping
 * Albert's own task list is all the writing it does. A skill that is to describe the application must
 * not be a route to changing it.</li>
 * <li>Every skill its instructions send the model to is one <b>Albert carries</b>. An instruction to
 * load a skill the agent does not have is a dead end in the middle of the workflow.</li>
 * </ul>
 *
 * <p>
 * The file is parsed rather than the booted definitions queried, for the reason
 * {@link TestFrameworkSkillReferences} gives. That test also checks the qualification of the skill
 * names in the description; this one checks the names in the instructions.
 * </p>
 */
public class TestInitSkill extends TestCase {

	/** Relative to the repository root. */
	private static final String CONFIG_FILE =
		"tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.conf.config.xml";

	/** The skill under test. */
	private static final String INIT = "TL_init";

	/** Albert, the agent the skill is for. */
	private static final String ALBERT = "TL_SingleAgent";

	/** The factory that builds its tools by scanning a class for {@code @ToolMethod} methods. */
	private static final String REFLECTIVE =
		"com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory";

	/** Every {@code TL_}-prefixed name the instructions could be naming. */
	private static final Pattern REFERENCE = Pattern.compile("TL_[A-Za-z]\\w*");

	/** Albert lists the skill, so it is in Albert's catalog. */
	public void testAlbertCarriesTheSkill() throws Exception {
		Document config = config();
		initSkill(config);
		assertTrue(ALBERT + " does not list " + INIT + " among its skills.",
			agentSkills(config, ALBERT).contains(INIT));
	}

	/**
	 * Neither the skill nor a skill its instructions load has a tool that changes the application.
	 */
	public void testNothingTheSkillReachesChangesTheApplication() throws Exception {
		Document config = config();
		Map<String, String> factories = reflectiveFactories(config);
		Element init = initSkill(config);

		List<String> reachable = new ArrayList<>(skillTools(init));
		Set<String> named = namedSkills(init);
		for (String skill : named) {
			Element definition = skillDefinition(config, skill);
			assertNotNull(INIT + " names '" + skill + "', which is not a skill of this configuration.", definition);
			reachable.addAll(skillTools(definition));
		}
		assertFalse("A run that found no tool behind the skill is not testing the rule.", reachable.isEmpty());

		for (String reference : reachable) {
			int dot = reference.indexOf('.');
			String toolClass = factories.get(reference.substring(0, dot));
			assertNotNull("'" + reference + "' is not built by a reflective factory, so its mode"
				+ " cannot be checked here.", toolClass);
			ToolMode mode = toolOf(toolClass, reference.substring(dot + 1)).mode();
			assertTrue("'" + reference + "' is " + mode + ". " + INIT + " describes the application and"
				+ " must not reach a tool that changes it; reading and keeping the agent's own task list"
				+ " is all it may do.",
				mode == ToolMode.READ || mode == ToolMode.USER_SCOPED);
		}
	}

	/** Each skill the instructions send the model to is one Albert can load. */
	public void testEverySkillTheInstructionsNameIsAlbertsToo() throws Exception {
		Document config = config();
		Set<String> albertsSkills = agentSkills(config, ALBERT);

		Set<String> named = namedSkills(initSkill(config));
		assertTrue("The skill reads the model with TL_modelExploration; a run that found no skill name in"
			+ " it is not testing the rule.", named.contains("TL_modelExploration"));
		for (String skill : named) {
			assertTrue(INIT + " sends the model to '" + skill + "', which " + ALBERT + " does not carry.",
				albertsSkills.contains(skill));
		}
	}

	// ===== Reading the configuration =====

	/** The tool of the given class with the given name. */
	private static Tool toolOf(String toolClass, String name) throws ReflectiveOperationException {
		Object provider = Class.forName(toolClass).getDeclaredConstructor().newInstance();
		for (Tool tool : AnnotatedTools.from(provider)) {
			if (tool.name().equals(name)) {
				return tool;
			}
		}
		throw new AssertionError(toolClass + " offers no tool '" + name + "'.");
	}

	/** Factory name to tool class, for the factories that declare one. */
	private static Map<String, String> reflectiveFactories(Document config) {
		Map<String, String> factories = new LinkedHashMap<>();
		NodeList candidates = config.getElementsByTagName("factory");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element factory = (Element) candidates.item(n);
			for (Element implementation : children(factory, "implementation")) {
				if (REFLECTIVE.equals(implementation.getAttribute("class"))) {
					factories.put(factory.getAttribute("name"), implementation.getAttribute("tool-class"));
				}
			}
		}
		return factories;
	}

	/** The definition of the skill under test. */
	private static Element initSkill(Document config) {
		Element skill = skillDefinition(config, INIT);
		assertNotNull("The skill " + INIT + " is not defined.", skill);
		return skill;
	}

	/**
	 * The {@code <skill>} element that defines the named skill, or <code>null</code>.
	 *
	 * <p>
	 * An agent's {@code <skills>} list uses the same element name to <em>reference</em> one; a
	 * definition is told from a reference by its {@code <content>}.
	 * </p>
	 */
	private static Element skillDefinition(Document config, String name) {
		NodeList candidates = config.getElementsByTagName("skill");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element candidate = (Element) candidates.item(n);
			if (name.equals(candidate.getAttribute("name")) && !children(candidate, "content").isEmpty()) {
				return candidate;
			}
		}
		return null;
	}

	/** The tool names a skill definition declares. */
	private static List<String> skillTools(Element skill) {
		List<String> tools = new ArrayList<>();
		for (Element list : children(skill, "tools")) {
			for (Element tool : children(list, "tool")) {
				tools.add(tool.getAttribute("name"));
			}
		}
		return tools;
	}

	/** The skills the instructions of a skill definition name, other than itself. */
	private static Set<String> namedSkills(Element skill) {
		Set<String> named = referencesIn(content(skill));
		named.remove(skill.getAttribute("name"));
		return named;
	}

	/** The instructions of a skill definition. */
	private static String content(Element skill) {
		return children(skill, "content").get(0).getTextContent();
	}

	/** The skill names the named agent lists. */
	private static Set<String> agentSkills(Document config, String agentName) {
		Set<String> skills = new TreeSet<>();
		NodeList candidates = config.getElementsByTagName("agent");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element agent = (Element) candidates.item(n);
			if (!agentName.equals(agent.getAttribute("name")) || children(agent, "system-prompt").isEmpty()) {
				continue;
			}
			for (Element list : children(agent, "skills")) {
				for (Element skill : children(list, "skill")) {
					skills.add(skill.getAttribute("name"));
				}
			}
		}
		return skills;
	}

	/** The skill and agent names a text mentions. */
	private static Set<String> referencesIn(String text) {
		Set<String> references = new TreeSet<>();
		Matcher matcher = REFERENCE.matcher(text);
		while (matcher.find()) {
			references.add(matcher.group());
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
