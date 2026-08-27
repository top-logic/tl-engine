/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.Test;

import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.layout.PolicyChecks;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.RoleTemplate;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.TriState;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * What the policy editor may save, and what it must refuse.
 *
 * <p>
 * Two properties are pinned here, and they are the ones the editor exists for. The <b>refusals</b> of
 * the editor are exactly the refusals of the next application start - anything else means a policy
 * that can be saved and then fails to boot, hours later and for somebody else. And every control is a
 * <b>projection</b> of the stored policy: reading a state and writing it back changes nothing, so
 * opening the editor and applying it cannot silently rewrite a hand-written policy.
 * </p>
 *
 * @see SecDemoEffectiveAccessTest
 */
public class SecDemoPolicyEditorTest extends SecDemoPersonaTest {

	/** A role name the fixture does not have, for the case an apply has to refuse. */
	private static final String UNKNOWN_ROLE = "supportAgent";

	/** The role the fixture grants the write capabilities to. */
	private static final String PARTNER = "projectPartner";

	/**
	 * The policy the application actually ships applies - otherwise every assertion below would be
	 * about a fixture nobody can boot.
	 */
	public void testTheShippedPolicyApplies() {
		PolicyDraft draft = draft();

		assertEquals("The fixture is a policy that starts, so the editor must accept it as it is.",
			List.of(), keys(PolicyChecks.refusals(draft.config(), ROLE_EXISTS)));
		assertFalse("A freshly opened draft has changed nothing.", draft.isModified());
	}

	/**
	 * An unknown role is refused, which is one of the two ways to provoke a refusal at all.
	 *
	 * <p>
	 * The either-or editing of a permission entry makes the group-versus-tools rule nearly
	 * unauthorable, so this and the missing background limit are the cases that matter - the
	 * prototype's own feedback said so before this existed.
	 * </p>
	 */
	public void testAnUnknownRoleIsRefused() {
		PolicyDraft draft = draft();
		draft.addRole(UNKNOWN_ROLE, RoleTemplate.READ_ONLY);

		assertTrue("The role does not exist, so applying this would stop the next start.",
			keys(PolicyChecks.refusals(draft.config(), ROLE_EXISTS))
				.contains(key("REFUSE_UNKNOWN_ROLE__ROLE")));
		assertTrue("The draft has changed, so there is something to refuse.", draft.isModified());
	}

	/** A policy that names roles and says nothing about unattended runs is refused. */
	public void testAMissingBackgroundLimitIsRefused() {
		PolicyDraft draft = draft();
		draft.setBackgroundChoice(BackgroundChoice.UNDECLARED);

		assertTrue("Silence about unattended runs is what the boot check refuses.",
			keys(PolicyChecks.refusals(draft.config(), ROLE_EXISTS))
				.contains(key("REFUSE_BACKGROUND_ALLOWLIST_UNDECLARED")));
	}

	/**
	 * The one broken state the either-or editing can still produce: an entry naming no tool yet.
	 */
	public void testAnEntryNamingNothingIsRefused() {
		PolicyDraft draft = draft();
		draft.addToolEntry(PARTNER, false);

		assertTrue("An entry that names neither a group nor a tool grants nothing and says so nowhere.",
			keys(PolicyChecks.refusals(draft.config(), ROLE_EXISTS))
				.contains(key("REFUSE_ENTRY_NAMES_NOTHING__WHERE_POSITION")));
	}

	/**
	 * The editor refuses exactly what the next application start refuses.
	 *
	 * <p>
	 * The assertion that makes the promise of the apply button true. The two lists are computed by
	 * different code - one for a log file, one for a form - so nothing but this keeps them in step.
	 * </p>
	 */
	public void testTheEditorRefusesWhatTheBootRefuses() {
		for (PolicyDraft draft : brokenAndSoundDrafts()) {
			ToolAccessPolicy policy = draft.config();
			boolean editorRefuses = !PolicyChecks.refusals(policy, ROLE_EXISTS).isEmpty();
			boolean bootRefuses = !AiToolAccessService.refusals(policy, ROLE_EXISTS).isEmpty();

			assertEquals("The editor and the boot check disagree about this policy: "
				+ AiToolAccessService.refusals(policy, ROLE_EXISTS), bootRefuses, editorRefuses);
		}
	}

