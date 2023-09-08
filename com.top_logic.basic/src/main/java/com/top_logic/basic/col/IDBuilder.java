/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Factory for {@link String} identifiers for arbitrary objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDBuilder {

	private static final char SEPARATOR = '-';

	/**
	 * Prefix for the ID.
	 * 
	 * <p>
	 * This prefix is introduced to avoid ID-clashes after a {@link #clear()} of this builder. In
	 * this case the ID for two different options may be the same, so if this value is uses as
	 * client-side identifier, no change is detected.
	 * </p>
	 */
	private int _prefix = 0;

	/** Lazily initialized, because it is needed by a super-class constructor. */
	private ArrayList<Object> objectByID = new ArrayList<>();

	/** Lazily initialized, because it is needed by a super-class constructor. */
	private HashMap<Object, String> idByObject = new HashMap<>();

	/**
	 * Look up the ID for the given object, or create a new one, if none was assigned to the object
	 * yet.
	 * 
	 * @param obj
	 *        The object to be identified.
	 * @return The ID for the given object.
	 * 
	 * @see #lookupId(Object)
	 */
	public final String makeId(Object obj) {
		return internalMakeId(obj);
	}

	/**
	 * Look up the ID for the given object.
	 * 
	 * @param option
	 *        The object to be identified.
	 * @return The ID for the given object, or <code>null</code>, if none was created yet.
	 * 
	 * @see #makeId(Object)
	 */
	public final String lookupId(Object option) {
		String id = internalGetId(option);
		if (id == null) {
			return null;
		} else {
			return id;
		}
	}

	private String internalMakeId(Object option) {
		String existingId = internalGetId(option);
		if (existingId != null) {
			return existingId;
		}

		StringBuilder tmp = new StringBuilder();
		tmp.append(_prefix);
		tmp.append(SEPARATOR);
		tmp.append(Integer.toString(objectByID.size()));
		String newId = tmp.toString();
		objectByID.add(option);
		idByObject.put(option, newId);
		return newId;
	}

	private String internalGetId(Object option) {
		return idByObject.get(option);
	}

	/**
	 * Retrieve the object with the given ID.
	 * 
	 * @param id
	 *        The object identifier to look up.
	 * @return The object with the given ID.
	 * @throws IllegalArgumentException
	 *         If the given ID was not assigned.
	 * 
	 * @see #makeId(Object)
	 */
	public Object getObjectById(String id) throws IllegalArgumentException {
		return internalGetObject(parseId(id));
	}

	private int parseId(String id) {
		int separatorIDX = id.indexOf(SEPARATOR);
		if (separatorIDX == -1) {
			throw new IllegalArgumentException("Illegal identifier: '" + id + "'");
		}
		int intID;
		try {
			intID = Integer.parseInt(id.substring(separatorIDX + 1));
		} catch (NumberFormatException ex) {
			// Caller expects an IllegalArgumentException, that identifiers are
			// encoded as numbers is an implementation detail. Therefore,
			// NumberFormatException must not be thrown by this method.
			throw new IllegalArgumentException("Illegal identifier: '" + id + "'");
		}
		return intID;
	}

	private Object internalGetObject(int internalId) throws IllegalArgumentException {
		if (internalId >= objectByID.size()) {
			throw new IllegalArgumentException("There is no option with id '" + internalId + "'.");
		}
		return objectByID.get(internalId);
	}

	/**
	 * Clear all identifier assignments.
	 */
	public void clear() {
		objectByID.clear();
		idByObject.clear();
		_prefix++;
	}

}
