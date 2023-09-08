/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.migration.Version;
import com.top_logic.knowledge.service.migration.VersionDescriptor;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Writer for knowledge base dumps.
 * 
 * <p>
 * The writer must be operated in the following way:
 * </p>
 * 
 * <ol>
 * <li>{@link #startDocument()} must be called.</li>
 * <li>{@link #beginChangeSets()} must be called.</li>
 * <li>{@link #writeChangeSet(ChangeSet)} must be called for each {@link ChangeSet} to dump.</li>
 * <li>{@link #endChangeSets()} must be called.</li>
 * <li>{@link #beginTypes()} must be called.</li>
 * <li>{@link #writeUnversionedType(MetaObject, Iterator)} must be called for each unversioned type
 * to dump.</li>
 * <li>{@link #endTypes()} must be called.</li>
 * <li>{@link #beginTables()} must be called.</li>
 * <li>{@link #writeTable(String, Iterable)} must be called for each table to dump.</li>
 * <li>{@link #endTables()} must be called.</li>
 * <li>{@link #endDocument()} must be called.</li>
 * <li>{@link #close()} must be called.</li>
 * </ol>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DumpWriter extends AbstractDumpWriter implements IDumpWriter {

	private ChangeSetDumper _changeSetDumper;

	private List<Exception> _errors = new ArrayList<>();

	private boolean _failOnError;

	private VersionDescriptor _version;

	/**
	 * Creates a {@link DumpWriter} without version information.
	 * 
	 * @param out
	 *        The writer to write to.
	 */
	public DumpWriter(TagWriter out) {
		this(out, null);
	}
	
	/**
	 * Creates a {@link DumpWriter}.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @param version
	 *        The version descriptor describing the application data version being dumped.
	 */
	public DumpWriter(TagWriter out, VersionDescriptor version) {
		super(out);
		_version = version;
		_changeSetDumper = new ChangeSetDumper(out, true);
	}

	/**
	 * Whether a dumping should terminate when an error occured. Otherwise the error is logged to
	 * the given output.
	 */
	public void failOnError(boolean failOnError) {
		_failOnError = failOnError;
	}

	/**
	 * Starts the dump document.
	 */
	public void startDocument() throws IOException {
		_out.writeXMLHeader("utf-8");
		_out.setIndent(true);
		_out.beginTag(DATA);

		if (_version != null) {
			_out.beginTag(VERSION);
			for (Version moduleVersion : sort(_version.getModuleVersions().values())) {
				_out.beginBeginTag(MODULE);
				_out.writeAttribute(MODULE_NAME_ATTR, moduleVersion.getModule());
				_out.writeAttribute(MODULE_VERSION_ATTR, moduleVersion.getName());
				_out.endEmptyTag();
			}
			_out.endTag(VERSION);
		}

		_out.beginTag(MODEL);
		_out.endTag(MODEL);
	}

	private List<Version> sort(Collection<Version> moduleVersions) {
		ArrayList<Version> result = new ArrayList<>(moduleVersions);
		Collections.sort(result, (v1, v2) -> v1.getModule().compareTo(v2.getModule()));
		return result;
	}

	/**
	 * The errors that occurred.
	 */
	public List<Exception> errors() {
		return _errors;
	}

	/**
	 * Writes the {@link DumpSchemaConstants#CHANGE_SETS} start tag.
	 */
	@Override
	public void beginChangeSets() {
		_out.beginTag(CHANGE_SETS);
	}

	/**
	 * Writes the {@link DumpSchemaConstants#CHANGE_SETS} end tag.
	 */
	@Override
	public void endChangeSets() {
		_out.endTag(CHANGE_SETS);
	}

	/**
	 * Writes the {@link DumpSchemaConstants#TABLES} start tag.
	 */
	@Override
	public void beginTables() {
		_out.beginTag(TABLES);
	}

	/**
	 * Writes the {@link DumpSchemaConstants#TABLES} end tag.
	 */
	@Override
	public void endTables() {
		_out.endTag(TABLES);
	}

	/**
	 * Writes the {@link DumpSchemaConstants#UNVERSIONED_TYPES} start tag.
	 */
	@Override
	public void beginTypes() {
		_out.beginTag(UNVERSIONED_TYPES);
	}

	/**
	 * Writes the {@link DumpSchemaConstants#UNVERSIONED_TYPES} end tag.
	 */
	@Override
	public void endTypes() {
		_out.endTag(UNVERSIONED_TYPES);
	}

	/**
	 * Finishes the dump document.
	 */
	public void endDocument() {
		try {
			writeErrorsAtLast();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		_out.endTag(DATA);
	}

	private void writeErrorsAtLast() throws IOException {
		List<Exception> errors = errors();
		if (errors.isEmpty()) {
			return;
		}
		_out.beginTag(ERROR_TAG);
		_out.write("Errors occurred:");
		for (Exception ex : errors) {
			_out.nl();
			_out.write(ex.getMessage());
		}
		_out.endTag(ERROR_TAG);
	}

	@Override
	public void writeChangeSet(ChangeSet cs) throws IOException {
		int depthBefore = _out.getDepth();
		try {
			_changeSetDumper.writeChangeSet(cs);
		} catch (RuntimeException ex) {
			handleUnexpectedFailure("Unable to dump CS " + cs, ex, depthBefore);
		}
	}

	@Override
	public void writeUnversionedType(MetaObject type, Iterator<? extends KnowledgeItem> items) throws IOException {
		int depthBefore = _out.getDepth();
		try {
			_out.beginBeginTag(UNVERSIONED_TYPE);
			_out.writeAttribute(TYPE_ATTR, type.getName());
			_out.endBeginTag();
			while (items.hasNext()) {
				dumpItem(items.next());
			}
			_out.endTag(UNVERSIONED_TYPE);
		} catch (RuntimeException ex) {
			handleUnexpectedFailure("Error processing " + type, ex, depthBefore);
		}
	}

	private void dumpItem(KnowledgeItem item) throws IOException {
		_out.beginBeginTag(UNVERSIONED_ITEM);
		dumpIdAttribute(ObjectBranchId.toObjectBranchId(item.tId()));
		_out.endBeginTag();
		dumpValues(getValues(item), null);
		_out.endTag(UNVERSIONED_ITEM);
	}

	private Map<String, Object> getValues(KnowledgeItem item) {
		Map<String, Object> values;
		if (item instanceof KnowledgeObject) {
			KnowledgeObject ko = (KnowledgeObject) item;
			Wrapper wrapper = WrapperFactory.getWrapper(ko);
			values = ((AbstractWrapper) wrapper).getAllValues();
		} else {
			values = new HashMap<>();
			String[] attributes = item.getAttributeNames();
			for (String attribute : attributes) {
				try {
					values.put(attribute, item.getAttributeValue(attribute));
				} catch (NoSuchAttributeException ex) {
					throw new UnreachableAssertion(ex);
				}
			}
		}
		values.remove(BasicTypes.BRANCH_ATTRIBUTE_NAME);
		values.remove(BasicTypes.IDENTIFIER_ATTRIBUTE_NAME);
		values.remove(BasicTypes.REV_MAX_ATTRIBUTE_NAME);
		return values;
	}

	@Override
	public void writeTable(String tableName, Iterable<RowValue> rows) throws IOException {
		int depthBefore = _out.getDepth();
		try {
			_out.beginBeginTag(TABLE);
			_out.writeAttribute(TABLE_NAME, tableName);
			_out.endBeginTag();
			for (RowValue row : rows) {
				dumpRow(row);
			}
			_out.endTag(TABLE);
		} catch (RuntimeException ex) {
			handleUnexpectedFailure("Error processing " + tableName, ex, depthBefore);
		}

	}

	private void dumpRow(RowValue row) throws IOException {
		_out.beginTag(ROW);
		dumpValues(row.getValues(), null);
		_out.endTag(ROW);
	}

	private void handleUnexpectedFailure(String message, RuntimeException ex, int depthBefore) {
		ex = new RuntimeException(message, ex);
		// stack trace of this exception does not care.
		ex.setStackTrace(new StackTraceElement[0]);
		if (_failOnError) {
			throw ex;
		}
		try {
			_out.endAll(depthBefore);
			_out.beginTag(ERROR_TAG);
			_out.beginCData();
			_out.nl();
			ex.printStackTrace(new PrintWriter(_out));
			_out.endCData();
			_out.endTag(ERROR_TAG);
		} catch (IOException flushException) {
			// Do not hide actual failure
			ex.addSuppressed(flushException);
		}
		_errors.add(ex);
	}

}
