/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
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
 * is the load-bearing decision of this editor. Proposals D1 and F describe an editor whose controls do
 * not resemble the stored format at all - a tri-state per capability group, three named baseline states
 * - and the honest way to build that without a second format is to make every control a *question about*
 * and an *edit of* the real configuration item. A parallel model would need a converter in each
 * direction, and the round-trip test would then be testing the converter rather than the policy.
 *
 * The tri-state and the "Erweitert" entries are two views of one list, not two layers: a capability set
 * to "granted" IS a <grant group="..."/> entry, and a tool-level entry is one that names tools instead.
 * That is why setTriState rewrites only the entries naming that group and leaves every tool entry alone.
 */
public class PolicyDraft {

	/**
	 * What a role is allowed to do with one capability group.
	 */
	/*
	 * Three states because the policy has three, and the middle one is the whole point: "not granted" and
	 * "explicitly refused" are different, and the second survives another role of the same account
	 * granting it. Pattern 2 of the concept's research (the fine-grained token picker): an ordered set of
	 * named levels, so that no illegal combination can be expressed at all.
	 */
	public enum TriState {

		/** Refused. A refusal wins over every grant, including the ones of other roles. */
		DENIED,

		/** Not mentioned. The role gets it only if something else grants it. */
		UNSET,

		/** Granted. */
		GRANTED;
	}

	/**
	 * What every account gets in addition to what its roles grant.
	 */
	/*
	 * P10: the three states of the <baseline> element, which the raw editor renders as "an empty box" and
	 * "a box with entries" and cannot distinguish "absent" from "empty" at all.
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

		/** Only the chosen capabilities. */
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
	}

	/** The policy being edited, ready to be written as a service configuration. */
	public AiToolAccessService.Config<?> config() {
		return _config;
	}

	/** Whether the draft differs from the policy it was created from. */
	public boolean isModified() {
		return !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_saved, _config);
	}

	/** The evaluation of the draft, for the notes that depend on what it grants. */
	public RoleToolAccess access() {
		return AiToolAccessService.evaluate(_config);
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
	 * What the named role may do with the given capability group.
	 *
	 * @param roleName
	 *        The role to ask about.
	 * @param group
	 *        The technical group name.
	 * @return The state, never <code>null</code>.
	 */
	public TriState triState(String roleName, String group) {
		RoleAccess role = role(roleName);
		if (role == null) {
			return TriState.UNSET;
		}
		if (namesGroup(role.getDenies(), group)) {
			return TriState.DENIED;
		}
		return namesGroup(role.getGrants(), group) ? TriState.GRANTED : TriState.UNSET;
	}

	/**
	 * Rewrites the entries of the named role so that the group ends up in the given state.
	 *
	 * <p>
	 * Entries naming single tools are left untouched: they are the same list, but a different decision.
	 * </p>
	 *
	 * @param roleName
	 *        The role to change.
	 * @param group
	 *        The technical group name.
	 * @param state
	 *        The state to reach. Never <code>null</code>.
	 */
	public void setTriState(String roleName, String group, TriState state) {
		RoleAccess role = role(roleName);
		if (role == null || triState(roleName, group) == state) {
			// Writing back the state that is already there has to leave the entry where it is: the
			// editor is opened on a hand-written policy, and re-saving it must not reorder its lists.
			return;
		}
		removeGroup(role.getGrants(), group);
		removeGroup(role.getDenies(), group);
		switch (state) {
			case GRANTED:
				role.getGrants().add(groupEntry(group));
				break;
			case DENIED:
				role.getDenies().add(groupEntry(group));
				break;
			default:
				// UNSET is the absence of both entries, which is what was just established.
				break;
		}
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
	 * The either-or of proposal D1, as a change of the entry rather than as two inputs shown at once:
	 * exactly one of the two is set afterwards, so the entry always names exactly one thing - which is
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

	/** Which of the three baseline states the draft is in. */
	public BaselineChoice baselineChoice() {
		Baseline baseline = _config.getBaseline();
		if (baseline == null) {
			return BaselineChoice.DEFAULT;
		}
		return baseline.getGrants().isEmpty() ? BaselineChoice.NOTHING : BaselineChoice.CUSTOM;
	}

	/**
	 * Puts the baseline into the given state.
	 *
	 * <p>
	 * {@link BaselineChoice#DEFAULT} removes the element, which is what makes the shipped default apply;
	 * {@link BaselineChoice#NOTHING} writes it empty, which is the explicit "nothing for everyone".
	 * </p>
	 *
	 * @param choice
	 *        The state to reach. Never <code>null</code>.
	 */
	public void setBaselineChoice(BaselineChoice choice) {
		switch (choice) {
			case DEFAULT:
				_config.setBaseline(null);
				break;
			case NOTHING:
				_config.setBaseline(TypedConfiguration.newConfigItem(Baseline.class));
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

	/** Adds the given group to the declared baseline, or removes it if it is already there. */
	public void toggleBaselineGroup(String group) {
		setBaselineChoice(BaselineChoice.CUSTOM);
		toggle(_config.getBaseline().getGrants(), group);
	}

	/** Which of the three states the background limit is in. */
	public BackgroundChoice backgroundChoice() {
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		if (allowlist == null) {
			return BackgroundChoice.UNDECLARED;
		}
		return allowlist.isAll() ? BackgroundChoice.ALL : BackgroundChoice.CUSTOM;
	}

	/**
	 * Puts the background limit into the given state.
	 *
	 * @param choice
	 *        The state to reach. Never <code>null</code>.
	 */
	public void setBackgroundChoice(BackgroundChoice choice) {
		if (choice == BackgroundChoice.UNDECLARED) {
			_config.setBackgroundAllowlist(null);
			return;
		}
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		if (allowlist == null) {
			allowlist = TypedConfiguration.newConfigItem(BackgroundAllowlist.class);
			_config.setBackgroundAllowlist(allowlist);
		}
		allowlist.setAll(choice == BackgroundChoice.ALL);
	}

	/** The capability groups an unattended run may reach, empty while nothing is declared. */
	public Set<String> backgroundGroups() {
		BackgroundAllowlist allowlist = _config.getBackgroundAllowlist();
		return allowlist == null ? Set.of() : groupsOf(allowlist.getGrants());
	}

	/** Adds the given group to the background limit, or removes it if it is already there. */
	public void toggleBackgroundGroup(String group) {
		setBackgroundChoice(BackgroundChoice.CUSTOM);
		toggle(_config.getBackgroundAllowlist().getGrants(), group);
	}

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

	private static Grant groupEntry(String group) {
		Grant entry = TypedConfiguration.newConfigItem(Grant.class);
		entry.setGroup(group);
		return entry;
	}

	private static void toggle(List<Grant> entries, String group) {
		if (namesGroup(entries, group)) {
			removeGroup(entries, group);
		} else {
			entries.add(groupEntry(group));
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
	 * page reload, in a second session, and after somebody restarted the service elsewhere - a flag set
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
