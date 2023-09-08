/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link ObjectImport} completing an already created object with additional import data.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectRef<C extends ObjectRef.Config<?>> extends ObjectImport<C> {

	/**
	 * Configuration options for {@link ObjectRef}.
	 */
	@TagName("object-ref")
	public interface Config<I extends ObjectRef<?>> extends ObjectImport.Config<I> {
		@Override
		@Mandatory
		String getIdAttribute();

		/**
		 * Whether the {@link #getIdAttribute()} is required in the import data and cannot be empty.
		 */
		boolean isMandatory();

		/**
		 * Whether the {@link #getIdAttribute()} can contain multiple values.
		 * 
		 * <p>
		 * Multiple values are expected to be separated by space (see XML IDREFS attributes).
		 * </p>
		 */
		boolean isMultiple();
	}

	/**
	 * Creates a {@link ObjectRef} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ObjectRef(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object importInScope(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = resolveId(context, in);

		if (id == null) {
			if (getConfig().isMandatory()) {
				context.error(in.getLocation(),
					I18NConstants.ERROR_MANDATORY_ID_ATTRIBUTES_NOT_SET__HANDLER_ATTR.fill(this.location(),
						getConfig().getIdAttribute()));
			}
			return null;
		}

		if (getConfig().isMultiple()) {
			Object result = null;
			for (String ref : id.split("\\s+")) {
				result = importSingle(context, in, ref);
			}
			return result;
		} else {
			return importSingle(context, in, id);
		}
	}

	private Object importSingle(ImportContext context, XMLStreamReader in, String id) throws XMLStreamException {
		Object result = context.resolveObject(this, in.getLocation(), id);
		return importTarget(context, in, result);
	}

}
