/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLLoader;
import com.top_logic.basic.util.StopWatch;

/**
 * {@link Tool} to import an SQL file using the JDBC API.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImportSQLFile extends Tool {

	/**
	 * Option specifying the name of the dump XML file to load.
	 */
	public static final String OPTION_IN = "in";

	/**
	 * Option specifying the name of the dump XML file to load.
	 */
	public static final String OPTION_CONNECTION_POOL = "connectionPool";

	/**
	 * Option specifying encoding of the output SQL file contents.
	 */
	public static final String OPTION_ENCODING = "encoding";

	private String _dumpFileName;

	private String _encoding = StringServices.UTF8;

	private String _poolName;

	private ConnectionPool _pool;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (OPTION_IN.equals(option)) {
			_dumpFileName = args[i++];
			return i;
		}
		if (OPTION_ENCODING.equals(option)) {
			_encoding = args[i++];
			return i;
		}
		if (OPTION_CONNECTION_POOL.equals(option)) {
			_poolName = args[i++];
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void runTool() throws Exception {
		Log log = new LogProtocol(ImportSQLFile.class);
		PooledConnection con = _pool.borrowWriteConnection();
		try {
			{
				BooleanFlag terminated = new BooleanFlag(false);
				try {
					startHeartbeat(log, terminated);
					StopWatch sw = StopWatch.createStartedWatch();
					log.info("Import started: " + new Date());
					try (Reader inReader = getDumpReader()) {
						new SQLLoader(con).setLog(log).setContinueOnError(true).executeSQL(inReader);
					}
					con.commit();
					log.info("Import finished in " + sw + ": " + new Date());
				} finally {
					synchronized (terminated) {
						terminated.set(true);
						terminated.notifyAll();
					}
				}
			}
		} finally {
			_pool.releaseWriteConnection(con);
		}

	}

	private void startHeartbeat(Log l, BooleanFlag finished) {
		new Thread() {
			@Override
			public void run() {
				synchronized (finished) {
					while (!finished.get()) {
						try {
							finished.wait(TimeUnit.MINUTES.toMillis(10));
						} catch (InterruptedException ex) {
							ex.printStackTrace();
							break;
						}
						l.info("Now: " + new Date());
					}
				}
			}
		}.start();
	}

	private Reader getDumpReader() throws IOException {
		InputStream in = new FileInputStream(_dumpFileName);
		if (_dumpFileName.endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		return new InputStreamReader(in, _encoding);
	}

	@Override
	protected void setUp(String[] args) throws Exception {
		super.setUp(args);
		addCustomModule(ConnectionPoolRegistry.Module.INSTANCE);
		addCustomModule(LoggerAdminBean.Module.INSTANCE);
		if (StringServices.isEmpty(_dumpFileName)) {
			throw new IllegalArgumentException("Option '" + OPTION_IN + "' not set.");
		}
		if (StringServices.isEmpty(_poolName)) {
			throw new IllegalArgumentException("Option '" + OPTION_CONNECTION_POOL + "' not set.");
		}
		_pool = ConnectionPoolRegistry.getConnectionPool(_poolName);
	}

	public static void main(String[] args) throws Exception {
		new ImportSQLFile().runMainCommandLine(args);
	}

}

