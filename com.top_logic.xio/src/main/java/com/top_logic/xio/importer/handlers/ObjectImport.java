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
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * Base class for {@link Handler}s importing objects.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ObjectImport<C extends ObjectImport.Config<?>> extends NestedImportHandler<C> {

	private static final String TEXT_CONTENT = "text()";

	/**
	 * Configuration options for {@link ObjectImport}.
	 */
	@Abstract
	public interface Config<I extends ObjectImport<?>> extends NestedImportHandler.Config<I> {

		/**
		 * The XML attribute that assigns the ID to the created object.
		 */
		@Nullable
		String getIdAttribute();

		/**
		 * Explicit variable name in the context for the created object.
		 * 
		 * <p>
		 * Inner handlers may explicitly reference this definition from the context.
		 * </p>
		 */
		@Nullable
		String getVar();
	}

	/**
	 * Creates a {@link ObjectImport} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ObjectImport(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object contextObject = context.getVarOrNull(ImportContext.THIS_VAR);

		return context.withVar(ImportContext.SCOPE_VAR, contextObject, in, this::importInScope);
	}

	/**
	 * Performs the actual import (object creation or lookup).
	 * 
	 * <p>
	 * The ID attribute value to assign or resolve the imported object(s) must be looked up with
	 * {@link #resolveId(ImportContext, XMLStreamReader)}.
	 * </p>
	 * 
	 * <p>
	 * The resulting object(s) must be post-processed with a call to
	 * {@link #importTarget(ImportContext, XMLStreamReader, Object)}.
	 * </p>
	 *
	 * @param context
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @param in
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}
	 * @return The (reference to the) imported object.
	 */
	protected abstract Object importInScope(ImportContext context, XMLStreamReader in) throws XMLStreamException;

	/**
	 * Reads the value of the {@link Config#getIdAttribute()} from the input data.
	 * 
	 * <p>
	 * If no {@link Config#getIdAttribute()} was specified, <code>null</code> is returned.
	 * <code>null</code> is also returned, if the input data does not specify a value for the
	 * {@link Config#getIdAttribute()}, or the value is empty.
	 * </p>
	 *
	 * @param context
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @param in
	 *        See {@link #importXml(ImportContext, XMLStreamReader)}.
	 * @return The value of the {@link Config#getIdAttribute()}, or <code>null</code>, if none is
	 *         given.
	 * @throws XMLStreamException
	 *         If reading fails.
	 * 
	 * @see #importInScope(ImportContext, XMLStreamReader)
	 */
	protected String resolveId(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String idAttribute = getConfig().getIdAttribute();

		if (idAttribute == null) {
			return null;
		}

		String id;
		if (TEXT_CONTENT.equals(idAttribute)) {
			id = XMLStreamUtil.nextText(in);
		} else {
			id = in.getAttributeValue(null, idAttribute);
		}

		if (id == null) {
			return null;
		}

		id = id.trim();

		if (id.isEmpty()) {
			return null;
		} else {
			return id;
		}
	}

	/**
	 * Completes the object by applying the nested import handlers.
	 * 
	 * <p>
	 * This method must be called on all objects imported by
	 * {@link #importInScope(ImportContext, XMLStreamReader)}.
	 * </p>
	 * 
	 * @see #importInScope(ImportContext, XMLStreamReader)
	 */
	protected Object importTarget(ImportContext context, XMLStreamReader in, Object target)
			throws XMLStreamException {
		return context.withVar(ImportContext.THIS_VAR, target, in, (context1, in1) -> {
			return context1.withVar(getConfig().getVar(), target, in1, (context2, in2) -> {
				return importXmlInScope(context2, in2);
			});
		});
	}

}
