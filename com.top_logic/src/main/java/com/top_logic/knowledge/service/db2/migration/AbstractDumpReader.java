/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.basic.xml.XMLStreamUtil.*;
import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.TLID;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.formats.ValueParser;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;

/**
 * Abstract super class for reader of dumps.
 * 
 * @see AbstractDumpWriter
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractDumpReader implements AutoCloseable {

	private final ValueParser<XMLStreamException> _parser = new ValueParser<>() {

		@Override
		protected MetaObject resolve(String typeName) throws XMLStreamException {
			return AbstractDumpReader.this.resolve(typeName);
		}

		@Override
		protected XMLStreamException parseError(String message, Throwable cause) throws XMLStreamException {
			return fail(message, cause);
		}

	};

	/** The {@link XMLStreamReader} to read contents from. */
	protected final XMLStreamReader _reader;

	private final TypeMapping _typeMapper;

	/**
	 * Creates a new {@link AbstractDumpReader}.
	 * 
	 * @param typeMapper
	 *        Mapping of name of a {@link MetaObject} to the type itself.
	 * @param reader
	 *        The actual input reader.
	 */
	public AbstractDumpReader(TypeMapping typeMapper, XMLStreamReader reader) {
		_typeMapper = typeMapper;
		_reader = reader;
	}

	/**
	 * Reads the attribute with the given name without namespace.
	 */
	protected String readAttribute(String localName) {
		return _reader.getAttributeValue(null, localName);
	}

	/**
	 * Checks {@link #hasLocalName(String) local name}.
	 */
	protected void ensureLocalName(String localName) throws XMLStreamException {
		if (!hasLocalName(localName)) {
			throw fail("Expected start of '" + localName + "' element, found: " + _reader.getLocalName());
		}
	}

	/**
	 * Fails with an {@link XMLStreamException}.
	 */
	protected XMLStreamException fail(String message) throws XMLStreamException {
		return fail(message, null);
	}

	/**
	 * Fails with an {@link XMLStreamException} with given cause.
	 */
	protected XMLStreamException fail(String message, Throwable cause) throws XMLStreamException {
		throw (XMLStreamException) new XMLStreamException(message, _reader.getLocation()).initCause(cause);
	}

	/**
	 * Converts the given given {@link Exception} to a {@link RuntimeException}.
	 */
	protected RuntimeException toRuntime(Exception ex) {
		throw new RuntimeException(ex);
	}

	/**
	 * Checks that the reader is currently located at an event with given
	 * {@link XMLStreamReader#getLocalName() local name}.
	 */
	protected boolean hasLocalName(String localName) {
		return localName.equals(_reader.getLocalName());
	}

	/**
	 * @see XMLStreamUtil#nextStartTag(XMLStreamReader)
	 */
	protected void readStartTag() throws XMLStreamException {
		nextStartTag(_reader);
	}

	/**
	 * @see XMLStreamUtil#nextEndTag(XMLStreamReader)
	 */
	protected void readEndTag() throws XMLStreamException {
		nextEndTag(_reader);
	}

	/**
	 * @see XMLStreamUtil#ensureEndTag(XMLStreamReader)
	 */
	protected void assertEndTag() throws XMLStreamException {
		ensureEndTag(_reader);
	}

	/**
	 * Switches to next tag.
	 * 
	 * @see XMLStreamReader#nextTag()
	 */
	protected void readTag() throws XMLStreamException {
		_reader.nextTag();
	}

	/**
	 * Checks whether the next event is a {@link XMLStreamConstants#START_ELEMENT start element}.
	 */
	protected boolean hasStartTag() {
		return _reader.getEventType() == XMLStreamConstants.START_ELEMENT;
	}

	/**
	 * Reads and resolves the {@link DumpSchemaConstants#TYPE_ATTR}.
	 */
	protected MetaObject readTypeAttribute() {
		return resolve(readAttribute(TYPE_ATTR));
	}

	/**
	 * Resolves the given type.
	 */
	protected MetaObject resolve(String typeName) {
		return _typeMapper.getType(typeName);
	}

	/**
	 * Resolves the given type.
	 */
	protected MOStructure resolveTable(String tableName) {
		return _typeMapper.getTableType(tableName);
	}

	/**
	 * Reads the {@link #readTypeAttribute() type} and {@link DumpSchemaConstants#ID_ATTR id}
	 * attribute to an {@link ObjectBranchId}.
	 */
	protected ObjectBranchId readId() throws XMLStreamException {
		ObjectBranchId id;
		{
			MetaObject objectType = readTypeAttribute();
			if (objectType == null) {
				return null;
			}
			id = readIdAttribute(objectType);
		}
		return id;
	}

	private ObjectBranchId readIdAttribute(MetaObject objectType) throws XMLStreamException {
		ObjectBranchId objectId;
		{
			String idValue = readAttribute(ID_ATTR);
			int sepIndex = idValue.indexOf("/");
			if (sepIndex < 0) {
				throw fail("Invalid ID format: " + idValue);
			}
			long branchId = Long.parseLong(idValue.substring(0, sepIndex));
			TLID objectName = _parser.fuzzyFromExternalForm(idValue.substring(sepIndex + 1));
			objectId = new ObjectBranchId(branchId, objectType, objectName);
		}
		return objectId;
	}

	/**
	 * Fills attributes to the given {@link ItemChange} event.
	 */
	protected void fillProperties(ItemChange evt) throws XMLStreamException {
		readTag();
		while (hasStartTag()) {
			ensureLocalName(PROPERTY);

			String name = readAttribute(NAME_ATTR);
			Object value = readValue(TYPE_ATTR, VALUE_ATTR);
			Object oldValue = readValue(OLD_TYPE_ATTR, OLD_VALUE_ATTR);
			// If dump contains equal values, also the recreated event should contain equal values.
			boolean dropEqualValues = false;
			if (evt instanceof ItemDeletion) {
				evt.setValue(name, value, null, dropEqualValues);
			} else {
				evt.setValue(name, oldValue, value, dropEqualValues);
			}

			readEndTag();
			readTag();
		}
		assertEndTag();
	}

	/**
	 * Reads the value from the given <code>valueAttr</code>
	 * 
	 * @param typeAttr
	 *        The attribute containing the type of the value.
	 * @param valueAttr
	 *        The attribute containing the serialized value.
	 */
	protected Object readValue(String typeAttr, String valueAttr) throws XMLStreamException {
		String typeValue = readAttribute(typeAttr);
		if (typeValue == null) {
			// Not found. Especially relevant for old values in events that have no old values set.
			return null;
		}
		ValueType type = ValueType.valueOf(typeValue);
		String value = readAttribute(valueAttr);
		return _parser.parseValue(type, value);
	}

}

