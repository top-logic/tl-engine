/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.filter.SimpleBooleanFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.ExecutableTableDataExport;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link TestCase} for {@link TableConfigurationFactory}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTableConfigurationFactory extends BasicTestCase {

	public void testTableConfigToTableConfiguration() throws ConfigurationException {
		TableConfig config = TypedConfiguration.newConfigItem(TableConfig.class);
		config.setTableName("tableName");
		config.setColumnCustomization(ColumnCustomization.ORDER);
		config.setDefaultFilterProvider(polyConfig(SimpleBooleanFilterProvider.class));
		List<SortConfig> sortOrder = new ArrayList<>();
		sortOrder.add(TypedConfiguration.newConfigItem(SortConfig.class));
		sortOrder.add(TypedConfiguration.newConfigItem(SortConfig.class));
		config.setDefaultSortOrder(sortOrder);
		config.setFilterDisplayChildren(true);
		config.setFilterDisplayParents(true);
		config.setFooterStyle("background-color: green; height:12px;");
		config.setHeaderStyle("background-color: red; height:13px;");
		config.setMaxColumns(156);
		config.setModelMapping(polyConfig(TestingNullMapping.class));
		config.setMultiSort(Enabled.always);
		config.setIDColumn("column1");
		config.setDefaultColumns(Arrays.asList("column1", "column"));
		config.setPageSizeOptions(12, 13, 16);
		config.setRowClassProvider(polyConfig(TestingRowClassProvider.class));
		config.setRowObjectResourceProvider(polyConfig(TestingRowObjectResourceProvider.class));
		config.setRowStyle("background-color: yellow; height:14px;");
		config.setShowColumnHeader(false);
		config.setShowFooter(false);
		config.setShowTitle(false);
		config.setTitleKey(ResKey.forTest("titleKey"));
		config.setTitleStyle("background-color: blue; height:15px;");
		config.setExporter(polyConfig(TestingTableExport.class));
		config.setSupportsMultipleSettings(true);
		config.setMultipleSettingsKey("someFunnyKey");

		TableConfiguration table =
			TestTableConfigurationFactory.build(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		assertEquals(config.getTableName(), table.getTableName());
		assertEquals(config.getColumnCustomization(), table.getColumnCustomization());
		assertEquals(config.getDefaultFilterProvider().getImplementationClass(),
			table.getDefaultFilterProvider().getClass());
		assertEquals(config.getIDColumn(), table.getIDColumn());
		assertEquals(config.getDefaultSortOrder(), table.getDefaultSortOrder());
		assertEquals(config.getFilterDisplayChildren(), table.getFilterDisplayChildren());
		assertEquals(config.getFilterDisplayParents(), table.getFilterDisplayParents());
		assertEquals(config.getFooterStyle(), table.getFooterStyle());
		assertEquals(12, table.getFooterHeight());
		assertEquals(config.getHeaderStyle(), table.getHeaderStyle());
		assertEquals(13, table.getHeaderRowHeight());
		assertEquals(config.getMaxColumns(), table.getMaxColumns());
		assertEquals(config.getModelMapping().getImplementationClass(), table.getModelMapping().getClass());
		assertEquals(config.getMultiSort(), table.getMultiSort());
		assertEquals(config.getDefaultColumns(), table.getDefaultColumns());
		assertEquals(config.getPageSizeOptions(), table.getPageSizeOptions());
		assertEquals(config.getRowClassProvider().getImplementationClass(), table.getRowClassProvider().getClass());
		assertEquals(config.getRowObjectResourceProvider().getImplementationClass(), table.getRowObjectResourceProvider().getClass());
		assertEquals(config.getRowStyle(), table.getRowStyle());
		assertEquals(14, table.getRowHeight());
		assertEquals(config.isShowColumnHeader(), table.getShowColumnHeader());
		assertEquals(config.isShowFooter(), table.getShowFooter());
		assertEquals(config.isShowTitle(), table.getShowTitle());
		assertEquals(config.getTitleStyle(), table.getTitleStyle());
		assertEquals(15, table.getTitleHeight());
		assertEquals(config.getTitleKey(), table.getTitleKey());
		assertEquals(config.getExporter().getImplementationClass(), table.getExporter().getClass());
		assertEquals(config.getSupportsMultipleSettings(), table.getSupportMultipleSettings());
		assertEquals(config.getMultipleSettingsKey(), table.getMultipleSettingsKey());
	}

	public static <T> PolymorphicConfiguration<T> polyConfig(Class<T> type) throws ConfigurationException {
		@SuppressWarnings("unchecked")
		Class<PolymorphicConfiguration<T>> configType = (Class) DefaultConfigConstructorScheme.getFactory(type)
				.getConfigurationInterface();
		PolymorphicConfiguration<T> result = TypedConfiguration.newConfigItem(configType);
		result.setImplementationClass(type);
		return result;
	}

	public static final class TestingTableExport extends ExecutableTableDataExport {

		@Override
		public BinaryData createExportData(TableData table, String name) throws IOException {
			return new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM, name);
		}

		@Override
		public HandlerResult exportTableData(DisplayContext context, TableData table) {
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	public static final class TestingRowObjectResourceProvider extends AbstractResourceProvider {

		@Override
		public String getLabel(Object object) {
			return "row_" + MetaLabelProvider.INSTANCE.getLabel(object);
		}

	}

	public static final class TestingRowClassProvider implements RowClassProvider {
		@Override
		public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
			return "trClass";
		}
	}

	public static final class TestingNullMapping implements Mapping<Object, TLObject> {
		@Override
		public TLObject map(Object input) {
			return null;
		}
	}

	public static class TableConfigurationProviderForTest extends NoDefaultColumnAdaption {

		static final String SOME_DYNAMIC_COLUMN = "someDynamicColumn";

		@Override
		public void adaptConfigurationTo(TableConfiguration table) {
			table.declareColumn(SOME_DYNAMIC_COLUMN);
		}

	}

	public void testConfigurationProviders() throws ConfigurationException {
		TableConfig config = TypedConfiguration.newConfigItem(TableConfig.class);
		config.getConfigurationProviders().add(polyConfig(TableConfigurationProviderForTest.class));

		TableConfiguration table =
			TestTableConfigurationFactory.build(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		assertNotNull(table.getDeclaredColumn(TableConfigurationProviderForTest.SOME_DYNAMIC_COLUMN));
	}

	public void testCombine() {
		TableConfigurationProvider p1 = new TableConfigurationProvider() {
			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				defaultColumn.setCssClass("default");
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration foo = table.declareColumn("foo");
				foo.setCssClass("foo1");
				ColumnConfiguration bar = table.declareColumn("bar");
				bar.setCssClass("bar");
			}
		};
		TableConfigurationProvider p2 = new NoDefaultColumnAdaption() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration foo = table.declareColumn("foo");
				foo.setCssClass("foo2");
				ColumnConfiguration baz = table.declareColumn("baz");
				baz.setCssClass("baz");
			}
		};

		check(
			TableConfigurationFactory.apply(
				TableConfigurationFactory.combine(p1, p2),
			TableConfigurationFactory.table()));
		check(TableConfigurationFactory.apply(
			TableConfigurationFactory.combine(TableConfigurationFactory.combine(null, p1),
				TableConfigurationFactory.combine(p2, (TableConfigurationProvider) null)),
			TableConfigurationFactory.table()));
		check(TableConfigurationFactory.apply(
			TableConfigurationFactory.combine(TableConfigurationFactory.combine(null, p1),
				TableConfigurationFactory.combine(p2, TableConfigurationFactory.toProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, (TableConfig) null))),
			TableConfigurationFactory.table()));
		check(TableConfigurationFactory.apply(
			TableConfigurationFactory.combine(null, p1, null, p2, null),
			TableConfigurationFactory.table()));
		check(TableConfigurationFactory.apply(
			TableConfigurationFactory.combine(Arrays.asList(null, p1, null, p2, null)),
			TableConfigurationFactory.table()));
	}

	private void check(TableConfiguration config) {
		assertEquals("foo2", config.declareColumn("foo").getCssClass());
		assertEquals("bar", config.declareColumn("bar").getCssClass());
		assertEquals("baz", config.declareColumn("baz").getCssClass());
	}

	public void testDefaultTableIsFrozen() {
		assertTrue(TableConfiguration.defaultTable().isFrozen());
	}

	public void testPageSizeOptionsIsInitialized() {
		TableConfiguration.defaultTable();
		assertNotNull(TableConfiguration.getDefaultPageSizeOptions());
		assertNotNull(TableConfiguration.defaultTable().getPageSizeOptions());
		int[] expectedValue = TableConfiguration.getDefaultPageSizeOptions();
		int[] actualValue = TableConfiguration.defaultTable().getPageSizeOptions();
		assertTrue(Arrays.equals(expectedValue, actualValue));
	}

	public void testCopyDefaultTable() {
		try {
			TableConfiguration.defaultTable().copy();
		} catch (RuntimeException ex) {
			fail("Copying the default table fails with an exception: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates a new {@link TableConfiguration} by applying all options set in the given
	 * {@link TableConfig}.
	 * 
	 * @see TableConfigurationFactory#apply(TableConfigurationProvider, TableConfiguration)
	 */
	public static TableConfiguration build(InstantiationContext context, TableConfig tableConfig) {
		if (tableConfig == null) {
			return null;
		}
		
		return TableConfigurationFactory.build(TableConfigurationFactory.toProvider(context, tableConfig));
	}

	public static Test suite() {
		Test tableConfigFactorySetup = createTableFactorySetup();
		Test customPropertiesSetup = createCustomPropertiesSetup(tableConfigFactorySetup);
		return ModuleTestSetup.setupModule(customPropertiesSetup);
	}

	private static Test createTableFactorySetup() {
		return ServiceTestSetup.createSetup(null, TestTableConfigurationFactory.class,
			TableConfigurationFactory.Module.INSTANCE);
	}

	private static Test createCustomPropertiesSetup(Test tableConfigFactorySetup) {
		String fileName = CustomPropertiesDecorator.createFileName(TestTableConfigurationFactory.class);
		return new CustomPropertiesSetup(tableConfigFactorySetup, fileName, true);
	}

}
