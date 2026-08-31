/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
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
 *     <instance>
 *         <tool-groups>
 *             <group name="egress"
 *                 tools="mcp__jira__createIssue, mcp__jira__addComment"
 *             />
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
 * There is no switch that turns this off. An application that writes no policy at all does not get an
 * unchecked agent; it gets one whose principal holds RoleToolAccess.DEFAULT_BASELINE - the read-only
 * default of #293 - and nothing else. See doc/security/tool-access-configuration.md.
 *
 * This is static application configuration, deliberately shaped like the engine's <security-config>
 * (ticket #29088) so administrators meet one idiom rather than two.
 */

/**
 * Which security roles may use which agent tools.
 *
 * <p>
 * Nothing is allowed that is not granted here: a role that is not listed may use no tool at all, and
 * so may a user who holds no listed role. This is always applied - there is no setting that switches
 * it off. An application that lists nothing leaves its agents with the read-only capabilities below.
 * </p>
 *
 * @see RoleToolAccess
 * @see ToolGroups
 */
public interface ToolAccessPolicy extends ConfigurationItem {

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
	 * A role that is not listed here may use no tool. An empty list therefore leaves every user with
	 * nothing but what {@link #getBaseline()} grants.
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
	 * Conversing with the user and reporting the agent's own toolset need no entry - both are granted
	 * anyway. Write nothing and everyone may also read the model and the business data. Write this
	 * element empty to grant nothing further.
	 * </p>
	 *
	 * @see RoleToolAccess#DEFAULT_BASELINE
	 */
	/*
	 * The two capabilities that need no entry are RoleToolAccess.IMPLICIT_BASELINE: "interaction" -
	 * asking a question, loading a skill, delegating - and "introspection" - listTools, the agent
	 * saying which tools and skills it carries. Both reach every role and every run, unattended ones
	 * included; an explicit <deny> still wins over either.
	 *
	 * An item rather than a plain list, and nullable, for the same reason getBackgroundAllowlist() is:
	 * three states have to stay distinguishable.
	 *
	 *   absent  -> RoleToolAccess.DEFAULT_BASELINE, the read-only default an unconfigured application
	 *              gets so that it looks unconfigured rather than broken (#293).
	 *   <baseline/>  -> nothing beyond RoleToolAccess.IMPLICIT_BASELINE. An explicit "no extra grants",
	 *              which the secdemo fixture depends on to keep demonstrating deny-by-default.
	 *   <baseline>..</baseline> -> exactly what is listed, replacing the default entirely.
	 *
	 * Reading an *empty* list as "absent" would make the middle state unsayable, and the default would
	 * then silently widen a policy that had explicitly asked for nothing - the failure mode this layer
	 * refuses to have. A list property cannot express it either way: the engine rejects a nullable
	 * LIST/MAP/ARRAY property outright ("Properties of kind 'LIST', 'MAP', and 'ARRAY' cannot be
	 * nullable"), which is exactly why the background allowlist wraps its grants in an item. This does
	 * the same, and @DefaultContainer keeps the XML an administrator writes unchanged - the <grant>
	 * elements still sit directly inside <baseline>.
	 *
	 * The default is deliberately NOT shipped as XML in tl-ai-core.conf.config.xml. A keyless
	 * configuration list composes additively across configuration files (the engine's
	 * SimpleListStorage seeds itself from the base value and appends), so a shipped <baseline> would be
	 * unioned with an application's own instead of replaced by it - and an application asking for one
	 * capability would silently also get these two. Resolving the default in Java keeps
	 * "an application that writes this element replaces it" exactly true.
	 */
	@Name(BASELINE)
	@Nullable
	Baseline getBaseline();

	/**
	 * Setter for {@link #getBaseline()}.
	 */
	void setBaseline(Baseline value);

	/**
	 * The capabilities granted to every user, whatever their roles.
	 *
	 * <p>
	 * An entry without any capability is legal and grants nothing - which is how an application says
	 * that no capability at all is given to everyone.
	 * </p>
	 */
	interface Baseline extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getGrants()}.
		 *
		 * @see #getGrants()
		 */
		String GRANTS = "grants";

		/**
		 * The capabilities and single tools every user may use.
		 */
		@Name(GRANTS)
		@EntryTag("grant")
		@DefaultContainer
		List<Grant> getGrants();
	}

	/**
	 * What an agent run with nobody to ask may use: a scheduled or asynchronous run, or a request from
	 * an external client.
	 *
	 * <p>
	 * Such a run confirms every tool call itself, so this is its upper limit; the acting user's roles
	 * apply on top. An application that lists any role above and leaves this out does not start.
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
	 * beyond conversing with the user and reporting the agent's own toolset.
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
		 * @implNote Redeclared from {@link NamedConfigMandatory} only to carry the option
		 *           annotations; the property itself is inherited. The editor then offers the known
		 *           groups and explains each of them, while the stored value stays the technical
		 *           group name, so a hand-written policy parses unchanged.
		 *
		 * @see AllToolGroups
		 */
		@Override
		@Options(fun = AllToolGroups.class)
		@OptionLabels(ToolGroupResources.class)
		String getName();

		/**
		 * The tools belonging to this group, by their qualified name.
		 *
		 * <p>
		 * Pick every tool that belongs here. A tool that is not listed keeps the group it declares
		 * itself, and has none at all if it declares none.
		 * </p>
		 *
		 * @see AllToolNames
		 */
		@Name(TOOLS)
		@Format(CommaSeparatedStrings.class)
		@Options(fun = AllToolNames.class)
		@OptionLabels(ToolNameResources.class)
		List<String> getTools();

		/**
		 * Setter for {@link #getTools()}.
		 */
		void setTools(List<String> value);
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
		 * The security role this entry is about.
		 *
		 * @implNote Redeclared from {@link NamedConfigMandatory} only to carry the option
		 *           annotation; the property itself is inherited. The editor then offers the roles
		 *           of the application instead of a text field, while the stored value stays the
		 *           role name, so a hand-written policy parses unchanged.
		 *
		 * @see AllSecurityRoles
		 */
		@Override
		@Options(fun = AllSecurityRoles.class)
		String getName();

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
	 * One entry of a list of permissions or refusals: either a whole capability group, or named
	 * single tools.
	 */
	interface Grant extends ConfigurationItem {

		/**
		 * Configuration property name for {@link #getGroup()}.
		 *
		 * @see #getGroup()
		 */
		String GROUP = "group";

		/**
		 * Configuration property name for {@link #getTools()}.
		 *
		 * @see #getTools()
		 */
		String TOOLS = "tools";

		/**
		 * The capability group this entry refers to.
		 *
		 * <p>
		 * Leave this empty if the entry names single tools instead.
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
		 * The single tools this entry refers to, by their qualified name.
		 *
		 * <p>
		 * Pick one or several. Leave this empty if the entry names a capability group instead: an
		 * entry says either a group or tools, never both.
		 * </p>
		 *
		 * @see AllToolNames
		 */
		@Name(TOOLS)
		@Format(CommaSeparatedStrings.class)
		@Options(fun = AllToolNames.class)
		@OptionLabels(ToolNameResources.class)
		List<String> getTools();

		/**
		 * Setter for {@link #getTools()}.
		 */
		void setTools(List<String> value);
	}
}
