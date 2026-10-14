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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link NestedImportHandler} that skips the current element, if its ID is already known.
 * 
 * <p>
 * If the {@link Config#getIdAttribute() ID attribute} of the current element resolves to an object
 * that was imported before or that is found by an enclosing {@link ResolveExisting}, the element is
 * skipped with all its content. Otherwise, the content handlers are applied.
 * </p>
 */
public class UnlessKnown<C extends UnlessKnown.Config<?>> extends NestedImportHandler<C> {

	/**
	 * Configuration options for {@link UnlessKnown}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends UnlessKnown<?>> extends NestedImportHandler.Config<I> {

		/**
		 * Tag name of {@link UnlessKnown}.
		 */
		String TAG_NAME = "unless-known";

		/**
		 * @see #getIdAttribute()
		 */
		String ID_ATTRIBUTE = "id-attribute";

		/**
		 * The XML attribute of the current element containing the ID to check.
		 * 
		 * <p>
		 * An element without this attribute, or with an empty value, is imported.
		 * </p>
		 */
		@Name(ID_ATTRIBUTE)
		@Mandatory
		String getIdAttribute();

	}

	/**
	 * Creates a {@link UnlessKnown} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UnlessKnown(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = ObjectImport.readIdAttribute(in, getConfig().getIdAttribute());
		if (id != null && context.resolveObjectBackwards(this, in.getLocation(), id) != null) {
			XMLStreamUtil.skipUpToMatchingEndTag(in);
			return null;
		}
		return importXmlInScope(context, in);
	}

}
