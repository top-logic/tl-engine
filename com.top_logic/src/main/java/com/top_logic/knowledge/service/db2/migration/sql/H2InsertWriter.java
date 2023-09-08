/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * {@link InsertWriter} for creating dumps that can be loaded into a H2 database.
 */
public class H2InsertWriter extends DefaultInsertWriter {

	/**
	 * Maximum size of a string or binary literal inserted.
	 * 
	 * <p>
	 * The actual maximum supported by H2 is 1M.
	 * </p>
	 */
	private static final int CHUNK_SIZE = 256 * 1024;

	int _lobId;

	/**
	 * Creates a {@link H2InsertWriter}.
	 */
	public H2InsertWriter(DBHelper sqlDialect, Appendable out, int insertChunkSize) {
		super(sqlDialect, out, insertChunkSize);
	}

	@Override
	public void beginDump() throws IOException {
		// Creates temporary tables and procedures to work around a maximum string length limit for
		// literals.
		_out.append(
			"CREATE CACHED LOCAL TEMPORARY TABLE IF NOT EXISTS SYSTEM_LOB_STREAM(ID INT NOT NULL, PART INT NOT NULL, CDATA VARCHAR, BDATA VARBINARY);");
		newLine();
		_out.append(
			"ALTER TABLE SYSTEM_LOB_STREAM ADD CONSTRAINT SYSTEM_LOB_STREAM_PRIMARY_KEY PRIMARY KEY(ID, PART);");
		newLine();
		_out.append(
			"CREATE ALIAS IF NOT EXISTS SYSTEM_COMBINE_CLOB FOR 'org.h2.command.dml.ScriptCommand.combineClob';");
		newLine();
		_out.append(
			"CREATE ALIAS IF NOT EXISTS SYSTEM_COMBINE_BLOB FOR 'org.h2.command.dml.ScriptCommand.combineBlob';");
		newLine();

		super.beginDump();
	}

	@Override
	protected void internalAppendInsert(DBTable table, List<Object[]> values) throws IOException {
		// Cache LOB values in stream table to work around maximum literal limit.
		for (Object[] row : values) {
			for (int n = 0, cnt = row.length; n < cnt; n++) {
				Object value = row[n];
				if (value instanceof BinaryDataSource) {
					BinaryData data = BinaryData.cast(value);
					long size = data.getSize();
					if (size < 0 || size > CHUNK_SIZE) {
						int lobId = mkId();

						byte[] buffer = new byte[CHUNK_SIZE];
						try (InputStream in = data.getStream()) {
							int partId = 0;
							while (true) {
								int direct = in.read(buffer);
								if (direct < 0) {
									break;
								}

								_out.append("INSERT INTO ");
								_out.append("SYSTEM_LOB_STREAM");
								_out.append(" VALUES (");
								_sqlDialect.literal(_out, DBType.INT, lobId);
								_out.append(",");
								_sqlDialect.literal(_out, DBType.INT, partId++);
								_out.append(",");
								_sqlDialect.literal(_out, DBType.STRING, null);
								_out.append(",");
								_sqlDialect.literal(_out, DBType.BLOB,
									BinaryDataFactory.createBinaryData(buffer, 0, direct));
								_out.append(");");
								newLine();
							}
						}

						row[n] = new BlobRef(lobId);
					}
				} else if (table.getColumns().get(n).getType() == DBType.CLOB) {
					if (value instanceof String) {
						String str = (String) value;
						int length = str.length();
						if (length > CHUNK_SIZE) {
							int lobId = mkId();
							int offset = 0;
							int partId = 0;
							while (offset < length) {
								int end = Math.min(length, offset + CHUNK_SIZE);

								_out.append("INSERT INTO ");
								_out.append("SYSTEM_LOB_STREAM");
								_out.append(" VALUES (");
								_sqlDialect.literal(_out, DBType.INT, lobId);
								_out.append(",");
								_sqlDialect.literal(_out, DBType.INT, partId++);
								_out.append(",");
								_sqlDialect.literal(_out, DBType.STRING, str.substring(offset, end));
								_out.append(",");
								_sqlDialect.literal(_out, DBType.BLOB, null);
								_out.append(");");
								newLine();

								offset = end;
							}
							row[n] = new ClobRef(lobId);
						}
					}
				}
			}
		}

		super.internalAppendInsert(table, values);
	}

	private int mkId() {
		return _lobId++;
	}

	@Override
	public void endDump() throws IOException {
		super.endDump();

		_out.append("DROP TABLE IF EXISTS SYSTEM_LOB_STREAM;");
		_out.append("DROP ALIAS IF EXISTS SYSTEM_COMBINE_CLOB;");
		_out.append("DROP ALIAS IF EXISTS SYSTEM_COMBINE_BLOB;");
	}

	@Override
	protected void appendLiteral(DBType type, Object literal) {
		if (literal instanceof LobRef) {
			try {
				((LobRef) literal).appendTo(_out);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else {
			super.appendLiteral(type, literal);
		}
	}

	private abstract static class LobRef {
		protected final int _lobId;

		/**
		 * Creates a {@link LobRef}.
		 */
		public LobRef(int lobId) {
			_lobId = lobId;
		}

		public abstract void appendTo(Appendable out) throws IOException;
	}

	private static class BlobRef extends LobRef {
		/**
		 * Creates a {@link BlobRef}.
		 */
		public BlobRef(int lobId) {
			super(lobId);
		}

		@Override
		public void appendTo(Appendable out) throws IOException {
			out.append("SYSTEM_COMBINE_BLOB(" + _lobId + ")");
		}
	}

	private static class ClobRef extends LobRef {
		/**
		 * Creates a {@link BlobRef}.
		 */
		public ClobRef(int lobId) {
			super(lobId);
		}

		@Override
		public void appendTo(Appendable out) throws IOException {
			out.append("SYSTEM_COMBINE_CLOB(" + _lobId + ")");
		}

	}

}
