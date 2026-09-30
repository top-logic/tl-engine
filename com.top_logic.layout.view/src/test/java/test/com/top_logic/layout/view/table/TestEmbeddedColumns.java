/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.layout.view.table.EmbeddedColumns;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test for the {@code <embedded-columns>} of a table: the object a row reaches, the columns its type
 * contributes, and how they are named, labelled and read as columns of the row.
 */
public class TestEmbeddedColumns extends TestCase {

	/** The module holding the test model. */
	private static final String MODULE = "test.embeddedColumns";

	/** The type of the table's rows. */
	private TLClass _rowType;

	/** The type an assignee of a row is of. */
	private TLClass _personType;

	/** The type a contact of a person is of. */
	private TLClass _contactType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_rowType = TLModelUtil.addClass(module, "Row");
		_personType = TLModelUtil.addClass(module, "Person");
		_contactType = TLModelUtil.addClass(module, "Contact");
		TLClass valueType = TLModelUtil.addClass(module, "Value");

		TLModelUtil.addProperty(_rowType, "subject", valueType);
		TLModelUtil.addProperty(_personType, "name", valueType);
		TLModelUtil.addProperty(_contactType, "email", valueType);
		TLModelUtil.addProperty(_contactType, "phone", valueType);

		TLModelUtil.addReference(_rowType, "assignee", _personType, null);
		TLModelUtil.addReference(_rowType, "members", _personType, null);
		TLModelUtil.addReference(_personType, "contact", _contactType, null);
		TLReference members = reference(_rowType, "members");
		members.setMultiple(true);
		members.setOrdered(true);
	}

	/**
	 * The columns of the object a reference leads to are named and labelled after that reference,
	 * and read their values through it.
	 */
	public void testReferenceEmbedsTheColumnsOfItsTarget() {
		ColumnSetup setup = column(embedded("assignee", attributeColumn("name")), "assignee.name");

		assertEquals("The embedded column keeps the type of the attribute it shows.",
			_personType.getPart("name"), setup.type().part());
		assertEquals("The label of the embedded column starts with the label of the reference.",
			prefixed(TLModelNamingConvention.resourceKey(reference(_rowType, "assignee")),
				TLModelNamingConvention.resourceKey(_personType.getPart("name"))),
			setup.label());
		assertEquals("The value is read from the object the row points to.", "Alice",
			setup.value().apply(row(person("Alice", null))));
	}

	/**
	 * A path of references reaches an object further away, and every step of the path names and
	 * labels the columns it leads to.
	 */
	public void testPathNavigatesEveryStep() {
		ColumnSetup setup =
			column(embedded("assignee.contact", attributeColumn("email")), "assignee.contact.email");

		assertEquals(prefixed(
			prefixed(TLModelNamingConvention.resourceKey(reference(_rowType, "assignee")),
				TLModelNamingConvention.resourceKey(reference(_personType, "contact"))),
			TLModelNamingConvention.resourceKey(_contactType.getPart("email"))), setup.label());
		assertEquals("alice@example.com",
			setup.value().apply(row(person("Alice", contact("alice@example.com")))));
	}

	/**
	 * A row leading nowhere shows nothing, wherever the path ends.
	 */
	public void testMissingObjectOnTheWayShowsNothing() {
		ColumnSetup setup =
			column(embedded("assignee.contact", attributeColumn("email")), "assignee.contact.email");

		assertNull("The row has no assignee.", setup.value().apply(row(null)));
		assertNull("The assignee has no contact.", setup.value().apply(row(person("Alice", null))));
	}

	/**
	 * Where the path leads over a reference holding several objects, every embedded column shows
	 * the value of each of them.
	 */
	public void testMultipleStepShowsEveryValue() {
		ColumnSetup setup = column(embedded("members.contact", attributeColumn("email")),
			"members.contact.email");

		assertTrue("A cell holds the value of every object the row reaches.",
			setup.type().multiple());

		TLObject row = object(_rowType);
		row.tUpdate(reference(_rowType, "members"),
			List.of(person("Alice", contact("alice@example.com")),
				person("Bob", contact("bob@example.com"))));

		assertEquals(List.of("alice@example.com", "bob@example.com"), setup.value().apply(row));
	}

	/**
	 * An embedding declaring no columns shows the main properties of the embedded type, and all of
	 * its non-hidden attributes where it names none.
	 */
	public void testEmbeddingWithoutColumnsShowsTheMainProperties() {
		assertEquals("Without main properties, everything the type holds is embedded.",
			List.of("assignee.name", "assignee.contact"), names(embedded("assignee")));

		MainProperties mainProperties = TypedConfiguration.newConfigItem(MainProperties.class);
		set(mainProperties, MainProperties.PROPERTIES, List.of("name"));
		_personType.setAnnotation(mainProperties);

		assertEquals("The main properties of the embedded type are what it shows, the rest is offered.",
			List.of("assignee.name", "assignee.contact"), names(embedded("assignee")));
		assertEquals("Only what the embedded type does not present itself by starts out hidden.",
			List.of("assignee.contact"), hidden(embedded("assignee")));
	}

	/**
	 * What the embedded type holds beyond the declared columns is offered in the column selection.
	 */
	public void testUndeclaredAttributesAreOffered() {
		List<ColumnSetup> columns = resolve(embedded("assignee.contact", attributeColumn("email")));

		assertEquals(List.of("assignee.contact.email", "assignee.contact.phone"), names(columns));
		assertEquals("The declared column is shown, the rest of the type is offered.",
			List.of("assignee.contact.phone"), hidden(columns));
	}

	/**
	 * An embedded object is displayed through the row, not edited through it - whatever the
	 * embedded column would offer on its own.
	 */
	public void testEmbeddedColumnsAreNotEdited() {
		assertNotNull("A column over an attribute is edited by writing that attribute.",
			resolve(attributeColumn("subject")).get(0).editing());

		for (ColumnSetup setup : resolve(embedded("assignee", attributeColumn("name")))) {
			assertNull("The embedded column " + setup.name() + " is displayed, not edited.",
				setup.editing());
		}
	}

	/**
	 * An embedding inside an embedding composes: the columns are named and labelled after the whole
	 * path and read through it.
	 */
	public void testEmbeddingsNest() {
		EmbeddedColumns.Config inner = embedded("contact", attributeColumn("email"));
		EmbeddedColumns.Config outer = embedded("assignee", inner);

		ColumnSetup setup = column(outer, "assignee.contact.email");

		assertEquals("The columns of a nested embedding are named after the whole path.",
			List.of("assignee.contact.email"), declaredNames(outer));
		assertEquals("alice@example.com",
			setup.value().apply(row(person("Alice", contact("alice@example.com")))));
	}

	/**
	 * What an embedded column declares about itself reaches the table: its label overrides the
	 * attribute's, and it sorts the table under its embedded name.
	 */
	public void testNestedDeclarationsKeepWhatTheyDeclare() {
		AttributeColumn.Config name = attributeColumn("name");
		set(name, AttributeColumn.Config.LABEL, ResKey.text("Assignee"));
		set(name, AttributeColumn.Config.WIDTH, Integer.valueOf(180));
		set(name, AttributeColumn.Config.SORT, SortDirection.DESC);
		EmbeddedColumns.Config embedding = embedded("assignee", name);

		ColumnSetup setup = column(embedding, "assignee.name");

		assertEquals("The declared label is prefixed like any other.",
			prefixed(TLModelNamingConvention.resourceKey(reference(_rowType, "assignee")),
				ResKey.text("Assignee")),
			setup.label());
		assertEquals(180, setup.width());
		assertEquals("The embedded column sorts the table under its embedded name.",
			List.of(new SortColumn("assignee.name", false)),
			instantiate(embedding).get(0).defaultSort());
	}

	/**
	 * A declared label replaces what the path would say, and prefixes the embedded columns instead.
	 */
	public void testDeclaredLabelReplacesThePathPrefix() {
		EmbeddedColumns.Config embedding = embedded("assignee.contact", attributeColumn("email"));
		set(embedding, EmbeddedColumns.Config.LABEL, ResKey.text("Contact"));

		assertEquals(prefixed(ResKey.text("Contact"),
			TLModelNamingConvention.resourceKey(_contactType.getPart("email"))),
			column(embedding, "assignee.contact.email").label());
	}

	/**
	 * An embedding computing the object it shows embeds the columns of the type it declares, under
	 * the name and the label it declares.
	 */
	public void testComputedEmbeddingUsesItsDeclaredType() {
		EmbeddedColumns.Config embedding = computedEmbedding(MODULE + ":Contact", attributeColumn("email"));

		ColumnSetup setup = column(embedding, "contact.email");

		assertEquals("The declared type says which columns are embedded.",
			_contactType.getPart("email"), setup.type().part());
		assertEquals(prefixed(ResKey.text("Contact"),
			TLModelNamingConvention.resourceKey(_contactType.getPart("email"))), setup.label());
		assertEquals("The columns are named after the declared name.",
			List.of("contact.email"), declaredNames(embedding));
	}

	/**
	 * An embedding whose function yields a collection reaches several objects, so every embedded
	 * column holds the value of each of them - exactly as a multi-valued reference step does.
	 *
	 * @see #testMultipleStepShowsEveryValue() The values such a column holds.
	 */
	public void testComputedEmbeddingReachesSeveralObjects() {
		EmbeddedColumns.Config embedding = computedEmbedding(MODULE + ":Contact", attributeColumn("email"));
		set(embedding, EmbeddedColumns.Config.MULTIPLE, Boolean.TRUE);

		assertTrue("A cell holds the value of every object the function yields.",
			column(embedding, "contact.email").type().multiple());

		assertFalse("An embedding reaches one object unless it says otherwise.",
			column(computedEmbedding(MODULE + ":Contact", attributeColumn("email")), "contact.email")
				.type().multiple());
	}

	/**
	 * The path of a reference embedding says how many objects it reaches, so only a computed
	 * embedding declares it.
	 */
	public void testOnlyAComputedEmbeddingDeclaresItsMultiplicity() {
		EmbeddedColumns.Config embedding = embedded("assignee", attributeColumn("name"));
		set(embedding, EmbeddedColumns.Config.MULTIPLE, Boolean.TRUE);

		assertContains("only by a computed embedding", errors(embedding));
	}

	/**
	 * A path naming something the row type does not hold - or holds as something other than a
	 * reference - is reported: such columns would stay empty and nothing would say why.
	 */
	public void testUnresolvableStepIsReported() {
		try {
			resolve(embedded("assignee.missing", attributeColumn("email")));
			fail("The step is not held by the type it is resolved against.");
		} catch (IllegalStateException ex) {
			assertContains("missing", ex.getMessage());
			assertContains("assignee.missing", ex.getMessage());
		}

		try {
			resolve(embedded("subject", attributeColumn("email")));
			fail("The step is not a reference.");
		} catch (IllegalStateException ex) {
			assertContains("subject", ex.getMessage());
		}
	}

	/**
	 * An embedding says which object it shows in exactly one way: through a reference, or through a
	 * computed object of a declared type.
	 */
	public void testTargetIsDeclaredExactlyOnce() {
		EmbeddedColumns.Config both = embedded("assignee", attributeColumn("name"));
		set(both, EmbeddedColumns.Config.TYPE, TLModelPartRef.ref(MODULE + ":Person"));
		set(both, EmbeddedColumns.Config.OBJECT, TypedConfiguration.newConfigItem(Expr.Null.class));

		assertContains("not both", errors(both));

		EmbeddedColumns.Config neither = TypedConfiguration.newConfigItem(EmbeddedColumns.Config.class);
		assertContains("must say which object they show", errors(neither));

		EmbeddedColumns.Config withoutType =
			TypedConfiguration.newConfigItem(EmbeddedColumns.Config.class);
		set(withoutType, EmbeddedColumns.Config.OBJECT, TypedConfiguration.newConfigItem(Expr.Null.class));
		assertContains("needs both", errors(withoutType));
	}

	/**
	 * A computed embedding has no reference to take a name and a label from and declares both.
	 */
	public void testComputedEmbeddingDeclaresNameAndLabel() {
		EmbeddedColumns.Config config = computedEmbedding(MODULE + ":Contact", attributeColumn("email"));
		set(config, EmbeddedColumns.Config.NAME, null);
		set(config, EmbeddedColumns.Config.LABEL, null);

		String errors = errors(config);
		assertContains("name", errors);
		assertContains("label", errors);
	}

	/** The single column of the given declaration under the given name. */
	private ColumnSetup column(PolymorphicConfiguration<? extends ColumnDeclaration> config, String name) {
		for (ColumnSetup setup : resolve(config)) {
			if (name.equals(setup.name())) {
				return setup;
			}
		}
		fail("No column '" + name + "' among " + names(resolve(config)) + ".");
		return null;
	}

	/** The columns the given declaration contributes, in display order. */
	private List<ColumnSetup> resolve(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		return ColumnDeclarations.resolve(instantiate(config), new ColumnResolution(_rowType, null));
	}

	/** The names of the given columns, in display order. */
	private static List<String> names(List<ColumnSetup> columns) {
		return columns.stream().map(ColumnSetup::name).toList();
	}

	/** The names of the columns the given declaration contributes, in display order. */
	private List<String> names(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		return names(resolve(config));
	}

	/** The names of the given columns that start out hidden, in display order. */
	private static List<String> hidden(List<ColumnSetup> columns) {
		return List.copyOf(ColumnDeclarations.hiddenByDefault(columns));
	}

	/** The names of the columns of the given declaration that start out hidden. */
	private List<String> hidden(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		return hidden(resolve(config));
	}

	/** The names the given declaration contributes without being resolved. */
	private List<String> declaredNames(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		return ColumnDeclarations.declaredNames(instantiate(config));
	}

	/** The label of an embedded column: what the path says, and the column's own label. */
	private static ResKey prefixed(ResKey prefix, ResKey label) {
		return I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN.fill(prefix, label);
	}

	/** An {@code <embedded-columns>} over the given path of references. */
	@SafeVarargs
	private static EmbeddedColumns.Config embedded(String reference,
			PolymorphicConfiguration<? extends ColumnDeclaration>... columns) {
		EmbeddedColumns.Config config = TypedConfiguration.newConfigItem(EmbeddedColumns.Config.class);
		set(config, EmbeddedColumns.Config.REFERENCE, reference);
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : columns) {
			config.getColumns().add(column);
		}
		return config;
	}

	/** An {@code <embedded-columns>} over a computed object of the given type. */
	@SafeVarargs
	private static EmbeddedColumns.Config computedEmbedding(String type,
			PolymorphicConfiguration<? extends ColumnDeclaration>... columns) {
		EmbeddedColumns.Config config = TypedConfiguration.newConfigItem(EmbeddedColumns.Config.class);
		set(config, EmbeddedColumns.Config.NAME, "contact");
		set(config, EmbeddedColumns.Config.TYPE, TLModelPartRef.ref(type));
		set(config, EmbeddedColumns.Config.OBJECT, TypedConfiguration.newConfigItem(Expr.Null.class));
		set(config, EmbeddedColumns.Config.LABEL, ResKey.text("Contact"));
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : columns) {
			config.getColumns().add(column);
		}
		return config;
	}

	/** A {@code <column>} over the given attribute. */
	private static AttributeColumn.Config attributeColumn(String attribute) {
		AttributeColumn.Config config = TypedConfiguration.newConfigItem(AttributeColumn.Config.class);
		set(config, AttributeColumn.Config.ATTRIBUTE, attribute);
		return config;
	}

	/** The instantiated declaration, failing the test when it is reported. */
	private static List<ColumnDeclaration> instantiate(
			PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		BufferingProtocol log = new BufferingProtocol();
		List<ColumnDeclaration> result =
			ColumnDeclarations.instantiate(new DefaultInstantiationContext(log), columns(config));
		assertEquals("The declaration is accepted.", List.of(), log.getErrors());
		return result;
	}

	/** What is reported when the given declaration is instantiated. */
	private static String errors(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		BufferingProtocol log = new BufferingProtocol();
		ColumnDeclarations.instantiate(new DefaultInstantiationContext(log), columns(config));
		assertFalse("The declaration is reported.", log.getErrors().isEmpty());
		return String.join("\n", log.getErrors());
	}

	/** The {@code <columns>} of a table declaring the given column. */
	private static ColumnsConfig columns(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		ColumnsConfig result = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		result.getColumns().add(config);
		return result;
	}

	/** A row pointing to the given assignee, or to nobody. */
	private TLObject row(TLObject assignee) {
		TLObject result = object(_rowType);
		if (assignee != null) {
			result.tUpdate(reference(_rowType, "assignee"), assignee);
		}
		return result;
	}

	/** A person of the given name, holding the given contact. */
	private TLObject person(String name, TLObject contact) {
		TLObject result = object(_personType);
		result.tUpdate(_personType.getPart("name"), name);
		if (contact != null) {
			result.tUpdate(reference(_personType, "contact"), contact);
		}
		return result;
	}

	/** A contact holding the given mail address. */
	private TLObject contact(String email) {
		TLObject result = object(_contactType);
		result.tUpdate(_contactType.getPart("email"), email);
		return result;
	}

	/** An object of the given type, living only in this test. */
	private static TLObject object(TLClass type) {
		return TransientObjectFactory.INSTANCE.createObject(type, null);
	}

	/** The reference of the given name held by the given type. */
	private static TLReference reference(TLStructuredType type, String name) {
		TLStructuredTypePart part = type.getPart(name);
		return (TLReference) part;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	private static void assertContains(String expected, String actual) {
		assertTrue("Expected '" + expected + "' in: " + actual, actual != null && actual.contains(expected));
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService} every column is built through, the
	 * {@link AttributeSettings} the test model is built with, and the {@link CompatibilityService}
	 * the transient objects read and write their values through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestEmbeddedColumns.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE));
	}

}
