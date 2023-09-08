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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link ObjectImport} creating a new object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ObjectCreate<C extends ObjectCreate.Config<?>> extends ObjectImport<C> {

	/**
	 * Configuration options for {@link ObjectCreate}.
	 */
	@TagName("object")
	public interface Config<I extends ObjectCreate<?>> extends ObjectImport.Config<I> {

		/**
		 * The type to instantiate.
		 */
		String getModelType();

		/**
		 * Whether an existing object should be re-used, if the object with the given
		 * {@link #getId()} was already imported.
		 */
		boolean getJoinDuplicates();

		/**
		 * An optional variable that contains an unassigned object reference to fill with the newly
		 * created object.
		 * 
		 * <p>
		 * This is useful, if an object is assigned some property in an outer context before it can
		 * be created.
		 * </p>
		 */
		@Nullable
		String getFillForwardVar();
	}

	/**
	 * Creates a {@link ObjectCreate} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ObjectCreate(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected Object importInScope(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object target = getOrCreate(context, in);
		return importTarget(context, in, target);
	}

	private Object getOrCreate(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String id = resolveId(context, in);
		if (id != null && getConfig().getJoinDuplicates()) {
			Object result = context.resolveObjectBackwards(this, in.getLocation(), id);
			if (result != null) {
				return result;
			}
		}

		Object result = createObject(context, in, id);
		String forwardVar = getConfig().getFillForwardVar();
		if (forwardVar != null) {
			Object var = context.getVar(getConfig().getFillForwardVar());
			boolean success = context.fillForward(var, result);
			if (!success) {
				context.error(in.getLocation(),
					I18NConstants.ERROR_DUPLICATE_ASSIGNMENT_TO_VARIABLE__HANDLER_VAR.fill(location(),
						getConfig().getFillForwardVar()));
			}
		}
		return result;
	}

	/**
	 * Actually creates the new object instance.
	 * 
	 * @see ImportContext#createObject(ImportPart, javax.xml.stream.Location, String, String)
	 */
	protected Object createObject(ImportContext context, XMLStreamReader in, String id) {
		return context.createObject(this, in.getLocation(), getConfig().getModelType(), id);
	}

	@Override
	protected Object importTarget(ImportContext context, XMLStreamReader in, Object target) throws XMLStreamException {
		super.importTarget(context, in, target);

		// Make sure to return the created object instead of its contents.
		return target;
	}

}
