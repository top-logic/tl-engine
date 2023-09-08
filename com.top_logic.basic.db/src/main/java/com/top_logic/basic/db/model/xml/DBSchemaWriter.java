/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.DBSchemaPart;

/**
 * Serializer that transforms a {@link DBSchema} into XML format defined by typed configuration
 * conventions.
 * 
 * @see #writeSchema(XMLStreamWriter, DBSchema)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBSchemaWriter {
	
	/**
	 * Serialized the given {@link DBSchemaPart} to the given writer.
	 * 
	 * @param writer
	 *        The {@link XMLStreamWriter} to write to.
	 * @param model
	 *        The {@link DBSchemaPart} element to serialize as top-level
	 *        element.
	 * @throws XMLStreamException
	 *         If writing fails.
	 */
	public static void writeSchema(XMLStreamWriter writer, DBSchema model) throws XMLStreamException {
		ConfigurationWriter out = new ConfigurationWriter(writer);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(DBSchema.class);
		out.write(DBSchema.SCHEMA_ELEMENT, descriptor, model);
	}
	
}
