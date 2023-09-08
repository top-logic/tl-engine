/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.sql.DBHelper;

/**
 * {@link DefaultInsertWriter} with special optimisations for MySQL dialect.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MySQLInsertWriter extends DefaultInsertWriter {

	/**
	 * Creates a new {@link MySQLInsertWriter}.
	 */
	public MySQLInsertWriter(DBHelper sqlDialect, Appendable out, int insertChunkSize) {
		super(sqlDialect, out, insertChunkSize);
	}

	@Override
	public void beginDump() throws IOException {
		super.beginDump();
		appendLine("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
		appendLine("/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
		appendLine("/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
		appendLine("/*!40101 SET NAMES utf8 */;");
		appendLine("/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
		appendLine("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
		appendLine("/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
		appendLine("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
	}

	@Override
	public void endDump() throws IOException {
		appendLine("/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
		appendLine("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
		appendLine("/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
		appendLine("/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
		appendLine("/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
		appendLine("/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
		appendLine("/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
		super.endDump();
	}

	@Override
	protected void internalAppendInsert(DBTable table, List<Object[]> values) throws IOException {
		String tableRef = _sqlDialect.tableRef(table.getDBName());
		appendLine("LOCK TABLES " + tableRef + " WRITE;");
		appendLine("/*!40000 ALTER TABLE " + tableRef + " DISABLE KEYS */;");

		super.internalAppendInsert(table, values);

		appendLine("/*!40000 ALTER TABLE " + tableRef + " ENABLE KEYS */;");
		appendLine("UNLOCK TABLES;");
	}

}

