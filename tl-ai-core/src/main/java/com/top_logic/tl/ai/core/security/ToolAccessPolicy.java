/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/*
 * Every JavaDoc comment in this file - the class comments included - is exported to
 * META-INF/messages_en.properties by the build's doclet and shown as the label and tooltip of the
 * corresponding field in the service editor. Its audience is therefore an administrator configuring
 * the application, not a developer reading the source: short sentences saying what a setting does and
 * what happens when it is left out, no Java identifiers, no design rationale. That rationale is
 * valuable, so it lives in comments like this one, which the doclet does not export.
 *
 * The policy an administrator writes, in full:
 *
 * <config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
 *     <instance security-mode="ENFORCED">
 *         <tool-groups>
 *             <group name="egress">
 *                 <tools>
 *                     <tool name="mcp__jira__*"/>
 *                 </tools>
 *             </group>
 *         </tool-groups>
 *         <tool-access>
 *             <role name="SalesAssistantUser">
 *                 <grants>
 *                     <grant group="model.read"/>
 *                     <grant group="data.read"/>
 *                 </grants>
 *                 <denies>
 *                     <deny group="script"/>
 *                 </denies>
 *             </role>
 *         </tool-access>
 *         <baseline>
 *             <grant group="model.read"/>
 *         </baseline>
 *         <background-allowlist>
 *             <grant group="data.read"/>
 *         </background-allowlist>
 *     </instance>
 * </config>
 *
 * Deny-by-default: a role with no entry, and a tool in no granted group, are both denied - the one
 * exception being RoleToolAccess.IMPLICIT_BASELINE. Grants of all roles a user holds are unioned; an
 * explicit deny from any held role wins over every grant, so an administrator can carve a privileged
 * group out of an otherwise broad grant without re-enumerating.
 *
 * This is static application configuration, deliberately shaped like the engine's <security-config>
 * (ticket #29088) so administrators meet one idiom rather than two.
 */

/**
 * Which security roles may use which agent tools.
 *
 * <p>
 * Nothing is allowed that is not granted here: a role that is not listed may use no tool at all. As
 * long as no role is listed, the policy is not enforced and every tool an agent carries stays usable
 * by everyone who may use that agent.
 * </p>
 *
 * @see RoleToolAccess
 * @see ToolGroups
 */
public interface ToolAccessPolicy extends ConfigurationItem {

	/**
	 * Configuration property name for {@link #getSecurityMode()}.
	 *
	 * @see #getSecurityMode()
	 */
	String SECURITY_MODE = "security-mode";

	/**
	 * Configuration property name for {@link #getToolGroups()}.
	 *
	 * @see #getToolGroups()
	 */
	String TOOL_GROUPS = "tool-groups";

	/**
	 * Configuration property name for {@link #getToolAccess()}.
	 *
	 * @see #getToolAccess()
	 */
	String TOOL_ACCESS = "tool-access";

	/**
	 * Configuration property name for {@link #getBaseline()}.
	 *
	 * @see #getBaseline()
	 */
	String BASELINE = "baseline";

	/**
	 * Configuration property name for {@link #getBackgroundAllowlist()}.
	 *
	 * @see #getBackgroundAllowlist()
	 */
	String BACKGROUND_ALLOWLIST = "background-allowlist";

	/**
	 * How strictly this policy is applied.
	 *
	 * <p>
	 * With {@link AiSecurityMode#ENFORCED} every tool call is checked against the roles of the acting
	 * user; with {@link AiSecurityMode#LEGACY} nothing is checked. If no mode is chosen, the policy is
	 * enforced as soon as at least one role is configured below.
	 * </p>
	 */
	/*
	 * Nullable with a null default, both needed: an unconfigured mode must stay distinguishable from an
	 * explicitly configured LEGACY. The latter says "do not enforce even though I wrote a matrix"; the
	 * former leaves the decision to AiToolAccessService.resolveCeiling(ToolAccessPolicy), which reads
	 * the presence of a <tool-access> matrix as the opt-in signal. Without both annotations an enum
	 * property defaults to its first constant and the distinction would be unavailable.
	 */
	@Name(SECURITY_MODE)
	@Nullable
	@NullDefault
	AiSecurityMode getSecurityMode();

