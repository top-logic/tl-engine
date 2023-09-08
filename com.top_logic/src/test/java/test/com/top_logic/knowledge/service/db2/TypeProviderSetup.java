/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.TestFactoryProxy;
import test.com.top_logic.basic.ThreadContextDecorator;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.DefaultMORepository;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.meta.DefaultMOFactory;

/**
 * Setup that creates the tables defined by a given {@link TypeProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeProviderSetup extends RearrangableTestSetup {
	
	private static final Map<Object, MutableInteger> _setupCnt = new HashMap<>();

	private final Iterable<TypeProvider> _providers;

	private final boolean _multipleBranches;

	private ConnectionPool _connectionPool;

	private DBSchema _schema;

	/**
	 * Creates a new {@link TypeProviderSetup}.
	 * 
	 * @param test
	 *        the actual test.
	 * @param multiBranchSupport
	 *        Whether the {@link TypeProvider} support multiple branches.
	 * @param provider
	 *        The {@link TypeProvider} to create tables for.
	 */
	public TypeProviderSetup(Test test, boolean multiBranchSupport, TypeProvider... provider) {
		super(ThreadContextDecorator.INSTANCE, test, getSetupCnt(toIterable(provider)));
		_multipleBranches = multiBranchSupport;
		_providers = toIterable(provider);
	}

	private static <T extends TypeProvider> Iterable<T> toIterable(T[] provider) {
		Collection<T> iterable = new LinkedHashSet<>();
		Collections.addAll(iterable, provider);
		return iterable;
	}

	private static MutableInteger getSetupCnt(Object key) {
		MutableInteger counter = _setupCnt.get(key);
		if (counter == null) {
			counter = new MutableInteger();
			_setupCnt.put(key, counter);
		}
		return counter;
	}

	@Override
	public Object configKey() {
		return _providers;
	}

	@Override
	protected void doSetUp() throws Exception {
		setupVariables();
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			DBSchemaUtils.createTables(connection, _schema, true);
		} finally {
			_connectionPool.releaseWriteConnection(connection);
		}
	}

	private void setupVariables() throws DataObjectException {
		AssertProtocol log = new AssertProtocol();
		_schema = DBSchemaUtils.newDBSchema();
		MORepository repository = new DefaultMORepository(_multipleBranches);
		for (TypeProvider provider : _providers) {
			provider.createTypes(log, DefaultMOFactory.INSTANCE, repository);
		}
		repository.resolveReferences();
		SchemaSetup.addTables(_schema, repository);
		_connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
	}

	@Override
	protected void doTearDown() throws Exception {
		PooledConnection connection = _connectionPool.borrowWriteConnection();
		try {
			DBSchemaUtils.resetTables(connection, _schema, false);
		} finally {
			_connectionPool.releaseWriteConnection(connection);
		}
		teardownVariables();
	}

	private void teardownVariables() {
		_schema = null;
		_connectionPool = null;
	}

	/**
	 * Creates a factory that ensures that the types of the {@link TypeProvider}s can be used in
	 * tests created by the inner factory.
	 */
	public static TestFactory setupTypes(TestFactory innerFactory, final boolean multipleBranches,
			final TypeProvider... provider) {
		return new TestFactoryProxy(innerFactory) {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				Test actualTest = super.createSuite(testCase, suiteName);
				return new TypeProviderSetup(actualTest, multipleBranches, provider);
			}
		};
	}

}

