/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.basic.xml.XMLStreamUtil.*;
import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.db.DefaultRowValue;
import com.top_logic.knowledge.service.db2.migration.db.DefaultTableContent;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.TableContent;
import com.top_logic.knowledge.service.migration.Version;
import com.top_logic.knowledge.service.migration.VersionDescriptor;

/**
 * Reader for knowledge base event dump format.
 * 
 * @see DumpSchemaConstants
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DumpReader extends AbstractDumpReader {

	final ChangeSetReader _changeSetReader;
	private VersionDescriptor _version;

	/**
	 * Creates a {@link DumpReader}.
	 * 
	 * @param nameMapper
	 *        Maps type names in the dump to types in the target repository.
	 * @param in
	 *        The XML stream.
	 * @throws XMLStreamException
	 *         If reading fails.
	 */
	public DumpReader(TypeMapping nameMapper, InputStream in) throws XMLStreamException {
		this(nameMapper, getDefaultInputFactory().createXMLStreamReader(in));
	}
	
	/**
	 * Creates a {@link DumpReader}.
	 * 
	 * @param nameMapper
	 *        Maps type names in the dump to types in the target repository.
	 * @param reader
	 *        The XML stream.
	 * @throws XMLStreamException
	 *         If reading fails.
	 */
	public DumpReader(TypeMapping nameMapper, XMLStreamReader reader) throws XMLStreamException {
		super(nameMapper, reader);
		_changeSetReader = new ChangeSetReader(nameMapper, reader);

		readStartTag(DATA);

		while (true) {
			readTag();
			if (hasLocalName(CHANGE_SETS)) {
				break;
			} else if (hasLocalName(MODEL)) {
				skipUpToMatchingEndTag(_reader);
			} else if (hasLocalName(VERSION)) {
				_version = TypedConfiguration.newConfigItem(VersionDescriptor.class);
				while (true) {
					readTag();
					if (isAtEndTag(_reader)) {
						break;
					}
					ensureLocalName(MODULE);
					Version moduleVersion = TypedConfiguration.newConfigItem(Version.class);
					moduleVersion.setModule(_reader.getAttributeValue(null, MODULE_NAME_ATTR));
					moduleVersion.setName(_reader.getAttributeValue(null, MODULE_VERSION_ATTR));
					_version.getModuleVersions().put(moduleVersion.getModule(), moduleVersion);
					readEndTag();
				}
			} else {
				throw new XMLStreamException("Unexpected tag: " + _reader.getLocalName(), _reader.getLocation());
			}
		}

		readTag();
	}

	/**
	 * The version descriptor read from the dump input.
	 * 
	 * @return The dump version, or <code>null</code> if the dump does not include version
	 *         information.
	 */
	public VersionDescriptor getVersion() {
		return _version;
	}

	/**
	 * Returns the {@link ChangeSet}s in the dump.
	 * 
	 * @see #getUnversionedObjects(long)
	 * @see #getTables()
	 */
	public Iterator<ChangeSet> getChangeSets() {
		return new Iterator<>() {

			@Override
			public boolean hasNext() {
				return DumpReader.this._reader.getEventType() == XMLStreamConstants.START_ELEMENT;
			}

			@Override
			public ChangeSet next() {
				ChangeSet cs = DumpReader.this._changeSetReader.read();
				try {
					DumpReader.this.readTag();
				} catch (XMLStreamException ex) {
					throw DumpReader.this.toRuntime(ex);
				}
				return cs;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove not supported");
			}
		};
	}

	/**
	 * Returns the {@link UnversionedCreations} in the dump.
	 * 
	 * <p>
	 * Must be called after {@link #getChangeSets()} was completely processed.
	 * </p>
	 * 
	 * @param eventRev
	 *        The revision which each event in {@link UnversionedCreations#getItems()} has.
	 * 
	 * @see #getChangeSets()
	 * @see #getTables()
	 */
	public Iterator<UnversionedCreations> getUnversionedObjects(long eventRev) {
		try {
			readTag();
			ensureLocalName(UNVERSIONED_TYPES);
			readTag();
			return new AbstractIterator<>() {

				@Override
				protected UnversionedCreations computeNext() {
					UnversionedCreations creations = null;
					while (creations == null && _reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
						creations = DumpReader.this.nextUnversioned(eventRev);
					}

					return creations;
				}

			};
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		}

	}

	UnversionedCreations nextUnversioned(long eventRev) {
		try {
			ensureLocalName(UNVERSIONED_TYPE);

			MetaObject type = readTypeAttribute();
			if (type == null) {
				XMLStreamUtil.skipToMatchingEndTag(_reader);
				readTag();
				return null;
			}
			readTag();
			List<ObjectCreation> allItems = readAllItems(eventRev);
			return new DefaultUnversionedCreations(type, allItems);
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		}
	}

	private List<ObjectCreation> readAllItems(long eventRev) throws XMLStreamException {
		List<ObjectCreation> items = new ArrayList<>();
		while (hasStartTag()) {
			ObjectBranchId id = readId();
			if (id == null) {
				XMLStreamUtil.skipToMatchingEndTag(_reader);
			} else {
				ObjectCreation evt = new ObjectCreation(eventRev, id);
				fillProperties(evt);
				items.add(evt);
				readTag();
			}
		}
		// skip table end tag
		readTag();
		return items;
	}

	/**
	 * Returns the {@link TableContent}s in the dump.
	 * 
	 * <p>
	 * Must be called after {@link #getUnversionedObjects(long)} was completely processed.
	 * </p>
	 * 
	 * @see #getChangeSets()
	 * @see #getUnversionedObjects(long)
	 */
	public Iterator<TableContent> getTables() {
		try {
			readTag();
			ensureLocalName(TABLES);
			readTag();
			return new AbstractIterator<>() {

				@Override
				protected TableContent computeNext() {
					TableContent table = null;
					while (table == null && _reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
						table = DumpReader.this.nextTable();
					}

					return table;
				}

			};
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		}

	}

	TableContent nextTable() {
		try {
			ensureLocalName(TABLE);

			String tableName = readAttribute(TABLE_NAME);
			MOStructure table = resolveTable(tableName);
			if (table == null) {
				skipToMatchingEndTag(_reader);
				readTag();
				return null;
			}

			readTag();
			List<RowValue> allRows = readAllRows(table);
			return new DefaultTableContent(table, allRows);
		} catch (XMLStreamException ex) {
			throw toRuntime(ex);
		}
	}

	private List<RowValue> readAllRows(MOStructure table) throws XMLStreamException {
		List<RowValue> rows = new ArrayList<>();
		while (hasStartTag()) {
			rows.add(readRow(table));
			readTag();
		}
		// skip table end tag
		readTag();
		return rows;
	}

	RowValue readRow(MOStructure table) throws XMLStreamException {
		readTag();
		HashMap<String, Object> values = new HashMap<>();
		while (hasStartTag()) {
			ensureLocalName(PROPERTY);

			String name = readAttribute(NAME_ATTR);
			Object value = readValue(TYPE_ATTR, VALUE_ATTR);
			values.put(name, value);

			readEndTag();
			readTag();
		}
		assertEndTag();
		return new DefaultRowValue(table, values);
	}

	ChangeSet nextChangeSet() {
		return _changeSetReader.read();
	}

	@Override
	public void close() throws XMLStreamException {
		_reader.close();
	}

	private void readStartTag(String localName) throws XMLStreamException {
		readStartTag();
		ensureLocalName(localName);
	}

}
