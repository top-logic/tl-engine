/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link NestedImportHandler} binding a variable to the value of an XML attribute of the current
 * element for its content.
 * 
 * <p>
 * If the current element has no {@link Config#getXmlAttribute() XML attribute} (or an empty one),
 * the content is applied with the {@link Config#getVar() variable} unchanged: A binding of an
 * enclosing element stays in effect.
 * </p>
 */
@Label("With variable")
public class WithVar<C extends WithVar.Config<?>> extends NestedImportHandler<C> {

	/**
	 * Configuration options for {@link WithVar}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends WithVar<?>> extends NestedImportHandler.Config<I> {

		/**
		 * Tag name of {@link WithVar}.
		 */
		String TAG_NAME = "with-var";

		/**
		 * @see #getVar()
		 */
		String VAR = "var";

		/**
		 * @see #getXmlAttribute()
		 */
		String XML_ATTRIBUTE = "xml-attribute";

		/**
		 * The name of the {@link ImportContext#getVar(String) variable} to bind.
		 */
		@Name(VAR)
		@Label("Variable")
		@Mandatory
		String getVar();

		/**
		 * The XML attribute of the current element whose value is bound to the {@link #getVar()
		 * variable}.
		 */
		@Name(XML_ATTRIBUTE)
		@Mandatory
		String getXmlAttribute();

	}

	/**
	 * Creates a {@link WithVar} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WithVar(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String value = in.getAttributeValue(null, getConfig().getXmlAttribute());
		if (value == null || value.isBlank()) {
			return importXmlInScope(context, in);
		}
		return context.withVar(getConfig().getVar(), value, in, this::importXmlInScope);
	}

}
