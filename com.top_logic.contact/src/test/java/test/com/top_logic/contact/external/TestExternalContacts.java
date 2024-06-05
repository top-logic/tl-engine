/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.external;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.contact.external.ExternalContact;
import com.top_logic.contact.external.ExternalContacts;
import com.top_logic.contact.external.PlainExternalContact;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Test case for {@link ExternalContacts}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TestExternalContacts extends BasicTestCase {
	PlainExternalContact c1;
	PlainExternalContact c2;
	PlainExternalContact c3;

	private DBHelper _sqlDialect;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		c1 = new PlainExternalContact("u0001", "Otto", "Maier", "dpx", "o.maier@xtl.com", "+49-751-35676-351", "test");
		c2 = new PlainExternalContact("u0042", "Hermine", "Müller", "dpc", "h.mueller@xtl.com", "+49-751-35676-315", "test");
		c3 = new PlainExternalContact("u0013", "Mathilde", "Bauer", "tpd", "m.bauer@xtl.com", "+49-324-45332-242", "test");

		_sqlDialect = getSQLDialect();
	}

	private DBHelper getSQLDialect() throws SQLException {
		return getKnowledgeBase().getConnectionPool().getSQLDialect();
	}

	private DBKnowledgeBase getKnowledgeBase() {
		return (DBKnowledgeBase) PersistencyLayer.getKnowledgeBase();
	}
	
	public void testCreateAssign() throws SQLException, KnowledgeBaseException, Throwable {
		try {
			final KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			Transaction tx = kb.beginTransaction();

			ExternalContacts.newContact(c1);
			ExternalContacts.newContact(c2);
			ExternalContacts.newContact(c3);

			Set setC2 = Collections.singleton(c2);
			Date date1 = new GregorianCalendar(2007, GregorianCalendar.MARCH, 31).getTime();
			final TLID objectId = kb.createID();
			final TLID attributeId = kb.createID();
			Assert.assertEquals(setC2, ExternalContacts.updateContacts(objectId, attributeId, date1, setC2));
			tx.commit();

			Assert.assertEquals(setC2, ExternalContacts.getCurrentContacts(objectId, attributeId));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("otto maier"));

			cleanupCreate();
			cleanupAssignments(objectId);
		} catch (Throwable exception) {
			if (_sqlDialect instanceof OracleHelper) {
				/* Exptected due to the known bug in ticket #21224: External contacts does not work with
				 * ORACLE. */
	
				return;
			} else {
				throw exception;
			}
		}

		if (_sqlDialect instanceof OracleHelper) {
			fail(
				"Test should fail due to the known bug in ticket #21224: External contacts does not work with ORACLE.");
		}
	}
	
	public void testCreate() throws SQLException, KnowledgeBaseException, Throwable {
		try {
			cleanupCreate();

			Transaction beginTransaction =
				PersistencyLayer.getKnowledgeBase().beginTransaction();

			ExternalContacts.newContact(c1);
			ExternalContacts.newContact(c2);
			ExternalContacts.newContact(c3);
			beginTransaction.commit();

			ExternalContact u0042 = ExternalContacts.getContact("u0042", "test");
			Assert.assertEquals(c2, u0042);

			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("otto maier"));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("Maier, Otto"));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("Maier (dpx)"));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("M, O (dpx)"));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("Ott Ma (dp"));
			Assert.assertEquals(list(c1, c2), ExternalContacts.getMatchingContacts("dp"));
			Assert.assertEquals(list(c1), ExternalContacts.getMatchingContacts("o.mai"));
			Assert.assertEquals(list(c2), ExternalContacts.getMatchingContacts("+49-751-35676-31"));

			cleanupCreate();
		} catch (Throwable exception) {
			if (_sqlDialect instanceof OracleHelper) {
				/* Exptected due to the known bug in ticket #21224: External contacts does not work
				 * with ORACLE. */

				return;
			} else {
				throw exception;
			}
		}
		

		if (_sqlDialect instanceof OracleHelper) {
			fail(
				"Test should fail due to the known bug in ticket #21224: External contacts does not work with ORACLE.");
		}
	}
	
	private List list(ExternalContact c1) {
		return Arrays.asList(new ExternalContact[] {c1});
	}

	private List list(ExternalContact c1, ExternalContact c2) {
		return Arrays.asList(new ExternalContact[] {c1, c2});
	}
	
	public void testAssign() throws SQLException, KnowledgeBaseException, Throwable {
		final KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction beginTransaction = kb.beginTransaction();
		Set setC2 = Collections.singleton(c2);
		Date date1 = new GregorianCalendar(2007, GregorianCalendar.MARCH, 31).getTime();
		final TLID objectId = kb.createID();
		final TLID attributeId = kb.createID();
		Assert.assertEquals(setC2, 
			ExternalContacts.updateContacts(objectId, attributeId, date1, setC2));
		beginTransaction.commit();
		
		Assert.assertEquals(setC2, ExternalContacts.getCurrentContacts(objectId, attributeId));
		
		beginTransaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		HashSet setC1C3 = new HashSet(Arrays.asList(new ExternalContact[] {c1, c3}));
		Date date2 = new GregorianCalendar(2007, GregorianCalendar.APRIL, 1).getTime();
		Assert.assertEquals(setC1C3, 
			ExternalContacts.updateContacts(objectId, attributeId, date2, setC1C3));
		beginTransaction.commit();

		Assert.assertEquals(setC1C3, ExternalContacts.getCurrentContacts(objectId, attributeId));

		beginTransaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		HashSet setC1C2C3 = new HashSet(Arrays.asList(new ExternalContact[] {c1, c2, c3}));
		Date date3 = new GregorianCalendar(2007, GregorianCalendar.APRIL, 2).getTime();
		Assert.assertEquals(setC2, 
			ExternalContacts.updateContacts(objectId, attributeId, date3, setC1C2C3));
		beginTransaction.commit();

		Assert.assertEquals(setC1C2C3, ExternalContacts.getCurrentContacts(objectId, attributeId));
		
		beginTransaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		Date date4 = new GregorianCalendar(2007, GregorianCalendar.MAY, 1).getTime();
		Assert.assertEquals(Collections.EMPTY_SET, 
			ExternalContacts.updateContacts(objectId, attributeId, date4, setC2));
		beginTransaction.commit();
		
		Assert.assertEquals(setC2, ExternalContacts.getCurrentContacts(objectId, attributeId));

		cleanupAssignments(objectId);
	}

	private void cleanupAssignments(TLID objectId) throws SQLException, KnowledgeBaseException {
		Transaction beginTransaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		ExternalContacts.dropObjectHistory(objectId);
		beginTransaction.commit();
	}

	private void cleanupCreate() throws SQLException, KnowledgeBaseException {
		Transaction beginTransaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		ExternalContacts.dropContact(c1.getUNumber(), "test");
		ExternalContacts.dropContact(c2.getUNumber(), "test");
		ExternalContacts.dropContact(c3.getUNumber(), "test");
		beginTransaction.commit();
	}

    public static Test suite () {
		return KBSetup.getKBTest(TestExternalContacts.class);
    }

}
