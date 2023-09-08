/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.util;

import java.io.IOException;

import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBForeignKey;
import com.top_logic.basic.db.model.DBIndex;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaPart;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.db.model.DBTablePart;
import com.top_logic.basic.db.model.visit.DefaultDBSchemaVisitor;
import com.top_logic.basic.io.WrappedIOException;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;

/**
 * Generation of an SQL script that creates a schema specified by a {@link DBSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class CreateStatementBuilder extends DefaultDBSchemaVisitor<Void, CreateStatementBuilder.Phase> {

	/**
	 * Phases of the SQL transformation.
	 */
	/*package protected*/ enum Phase {
		TableCreation,
		IndexCreation,
		ConstraintCreation
	}
	
	private static final char TAB = '\t';
	private static final String NL = "\n";
	
	private final Appendable buffer;
	private final DBHelper sqlDialect;

	/**
	 * Creates a {@link CreateStatementBuilder}.
	 * 
	 * @param sqlDialect
	 *        The SQL dialect in which create statements are produced.
	 * @param buffer
	 *        The buffer to which create statements are appended.
	 */
	public CreateStatementBuilder(DBHelper sqlDialect, Appendable buffer) {
		this.sqlDialect = sqlDialect;
		this.buffer = buffer;
	}

	/**
	 * Triggers creation of all
	 * 
	 * @param schemaPart
	 *        the {@link DBSchemaPart} for which create statements should be
	 *        generated.
	 * 
	 * @throws WrappedIOException
	 *         If writing to the given {@link Appendable} fails.
	 */
	public void createSQL(DBSchemaPart schemaPart) throws WrappedIOException {
		schemaPart.visit(this, Phase.TableCreation);
		schemaPart.visit(this, Phase.IndexCreation);
		schemaPart.visit(this, Phase.ConstraintCreation);
	}

	@Override
	public Void visitTable(DBTable model, Phase arg) throws WrappedIOException {
		try {
			switch (arg) {
			case TableCreation: {
				buffer.append("CREATE TABLE ");
				appendSchemaNamePrefix(model);
				buffer.append(sqlDialect.tableRef(model.getDBName()));
				buffer.append(" (");
				buffer.append(NL);
				for (int n = 0, cnt = model.getColumns().size(); n < cnt; n++) {
					DBColumn column = model.getColumns().get(n);
					
					if (n > 0) {
						buffer.append(',');
						buffer.append(NL);
					}
					
					buffer.append(TAB);
					column.visit(this, arg);
				}
				
				DBIndex primaryKey = model.getPrimaryKey();
				if (primaryKey != null) {
					buffer.append(',');
					buffer.append(NL);
					buffer.append(TAB);
					
					buffer.append("PRIMARY KEY");
					buffer.append(" (");
					for (int n = 0, cnt = primaryKey.getColumns().size(); n < cnt; n++) {
						DBColumn keyColumn = primaryKey.getColumns().get(n);

						if (n > 0) {
							buffer.append(", ");
						}
							// TODO #18743: Add binary for Oracle, here
							buffer.append(sqlDialect.columnRef(keyColumn.getDBName()));

					}
					buffer.append(')');
				}
				buffer.append(NL);
				
				buffer.append(')');
				buffer.append(NL);
				
				sqlDialect.appendTableOptions(buffer, model.getPKeyStorage(), model.getCompress());
				String comment = model.getComment();
	            if (comment != null) {
	                sqlDialect.appendComment(buffer, comment);
	            }
				completeStatement();
				break;
			}

			case IndexCreation: {
				descend(model.getIndices(), arg);
				break;
			}
				
			case ConstraintCreation: {
				descend(model.getForeignKeys(), arg);
				break;
			}
			}
		} catch (IOException ex) {
			throw new WrappedIOException(ex);
		}
		return null;
	}

	private void appendSchemaNamePrefix(DBSchemaPart model) throws IOException {
		String schemaName = model.getSchema().getDBName();
		if (schemaName != null) {
			buffer.append(schemaName);
			buffer.append('.');
		}
	}

	protected void completeStatement() throws IOException {
		buffer.append(';');
		buffer.append(NL);
		buffer.append(NL);
	}
	
	@Override
	public Void visitColumn(DBColumn model, Phase arg) throws WrappedIOException {
        try {
			DBType type = model.getType();
			long size = model.getSize();
			int prec = model.getPrecision();
			boolean mandatory = model.isMandatory();
			boolean binary = model.isBinary();
			String comment = model.getComment();

			buffer.append(sqlDialect.columnRef(model.getDBName()));
			buffer.append(TAB);
			sqlDialect.appendDBType(buffer, type, model.getDBName(), size, prec, mandatory, binary);
			if (comment != null) {
			    sqlDialect.appendComment(buffer, comment);
			}
		} catch (IOException ex) {
			throw new WrappedIOException(ex);
		}

		return super.visitColumn(model, arg);
	}

	@Override
	public Void visitForeignKey(DBForeignKey model, Phase arg) throws WrappedIOException {
		try {
			buffer.append("ALTER TABLE ");
			appendSchemaNamePrefix(model);
			buffer.append(sqlDialect.tableRef(model.getTable().getDBName()));
			buffer.append(" ADD CONSTRAINT ");
			buffer.append(sqlDialect.columnRef(qName(model)));
			buffer.append(" FOREIGN KEY ");
			buffer.append(" (");
			for (int n = 0, cnt = model.getSourceColumns().size(); n < cnt; n++) {
				DBColumn sourceColumn = model.getSourceColumns().get(n);
				
				if (n > 0) {
					buffer.append(", ");
				}
				buffer.append(sqlDialect.columnRef(sourceColumn.getDBName()));
			}
			buffer.append(')');

			buffer.append(NL);
			buffer.append(TAB);

			buffer.append("REFERENCES ");
			buffer.append(sqlDialect.tableRef(model.getTargetTable().getDBName()));
			buffer.append(" (");
			for (int n = 0, cnt = model.getTargetColumns().size(); n < cnt; n++) {
				DBColumn targetColumn = model.getTargetColumns().get(n);
				
				if (n > 0) {
					buffer.append(", ");
				}
				buffer.append(sqlDialect.columnRef(targetColumn.getDBName()));
			}
			buffer.append(')');

			buffer.append(NL);
			buffer.append(TAB);
			sqlDialect.onDelete(buffer, model.getOnDelete());
			
			buffer.append(NL);
			buffer.append(TAB);
			sqlDialect.onUpdate(buffer, model.getOnUpdate());
			
			completeStatement();
		} catch (IOException ex) {
			throw new WrappedIOException(ex);
		}
		
		return null;
	}

	@Override
	public Void visitIndex(DBIndex model, Phase arg) throws WrappedIOException {
		try {
			buffer.append("CREATE ");
			if (model.isUnique()) {
				buffer.append("UNIQUE ");
			}
			buffer.append("INDEX ");
			buffer.append(sqlDialect.columnRef(qName(model)));
			buffer.append(" ON ");
			appendSchemaNamePrefix(model);
			buffer.append(sqlDialect.tableRef(model.getTable().getDBName()));
			buffer.append('(');
			for (int n = 0, cnt = model.getColumns().size(); n < cnt; n++) {
				DBColumn column = model.getColumns().get(n);
				if (n > 0) {
					buffer.append(',');
				}
				buffer.append(sqlDialect.columnRef(column.getDBName()));
			}
			buffer.append(')');
			buffer.append(sqlDialect.getAppendIndex(model.getCompress()));
			completeStatement();
		} catch (IOException ex) {
			throw new WrappedIOException(ex);
		}
		return null;
	}

	private String qName(DBTablePart model) {
		return qName(sqlDialect, model);
	}

	/**
	 * Qualified name of the given part respecting {@link DBHelper#qualifiedName(String, String)}.
	 */
	public static String qName(DBHelper sqlDialect, DBTablePart model) {
		return sqlDialect.qualifiedName(model.getTable().getDBName(), model.getDBName());
	}

	@Override
	public Void visitSchema(DBSchema model, Phase arg) throws WrappedIOException {
		descend(model.getTables(), arg);
		return null;
	}

}
