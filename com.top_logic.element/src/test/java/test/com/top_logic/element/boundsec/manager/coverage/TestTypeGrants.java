/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageAnalysis;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.SecurityDefinitionEditor;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants.Cell;
import com.top_logic.element.boundsec.manager.coverage.TypeGrants.Row;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.security.AccessGrant;
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.Resources;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Test of {@link TypeGrants}: the grants of a type as an editor presents them, and the round trip
 * of a changed grant through the {@link SecurityDefinitionEditor}.
 *
 * <p>
 * The editor works on temporary files, so the services keep the configuration they were started
 * with: a changed grant is stored, but not in effect, and shown as pending.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeGrants extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	/** Type with a role rule and grants of Read and Write. */
	private static final String COVERED = MODULE + ":Covered";

	/** Type declaring Finish implied by Read. */
	private static final String IMPLIED_OPERATION = MODULE + ":ImpliedOperation";

	/** Type delegating to its container. */
	private static final String EXPLICIT_PART = MODULE + ":ExplicitPart";

	/** Type excluded from access control. */
	private static final String UNSECURED = MODULE + ":Unsecured";

	private static final String OWNER = COVERED + "#owner";

	/** Type without a class grant, whose attribute {@link #PART_ONLY_LABEL} grants Read. */
	private static final String PART_ONLY = MODULE + ":PartOnly";

	/** The attribute of {@link #PART_ONLY} granting Read. */
	private static final String PART_ONLY_LABEL = PART_ONLY + "#label";

	/** The composition {@link #EXPLICIT_PART} delegates through. */
	private static final String EXPLICIT_PARTS = COVERED + "#explicitParts";

	private static final String ROLE_READER = MODULE + ".Reader";

	/** Role granted on {@link #DEAD_GRANT} that no rule delivers there. */
	private static final String ROLE_GHOST = MODULE + ".Ghost";

	/** Type with a read grant to {@link #ROLE_GHOST}. */
	private static final String DEAD_GRANT = MODULE + ":DeadGrant";

	private static final BoundCommandGroup READ = SimpleBoundCommandGroup.READ;

	private static final BoundCommandGroup WRITE = SimpleBoundCommandGroup.WRITE;

	private static final BoundCommandGroup DELETE = SimpleBoundCommandGroup.DELETE;

	private static final BoundCommandGroup DISCOVER = SimpleBoundCommandGroup.DISCOVER;

	private SecurityDefinitionEditor _editor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		File dir = createdCleanTestDir(getName());
		_editor = new SecurityDefinitionEditor(
			new File(dir, InAppServiceConfigStore.fileName(AccessManager.Module.INSTANCE)),
			new File(dir, InAppServiceConfigStore.fileName(SecurityConfigurationService.Module.INSTANCE)));
	}

	@Override
	protected void tearDown() throws Exception {
		_editor = null;

		super.tearDown();
	}

	public void testRowsAndOperations() throws Exception {
		TypeGrants grants = grants(COVERED);
		assertEquals(List.of(reader()), grants.getRoles());
		assertEquals("The built-in operations in their order.",
			List.of(READ, WRITE, SimpleBoundCommandGroup.CREATE, DELETE, DISCOVER, SimpleBoundCommandGroup.EXPORT),
			grants.getOperations());
		List<Row> rows = grants.getRows();
		assertEquals(new Row(null, reader()), rows.get(0));
		assertEquals("A row of the type, followed by a row per attribute.", 1 + grants.getAttributes().size(),
			rows.size());
	}

	public void testCellsInEffect() throws Exception {
		TypeGrants grants = grants(COVERED);
		Row row = new Row(null, reader());

		Cell read = grants.getCell(row, READ);
		assertTrue(read.granted());
		assertTrue(read.editable());
		assertFalse(read.pending());

		Cell delete = grants.getCell(row, DELETE);
		assertFalse(delete.granted());
		assertTrue(delete.editable());

		Cell discover = grants.getCell(row, DISCOVER);
		assertTrue(discover.granted());
		assertEquals("Every other operation implies Discover.", Set.of(READ, WRITE), discover.impliedBy());
		assertFalse("A grant held through an implying operation is changed there.", discover.editable());

		assertEquals("Discover held through type operations is not attributed to attributes.", Map.of(),
			discover.impliedByAttributes());

		assertSame("An attribute grant decides Read and Write only.", Cell.NOT_APPLICABLE,
			grants.getCell(new Row(attribute(OWNER), reader()), DELETE));
	}

	public void testDeclaredImplication() throws Exception {
		TypeGrants grants = grants(IMPLIED_OPERATION);
		BoundCommandGroup finish = CommandGroupRegistry.getInstance().getGroup(SimpleBoundCommandGroup.FINISH_NAME);
		assertTrue("A declared operation is offered after the built-in ones.", grants.getOperations().contains(finish));

		Cell cell = grants.getCell(new Row(null, reader()), finish);
		assertTrue(cell.granted());
		assertEquals(Set.of(READ), cell.impliedBy());
		assertFalse(cell.pending());
	}

	public void testGrantRoundTrip() throws Exception {
		TypeGrants grants = grants(COVERED);
		Row row = new Row(null, reader());

		grants.setGranted(row, DELETE, true);
		Cell granted = grants.getCell(row, DELETE);
		assertTrue(granted.granted());
		assertTrue("Stored, but not applied.", granted.pending());
		assertEquals(List.of("grant Delete " + ROLE_READER), describe(_editor.storedRules(COVERED)));

		grants.setGranted(row, DELETE, false);
		Cell revoked = grants.getCell(row, DELETE);
		assertFalse(revoked.granted());
		assertFalse("The stored state is the one in effect.", revoked.pending());
		assertEquals("Restoring what the underlying layers decide stores nothing.", List.of(),
			describe(_editor.storedRules(COVERED)));

		grants.setGranted(row, READ, false);
		Cell read = grants.getCell(row, READ);
		assertFalse(read.granted());
		assertTrue(read.pending());
		assertEquals("A revocation of a grant of the underlying layers is stored.",
			List.of("revoke Read " + ROLE_READER), describe(_editor.storedRules(COVERED)));
		assertEquals("Discover stays implied by Write.", Set.of(WRITE), grants.getCell(row, DISCOVER).impliedBy());
	}

	/**
	 * An implied grant follows the stored, not yet applied rules of the implying operations: it
	 * is gone when they are revoked (pending, since it differs from what is in effect), and back,
	 * read-only, when one of them is granted again.
	 */
	public void testImpliedStateFollowsTheStoredRules() throws Exception {
		TypeGrants grants = grants(COVERED);
		Row row = new Row(null, reader());

		grants.setGranted(row, READ, false);
		grants.setGranted(row, WRITE, false);
		Cell discover = grants.getCell(row, DISCOVER);
		assertFalse("Nothing implies Discover any more.", discover.granted());
		assertEquals(Set.of(), discover.impliedBy());
		assertTrue("Differs from what is in effect.", discover.pending());

		grants.setGranted(row, WRITE, true);
		Cell implied = grants.getCell(row, DISCOVER);
		assertTrue(implied.granted());
		assertEquals(Set.of(WRITE), implied.impliedBy());
		assertFalse(implied.editable());
		assertFalse("As in effect.", implied.pending());
	}

	/**
	 * A declared operation implied by another one follows the stored rules of the implying one.
	 */
	public void testDeclaredImplicationFollowsTheStoredRules() throws Exception {
		TypeGrants grants = grants(IMPLIED_OPERATION);
		BoundCommandGroup finish = CommandGroupRegistry.getInstance().getGroup(SimpleBoundCommandGroup.FINISH_NAME);
		Row row = new Row(null, reader());

		grants.setGranted(row, READ, false);
		Cell revoked = grants.getCell(row, finish);
		assertFalse(revoked.granted());
		assertTrue(revoked.pending());

		grants.setGranted(row, READ, true);
		Cell restored = grants.getCell(row, finish);
		assertTrue(restored.granted());
		assertEquals(Set.of(READ), restored.impliedBy());
		assertFalse(restored.pending());
	}

	/**
	 * A grant on an attribute implies Discover on the object; the cell names the attributes rather
	 * than the operation on the type.
	 */
	public void testDiscoverImpliedByAttributeGrants() throws Exception {
		TypeGrants grants = grants(PART_ONLY);
		Row row = new Row(null, reader());

		Cell discover = grants.getCell(row, DISCOVER);
		assertTrue(discover.granted());
		assertEquals("Not held through Read on the type.", Set.of(), discover.impliedBy());
		assertEquals(Map.of(READ, List.of(attribute(PART_ONLY_LABEL))), discover.impliedByAttributes());
		assertFalse("Changed on the attributes.", discover.editable());
		assertFalse(grants.getCell(row, READ).granted());
	}

	/**
	 * Toggling a grant off and on again restores what the underlying configuration layers decide:
	 * nothing is stored, and the file is gone.
	 */
	public void testToggleOffAndOnStoresNothing() throws Exception {
		TypeGrants grants = grants(COVERED);
		Row row = new Row(null, reader());

		grants.setGranted(row, READ, false);
		assertEquals(1, _editor.storedAccessRightsCount());
		grants.setGranted(row, READ, true);
		assertEquals("A grant of the underlying layers is not repeated.", 0, _editor.storedAccessRightsCount());
		assertFalse("Nothing is left to store.", _editor.getGrantsFile().exists());
		assertFalse(grants.getCell(row, READ).pending());

		grants.setGranted(row, DELETE, true);
		grants.setGranted(row, DELETE, false);
		assertEquals(0, _editor.storedAccessRightsCount());

		TLStructuredTypePart owner = attribute(OWNER);
		Row attributeRow = new Row(owner, reader());
		grants.setGranted(attributeRow, READ, true);
		grants.setGranted(attributeRow, READ, false);
		assertEquals(0, _editor.storedAccessRightsCount());
		assertFalse(_editor.getGrantsFile().exists());
	}

	/**
	 * A grant of the underlying layers to a role that cannot be held is revoked by a stored rule,
	 * and granting it again drops that rule.
	 */
	public void testRevokeAndRestoreAGrantOfTheUnderlyingLayers() throws Exception {
		TypeGrants grants = grants(DEAD_GRANT);
		Row row = new Row(null, BoundedRole.getRoleByName(ROLE_GHOST));

		grants.setGranted(row, READ, false);
		assertEquals(List.of("revoke Read " + ROLE_GHOST), describe(_editor.storedRules(DEAD_GRANT)));
		grants.setGranted(row, READ, true);
		assertEquals(List.of(), describe(_editor.storedRules(DEAD_GRANT)));
	}

	public void testImpliedGrantCannotBeChanged() throws Exception {
		TypeGrants grants = grants(COVERED);
		try {
			grants.setGranted(new Row(null, reader()), DISCOVER, false);
			fail("A grant held through an implying operation cannot be changed.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testAttributeRoundTrip() throws Exception {
		TypeGrants grants = grants(COVERED);
		TLStructuredTypePart owner = attribute(OWNER);
		Row row = new Row(owner, reader());

		Cell unrestricted = grants.getCell(row, READ);
		assertFalse("An attribute without a grant is not restricted.", unrestricted.restricted());
		assertTrue(unrestricted.editable());

		grants.setGranted(row, READ, true);
		Cell restricted = grants.getCell(row, READ);
		assertTrue(restricted.restricted());
		assertTrue(restricted.granted());
		assertTrue(restricted.pending());
		assertEquals(List.of("grant Read " + ROLE_READER),
			describe(_editor.storedRules(SecurityDefinitionEditor.entryName(owner))));

		grants.setGranted(row, READ, false);
		Cell lifted = grants.getCell(row, READ);
		assertFalse("Revoking the last role lifts the restriction.", lifted.restricted());
		assertFalse(lifted.pending());
		assertEquals("The underlying layers do not restrict the attribute, so nothing is stored.", List.of(),
			describe(_editor.storedRules(SecurityDefinitionEditor.entryName(owner))));
	}

	/**
	 * A role granted an operation on the type that cannot be held there gets a row, so that its
	 * grant can be revoked.
	 */
	public void testIneffectiveRole() throws Exception {
		TypeGrants grants = grants(DEAD_GRANT);
		BoundedRole ghost = BoundedRole.getRoleByName(ROLE_GHOST);
		assertEquals(List.of(reader()), grants.getRoles());
		assertEquals(List.of(ghost), grants.getIneffectiveRoles());

		Row row = new Row(null, ghost);
		assertTrue(grants.getRows().contains(row));
		assertTrue(grants.isIneffective(row));
		assertFalse(grants.isIneffective(new Row(null, reader())));
		Cell read = grants.getCell(row, READ);
		assertTrue(read.granted());
		assertTrue("The grant can be revoked.", read.editable());

		grants.setGranted(row, READ, false);
		Cell revoked = grants.getCell(row, READ);
		assertFalse(revoked.granted());
		assertTrue(revoked.pending());
	}

	/**
	 * The label of an access parent names the model parts by their labels, not by their
	 * qualified names.
	 */
	public void testAccessParentLabel() throws Exception {
		TypeGrants delegating = grants(EXPLICIT_PART);
		String label = Resources.getInstance().getString(delegating.getAccessParent().getLabel());
		assertFalse(label, label.contains(EXPLICIT_PARTS));
		assertTrue("A part without a label is named by its name: " + label, label.contains("#explicitParts"));
	}

	public void testTypesWithoutGrants() throws Exception {
		TypeGrants delegating = grants(EXPLICIT_PART);
		assertNotNull(delegating.getAccessParent());
		assertEquals(List.of(), delegating.getRows());
		assertEquals(List.of(), delegating.getOperations());

		TypeGrants exempt = grants(UNSECURED);
		assertTrue(exempt.isExempt());
		assertEquals(List.of(), exempt.getRows());
	}

	private TypeGrants grants(String typeName) {
		SecurityCoverageAnalysis analysis = SecurityCoverageCheck.getInstance().newAnalysis();
		return new TypeGrants(analysis, ModelAccessRights.getInstance(), _editor, type(typeName));
	}

	private static List<String> describe(List<AccessRule> rules) {
		return rules.stream().map(TestTypeGrants::describe).toList();
	}

	private static String describe(AccessRule rule) {
		String kind = rule instanceof AccessRevoke ? AccessRevoke.TAG_NAME : AccessGrant.TAG_NAME;
		StringBuilder result = new StringBuilder(kind).append(' ').append(rule.getOperation().id());
		for (RoleConfig role : rule.getRoles()) {
			result.append(' ').append(role.getName());
		}
		return result.toString();
	}

	private static BoundedRole reader() {
		return BoundedRole.getRoleByName(ROLE_READER);
	}

	private static TLClass type(String name) {
		return (TLClass) TLModelUtil.findType(name);
	}

	private static TLStructuredTypePart attribute(String name) {
		return (TLStructuredTypePart) TLModelUtil.findPart(name);
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestTypeGrants.class),
			SecurityCoverageCheck.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
