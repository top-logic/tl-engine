/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * Reads the content of the current element and sets it's XML representation in the given property.
 * 
 * @see Config#getName()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLPropertyImport<C extends XMLPropertyImport.Config<?>> extends DispatchingImporter<C> {

	/**
	 * Configuration options for {@link XMLPropertyImport}.
	 */
	@TagName("xml-content-property")
	public interface Config<I extends XMLPropertyImport<?>> extends DispatchingImporter.Config<I> {

		/**
		 * Name of the variable to take the context object from.
		 */
		@StringDefault(ImportContext.THIS_VAR)
		@Nullable
		String getTargetVar();

		/**
		 * Name of the property to assign.
		 */
		String getName();

		/**
		 * Whether to read only the content of the current element instead of the current element
		 * itself.
		 */
		boolean isContent();
	}

	/**
	 * Creates a {@link XMLPropertyImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public XMLPropertyImport(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String rawXML = readXmlContent(in);
		if (rawXML != null) {
			context.setProperty(this, context.getVar(getConfig().getTargetVar()),
				getConfig().getName(), rawXML);
		}
		return null;
	}

	private String readXmlContent(XMLStreamReader in) throws XMLStreamException {
		Document document = DOMUtil.newDocument();
		Node parent = getConfig().isContent() ? document : DOMUtil.createCurrentElement(in, document, document);
		DOMUtil.appendStripped(in, parent);
		return DOMUtil.toStringRaw(parent);
	}

}
