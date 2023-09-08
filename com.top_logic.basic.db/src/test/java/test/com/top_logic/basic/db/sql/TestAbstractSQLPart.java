/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import junit.framework.TestCase;

import com.top_logic.basic.db.sql.AbstractSQLPart;
import com.top_logic.basic.sql.DBType;

/**
 * Test case for {@link AbstractSQLPart}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAbstractSQLPart extends TestCase {

	public void testToStringLiteral() {
		assertNotEquals(literalString("foo").toString(), literalString("bar").toString());
	}

	public void testToStringParameter() {
		assertEquals(":p IN (1, 2)", inSet(parameter(DBType.INT, "p"), list(1, 2), DBType.INT).toString());
	}

	public void testToStringSetParameter() {
		assertEquals("1 IN (:p)", inSet(literalInteger(1), setParameter("p", DBType.INT)).toString());
	}

}