	/**
	 * Setter for {@link #getSecurityMode()}.
	 */
	void setSecurityMode(AiSecurityMode value);

	/**
	 * Assigns tools to a capability group when the tool does not state its own.
	 *
	 * <p>
	 * Needed for tools that appear only while the application runs: those of a connected MCP server,
	 * and TL-Script tools. Such a tool is usable by nobody until it is listed here, and an entry here
	 * also overrides the group a tool states itself.
	 * </p>
	 */
	@Name(TOOL_GROUPS)
	@Key(GroupMembership.NAME_ATTRIBUTE)
	@EntryTag("group")
	Map<String, GroupMembership> getToolGroups();

	/**
	 * The roles, and what each of them may use.
	 *
	 * <p>
	 * A role that is not listed here may use no tool. As long as this list is empty, the policy is not
	 * enforced at all.
	 * </p>
	 */
	@Name(TOOL_ACCESS)
	@Key(RoleAccess.NAME_ATTRIBUTE)
	@EntryTag("role")
	Map<String, RoleAccess> getToolAccess();

	/**
	 * Capabilities every logged-in user gets in addition to the ones their roles grant.
	 *
	 * <p>
	 * Asking the user a question, loading a skill and delegating to a sub-agent are granted anyway and
	 * need no entry here. Leave this empty unless you want a further capability for everyone.
	 * </p>
	 */
	@Name(BASELINE)
	@EntryTag("grant")
	List<Grant> getBaseline();

	/**
	 * What an agent run with nobody to ask may use: a scheduled or asynchronous run, or a request from
	 * an external client.
	 *
	 * <p>
	 * Such a run confirms every tool call itself, so this is its upper limit; the acting user's roles
	 * apply on top. Once the policy is enforced, an application leaving this out does not start.
	 * </p>
	 *
	 * @see BackgroundAllowlist
	 */
	/*
	 * Nullable so that "the administrator wrote nothing" stays distinguishable from every declaration
	 * that means something. The allowlist is the whole compensating control for the approval prompt an
	 * unattended run cannot show, so its absent state must not be the permissive one - see
	 * AiToolAccessService.checkBackgroundAllowlistDeclared and RoleToolAccess.allowedInBackground,
	 * which are the boot-time and decision-time halves of that rule.
	 */
	@Name(BACKGROUND_ALLOWLIST)
	@Nullable
	BackgroundAllowlist getBackgroundAllowlist();

	/**
	 * Setter for {@link #getBackgroundAllowlist()}.
	 */
	void setBackgroundAllowlist(BackgroundAllowlist value);

	/**
	 * The upper limit for agent runs that have nobody to confirm a tool call.
	 *
	 * <p>
	 * Either a list of capabilities, or {@link #isAll()} to accept such runs with no limit of their
	 * own. An entry without any capability is the strictest choice: those runs then reach nothing
	 * except asking a question.
	 * </p>
	 */
	interface BackgroundAllowlist extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #isAll()}.
		 *
		 * @see #isAll()
		 */
		String ALL = "all";

		/**
		 * Configuration property name for {@link #getGrants()}.
		 *
		 * @see #getGrants()
		 */
		String GRANTS = "grants";

		/**
		 * Lets a run with nobody to ask use everything the acting user's roles allow.
		 *
		 * <p>
		 * Switch this on only if you deliberately want such runs without a limit of their own. It gives
		 * up the only safeguard that stands in for the confirmation nobody is there to give.
		 * </p>
		 */
		@Name(ALL)
		boolean isAll();

		/**
		 * Setter for {@link #isAll()}.
		 */
		void setAll(boolean value);

