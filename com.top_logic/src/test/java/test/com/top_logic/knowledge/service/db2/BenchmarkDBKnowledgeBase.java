/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import junit.framework.Test;

import test.com.top_logic.basic.sql.log.LoggingDataSourceTestUtil;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.LoggingDataSourceProxy;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Test case for benchmarking {@link DBKnowledgeBase} performance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BenchmarkDBKnowledgeBase extends AbstractDBKnowledgeBaseClusterTest {

	private static final int LOOPS = 200;

	private static final int FANOUT = 5;

	private static final int DEPTH = 5;

	private static final String[] ATTRIBUTES = attributes(15);

	private static final int CHANGES = 5;

	private static final int CHANGES_PER_NODE = 3;

	private static final int SOURCE_CNT = 1000;

	private static final int DEST_CNT = 50;

	private static final int CONNECTION_CNT = 10;

	private int _id;

	private int _value;

	private Random _rnd = new Random(42);

	public void testOldObjects() throws SQLException {
		BObj[] sources = newBs(SOURCE_CNT);
		BObj[] targets = newBs(DEST_CNT);

		logStatements();

		StopWatch watch = new StopWatch();
		for (int n = 0; n < LOOPS; n++) {
			watch.reset();
			watch.start();
			connect(sources, targets, CONNECTION_CNT);
			watch.stop();
			log("Connecting round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			watch.reset();
			watch.start();
			List<KnowledgeItem> connections = lookupConnections(sources);
			watch.stop();
			log("Lookup round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			watch.reset();
			watch.start();
			delete(connections);
			watch.stop();
			log("Cleaning round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			logTableSizes();
		}
	}

	private void delete(List<KnowledgeItem> connections) throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		try {
			for (KnowledgeItem item : connections) {
				item.delete();
			}
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	private List<KnowledgeItem> lookupConnections(BObj[] sources) {
		ArrayList<KnowledgeItem> result = new ArrayList<>();
		for (int n = 0, cnt = sources.length; n < cnt; n++) {
			Iterable<KnowledgeAssociation> links =
				CollectionUtil.toIterable(sources[n].tHandle().getOutgoingAssociations(AB_NAME));
			for (KnowledgeItem link : links) {
				result.add(link);
			}
		}
		return result;
	}

	private void connect(BObj[] sources, BObj[] targets, int connectionCnt) throws KnowledgeBaseException {
		Transaction tx = kb().beginTransaction();
		try {
			for (int n = 0, cnt = sources.length; n < cnt; n++) {
				connetTo(sources[n], targets, connectionCnt);
			}
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	private void connetTo(BObj src, BObj[] targets, int connectionCnt) {
		for (int n = 0; n < connectionCnt; n++) {
			src.addAB(targets[_rnd.nextInt(targets.length)]);
		}
	}

	private BObj[] newBs(int cnt) {
		BObj[] result = new BObj[cnt];
		for (int n = 0; n < cnt; n++) {
			result[n] = newB();
		}
		return result;
	}

	public void testObjectLifecycle() throws SQLException {
		logStatements();

		StopWatch watch = new StopWatch();
		for (int n = 0; n < LOOPS; n++) {
			watch.reset();
			watch.start();
			BObj root = createTree();
			watch.stop();
			log("Creating round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			watch.reset();
			watch.start();
			performChanges(root);
			watch.stop();
			log("Changing round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			watch.reset();
			watch.start();
			queryAssociations(root);
			watch.stop();
			log("Querying round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			watch.reset();
			watch.start();
			deleteTree(root);
			watch.stop();
			log("Deleting round " + n + ": " + StopWatch.toStringNanos(watch.getElapsedNanos()));
			logStatements();

			logTableSizes();
		}

	}

	protected void logTableSizes() throws SQLException, UnknownTypeException {
		log("--- Table size: B: " + getTableSize(B_NAME) + ", AB: " + getTableSize(AB_NAME)
			+ ", FLEX: " + getTableSize(AbstractFlexDataManager.FLEX_DATA));
	}

	private void log(String message) {
		Logger.info(message, BenchmarkDBKnowledgeBase.class);
	}

	private void logStatements() {
		DataSource ds = kb().getConnectionPool().getDataSource();
		if (ds instanceof LoggingDataSourceProxy) {
			LoggingDataSourceProxy loggingDs = (LoggingDataSourceProxy) ds;
			LoggingDataSourceTestUtil.flush(loggingDs);
		}
	}

	protected BObj createTree() {
		Transaction tx = kb().beginTransaction();
		try {
			BObj root = newB();
			createTree(root, FANOUT, DEPTH);
			tx.commit();
			return root;
		} finally {
			tx.rollback();
		}
	}

	private void createTree(BObj parent, int fanout, int depth) {
		if (depth == 0) {
			return;
		}

		for (int n = 0; n < fanout; n++) {
			BObj node = newB();
			parent.addAB(node);

			createTree(node, fanout, depth - 1);
		}
	}

	protected void performChanges(BObj root) throws KnowledgeBaseException {
		for (int n = 0; n < CHANGES; n++) {
			changeTree(root, CHANGES_PER_NODE);
		}
	}

	private void changeTree(BObj root, int changesPerNode) throws KnowledgeBaseException {
		Transaction tx = kb().beginTransaction();
		try {
			changeNode(root, changesPerNode);
			for (Object child : root.getAB()) {
				changeTree((BObj) child, changesPerNode);
			}
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	protected void changeNode(BObj root, int changesPerNode) {
		for (int n = 0; n < changesPerNode; n++) {
			setRandomValue(root);
		}
	}

	private void queryAssociations(BObj root) {
		for (Object child : root.getAB()) {
			BObj bChild = (BObj) child;

			int cnt = 0;
			Iterator<KnowledgeAssociation> it =
				root.tHandle().getOutgoingAssociations(AB_NAME, bChild.tHandle());
			while (it.hasNext()) {
				it.next();
				cnt++;
			}
			assertEquals(1, cnt);

			queryAssociations(bChild);
		}
	}

	private void deleteTree(BObj root) {
		Transaction tx = kb().beginTransaction();
		try {
			for (Object child : root.getAB()) {
				deleteTree((BObj) child);
			}
			root.tDelete();
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	protected int getTableSize(String typeName) throws SQLException, UnknownTypeException {
		ConnectionPool pool = kb().getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try {
			PreparedStatement statement =
				connection.prepareStatement("SELECT count(1) FROM "
					+ pool.getSQLDialect().tableRef(
						((DBTableMetaObject) kb().getMORepository().getMetaObject(typeName)).getDBName()));
			try {
				ResultSet query = statement.executeQuery();
				try {
					query.next();
					return query.getInt(1);
				} finally {
					query.close();
				}
			} finally {
				statement.close();
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	protected BObj newB() {
		return BObj.newBObj("b" + _id++);
	}

	protected void setRandomValue(BObj root) {
		root.setValue(ATTRIBUTES[_rnd.nextInt(ATTRIBUTES.length)], newValue());
	}

	private String newValue() {
		return "Some value " + (_value++);
	}

	private static String[] attributes(int cnt) {
		String[] result = new String[cnt];
		for (int n = 0; n < cnt; n++) {
			result[n] = "attribute" + n;
		}
	
		// Use some row-level attributes.
		result[0] = A2_NAME;
		result[1] = B1_NAME;
		result[2] = B2_NAME;
		result[3] = B3_NAME;
		result[4] = B3_NAME;
	
		return result;
	}

	public static Test suite() {
		return suite(BenchmarkDBKnowledgeBase.class);
    }
	
}
