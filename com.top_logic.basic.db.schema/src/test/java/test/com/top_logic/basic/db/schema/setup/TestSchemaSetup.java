/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema.setup;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.AbstractMOFactory;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.sql.DBAttribute;

/**
 * Test of {@link SchemaSetup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSchemaSetup extends BasicTestCase {

	private static final MOFactory TEST_FACTORY = new AbstractMOFactory() {

		@Override
		public MOReference createMOReference(String name, MetaObject targetType, Boolean byValue) {
			throw new UnsupportedOperationException();
		}
	};

	public void testCreateTable() {
		DBTable table = DBSchemaFactory.createTable("table");
		table.setExplicitDBName("DB_TABLE_NAME");
		DBColumn col1 = DBSchemaFactory.createColumn("col1");
		col1.setExplicitDBName("DB_COL1");
		col1.setType(DBType.ID);
		table.getColumns().add(col1);
		DBColumn col2 = DBSchemaFactory.createColumn("col2");
		col2.setType(DBType.STRING);
		table.getColumns().add(col2);
		DBIndex index = DBSchemaFactory.createIndex("index");
		index.getColumnRefs().add(DBSchemaFactory.ref(col1));
		table.getIndices().add(index);

		MOClass moClass = SchemaSetup.createTableType(table, TEST_FACTORY);
		moClass.freeze();
		assertEquals("table", moClass.getName());
		assertEquals("DB_TABLE_NAME", moClass.getDBMapping().getDBName());
		List<MOAttribute> attributes = moClass.getAttributes();
		assertEquals(2, attributes.size());
		MOAttribute attr = attributes.get(0);
		assertEquals("col1", attr.getName());
		DBAttribute dbAttr = attr.getDbMapping()[0];
		assertEquals("DB_COL1", dbAttr.getDBName());
		assertEquals(DBType.ID, dbAttr.getSQLType());
		MOAttribute attr2 = attributes.get(1);
		assertEquals("col2", attr2.getName());
		assertEquals("col2", attr2.getDbMapping()[0].getDBName());
		List<MOIndex> indexes = moClass.getIndexes();
		assertEquals(1, indexes.size());
		MOIndex moIndex = indexes.get(0);
		assertEquals("index", moIndex.getName());
		List<MOAttribute> indexAttrs = moIndex.getAttributes();
		assertEquals(list(attr), indexAttrs);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSchemaSetup}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestSchemaSetup.class);
	}

}