		/**
		 * The capabilities and single tools a run with nobody to ask may use.
		 *
		 * <p>
		 * Ignored when {@link #isAll()} is switched on.
		 * </p>
		 */
		@Name(GRANTS)
		@EntryTag("grant")
		@DefaultContainer
		List<Grant> getGrants();
	}

	/**
	 * One capability group and the tools belonging to it.
	 *
	 * <p>
	 * For tools that do not state a group themselves.
	 * </p>
	 */
	interface GroupMembership extends NamedConfigMandatory {

		/**
		 * Configuration property name for {@link #getTools()}.
		 *
		 * @see #getTools()
		 */
		String TOOLS = "tools";

		/**
		 * The capability group the tools listed here belong to.
		 *
		 * @see AllToolGroups
		 */
		/*
		 * Redeclared from NamedConfigMandatory only to carry the option annotations; the property
		 * itself is inherited. The editor then offers the known groups and explains each of them,
		 * while the stored value stays the technical group name, so a hand-written policy parses
		 * unchanged.
		 */
		@Override
		@Options(fun = AllToolGroups.class)
		@OptionLabels(ToolGroupResources.class)
		String getName();

		/**
		 * The tools belonging to this group, by their qualified name.
		 *
		 * <p>
		 * A name ending in {@code *} covers every tool whose name starts with it, which classifies a
		 * whole MCP server or a whole tool factory in one line.
		 * </p>
		 */
		@Name(TOOLS)
		@Key(ToolRef.NAME_ATTRIBUTE)
		@EntryTag("tool")
		Map<String, ToolRef> getTools();
	}

	/**
	 * The qualified name of a tool.
	 *
	 * <p>
	 * A name ending in {@code *} covers every tool whose name starts with it.
	 * </p>
	 */
	interface ToolRef extends NamedConfigMandatory {
		// Marker: the name carries the reference.
	}

	/**
	 * What one security role may and may not use.
	 */
	interface RoleAccess extends NamedConfigMandatory {

		/**
		 * Configuration property name for {@link #getGrants()}.
		 *
		 * @see #getGrants()
		 */
		String GRANTS = "grants";

		/**
		 * Configuration property name for {@link #getDenies()}.
		 *
		 * @see #getDenies()
		 */
		String DENIES = "denies";

		/**
		 * The capabilities and single tools this role may use.
		 */
		@Name(GRANTS)
		@EntryTag("grant")
		List<Grant> getGrants();

		/**
		 * The capabilities and single tools this role may never use.
		 *
		 * <p>
		 * A refusal here always wins - over what this role is granted, and over what every other role
		 * of the same user grants.
		 * </p>
		 */
		@Name(DENIES)
		@EntryTag("deny")
		List<Grant> getDenies();
	}

	/**
	 * One entry of a list of permissions or refusals: either a whole capability group or a single
	 * tool.
	 */
	interface Grant extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getGroup()}.
		 *
		 * @see #getGroup()
		 */
		String GROUP = "group";

		/**
		 * Configuration property name for {@link #getTool()}.
		 *
		 * @see #getTool()
		 */
		String TOOL = "tool";

		/**
		 * The capability group this entry refers to.
		 *
		 * <p>
		 * Leave this empty if the entry names a single tool instead.
		 * </p>
		 *
		 * @see ToolGroups
		 * @see AllToolGroups
		 */
		/*
		 * The editor offers the known groups and explains each of them, while the stored value stays
		 * the technical group name, so a hand-written policy parses unchanged.
		 */
		@Name(GROUP)
		@Options(fun = AllToolGroups.class)
		@OptionLabels(ToolGroupResources.class)
		String getGroup();

		/**
		 * Setter for {@link #getGroup()}.
		 */
		void setGroup(String value);

		/**
		 * The qualified name of the single tool this entry refers to.
		 *
		 * <p>
		 * Leave this empty if the entry names a capability group instead. A name ending in {@code *}
		 * covers every tool whose name starts with it, but never reaches a privileged group - those
		 * tools have to be named in full.
		 * </p>
		 */
		/*
		 * Free text on purpose, unlike getGroup(): mcp__jira__* is not among any list of registered
		 * tool names, and it is the form most entries here take.
		 */
		@Name(TOOL)
		String getTool();

		/**
		 * Setter for {@link #getTool()}.
		 */
		void setTool(String value);
	}
}
