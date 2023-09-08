/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.Test;

import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * Performance test for {@link FlexDataManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DeactivatedTest("Performance Test for FlexDataManager. Including into nightly build would be too expensive.")
public class FlexManagerPerformanceTest extends AbstractDBKnowledgeBaseTest {

	private MOKnowledgeItemImpl flexDataType() {
		return kb().lookupType(AbstractFlexDataManager.FLEX_DATA);
	}

	/**
	 * Performance test to test deletion of multiple objects with many flex data.
	 */
	public void testBulkDelete() throws SQLException {
		long numberBulkDeleteObjects = Math.min(sqlDialect().getMaxSetSize(), 100000);
		long seed = 8275487265L;
		Random r = new Random(seed);

		MutableInteger sequence = new MutableInteger();
		List<KnowledgeObject> created = new ArrayList<>();
		for (int i = 0, size = randomNumberObjects(r); i < size; i++) {
			Logger.info("Start create transaction " + i + "/" + size, FlexManagerPerformanceTest.class);
			try (Transaction tx = begin()) {
				for (int j = 0, j_size = randomNumberObjects(r); j < j_size; j++) {
					created.add(createB(randomNumberAttributes(r), sequence));
				}
				for (int j = 0, j_size = randomNumberObjects(r); j < j_size; j++) {
					created.add(createC(randomNumberAttributes(r), sequence));
				}
				for (int j = 0, j_size = randomNumberObjects(r); j < j_size; j++) {
					created.add(createD(randomNumberAttributes(r), sequence));
				}
				for (int j = 0, j_size = randomNumberObjects(r); j < j_size; j++) {
					created.add(createE(randomNumberAttributes(r), sequence));
				}
				tx.commit();
			}
		}

		Collections.shuffle(created, r);

		loadOracleStatistics();

		List<KnowledgeObject> itemsToDelete = new ArrayList<>();
		Logger.info("Create elements for later deletion.", FlexManagerPerformanceTest.class);
		try (Transaction tx = begin()) {
			for (int j = 0; j < numberBulkDeleteObjects; j++) {
				itemsToDelete.add(createB(randomNumberAttributes(r), sequence));
			}
			tx.commit();
		}

		int i = 0;
		while (true) {
			try (Transaction tx = begin()) {
				int numberChangedElements = r.nextInt(1000);
				Logger.info("Update " + numberChangedElements + " of " + created.size() + " elements.",
					FlexManagerPerformanceTest.class);
				while (numberChangedElements-- > 0) {
					if (i < created.size() / 2) {
						updateFlexValue(created.get(i++), r, sequence);
					} else {
						break;
					}
				}
				tx.commit();
			}
			if (i >= created.size() / 2) {
				break;
			}
		}

		try (Transaction tx = begin()) {
			Logger.info("Update elements for later deletion.", FlexManagerPerformanceTest.class);
			for (KnowledgeItem itemToDelete : itemsToDelete) {
				updateFlexValue(itemToDelete, r, sequence);
			}
			tx.commit();
		}

		while (true) {
			try (Transaction tx = begin()) {
				int numberChangedElements = r.nextInt(1000);
				Logger.info("Update " + numberChangedElements + " of " + created.size() + " elements.",
					FlexManagerPerformanceTest.class);
				while (numberChangedElements-- > 0) {
					if (i < created.size()) {
						updateFlexValue(created.get(i++), r, sequence);
					} else {
						break;
					}
				}
				tx.commit();
			}
			if (i >= created.size()) {
				break;
			}
		}

		int numberRows;
		PooledConnection readConnection = kb().getConnectionPool().borrowReadConnection();
		try {
			try (Statement stmt = readConnection.createStatement()) {
				try (ResultSet query =
					stmt.executeQuery("Select count(*) from " + sqlDialect().tableRef(flexDataType().getDBName()))) {
					query.next();
					numberRows = query.getInt(1);
				}
			}
		} finally {
			kb().getConnectionPool().releaseReadConnection(readConnection);
		}

		StopWatch sw = StopWatch.createStartedWatch();
		try (Transaction tx = begin()) {
			Logger.info("Delete test objects.", FlexManagerPerformanceTest.class);
			for (KnowledgeItem item : itemsToDelete) {
				item.delete();
			}
			tx.commit();
		}
		sw.stop();
		fail("Deleting " + itemsToDelete.size() + " elements needed " + sw + ". Number rows: " + numberRows + ". KB: "
				+ kb());
	}

	/**
	 * Note: Oracle seems to be a very strange database. It is necessary to force ORACLE to update
	 * its statistics, because otherwise the correct index is not used. Updating the statistics must
	 * occur when there are data in the database.
	 * 
	 * <p>
	 * When executing this test, entering this method can be a good time to update the statistics:
	 * </p>
	 * 
	 * <pre>
	 * EXEC DBMS_STATS.GATHER_SCHEMA_STATS(ownname=> '<owner_name>' ,cascade=> TRUE,estimate_percent=> DBMS_STATS.AUTO_SAMPLE_SIZE,granularity=>'AUTO',method_opt=> 'FOR ALL COLUMNS SIZE AUTO');
	 * </pre>
	 */
	private void loadOracleStatistics() {
		// Add break point here to update statistics in SQL-Developer
		kb().getConnectionPool().clear();
	}

	private DBHelper sqlDialect() throws SQLException {
		return kb().getConnectionPool().getSQLDialect();
	}

	private void updateFlexValue(KnowledgeItem item, Random r, MutableInteger sequence) {
		for (int i = 0; i < 15; i++) {
			String value;
			if (r.nextInt(10) < 6) {
				value = flexAttrValue(sequence);
			} else {
				value = null;
			}
			item.setAttributeValue(flexAttrName(i), value);
		}
	}

	private int randomNumberObjects(Random r) {
		return 100 + r.nextInt(100);
	}

	private int randomNumberAttributes(Random r) {
		return 5 + r.nextInt(10);
	}

	private void setAttributes(KnowledgeObject item, int numberAttributes, MutableInteger sequence) {
		for (int i = 0, size = numberAttributes; i < size; i++) {
			item.setAttributeValue(flexAttrName(i), flexAttrValue(sequence));
		}
	}

	private String flexAttrValue(MutableInteger sequence) {
		return "value_" + sequence.inc();
	}

	private String flexAttrName(int i) {
		return "flexAttr_" + i;
	}

	private KnowledgeObject createB(int numberAttributes, MutableInteger sequence) {
		KnowledgeObject newB = newB("b_" + sequence.inc());
		setAttributes(newB, numberAttributes, sequence);
		return newB;
	}

	private KnowledgeObject createC(int numberAttributes, MutableInteger sequence) {
		KnowledgeObject newC = newC("c_" + sequence.inc());
		setAttributes(newC, numberAttributes, sequence);
		return newC;
	}

	private KnowledgeObject createD(int numberAttributes, MutableInteger sequence) {
		KnowledgeObject newC = newD("d_" + sequence.inc());
		setAttributes(newC, numberAttributes, sequence);
		return newC;
	}

	private KnowledgeObject createE(int numberAttributes, MutableInteger sequence) {
		KnowledgeObject newC = newE("e_" + sequence.inc());
		setAttributes(newC, numberAttributes, sequence);
		return newC;
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link FlexManagerPerformanceTest}.
	 */
	public static Test suite() {
		return AbstractDBKnowledgeBaseTest.suite(FlexManagerPerformanceTest.class);
	}

}

