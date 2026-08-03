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
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Administrator-authored mapping of security roles to the tools they may use.
 *
 * <p>
 * Deny-by-default: a role with no entry, and a tool in no granted group, are both denied. Grants of
 * all roles a user holds are unioned; an explicit deny from any held role wins over every grant, so
 * an administrator can carve a privileged group out of an otherwise broad grant without
 * re-enumerating.
 * </p>
 *
 * <p>
 * This is static application configuration, deliberately shaped like the engine's
 * {@code <security-config>} (ticket #29088) so administrators meet one idiom rather than two.
 * Example:
 * </p>
 *
 * <pre>
 * &lt;instance security-mode="ENFORCED"&gt;
 *     &lt;tool-groups&gt;
 *         &lt;group name="egress"&gt;
 *             &lt;tools&gt;
 *                 &lt;tool name="mcp__jira__*"/&gt;
 *             &lt;/tools&gt;
 *         &lt;/group&gt;
 *     &lt;/tool-groups&gt;
 *     &lt;tool-access&gt;
 *         &lt;role name="SalesAssistantUser"&gt;
 *             &lt;grants&gt;
 *                 &lt;grant group="model.read"/&gt;
 *                 &lt;grant group="data.read"/&gt;
 *             &lt;/grants&gt;
 *             &lt;denies&gt;
 *                 &lt;deny group="script"/&gt;
 *             &lt;/denies&gt;
 *         &lt;/role&gt;
 *     &lt;/tool-access&gt;
 *     &lt;baseline&gt;
 *         &lt;grant group="interaction"/&gt;
 *     &lt;/baseline&gt;
 *     &lt;background-allowlist&gt;
 *         &lt;grant group="data.read"/&gt;
 *     &lt;/background-allowlist&gt;
 * &lt;/instance&gt;
 * </pre>
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
	 * The deployment-wide enforcement mode.
	 *
	 * <p>
	 * <code>null</code> when the administrator named no mode, which is a different statement from
	 * naming {@link AiSecurityMode#LEGACY}: the latter says "do not enforce even though I wrote a
	 * matrix", the former leaves the decision to
	 * {@code AiToolAccessService.resolveCeiling(ToolAccessPolicy)}, which reads the presence of a
	 * {@code <tool-access>} matrix as the opt-in signal. Both {@link NullDefault} and {@link Nullable}
	 * are needed for that: without them an enum property defaults to its first constant, and the
	 * distinction would be unavailable.
	 * </p>
	 *
	 * @see AiSecurityMode#resolve(ToolAccessPolicy)
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
	 * Group membership for tools that cannot declare their own group.
	 *
	 * <p>
	 * Tools written in Java declare their group on {@code @ToolMethod}. Tools that arrive at
	 * runtime - MCP tools and TL-Script tools authored in the running application - have no
	 * annotation to carry it, so their group is assigned here. An entry here also overrides an
	 * annotated group, which lets an administrator move a tool into {@link ToolGroups#EGRESS} that
	 * its author did not classify as such.
	 * </p>
	 */
	@Name(TOOL_GROUPS)
	@Key(GroupMembership.NAME_ATTRIBUTE)
	@EntryTag("group")
	Map<String, GroupMembership> getToolGroups();

	/**
	 * The role-to-tool grants, indexed by role name.
	 *
	 * <p>
	 * A role that appears nowhere here is granted nothing beyond {@link #getBaseline()}.
	 * </p>
	 */
	@Name(TOOL_ACCESS)
	@Key(RoleAccess.NAME_ATTRIBUTE)
	@EntryTag("role")
	Map<String, RoleAccess> getToolAccess();

	/**
	 * Access granted to every authenticated user on top of their role grants.
	 *
	 * <p>
	 * Intentionally tiny - without {@link ToolGroups#INTERACTION} an agent cannot even ask the user
	 * a question.
	 * </p>
	 */
	@Name(BASELINE)
	@EntryTag("grant")
	List<Grant> getBaseline();

	/**
	 * The ceiling for runs that have no human available to answer an approval prompt.
	 *
	 * <p>
	 * Background runs auto-approve every tool call, which removes the compensating control that
	 * makes {@code DATA_WRITE} and {@code STRUCTURAL} tools safe. Their effective tool set is the
	 * intersection of the user's grants with this declaration.
	 * </p>
	 *
	 * <p>
	 * <code>null</code> when the administrator wrote no declaration at all, which under
	 * {@link AiSecurityMode#ENFORCED} is a <b>configuration error</b>, not a permissive default: the
	 * allowlist is the whole compensating control for the lost approval prompt, so its absence must
	 * not be the state that allows everything. An administrator who really wants unattended runs
	 * bounded only by the user's grants says so with
	 * {@code <background-allowlist all="true"/>}.
	 * </p>
	 *
	 * @see BackgroundAllowlist
	 */
	@Name(BACKGROUND_ALLOWLIST)
	@Nullable
	BackgroundAllowlist getBackgroundAllowlist();

	/**
	 * Setter for {@link #getBackgroundAllowlist()}.
	 */
	void setBackgroundAllowlist(BackgroundAllowlist value);

	/**
	 * What an unattended run may reach, on top of being bounded by the user's own grants.
	 *
	 * <p>
	 * Written either as an explicit list of capabilities:
	 * </p>
	 *
	 * <pre>
	 * &lt;background-allowlist&gt;
	 *     &lt;grant group="data.read"/&gt;
	 * &lt;/background-allowlist&gt;
	 * </pre>
	 *
	 * <p>
	 * or as the explicit opt-out, which accepts unattended runs bounded only by the acting user's
	 * grants:
	 * </p>
	 *
	 * <pre>
	 * &lt;background-allowlist all="true"/&gt;
	 * </pre>
	 *
	 * <p>
	 * An empty declaration ({@code <background-allowlist/>}) is the third meaningful state: no
	 * capability at all is reachable without a human, which is the strictest setting and a
	 * legitimate one.
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
		 * Whether unattended runs are bounded only by the acting user's grants.
		 *
		 * <p>
		 * The explicit opt-out from the background ceiling. It has to be said out loud because it
		 * gives up the only control that stands in for the missing approval prompt; nothing infers
		 * it from an omission.
		 * </p>
		 */
		@Name(ALL)
		boolean isAll();

		/**
		 * Setter for {@link #isAll()}.
		 */
		void setAll(boolean value);

		/**
		 * The groups and individual tools an unattended run may reach.
		 *
		 * <p>
		 * Ignored when {@link #isAll()} is set.
		 * </p>
		 */
		@Name(GRANTS)
		@EntryTag("grant")
		@DefaultContainer
		List<Grant> getGrants();
	}

	/**
	 * Assignment of tools to a group, for tools that carry no group declaration of their own.
	 */
	interface GroupMembership extends NamedConfigMandatory {

		/**
		 * Configuration property name for {@link #getTools()}.
		 *
		 * @see #getTools()
		 */
		String TOOLS = "tools";

		/**
		 * The qualified tool names belonging to this group.
		 *
		 * <p>
		 * A trailing {@code *} matches a prefix, which is how a whole MCP server is classified at
		 * once ({@code mcp__jira__*}).
		 * </p>
		 */
		@Name(TOOLS)
		@Key(ToolRef.NAME_ATTRIBUTE)
		@EntryTag("tool")
		Map<String, ToolRef> getTools();
	}

	/**
	 * A reference to a tool by its qualified name, optionally with a trailing {@code *} wildcard.
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
		 * The groups and individual tools this role may use.
		 */
		@Name(GRANTS)
		@EntryTag("grant")
		List<Grant> getGrants();

		/**
		 * The groups and individual tools this role may never use.
		 *
		 * <p>
		 * A deny wins over every grant, including grants from the user's other roles.
		 * </p>
		 */
		@Name(DENIES)
		@EntryTag("deny")
		List<Grant> getDenies();
	}

	/**
	 * One entry of a grant or deny list: either a whole group or a single tool.
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
		 * The tool group this entry refers to, or <code>null</code> when it refers to a single
		 * {@link #getTool() tool}.
		 *
		 * @see ToolGroups
		 */
		@Name(GROUP)
		String getGroup();

		/**
		 * Setter for {@link #getGroup()}.
		 */
		void setGroup(String value);

		/**
		 * The qualified name of the single tool this entry refers to, or <code>null</code> when it
		 * refers to a whole {@link #getGroup() group}.
		 *
		 * <p>
		 * A trailing {@code *} matches a prefix. A wildcard never reaches a tool in a
		 * {@link ToolGroups#PRIVILEGED privileged} group - those must be named in full.
		 * </p>
		 */
		@Name(TOOL)
		String getTool();

		/**
		 * Setter for {@link #getTool()}.
		 */
		void setTool(String value);
	}
}
