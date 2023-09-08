/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.table.RowClassProvider;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.ColumnDescriptionManager;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.ExecutableTableDataExport;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.layout.tree.renderer.NoResourceProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the {@link ColumnDescriptionManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestColumnDescriptionManager extends BasicTestCase {

	public void testCopyTable() {
		TableConfiguration configuration = TableConfiguration.table();
		configuration.setRowClassProvider(new RowClassProvider() {

			@Override
			public String getTRClass(TableControl view, int rowOptions, int displayedRow, int row) {
				return null;
			}
		});
		configuration.setFooterStyle("footerStyle");
		configuration.setHeaderStyle("headerStyle");
		configuration.setTableName("tableName");
		configuration.setTableRenderer(DefaultTableRenderer.newInstance());
		configuration.setRowObjectResourceProvider(NoResourceProvider.INSTANCE);
		configuration
			.setColumnCustomization(nextEnum(ColumnCustomization.class, configuration.getColumnCustomization()));
		configuration.setShowTitle(!configuration.getShowTitle());
		configuration.setShowColumnHeader(!configuration.getShowColumnHeader());
		configuration.setRowStyle("rowStyle");
		configuration.setResPrefix(ResPrefix.forTest("my.general.table."));
		configuration.setTitleKey(ResKey.forTest("my.personal.title"));
		configuration.setTitleStyle("titleStyle");
		configuration.setMaxColumns(configuration.getMaxColumns() + 1);
		configuration.setPageSizeOptions(1, 2, 3, 4);
		configuration.setMultiSort(nextEnum(Enabled.class, configuration.getMultiSort()));
		configuration.setDefaultSortOrder(list(SortConfigFactory.sortConfig("columnName", false)));
		TableFilterProvider filterProvider = new TableFilterProvider() {

			@Override
			public TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition) {
				return null;
			}

		};
		configuration.setDefaultFilterProvider(filterProvider);
		configuration.setFilterProvider(filterProvider);
		configuration.setFilterDisplayParents(false);
		configuration.setFilterDisplayChildren(false);
		configuration.setShowFooter(!configuration.getShowFooter());
		configuration.setModelMapping(new Mapping<Object, TLObject>() {

			@Override
			public TLObject map(Object input) {
				return null;
			}
		});
		configuration.setExporter(new ExecutableTableDataExport() {

			@Override
			public BinaryData createExportData(TableData table, String name) throws IOException {
				return new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM, name);
			}

			@Override
			public HandlerResult exportTableData(DisplayContext context, TableData table) {
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		configuration.setMultipleSettingsKey("someNewKey");
		configuration.setSupportMultipleSettings(!configuration.getSupportMultipleSettings());

		assertEqualConfiguration(configuration, configuration.copy());
	}

	private <T extends Enum<T>> T nextEnum(Class<T> enumClass, T value) {
		T[] allValues = enumClass.getEnumConstants();
		if (value == null) {
			return allValues[0];
		} else {
			return allValues[(value.ordinal() + 1) % allValues.length];
		}
	}

	private void assertEqualConfiguration(TableConfiguration expected, TableConfiguration actual) {
		assertEquals("FooterStyle mismatch!", expected.getFooterStyle(), actual.getFooterStyle());
		assertEquals("HeaderStyle mismatch!", expected.getHeaderStyle(), actual.getHeaderStyle());
		assertEquals("TableName mismatch!", expected.getTableName(), actual.getTableName());
		assertEquals("TableRenderer mismatch!", expected.getTableRenderer(), actual.getTableRenderer());
		assertEquals("RowClassProvider mismatch!", expected.getRowClassProvider(), actual.getRowClassProvider());
		assertEquals("RowObjectResourceProvider mismatch!", expected.getRowObjectResourceProvider(),
			actual.getRowObjectResourceProvider());
		assertEquals("ColumnCustomization mismatch!", expected.getColumnCustomization(),
			actual.getColumnCustomization());
		assertEquals("ShowTitle mismatch!", expected.getShowTitle(), actual.getShowTitle());
		assertEquals("ShowColumnHeader mismatch!", expected.getShowColumnHeader(), actual.getShowColumnHeader());
		assertEquals("ShowFooter mismatch!", expected.getShowFooter(), actual.getShowFooter());
		assertEquals("RowStyle mismatch!", expected.getRowStyle(), actual.getRowStyle());
		assertEquals("TitleKey mismatch!", expected.getTitleKey(), actual.getTitleKey());
		assertEquals("TitleStyle mismatch!", expected.getTitleStyle(), actual.getTitleStyle());
		assertEquals("MaxColumns mismatch!", expected.getMaxColumns(), actual.getMaxColumns());
		arrayEquals("PageSizeOptions mismatch!", expected.getPageSizeOptions(), actual.getPageSizeOptions());
		assertEquals("MultiSort mismatch!", expected.getMultiSort(), actual.getMultiSort());
		assertEquals("DefaultSortOrder mismatch!", expected.getDefaultSortOrder(), actual.getDefaultSortOrder());
		assertEquals("DefaultFilterProvider names mismatch!", expected.getDefaultFilterProvider(),
			actual.getDefaultFilterProvider());
		assertEquals("Filterprovider mismatch!", expected.getFilterProvider(), actual.getFilterProvider());
		assertEquals("FilterDisplayParents mismatch!", expected.getFilterDisplayParents(),
			actual.getFilterDisplayParents());
		assertEquals("FilterDisplayChildren mismatch!", expected.getFilterDisplayChildren(),
			actual.getFilterDisplayChildren());
		assertEquals("ShowFooter mismatch!", expected.getShowFooter(), actual.getShowFooter());
		assertEquals("Model mapping mismatch!", expected.getModelMapping(), actual.getModelMapping());
		assertEquals("Exporter mismatch!", expected.getExporter(), actual.getExporter());
		assertEquals(TableConfig.SUPPORTS_MULTIPLE_SETTINGS_ATTRIBUTE + " mismatch!", expected.getSupportMultipleSettings(),
			actual.getSupportMultipleSettings());
		assertEquals(TableConfig.MULTIPLE_SETTINGS_KEY_ATTRIBUTE + " mismatch!", expected.getMultipleSettingsKey(),
			actual.getMultipleSettingsKey());
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestColumnDescriptionManager.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
