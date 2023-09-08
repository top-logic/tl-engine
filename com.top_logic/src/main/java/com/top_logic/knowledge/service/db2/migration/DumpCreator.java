/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaFactory;
import com.top_logic.basic.db.model.util.SchemaExtraction;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.SequenceTypeProvider;

/**
 * Dumps all changes in the {@link PersistencyLayer} into an XML file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DumpCreator extends Tool implements DumpSchemaConstants {
	
	/**
	 * Option specifying the output file name.
	 * 
	 * <p>
	 * Using the name <code>-</code>, writes to the standard output.
	 * </p>
	 */
	public static final String OPTION_OUT = "out";

	/**
	 * Option enabling GZip compression of the output.
	 */
	public static final String OPTION_GZIP = "gzip";

	/**
	 * Option setting flush sequence. Value must be an integer. After this number of change sets,
	 * the content is flushed to file.
	 */
	public static final String OPTION_FLUSH_SEQUENCE = "gzip";
	
	private static final String GZIP_EXTENSION = ".gz";
	
	private String _fileName = "dump.xml";
	
	private boolean _gzip = false;

	private int _flushSequence = 1000;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (OPTION_OUT.equals(option)) {
			_fileName = args[i++];
			return i;
		}
		if (OPTION_GZIP.equals(option)) {
			_gzip = true;
			return i;
		}
		if (OPTION_FLUSH_SEQUENCE.equals(option)) {
			int flushSequence = Integer.parseInt(args[i++]);
			if (flushSequence < 1) {
				getProtocol().error("Flush sequence must be greater than 0");
			} else {
				_flushSequence = flushSequence;
			}
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void setUp(String[] args) throws Exception {
		super.setUp(args);
		addCustomModule(PersistencyLayer.Module.INSTANCE);
	}

	@Override
	protected void runTool() throws IOException {
		if ("-".equals(_fileName)) {
			dump(System.out);
		} else {
			String fileName = _fileName;
			if (_gzip && !fileName.endsWith(GZIP_EXTENSION)) {
				fileName += GZIP_EXTENSION;
			}
			try (OutputStream out = new FileOutputStream(new File(fileName))) {
				if (_gzip) {
					GZIPOutputStream gzipOut = new GZIPOutputStream(out);
					dump(gzipOut);
					gzipOut.finish();
				} else {
					dump(out);
				}
			}
		}
	}

	private void dump(OutputStream out) throws IOException {
		KnowledgeBaseDumper dumper = new KnowledgeBaseDumper(PersistencyLayer.getKnowledgeBase()) {

			@Override
			protected DBSchema getNonKBTables(PooledConnection connection, MORepository typeSystem)
					throws SQLException {
				Set<String> kbTables = getKBTables(typeSystem);
				SchemaExtraction extraction = new SchemaExtraction(connection.getMetaData(), connection.getSQLDialect());
				DBSchema dumpSchema = DBSchemaFactory.createDBSchema();
				for (String tableName : getCopyTables(connection, kbTables)) {
					extraction.addTable(dumpSchema, tableName);
				}
				return dumpSchema;
			}

			private List<String> getCopyTables(PooledConnection readConnection, Set<String> ignoredTables)
					throws SQLException {
				DBHelper sqlDialect = readConnection.getSQLDialect();
				String catalog = null;
				String schemaPattern = sqlDialect.getCurrentSchema(readConnection);
				String tableNamePattern = "%";
				String[] types = { "TABLE", "VIEW" };
				DatabaseMetaData metaData = readConnection.getMetaData();
				try (ResultSet tables = metaData.getTables(catalog, schemaPattern, tableNamePattern, types)) {
					List<String> additionalTables = new ArrayList<>();
					while (tables.next()) {
						String tableName = tables.getString("TABLE_NAME");
						if (ignoredTables.contains(tableName.toLowerCase())) {
							continue;
						}
						additionalTables.add(tableName);
					}
					return additionalTables;
				}
			}

			private Set<String> getKBTables(MORepository typeSystem) {
				Set<String> ignoreTables = new HashSet<>();
				for (MetaObject type : typeSystem.getMetaObjects()) {
					if (!(type instanceof MOClass)) {
						continue;
					}

					if (type.getName().equals(SequenceTypeProvider.SEQUENCE_TYPE_NAME)) {
						// Sequence must also be dumped because it also contains application
						// sequences,
						// like NumberHandler values.
						continue;
					}

					MOClass moClass = (MOClass) type;
					DBTableMetaObject dbMapping = moClass.getDBMapping();
					ignoreTables.add(dbMapping.getDBName().toLowerCase());
				}
				return ignoreTables;
			}

		};
		dumper.setIgnoreTypes(Collections.singleton(SecurityStorage.SECURITY_STORAGE_OBJECT_NAME));
		dumper.setLog(this);
		dumper.setFlushSequence(_flushSequence);
		dumper.dump(out);
	}

	public static void main(String[] args) throws Exception {
		new DumpCreator().runMainCommandLine(args);
	}

}
