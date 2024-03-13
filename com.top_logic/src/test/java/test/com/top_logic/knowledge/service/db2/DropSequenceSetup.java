/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.ThreadContextDecorator;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceTypeProvider;

/**
 * {@link TestSetup} dropping sequences with the given names.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DropSequenceSetup extends RearrangableTestSetup {

	private static MultipleSetupCounter SETUP_COUNTER = newMultipleCounter();

	private final String[] _sequences;

	/**
	 * Creates a new {@link DropSequenceSetup}.
	 * 
	 * @param test
	 *        The test to drop sequences for.
	 * @param sequences
	 *        The sequences to drop.
	 */
	public DropSequenceSetup(Test test, String... sequences) {
		super(ThreadContextDecorator.INSTANCE, test, SETUP_COUNTER.getCounterFor(sequences));
		_sequences = sequences;
	}

	private static Object key(String[] sequences) {
		switch (sequences.length) {
			case 0:
				return Collections.emptySet();
			case 1:
				return Collections.singleton(sequences[0]);
			default:
				HashSet<String> allSchemas = new HashSet<>();
				Collections.addAll(allSchemas, sequences);
				return allSchemas;
		}
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), key(_sequences));
	}

	@Override
	protected void doSetUp() throws Exception {
		// Create sequence table if necessary.
		new TypeProviderSetup(null, false, SequenceTypeProvider.INSTANCE).setUpDecorated();
		dropSequences(_sequences);
	}

	@Override
	protected void doTearDown() throws Exception {
		dropSequences(_sequences);
	}

	/**
	 * Creates a {@link TestFactory} that ensures that the given sequences are dropped for the test
	 * created by the given factory.
	 */
	public static TestFactory dropSequences(TestFactory f, final String... sequences) {
		return new TestFactoryProxy(f) {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				Test t = super.createSuite(testCase, suiteName);
				t = new DropSequenceSetup(t, sequences);
				return t;
			}
		};
	}

	/**
	 * Drops the sequences with the given names from the default database.
	 */
	public static void dropSequences(String... sequences) throws SQLException {
		dropSequences(ConnectionPoolRegistry.getDefaultConnectionPool(), sequences);
	}

	/**
	 * Drops the sequences with the given names from the given database.
	 */
	public static void dropSequences(ConnectionPool pool, String... sequences) throws SQLException {
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			for (String sequence : sequences) {
				RowLevelLockingSequenceManager.dropSequence(connection, sequence);
			}
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
	}

}