	/** One draft per authorable state, sound ones included, for the agreement check above. */
	private List<PolicyDraft> brokenAndSoundDrafts() {
		List<PolicyDraft> result = new ArrayList<>();
		result.add(draft());

		PolicyDraft unknownRole = draft();
		unknownRole.addRole(UNKNOWN_ROLE, RoleTemplate.CLERK);
		result.add(unknownRole);

		PolicyDraft noBackground = draft();
		noBackground.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		result.add(noBackground);

		PolicyDraft emptyEntry = draft();
		emptyEntry.addToolEntry(PARTNER, true);
		result.add(emptyEntry);

		PolicyDraft everythingAtOnce = draft();
		everythingAtOnce.addRole(UNKNOWN_ROLE, RoleTemplate.DEVELOPER);
		everythingAtOnce.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		everythingAtOnce.addToolEntry(PARTNER, false);
		result.add(everythingAtOnce);

		PolicyDraft withoutRoles = draft();
		for (String roleName : draft().roleNames()) {
			withoutRoles.removeRole(roleName);
		}
		withoutRoles.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		// A policy that names no role has nothing it could have forgotten, so this one is sound.
		result.add(withoutRoles);

		return result;
	}

	/**
	 * The three baseline states, and what each of them stores.
	 *
	 * <p>
	 * The element's absent, empty and filled states are three different answers, and the editor's job
	 * is to make them three named choices. This is that mapping, in both directions.
	 * </p>
	 */
	public void testTheThreeBaselineStates() {
		PolicyDraft draft = draft();

		draft.setBaselineChoice(BaselineChoice.DEFAULT);
		assertNull("The shipped default applies exactly when no element is written.",
			draft.config().getBaseline());
		assertEquals(BaselineChoice.DEFAULT, draft.baselineChoice());

		draft.setBaselineChoice(BaselineChoice.NOTHING);
		Baseline empty = draft.config().getBaseline();
		assertNotNull("An explicit 'nothing for everyone' is a written element.", empty);
		assertEquals("...with no entry in it.", List.of(), empty.getGrants());
		assertEquals(BaselineChoice.NOTHING, draft.baselineChoice());

		draft.toggleBaselineGroup(ToolGroups.MODEL_READ);
		assertEquals(BaselineChoice.CUSTOM, draft.baselineChoice());
		assertEquals(Set.of(ToolGroups.MODEL_READ), draft.baselineGroups());
		assertEquals("One entry, naming the group and nothing else.", List.of(ToolGroups.MODEL_READ),
			groupsOf(draft.config().getBaseline().getGrants()));

		draft.toggleBaselineGroup(ToolGroups.MODEL_READ);
		assertEquals("Taking the last capability away is 'nothing', not 'the default'.",
			BaselineChoice.NOTHING, draft.baselineChoice());

		draft.setBaselineChoice(BaselineChoice.DEFAULT);
		assertNull(draft.config().getBaseline());
	}

	/** The three states of the background limit, and what each of them stores. */
	public void testTheThreeBackgroundStates() {
		PolicyDraft draft = draft();

		draft.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		assertNull("Nothing stated is no element at all - and refused when applied.",
			draft.config().getBackgroundAllowlist());

		draft.setBackgroundChoice(BackgroundChoice.ALL);
		BackgroundAllowlist all = draft.config().getBackgroundAllowlist();
		assertNotNull(all);
		assertTrue("The explicit opt-out is a flag, not an empty list.", all.isAll());
		assertEquals(BackgroundChoice.ALL, draft.backgroundChoice());

		draft.setBackgroundChoice(BackgroundChoice.CUSTOM);
		assertFalse(draft.config().getBackgroundAllowlist().isAll());
		draft.toggleBackgroundGroup(ToolGroups.DATA_READ);
		assertEquals(Set.of(ToolGroups.DATA_READ), draft.backgroundGroups());
		assertEquals(BackgroundChoice.CUSTOM, draft.backgroundChoice());
	}

	/**
	 * Reading a state and writing it back changes nothing.
	 *
	 * <p>
	 * The round-trip that makes the editor safe to open on a hand-written policy: every tri-state of
	 * every configured role is set to the value it already has, and the result has to be the same
	 * policy - not an equivalent one, the same one.
	 * </p>
	 */
	public void testWritingBackWhatWasReadChangesNothing() {
		PolicyDraft draft = draft();
		for (String roleName : draft.roleNames()) {
			for (String group : draft.grantableGroups()) {
				draft.setTriState(roleName, group, draft.triState(roleName, group));
			}
		}
		draft.setBaselineChoice(draft.baselineChoice());
		draft.setBackgroundChoice(draft.backgroundChoice());

		assertFalse("Opening the editor and applying it must not rewrite the policy.",
			draft.isModified());
	}

