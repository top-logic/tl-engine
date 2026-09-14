/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.kbbased.storage.mappings.BooleanMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.FloatMapping;
import com.top_logic.element.meta.kbbased.storage.mappings.IntMapping;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ScriptedFilter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.filter.TextColumnFilter;

/**
 * Test for the width a {@link ColumnProviderService} column is displayed in: the one configured for
 * the kind of attribute the column shows.
 */
public class TestColumnProviderService extends TestCase {

	/** The type the columns under test show attributes of. */
	private TLClass _rowType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.columnWidth");
		_rowType = TLModelUtil.addClass(module, "Row");

		TLEnumeration priority = TLModelUtil.addEnumeration(module, "Priority");
		TLModelUtil.addClassifier(priority, "low");
		TLModelUtil.addClassifier(priority, "high");

		addProperty("flag", datatype(module, "Boolean", Kind.BOOLEAN, BooleanMapping.INSTANCE));
		addProperty("state", datatype(module, "Tristate", Kind.TRISTATE, directMapping(Boolean.class)));
		addProperty("count", datatype(module, "Integer", Kind.INT, IntMapping.INSTANCE));
		addProperty("amount", datatype(module, "Float", Kind.FLOAT, FloatMapping.INSTANCE));
		addProperty("day", dateType(module, "Date", null));
		addProperty("startTime", dateType(module, "Time", "TIME"));
		addProperty("timestamp", dateType(module, "DateTime", "DATE_TIME"));
		addProperty("name", datatype(module, "String", Kind.STRING, directMapping(String.class)));
		addProperty("priority", priority);
		addProperty("owner", _rowType);
		addProperty("color", datatype(module, "Color", Kind.CUSTOM, directMapping(Integer.class)));

