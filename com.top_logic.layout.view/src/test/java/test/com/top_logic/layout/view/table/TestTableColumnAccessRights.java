/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.table.AttributeCellEditing;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProvider;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterState;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Tests that the columns of a table follow the model access rights of the current user, see
 * {@link ModelAccessPolicy#onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)} and
 * {@link ModelAccessPolicy#onAttributeOfType(BoundCommandGroup, com.top_logic.model.TLStructuredType, TLStructuredTypePart)}.
 *
 * <p>
 * A cell whose value the user may not read on its row shows the placeholder
 * {@link com.top_logic.layout.react.I18NConstants#MASKED_VALUE} instead of its value, a column whose attribute the user
 * may read on no row is not offered, and a cell is edited only where the user may write it - the
 * condition the edited tables (row edit and composition table) ask through
 * {@link AttributeCellEditing#canEdit(Object)}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTableColumnAccessRights extends AbstractModelAccessTest {

	/** Name of the {@link #STEP} attribute only {@link #ROLE_RESPONSIBLE} may write. */
	private static final String NOTE = "note";

	/** Name of the computed column reading {@link #BUDGET} through TL-Script. */
	private static final String COMPUTED_BUDGET = "computedBudget";

	/** Name of the embedded column showing the name of the {@link #SPONSOR}. */
	private static final String SPONSOR_NAME = SPONSOR + ".name";

	/** The columns of a table filtering {@link #BUDGET} and {@link #SPONSOR} by a script. */
	private static final String SCRIPTED_COLUMNS = "<columns>"
		+ "<column attribute='" + BUDGET + "'>"
		+ "<filter class='com.top_logic.layout.view.table.ScriptedFilter' model='" + MODULE + ":" + PROJECT
		+ "' match='true'/>"
		+ "</column>"
		+ "<column attribute='" + SPONSOR + "'>"
		+ "<filter class='com.top_logic.layout.view.table.ScriptedFilter' model='" + MODULE + ":" + PROJECT
		+ "' match='true'/>"
		+ "</column>"
		+ "</columns>";

	/** Root tag of the parsed column declarations. */
	private static final String COLUMNS_TAG = "columns";

	private static final String BUDGET_VALUE = "budget-of-project";

	private static final String OWN_BUDGET_VALUE = "budget-of-own";

	private static final String OWN_SPONSOR_NAME = "sponsor-of-own";

	/** The placeholder an unreadable cell shows. */
	private static final String MASKED = "*****";

	private static final String COLUMNS = "<columns>"
		+ "<column attribute='" + NAME + "'/>"
		+ "<column attribute='" + BUDGET + "'/>"
		+ "<column attribute='" + CLASSIFIED + "'/>"
		+ "<column attribute='" + APPROVED + "'/>"
		+ "<column attribute='" + SPONSOR + "'/>"
		+ "<embedded-columns reference='" + SPONSOR + "'><column attribute='" + NAME + "'/></embedded-columns>"
		+ "<computed-column name='" + COMPUTED_BUDGET + "' type='tl.core:String'>"
		+ "<value>$row.get(`" + MODULE + ":" + PROJECT + "#" + BUDGET + "`)</value>"
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
			_project.tUpdateByName(APPROVED, Boolean.TRUE);
			_project.tUpdateByName(SPONSOR, _category);
			ownSponsor.tUpdateByName(READER, _roleless);
			_own.tUpdateByName(RESPONSIBLE, _roleless);
			_own.tUpdateByName(BUDGET, OWN_BUDGET_VALUE);
			_own.tUpdateByName(APPROVED, Boolean.FALSE);
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
	 * A cell whose attribute the user may not read on its row shows the placeholder, the cell of a
	 * row the user may read it on shows the value; sorting and filtering read the same value.
	 */
	public void testCellValueFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup budget = column(BUDGET);

		assertSame("The value is not read on a row the user may not read it on.", ColumnProviderService.UNREADABLE,
			budget.value().apply(_project));
		assertEquals(OWN_BUDGET_VALUE, budget.value().apply(_own));

		Column<Object, ?> column = budget.buildColumn();
		assertSame(ColumnProviderService.UNREADABLE, column.value(_project));
		assertMasked(column.renderCell(_project));
		assertEquals("The searched text is the placeholder.", MASKED, column.searchText(_project));
		assertEquals(OWN_BUDGET_VALUE, column.value(_own));
		assertTrue(String.valueOf(column.searchText(_own)).contains(OWN_BUDGET_VALUE));
	}

	/**
	 * An unreadable boolean cell shows the placeholder, not an unchecked checkbox: whether the value
	 * is {@code false} or not readable must be told apart.
	 */
	public void testUnreadableBooleanCell() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup approved = column(APPROVED);
		Column<Object, ?> column = approved.buildColumn();

		assertSame(ColumnProviderService.UNREADABLE, approved.value().apply(_project));
		assertMasked(column.renderCell(_project));
		assertEquals(MASKED, column.searchText(_project));
		assertFalse("A readable cell is displayed by its checkbox.",
			column.renderCell(_own) instanceof CellContent.Text);
	}

	/**
	 * An unreadable reference cell shows the placeholder, not an empty cell.
	 */
	public void testUnreadableReferenceCell() throws ConfigurationException {
		becomeUser(_roleless);
		Column<Object, ?> column = column(SPONSOR).buildColumn();

		assertSame(ColumnProviderService.UNREADABLE, column.value(_project));
		assertMasked(column.renderCell(_project));
		assertEquals(MASKED, column.searchText(_project));
		assertEquals(OWN_SPONSOR_NAME, column.searchText(_own));
	}

	/**
	 * The display of a value in a table cell - also where an edited table shows a read-only cell -
	 * shows the placeholder for an unreadable value, whatever the attribute's type.
	 */
	public void testUnreadableDisplayContent() throws ConfigurationException {
		becomeUser(_roleless);
		for (String name : List.of(BUDGET, APPROVED, SPONSOR)) {
			ColumnSetup setup = column(name);
			assertMasked(ColumnProviderService.displayContent(setup.type(), ColumnProviderService.UNREADABLE));
		}
		assertEquals(MASKED, ColumnProviderService.label(ColumnProviderService.UNREADABLE));
		assertEquals(MASKED + ", " + OWN_SPONSOR_NAME,
			ColumnProviderService.label(List.of(ColumnProviderService.UNREADABLE, OWN_SPONSOR_NAME)));
	}

	/**
	 * An unreadable cell sorts after every value, in a column of every kind.
	 */
	public void testUnreadableSortsLast() throws ConfigurationException {
		becomeUser(_roleless);
		assertSortsLast(column(BUDGET).buildColumn());
		assertSortsLast(column(APPROVED).buildColumn());
		assertSortsLast(column(SPONSOR).buildColumn());
	}

	/**
	 * A comparator declared for a column never receives the marker of an unreadable cell, neither
	 * as the value of a cell nor as an element of a collection ordered element by element: an
	 * unreadable cell sorts after every value.
	 */
	public void testDeclaredComparatorNeverSeesUnreadable() throws ConfigurationException {
		becomeUser(_roleless);
		Comparator<Object> declared = (value1, value2) -> {
			assertFalse("Marker passed to a declared comparator.",
				ColumnProviderService.isUnreadable(value1) || ColumnProviderService.isUnreadable(value2));
			return String.valueOf(value1).compareTo(String.valueOf(value2));
		};
		ColumnSetup budget = column(BUDGET);
		assertSortsLast(new ColumnSetup(budget.name(), budget.label(), budget.type(), budget.value(),
			budget.viewContext(), budget.binding(), budget.width(), budget.editing(), budget.aggregate(), declared,
			budget.hiddenByDefault()).buildColumn());

		Comparator<Object> elementwise = ColumnSetup.elementwise(declared);
		assertTrue(elementwise.compare(List.of(ColumnProviderService.UNREADABLE), List.of(OWN_SPONSOR_NAME)) > 0);
		assertTrue(elementwise.compare(List.of(OWN_SPONSOR_NAME), List.of(ColumnProviderService.UNREADABLE)) < 0);
	}

	/**
	 * A column filter never matches an unreadable cell through its value: a boolean filter
	 * accepting {@code true} or {@code false}, and a text filter searching the value, do not see
	 * it, and the cell offers no filter state selecting its value.
	 */
	public void testUnreadableNotFiltered() throws ConfigurationException {
		becomeUser(_roleless);
		Column<Object, ?> approved = column(APPROVED).buildColumn();
		assertFalse(matches(approved, new BooleanFilterState(true, false, false), _project));
		assertFalse(matches(approved, new BooleanFilterState(false, true, true), _project));
		assertTrue(matches(approved, new BooleanFilterState(false, true, true), _own));

		Column<Object, ?> budget = column(BUDGET).buildColumn();
		assertFalse(matches(budget, TextFilterState.contains(BUDGET_VALUE), _project));
		assertTrue(matches(budget, TextFilterState.contains(OWN_BUDGET_VALUE), _own));
		assertNull(filterOf(approved).stateFor(ColumnProviderService.UNREADABLE));
	}

	/**
	 * A cell the user may not read is not edited, whatever the attribute's type, even where the
	 * user may write it.
	 */
	public void testUnreadableCellNotEditable() {
		becomeUser(_roleless);
		assertFalse(new AttributeCellEditing(APPROVED).canEdit(new TLObjectOverlay(_project)));
		assertFalse(new AttributeCellEditing(SPONSOR).canEdit(new TLObjectOverlay(_project)));
		assertFalse(new AttributeCellEditing(BUDGET).canEdit(new TLObjectOverlay(_project)));
	}

	/**
	 * A cell of an attribute without a read grant of its own is decided by the read grant of the
	 * row's type: a row is only known to be discoverable, which releases none of its attributes.
	 */
	public void testCellWithoutAttributeGrant() throws ConfigurationException {
		TLObject foreign = newObject(qualified(PROJECT), "foreign");
		becomeUser(_roleless);
		ColumnSetup name = column(NAME);

		assertEquals("The viewer may read the project.", "project", name.value().apply(_project));
		assertSame("No role on the project.", ColumnProviderService.UNREADABLE, name.value().apply(foreign));
		assertMasked(name.buildColumn().renderCell(foreign));
	}

	/**
	 * The grant of an attribute replaces the grant of the type for this attribute: an operation the
	 * type grants to no role is possible on an attribute granting it to a role, and a refusal
	 * depends on the object then.
	 */
	public void testAttributeGrantReplacesTypeGrant() {
		TLStructuredTypePart categoryName = part(CATEGORY, NAME);
		TLStructuredTypePart categoryReader = part(CATEGORY, READER);
		BoundCommandGroup write = SimpleBoundCommandGroup.WRITE;

		becomeUser(_responsible);
		assertTrue(ModelAccessPolicy.onAttributeOfType(write, type(CATEGORY), categoryName).isExecutable());
		assertTrue(ModelAccessPolicy.onAttribute(write, _category, categoryName).isExecutable());
		ExecutableState refused = ModelAccessPolicy.onAttribute(write, _hiddenCategory, categoryName);
		assertTrue("Refused on the object: disabled, not hidden.", refused.isDisabled());
		assertTrue(ModelAccessPolicy.onObject(write, _hiddenCategory, categoryName, null).isDisabled());

		// Without a grant of its own, the type decides, which grants writing to no role.
		assertTrue(ModelAccessPolicy.onAttributeOfType(write, type(CATEGORY), categoryReader).isHidden());
		assertTrue(ModelAccessPolicy.onAttribute(write, _category, categoryReader).isHidden());
	}

	/**
	 * A cell of a row the user edits in an overlay follows the rights on the edited object.
	 */
	public void testOverlayRowFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup budget = column(BUDGET);

		assertSame(ColumnProviderService.UNREADABLE, budget.value().apply(new TLObjectOverlay(_project)));
		assertEquals(OWN_BUDGET_VALUE, budget.value().apply(new TLObjectOverlay(_own)));
	}

	/**
	 * A reference the user may not read on a row leads nowhere: the embedded cell is empty.
	 */
	public void testEmbeddedNavigationFollowsReadRight() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup sponsorName = column(SPONSOR_NAME);

		assertSame("The reference leading to the sponsor may not be read.", ColumnProviderService.UNREADABLE,
			sponsorName.value().apply(_project));
		assertEquals(OWN_SPONSOR_NAME, sponsorName.value().apply(_own));

		Column<Object, ?> column = sponsorName.buildColumn();
		assertMasked(column.renderCell(_project));
		assertEquals(MASKED, column.searchText(_project));
		assertSortsLast(column);
	}

	/**
	 * A column filtered by a script sorts an unreadable cell after every value - not by the text of
	 * its placeholder, which comes before every letter.
	 */
	public void testScriptedFilterColumnSortsUnreadableLast() throws ConfigurationException {
		becomeUser(_roleless);
		List<ColumnSetup> columns = columns(SCRIPTED_COLUMNS);
		for (String name : List.of(BUDGET, SPONSOR)) {
			Column<Object, ?> column = find(columns, name).buildColumn();
			assertMasked(column.renderCell(_project));
			assertSortsLastBy(column, _project, _own);
		}
	}

	/**
	 * A column built by a {@link ColumnProvider} of an application shows and searches an unreadable
	 * cell by the placeholder. The provider is given the readable values only: its sort and filter
	 * see an unreadable cell as an empty one.
	 */
	public void testMappedProviderShowsPlaceholder() throws ConfigurationException {
		becomeUser(_roleless);
		ColumnSetup budget = column(BUDGET);
		Column<Object, ?> column = mappedService().createColumn(BUDGET, ResKey.text(BUDGET), budget.type(),
			budget.value());

		assertMasked(column.renderCell(_project));
		assertEquals(MASKED, column.searchText(_project));
		assertNull("The provider sees an empty cell.", column.value(_project));
		assertEquals(OWN_BUDGET_VALUE, column.value(_own));
		assertEquals("provided:" + OWN_BUDGET_VALUE, ((CellContent.Text) column.renderCell(_own)).text());
	}

	/**
	 * A {@link ColumnProviderService} building the columns of strings by {@link StubProvider}.
	 */
	private static ColumnProviderService mappedService() throws ConfigurationException {
		ColumnProviderService.Config config = TypedConfiguration.newConfigItem(ColumnProviderService.Config.class);
		config.setImplementationClass(ColumnProviderService.class);
		ColumnProviderService.ProviderMapping mapping =
			TypedConfiguration.newConfigItem(ColumnProviderService.ProviderMapping.class);
		TLModelPartRef stringType = TLModelPartRef.ref("tl.core:String");
		mapping.update(mapping.descriptor().getProperty(ColumnProviderService.ProviderMapping.TYPE), stringType);
		mapping.update(mapping.descriptor().getProperty("impl"),
			TypedConfiguration.createConfigItemForImplementationClass(StubProvider.class));
		config.getProviders().put(stringType, mapping);
		return new ColumnProviderService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config) {
			{
				startUp();
			}
		};
	}

	/**
	 * A {@link ColumnProvider} of an application, knowing nothing of unreadable values.
	 */
	public static class StubProvider implements ColumnProvider {

		/**
		 * Creates a {@link StubProvider}.
		 */
		@CalledByReflection
		public StubProvider(InstantiationContext context, PolymorphicConfiguration<?> config) {
			// No configuration.
		}

		@Override
		public Column<Object, ?> createColumn(String name, ResKey label, ColumnType type,
				Function<Object, Object> value) {
			return DefaultColumn.<Object, Object> builder(name, value)
				.label(label)
				.renderer(cellValue -> CellContent.text("provided:" + cellValue))
				.sort(() -> Comparator.comparing(String::valueOf))
				.build();
		}
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
	 * The cells of a row added to a composition table (a draft) are decided by the access parent of
	 * the draft, its container.
	 */
	public void testDraftRow() {
		AttributeCellEditing note = new AttributeCellEditing(NOTE);
		AttributeCellEditing secret = new AttributeCellEditing(SECRET);
		AttributeCellEditing name = new AttributeCellEditing(NAME);

		becomeUser(_responsible);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(STEP), _project);
		assertTrue(note.canEdit(draft));
		assertTrue(name.canEdit(draft));
		assertFalse(secret.canEdit(draft));

		becomeUser(_roleless);
		assertFalse("The user holds no role on the container.", note.canEdit(draft));
		assertTrue(name.canEdit(draft));
	}

	private static void assertMasked(CellContent content) {
		assertTrue("Not a plain text: " + content, content instanceof CellContent.Text);
		assertEquals(MASKED, ((CellContent.Text) content).text());
		assertEquals(MASKED, Resources.getInstance().getString(com.top_logic.layout.react.I18NConstants.MASKED_VALUE));
	}

	/**
	 * Sorting the rows of the given column puts the unreadable row after the readable one, as the
	 * table orders rows: by the column's comparator, an empty cell last.
	 */
	private static <V> void assertSortsLastBy(Column<Object, V> column, Object unreadableRow, Object readableRow) {
		Comparator<Object> rows = Comparator.comparing(column::value,
			Comparator.nullsLast(column.sort().orElseThrow().comparator()));
		List<Object> sorted = new ArrayList<>(List.of(unreadableRow, readableRow));
		sorted.sort(rows);
		assertEquals(column.name(), List.of(readableRow, unreadableRow), sorted);
	}

	private <V> void assertSortsLast(Column<Object, V> column) {
		V unreadable = column.value(_project);
		V readable = column.value(_own);
		assertNotNull("A readable value to compare with in " + column.name() + ".", readable);
		Comparator<V> comparator = column.sort().orElseThrow().comparator();
		assertTrue(column.name(), comparator.compare(unreadable, readable) > 0);
		assertTrue(column.name(), comparator.compare(readable, unreadable) < 0);
		assertEquals(column.name(), 0, comparator.compare(unreadable, unreadable));
		List<V> values = new ArrayList<>(List.of(unreadable, readable));
		values.sort(comparator);
		assertSame(column.name(), ColumnProviderService.UNREADABLE, values.get(1));
	}

	private static <V> boolean matches(Column<Object, V> column, FilterState state, Object row) {
		return filterOf(column).predicate(state).test(column.value(row));
	}

	private static <V> ColumnFilter<V> filterOf(Column<Object, V> column) {
		return column.filter().orElseThrow();
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
		return columns(COLUMNS);
	}

	private static List<ColumnSetup> columns(String columns) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableColumnAccessRights.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(COLUMNS_TAG, TypedConfiguration.getConfigurationDescriptor(ColumnsConfig.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(columns));
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
