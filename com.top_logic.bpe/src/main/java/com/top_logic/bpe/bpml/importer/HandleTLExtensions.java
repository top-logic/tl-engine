/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.importer;

import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.bpe.bpml.exporter.BPMLExporter;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.handlers.Handler;

/**
 * {@link Handler} reading <i>TopLogic</i> BPML extension elements.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HandleTLExtensions implements Handler {

	private static final Property<Map<TLStructuredTypePart, AttributeValueBinding<?>>> BINDINGS =
		TypedAnnotatable.propertyMap("bindings");

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		readExtensions:
		while (true) {
			switch (in.next()) {
				case XMLStreamConstants.START_ELEMENT: {
					if (BPMLExporter.TL_EXTENSIONS_NS.equals(in.getNamespaceURI())) {
						importTLExtension(context, in);
					} else {
						XMLStreamUtil.skipUpToMatchingEndTag(in);
					}
					break;
				}
				case XMLStreamConstants.END_DOCUMENT:
				case XMLStreamConstants.END_ELEMENT: {
					break readExtensions;
				}
			}
		}
		return null;
	}

	private void importTLExtension(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();

		TLObject self = (TLObject) context.getVar(ImportContext.THIS_VAR);
		TLStructuredTypePart attribute = self.tType().getPart(localName);
		if (attribute == null) {
			context.error(in.getLocation(), I18NConstants.INVALID_BPML_EXTENSION__NAME.fill(localName));
			XMLStreamUtil.skipUpToMatchingEndTag(in);
		} else {
			Map<TLStructuredTypePart, AttributeValueBinding<?>> bindings = context.mkMap(BINDINGS);
			AttributeValueBinding<?> binding = bindings.get(attribute);
			if (binding == null && !bindings.containsKey(attribute)) {
				binding = AttributeSettings.getInstance()
					.getExportBinding(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, attribute);
				bindings.put(attribute, binding);
			}

			if (binding == null) {
				context.error(in.getLocation(), I18NConstants.INVALID_BPML_EXTENSION__NAME.fill(localName));
				XMLStreamUtil.skipUpToMatchingEndTag(in);
			} else {
				Object value = binding.loadValue(context, in, attribute);
				self.tUpdate(attribute, value);
			}
		}

		assert in.getEventType() == XMLStreamConstants.END_ELEMENT : "Not at an end tag after reading extendsion '"
			+ localName + "': " + XMLStreamUtil.getEventName(in.getEventType());

		String localNameAfter = in.getLocalName();

		assert BPMLExporter.TL_EXTENSIONS_NS.equals(in.getNamespaceURI())
			&& localName.equals(localNameAfter) : "Invalid state after reading extension '" + localName + "': "
				+ in.getNamespaceURI() + "/" + localNameAfter;
	}

}
