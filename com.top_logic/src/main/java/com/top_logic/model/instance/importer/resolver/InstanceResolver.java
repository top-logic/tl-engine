/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;

/**
 * Strategy for resolving instances in an application using a textual identifier.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see XMLInstanceImporter#addResolver(String, InstanceResolver)
 */
public interface InstanceResolver {

	/**
	 * Resolves the object of the given type with the given ID.
	 * 
	 * @param log
	 *        The export log to report problems to.
	 * @param context
	 *        The context of the import. It is constant for all objects resolved during a single
	 *        import and specified before the import starts, see
	 *        {@link XMLInstanceImporter#setContext(Object)}.
	 * @param kind
	 *        Kind of resolver, see
	 *        {@link XMLInstanceImporter#addResolver(String, InstanceResolver)}
	 * @param id
	 *        The textual identifier of the object, see {@link GlobalRefConf#getId()}
	 * @return The resolved object, or <code>null</code>, if no such object was found.
	 */
	TLObject resolve(I18NLog log, Object context, String kind, String id);

	/**
	 * Creates a textual identifier for the given object.
	 * 
	 * @param obj
	 *        The object to identify.
	 * @return A textual identifier for the given object resolvable by
	 *         {@link #resolve(I18NLog, Object, String, String)}. See {@link GlobalRefConf#getId()}.
	 */
	String buildId(TLObject obj);

	/**
	 * Callback that initializes this resolver with the type it was declared for.
	 * 
	 * @param type
	 *        The type this resolver was declared on.
	 */
	default void initType(TLStructuredType type) {
		// Hook for implementations.
	}

}
