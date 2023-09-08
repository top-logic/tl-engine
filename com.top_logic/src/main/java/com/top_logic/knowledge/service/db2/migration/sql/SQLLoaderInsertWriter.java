/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.sql;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.Environment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.db.model.DBColumn;
import com.top_logic.basic.db.model.DBTable;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.time.TimeZones;

/**
 * Special {@link InsertWriter} which does not produce literal insert SQL statements but control
 * files for the ORACLE SQL*Loader.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLLoaderInsertWriter implements InsertWriter {

	/**
	 * Helper class holding for a {@link DBType#DOUBLE}, {@link DBType#FLOAT}, or
	 * {@link DBType#DECIMAL} column the numbers of digits that are maximal needed.
	 */
	private static class FloatingPointPrecision {

		/** The maximal number of digits before '.' */
		int _intDigits = 1;

		/** The maximal number of digits after '.' */
		int _decimalDigits = 1;

		FloatingPointPrecision() {
			// Make constructor public to avoid "private access" warning
		}

		void ensureIntDigit(int digit) {
			_intDigits = Math.max(_intDigits, digit);
		}

		void ensureDecimalDigit(int digit) {
			_decimalDigits = Math.max(_decimalDigits, digit);
		}

		@Override
		public String toString() {
			return "FloatingPointPrecision [_intDigits=" + _intDigits + ", _decimalDigits=" + _decimalDigits + "]";
		}

	}

	/**
	 * System or environment variable to use {@link SQLLoaderInsertWriter} instead of ordinary SQL
	 * insert scripts.
	 */
	public static final String USE_SQL_LOADER_SYSTEM_PROPERTY = "tl_migration_sqlLoader_useSQLLoader";

	/**
	 * System or environment variable to define the output directory.
	 */
	public static final String SQL_LOADER_OUTPUT_DIR_PROPERTY = "tl_migration_sqlLoader_outputDir";

	private static final char FIELD_SEPARATOR = ',';

	private static final String ROW_TERMINATOR = "|\n";

	private static final String ROW_TERMINATOR_ESCAPED = StringServices.escape(ROW_TERMINATOR);

	private static final String FILE_ENCODING = "UTF8";

	private static final String BAD_DIR_NAME = "bad";

	private final File _outDir;

	private int _nextLobIndexFilename = 0;

	private Map<DBTable, Writer> _dataStreams = new HashMap<>();

	private Map<String, DBTable> _chachedTables = new HashMap<>();

	private Map<DBColumn, Object> _maxSize = new HashMap<>();

	/**
	 * {@link Formatter} to format floating point numbers.
	 * 
	 * @see #formatFloatingPoint(DBColumn, Number)
	 */
	private final Formatter _doubleFormattter = new Formatter(new StringBuilder(), Locale.US);

	private final char _decimalSeparator =
		DecimalFormatSymbols.getInstance(_doubleFormattter.locale()).getDecimalSeparator();
	{
		if (_decimalSeparator == FIELD_SEPARATOR) {
			throw new IllegalStateException(
				FIELD_SEPARATOR + " is the separator for fields. It can not also be used as separator for decimals.");
		}
	}


	/**
	 * Format to format values of type {@link DBType#DATE}, {@link DBType#DATETIME}, and
	 * {@link DBType#TIME}.
	 * 
	 * @see #sqlLoaderColumnDef(Appendable, DBColumn)
	 */
	private Format _dateTimeFormat = dateFormat("yyyyMMdd HHmmssSSS'000'");

	private static final String[] ZEROS = {
		createZeros(0),
		createZeros(1),
		createZeros(2),
		createZeros(3),
		createZeros(4),
		createZeros(5),
		createZeros(6),
		createZeros(7)
	};

	private DBHelper _sqlDialect;

	/**
	 * Creates a new {@link SQLLoaderInsertWriter}.
	 */
	public SQLLoaderInsertWriter(DBHelper sqlDialect) {
		this(sqlDialect, createOutDir());
	}

	/**
	 * Creates a new {@link SQLLoaderInsertWriter}.
	 * @param outputDir
	 *        Directory to place insert scripts. Must exist and be empty.
	 */
	public SQLLoaderInsertWriter(DBHelper sqlDialect, File outputDir) {
		_sqlDialect = sqlDialect;
		checkOutputDir(outputDir);
		_outDir = outputDir;
	}

	private static void checkOutputDir(File outputDir) {
		try {
			if (FileUtilities.list(outputDir).length > 0) {
				throw new IllegalArgumentException("Not empty: " + outputDir.getAbsolutePath());
			}
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	private static File createOutDir() {
		String outputDirProp = Environment.getSystemPropertyOrEnvironmentVariable(SQL_LOADER_OUTPUT_DIR_PROPERTY, null);
		File outDir;
		if (outputDirProp != null) {
			outDir = new File(outputDirProp);
		} else {
			outDir = new File("loaderScripts");
			if (outDir.exists()) {
				int i = 1;
				StringBuffer temp = new StringBuffer(outDir.getName());
				temp.append('_');
				int size = temp.length();
				while (true) {
					temp.append(i);
					outDir = new File(temp.toString());
					if (!outDir.exists()) {
						break;
					}
					temp.setLength(size);
					i++;
				}
			}
		}
		outDir.mkdir();
		return outDir;
	}

	@Override
	public void endDump() throws IOException {
		new File(_outDir, BAD_DIR_NAME).mkdir();
		writeLoaderScript();
		_dataStreams.entrySet().forEach(entry -> {
			try {
				entry.getValue().close();
				writeControlFile(entry.getKey());
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		});
		_dataStreams.clear();
	}

	private void writeLoaderScript() throws IOException {
		DBTable[] tables = _dataStreams.keySet().toArray(new DBTable[_dataStreams.size()]);
		Arrays.sort(tables, (o1, o2) -> o1.getDBName().compareTo(o2.getDBName()));
		writeBatchScript(tables);
		writeShellScript(tables);
	}

	private void writeShellScript(DBTable[] tables) throws IOException {
		try (Writer out = newFileWriter("sqlLoader.sh")) {
			appendLine(out, "#!/bin/sh");
			appendLine(out, "if [ -z \"$1\" ] || [ -z \"$2\" ]");
			appendLine(out, "then");
			appendLine(out,
				"echo \"Missing arguments: First must be the path to sqlldr.sh file, the second the user id to connect to oracle in the format <user>/<passswd>@<server>.\"");
			appendLine(out, "else");
			appendLine(out, "SQLLDR_CMD=$1");
			appendLine(out, "USERID=$2");
			appendLine(out, "echo Import started:");
			appendLine(out, "date");
			for (DBTable table : tables) {
				appendLine(out,
					"$SQLLDR_CMD USERID=$USERID CONTROL=" + controlFileName(table) + " BAD=" + badFile(table, '/'));
			}
			appendLine(out, "echo Import ended:");
			appendLine(out, "date");
			appendLine(out, "echo Please check '" + BAD_DIR_NAME + "'-folder for potential problems.");
			appendLine(out, "fi");
		}
	}

	private void writeBatchScript(DBTable[] tables) throws IOException {
		try (Writer out = newFileWriter("sqlLoader.bat")) {
			appendLine(out, "@echo off");
			appendLine(out, "if %1.==. goto no_args");
			appendLine(out, "if %2.==. goto no_args");
			newLine(out);
			appendLine(out, "SET SQLLDR_CMD=%1");
			appendLine(out, "SET USERID=%2");
			newLine(out);
			appendLine(out, "echo Import started: %DATE% %TIME%");
			for (DBTable table : tables) {
				appendLine(out,
					"%SQLLDR_CMD% USERID=%USERID% CONTROL=" + controlFileName(table) + " BAD=" + badFile(table, '\\'));
			}
			appendLine(out, "echo Import ended: %DATE% %TIME%");
			appendLine(out, "echo Please check '" + BAD_DIR_NAME + "'-folder for potential problems.");
			appendLine(out, "goto:eof");
			newLine(out);
			appendLine(out, ":no_args");
			appendLine(out,
				"echo \"Missing arguments: First must be the path to sqlldr.exe file, the second the user id to connect to oracle in the format <user>/<passswd>@<server>.\"");
		}
	}

	@Override
	public void appendInsert(DBTable table, List<Object[]> values) throws IOException {
		table = getCachedTable(table);
		Appendable out = getOrCreateOut(table);
		List<DBColumn> columns = table.getColumns();
		for (Object[] value : values) {
			if (value == null) {
				continue;
			}
			for (int columnNumber = 0, lastColIndex =
				columns.size() - 1; columnNumber <= lastColIndex; columnNumber++) {
				sqlLoaderLiteral(out, table, columns.get(columnNumber), value[columnNumber],
					columnNumber < lastColIndex);
			}
			out.append(ROW_TERMINATOR);
		}
	}

	/**
	 * Neither {@link DBTable} nor {@link DBColumn} have {@link Object#equals(Object)}
	 * implementation. As there are maps using them as keys, we must work with the same
	 * {@link DBTable} and {@link DBColumn}.
	 * 
	 * @see #_maxSize
	 * @see #_dataStreams
	 */
	private DBTable getCachedTable(DBTable table) {
		String key = table.getDBName();
		DBTable cachedTable = _chachedTables.get(key);
		if (cachedTable == null) {
			_chachedTables.put(key, table);
			cachedTable = table;
		}
		return cachedTable;
	}

	private void sqlLoaderLiteral(Appendable out, DBTable table, DBColumn column, Object columnValue,
			boolean appendSeparator) throws IOException {
		DBType columnType = column.getType();
		switch (columnType) {
			case FLOAT:
			case DECIMAL:
			case DOUBLE:
				if (columnValue != null) {
					out.append(formatFloatingPoint(column, (Number) columnValue));
				}
				if (appendSeparator) {
					out.append(FIELD_SEPARATOR);
				}
				break;
			case TIME:
			case DATETIME:
			case DATE: {
				if (columnValue != null) {
					/* Use own format as constants in DBHelper format (TIMESTAMP ...) are not
					 * understood. */
//				_sqlDialect.literal(out, columnType, columnValue); 
					_sqlDialect.escape(out, _dateTimeFormat.format(columnValue));
				}
				if (appendSeparator) {
					out.append(FIELD_SEPARATOR);
				}
				break;
			}
			case BLOB: {
				if (columnValue == null) {
					out.append("NULL");
				} else {
					String fileName =
						table.getDBName() + "_blobs/" + table.getDBName() + "_blob_" + (_nextLobIndexFilename++)
							+ ".dat";
					out.append(fileName);
					File blobFile = new File(_outDir, fileName);
					blobFile.getParentFile().mkdir();
					storeBlob(blobFile, columnValue);
				}
				if (appendSeparator) {
					out.append(FIELD_SEPARATOR);
				}

				break;
			}
			case STRING:
			case CLOB: {
				long colLength = size(column);
				if (colLength > 0) {
					String zeros = zeros(Long.toString(colLength).length());
					if (columnValue == null) {
						if (columnType == DBType.CLOB) {
							/* It is actually an Oracle SQL*Loader Bug that NULLIF "a"=BLANKS or
							 * such does not work. For this reason NULL is written instead. */
							String replacement = "NULL";
							out.append(zeros.substring(1));
							out.append("4");
							out.append(replacement);
							ensureMaxWrittenChars(column, 4);
						} else {
							out.append(zeros);
							ensureMaxWrittenChars(column, 0);
						}
					} else {
						String stringValue = columnValue.toString();
						int numberCharacters = stringValue.length();
						String length = Integer.toString(numberCharacters);
						out.append(zeros.substring(length.length()));
						out.append(length);
						// No escape of strings when using VARCHARC
						out.append(stringValue);
						ensureMaxWrittenChars(column, numberCharacters);
					}
					// No comma when using datatype VARCHARC
				} else {
					_sqlDialect.literal(out, columnType, columnValue);
					if (appendSeparator) {
						out.append(FIELD_SEPARATOR);
					}

				}

				break;
			}
			default:
				if (columnValue != null) {
					_sqlDialect.literal(out, columnType, columnValue);
				}
				if (appendSeparator) {
					out.append(FIELD_SEPARATOR);
				}

				break;
		}
	}

	private void storeBlob(File file, Object columnValue) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			if (columnValue instanceof byte[]) {
				out.write((byte[]) columnValue);
			} else if (columnValue instanceof BinaryContent) {
				try (InputStream in = ((BinaryContent) columnValue).getStream()) {
					StreamUtilities.copyStreamContents(in, out);
				}
			} else if (columnValue instanceof Blob) {
				try (InputStream in = ((Blob) columnValue).getBinaryStream()) {
					StreamUtilities.copyStreamContents(in, out);
				} catch (SQLException ex) {
					throw new IOException(ex);
				}
			} else if (columnValue instanceof InputStream) {
				StreamUtilities.copyStreamContents((InputStream) columnValue, out);
			} else {
				throw new IllegalArgumentException("Unknown blob content type: " + columnValue.getClass());
			}
		}
	}

	@SuppressWarnings("unused")
	private void storeClob(File file, Object columnValue) throws IOException {
		try (Writer out = new OutputStreamWriter(new FileOutputStream(file), FILE_ENCODING)) {
			if (columnValue instanceof char[]) {
				out.write((char[]) columnValue);
			} else if (columnValue instanceof CharSequence) {
				out.write(((CharSequence) columnValue).toString());
			} else if (columnValue instanceof CharacterContent) {
				try (Reader in = ((CharacterContent) columnValue).getReader()) {
					StreamUtilities.copyReaderWriterContents(in, out);
				}
			} else if (columnValue instanceof Clob) {
				try (Reader in = ((Clob) columnValue).getCharacterStream()) {
					StreamUtilities.copyReaderWriterContents(in, out);
				} catch (SQLException ex) {
					throw new IOException(ex);
				}
			} else if (columnValue instanceof Reader) {
				StreamUtilities.copyReaderWriterContents((Reader) columnValue, out);
			} else {
				throw new IllegalArgumentException("Unknown clob content type: " + columnValue.getClass());
			}

		}
	}

	private void ensureMaxWrittenChars(DBColumn col, int numberChars) {
		MutableInteger length = (MutableInteger) _maxSize.get(col);
		if (length == null) {
			// At least one, otherwise error occurs: "positive number expected".
			length = new MutableInteger(1);
			_maxSize.put(col, length);
		}
		length.setValue(Math.max(length.intValue(), numberChars));
	}

	private Writer getOrCreateOut(DBTable table) throws IOException {
		Writer out = _dataStreams.get(table);
		if (out == null) {
			out = newFileWriter(dataFileName(table));
			_dataStreams.put(table, out);
		}
		return out;
	}

	private Writer newFileWriter(String fileName) throws IOException {
		File file = new File(_outDir, fileName);
		OutputStream fileOut = new FileOutputStream(file);
		OutputStream bufferedOut = new BufferedOutputStream(fileOut);
		return new OutputStreamWriter(bufferedOut, FILE_ENCODING);
	}

	private String dataFileName(DBTable table) {
		return table.getDBName() + ".dat";
	}

	private String controlFileName(DBTable table) {
		return table.getDBName() + ".ctl";
	}

	private String badFile(DBTable table, char fileSeparator) {
		return BAD_DIR_NAME + fileSeparator + table.getDBName() + ".ctl";
	}

	private void writeControlFile(DBTable table) {
		try (Writer out = newFileWriter(controlFileName(table))) {
			appendLine(out, "load data");
			appendLine(out, "characterset " + FILE_ENCODING + " length char");
			appendLine(out, "infile " + dataFileName(table) + " \"str '" + ROW_TERMINATOR_ESCAPED + "'\"");
			appendLine(out, "into table " + table.getDBName());
			appendLine(out, "fields terminated by '" + FIELD_SEPARATOR + "' optionally enclosed by \"'\"");
			appendLine(out, "trailing nullcols");
			out.append('(');
			boolean first = true;
			for (DBColumn col : table.getColumns()) {
				if (first) {
					first = false;
				} else {
					out.append(',');
				}
				sqlLoaderColumnDef(out, col);
			}
			out.append(')');
			newLine(out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}

	}

	private void appendLine(Appendable out, String line) throws IOException {
		out.append(line);
		newLine(out);
	}

	/**
	 * Appends a new line to given {@link Appendable}.
	 */
	protected void newLine(Appendable out) throws IOException {
		out.append('\n');
	}

	private void sqlLoaderColumnDef(Appendable out, DBColumn col) throws IOException {
		String colName = col.getDBName();
		DBType dbType = col.getType();
		switch (dbType) {
			case DECIMAL:
			case FLOAT:
			case DOUBLE: {
				FloatingPointPrecision precision = (FloatingPointPrecision) _maxSize.get(col);
				if (precision == null) {
					precision = new FloatingPointPrecision();
				}
				out.append(_sqlDialect.columnRef(colName))
					.append(" FLOAT EXTERNAL \"TO_NUMBER(:").append(colName)
					.append(",'");
				for (int i = 0; i < precision._intDigits; i++) {
					out.append('9');
				}
				out.append('D');
				for (int i = 0; i < precision._decimalDigits; i++) {
					out.append('9');
				}
				final char groupSeparator = ' '; // Actually unused in data
				out.append("','NLS_NUMERIC_CHARACTERS=''").append(_decimalSeparator).append(groupSeparator)
					.append("''')\"");
				break;
			}
			case TIME:
			case DATETIME:
			case DATE: {
				out.append(_sqlDialect.columnRef(colName))
					.append(" TIMESTAMP 'YYYYMMDD HH24MISSFF'");
				break;
			}
			case BLOB: {
				String fileNameCol = colName + "_fileName";
				out.append(fileNameCol)
					.append(" FILLER CHAR(100),");
				out.append(_sqlDialect.columnRef(colName))
					.append(" LOBFILE(")
					.append(fileNameCol)
					.append(") TERMINATED BY EOF NULLIF ")
					.append(fileNameCol)
					.append(" = 'NULL'");
				break;
			}
			case CLOB:
			case STRING: {
				/* Char size is needed: When the column e.g. contains 256 CHAR and the value
				 * contains non ASCII chars (ä,ö,ü,ß,...) the row can not be imported. */
				out.append(_sqlDialect.columnRef(colName));
				int lengthFieldLength = String.valueOf(size(col)).length();
				out.append(" VARCHARC(")
					.append(String.valueOf(lengthFieldLength))
					.append(",")
					.append(Integer.toString(((MutableInteger) _maxSize.get(col)).intValue()))// .append(Long.toString(size))
					.append(")");
				if (dbType == DBType.CLOB) {
					/* It is actually an Oracle SQL*Loader Bug that NULLIF "a"=BLANKS or such does
					 * not work. For this reason NULL is written instead. */
					out.append(" NULLIF ").append(_sqlDialect.columnRef(colName)).append("='NULL'");
				}
				break;
			}
			default:
				out.append(_sqlDialect.columnRef(colName));
				break;
		}
	}

	private int size(DBColumn col) {
		long size = col.getSize();
		if (size == 0) {
			size = 100000000;
		}
		return (int) size;
	}

	private String zeros(int i) {
		if (i < ZEROS.length) {
			return ZEROS[i];
		} else {
			return createZeros(i);
		}
	}

	private static String createZeros(int i) {
		char[] chars = new char[i];
		Arrays.fill(chars, '0');
		return new String(chars);
	}

	/**
	 * Taken from {@link DBHelper}.
	 */
	private Format dateFormat(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setTimeZone(TimeZones.systemTimeZone());
		return format;
	}

	/**
	 * Formats the given floating point {@link Number} as decimal. Potential decimal digits are
	 * separated by '.'.
	 * 
	 * @param col
	 *        The {@link DBColumn} for which the given number is the value for.
	 * @param n
	 *        The number to format.
	 * @return A decimal representation of the given floating point number.
	 */
	private String formatFloatingPoint(DBColumn col, Number n) {
		StringBuilder out = (StringBuilder) _doubleFormattter.out();
		// Reset output.
		out.setLength(0);
		_doubleFormattter.format("%.324f", n);

		// Find maximal value such that for all indexes >= the string just contains 0
		int lastZeroIndex = out.length();
		for (int i = out.length() - 1; i >= 0; i--) {
			if (out.charAt(i) == '0') {
				lastZeroIndex = i;
				continue;
			} else {
				break;
			}
		}
		assert lastZeroIndex >= 2 : "Formatted string has at least form 0.xxx";
		FloatingPointPrecision precision = (FloatingPointPrecision) _maxSize.get(col);
		if (precision == null) {
			precision = new FloatingPointPrecision();
			_maxSize.put(col, precision);
		}
		if (out.charAt(lastZeroIndex - 1) == _decimalSeparator) {
			// Only 0's after '.' Do not skip last '0'
			out.setLength(lastZeroIndex + 1);
			precision.ensureIntDigit(out.length() - 2);
			precision.ensureDecimalDigit(1);
		} else {
			out.setLength(lastZeroIndex);
			for (int i = lastZeroIndex - 2; i >= 0; i--) {
				if (out.charAt(i) == _decimalSeparator) {
					precision.ensureIntDigit(i);
					precision.ensureDecimalDigit(lastZeroIndex - i - 1);
					break;
				}
			}
		}
		return out.toString();
	}

}
