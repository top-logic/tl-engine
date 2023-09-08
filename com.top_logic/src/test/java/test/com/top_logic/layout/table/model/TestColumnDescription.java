/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.model;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.layout.table.DefaultCellRenderer;
import com.top_logic.layout.table.control.ColumnDescription;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Test class for {@link ColumnDescription}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestColumnDescription extends BasicTestCase {

	public void testCopy() {
		ColumnConfiguration column = TableConfigurationFactory.table().declareColumn("testColumn");

		column.setAccessor(WrapperAccessor.INSTANCE);
		column.setCellStyle("width:80px");
		column.setCellRenderer(DefaultCellRenderer.INSTANCE);
		String class1 = "classifier1";
		String class2 = "classifier2";
		column.setClassifiers(list(class1, class2));
		column.setColumnLabel(column.getColumnLabel() + "label");
		column.setCssClass(column.getCssClass() + "cssClass");
		column.setCommandGroup(SimpleBoundCommandGroup.CREATE_NAME);
		column.setComparator(Equality.INSTANCE);
		column.setCssClassGroupFirst("firstClass");
		column.setCssClassGroupLast("lastCSS");
		column.setCssClassProvider(CellClassProviderTestFixture.INSTANCE);
		column.setDefaultColumnWidth("800000px");
		column.setDescendingComparator(WrapperComparator.ID_COMPARATOR);
		Property<String> key = TypedAnnotatable.property(String.class, "key");
		column.set(key, "anyPropValue");

		ColumnConfiguration copy = column.copy("columnCopy");
		assertEquals(column.getAccessor(), copy.getAccessor());
		assertEquals(column.getCellStyle(), copy.getCellStyle());
		assertEquals(column.getCellRenderer(), copy.getCellRenderer());
		assertTrue(copy.isClassifiedBy(class1));
		assertTrue(copy.isClassifiedBy(class2));
		assertEquals(column.getColumnLabel(), copy.getColumnLabel());
		assertEquals(column.getCssClass(), copy.getCssClass());
		assertEquals(column.getCssClassProvider(), copy.getCssClassProvider());
		assertEquals(column.getCommandGroup(), copy.getCommandGroup());
		assertEquals(column.getComparator(), copy.getComparator());
		assertEquals(column.getCssClassGroupFirst(), copy.getCssClassGroupFirst());
		assertEquals(column.getCssClassGroupLast(), copy.getCssClassGroupLast());
		assertEquals(column.getDefaultColumnWidth(), copy.getDefaultColumnWidth());
		assertEquals(column.getDescendingComparator(), copy.getDescendingComparator());
		assertEquals(column.get(key), copy.get(key));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestColumnDescription}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestColumnDescription.class, TableConfigurationFactory.Module.INSTANCE));
	}

}
