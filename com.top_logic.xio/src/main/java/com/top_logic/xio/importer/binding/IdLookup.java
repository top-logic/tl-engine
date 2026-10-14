/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import javax.xml.stream.Location;

/**
 * Source of objects for IDs that are not known to the import.
 * 
 * <p>
 * An {@link IdLookup} is installed for a part of an import with
 * {@link ImportContext#withIdLookup(IdLookup, javax.xml.stream.XMLStreamReader, com.top_logic.xio.importer.handlers.Handler)}.
 * Within that part, an ID that does not resolve to an object created or registered by the import
 * is passed to the lookup, e.g. to find an object that already exists in the application.
 * </p>
 *
 * @see ImportContext#resolveObjectBackwards(com.top_logic.xio.importer.handlers.ImportPart,
 *      Location, String)
 */
@FunctionalInterface
public interface IdLookup {

	/**
	 * Looks up the object with the given ID.
	 *
	 * @param location
	 *        The location in the source XML that requested the resolution (for reporting).
	 * @param id
	 *        The ID to resolve.
	 * @return The object with the given ID, or <code>null</code> if this lookup does not know the
	 *         ID.
	 */
	Object lookup(Location location, String id);

}
