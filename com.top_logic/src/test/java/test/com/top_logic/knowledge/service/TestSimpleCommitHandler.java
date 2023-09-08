/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.sql.SQLException;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.SimpleCommitHandler;

/**
 * Test the {@link SimpleCommitHandler}.
 * 
 * @author <a href="mailto:klaus.halfmanna@top-logic.com">Klaus Halfmann/a>
 */
public class TestSimpleCommitHandler extends BasicTestCase {

	/**
	 * Create a new TestSimpleCommitHandler.
	 */
	public TestSimpleCommitHandler(String name) {
		super(name);
	}

	/**
	 * Test commit without begin.
	 */
	public void testMissingBegin() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable allowAll = new TestingCommittable(true, true, true, true);
		sch.addCommittable(allowAll);
		assertFalse(sch.commit());
		assertFalse(allowAll.wasPrepared);
		assertFalse(allowAll.wasDeleted);
		assertFalse(allowAll.wasCommited);
		assertFalse(allowAll.wasRolledBack);
		assertFalse(allowAll.wasCompleted);
	}

	/**
	 * Test normal usage of a {@link Committable}.
	 */
	public void testNormalUsage() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable allowAll = new TestingCommittable(true, true, true, true);
		sch.addCommittable(allowAll);
		sch.begin();
		assertTrue(sch.commit());
		assertTrue(allowAll.wasPrepared);
		assertFalse(allowAll.wasDeleted);
		assertTrue(allowAll.wasCommited);
		assertFalse(allowAll.wasRolledBack);
		assertTrue(allowAll.wasCompleted);

	}

	/**
	 * Test a failed prepare.
	 */
	public void testNoPrepare() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable noPrep = new TestingCommittable(false, true, true, true);
		sch.addCommittable(noPrep);
		sch.begin();
		assertFalse(sch.commit());
		assertTrue(noPrep.wasPrepared);
		assertFalse(noPrep.wasDeleted);
		assertFalse(noPrep.wasCommited);
		assertTrue(noPrep.wasRolledBack);
		assertTrue(noPrep.wasCompleted);
	}

	/**
	 * Test a failed delete.
	 */
	public void testNoDelete() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable noPrep = new TestingCommittable(true, false, true, true);
		sch.addCommittableDelete(noPrep);
		sch.begin();
		assertFalse(sch.commit());
		assertFalse(noPrep.wasPrepared);
		assertTrue(noPrep.wasDeleted);
		assertFalse(noPrep.wasCommited);
		assertTrue(noPrep.wasRolledBack);
		assertTrue(noPrep.wasCompleted);
	}

	/**
	 * Test a failed commit.
	 */
	public void testNoCommit() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable noPrep = new TestingCommittable(true, true, false, true);
		sch.addCommittable(noPrep);
		sch.begin();
		assertFalse(sch.commit());
		assertTrue(noPrep.wasPrepared);
		assertFalse(noPrep.wasDeleted);
		assertTrue(noPrep.wasCommited);
		assertTrue(noPrep.wasRolledBack);
		assertTrue(noPrep.wasCompleted);
	}

	/**
	 * Test a failed rollback.
	 */
	public void testNoRollback() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable noPrep = new TestingCommittable(true, true, true, false);
		sch.addCommittable(noPrep);
		sch.begin();
		assertFalse(sch.rollback());
		assertFalse(noPrep.wasPrepared);
		assertFalse(noPrep.wasDeleted);
		assertFalse(noPrep.wasCommited);
		assertTrue(noPrep.wasRolledBack);
		assertTrue(noPrep.wasCompleted);
	}

	/**
	 * Test a failed rollback for delete.
	 */
	public void testNoRollbackDel() throws SQLException {
		ConnectionPool defaultPool = ConnectionPoolRegistry.getDefaultConnectionPool();
		SimpleCommitHandler sch = new SimpleCommitHandler(defaultPool);
		TestingCommittable noPrep = new TestingCommittable(true, true, true, false);
		sch.addCommittableDelete(noPrep);
		sch.begin();
		assertFalse(sch.rollback());
		assertFalse(noPrep.wasPrepared);
		assertFalse(noPrep.wasDeleted);
		assertFalse(noPrep.wasCommited);
		assertTrue(noPrep.wasRolledBack);
		assertTrue(noPrep.wasCompleted);
	}

	/**
	 * Create suite of test to execute for testing.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSimpleCommitHandler.class);
	}
}