	/**
	 * A tri-state that is changed and changed back leaves the policy as it was.
	 */
	public void testTheTriStateRoundTrips() {
		for (String group : List.of(ToolGroups.DATA_WRITE, ToolGroups.EGRESS, ToolGroups.SCRIPT)) {
			for (TriState state : TriState.values()) {
				PolicyDraft draft = draft();
				TriState before = draft.triState(PARTNER, group);

				draft.setTriState(PARTNER, group, state);
				assertEquals(group + " -> " + state, state, draft.triState(PARTNER, group));

				draft.setTriState(PARTNER, group, before);
				assertEquals(group + ": back to " + before + " has to be the state we started from.",
					before, draft.triState(PARTNER, group));
				assertEquals(group + ": and no other capability may have moved.",
					draft().role(PARTNER) == null ? Set.of() : grantedGroups(draft().role(PARTNER)),
					grantedGroups(draft.role(PARTNER)));
			}
		}
	}

	/** Each state writes the entry it means, and nothing else. */
	public void testWhatEachTriStateWrites() {
		PolicyDraft draft = draft();

		draft.setTriState(PARTNER, ToolGroups.EGRESS, TriState.GRANTED);
		RoleAccess role = draft.role(PARTNER);
		assertTrue("Granted is a permission naming the group.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertFalse(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setTriState(PARTNER, ToolGroups.EGRESS, TriState.DENIED);
		assertFalse("A refusal replaces the permission rather than sitting next to it.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertTrue(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setTriState(PARTNER, ToolGroups.EGRESS, TriState.UNSET);
		assertFalse("Unmentioned is the absence of both entries.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertFalse(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));
	}

	/**
	 * The tri-states and the entries naming single tools are two views of one list, not two layers.
	 */
	public void testTheTriStateLeavesToolEntriesAlone() {
		PolicyDraft draft = draft();
		Grant entry = draft.addToolEntry(PARTNER, false);
		entry.setTools(new ArrayList<>(List.of(CLASSIFIED_READ_SCRIPT_TOOL)));

		draft.setTriState(PARTNER, ToolGroups.DATA_READ, TriState.DENIED);
		draft.setTriState(PARTNER, ToolGroups.DATA_READ, TriState.GRANTED);

		assertEquals("Changing a capability must not touch an entry that names tools.", 1,
			draft.toolEntries(PARTNER, false).size());
		assertEquals(List.of(CLASSIFIED_READ_SCRIPT_TOOL),
			draft.toolEntries(PARTNER, false).get(0).getTools());
	}

	/** An entry names a group or tools, and switching between the two leaves exactly one of them. */
	public void testAnEntryNamesExactlyOneThing() {
		PolicyDraft draft = draft();
		Grant entry = draft.addToolEntry(PARTNER, false);

		assertFalse("A new entry names tools, which is what the editor offers to pick.",
			PolicyDraft.namesWholeGroup(entry));

		PolicyDraft.setNamesGroup(entry, true);
		assertTrue(PolicyDraft.namesWholeGroup(entry));
		assertEquals("Switching to a group must leave no tool behind, or the entry names both.",
			List.of(), entry.getTools());

		PolicyDraft.setNamesGroup(entry, false);
		assertFalse("Switching back must leave no group behind.",
			PolicyDraft.namesWholeGroup(entry));
	}

	/**
	 * A template prefills the tri-states and nothing else.
	 */
	public void testATemplatePrefillsTheCapabilities() {
		PolicyDraft draft = draft();
		draft.removeRole(PARTNER);
		draft.addRole(PARTNER, RoleTemplate.DEVELOPER);

		for (String group : RoleTemplate.DEVELOPER.groups()) {
			assertEquals(group + " belongs to the developer template.", TriState.GRANTED,
				draft.triState(PARTNER, group));
		}
		assertEquals("Nothing beyond the template's capabilities.", TriState.UNSET,
			draft.triState(PARTNER, ToolGroups.EGRESS));
		assertEquals("A template writes permissions, never refusals.", List.of(),
			draft.role(PARTNER).getDenies());
	}

	/**
	 * The foot-gun warnings the fixture earns, and the ones it must not.
	 */
	public void testTheWarningsOfTheFixture() {
		PolicyDraft draft = draft();
		List<String> warnings = keys(PolicyChecks.warnings(draft.config(), Set.of(PARTNER)));

		assertTrue("The partner holds both TL-Script and the permission to change data: " + warnings,
			warnings.contains(key("WARN_SCRIPT_WITH_DATA_WRITE__ROLE")));
		assertTrue("The ticket reader holds TL-Script and not the permission to change data, so the"
			+ " other half of that pair is earned by the other role: " + warnings,
			warnings.contains(key("WARN_SCRIPT_WITHOUT_DATA_WRITE__ROLE")));
		assertFalse("The fixture grants nobody the permission to send data outward.",
			warnings.contains(key("WARN_DATA_AND_EGRESS__ROLE")));
	}

	/** Adding the capability to send data outward next to a read capability is worth a warning. */
	public void testTheEgressWarningAppearsWhenItIsEarned() {
		PolicyDraft draft = draft();
		draft.setTriState(PARTNER, ToolGroups.EGRESS, TriState.GRANTED);

		assertTrue("Reading data and sending it outward is the combination worth one sentence.",
			keys(PolicyChecks.warnings(draft.config(), Set.of(PARTNER)))
				.contains(key("WARN_DATA_AND_EGRESS__ROLE")));
	}

	/** A limit that admits every tool for unattended runs says so. */
	public void testTheBackgroundWarnings() {
		PolicyDraft draft = draft();
		draft.setBackgroundChoice(BackgroundChoice.ALL);
		assertTrue(keys(PolicyChecks.warnings(draft.config(), Set.of()))
			.contains(key("WARN_BACKGROUND_ALL")));

		// The fixture's own limit carries TL-Script and deliberately not the permission to change
		// data, which is exactly the combination the two sentences have to tell apart.
		assertTrue("TL-Script without the permission to change data cannot persist anything, and an"
			+ " administrator has to know which of the two they wrote.",
			keys(PolicyChecks.warnings(draft().config(), Set.of()))
				.contains(key("WARN_BACKGROUND_SCRIPT")));

		PolicyDraft alsoWriting = draft();
		alsoWriting.toggleBackgroundGroup(ToolGroups.DATA_WRITE);
		assertTrue("With both on the list, an unattended run can persist anything a script expresses.",
			keys(PolicyChecks.warnings(alsoWriting.config(), Set.of()))
				.contains(key("WARN_BACKGROUND_SCRIPT_AND_DATA_WRITE")));
	}

	/**
	 * The saved policy and the running one are the same until something writes.
	 *
	 * <p>
	 * The banner of the editor is computed from this comparison rather than remembered, so that it is
	 * right after a page reload and in a second session.
	 * </p>
	 */
	public void testSavedAndRunningAgreeUntilSomethingWrites() {
		assertFalse("Nothing has been applied in this test run, so there is nothing to warn about.",
			PolicyDraft.differsFromRunningService(AiToolAccessService.savedPolicy()));
		assertTrue("The running service reads the saved configuration, so the two are one policy.",
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(AiToolAccessService.policy(),
				AiToolAccessService.savedPolicy()));
	}

	/** A draft of the policy the application ships. */
	private static PolicyDraft draft() {
		PolicyDraft draft = PolicyDraft.ofSavedPolicy();
		assertNotNull("The saved tool access configuration has to be readable.", draft);
		return draft;
	}

	/** Whether a role of the given name exists, the oracle the boot check uses. */
	private static final Predicate<String> ROLE_EXISTS =
		name -> BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), name) != null;

	/** The plain resource keys of the given messages, so that an assertion names a check. */
	private static List<String> keys(List<ResKey> messages) {
		List<String> result = new ArrayList<>();
		for (ResKey message : messages) {
			result.add(message.getKey());
		}
		return result;
	}

	/** The plain resource key of the named constant of the view's resources. */
	private static String key(String constant) {
		return "class.com.top_logic.tl.ai.core.security.layout.I18NConstants." + constant;
	}

	/** The capability groups the given role entry grants. */
	private static Set<String> grantedGroups(RoleAccess role) {
		return new java.util.TreeSet<>(groupsOf(role.getGrants()));
	}

	/** The capability groups the given entries name, in the order they are stored. */
	private static List<String> groupsOf(List<Grant> entries) {
		List<String> result = new ArrayList<>();
		for (Grant entry : entries) {
			if (entry.getGroup() != null && !entry.getGroup().isBlank()) {
				result.add(entry.getGroup());
			}
		}
		return result;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoPolicyEditorTest.class);
	}

}
