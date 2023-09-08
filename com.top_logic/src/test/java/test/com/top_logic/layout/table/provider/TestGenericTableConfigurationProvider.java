/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.provider;

import static com.top_logic.layout.table.provider.GenericTableConfigurationProvider.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.control.FirstLineRenderer;
import com.top_logic.layout.form.control.TextPopupControl;
import com.top_logic.layout.provider.DefaultLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.table.provider.generic.TableConfigModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link GenericTableConfigurationProvider}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestGenericTableConfigurationProvider extends BasicTestCase {

	private static final Class<TestGenericTableConfigurationProvider> THIS_CLASS =
		TestGenericTableConfigurationProvider.class;

	private static final String TL_CLASS_1 = "tlClass1";

	private static final String TL_CLASS_2 = "tlClass2";

	private static final String MAIN_PROPERTY_1 = "mainProperty1";

	private static final String MAIN_PROPERTY_2 = "mainProperty2";

	private static final String MAIN_PROPERTY_3 = "mainProperty3";

	private static final String NON_MAIN_PROPERTY_1 = "nonMainProperty1";

	private static final String NON_MAIN_PROPERTY_2 = "nonMainProperty2";

	private static final String NON_MAIN_PROPERTY_3 = "nonMainProperty3";

	private TLModel _model;

	private TLModule _module;

	private TLPrimitive _primitiveType;

	private TableConfiguration _tableConfig;

	@Override
	protected void setUp() {
		_model = new TLModelImpl();
		_module = TLModelUtil.addModule(_model, "module");
		_primitiveType = TLModelUtil.addDatatype(_module, "primitiveType", IdentityMapping.INSTANCE);
		_tableConfig = newTableConfig();
	}

	@Override
	protected void tearDown() {
		_tableConfig = null;
		_primitiveType = null;
		_module = null;
		_model = null;
	}

	public void testNoProperty() {
		TLClass type = createClass(TL_CLASS_1);
		configureTable(types(type));
		
		// There are no explicit main properties and there is no resource provider for this type
		// registered. Therefore, no self column is synthesized.
		assertVisibleColumns(columns());
	}

	public void testNameProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createProperty(type, NAME_COLUMN);
		configureTable(types(type));
		assertVisibleColumns(columns(NAME_COLUMN));
	}

	public void testMainProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createProperty(type, MAIN_PROPERTY_1);
		setMainProperties(type, MAIN_PROPERTY_1);
		configureTable(types(type));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testNoneMainProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createProperty(type, NON_MAIN_PROPERTY_1);
		configureTable(types(type));
		
		// There are no explicit main properties and there is no resource provider for this type
		// registered. Therefore, no self column is synthesized.
		assertVisibleColumns(columns(NON_MAIN_PROPERTY_1));
	}

	public void testNameAndMainProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createPropertyWithSortOrder(type, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type, MAIN_PROPERTY_1, 2);
		setMainProperties(type, MAIN_PROPERTY_1);
		configureTable(types(type));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));

		// The name column is not displayed by default, but selectable by the user.
		assertEquals(DisplayMode.hidden, _tableConfig.getCol(NAME_COLUMN).getVisibility());
	}

	public void testNameAndNoneMainProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createPropertyWithSortOrder(type, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type, NON_MAIN_PROPERTY_1, 2);
		configureTable(types(type));
		assertVisibleColumns(columns(NAME_COLUMN, NON_MAIN_PROPERTY_1));
	}

	public void testNameAndMainPropertyAndNoneMainProperty() {
		TLClass type = createClass(TL_CLASS_1);
		createPropertyWithSortOrder(type, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type, MAIN_PROPERTY_1, 3);
		createPropertyWithSortOrder(type, NON_MAIN_PROPERTY_1, 2);
		setMainProperties(type, MAIN_PROPERTY_1);
		configureTable(types(type));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testTwoClassesOnlyCommonProperties() {
		TLClass type1 = createClass(TL_CLASS_1);
		createPropertyWithSortOrder(type1, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type1, MAIN_PROPERTY_1, 3);
		createPropertyWithSortOrder(type1, NON_MAIN_PROPERTY_1, 2);
		setMainProperties(type1, MAIN_PROPERTY_1);

		TLClass type2 = createClass(TL_CLASS_2);
		createPropertyWithSortOrder(type2, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type2, MAIN_PROPERTY_1, 3);
		createPropertyWithSortOrder(type2, NON_MAIN_PROPERTY_1, 2);
		setMainProperties(type2, MAIN_PROPERTY_1);

		configureTable(types(type1, type2));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testTwoClassesWithNonCommonProperties() {
		TLClass type1 = createClass(TL_CLASS_1);
		createPropertyWithSortOrder(type1, NAME_COLUMN, 11);
		createPropertyWithSortOrder(type1, MAIN_PROPERTY_1, 15);
		createPropertyWithSortOrder(type1, MAIN_PROPERTY_2, 14);
		createPropertyWithSortOrder(type1, NON_MAIN_PROPERTY_1, 13);
		createPropertyWithSortOrder(type1, NON_MAIN_PROPERTY_2, 12);
		setMainProperties(type1, MAIN_PROPERTY_1, MAIN_PROPERTY_2);

		TLClass type2 = createClass(TL_CLASS_2);
		createPropertyWithSortOrder(type2, NAME_COLUMN, 1);
		createPropertyWithSortOrder(type2, MAIN_PROPERTY_1, 5);
		createPropertyWithSortOrder(type2, MAIN_PROPERTY_3, 4);
		createPropertyWithSortOrder(type2, NON_MAIN_PROPERTY_1, 3);
		createPropertyWithSortOrder(type2, NON_MAIN_PROPERTY_3, 2);
		setMainProperties(type2, MAIN_PROPERTY_1, MAIN_PROPERTY_3);

		configureTable(types(type1, type2));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	/**
	 * Test that a property which is a main property in one class but not in the other is still
	 * visible by default.
	 */
	public void testTwoClassesWithConflictingProperty() {
		TLClass type1 = createClass(TL_CLASS_1);
		createProperty(type1, MAIN_PROPERTY_1);
		createProperty(type1, MAIN_PROPERTY_2);
		setMainProperties(type1, MAIN_PROPERTY_1, MAIN_PROPERTY_2);

		TLClass type2 = createClass(TL_CLASS_2);
		createProperty(type2, MAIN_PROPERTY_1);
		/* This "main property" is used as a non main property in this type: */
		createProperty(type2, MAIN_PROPERTY_2);
		setMainProperties(type2, MAIN_PROPERTY_1);

		configureTable(types(type1, type2));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testClassWithMainPropertyAndClassWithoutMainProperty() {
		TLClass type1 = createClass(TL_CLASS_1);
		createProperty(type1, MAIN_PROPERTY_1);
		createProperty(type1, NON_MAIN_PROPERTY_1);
		setMainProperties(type1, MAIN_PROPERTY_1);

		TLClass type2 = createClass(TL_CLASS_2);
		/* This "main property" is used as a non main property in this type: */
		createProperty(type2, MAIN_PROPERTY_1);
		createProperty(type2, NON_MAIN_PROPERTY_1);

		configureTable(types(type1, type2));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testClassWithMainPropertyAndClassWithoutMainPropertyReverseTypeOrder() {
		TLClass type1 = createClass(TL_CLASS_1);
		createProperty(type1, MAIN_PROPERTY_1);
		createProperty(type1, NON_MAIN_PROPERTY_1);
		setMainProperties(type1, MAIN_PROPERTY_1);

		TLClass type2 = createClass(TL_CLASS_2);
		/* This "main property" is used as a non main property in this type: */
		createProperty(type2, MAIN_PROPERTY_1);
		createProperty(type2, NON_MAIN_PROPERTY_1);

		configureTable(types(type2, type1));
		assertVisibleColumns(columns(MAIN_PROPERTY_1));
	}

	public void testShowVisibleDefaultColumn() throws Exception {
		assertVisibilityTransisition("visibleColumn", DisplayMode.visible, DisplayMode.visible, "visibleColumn");
	}

	public void testShowHiddenDefaultColumn() throws Exception {
		assertVisibilityTransisition("hiddenColumn", DisplayMode.hidden, DisplayMode.visible, "hiddenColumn");
	}

	public void testShowExcludedDefaultColumn() throws Exception {
		assertVisibilityTransisition("excludedColumn", DisplayMode.excluded, DisplayMode.visible, "excludedColumn");
	}

	public void testHideVisibleNonDefaultColumn() throws Exception {
		assertVisibilityTransisition("visibleColumn", DisplayMode.visible, DisplayMode.hidden, "defaultColumn");
	}

	public void testHideHiddenNonDefaultColumn() throws Exception {
		assertVisibilityTransisition("hiddenColumn", DisplayMode.hidden, DisplayMode.hidden, "defaultColumn");
	}

	public void testExcludeExcludedNonDefaultColumn() throws Exception {
		assertVisibilityTransisition("excludedColumn", DisplayMode.excluded, DisplayMode.excluded, "defaultColumn");
	}

	private void assertVisibilityTransisition(String columnName, DisplayMode visibilityBefore,
			DisplayMode visibilityAfter, String defaultColumnName) {
		ColumnConfiguration column = _tableConfig.declareColumn(columnName);
		column.setVisibility(visibilityBefore);
		_tableConfig.setDefaultColumns(Collections.singletonList(defaultColumnName));

		GenericTableConfigurationProvider.showDefaultColumns().adaptConfigurationTo(_tableConfig);

		assertEquals(visibilityAfter, column.getVisibility());
	}

	public void testUseTextPopupControlForEmptyRendererInStringColumn() {
		TLClass tlClass = createMultiLineStringPropertyClass();

		configureTable(types(tlClass));

		ColumnConfiguration columnConfiguration = _tableConfig.getDeclaredColumn(NON_MAIN_PROPERTY_1);
		assertEquals(TextPopupControl.CP.DEFAULT_INSTANCE,
			columnConfiguration.getEditControlProvider());
		assertEquals(FirstLineRenderer.DEFAULT_INSTANCE,
			columnConfiguration.getRenderer());
		assertEquals(DefaultLabelProvider.INSTANCE,
			columnConfiguration.getFullTextProvider());
	}

	public void testNoTextPopupControlForCustomRendererInStringColumn() {
		TLClass tlClass = createMultiLineStringPropertyClass();

		ColumnConfiguration preSetConfiguration = _tableConfig.declareColumn(NON_MAIN_PROPERTY_1);
		preSetConfiguration.setCellRenderer(DefaultCellRenderer.INSTANCE);
		configureTable(types(tlClass));

		ColumnConfiguration columnConfiguration = _tableConfig.getDeclaredColumn(NON_MAIN_PROPERTY_1);
		assertEquals(null,
			columnConfiguration.getEditControlProvider());
		assertEquals(DefaultCellRenderer.INSTANCE,
			columnConfiguration.getCellRenderer());
		assertEquals(MetaResourceProvider.INSTANCE,
			columnConfiguration.getFullTextProvider());
	}

	private TLClass createMultiLineStringPropertyClass() {
		TLPrimitive stringType = TLModelUtil.addDatatype(_module, _module, "newString", Kind.STRING);
		TLClass tlClass = createClass(TL_CLASS_1);
		TLClassProperty nonMainProperty = TLModelUtil.addProperty(tlClass, NON_MAIN_PROPERTY_1, stringType);
		MultiLine stringDisplay = TypedConfiguration.newConfigItem(MultiLine.class);
		stringDisplay.setValue(true);
		nonMainProperty.setAnnotation(stringDisplay);
		return tlClass;
	}

	private TLClass createClass(String name) {
		return TLModelUtil.addClass(_module, name);
	}

	private TLClassProperty createPropertyWithSortOrder(TLClass type, String propertyName, double sortOrder) {
		TLClassProperty property = createProperty(type, propertyName);
		DisplayAnnotations.setSortOrder(property, sortOrder);
		return property;
	}

	private TLClassProperty createProperty(TLClass type, String propertyName) {
		return TLModelUtil.addProperty(type, propertyName, _primitiveType);
	}

	private void setMainProperties(TLClass type, String... mainProperties) {
		MainProperties annotation = TypedConfiguration.newConfigItem(MainProperties.class);
		for (String mainProperty : mainProperties) {
			annotation.getProperties().add(mainProperty);
		}
		type.setAnnotation(annotation);
	}

	private Set<TLClass> types(TLClass... typeNames) {
		return new HashSet<>(Arrays.asList(typeNames));
	}

	private List<String> columns(String... columnNames) {
		return Arrays.asList(columnNames);
	}

	private TableConfiguration newTableConfig() {
		return TableConfigurationFactory.table();
	}

	private void configureTable(Set<TLClass> types) {
		new GenericTableConfigurationProvider(types).adaptConfigurationTo(_tableConfig);
	}

	private void assertVisibleColumns(List<String> additionalColumns) {
		ArrayList<String> expectedColumns = new ArrayList<>(additionalColumns);
		expectedColumns.add(0, TableControl.SELECT_COLUMN_NAME);
		assertEquals(expectedColumns, getVisibleColumns());
	}

	private List<String> getVisibleColumns() {
		Collection<? extends ColumnConfiguration> declaredColumns = _tableConfig.getDeclaredColumns();
		List<ColumnConfiguration> visibleColumns = new ArrayList<>();
		for (ColumnConfiguration column : declaredColumns) {
			if (column.isVisible()) {
				visibleColumns.add(column);
			}
		}
		return getColumnNames(visibleColumns);
	}

	private List<String> getColumnNames(Collection<? extends ColumnConfiguration> columns) {
		List<String> columnNames = new ArrayList<>();
		for (ColumnConfiguration columnConfig : columns) {
			columnNames.add(columnConfig.getName());
		}
		return columnNames;
	}

	public static Test suite() {
		Test innerSetup = ServiceTestSetup.createSetup(THIS_CLASS,
			TableConfigurationFactory.Module.INSTANCE,
			ModelService.Module.INSTANCE,
			TableConfigModelService.Module.INSTANCE);
		return KBSetup.getSingleKBTest(innerSetup);
	}

}
