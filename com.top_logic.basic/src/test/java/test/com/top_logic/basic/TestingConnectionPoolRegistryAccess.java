/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Map;

import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;

/**
 * Allow dynamically changing the default {@link ConnectionPool} for testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingConnectionPoolRegistryAccess extends ConnectionPoolRegistry {
	
	/**
	 * Reference given by {@link TestingConnectionPoolRegistryAccess#setupDefaultPool(String)} to
	 * use in {@link TestingConnectionPoolRegistryAccess#restoreDefaultPool(PoolRef)}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class PoolRef {

		final String _newPool;

		final String _oldPool;

		PoolRef(String newPool, String oldPool) {
			_newPool = newPool;
			_oldPool = oldPool;
		}

		@Override
		public String toString() {
			return "new Pool: " + _newPool + ", old pool: " + _oldPool;
		}

	}

	private static int _cnt = 0;

	private static final String DEFAULT_OLD = "default_Old";

	private static Stack<PoolRef> POOL_NAMES = new ArrayStack<>();

	public TestingConnectionPoolRegistryAccess(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (!POOL_NAMES.isEmpty()) {
			internalSetupDefaultPool(POOL_NAMES.peek()._newPool);
		}
	}

	public static boolean hasConnectionPool(String poolName) {
		Map<String, ConnectionPool> pools = getPools();
		return pools.containsKey(poolName);
	}
	
	/**
	 * Installs the {@link ConnectionPool} with registered with the given name as
	 * {@link ConnectionPoolRegistry#getDefaultConnectionPool()} default {@link ConnectionPool}.
	 * 
	 * @param poolName
	 *        name of the pool that should work as default pool.
	 * 
	 * @return A reference to the former default pool. Must be used to reinstall in
	 *         {@link #restoreDefaultPool(PoolRef)}.
	 */
	public static PoolRef setupDefaultPool(String poolName) {
		String oldPool = internalSetupDefaultPool(poolName);
		PoolRef poolref = new PoolRef(poolName, oldPool);
		POOL_NAMES.push(poolref);
		return poolref;
	}

	private static String internalSetupDefaultPool(String poolName) {
		Map<String, ConnectionPool> instanceMap = getPools();

		ConnectionPool newDefaultPool = instanceMap.get(poolName);
		if (newDefaultPool == null) {
			throw new IllegalStateException("No Pool with name '" + poolName + "' available.");
		}

		ConnectionPool oldDefaultPool = setDefaultPool(instanceMap, newDefaultPool);

		String newPoolName;
		while (true) {
			newPoolName = TestingConnectionPoolRegistryAccess.class.getName() + "_" + poolName + "_" + _cnt++;
			if (!instanceMap.containsKey(newPoolName)) {
				break;
			}
		}
		ConnectionPool clash = installPool(instanceMap, newPoolName, oldDefaultPool);
		assert clash == null : newPoolName + " is not contained as key.";
		return newPoolName;
	}

	private static ConnectionPool setDefaultPool(Map<String, ConnectionPool> poolMap, ConnectionPool newDefaultPool) {
		ConnectionPool oldDefaultPool =
			(ConnectionPool) ReflectionUtils.setValue(registry(), "defaultInstance", newDefaultPool);

		ConnectionPool tmp = installPool(poolMap, ConnectionPoolRegistry.DEFAULT_POOL_NAME, newDefaultPool);
		assert tmp == oldDefaultPool : "default instance and value in instance map does not match.";
		return oldDefaultPool;
	}

	/**
	 * Restores the pool known under the given pool name as default {@link ConnectionPool}.
	 * 
	 * @param oldDefaultPool
	 *        formerly given by {@link #setupDefaultPool(String)}.
	 */
	public static void restoreDefaultPool(PoolRef oldDefaultPool) {
		if (POOL_NAMES.isEmpty()) {
			throw new IllegalStateException("Default pool  was not changed before: " + oldDefaultPool);
		}
		PoolRef pop = POOL_NAMES.pop();
		if (pop != oldDefaultPool) {
			POOL_NAMES.push(pop);
			throw new IllegalStateException("Illegal nesting of pool setup: Try reset " + oldDefaultPool
				+ ",  expected: " + oldDefaultPool);
		}
		internalRestoreDefaultPool(oldDefaultPool._oldPool);
	}

	private static void internalRestoreDefaultPool(String oldDefaultPoolName) {
		Map<String, ConnectionPool> instanceMap = getPools();

		ConnectionPool formerDefaultPool = instanceMap.remove(oldDefaultPoolName);
		if (formerDefaultPool == null) {
//			throw new IllegalStateException("No Pool with name '" + oldDefaultPoolName
//				+ "' available. setupDefaultPool called earlier?");

			// This may happen when tests re-starts ConnectionPoolRegistry.
			return;
		}
		setDefaultPool(instanceMap, formerDefaultPool);
	}

	public static ConnectionPool installConnectionPool(String poolName, ConnectionPool currentDB) {
		ConnectionPool oldDB;
		Map<String, ConnectionPool> instanceMap = getPools();
		if (poolName.equals(ConnectionPoolRegistry.DEFAULT_POOL_NAME)) {
			oldDB = setDefaultPool(instanceMap, currentDB);
		} else {
			oldDB = installPool(instanceMap, poolName, currentDB);
		}
		return oldDB;
	}

	private static ConnectionPool installPool(Map<String, ConnectionPool> instanceMap, String poolName,
			ConnectionPool pool) {
		ConnectionPool oldDB;
		if (pool == null) {
			oldDB = instanceMap.remove(poolName);
		} else {
			oldDB = instanceMap.put(poolName, pool);
		}
		return oldDB;
	}

	private static Map<String, ConnectionPool> getPools() {
		return ReflectionUtils.getValue(registry(), "instanceByName", Map.class);
	}

	/**
	 * Typesafe access to {@link ConnectionPoolRegistry}.
	 */
	public static final TestingConnectionPoolRegistryAccess registry() {
		return (TestingConnectionPoolRegistryAccess) ConnectionPoolRegistry.Module.INSTANCE.getImplementationInstance();
	}
}