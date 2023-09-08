/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.basic.LogListeningTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.Icons;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnContainer;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.CellControlRenderer;

/**
 * Common tests for all {@link TableConfiguration} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTestTableConfigurationTest<T extends TableConfiguration> extends LogListeningTestCase {

	private T table;

	private Supplier<TablePart> title = new Supplier<>() {

		@Override
		public TablePart get() {
			return new TablePart("title",
				com.top_logic.layout.form.treetable.component.Icons.FROZEN_TABLE_TITLE_HEIGHT) {


				@Override
				public int getHeight() {
					return table.getTitleHeight();
				}

				@Override
				public void setStyle(String tablePartStyle) {
					table.setTitleStyle(tablePartStyle);
				}
			};
		}
	};

	private Supplier<TablePart> header = new Supplier<>() {

		@Override
		public TablePart get() {
			return new TablePart("header", Icons.FROZEN_TABLE_HEADER_ROW_HEIGHT) {

				@Override
				public int getHeight() {
					return table.getHeaderRowHeight();
				}

				@Override
				public void setStyle(String tablePartStyle) {
					table.setHeaderStyle(tablePartStyle);
				}
			};
		}
	};

	private Supplier<TablePart> footer = new Supplier<>() {

		@Override
		public TablePart get() {
			return new TablePart("footer", Icons.FROZEN_TABLE_FOOTER_HEIGHT) {

				@Override
				public int getHeight() {
					return table.getFooterHeight();
				}

				@Override
				public void setStyle(String tablePartStyle) {
					table.setFooterStyle(tablePartStyle);
				}
			};
		}
	};

	private Supplier<TablePart> row = new Supplier<>() {

		@Override
		public TablePart get() {
			return new TablePart("row", Icons.FROZEN_TABLE_ROW_HEIGHT) {

				@Override
				public int getHeight() {
					return table.getRowHeight();
				}

				@Override
				public void setStyle(String tablePartStyle) {
					table.setRowStyle(tablePartStyle);
				}
			};
		}
	};

	protected interface Check {
		void check(TableConfiguration check);
	}

	abstract class TablePart {
		private ThemeVar<DisplayDimension> tablePartHeightAttribute;

		private String tablePartId;

		protected TablePart(String tablePartId, ThemeVar<DisplayDimension> tablePartHeightAttribute) {
			this.tablePartId = tablePartId;
			this.tablePartHeightAttribute = tablePartHeightAttribute;
		}

		abstract int getHeight();

		abstract void setStyle(String tablePartStyle);

		final int getDefaultHeight() {
			return (int) ThemeFactory.getTheme().getValue(tablePartHeightAttribute).getValue();
		}

		final String getTablePartId() {
			return tablePartId;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		table = createTableConfiguration();
	}

	protected T getTable() {
		return table;
	}

	public void testGroups() {
		ColumnConfiguration c1 = declareColumn(table, "c1");
		c1.setColumnLabel("c1");
		ColumnConfiguration g1 = declareColumn(table, "g1");
		declareColumn(g1, "c2");
		declareColumn(g1, "c3");
		ColumnConfiguration g11 = declareColumn(g1, "g11");
		declareColumn(g11, "c4");

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertColumn(check, "c1");
				ColumnConfiguration g1x = assertColumn(check, "g1");
				assertColumn(g1x, "c2");
				assertColumn(g1x, "c3");
				ColumnConfiguration g11x = assertColumn(g1x, "g11");
				assertColumn(g11x, "c4");
			}

			private ColumnConfiguration assertColumn(ColumnContainer<ColumnConfiguration> check, String name) {
				ColumnConfiguration column = check.getDeclaredColumn(name);
				assertNotNull("Column '" + name + "' not declared.", column);
				assertEquals(name, column.getColumnLabel());
				return column;
			}
		});
	}

	private ColumnConfiguration declareColumn(ColumnContainer<ColumnConfiguration> g1, String name) {
		ColumnConfiguration column = g1.declareColumn(name);
		column.setColumnLabel(name);
		return column;
	}

	public void testInheritance() {
		assertEquals(null, table.getDefaultColumn().getCssClass());
		assertEquals(null, table.getCol("c").getCssClass());

		table.getDefaultColumn().setCssClass("A");
		assertEquals("A", table.getDefaultColumn().getCssClass());
		assertEquals("A", table.getCol("c").getCssClass());

		ColumnConfiguration b = table.declareColumn("b");
		assertEquals("A", b.getCssClass());
		assertEquals("A", table.getCol("c").getCssClass());

		b.setCssClass("B");
		assertEquals("B", b.getCssClass());
		assertEquals("A", table.getDefaultColumn().getCssClass());
		assertEquals("A", table.getCol("c").getCssClass());

		assertEquals(set(TableControl.SELECT_COLUMN_NAME, "b"), toSet(table.getElementaryColumnNames()));

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals("A", check.getDefaultColumn().getCssClass());
				assertEquals("B", check.getCol("b").getCssClass());
				assertEquals("A", check.getCol("c").getCssClass());

				assertEquals(set(TableControl.SELECT_COLUMN_NAME, "b"), toSet(check.getElementaryColumnNames()));
			}
		});
	}

	public void testUnsetDefaultComparator() {
		ColumnConfiguration b = table.declareColumn("b");
		assertEquals("Default value", true, b.isSortable());
		assertEquals("Default value", true, b.isSelectable());

		b.setSelectable(false);
		b.setComparator(null);

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals(false, check.getCol("b").isSelectable());

				assertEquals(null, check.getCol("b").getComparator());
				assertEquals(false, check.getCol("b").isSortable());
			}
		});
	}

	public void testUnsetDefaultControlProvider() {
		table.getDefaultColumn().setCellRenderer(new CellControlRenderer(DefaultFormFieldControlProvider.INSTANCE));
		ColumnConfiguration b = table.declareColumn("b");

		b.setCellRenderer(null);

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals(null, check.getCol("b").getCellRenderer());
			}
		});
	}

	public void testRedeclare() {
		ColumnConfiguration b = table.declareColumn("b");
		assertSame(b, table.declareColumn("b"));
	}

	public void testGetName() {
		assertEquals("a", table.declareColumn("a").getName());
	}

	public void testGetDefaultHeight() throws Exception {
		assertDefaultHeight(title.get());
		assertDefaultHeight(header.get());
		assertDefaultHeight(footer.get());
		assertDefaultHeight(row.get());
	}

	public void testGetCustomizedTitleHeight() throws Exception {
		int customizedHeight = 50;
		assertCustomizedHeight(customizedHeight, title.get());
		assertCustomizedHeight(customizedHeight, header.get());
		assertCustomizedHeight(customizedHeight, footer.get());
		assertCustomizedHeight(customizedHeight, row.get());
	}

	public void testGetDefaultTitleHeightForNoHeightStyle() throws Exception {
		int customizedHeight = 50;
		assertUnsetCustomizedHeight(customizedHeight, title.get());
		assertUnsetCustomizedHeight(customizedHeight, header.get());
		assertUnsetCustomizedHeight(customizedHeight, footer.get());
		assertUnsetCustomizedHeight(customizedHeight, row.get());
	}

	private void assertDefaultHeight(TablePart tablePart) {
		assertEquals("Default height of " + tablePart.getTablePartId() + " is invalid!", tablePart.getDefaultHeight(),
			tablePart.getHeight());
	}

	private void assertCustomizedHeight(int customizedHeight, TablePart tablePart) {
		setupCustomizedHeight(customizedHeight, tablePart);
		assertEquals("Customized height of " + tablePart.getTablePartId() + " is invalid!", customizedHeight,
			tablePart.getHeight());

		String embeddedHeightStyle = "color: red; height: " + customizedHeight + "px; backgroundColor: red";
		tablePart.setStyle(embeddedHeightStyle);
		assertEquals("Customized height of " + tablePart.getTablePartId() + " is invalid!", customizedHeight,
			tablePart.getHeight());
	}

	private void assertUnsetCustomizedHeight(int customizedTitleHeight, TablePart tablePart) {
		setupCustomizedHeight(customizedTitleHeight, tablePart);

		String noHeightStyle = "color: red";
		tablePart.setStyle(noHeightStyle);

		assertDefaultHeight(tablePart);
	}

	private void setupCustomizedHeight(int customizedHeight, TablePart tablePart) {
		String pureHeightStyle = "height: " + customizedHeight + "px";
		tablePart.setStyle(pureHeightStyle);
	}

	protected abstract T createTableConfiguration();

	protected void check(T table, Check check) {
		check.check(table);
		check.check(table.copy());
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test generateSuite(Class<? extends Test> testClass) {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(testClass, TableConfigurationFactory.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE));
	}

}
