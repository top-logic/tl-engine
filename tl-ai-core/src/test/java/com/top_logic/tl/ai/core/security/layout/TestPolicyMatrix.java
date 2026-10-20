/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Decision;
import com.top_logic.tl.ai.core.security.RoleAccessPolicy.Dimension;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelDefault;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelRoleAccess;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BackgroundChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.BaselineChoice;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix.RowState;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * How a policy matrix reads and writes the draft (#404).
 *
 * <p>
 * The rules pinned here are the ones that make the matrix safe to click in: a row the section does not
 * name is locked, emptying a row never widens it, and setting what is already there changes nothing.
 * </p>
 */
public class TestPolicyMatrix extends TestCase {

	private static final String READER = "ticketReader";

	private static final String PARTNER = "projectPartner";

	private static final String HAIKU = "claude-haiku";

	private static final String OPUS = "claude-opus";

	/** The draft copies configuration items, which resolve their types through the type index. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestPolicyMatrix.class)));
	}

	/** A role the section does not name is locked: a click would silently restrict it. */
	public void testANotNamedRowIsLocked() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);

		assertEquals(RowState.NOT_NAMED, models.rowState(READER));
		assertTrue(models.isLocked(READER));
		assertFalse("A locked row does not take a cell.", models.setCell(READER, HAIKU, CellState.GRANTED));
		assertFalse("And the draft is untouched.", models.draft().isModified());
	}

	/** Naming a role writes an empty entry, which restricts it to nothing. */
	public void testNamingARowRestrictsItToNothing() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);

		assertEquals(RowState.NAMED, models.rowState(READER));
		assertFalse(models.isLocked(READER));
		assertEquals(CellState.ABSENT, models.cell(READER, HAIKU));
		assertTrue(RoleAccessPolicy.models(models.draft().config()).check(Set.of(READER), HAIKU, Set.of())
			instanceof Decision.Denied);
	}

	/** The three states of a cell, and what each writes. */
	public void testGrantDenyClear() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);

		assertTrue(models.setCell(READER, HAIKU, CellState.GRANTED));
		assertEquals(CellState.GRANTED, models.cell(READER, HAIKU));
		assertEquals(List.of(HAIKU), models.draft().runNames(Dimension.MODEL, READER, false));

		models.setCell(READER, HAIKU, CellState.DENIED);
		assertEquals(CellState.DENIED, models.cell(READER, HAIKU));
		assertEquals("A name is in one list, never both.", List.of(),
			models.draft().runNames(Dimension.MODEL, READER, false));
		assertEquals(List.of(HAIKU), models.draft().runNames(Dimension.MODEL, READER, true));

		models.setCell(READER, HAIKU, CellState.ABSENT);
		assertEquals(CellState.ABSENT, models.cell(READER, HAIKU));
		assertEquals(List.of(), models.draft().runNames(Dimension.MODEL, READER, true));
	}

	/** Clearing the last cell leaves the role restricted - "may use none" - rather than widening it. */
	public void testAnEmptiedRowStaysRestricted() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);
		models.setCell(READER, HAIKU, CellState.GRANTED);
		models.setCell(READER, HAIKU, CellState.ABSENT);

		assertEquals(RowState.NAMED, models.rowState(READER));
		assertTrue("Under the default 'all', an emptied row must not fall back to it.",
			RoleAccessPolicy.models(allModels(models.draft()).config()).check(Set.of(READER), OPUS, Set.of())
				instanceof Decision.Denied);
	}

	/** Only the row state returns a role to the section default. */
	public void testTheRowStateReturnsARoleToTheDefault() {
		PolicyMatrix models = new PolicyMatrix(allModels(draft()), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);
		models.setCell(READER, HAIKU, CellState.DENIED);
		models.setRowState(READER, RowState.NOT_NAMED);

		assertEquals(RowState.NOT_NAMED, models.rowState(READER));
		assertEquals("Its cells went with its entry.", CellState.ABSENT, models.cell(READER, HAIKU));
		assertTrue(RoleAccessPolicy.models(models.draft().config()).check(Set.of(READER), HAIKU, Set.of())
			instanceof Decision.Granted);
	}

	/**
	 * Setting every cell and row to the state it has changes nothing, even for a hand-written policy
	 * with a name in two entries.
	 */
	public void testWritingWhatIsReadIsARoundTrip() {
		PolicyDraft draft = draft();
		ModelRoleAccess role = TypedConfiguration.newConfigItem(ModelRoleAccess.class);
		role.setName(READER);
		role.getGrants().add(modelGrant(HAIKU));
		role.getGrants().add(modelGrant(OPUS));
		draft.config().getModelAccess().getRoles().put(READER, role);
		PolicyDraft handWritten = new PolicyDraft(draft.config());
		String before = TypedConfiguration.toString(handWritten.config());

		PolicyMatrix models = new PolicyMatrix(handWritten, MatrixDimension.MODELS);
		for (String roleName : models.roles(List.of(READER, PARTNER))) {
			models.setRowState(roleName, models.rowState(roleName));
			for (String name : models.columns(List.of(HAIKU, OPUS))) {
				models.setCell(roleName, name, models.cell(roleName, name));
			}
		}

		assertEquals(before, TypedConfiguration.toString(handWritten.config()));
		assertFalse(handWritten.isModified());
	}

	/** A bulk edit skips rows the section does not name, unless asked to restrict them first. */
	public void testBulkSkipsLockedRowsUnlessAskedToRestrict() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);
		List<String> roles = List.of(READER, PARTNER);
		List<String> names = List.of(HAIKU, OPUS);

		assertEquals(2, models.affectedCells(roles, names, CellState.DENIED, false));
		assertEquals(4, models.affectedCells(roles, names, CellState.DENIED, true));

		assertEquals(2, models.apply(roles, names, CellState.DENIED, false));
		assertEquals(RowState.NOT_NAMED, models.rowState(PARTNER));

		assertEquals(2, models.apply(roles, names, CellState.DENIED, true));
		assertEquals(RowState.NAMED, models.rowState(PARTNER));
		assertEquals(CellState.DENIED, models.cell(PARTNER, OPUS));

		models.apply(roles, names, CellState.ABSENT, false);
		assertEquals("Clearing keeps both rows restricted.", RowState.NAMED, models.rowState(PARTNER));
		assertEquals(CellState.ABSENT, models.cell(PARTNER, OPUS));
	}

	/** A name the policy mentions and the application does not have is a column of its own. */
	public void testMentionedNamesAreColumns() {
		PolicyMatrix models = new PolicyMatrix(draft(), MatrixDimension.MODELS);
		models.setRowState(READER, RowState.NAMED);
		models.setCell(READER, "gpt-4-turbo", CellState.GRANTED);

		assertEquals(List.of(HAIKU, "gpt-4-turbo"), models.columns(List.of(HAIKU)));
	}

	/** Every role is a row, and so is a role only the policy names. */
	public void testRolesAreTheApplicationsAndThePolicys() {
		PolicyMatrix agents = new PolicyMatrix(draft(), MatrixDimension.AGENTS);
		agents.setRowState("legacyReportUser", RowState.NAMED);

		assertEquals(List.of("legacyReportUser", PARTNER, READER, SecurityRoleKind.ADMINISTRATOR),
			agents.roles(List.of(READER, SecurityRoleKind.ADMINISTRATOR, PARTNER)));
	}

	/** The capability matrix has no locked rows: the first mark gives a role its entry. */
	public void testACapabilityCellCreatesTheEntry() {
		PolicyMatrix capabilities = new PolicyMatrix(draft(), MatrixDimension.CAPABILITIES);

		assertFalse(capabilities.isLocked(READER));
		assertTrue(capabilities.setCell(READER, ToolGroups.SCRIPT, CellState.DENIED));
		assertEquals(RowState.NAMED, capabilities.rowState(READER));
		assertEquals(CellState.DENIED, capabilities.cell(READER, ToolGroups.SCRIPT));

		capabilities.setRowState(READER, RowState.NOT_NAMED);
		assertNull("Taking the entry away drops what it said.", capabilities.draft().role(READER));
	}

	/** The capability columns start with what every agent has and end with the privileged groups. */
	public void testCapabilityColumnsFollowTheCapabilityOrder() {
		List<String> columns = new PolicyMatrix(draft(), MatrixDimension.CAPABILITIES).columns(List.of());

		assertEquals(ToolGroups.MODEL_READ, columns.get(0));
		assertTrue(columns.indexOf(ToolGroups.DATA_READ) < columns.indexOf(ToolGroups.EGRESS));
		assertFalse("The implicit groups are no decision.", columns.contains(ToolGroups.INTERACTION));
	}

	/** The rows no role owns take a click only while they are an own list. */
	public void testBaselineAndBackgroundCellsNeedAnOwnList() {
		PolicyMatrix capabilities = new PolicyMatrix(draft(), MatrixDimension.CAPABILITIES);

		assertTrue("The shipped default shows.", capabilities.baselineCell(ToolGroups.MODEL_READ));
		assertFalse(capabilities.setBaselineCell(ToolGroups.SCRIPT, true));

		capabilities.draft().setBaselineChoice(BaselineChoice.CUSTOM);
		assertTrue(capabilities.setBaselineCell(ToolGroups.SCRIPT, true));
		assertTrue(capabilities.baselineCell(ToolGroups.SCRIPT));

		assertFalse(capabilities.setBackgroundCell(ToolGroups.SCRIPT, true));
		capabilities.draft().setBackgroundChoice(BackgroundChoice.ALL);
		assertTrue("All admits every group.", capabilities.backgroundCell(ToolGroups.EGRESS));
		capabilities.draft().setBackgroundChoice(BackgroundChoice.CUSTOM);
		assertTrue(capabilities.setBackgroundCell(ToolGroups.DATA_READ, true));
		assertEquals(Set.of(ToolGroups.DATA_READ), capabilities.draft().backgroundGroups());
	}

	private static ModelGrant modelGrant(String model) {
		ModelGrant grant = TypedConfiguration.newConfigItem(ModelGrant.class);
		grant.setModels(List.of(model));
		return grant;
	}

	private static PolicyDraft allModels(PolicyDraft draft) {
		draft.setSectionDefault(Dimension.MODEL, ModelDefault.ALL);
		return draft;
	}

	private static PolicyDraft draft() {
		return new PolicyDraft(TypedConfiguration.newConfigItem(AiToolAccessService.Config.class));
	}
}
