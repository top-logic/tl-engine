/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import static com.top_logic.knowledge.service.KBUtils.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.migration.model.refactor.MoveObjectsProcessor;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link MoveObjectsProcessor}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMoveObjectsProcessor extends BasicTestCase {

	private static final Class<TestMoveObjectsProcessor> THIS_CLASS = TestMoveObjectsProcessor.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String TYPE_NAME = "Node";

	private static final String LABEL_ATTRIBUTE = "label";

	private static final String CONTENT_ATTRIBUTE = "content";

	/** The table the objects are moved to, declared in <code>TestMoveObjectsProcessorMeta.xml</code>. */
	private static final String TARGET_TABLE = "TestMoveTarget";

	private TLObject _node;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> {
			_node = DynamicModelService.getInstance().createObject(getNodeType());
			_node.tUpdateByName(LABEL_ATTRIBUTE, "moved");
			_node.tUpdateByName(CONTENT_ATTRIBUTE,
				BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 }, "text/plain", "content.txt"));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> _node.tDelete());
		super.tearDown();
	}

	/**
	 * The values of dynamic attributes, also the binary ones stored in a table of their own, move
	 * with their object.
	 */
	public void testMoveDynamicValues() throws SQLException {
		String sourceTable = TLAnnotations.getTable(getNodeType());
		TLID id = _node.tId().getObjectName();

		assertEquals(1, countRows(sourceTable, id));
		assertEquals(1, countFlexRows(AbstractFlexDataManager.FLEX_DATA_DB_NAME, sourceTable, id));
		assertEquals(1, countFlexRows(AbstractFlexDataManager.FLEX_BINARY_DATA_DB_NAME, sourceTable, id));

		move(sourceTable, TARGET_TABLE);
		try {
			assertEquals(0, countRows(sourceTable, id));
			assertEquals(1, countRows(TARGET_TABLE, id));
			for (String flexTable : List.of(AbstractFlexDataManager.FLEX_DATA_DB_NAME,
				AbstractFlexDataManager.FLEX_BINARY_DATA_DB_NAME)) {
				assertEquals("Values left behind in '" + flexTable + "'.", 0,
					countFlexRows(flexTable, sourceTable, id));
				assertEquals("Values not moved in '" + flexTable + "'.", 1,
					countFlexRows(flexTable, TARGET_TABLE, id));
			}
		} finally {
			// Restore the state the knowledge base has cached.
			move(TARGET_TABLE, sourceTable);
		}
		assertEquals(1, countRows(sourceTable, id));
		assertEquals(1, countFlexRows(AbstractFlexDataManager.FLEX_BINARY_DATA_DB_NAME, sourceTable, id));
	}

	private void move(String sourceTable, String destTable) throws SQLException {
		MoveObjectsProcessor.Config<?> config = TypedConfiguration.newConfigItem(MoveObjectsProcessor.Config.class);
		config.setSourceTable(sourceTable);
		config.setDestTable(destTable);
		QualifiedTypeName typeName = TypedConfiguration.newConfigItem(QualifiedTypeName.class);
		typeName.setName(MODULE_NAME + ":" + TYPE_NAME);
		config.setTypes(List.of(typeName));
		MoveObjectsProcessor processor = TypedConfigUtil.createInstance(config);

		ConnectionPool pool = KBUtils.getConnectionPool(kb());
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public MORepository getPersistentRepository() {
					return kb().getMORepository();
				}
			};
			processor.doMigration(context, log, connection);
			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
		log.checkErrors();
	}

	private int countRows(String tableName, TLID id) throws SQLException {
		MOStructure table = (MOStructure) kb().getMORepository().getTypeOrNull(tableName);
		return count(table.getDBMapping().getDBName(), BasicTypes.IDENTIFIER_DB_NAME, id, null);
	}

	private int countFlexRows(String flexTable, String tableName, TLID id) throws SQLException {
		return count(flexTable, AbstractFlexDataManager.IDENTIFIER_DBNAME, id, tableName);
	}

	private int count(String dbTable, String idColumn, TLID id, String type) throws SQLException {
		ConnectionPool pool = KBUtils.getConnectionPool(kb());
		PooledConnection connection = pool.borrowReadConnection();
		try {
			DBHelper sql = connection.getSQLDialect();
			String query = "SELECT COUNT(*) FROM " + sql.tableRef(dbTable) + " WHERE " + sql.columnRef(idColumn)
				+ " = ?";
			if (type != null) {
				query += " AND " + sql.columnRef(AbstractFlexDataManager.TYPE_DBNAME) + " = ?";
			}
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				IdentifierUtil.setId(statement, 1, id);
				if (type != null) {
					statement.setString(2, type);
				}
				try (ResultSet result = statement.executeQuery()) {
					assertTrue(result.next());
					return result.getInt(1);
				}
			}
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static TLClass getNodeType() {
		return (TLClass) ModelService.getInstance().getModel().getModule(MODULE_NAME).getType(TYPE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		String configFileName = THIS_CLASS.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(THIS_CLASS, configFileName);
		Test customConfigSetup = TestUtils.doNotMerge(new CustomPropertiesSetup(kbSetup, createFilePath, true));
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}
}
