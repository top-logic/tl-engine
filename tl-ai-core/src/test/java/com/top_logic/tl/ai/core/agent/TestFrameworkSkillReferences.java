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

/**
 * Guards that no framework skill sends the agent to a skill it may not be able to load.
 *
 * <p>
 * A skill description is routing instruction: it is in the system prompt of every agent that declares
 * the skill, and it is what the model matches a request against. Several of them deliberately point at
 * another skill - "to change data with a script, load {@code TL_writingDataChanges}" - because agents used to load the
 * wrong one. The pointer becomes a <b>dead end</b> as soon as the acting user cannot load the skill it
 * names: the referring skill is offered, the referenced one is gone, and the model was observed
 * concluding from that that a capability nobody had mentioned was missing.
 * </p>
 *
 * <p>
 * Two ways out, and which one a skill takes is a design decision this test only checks the result of:
 * </p>
 * <ul>
 * <li><b>Carry the tool.</b> A guidance-only skill that depends on a tool declares that tool, and the
 * skill filter then drops the pair together - there is no dead end because there is no surviving
 * referrer. {@code TL_tlScriptInstanceCreation} is that case: it carries {@code changeData}.</li>
 * <li><b>Qualify the reference.</b> Where the referenced skill belongs to a different domain and
 * carrying its tools would drop the referrer for users who need it - {@code TL_agenticAuthoring}
 * pointing at {@code TL_writingDataChanges} - the description says what to do when the reference is not
 * reachable. That is what {@link #QUALIFICATION} looks for.</li>
 * </ul>
 *
 * <p>
 * The file is parsed rather than the booted definitions queried, because what is under test is the
 * shipped framework configuration and nothing an application adds to it.
 * </p>
 *
 * @see AgentCapabilities#skillAbsenceReason(String, java.util.Collection)
 */
public class TestFrameworkSkillReferences extends TestCase {

	/** Relative to the repository root. */
	private static final String CONFIG_FILE =
		"tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.conf.config.xml";

	/**
	 * What a description naming another skill has to say about that skill being out of reach.
	 *
	 * <p>
	 * Deliberately one fragment rather than a full sentence: the sentence around it is prompt text and
	 * belongs to whoever writes the skill. What may not be omitted is telling the model that the
	 * reference can be unreachable, because a model that does not expect that explains it.
	 * </p>
	 */
	private static final String QUALIFICATION = "not available to you";

	/** Every {@code TL_}-prefixed name a description could be naming. */
	private static final Pattern REFERENCE = Pattern.compile("TL_[A-Za-z]\\w*");

	/**
	 * A description that names another skill either qualifies the reference or has no business naming
	 * it.
	 */
	public void testEveryCrossReferenceIsQualified() throws Exception {
		Document config = config();
		Map<String, String> skills = skillDescriptions(config);
		Set<String> agents = agentNames(config);

		List<String> failures = new ArrayList<>();
		int qualified = 0;
		for (Map.Entry<String, String> skill : skills.entrySet()) {
			String name = skill.getKey();
			String description = skill.getValue();
			for (String referenced : referencesIn(description)) {
				if (referenced.equals(name) || agents.contains(referenced)) {
					// Its own name, or a delegate - a delegate the user cannot reach is filtered and
					// annotated one level up, in the generated delegate catalog.
					continue;
				}
				if (!skills.containsKey(referenced)) {
					failures.add(name + " names '" + referenced + "', which is neither a skill nor an"
						+ " agent of this configuration.");
					continue;
				}
				if (!description.contains(QUALIFICATION)) {
					failures.add(name + " points at '" + referenced + "' without saying what to do"
						+ " when that skill is not reachable. Either let " + name + " carry the tool it"
						+ " depends on - then both are dropped together - or add the qualification"
						+ " ('... " + QUALIFICATION + " ...').");
					continue;
				}
				qualified++;
			}
		}

		assertTrue(String.join("\n", failures), failures.isEmpty());
		assertTrue("The framework deliberately routes between skills; a run that found no"
			+ " cross-reference at all is not testing the rule.", qualified > 0);
	}

