/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * How a tool group presents itself where an administrator has to choose one.
 *
 * <p>
 * The point of this class is that the presentation cannot fall behind the taxonomy: a group added to
 * {@link ToolGroups#ALL} without a resource would show up in the policy editor as a bare technical
 * name with nothing explaining it, which is the state this whole mechanism exists to end.
 * </p>
 */
public class TestToolGroupPresentation extends TestCase {

	/** Every group the framework ships is described - a new one cannot be forgotten silently. */
	public void testEveryFrameworkGroupIsDescribed() {
		for (String group : ToolGroups.ALL) {
			assertNotNull("The tool group '" + group + "' has no resource. Add one to"
				+ " com.top_logic.tl.ai.tool.api.I18NConstants and register it in ToolGroups.",
				ToolGroups.labelKey(group));
		}
	}

	/**
	 * Every group resolves to a label and to a sentence, in the language of the session.
	 *
	 * <p>
	 * The resources are generated from the javadoc of {@code ToolGroups}' I18N constants, so a
	 * declared constant with no text behind it would show up here as an unresolved key rather than as
	 * a compile error.
	 * </p>
	 */
	public void testEveryFrameworkGroupResolvesToTextAndASentence() {
		ToolGroupResources resources = new ToolGroupResources();

		for (String group : ToolGroups.ALL) {
			String label = resources.getLabel(group);
			assertNotNull(group, label);
			assertFalse("Unresolved label resource for '" + group + "': " + label,
				label.contains("["));
			assertTrue("The label must carry the technical name an operator reads in the log and in"
				+ " a denial message: " + label, label.contains(group));

			String tooltip = resources.getTooltip(group);
			assertNotNull("The group '" + group + "' must explain itself.", tooltip);
			assertFalse("Unresolved tooltip resource for '" + group + "': " + tooltip,
				tooltip.contains("["));
		}
	}

	/** A group nobody documented shows its technical name and explains nothing it cannot. */
	public void testUndocumentedGroupKeepsItsTechnicalName() {
		ToolGroupResources resources = new ToolGroupResources();

		assertEquals("mcp.jira", resources.getLabel("mcp.jira"));
		assertNull(resources.getTooltip("mcp.jira"));
		assertNull(ToolGroupResources.englishDescription("mcp.jira"));
	}

	/**
	 * The sentence the capability notice embeds is the same one, in the prompt's language.
	 *
	 * @see com.top_logic.tl.ai.core.agent.AgentCapabilities#notice
	 */
	public void testCapabilityNoticeGetsAnEnglishSentencePerGroup() {
		for (String group : ToolGroups.ALL) {
			String description = ToolGroupResources.englishDescription(group);

			assertNotNull("The notice must be able to say what '" + group + "' covers.", description);
			assertFalse("Unresolved resource for '" + group + "': " + description,
				description.contains("["));
		}
		assertTrue("agent.authoring is about authoring agents, not the data model.",
			ToolGroupResources.englishDescription(ToolGroups.AGENT_AUTHORING)
				.contains("agents, skills and TL-Script tools"));
	}

	/** A group nobody documented has no resource, rather than a made-up one. */
	public void testUnknownGroupHasNoResource() {
		assertNull(ToolGroups.labelKey("mcp.something.an.application.invented"));
		assertNull(ToolGroups.labelKey(null));
	}

	/** The editor offers at least the framework's own groups. */
	public void testOptionsCoverTheFrameworkGroups() {
		List<String> options = new AllToolGroups().apply();

		assertTrue(options + " must contain every framework group.",
			options.containsAll(ToolGroups.ALL));
	}

	/** A grant's group is a choice, not free text, and the choice is explained. */
	public void testGrantGroupIsAnExplainedChoice() {
		assertOptionsWithLabels(property(Grant.class, Grant.GROUP));
	}

	/** So is the group a {@code <tool-groups>} entry assigns tools to. */
	public void testToolGroupNameIsAnExplainedChoice() {
		assertOptionsWithLabels(property(GroupMembership.class, GroupMembership.NAME_ATTRIBUTE));
	}

	/**
	 * A tool reference stays free text: {@code secdemo-editing.*} is the whole point of it, and a
	 * wildcard is not among any list of registered names.
	 */
	public void testToolReferenceStaysFreeText() {
		assertNull("A closed list would make a wildcard unwritable.",
			property(Grant.class, Grant.TOOL).getAnnotation(Options.class));
	}

	private static void assertOptionsWithLabels(PropertyDescriptor property) {
		Options options = property.getAnnotation(Options.class);
		assertNotNull("Property '" + property.getPropertyName() + "' must offer options.", options);
		assertEquals(AllToolGroups.class, options.fun());

		OptionLabels labels = property.getAnnotation(OptionLabels.class);
		assertNotNull("Property '" + property.getPropertyName() + "' must explain its options.", labels);
		assertEquals(ToolGroupResources.class, labels.value());
	}

	private static PropertyDescriptor property(Class<?> configType, String name) {
		PropertyDescriptor property =
			TypedConfiguration.getConfigurationDescriptor(configType).getProperty(name);
		assertNotNull("No property '" + name + "' on " + configType.getSimpleName() + ".", property);
		return property;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestToolGroupPresentation.class)));
	}

}
