/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.table.AttributeCellEditing;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.table.Column;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Tests that the columns of a table follow the model access rights of the current user, see
 * {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)} and
 * {@link ModelAccessPolicy#onAttributeOfType(BoundCommandGroup, com.top_logic.model.TLStructuredType, TLStructuredTypePart)}.
 *
 * <p>
 * A cell whose value the user may not read on its row is empty, a column whose attribute the user
 * may read on no row is not offered, and a cell is edited only where the user may write it - the
 * condition the edited tables (row edit and composition table) ask through
 * {@link AttributeCellEditing#canEdit(Object)}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTableColumnAccessRights extends AbstractModelAccessTest {

	/** Name of the {@link #TASK} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE = "note";

	/** Name of the computed column reading {@link #BUDGET} through TL-Script. */
	private static final String COMPUTED_BUDGET = "computedBudget";

	/** Name of the embedded column showing the name of the {@link #SPONSOR}. */
	private static final String SPONSOR_NAME = SPONSOR + ".name";

	/** Root tag of the parsed column declarations. */
	private static final String COLUMNS_TAG = "columns";

	private static final String BUDGET_VALUE = "budget-of-project";

	private static final String OWN_BUDGET_VALUE = "budget-of-own";

	private static final String OWN_SPONSOR_NAME = "sponsor-of-own";

	private static final String COLUMNS = "<columns>"
		+ "<column attribute='" + NAME + "'/>"
		+ "<column attribute='" + BUDGET + "'/>"
		+ "<column attribute='" + CLASSIFIED + "'/>"
		+ "<embedded-columns reference='" + SPONSOR + "'><column attribute='" + NAME + "'/></embedded-columns>"
		+ "<computed-column name='" + COMPUTED_BUDGET + "' type='tl.core:String'>"
		+ "<value>row -> $row.get(`" + MODULE + ":" + PROJECT + "#" + BUDGET + "`)</value>"
		+ "</computed-column>"
		+ "</columns>";

	/**
	 * A project the {@link #_roleless} user is responsible for; on {@link #_project} the same user
	 * is viewer only.
	 */
	private TLObject _own;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_own = newObject(qualified(PROJECT), "own");
		TLObject ownSponsor = newObject(qualified(CATEGORY), OWN_SPONSOR_NAME);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			_project.tUpdateByName(BUDGET, BUDGET_VALUE);
			_project.tUpdateByName(CLASSIFIED, "classified");
			_project.tUpdateByName(SPONSOR, _category);
			ownSponsor.tUpdateByName(READER, _roleless);
			_own.tUpdateByName(RESPONSIBLE, _roleless);
			_own.tUpdateByName(BUDGET, OWN_BUDGET_VALUE);
			_own.tUpdateByName(SPONSOR, ownSponsor);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_own = null;
		super.tearDown();
	}

	/**
	 * A cell whose attribute the user may not read on its row is empty, the cell of a row the user
	 * may read it on shows the value; sorting and filtering read the same value.
	 */
	public void testCellValueFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup budget = column(BUDGET);

		assertNull("The value is not read on a row the user may not read it on.", budget.value().apply(_project));
		assertEquals(OWN_BUDGET_VALUE, budget.value().apply(_own));

		Column<Object, ?> column = budget.buildColumn();
		assertNull("The displayed value is empty.", column.value(_project));
		assertFalse("The searched text does not reveal the value.",
			String.valueOf(column.searchText(_project)).contains(BUDGET_VALUE));
		assertEquals(OWN_BUDGET_VALUE, column.value(_own));
		assertTrue(String.valueOf(column.searchText(_own)).contains(OWN_BUDGET_VALUE));
	}

	/**
	 * A cell of a row the user edits in an overlay follows the rights on the edited object.
	 */
	public void testOverlayRowFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup budget = column(BUDGET);

		assertNull(budget.value().apply(new TLObjectOverlay(_project)));
		assertEquals(OWN_BUDGET_VALUE, budget.value().apply(new TLObjectOverlay(_own)));
	}

	/**
	 * A reference the user may not read on a row leads nowhere: the embedded cell is empty.
	 */
	public void testEmbeddedNavigationFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup sponsorName = column(SPONSOR_NAME);

		assertNull(sponsorName.value().apply(_project));
		assertEquals(OWN_SPONSOR_NAME, sponsorName.value().apply(_own));
	}

	/**
	 * A computed column reads through TL-Script, which applies the read right itself.
	 */
	public void testComputedColumnReadsWithRights() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup computed = column(COMPUTED_BUDGET);

		Object refused = computed.value().apply(_project);
		assertTrue("Got: " + refused, refused == null || "".equals(refused));
		assertEquals(OWN_BUDGET_VALUE, computed.value().apply(_own));
	}

	/**
	 * A column whose attribute the user may read on no row is not offered; the others are.
	 */
	public void testColumnReadByNoRoleNotOffered() throws ConfigurationException {
		becomeUser(_responsible);
		List<String> names = names(columns());

		assertFalse(names.contains(CLASSIFIED));
		assertTrue(names.contains(BUDGET));
		assertTrue(names.contains(NAME));
	}

	/**
	 * The columns withheld from the user are told apart from columns the table does not declare.
	 */
	public void testWithheldColumns() throws ConfigurationException {
		becomeUser(_responsible);
		List<Column<?, ?>> built = columns().stream().<Column<?, ?>> map(ColumnSetup::buildColumn).toList();

		assertEquals(Set.of(CLASSIFIED),
			ColumnDeclarations.withheld(List.of(NAME, BUDGET, CLASSIFIED, COMPUTED_BUDGET), built));
	}

	/**
	 * The columns a table derives from its row type follow the same rule.
	 */
	public void testDerivedColumnsFollowReadRight() {
		becomeUser(_responsible);
		List<String> names = names(ColumnDeclarations.resolve(ColumnDeclarations.mainColumns(type(PROJECT)),
			new ColumnResolution(type(PROJECT), null)));

		assertFalse(names.contains(CLASSIFIED));
		assertTrue(names.contains(BUDGET));
	}

	/**
	 * A user bypassing the model security is offered every column and reads every value.
	 */
	public void testRoot() throws ConfigurationException {
		becomeUser(_root);
		List<ColumnSetup> columns = columns();

		assertTrue(names(columns).contains(CLASSIFIED));
		assertEquals(BUDGET_VALUE, find(columns, BUDGET).value().apply(_project));
	}

	/**
	 * A cell is edited only where the user may write it on the row, also on the overlay of an
	 * edited row.
	 */
	public void testCellEditFollowsWriteRight() {
		AttributeCellEditing name = new AttributeCellEditing(NAME);
		AttributeCellEditing secret = new AttributeCellEditing(SECRET);
		AttributeCellEditing rating = new AttributeCellEditing(RATING);

		becomeUser(_responsible);
		assertTrue(name.canEdit(_project));
		assertTrue(name.canEdit(new TLObjectOverlay(_project)));
		assertFalse("Granted to no role.", secret.canEdit(_project));
		assertFalse("Not granted on this object.", rating.canEdit(new TLObjectOverlay(_project)));

		becomeUser(_roleless);
		assertFalse("The viewer may not write the project.", name.canEdit(new TLObjectOverlay(_project)));
		assertTrue(name.canEdit(new TLObjectOverlay(_own)));

		becomeUser(_root);
		assertTrue(secret.canEdit(new TLObjectOverlay(_project)));
	}

	/**
	 * A cell the user may write but not read is not edited: editing would show the value.
	 */
	public void testCellEditNeedsReadRight() {
		becomeUser(_roleless);
		assertFalse(new AttributeCellEditing(BUDGET).canEdit(new TLObjectOverlay(_project)));
	}

	/**
	 * The cells of a row added to a composition table (a draft) are decided in the creation
	 * context, the container of the draft.
	 */
	public void testDraftRow() {
		AttributeCellEditing note = new AttributeCellEditing(NOTE);
		AttributeCellEditing secret = new AttributeCellEditing(SECRET);
		AttributeCellEditing name = new AttributeCellEditing(NAME);

		becomeUser(_responsible);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(TASK), _project);
		assertTrue(note.canEdit(draft));
		assertTrue(name.canEdit(draft));
		assertFalse(secret.canEdit(draft));

		becomeUser(_roleless);
		assertFalse("The user holds no role in the creation context.", note.canEdit(draft));
		assertTrue(name.canEdit(draft));
	}

	private ColumnSetup column(String name) throws ConfigurationException {
		return find(columns(), name);
	}

	private static ColumnSetup find(List<ColumnSetup> columns, String name) {
		for (ColumnSetup column : columns) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new AssertionError("No column '" + name + "' in " + names(columns) + ".");
	}

	private static List<ColumnSetup> columns() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableColumnAccessRights.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(COLUMNS_TAG, TypedConfiguration.getConfigurationDescriptor(ColumnsConfig.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(COLUMNS));
		ColumnsConfig config = (ColumnsConfig) reader.read();
		List<ColumnDeclaration> declarations = ColumnDeclarations.instantiate(context, config);
		context.checkErrors();
		return ColumnDeclarations.resolve(declarations, new ColumnResolution(type(PROJECT), null));
	}

	private static List<String> names(List<ColumnSetup> columns) {
		return columns.stream().map(ColumnSetup::name).toList();
	}

	/**
	 * The test suite, additionally starting the services building the columns.
	 */
	public static Test suite() {
		return suiteWith(TestTableColumnAccessRights.class, TypeIndex.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE);
	}
}
