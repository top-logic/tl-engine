/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.text.ParseException;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.MySQLHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PostgreSQLHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Transaction;

/**
 * The class {@link TestSettingValues} tests setting and getting values of different types into a
 * {@link KnowledgeItem}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSettingValues extends AbstractDBKnowledgeBaseClusterTest {

	private DBHelper _sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_sqlDialect = kb().getConnectionPool().getSQLDialect();
	}

	/**
	 * Test setting all possible maximal values.
	 * 
	 * @see #testMaximumByteValue()
	 * @see #testMaximumCharacterValue()
	 * @see #testMaximumIntegerValue()
	 * @see #testMaximumDoubleValue()
	 * @see #testMaximumFloatValue()
	 * @see #testMaximumLongValue()
	 * @see #testMaximumShortValue()
	 */
	public void testAllMaximumValues() throws DataObjectException, NoSuchAttributeException {
		StringBuffer buffer = new StringBuffer();
		for (int n = 0; n < 150; n++) {
			buffer.append('X');
		}
		String char150 = buffer.toString();

		Transaction tx = begin();
		KnowledgeObject x =
			newX(true, Byte.MAX_VALUE, Character.MAX_VALUE, "2000-1-1 00:00", Double.MAX_VALUE, Float.MAX_VALUE,
				Integer.MAX_VALUE, Long.MAX_VALUE, Short.MAX_VALUE, char150);
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof MySQLHelper || _sqlDialect instanceof OracleHelper) {
				/* Expected due to the known bug in ticket #3977: Problem setting maximum
				 * values. */

				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof MySQLHelper || _sqlDialect instanceof OracleHelper) {
			fail("Test should fail due to the known bug in ticket #3977: Problem setting maximum values.");
		}

		DataObject max = refetchedNode2Item(x);

		assertNotNull(max);

		assertEquals(true, ((Boolean) max.getAttributeValue(X1_NAME)).booleanValue());
		assertEquals(Byte.MAX_VALUE, ((Byte) max.getAttributeValue(X2_NAME)).byteValue());
		assertEquals(Character.MAX_VALUE, ((Character) max.getAttributeValue(X3_NAME)).charValue());
		assertEquals(Double.MAX_VALUE, ((Double) max.getAttributeValue(X5_NAME)).doubleValue(), 0);
		assertEquals(Float.MAX_VALUE, ((Float) max.getAttributeValue(X6_NAME)).floatValue(), 0);
		assertEquals(Long.MAX_VALUE, ((Long) max.getAttributeValue(X8_NAME)).longValue());
		assertEquals(Short.MAX_VALUE, ((Short) max.getAttributeValue(X9_NAME)).shortValue());
		assertEquals(char150, (String) max.getAttributeValue(X10_NAME));
	}

	/**
	 * Test setting all possible minimal values.
	 * 
	 * @see #testMinimumByteValue()
	 * @see #testMinimumShortValue()
	 * @see #testMinimumIntegerValue()
	 * @see #testMinimumLongValue()
	 * @see #testMinimumDoubleValue()
	 * @see #testMinimumFloatValue()
	 * @see #testMinimumDateValue()
	 * @see #testMinimumCharacterValue()
	 */
	public void testAllMinimumValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, Byte.MIN_VALUE, Character.MIN_VALUE, "1970-1-1 00:00", Double.MIN_VALUE, Float.MIN_VALUE,
				Integer.MIN_VALUE, Long.MIN_VALUE, Short.MIN_VALUE, StringServices.EMPTY_STRING);
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof OracleHelper || _sqlDialect instanceof PostgreSQLHelper) {
				/* Expected due to the known bug in ticket #3977: Problem setting minimum
				 * values. */
				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof OracleHelper || _sqlDialect instanceof PostgreSQLHelper) {
			fail("Test should fail due to the known bug in ticket #3977: Problem setting minimum values.");
		}

		DataObject min = refetchedNode2Item(x);
		assertNotNull(min);
		assertEquals(false, ((Boolean) min.getAttributeValue(X1_NAME)).booleanValue());
		assertEquals(Byte.MIN_VALUE, ((Byte) min.getAttributeValue(X2_NAME)).byteValue());
		try {
			assertEquals(Character.MIN_VALUE, ((Character) min.getAttributeValue(X3_NAME)).charValue());
		} catch (ClassCastException exception) {
			fail("Ticket #8911: MySQL returns String for attribute of type Character.");
		}
		assertEquals(Double.MIN_VALUE, ((Double) min.getAttributeValue(X5_NAME)).doubleValue(), 0);
		assertEquals(Float.MIN_VALUE, ((Float) min.getAttributeValue(X6_NAME)).floatValue(), 0);
		assertEquals(Long.MIN_VALUE, ((Long) min.getAttributeValue(X8_NAME)).longValue());
		assertEquals(Short.MIN_VALUE, ((Short) min.getAttributeValue(X9_NAME)).shortValue());
		assertEquals(StringServices.EMPTY_STRING, (String) min.getAttributeValue(X10_NAME));

	}

	/**
	 * Test setting "normal values".
	 */
	public void testNormalValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		DataObject normal = refetchedNode2Item(x);
		assertNotNull(normal);
		assertEquals(false, ((Boolean) normal.getAttributeValue(X1_NAME)).booleanValue());
		assertEquals(0, ((Byte) normal.getAttributeValue(X2_NAME)).byteValue());
		assertEquals('A', ((Character) normal.getAttributeValue(X3_NAME)).charValue());
		assertEquals(0.0d, ((Double) normal.getAttributeValue(X5_NAME)).doubleValue(), 0);
		assertEquals(0.0f, ((Float) normal.getAttributeValue(X6_NAME)).floatValue(), 0);
		assertEquals(0L, ((Long) normal.getAttributeValue(X8_NAME)).longValue());
		assertEquals(0, ((Short) normal.getAttributeValue(X9_NAME)).shortValue());
		assertEquals("normal", (String) normal.getAttributeValue(X10_NAME));
	}

	/**
	 * Test searching {@link Boolean} values.
	 */
	public void testSearchBooleanValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X1_NAME, Boolean.FALSE);
	}

	/**
	 * Test searching {@link Byte} values.
	 */
	public void testSearchByteValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X2_NAME, (byte) 0);
	}

	/**
	 * Test searching {@link Character} values.
	 */
	public void testSearchCharacterValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X3_NAME, 'A');
	}

	/**
	 * Test searching {@link Byte} values.
	 */
	public void testSearchDateValues() throws DataObjectException, ParseException {
		String dateSerialization = "2009-07-15 12:27";
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', dateSerialization, 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X4_NAME, x4Format().parse(dateSerialization));
	}

	/**
	 * Test searching {@link Double} values.
	 */
	public void testSearchDoubleValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X5_NAME, 0.0d);
	}

	/**
	 * Test searching {@link Float} values.
	 */
	public void testSearchFloatValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X6_NAME, 0.0f);
	}

	/**
	 * Test searching {@link Integer} values.
	 */
	public void testSearchIntegerValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X7_NAME, 0);
	}

	/**
	 * Test searching {@link Long} values.
	 */
	public void testSearchLongValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X8_NAME, 0L);
	}

	/**
	 * Test searching {@link Short} values.
	 */
	public void testSearchShortValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X9_NAME, (short) 0);
	}

	/**
	 * Test searching {@link String} values.
	 */
	public void testSearchStringValues() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "normal");
		commit(tx);

		assertFoundByQuery(x, X10_NAME, "normal");
	}

	private void assertFoundByQuery(KnowledgeObject expected, String attribute, Object value) {
		RevisionQuery<KnowledgeObject> searchDirect =
			queryUnresolved(filter(allOf(X_NAME), eqBinary(attribute(X_NAME, attribute), literal(value))));
		List<KnowledgeObject> directResult = kb().search(searchDirect);
		assertEquals(list(expected), directResult);

		String valueParam = "value";
		SetExpression filter = filter(allOf(X_NAME), eqBinary(attribute(X_NAME, attribute), param(valueParam)));
		MetaObject primitiveType = MOPrimitive.typeOfValue(value);
		RevisionQuery<KnowledgeObject> searchParam =
			queryUnresolved(params(paramDecl(primitiveType, valueParam)), filter, NO_ORDER);
		List<KnowledgeObject> paramResult = kb().search(searchParam, revisionArgs().setArguments(value));
		assertEquals(list(expected), paramResult);

		assertEquals(expected, kb().getObjectByAttribute(X_NAME, attribute, value));
		assertEquals(list(expected), toList(kb().getObjectsByAttribute(X_NAME, attribute, value)));
	}

	/**
	 * Tests setting {@link Short#MIN_VALUE}.
	 */
	public void testMinimumShortValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, Short.MIN_VALUE, "min");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Short.MIN_VALUE, ((Short) x2.getAttributeValue(X9_NAME)).shortValue());
	}

	/**
	 * Tests setting {@link Short#MAX_VALUE}.
	 */
	public void testMaximumShortValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, Short.MAX_VALUE, "max");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Short.MAX_VALUE, ((Short) x2.getAttributeValue(X9_NAME)).shortValue());
	}

	/**
	 * Tests setting {@link Long#MIN_VALUE}.
	 */
	public void testMinimumLongValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, Long.MIN_VALUE, (short) 0, "min");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Long.MIN_VALUE, ((Long) x2.getAttributeValue(X8_NAME)).longValue());
	}

	/**
	 * Tests setting {@link Long#MAX_VALUE}.
	 */
	public void testMaximumLongValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, Long.MAX_VALUE, (short) 0, "max");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Long.MAX_VALUE, ((Long) x2.getAttributeValue(X8_NAME)).longValue());
	}

	/**
	 * Tests setting {@link Integer#MIN_VALUE}.
	 */
	public void testMinimumIntegerValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, Integer.MIN_VALUE, 0L, (short) 0, "min");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Integer.MIN_VALUE, ((Integer) x2.getAttributeValue(X7_NAME)).intValue());
	}

	/**
	 * Tests setting {@link Integer#MAX_VALUE}.
	 */
	public void testMaximumIntegerValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, 0.0f, Integer.MAX_VALUE, 0L, (short) 0, "max");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Integer.MAX_VALUE, ((Integer) x2.getAttributeValue(X7_NAME)).intValue());
	}

	/**
	 * Tests setting {@link Float#MIN_VALUE}.
	 */
	public void testMinimumFloatValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, Float.MIN_VALUE, 0, 0L, (short) 0, "min");
		commit(tx);
		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Float.MIN_VALUE, ((Float) x2.getAttributeValue(X6_NAME)).floatValue());
	}

	/**
	 * Tests setting {@link Float#MAX_VALUE}.
	 */
	public void testMaximumFloatValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", 0.0d, Float.MAX_VALUE, 0, 0L, (short) 0, "max");
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof MySQLHelper) {
				/* Expected due to the known bug in ticket #3977: MySQL-Float database type can not
				 * contain Float.MAX_VALUE. */

				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof MySQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #3977: MySQL-Float database type can not contain Float.MAX_VALUE.");
		}


		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Float.MAX_VALUE, ((Float) x2.getAttributeValue(X6_NAME)).floatValue());
	}

	/**
	 * Tests setting {@link Double#MIN_VALUE}.
	 */
	public void testMinimumDoubleValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", Double.MIN_VALUE, 0.0f, 0, 0L, (short) 0, "min");
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof OracleHelper) {
				/* Expected due to the known bug in ticket #3977: ORACLE- and DB2-Double database
				 * type can not contain Double.MIN_VALUE. */

				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof OracleHelper) {
			fail(
				"Test should fail due to the known bug in ticket #3977: ORACLE- and DB2-Double database type can not contain Double.MIN_VALUE.");
		}

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Double.MIN_VALUE, ((Double) x2.getAttributeValue(X5_NAME)).doubleValue());
	}

	/**
	 * Tests setting {@link Double#MAX_VALUE}.
	 */
	public void testMaximumDoubleValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, 'A', "2009-07-15 12:27", Double.MAX_VALUE, 0.0f, 0, 0L, (short) 0, "max");
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof OracleHelper) {
				/* Expected due to the known bug in ticket #3977: ORACLE-Double database type can
				 * not contain Double.MAX_VALUE. */

				return;
			} else {
				throw exception;
			}
		}

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);

		try {
			assertEquals(Double.MAX_VALUE, ((Double) x2.getAttributeValue(X5_NAME)).doubleValue());
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof OracleHelper) {
				/* Expected due to the known bug in ticket #3977: Double.MAX_VALUE was stored in the
				 * database. */

				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof OracleHelper) {
			fail(
				"Test should fail due to the known bug in ticket #3977: ORACLE-Double database type can not contain Double.MAX_VALUE and Double.MAX_VALUE was stored in the database.");
		}
	}

	/**
	 * Tests setting "minimal date" (1970-1-1 00:00).
	 */
	public void testMinimumDateValue() throws DataObjectException {
		Transaction tx = begin();
		newX(false, (byte) 0, 'A', "1970-1-1 00:00", 0.0d, 0.0f, 0, 0L, (short) 0, "min");
		commit(tx);
	}

	/**
	 * Tests setting {@link Character#MIN_VALUE}.
	 */
	public void testMinimumCharacterValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, Character.MIN_VALUE, "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "min");
		try {
			commit(tx);
		} catch (AssertionFailedError exception) {
			if (_sqlDialect instanceof PostgreSQLHelper) {
				/* Expected due to the known bug in ticket #3977: PostgreSQL-Character database type
				 * can not contain Character.MIN_VALUE. PostgreSQL does not support storing NULL
				 * (\0x00) characters in text fields. */

				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof PostgreSQLHelper) {
			fail(
				"Test should fail due to the known bug in ticket #3977: PostgreSQL-Character database type can not contain Character.MIN_VALUE.");
		}

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Character.MIN_VALUE, ((Character) x2.getAttributeValue(X3_NAME)).charValue());
	}

	/**
	 * Tests setting {@link Character#MAX_VALUE}.
	 */
	public void testMaximumCharacterValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x =
			newX(false, (byte) 0, Character.MAX_VALUE, "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "max");
		try {
			commit(tx);
		} catch (AssertionFailedError err) {
			fail("Ticket #3977: DB2-Character database type can not contain Character.MAX_VALUE", err);
		}

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Character.MAX_VALUE, ((Character) x2.getAttributeValue(X3_NAME)).charValue());
	}

	/**
	 * Tests setting {@link Byte#MIN_VALUE}.
	 */
	public void testMinimumByteValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, Byte.MIN_VALUE, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "min");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Byte.MIN_VALUE, ((Byte) x2.getAttributeValue(X2_NAME)).byteValue());
	}

	/**
	 * Tests setting {@link Byte#MAX_VALUE}.
	 */
	public void testMaximumByteValue() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x = newX(false, Byte.MAX_VALUE, 'A', "2009-07-15 12:27", 0.0d, 0.0f, 0, 0L, (short) 0, "max");
		commit(tx);

		DataObject x2 = refetchedNode2Item(x);
		assertNotNull(x2);
		assertEquals(Byte.MAX_VALUE, ((Byte) x2.getAttributeValue(X2_NAME)).byteValue());
	}

	/**
	 * the {@link #kbNode2()} version of the given item. before resolving item in
	 *         {@link #kbNode2()} a refetch is done.
	 */
	private KnowledgeItem refetchedNode2Item(KnowledgeItem item) {
		// refetch to be able to resolve object
		refetchNode2();
		// Fetch on node2 to ensure clean copy from database
		return node2Item(item);
	}

	/**
	 * A cumulative {@link Test} for all Tests in {@link TestSettingValues}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestSettingValues.class, "testSearchCharacterValues");
		}
		return suite(TestSettingValues.class);
	}

}

