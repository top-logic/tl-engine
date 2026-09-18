/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.format.DecimalFormatDefinition;
import com.top_logic.basic.format.PatternBasedFormatDefinition;
import com.top_logic.basic.format.SimpleDateFormatDefinition;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.annotate.ui.Format;
import com.top_logic.model.annotate.ui.FormatBase;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ComparableColumnFilter;

/**
 * Test for the format a point in time is displayed in: the {@link Format} annotation of the
 * attribute, the one of its type, or the user's default format, and its use for the cells of a
 * column and the bounds of its filter.
 */
public class TestDateFormat extends TestCase {

	/** The pattern of the format annotated at the type. */
	private static final String TYPE_PATTERN = "yyyy-MM";

	/** The pattern of the format annotated at an attribute. */
	private static final String ATTRIBUTE_PATTERN = "yyyy/MM/dd HH:mm";

	/** The type the attributes under test belong to. */
	private TLClass _rowType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.dateFormat");
		_rowType = TLModelUtil.addClass(module, "Row");

		TLPrimitive date = dateType(module, "Date", null);
		TLPrimitive dateTime = dateType(module, "DateTime", "DATE_TIME");
		TLPrimitive stamp = dateType(module, "Stamp", "DATE_TIME");
		stamp.setAnnotation(customDate(TYPE_PATTERN));

		addProperty("day", date);
		addProperty("timestamp", dateTime);
		addProperty("timestampCustom", dateTime).setAnnotation(customDate(ATTRIBUTE_PATTERN));
		addProperty("stamp", stamp);
		addProperty("stampCustom", stamp).setAnnotation(customDate("dd.MM."));
		addProperty("broken", dateTime).setAnnotation(decimal("#0.00"));
		addProperty("days", date).setMultiple(true);