	/**
	 * {@code TL_tlScriptInstanceCreation} carries the tool its guidance is about.
	 *
	 * <p>
	 * The structural half of the rule above, pinned by name because it is the case that produced the
	 * defect: a skill with no tools always survives the skill filter, so a guidance-only skill whose
	 * guidance needs {@code script} was offered to users without {@code script} and pointed them at the
	 * skill that has {@code changeData} - which those users could not load either. With the tool
	 * declared, the filter drops the whole skill for them, and a user who does hold {@code script} gets
	 * the tool the guidance tells them to run.
	 * </p>
	 */
	public void testInstanceCreationCarriesTheToolItsGuidanceRuns() throws Exception {
		assertEquals("Its workflow ends in running script-exec.changeData.",
			List.of("script-exec.changeData"),
			skillTools(config(), "TL_tlScriptInstanceCreation"));
	}

	/** The positive control: the file really was read and really does define the framework skills. */
	public void testTheConfigurationWasRead() throws Exception {
		Map<String, String> skills = skillDescriptions(config());

		assertTrue("Expected the shipped framework skills, found: " + skills.keySet(),
			skills.containsKey("TL_agenticAuthoring") && skills.containsKey("TL_writingDataChanges"));
		for (Map.Entry<String, String> skill : skills.entrySet()) {
			assertFalse("Every framework skill needs a description - it is the only thing the model"
				+ " matches a request against: " + skill.getKey(), skill.getValue().isEmpty());
		}
	}

	// ===== Reading the configuration =====

	/** The declared skills of the framework configuration, by name, with their descriptions. */
	private static Map<String, String> skillDescriptions(Document config) {
		Map<String, String> skills = new LinkedHashMap<>();
		for (Element definition : skillDefinitions(config)) {
			skills.put(definition.getAttribute("name"), definition.getAttribute("description"));
		}
		return skills;
	}

	/** The tool names the named skill declares. */
	private static List<String> skillTools(Document config, String skillName) {
		List<String> tools = new ArrayList<>();
		for (Element definition : skillDefinitions(config)) {
			if (!skillName.equals(definition.getAttribute("name"))) {
				continue;
			}
			for (Element tool : children(definition, "tools")) {
				for (Element entry : children(tool, "tool")) {
					tools.add(entry.getAttribute("name"));
				}
			}
		}
		return tools;
	}

	/**
	 * The {@code <skill>} elements that define a skill.
	 *
	 * <p>
	 * An agent's {@code <skills>} list uses the same element name to <em>reference</em> one. A
	 * definition is told from a reference by its {@code <content>}, which is the skill's whole point.
	 * </p>
	 */
	private static List<Element> skillDefinitions(Document config) {
		List<Element> definitions = new ArrayList<>();
		NodeList candidates = config.getElementsByTagName("skill");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element candidate = (Element) candidates.item(n);
			if (!children(candidate, "content").isEmpty()) {
				definitions.add(candidate);
			}
		}
		return definitions;
	}

	/** The names of the agents the configuration defines. */
	private static Set<String> agentNames(Document config) {
		Set<String> names = new TreeSet<>();
		NodeList candidates = config.getElementsByTagName("agent");
		for (int n = 0; n < candidates.getLength(); n++) {
			Element candidate = (Element) candidates.item(n);
			if (!children(candidate, "system-prompt").isEmpty()) {
				names.add(candidate.getAttribute("name"));
			}
		}
		return names;
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

	/** The skill and agent names a description mentions, in order of appearance. */
	private static Set<String> referencesIn(String description) {
		Set<String> references = new TreeSet<>();
		Matcher matcher = REFERENCE.matcher(description);
		while (matcher.find()) {
			references.add(matcher.group());
		}
		return references;
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
	 * <p>
	 * The working directory of a surefire run is the module and of a reactor run the repository root,
	 * so searching upwards is what makes the test work either way - the same approach
	 * {@code SecDemoHarnessScriptsTest} takes to the harness scripts.
	 * </p>
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
		throw new AssertionError("Could not find '" + CONFIG_FILE + "' above the working directory "
			+ Paths.get("").toAbsolutePath() + ".");
	}
}
