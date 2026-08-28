/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.GroupMembership;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * The policy an administrator is editing, and the states the editor shows instead of raw entries.
 *
 * <p>
 * Every state below is a projection of the stored policy, never a second model of it: reading one
 * inspects the entries, setting one rewrites them. What is saved is therefore exactly what a policy
 * written by hand looks like, and opening the editor and saving it again changes nothing.
 * </p>
 */
/*
 * The draft is a copy of the service configuration itself rather than a parallel object graph, and that
 * is the load-bearing decision of this editor. The controls do not resemble the stored format at all -
 * a checkbox per capability, three named baseline states - and the honest way to build that without a
 * second format is to make every control a *question about* and an *edit of* the real configuration
 * item. A parallel model would need a converter in each direction, and the round-trip test would then
 * be testing the converter rather than the policy.
 *
 * Grants and denies are two lists, and the editor shows two lists. An earlier version offered one
 * three-way choice per capability (refused / unmentioned / granted), which reads as a scale and is not
 * one: "unmentioned" is the absence of an opinion, and another role of the same account or the basic
 * permissions can still grant the capability, while a deny beats both. A checkbox that says "granted"
 * and a separate, rarely used section that says "forbidden" is the shape the storage has, and the shape
 * a first-time reader gets right.
 *
 * Setting a state that is already there is a no-op everywhere, deliberately: the editor is opened on a
 * hand-written policy, and re-saving it must not reorder its lists.
 */
public class PolicyDraft {

	/**
	 * What every account gets in addition to what its roles grant.
	 */
	/*
	 * The three states of the <baseline> element, which the raw editor renders as "an empty box" and "a
	 * box with entries" and cannot distinguish "absent" from "empty" at all.
	 *
	 * The choice is held here rather than derived from the element on every read, because CUSTOM with
	 * nothing chosen yet and NOTHING are the same element. Deriving it made the "own list" button
	 * unclickable: it wrote an empty element, the next read called that NOTHING, and the form came back
	 * in the state the user had just left.
	 */
	public enum BaselineChoice {

		/** The shipped read-only default: every account may read the model and the data. */
		DEFAULT,

		/** Nothing. Only what a role grants, plus the two capabilities no policy can withhold. */
		NOTHING,

		/** Exactly the chosen capabilities, replacing the default entirely. */
		CUSTOM;
	}

	/**
	 * The upper limit for runs that have nobody to confirm a tool call.
	 */
	/*
	 * The same three states, plus the difference that the absent one is invalid here rather than a
	 * default: the allowlist is the compensating control for the approval prompt an unattended run cannot
	 * show, so silence must not be the permissive answer.
	 */
	public enum BackgroundChoice {

		/** Not stated. Invalid as soon as the policy names a role, and refused when applied. */
		UNDECLARED,

		/** No limit of their own: such runs reach whatever the acting account's roles grant. */
		ALL,

		/** Only the chosen capabilities and tools. */
		CUSTOM;
	}

	/**
	 * A starting point for a role that is being added to the policy.
	 */
	public enum RoleTemplate {

		/** Nothing granted. Every capability starts unset. */
		NONE(),

		/** Reads the model and the business data, changes nothing. */
		READ_ONLY(ToolGroups.MODEL_READ, ToolGroups.DATA_READ),

		/** Reads, and changes business data with the user confirming each call. */
		CLERK(ToolGroups.MODEL_READ, ToolGroups.DATA_READ, ToolGroups.DATA_WRITE),

		/** The clerk's capabilities plus running TL-Script. */
		DEVELOPER(ToolGroups.MODEL_READ, ToolGroups.DATA_READ, ToolGroups.DATA_WRITE,
			ToolGroups.SCRIPT);

		private final List<String> _groups;

		RoleTemplate(String... groups) {
			_groups = List.of(groups);
		}

		/** The capability groups this template grants. */
		public List<String> groups() {
			return _groups;
		}
	}

	private final AiToolAccessService.Config<?> _config;

	private final AiToolAccessService.Config<?> _saved;

	private BaselineChoice _baselineChoice;

