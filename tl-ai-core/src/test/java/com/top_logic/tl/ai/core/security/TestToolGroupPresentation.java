/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.Collection;
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
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * How the choices of the policy editor present themselves: tool groups, roles and tools.
 *
 * <p>
 * The point of this class is that the presentation cannot fall behind the taxonomy: a group added to
 * {@link ToolGroups#ALL} without a resource would show up in the policy editor as a bare technical
 * name with nothing explaining it, which is the state this whole mechanism exists to end. The same
 * holds for the properties themselves - a policy field that lost its {@link Options} annotation would
 * quietly become a text field again, and nothing but a test says otherwise.
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

	/** A role is picked from the application's roles, not typed. */
	public void testRoleIsAChoice() {
		Options options = property(RoleAccess.class, RoleAccess.NAME_ATTRIBUTE).getAnnotation(Options.class);

		assertNotNull("A misspelled role name reaches nobody, so it must not be typeable.", options);
		assertEquals(AllSecurityRoles.class, options.fun());
	}

	/**
	 * A role the policy already names stays selectable even when the application has no such role.
	 *
	 * <p>
	 * That is the state an aspect which is currently switched off produces, and the state of a policy
	 * written ahead of the role. Offering the existing roles alone would let an open-and-save of the
	 * editor drop the entry, silently widening or narrowing access - which is what the boot-time check
	 * on unknown role names exists to make loud.
	 * </p>
	 */
	public void testConfiguredRoleStaysAmongTheOptions() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName("RoleCreatedByAnAspectThatIsOff");
		policy.getToolAccess().put(role.getName(), role);

		assertEquals(List.of("Existing", "RoleCreatedByAnAspectThatIsOff"),
			AllSecurityRoles.union(List.of("Existing"), policy));
	}

	/** Without a role store and without a policy there is nothing to offer, and nothing breaks. */
	public void testRoleOptionsSurviveWithoutARoleStore() {
		assertEquals(List.of(), new AllSecurityRoles().apply());
	}

	/** A grant's tools are picked from the registered ones, and each explains itself. */
	public void testGrantToolsAreAnExplainedChoice() {
		assertToolOptions(property(Grant.class, Grant.TOOLS));
	}

	/** So are the tools a {@code <tool-groups>} entry classifies. */
	public void testGroupMembershipToolsAreAnExplainedChoice() {
		assertToolOptions(property(GroupMembership.class, GroupMembership.TOOLS));
	}

	/**
	 * A tool the policy already names stays selectable even when nothing can call it.
	 *
	 * <p>
	 * The state of an MCP server that is offline right now, and of an entry written ahead of its tool.
	 * Both are legal, both keep working, and neither may be dropped by an open-and-save of the editor.
	 * </p>
	 */
	public void testConfiguredToolStaysAmongTheOptions() {
		ToolAccessPolicy policy = TypedConfiguration.newConfigItem(ToolAccessPolicy.class);
		GroupMembership egress = TypedConfiguration.newConfigItem(GroupMembership.class);
		egress.setName(ToolGroups.EGRESS);
		egress.setTools(List.of("mcp__offline__doThing"));
		policy.getToolGroups().put(egress.getName(), egress);

		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName("Clerk");
		Grant grant = TypedConfiguration.newConfigItem(Grant.class);
		grant.setTools(List.of("not-yet-written.aTool"));
		role.getGrants().add(grant);
		policy.getToolAccess().put(role.getName(), role);

		assertEquals(List.of("mcp__offline__doThing", "not-yet-written.aTool", "registry.aTool"),
			AllToolNames.union(List.of("registry.aTool"), policy));
	}

	/** A tool nobody registered keeps its name and explains nothing it cannot. */
	public void testUnregisteredToolKeepsItsName() {
		ToolNameResources resources = new ToolNameResources();

		assertEquals("mcp__offline__doThing", resources.getLabel("mcp__offline__doThing"));
		assertNull(resources.getTooltip("mcp__offline__doThing"));
	}

	/**
	 * A tool property offers the registered tools, explains each of them, and takes several at once.
	 *
	 * <p>
	 * A collection type is what makes the editor render a selection popup rather than a single choice
	 * ({@code ValueEditor} asks exactly this question), so it is the thing to assert - the popup itself
	 * is not reachable from a unit test.
	 * </p>
	 */
	private static void assertToolOptions(PropertyDescriptor property) {
		assertTrue("Property '" + property.getPropertyName() + "' must accept several tools, so that the"
			+ " editor offers a selection rather than one choice; its type is " + property.getType(),
			Collection.class.isAssignableFrom(property.getType()));

		Options options = property.getAnnotation(Options.class);
		assertNotNull("Property '" + property.getPropertyName() + "' must offer options.", options);
		assertEquals(AllToolNames.class, options.fun());

		OptionLabels labels = property.getAnnotation(OptionLabels.class);
		assertNotNull("Property '" + property.getPropertyName() + "' must explain its options.", labels);
		assertEquals(ToolNameResources.class, labels.value());
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
