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

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.BackgroundAllowlist;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Baseline;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.Grant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.RoleAccess;
import com.top_logic.tl.ai.core.security.ToolAccessRule;
import com.top_logic.tl.ai.core.security.layout.PolicyChecks;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CapabilityState;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.RoleTemplate;
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

		draft.setBaselineGroups(List.of(ToolGroups.MODEL_READ));
		assertEquals(BaselineChoice.CUSTOM, draft.baselineChoice());
		assertEquals(Set.of(ToolGroups.MODEL_READ), draft.baselineGroups());
		assertEquals("One entry, naming the group and nothing else.", List.of(ToolGroups.MODEL_READ),
			groupsOf(draft.config().getBaseline().getGrants()));

		draft.setBaselineChoice(BaselineChoice.DEFAULT);
		assertNull(draft.config().getBaseline());
	}

	/**
	 * "Own list" is a state the editor can actually reach.
	 *
	 * <p>
	 * It writes the same empty element as "nothing", so a choice derived from the stored element alone
	 * read it back as "nothing" and the button did nothing at all. Both sections had the defect; the
	 * background one hid it because the fixture starts in that state.
	 * </p>
	 */
	public void testOwnListCanBeChosen() {
		PolicyDraft draft = draft();

		draft.setBaselineChoice(BaselineChoice.NOTHING);
		draft.setBaselineChoice(BaselineChoice.CUSTOM);
		assertEquals("Choosing an own list has to leave the editor in that state.",
			BaselineChoice.CUSTOM, draft.baselineChoice());
		assertEquals("An own list with nothing chosen yet grants nothing.", Set.of(),
			draft.baselineGroups());

		draft.setBaselineGroups(List.of(ToolGroups.DATA_READ));
		assertEquals(Set.of(ToolGroups.DATA_READ), draft.baselineGroups());

		draft.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		draft.setBackgroundChoice(BackgroundChoice.CUSTOM);
		assertEquals(BackgroundChoice.CUSTOM, draft.backgroundChoice());
		assertEquals(Set.of(), draft.backgroundGroups());

		draft.setBackgroundChoice(BackgroundChoice.ALL);
		draft.setBackgroundChoice(BackgroundChoice.CUSTOM);
		assertEquals("Coming from 'everything' also has to reach the own list.",
			BackgroundChoice.CUSTOM, draft.backgroundChoice());
	}

	/** The three states of the background limit, and what each of them stores. */
	public void testTheThreeBackgroundStates() {
		PolicyDraft draft = draft();

		draft.setBackgroundChoice(BackgroundChoice.UNDECLARED);
		assertNull("Nothing stated is no element at all, and refused when applied.",
			draft.config().getBackgroundAllowlist());

		draft.setBackgroundChoice(BackgroundChoice.ALL);
		BackgroundAllowlist all = draft.config().getBackgroundAllowlist();
		assertNotNull(all);
		assertTrue("The explicit opt-out is a flag, not an empty list.", all.isAll());
		assertEquals(BackgroundChoice.ALL, draft.backgroundChoice());

		draft.setBackgroundChoice(BackgroundChoice.CUSTOM);
		assertFalse(draft.config().getBackgroundAllowlist().isAll());
		draft.setBackgroundGroups(List.of(ToolGroups.DATA_READ));
		assertEquals(Set.of(ToolGroups.DATA_READ), draft.backgroundGroups());
		assertEquals(BackgroundChoice.CUSTOM, draft.backgroundChoice());
	}

	/**
	 * Single tools can be named where only whole capabilities used to be offered.
	 *
	 * <p>
	 * The stored format uses the same entry type for the basic permissions and for the limit of
	 * unattended runs as for a role, so both accept a tool by name, and the enforcement honours it.
	 * </p>
	 */
	public void testSingleToolsInTheBaselineAndTheBackgroundLimit() {
		PolicyDraft draft = draft();

		draft.setBaselineTools(List.of(MODEL_READ_TOOL));
		assertEquals(Set.of(MODEL_READ_TOOL), draft.baselineTools());
		assertTrue("A named tool must not turn into a capability.", draft.baselineGroups().isEmpty());

		draft.setBackgroundTools(List.of(MODEL_READ_TOOL));
		assertEquals(Set.of(MODEL_READ_TOOL), draft.backgroundTools());
		assertEquals("The capabilities of the fixture's limit stay where they are.",
			draft().backgroundGroups(), draft.backgroundGroups());

		assertEquals("Naming a tool must not make the policy unbootable.", List.of(),
			keys(PolicyChecks.refusals(draft.config(), ROLE_EXISTS)));
	}

	/**
	 * A group assignment can be added, moved to another capability and removed.
	 *
	 * <p>
	 * The catalog reads the same map, so the "source of the group" it reports follows an apply.
	 * </p>
	 */
	public void testGroupAssignmentsRoundTrip() {
		PolicyDraft draft = draft();
		String free = draft.unassignedGroup();
		assertNotNull("The fixture has to leave at least one capability unassigned.", free);

		draft.setAssignedTools(free, List.of(MODEL_READ_TOOL));
		assertEquals(List.of(MODEL_READ_TOOL), draft.assignedTools(free));
		assertTrue(draft.assignedGroups().contains(free));

		String moved = draft.unassignedGroup();
		assertNotNull("With one capability taken there still has to be another one free.", moved);
		draft.renameAssignment(free, moved);
		assertFalse("Moving an assignment leaves nothing behind.",
			draft.assignedGroups().contains(free));
		assertEquals("Moving an assignment keeps its tools.", List.of(MODEL_READ_TOOL),
			draft.assignedTools(moved));

		draft.removeAssignment(moved);
		assertFalse(draft.assignedGroups().contains(moved));
		assertEquals("Adding and removing an assignment leaves the policy as it was.",
			draft().assignedGroups(), draft.assignedGroups());

		draft.setAssignedTools(moved, List.of(MODEL_READ_TOOL));
		draft.setAssignedTools(moved, List.of());
		assertFalse("An assignment naming no tool is no assignment, because it would be refused.",
			draft.assignedGroups().contains(moved));
	}

	/**
	 * One capability group has one assignment, so the editor never offers a taken one.
	 *
	 * <p>
	 * And where two do end up on the same group, the tools are merged rather than one list quietly
	 * replacing the other.
	 * </p>
	 */
	public void testAnAssignmentIsOfferedOnlyFreeGroups() {
		PolicyDraft draft = draft();
		String taken = draft.assignedGroups().get(0);
		String other = draft.assignedGroups().get(1);

		assertFalse("A group another assignment holds is not offered.",
			draft.assignableGroups(other).contains(taken));
		assertTrue("Its own group always is, or the row could not stay as it is.",
			draft.assignableGroups(other).contains(other));

		List<String> before = new ArrayList<>(draft.assignedTools(taken));
		before.addAll(draft.assignedTools(other));
		draft.renameAssignment(other, taken);
		assertEquals("Merging two assignments keeps every tool of both.", before,
			draft.assignedTools(taken));
		assertFalse(draft.assignedGroups().contains(other));
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
				draft.setCapabilityState(roleName, group, draft.capabilityState(roleName, group));
			}
		}
		draft.setBaselineChoice(draft.baselineChoice());
		draft.setBaselineGroups(new ArrayList<>(draft.baselineGroups()));
		draft.setBaselineTools(new ArrayList<>(draft.baselineTools()));
		draft.setBackgroundChoice(draft.backgroundChoice());
		draft.setBackgroundGroups(new ArrayList<>(draft.backgroundGroups()));
		draft.setBackgroundTools(new ArrayList<>(draft.backgroundTools()));
		for (String group : draft.assignedGroups()) {
			draft.setAssignedTools(group, draft.assignedTools(group));
		}

		assertFalse("Opening the editor and applying it must not rewrite the policy.",
			draft.isModified());
	}

	/**
	 * A capability that is switched to any of the three states and back leaves the policy as it was.
	 */
	public void testTheCapabilityStatesRoundTrip() {
		for (String group : List.of(ToolGroups.DATA_WRITE, ToolGroups.EGRESS, ToolGroups.SCRIPT)) {
			for (CapabilityState state : CapabilityState.values()) {
				PolicyDraft draft = draft();
				CapabilityState before = draft.capabilityState(PARTNER, group);

				draft.setCapabilityState(PARTNER, group, state);
				assertEquals(group + " -> " + state, state, draft.capabilityState(PARTNER, group));

				draft.setCapabilityState(PARTNER, group, before);
				assertEquals(group + ": back to " + before + " has to be the state we started from.",
					before, draft.capabilityState(PARTNER, group));
				assertEquals(group + ": and no other capability may have moved.",
					grantedGroups(draft().role(PARTNER)), grantedGroups(draft.role(PARTNER)));
				assertEquals(group + ": nor may a prohibition have been left behind.",
					deniedGroups(draft().role(PARTNER)), deniedGroups(draft.role(PARTNER)));
			}
		}
	}

	/**
	 * What each of the three states writes, and that the walk through all of them leaves one entry.
	 *
	 * <p>
	 * The mutual exclusion is structural here: one row writes one list, so a permission can never end
	 * up next to a prohibition of the same capability, and the empty state is the absence of both.
	 * </p>
	 */
	public void testWhatTheThreeStatesWrite() {
		PolicyDraft draft = draft();
		RoleAccess role = draft.role(PARTNER);

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.GRANTED);
		assertTrue("Granted is a permission naming the group.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertFalse(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.DENIED);
		assertFalse("A prohibition replaces the permission rather than sitting next to it.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertTrue(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.ABSENT);
		assertFalse("The empty state is the absence of both entries.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS));
		assertFalse(groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.DENIED);
		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.GRANTED);
		assertEquals("Walking the cycle must leave one entry, not one per step.", 1,
			role.getGrants().stream().filter(e -> ToolGroups.EGRESS.equals(e.getGroup())).count());
	}

	/**
	 * A policy naming one capability in both lists is what it enforces, and survives until it is clicked.
	 */
	public void testAPolicyThatGrantsAndForbidsTheSameThing() {
		PolicyDraft draft = draft();
		RoleAccess role = draft.role(PARTNER);
		Grant permission = TypedConfiguration.newConfigItem(Grant.class);
		permission.setGroup(ToolGroups.EGRESS);
		role.getGrants().add(permission);
		Grant prohibition = TypedConfiguration.newConfigItem(Grant.class);
		prohibition.setGroup(ToolGroups.EGRESS);
		role.getDenies().add(prohibition);

		assertEquals("A prohibition is what such a policy enforces, so it is what the row shows.",
			CapabilityState.DENIED, draft.capabilityState(PARTNER, ToolGroups.EGRESS));

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.DENIED);
		assertTrue("Writing back the state that was read must not touch either list.",
			groupsOf(role.getGrants()).contains(ToolGroups.EGRESS)
				&& groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.GRANTED);
		assertFalse("A deliberate click resolves it.",
			groupsOf(role.getDenies()).contains(ToolGroups.EGRESS));
		assertEquals("And leaves exactly one permission.", 1,
			role.getGrants().stream().filter(e -> ToolGroups.EGRESS.equals(e.getGroup())).count());
	}

	/**
	 * The tri-state rows and the entries naming single tools are two views of one list, not two layers.
	 */
	public void testTheCapabilityStateLeavesToolEntriesAlone() {
		PolicyDraft draft = draft();
		Grant entry = draft.addToolEntry(PARTNER, false);
		entry.setTools(new ArrayList<>(List.of(CLASSIFIED_READ_SCRIPT_TOOL)));

		for (CapabilityState state : List.of(CapabilityState.DENIED, CapabilityState.ABSENT,
			CapabilityState.GRANTED)) {
			draft.setCapabilityState(PARTNER, ToolGroups.DATA_READ, state);

			assertEquals("Changing a capability must not touch an entry that names tools.", 1,
				draft.toolEntries(PARTNER, false).size());
			assertEquals(List.of(CLASSIFIED_READ_SCRIPT_TOOL),
				draft.toolEntries(PARTNER, false).get(0).getTools());
		}
	}

	/**
	 * What the empty state of a row says about the rest of the policy.
	 *
	 * <p>
	 * The question the empty state raises, and the reason it is not a prohibition: somebody else may
	 * grant the capability anyway.
	 * </p>
	 */
	public void testWhoElseGrantsWhatARoleSaysNothingAbout() {
		PolicyDraft draft = draft();
		String other = draft.roleNames().stream().filter(name -> !name.equals(PARTNER)).findFirst()
			.orElseThrow();

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.ABSENT);
		draft.setCapabilityState(other, ToolGroups.EGRESS, CapabilityState.ABSENT);
		assertNull("Nobody grants it, so the row has nobody to point at.",
			draft.grantedElsewhere(PARTNER, ToolGroups.EGRESS));

		draft.setCapabilityState(other, ToolGroups.EGRESS, CapabilityState.GRANTED);
		ToolAccessRule elsewhere = draft.grantedElsewhere(PARTNER, ToolGroups.EGRESS);
		assertEquals("The other role grants it, and the row names it.", ToolAccessRule.Kind.ROLE_GRANT,
			elsewhere.kind());
		assertEquals(other, elsewhere.roleName());

		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.GRANTED);
		assertNotNull("The role's own state never answers this question.",
			draft.grantedElsewhere(PARTNER, ToolGroups.EGRESS));
		draft.setCapabilityState(other, ToolGroups.EGRESS, CapabilityState.ABSENT);
		assertNull("Only the others do.", draft.grantedElsewhere(PARTNER, ToolGroups.EGRESS));

		draft.setBaselineGroups(List.of(ToolGroups.EGRESS));
		assertEquals("The basic permissions answer it too, and are told apart from a role.",
			ToolAccessRule.Kind.BASELINE,
			draft.grantedElsewhere(PARTNER, ToolGroups.EGRESS).kind());
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
	 * A template prefills the capabilities and nothing else.
	 */
	public void testATemplatePrefillsTheCapabilities() {
		PolicyDraft draft = draft();
		draft.removeRole(PARTNER);
		draft.addRole(PARTNER, RoleTemplate.DEVELOPER);

		for (String group : RoleTemplate.DEVELOPER.groups()) {
			assertTrue(group + " belongs to the developer template.", draft.isGranted(PARTNER, group));
		}
		assertFalse("Nothing beyond the template's capabilities.",
			draft.isGranted(PARTNER, ToolGroups.EGRESS));
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
		draft.setCapabilityState(PARTNER, ToolGroups.EGRESS, CapabilityState.GRANTED);

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
		List<String> withWrite = new ArrayList<>(alsoWriting.backgroundGroups());
		withWrite.add(ToolGroups.DATA_WRITE);
		alsoWriting.setBackgroundGroups(withWrite);
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

	/** The capability groups the given role entry forbids. */
	private static Set<String> deniedGroups(RoleAccess role) {
		return new java.util.TreeSet<>(groupsOf(role.getDenies()));
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
