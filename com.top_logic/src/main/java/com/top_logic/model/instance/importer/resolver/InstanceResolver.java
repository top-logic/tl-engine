/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.resolver;

import com.top_logic.model.TLObject;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;

/**
 * Strategy for resolving instances in an application using a textual identifier.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InstanceResolver {

	/**
	 * Resolves the object of the given type with the given ID.
	 * 
	 * @param kind
	 *        Kind of resolver, see {@link XMLInstanceImporter}#addResolver(xxx)
	 * @param id
	 *        The textual identifier of the object, see {@link GlobalRefConf#getId()}
	 * @return The resolved object, or <code>null</code>, if no such object was found.
	 */
	TLObject resolve(String kind, String id);

	/**
	 * Creates a textual identifier for the given object.
	 * 
	 * @param obj
	 *        The object to identify.
	 * @return A textual identifier for the given object resolvable by
	 *         {@link #resolve(String, String)}. See {@link GlobalRefConf#getId()}.
	 */
	String buildId(TLObject obj);

}