		TLProperty daysCustom = addProperty("daysCustom", date);
		daysCustom.setAnnotation(customDate(ATTRIBUTE_PATTERN));
		daysCustom.setMultiple(true);
	}

	public void testAnUnannotatedAttributeUsesTheUsersFormat() {
		assertEquals("A date is shown in the user's date format.",
			HTMLFormatter.getInstance().getDateFormat(), dateFormat("day"));
		assertEquals("A date with a time of day is shown in the user's date-time format.",
			HTMLFormatter.getInstance().getDateTimeFormat(), dateFormat("timestamp"));
	}

	public void testAnAnnotatedAttributeIsWrittenInItsFormat() throws ParseException {
		DateFormat format = dateFormat("timestampCustom");
		Date value = format.parse("2026/09/17 10:23");
		assertEquals("2026/09/17 10:23", format.format(value));
	}

	public void testTheTypeAnnotationAppliesWhereTheAttributeHasNone() {
		assertEquals(TYPE_PATTERN, pattern(dateFormat("stamp")));
	}

	public void testTheAttributeAnnotationWinsOverTheType() {
		assertEquals("dd.MM.", pattern(dateFormat("stampCustom")));
	}

	public void testAFormatThatWritesNoDatesFallsBackToTheUsersFormat() {
		assertEquals("A misconfigured attribute still shows its value.",
			HTMLFormatter.getInstance().getDateTimeFormat(), dateFormat("broken"));
	}

	public void testACollectionOfDatesIsWrittenInTheFormatOfOneDate() {
		assertEquals("Every point in time of the collection is shown in the user's date format.",
			HTMLFormatter.getInstance().getDateFormat(), dateFormat("days"));
		assertEquals("...and an annotated format applies to each of them, too.",
			ATTRIBUTE_PATTERN, pattern(dateFormat("daysCustom")));
	}

	/**
	 * A cell holding several points in time writes each of them in the attribute's format and
	 * separates them, exactly as the value list displaying them does.
	 */
	public void testACollectionOfDatesIsSearchedByAllOfItsValues() throws ParseException {
		DateFormat format = dateFormat("daysCustom");
		Date first = format.parse("2026/09/17 10:23");
		Date second = format.parse("2026/10/01 08:00");

		assertEquals("2026/09/17 10:23, 2026/10/01 08:00",
			column("daysCustom").searchText(List.of(first, second)));
	}

	public void testAFilterBoundIsAcceptedInTheAnnotatedAndInTheDefaultFormat() throws ParseException {
		BoundCodec<Date> codec = boundCodec(column("timestampCustom"));
		DateFormat format = dateFormat("timestampCustom");
		Date value = format.parse("2026/09/17 10:23");

		assertEquals("A bound is written the way the column shows its values.",
			"2026/09/17 10:23", codec.format(value));
		assertEquals("A bound typed the way the column shows its values is accepted.",
			value, codec.parse("2026/09/17 10:23"));
		assertEquals("A bound typed in the user's default format is accepted as well.",
			value, codec.parse(HTMLFormatter.getInstance().getDateTimeFormat().format(value)));
		assertNull("Text in neither format is no bound.", codec.parse("17.9.2026, 10 o'clock"));
	}

	public void testACellIsSearchedByTheTextItShows() throws ParseException {
		Date value = dateFormat("timestampCustom").parse("2026/09/17 10:23");
		assertEquals("2026/09/17 10:23", column("timestampCustom").searchText(value));
	}

	private DateFormat dateFormat(String attribute) {
		return FieldControlService.dateFormat(part(attribute));
	}

	private static String pattern(DateFormat format) {
		assertTrue("Expected a pattern-based format, got: " + format, format instanceof SimpleDateFormat);
		return ((SimpleDateFormat) format).toPattern();
	}

	/**
	 * The type-derived column over the given attribute, whose rows are the cell values themselves.
	 */
	private Column<Object, ?> column(String attribute) {
		return ColumnProviderService.getInstance().createColumn(attribute, ResKey.text(attribute),
			ColumnType.of(part(attribute)), row -> row);
	}

	@SuppressWarnings("unchecked")
	private static BoundCodec<Date> boundCodec(Column<Object, ?> column) {
		ComparableColumnFilter<Date> filter = (ComparableColumnFilter<Date>) column.filter().get();
		return filter.codec();
	}

	private TLStructuredTypePart part(String attribute) {
		TLStructuredTypePart part = _rowType.getPart(attribute);
		assertNotNull("No attribute '" + attribute + "' in the test model.", part);
		return part;
	}

	private TLProperty addProperty(String name, TLPrimitive valueType) {
		return TLModelUtil.addProperty(_rowType, name, valueType);
	}

	/**
	 * A datatype holding a point in time, annotated as the given configuration type.
	 *
	 * @param configType
	 *        The {@link ConfigType} value, or {@code null} for a plain date, which needs none.
	 */
	private static TLPrimitive dateType(TLModule module, String name, String configType)
			throws ConfigurationException {
		TLPrimitive type = TLModelUtil.addDatatype(module, module, name, Kind.DATE, directMapping(Date.class));
		if (configType != null) {
			ConfigType annotation = TypedConfiguration.newConfigItem(ConfigType.class);
			annotation.setValue(configType);
			type.setAnnotation(annotation);
		}
		return type;
	}

	/** A {@link Format} annotation writing dates in the given pattern. */
	private static Format customDate(String pattern) {
		return format(SimpleDateFormatDefinition.Config.class, pattern);
	}

	/** A {@link Format} annotation writing numbers in the given pattern. */
	private static Format decimal(String pattern) {
		return format(DecimalFormatDefinition.Config.class, pattern);
	}

	private static Format format(Class<? extends ConfigurationItem> definitionType, String pattern) {
		ConfigurationItem definition = TypedConfiguration.newConfigItem(definitionType);
		definition.update(definition.descriptor().getProperty(PatternBasedFormatDefinition.Config.PATTERN), pattern);
		Format annotation = TypedConfiguration.newConfigItem(Format.class);
		annotation.update(annotation.descriptor().getProperty(FormatBase.DEFINITION), definition);
		return annotation;
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
			ServiceTestSetup.createSetup(TestDateFormat.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE));
	}

}
