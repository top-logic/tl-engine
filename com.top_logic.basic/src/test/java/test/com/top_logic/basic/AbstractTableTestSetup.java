/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.sql.SQLException;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBSetup;

/**
 * Base class for {@link TestSetup}s that use {@link DBSetup} to create database
 * tables in a platform dependent manner.
 * 
 * <p>
 * Prerequisite: Some setup that selects the {@link ConnectionPool} to use for
 * table creation.
 * </p>
 * 
 * @see ModuleTestSetup Setting up the default connection pool.
 * @see DatabaseTestSetup Setting up a cutom connection pool for cross database
 *      testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTableTestSetup extends RearrangableThreadContextSetup {

	/**
	 * Creates a {@link AbstractTableTestSetup}.
	 *
	 * @param test The test to wrap.
	 * @param setupCnt The setup counter to use for prevention of multiple nested setups.
	 */
	public AbstractTableTestSetup(Test test, MutableInteger setupCnt) {
		super(test, setupCnt);
	}

	@Override
	public void doSetUp() throws Exception {
		DBSetup theSetup = new DBSetup(getFeature());       
        
		ConnectionPool connectionPool = ConnectionPoolRegistry.getDefaultConnectionPool();
        try {
			theSetup.doDropTables(connectionPool);
		} catch (SQLException ex) {
			Logger.info("Dropping '" + getFeature() + "' tables failed (maybe not yet set up).", AbstractTableTestSetup.class);
		}
        theSetup.doCreateTables(connectionPool);
	}
	
	@Override
	public void doTearDown() throws Exception {
		// Table creation is reverted in next setup to make post mortem
		// debugging more easy.
	}

	protected abstract String getFeature();
	
}