		TLProperty tags = TLModelUtil.addProperty(_rowType, "tags",
			datatype(module, "Tag", Kind.STRING, directMapping(String.class)));
		tags.setMultiple(true);
	}

	/** The configured widths, the seam an application retunes the column widths through. */
	private static ColumnProviderService.Config config() {
		return ColumnProviderService.getInstance().getConfig();
	}

	public void testBooleanWidth() {
		assertWidth("A truth value is shown in the boolean width.", config().getBooleanWidth(), "flag");
		assertWidth("A tri-state truth value is shown in the boolean width, too.",
			config().getBooleanWidth(), "state");
	}

	public void testNumberWidth() {
		assertWidth("A whole number is shown in the number width.", config().getNumberWidth(), "count");
		assertWidth("A fractional number is shown in the number width, too.", config().getNumberWidth(), "amount");
	}

	public void testTemporalWidths() {
		assertWidth("A date is shown in the date width.", config().getDateWidth(), "day");
		assertWidth("A time of day is shown in the time width.", config().getTimeWidth(), "startTime");
		assertWidth("A date with a time of day is shown in the date-time width.",
			config().getDateTimeWidth(), "timestamp");
	}

	public void testStringWidth() {
		assertWidth("A text is shown in the string width.", config().getStringWidth(), "name");
	}

	public void testEnumerationWidth() {
		assertWidth("A classifier is shown in the enumeration width.", config().getEnumerationWidth(), "priority");
	}

	public void testLabelWidth() {
		assertWidth("A reference is shown by the display label of its values, in the label width.",
			config().getLabelWidth(), "owner");
		assertWidth("An application-defined datatype is shown by its display label, in the label width.",
			config().getLabelWidth(), "color");
		assertWidth("A multi-valued attribute is shown by the display label of its values, in the label width.",
			config().getLabelWidth(), "tags");
	}

	public void testUnresolvedAttributeHasTheLabelWidth() {
		Column<Object, ?> column = ColumnProviderService.getInstance()
			.createColumn("unresolved", ResKey.text("Unresolved"), null);

		assertEquals("An attribute whose type is unresolved is shown by its display label, in the label width.",
			config().getLabelWidth(), column.defaultWidth());
	}

	/**
	 * A column bringing a filter of its own is displayed in the width of its kind of attribute -
	 * the filter decides what the column finds, not how wide it is.
	 */
	public void testCustomFilterColumnKeepsTheKindWidth() {
		assertEquals("A truth value with a filter of its own is shown in the boolean width.",
			config().getBooleanWidth(), customFilterColumn("flag").defaultWidth());
		assertEquals("A date with a time of day and a filter of its own is shown in the date-time width.",
			config().getDateTimeWidth(), customFilterColumn("timestamp").defaultWidth());
		assertEquals("A reference with a filter of its own is shown in the label width.",
			config().getLabelWidth(), customFilterColumn("owner").defaultWidth());
	}

	/**
	 * A binding building a column of its own - a {@link ScriptedFilter} is one - asks for the width
	 * the attribute's kind gives, so such a column is as wide as the type-derived one.
	 */
	public void testABindingBuildingItsOwnColumnAsksForTheKindWidth() {
		ColumnProviderService service = ColumnProviderService.getInstance();

		assertEquals("A whole number gives the number width.",
			config().getNumberWidth(), service.defaultWidth(part("count")));
		assertEquals("An unresolved attribute gives the label width.",
			config().getLabelWidth(), service.defaultWidth(null));

		assertEquals("The column such a binding builds is displayed in that width.",
			config().getNumberWidth(), ownColumn("count", 0).defaultWidth());
		assertEquals("A width configured at the column still wins.",
			220, ownColumn("count", 220).defaultWidth());
	}

	/**
	 * The column of a binding that builds its own, as {@code <table>} builds it: through the setup,
	 * which applies a width configured at the column.
	 */
	private Column<Object, ?> ownColumn(String attribute, int configuredWidth) {
		ColumnBinding binding = setup -> DefaultColumn.builder(setup.attribute(),
			row -> ColumnProviderService.attributeValue(row, setup.attribute()))
			.label(setup.label())
			.width(ColumnProviderService.getInstance().defaultWidth(setup.part()))
			.build();
		return new ColumnSetup(attribute, ResKey.text(attribute), part(attribute), null, binding, configuredWidth)
			.buildColumn();
	}

	/** The widths an application starts out with, before it configures any of its own. */
	public void testShippedWidths() {
		assertEquals(80, config().getBooleanWidth());
		assertEquals(100, config().getNumberWidth());
		assertEquals(110, config().getDateWidth());
		assertEquals(90, config().getTimeWidth());
		assertEquals(160, config().getDateTimeWidth());
		assertEquals(120, config().getEnumerationWidth());
		assertEquals(150, config().getStringWidth());
		assertEquals(150, config().getLabelWidth());
	}

	private void assertWidth(String message, int expectedWidth, String attribute) {
		assertEquals(message, expectedWidth, column(attribute).defaultWidth());
	}

	/** The type-derived column over the given attribute of the row type. */
	private Column<Object, ?> column(String attribute) {
		return ColumnProviderService.getInstance().createColumn(attribute, ResKey.text(attribute), part(attribute));
	}

	/** The column over the given attribute that matches a search term against the cell's text. */
	private Column<Object, ?> customFilterColumn(String attribute) {
		return ColumnProviderService.getInstance().createColumn(attribute, ResKey.text(attribute), part(attribute),
			TextColumnFilter.forStrings());
	}

	private TLStructuredTypePart part(String attribute) {
		TLStructuredTypePart part = _rowType.getPart(attribute);
		assertNotNull("No attribute '" + attribute + "' in the test model.", part);
		return part;
	}

	private void addProperty(String name, TLType valueType) {
		TLModelUtil.addProperty(_rowType, name, valueType);
	}

	/**
	 * A datatype of the given kind, whose values the given mapping translates.
	 */
	private static TLPrimitive datatype(TLModule module, String name, Kind kind, StorageMapping<?> mapping) {
		return TLModelUtil.addDatatype(module, module, name, kind, mapping);
	}

	/**
	 * A datatype holding a point in time, annotated as the given configuration type - what says
	 * whether it is a date, a time of day or a date with a time of day.
	 *
	 * @param configType
	 *        The {@link ConfigType} value, or {@code null} for a plain date, which needs none.
	 */
	private static TLPrimitive dateType(TLModule module, String name, String configType)
			throws ConfigurationException {
		TLPrimitive type = datatype(module, name, Kind.DATE, directMapping(Date.class));
		if (configType != null) {
			ConfigType annotation = TypedConfiguration.newConfigItem(ConfigType.class);
			annotation.setValue(configType);
			type.setAnnotation(annotation);
		}
		return type;
	}

	/**
	 * A mapping storing the values as they are, as the core datatypes of that application type use
	 * it.
	 */
	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService} that builds the columns under test and
	 * the {@link AttributeSettings} its display of an attribute value consults.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnProviderService.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE));
	}

}
