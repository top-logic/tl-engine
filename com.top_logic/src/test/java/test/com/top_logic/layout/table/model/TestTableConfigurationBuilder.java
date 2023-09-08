/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.LogListeningTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EmptyTableConfigurationProvider;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;

/**
 * Test case for {@link TableConfig}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTableConfigurationBuilder extends LogListeningTestCase {

	private TableConfig _table;

	protected interface Check {
		void check(TableConfiguration check);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_table = TableConfigurationFactory.tableConfig();
	}

	@Override
	protected void tearDown() throws Exception {
		_table = null;

		super.tearDown();
	}

	protected TableConfig getTable() {
		return _table;
	}

	public void testAdapt() {
		TableConfig table = getTable();

		ColumnConfig b = TableConfigurationFactory.declareColumn(table, "b");
		assertEquals(null, b.getCssClass());
		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals(null, check.getCol("b").getCssClass());
				assertEquals(null, check.getCol("c").getCssClass());
				assertEquals(null, check.getCol("d").getCssClass());
			}
		});

		// Declare column before setting a default property.
		TableConfigurationFactory.declareColumn(table, "d").setCssClass("D");
		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals("D", check.getCol("d").getCssClass());
			}
		});

		// Define a default column property.
		table.getDefaultColumn().setCssClass("A");
		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals("A", check.getDefaultColumn().getCssClass());
				assertEquals("A", check.getCol("b").getCssClass());
				assertEquals("A", check.getCol("c").getCssClass());
				assertEquals("D", check.getCol("d").getCssClass());
			}
		});

		// Declare a column after setting a default property.
		TableConfigurationFactory.declareColumn(table, "b").setCssClass("B");
		assertEquals("A", table.getDefaultColumn().getCssClass());
		assertEquals("B", table.getCol("b").getCssClass());
		assertEquals("D", table.getCol("d").getCssClass());

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals("A", check.getDefaultColumn().getCssClass());
				assertEquals("B", check.getCol("b").getCssClass());
				assertEquals("A", check.getCol("c").getCssClass());
				assertEquals("D", check.getCol("d").getCssClass());
			}
		});
	}

	public void testAdaptUnsetDefaultComparator() {
		TableConfig table = getTable();
		ColumnConfig b = TableConfigurationFactory.declareColumn(table, "b");
		b.setSortable(false);

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals(false, check.getCol("b").isSortable());
				assertEquals(null, check.getCol("b").getComparator());
			}
		});
	}

	public void testAdaptUnsetDefaultSortable() {
		TableConfig table = getTable();
		ColumnConfig b = TableConfigurationFactory.declareColumn(table, "b");
		assertEquals("Default value", true, b.isSelectable());
		b.setSelectable(false);

		check(table, new Check() {
			@Override
			public void check(TableConfiguration check) {
				assertEquals(false, check.getCol("b").isSelectable());
			}
		});
	}

	public void testEmptyProvider() {
		assertSame(EmptyTableConfigurationProvider.INSTANCE, TableConfigurationFactory.emptyProvider());
	}

	public void testParse() throws ConfigurationException {
		TableConfiguration config = parseTableConfiguration(""
			+ "<table>"
			+ "<columns>"
			+ "<column name='a'"
			+ " cssClass='foo'"
			+ " cssClassProvider='" + CellClassProviderTestFixture.class.getName()
			+ "'/>"
			+ "</columns>"
			+ "</table>");
		assertNotNull(config);

		// Default table configuration already contains selection column
		Iterator<? extends ColumnConfiguration> columnIterator = config.getDeclaredColumns().iterator();
		assertEquals(2, config.getDeclaredColumns().size());
		assertEquals(TableControl.SELECT_COLUMN_NAME, columnIterator.next().getName());

		assertEquals("a", columnIterator.next().getName());

		assertEquals("foo", config.getCol("a").getCssClass());
		assertEquals(CellClassProviderTestFixture.INSTANCE, config.getCol("a").getCssClassProvider());
	}

	public void testParseGroups() throws ConfigurationException {
		TableConfiguration builder = parseTableConfiguration(""
			+ "<table>"
			+ "    <column-default "
			+ "     fieldProvider='" + TestingFP1.class.getName() + "'"
			+ "    >"
			+ "    </column-default>"
			+ ""
			+ "    <columns>"
			+ "      <column name='g1'>"
			+ "        <columns>"
			+ "          <column name='c1'"
			+ "          >"
			+ "          </column>"
			+ ""
			+ "          <column name='c2'"
			+ "           fieldProvider=''"
			+ "          >"
			+ "          </column>"
			+ ""
			+ "          <column name='c3'"
			+ "           fieldProvider='" + TestingFP2.class.getName() + "'"
			+ "          >"
			+ "          </column>"
			+ "        </columns>"
			+ "      </column>"
			+ ""
			+ "      <column name='g2'>"
			+ "          <columns>"
			+ "            <column name='c4'"
			+ "            >"
			+ "            </column>"
			+ ""
			+ "            <column name='c5'"
			+ "             fieldProvider=''"
			+ "            >"
			+ "            </column>"
			+ ""
			+ "            <column name='c6'"
			+ "             fieldProvider='" + TestingFP3.class.getName() + "'"
			+ "            >"
			+ "            </column>"
			+ "          </columns>"
			+ "      </column>"
			+ "    </columns>"
			+ "</table>");
		assertNotNull(builder);

		checkGroupConfig(builder);
	}

	private void checkGroupConfig(TableConfiguration config) {
		ColumnConfiguration g1 = config.getCol("g1");
		FieldProvider fpC1 = g1.getCol("c1").getFieldProvider();
		FieldProvider fpC2 = g1.getCol("c2").getFieldProvider();
		FieldProvider fpC3 = g1.getCol("c3").getFieldProvider();

		assertNotNull(fpC1);
		assertNull(fpC2);
		assertNotNull(fpC3);

		assertEquals(TestingFP1.class, fpC1.getClass());
		assertEquals(TestingFP2.class, fpC3.getClass());

		ColumnConfiguration g2 = config.getCol("g2");
		FieldProvider fpC4 = g2.getCol("c4").getFieldProvider();
		FieldProvider fpC5 = g2.getCol("c5").getFieldProvider();
		FieldProvider fpC6 = g2.getCol("c6").getFieldProvider();

		assertNotNull(fpC4);
		assertNull(fpC5);
		assertNotNull(fpC6);

		assertEquals(TestingFP1.class, fpC4.getClass());
		assertEquals(TestingFP3.class, fpC6.getClass());
	}

	protected TableConfiguration parseTableConfiguration(String xml) throws ConfigurationException {
		ConfigurationDescriptor tableDescriptor = TypedConfiguration.getConfigurationDescriptor(TableConfig.class);
		Protocol log = new AssertProtocol();
		Map<String, ConfigurationDescriptor> globalDescriptors = Collections.singletonMap("table", tableDescriptor);
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		CharacterContent content = CharacterContents.newContent(xml);
		TableConfig config = (TableConfig) new ConfigurationReader(context, globalDescriptors).setSource(content).read();
		TableConfiguration tableConfiguration = TestTableConfigurationFactory.build(context, config);
		log.checkErrors();
		return tableConfiguration;
	}

	protected void check(TableConfig table, Check check) {
		check.check(TestTableConfigurationFactory.build(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, table));
		check.check(TestTableConfigurationFactory.build(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, table).copy());
	}

	public static class TestingFP1 extends AbstractFieldProvider {

		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			return null;
		}

	}

	public static class TestingFP2 extends AbstractFieldProvider {

		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			return null;
		}

	}

	public static class TestingFP3 extends AbstractFieldProvider {
		
		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			return null;
		}
		
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestTableConfigurationBuilder.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