	private BackgroundChoice _backgroundChoice;

	/**
	 * Creates a draft of the given policy.
	 *
	 * @param saved
	 *        The policy as the configuration files say it. Copied, so that editing the draft leaves the
	 *        application's own configuration item untouched until it is written.
	 */
	public PolicyDraft(AiToolAccessService.Config<?> saved) {
		_saved = saved;
		_config = TypedConfiguration.copy(saved);
		_baselineChoice = readBaselineChoice();
		_backgroundChoice = readBackgroundChoice();
	}

	/** The policy being edited, ready to be written as a service configuration. */
	public AiToolAccessService.Config<?> config() {
		return _config;
	}

	/** Whether the draft differs from the policy it was created from. */
	public boolean isModified() {
		return !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_saved, _config);
	}

	/** The roles the policy configures, in the order they are shown. */
	public List<String> roleNames() {
		return new ArrayList<>(new TreeSet<>(_config.getToolAccess().keySet()));
	}

	/** The entry of the named role, or <code>null</code> if the policy has none. */
	public RoleAccess role(String roleName) {
		return _config.getToolAccess().get(roleName);
	}

	/**
	 * Adds an entry for the named role, prefilled by the given template.
	 *
	 * @param roleName
	 *        The role to configure. An existing entry is left alone.
	 * @param template
	 *        The capabilities to grant right away. Never <code>null</code>.
	 */
	public void addRole(String roleName, RoleTemplate template) {
		if (roleName == null || roleName.isBlank() || _config.getToolAccess().containsKey(roleName)) {
			return;
		}
		RoleAccess role = TypedConfiguration.newConfigItem(RoleAccess.class);
		role.setName(roleName);
		for (String group : template.groups()) {
			role.getGrants().add(groupEntry(group));
		}
		_config.getToolAccess().put(roleName, role);
	}

	/** Removes the entry of the named role, so that it reaches nothing but the baseline. */
	public void removeRole(String roleName) {
		_config.getToolAccess().remove(roleName);
	}

	/**
	 * Whether the named role grants the given capability group.
	 *
	 * @param roleName
	 *        The role to ask about.
	 * @param group
	 *        The technical group name.
	 */
	public boolean isGranted(String roleName, String group) {
		RoleAccess role = role(roleName);
		return role != null && namesGroup(role.getGrants(), group);
	}

	/**
	 * Whether the named role forbids the given capability group.
	 *
	 * <p>
	 * A prohibition wins over every permission, including the ones of the other roles the account
	 * holds and the basic permissions.
	 * </p>
	 *
	 * @param roleName
	 *        The role to ask about.
	 * @param group
	 *        The technical group name.
	 */
	public boolean isDenied(String roleName, String group) {
		RoleAccess role = role(roleName);
		return role != null && namesGroup(role.getDenies(), group);
	}

	/**
	 * Grants or stops granting the given capability group to the named role.
	 *
	 * <p>
	 * Granting a capability the role forbids removes the prohibition: the two together mean the
	 * prohibition, so keeping both would leave a permission that says nothing. Entries naming single
	 * tools are left untouched, and setting the state that is already there changes nothing.
	 * </p>
	 *
	 * @param roleName
	 *        The role to change.
	 * @param group
	 *        The technical group name.
	 * @param granted
	 *        Whether the role should grant the group.
	 */
	public void setGranted(String roleName, String group, boolean granted) {
		RoleAccess role = role(roleName);
		if (role == null || isGranted(roleName, group) == granted) {
			return;
		}
		if (granted) {
			removeGroup(role.getDenies(), group);
			role.getGrants().add(groupEntry(group));
		} else {
			removeGroup(role.getGrants(), group);
		}
	}

	/**
	 * Forbids or stops forbidding the given capability group for the named role.
	 *
	 * <p>
	 * Forbidding a capability the role grants removes the permission, for the reason given at
	 * {@link #setGranted(String, String, boolean)}.
	 * </p>
	 *
	 * @param roleName
	 *        The role to change.
	 * @param group
	 *        The technical group name.
	 * @param denied
	 *        Whether the role should forbid the group.
	 */
	public void setDenied(String roleName, String group, boolean denied) {
		RoleAccess role = role(roleName);
		if (role == null || isDenied(roleName, group) == denied) {
			return;
		}
		if (denied) {
			removeGroup(role.getGrants(), group);
			role.getDenies().add(groupEntry(group));
		} else {
			removeGroup(role.getDenies(), group);
		}
	}

	/** The capability groups the named role forbids, in a stable order. */
	public Set<String> deniedGroups(String roleName) {
		RoleAccess role = role(roleName);
		return role == null ? Set.of() : groupsOf(role.getDenies());
	}

	/**
	 * The entries of the named role that name single tools rather than a capability group.
	 *
	 * @param roleName
	 *        The role to ask about.
	 * @param denies
	 *        Whether to read the refusals rather than the permissions.
	 * @return The entries, in the order they are stored; empty for a role the policy does not configure.
	 */
	public List<Grant> toolEntries(String roleName, boolean denies) {
		RoleAccess role = role(roleName);
		if (role == null) {
			return List.of();
		}
		List<Grant> result = new ArrayList<>();
		for (Grant entry : denies ? role.getDenies() : role.getGrants()) {
			if (isBlank(entry.getGroup())) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * Adds an entry naming single tools to the named role, with nothing chosen yet.
	 *
	 * @param roleName
	 *        The role to change.
	 * @param denies
	 *        Whether to add a refusal rather than a permission.
	 * @return The new entry, so that the caller can show it.
	 */
	public Grant addToolEntry(String roleName, boolean denies) {
		RoleAccess role = role(roleName);
		if (role == null) {
			return null;
		}
		Grant entry = TypedConfiguration.newConfigItem(Grant.class);
		entry.setTools(new ArrayList<>());
		(denies ? role.getDenies() : role.getGrants()).add(entry);
		return entry;
	}

	/** Removes the given entry from the named role. */
	public void removeEntry(String roleName, boolean denies, Grant entry) {
		RoleAccess role = role(roleName);
		if (role != null) {
			(denies ? role.getDenies() : role.getGrants()).remove(entry);
		}
	}

	/**
	 * Turns the given entry into one that names a capability group, or one that names tools.
	 *
	 * <p>
	 * Exactly one of the two is set afterwards, so the entry always names exactly one thing, which is
	 * the rule the boot check enforces and the raw editor cannot express.
	 * </p>
	 *
	 * @param entry
	 *        The entry to change.
	 * @param wholeGroup
	 *        Whether the entry should name a capability group.
	 */
	public static void setNamesGroup(Grant entry, boolean wholeGroup) {
		if (wholeGroup) {
			entry.setTools(new ArrayList<>());
			if (isBlank(entry.getGroup())) {
				entry.setGroup(ToolGroups.MODEL_READ);
			}
		} else {
			entry.setGroup(null);
			if (entry.getTools().isEmpty()) {
				entry.setTools(new ArrayList<>());
			}
		}
	}

	/** Whether the given entry names a capability group rather than single tools. */
	public static boolean namesWholeGroup(Grant entry) {
		return !isBlank(entry.getGroup());
	}

	// ------------------------------------------------------- basic permissions

	/** Which of the three baseline states the draft is in. */
	public BaselineChoice baselineChoice() {
		return _baselineChoice;
	}

	/**
	 * Puts the baseline into the given state.
	 *
	 * <p>
	 * {@link BaselineChoice#DEFAULT} removes the element, which is what makes the shipped default apply;
	 * {@link BaselineChoice#NOTHING} writes it empty, which is the explicit "nothing for everyone". An
	 * own list with nothing chosen writes the same empty element and therefore grants nothing either.
	 * </p>
	 *
	 * @param choice
	 *        The state to reach. Never <code>null</code>.
	 */
	public void setBaselineChoice(BaselineChoice choice) {
		_baselineChoice = choice;
		switch (choice) {
			case DEFAULT:
				_config.setBaseline(null);
				break;
			case NOTHING:
				if (_config.getBaseline() == null || !_config.getBaseline().getGrants().isEmpty()) {
					_config.setBaseline(TypedConfiguration.newConfigItem(Baseline.class));
				}
				break;
			default:
				if (_config.getBaseline() == null) {
					_config.setBaseline(TypedConfiguration.newConfigItem(Baseline.class));
				}
				break;
		}
	}

	/** The capability groups the declared baseline grants, empty when it declares none. */
	public Set<String> baselineGroups() {
		Baseline baseline = _config.getBaseline();
		return baseline == null ? Set.of() : groupsOf(baseline.getGrants());
	}

	/** The single tools the declared baseline grants, empty when it names none. */
	public Set<String> baselineTools() {
		Baseline baseline = _config.getBaseline();
		return baseline == null ? Set.of() : toolsOf(baseline.getGrants());
	}

	/** Makes the declared baseline grant exactly the given capability groups. */
	public void setBaselineGroups(List<String> groups) {
		setBaselineChoice(BaselineChoice.CUSTOM);
		setGroups(_config.getBaseline().getGrants(), groups);
	}

	/** Makes the declared baseline grant exactly the given single tools. */
	public void setBaselineTools(List<String> tools) {
		setBaselineChoice(BaselineChoice.CUSTOM);
		setTools(_config.getBaseline().getGrants(), tools);
	}

	// --------------------------------------------------------- unattended runs

	/** Which of the three states the background limit is in. */
	public BackgroundChoice backgroundChoice() {
		return _backgroundChoice;
	}

	/**
	 * Puts the background limit into the given state.
	 *
	 * @param choice
	 *        The state to reach. Never <code>null</code>.
	 */
	public void setBackgroundChoice(BackgroundChoice choice) {
		_backgroundChoice = choice;
		if (choice == BackgroundChoice.UNDECLARED) {
			_config.setBackgroundAllowlist(null);
			return;
		}
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		if (allowlist == null) {
			allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
			_config.setBackgroundAllowlist(allowlist);
		}
		if (allowlist.isAll() != (choice == BackgroundChoice.ALL)) {
			allowlist.setAll(choice == BackgroundChoice.ALL);
		}
	}

	/** The capability groups an unattended run may reach, empty while nothing is declared. */
	public Set<String> backgroundGroups() {
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		return allowlist == null ? Set.of() : groupsOf(allowlist.getGrants());
	}

	/** The single tools an unattended run may reach, empty while none is named. */
	public Set<String> backgroundTools() {
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		return allowlist == null ? Set.of() : toolsOf(allowlist.getGrants());
	}

	/** Makes the background limit admit exactly the given capability groups. */
	public void setBackgroundGroups(List<String> groups) {
		setBackgroundChoice(BackgroundChoice.CUSTOM);
		setGroups(_config.getBackgroundAllowlist().getGrants(), groups);
	}

	/** Makes the background limit admit exactly the given single tools. */
	public void setBackgroundTools(List<String> tools) {
		setBackgroundChoice(BackgroundChoice.CUSTOM);
		setTools(_config.getBackgroundAllowlist().getGrants(), tools);
	}

	// ------------------------------------------------------- group assignments

	/**
	 * The capability groups the policy assigns tools to, in the order they are shown.
	 *
	 * <p>
	 * A group assignment says what a tool <em>is</em>. Tools of a connected server and tools authored in
	 * the running application declare no group of their own and are reachable by nobody until one is
	 * assigned here.
	 * </p>
	 */
	public List<String> assignedGroups() {
		return new ArrayList<>(new TreeSet<>(_config.getToolGroups().keySet()));
	}

	/** The tools the policy assigns to the named capability group, empty when there is no entry. */
	public List<String> assignedTools(String group) {
		GroupMembership membership = _config.getToolGroups().get(group);
		return membership == null ? List.of() : new ArrayList<>(membership.getTools());
	}

	/**
	 * Assigns exactly the given tools to the named capability group.
	 *
	 * @param group
	 *        The technical group name. An empty name is ignored.
	 * @param tools
	 *        The tools to assign. An empty list removes the assignment, because an entry naming no tool
	 *        assigns nothing and is refused as a policy that says nothing.
	 */
	public void setAssignedTools(String group, List<String> tools) {
		if (isBlank(group)) {
			return;
		}
		if (tools.isEmpty()) {
			_config.getToolGroups().remove(group);
			return;
		}
		GroupMembership membership = _config.getToolGroups().get(group);
		if (membership == null) {
			membership = TypedConfiguration.newConfigItem(GroupMembership.class);
			membership.setName(group);
			_config.getToolGroups().put(group, membership);
		}
		if (!membership.getTools().equals(tools)) {
			membership.setTools(new ArrayList<>(tools));
		}
	}

	/**
	 * Moves an assignment to another capability group, keeping its tools.
	 *
	 * @param group
	 *        The group the assignment has now.
	 * @param newGroup
	 *        The group it should have. When that group already has an assignment, the two are merged,
	 *        because one group has one entry and losing the other one silently would be worse.
	 */
	public void renameAssignment(String group, String newGroup) {
		if (isBlank(newGroup) || newGroup.equals(group)) {
			return;
		}
		List<String> tools = new ArrayList<>(assignedTools(newGroup));
		for (String tool : assignedTools(group)) {
			if (!tools.contains(tool)) {
				tools.add(tool);
			}
		}
		_config.getToolGroups().remove(group);
		setAssignedTools(newGroup, tools);
	}

	/**
	 * The capability groups an assignment may be moved to: the free ones, plus its own.
	 *
	 * <p>
	 * One group has one entry, so offering a group that already has one would mean offering to merge
	 * two assignments by picking a value in a drop-down.
	 * </p>
	 *
	 * @param group
	 *        The group the assignment has now, always part of the result.
	 */
	public List<String> assignableGroups(String group) {
		List<String> result = new ArrayList<>();
		for (String candidate : grantableGroups()) {
			if (candidate.equals(group) || !_config.getToolGroups().containsKey(candidate)) {
				result.add(candidate);
			}
		}
		return result;
	}

	/** Removes the assignment of the named capability group, declassifying the tools in it. */
	public void removeAssignment(String group) {
		_config.getToolGroups().remove(group);
	}

	/**
	 * The capability group an added assignment starts with: one no assignment names yet.
	 *
	 * @return The group name, or <code>null</code> if every known group is already assigned to.
	 */
	public String unassignedGroup() {
		for (String group : grantableGroups()) {
			if (!_config.getToolGroups().containsKey(group)) {
				return group;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------ groups

	/**
	 * The capability groups the editor offers a decision about.
	 *
	 * <p>
	 * Every group the framework ships and every group the policy itself mentions, minus the two that are
	 * granted implicitly - those are shown as locked rows and have no decision to make. A group an
	 * application contributes appears as soon as the policy mentions it, so that an entry naming it stays
	 * visible and editable.
	 * </p>
	 *
	 * @return The group names, in a stable order.
	 */
	public List<String> grantableGroups() {
		Set<String> groups = new TreeSet<>(ToolGroups.ALL);
		groups.addAll(_config.getToolGroups().keySet());
		for (RoleAccess role : _config.getToolAccess().values()) {
			groups.addAll(groupsOf(role.getGrants()));
			groups.addAll(groupsOf(role.getDenies()));
		}
		groups.addAll(baselineGroups());
		groups.addAll(backgroundGroups());
		groups.removeAll(RoleToolAccess.IMPLICIT_BASELINE);
		return new ArrayList<>(groups);
	}

	/** The capability groups every account holds whatever the policy says, in a stable order. */
	public static List<String> implicitGroups() {
		return new ArrayList<>(new TreeSet<>(RoleToolAccess.IMPLICIT_BASELINE));
	}

	/** Whether the baseline of the draft grants the given group to every account. */
	public boolean baselineCovers(String group) {
		Baseline declared = _config.getBaseline();
		if (declared == null) {
			return RoleToolAccess.DEFAULT_BASELINE.contains(group);
		}
		return namesGroup(declared.getGrants(), group);
	}

	// ----------------------------------------------------------------- helpers

	private BaselineChoice readBaselineChoice() {
		Baseline baseline = _config.getBaseline();
		if (baseline == null) {
			return BaselineChoice.DEFAULT;
		}
		return baseline.getGrants().isEmpty() ? BaselineChoice.NOTHING : BaselineChoice.CUSTOM;
	}

	private BackgroundChoice readBackgroundChoice() {
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		if (allowlist == null) {
			return BackgroundChoice.UNDECLARED;
		}
		return allowlist.isAll() ? BackgroundChoice.ALL : BackgroundChoice.CUSTOM;
	}

	private static Grant groupEntry(String group) {
		Grant entry = TypedConfiguration.newConfigItem(Grant.class);
		entry.setGroup(group);
		return entry;
	}

	/**
	 * Rewrites the group entries of a list so that it names exactly the given groups.
	 *
	 * <p>
	 * A no-op when it already does, so that reading a list and writing it back leaves the order of a
	 * hand-written policy alone. Entries naming single tools are not touched.
	 * </p>
	 */
	private static void setGroups(List<Grant> entries, List<String> groups) {
		if (groupsOf(entries).equals(new TreeSet<>(groups))) {
			return;
		}
		entries.removeIf(entry -> !isBlank(entry.getGroup()));
		for (String group : new TreeSet<>(groups)) {
			entries.add(groupEntry(group));
		}
	}

	/**
	 * Rewrites the tool entries of a list so that they name exactly the given tools.
	 *
	 * <p>
	 * A no-op when they already do. Several entries naming tools are collapsed into one when the set
	 * changes, which is the same statement in fewer entries.
	 * </p>
	 */
	private static void setTools(List<Grant> entries, List<String> tools) {
		if (toolsOf(entries).equals(new LinkedHashSet<>(tools))) {
			return;
		}
		entries.removeIf(entry -> isBlank(entry.getGroup()));
		if (!tools.isEmpty()) {
			Grant entry = TypedConfiguration.newConfigItem(Grant.class);
			entry.setTools(new ArrayList<>(tools));
			entries.add(entry);
		}
	}

	private static Set<String> groupsOf(List<Grant> entries) {
		Set<String> result = new TreeSet<>();
		for (Grant entry : entries) {
			if (!isBlank(entry.getGroup())) {
				result.add(entry.getGroup());
			}
		}
		return result;
	}

	private static Set<String> toolsOf(List<Grant> entries) {
		Set<String> result = new LinkedHashSet<>();
		for (Grant entry : entries) {
			if (isBlank(entry.getGroup())) {
				result.addAll(entry.getTools());
			}
		}
		return result;
	}

	private static boolean namesGroup(List<Grant> entries, String group) {
		for (Grant entry : entries) {
			if (group.equals(entry.getGroup())) {
				return true;
			}
		}
		return false;
	}

	private static void removeGroup(List<Grant> entries, String group) {
		entries.removeIf(entry -> group.equals(entry.getGroup()));
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	/**
	 * A draft of the policy the configuration files currently say.
	 *
	 * @return The draft, or <code>null</code> if the saved configuration cannot be read.
	 */
	public static PolicyDraft ofSavedPolicy() {
		AiToolAccessService.Config<?> saved = AiToolAccessService.savedPolicy();
		return saved == null ? null : new PolicyDraft(saved);
	}

	/**
	 * Whether the saved policy differs from the one the running service applies.
	 *
	 * <p>
	 * Computed rather than remembered, so that the "saved but not yet in effect" banner is right after a
	 * page reload, in a second session, and after somebody restarted the service elsewhere. A flag set
	 * when this editor saved would be wrong in all three cases.
	 * </p>
	 *
	 * @param saved
	 *        The saved policy, may be <code>null</code> - in which case nothing can be said and
	 *        <code>false</code> is the honest answer.
	 */
	public static boolean differsFromRunningService(ToolAccessPolicy saved) {
		if (saved == null || !AiToolAccessService.Module.INSTANCE.isActive()) {
			return false;
		}
		return !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(AiToolAccessService.policy(), saved);
	}

}
