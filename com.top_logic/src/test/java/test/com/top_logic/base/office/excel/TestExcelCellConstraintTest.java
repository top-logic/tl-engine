/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.excel.ExcelCellConstraint;
import com.top_logic.layout.form.CheckException;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestExcelCellConstraintTest extends BasicTestCase {

	
	public void testCheckCellValidity() {
		try {
			ExcelCellConstraint.checkCellValidity("A1");
			ExcelCellConstraint.checkCellValidity("A65536");
			ExcelCellConstraint.checkCellValidity("IV1");
			ExcelCellConstraint.checkCellValidity("IV65536");
		} catch (CheckException ex) {
			fail(ex.getMessage());
		}
		try {
			ExcelCellConstraint.checkCellValidity("A0");
			fail("A0 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			ExcelCellConstraint.checkCellValidity("A65537");
			fail("A65537 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			ExcelCellConstraint.checkCellValidity("-A42");
			fail("-A42 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			ExcelCellConstraint.checkCellValidity("IW42");
			fail("IW42 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
	}
	
	public void testCheckAreaValidity() {
		try {
			ExcelCellConstraint.checkAreaValidity("A1:A65536");
			ExcelCellConstraint.checkAreaValidity("IV1:IV65536");
			ExcelCellConstraint.checkAreaValidity("IV65536:IV65536");
		} catch (CheckException ex) {
			fail(ex.getMessage());
		}
		try {
			ExcelCellConstraint.checkAreaValidity("A65536:A1");
			fail("A65536:A1 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			ExcelCellConstraint.checkAreaValidity("B1:A1");
			fail("B1:A1 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			ExcelCellConstraint.checkAreaValidity("A1;A65536");
			fail("A1;A65536 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
	}

	public void testCheck() {
		ExcelCellConstraint constraint = new ExcelCellConstraint(false);
		try {
			constraint.check("A1");
		} catch (CheckException ex) {
			fail(ex.getMessage());
		}
		try {
			constraint.check("A100000");
			fail("A100000 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			constraint.check("A1:B1");
			fail("A1:B1 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		
		constraint = new ExcelCellConstraint(true);
		try {
			constraint.check("A1:B1");
		} catch (CheckException ex) {
			fail(ex.getMessage());
		}
		try {
			constraint.check("A1");
			fail("A1 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
		try {
			constraint.check("B1:A1");
			fail("A1 should be invalid");
		} catch (CheckException ex) {
			//expected
		}
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(TestExcelCellConstraintTest.class);
		
		return TLTestSetup.createTLTestSetup(suite);
	}

	
	
}

