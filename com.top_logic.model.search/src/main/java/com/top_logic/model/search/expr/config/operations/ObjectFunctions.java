/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;

/**
 * TL-Script functions identifying an object by a text and finding it again.
 *
 * <p>
 * Together the two make an object addressable from outside the application: its identifier can be
 * put into a URL, a file name or a message, and the object it names can be looked up when such a
 * text comes back.
 * </p>
 */
@ScriptPrefix("object")
public class ObjectFunctions extends TLScriptFunctions {

	/**
	 * The identifier of the given object, as a text that can be part of a URL.
	 *
	 * <p>
	 * The identifier names the object within its type, so finding the object again takes both the
	 * identifier and the type it belongs to.
	 * </p>
	 *
	 * @param object
	 *        The object to identify.
	 * @return The identifier of the object, or <code>null</code> for no object and for one that is
	 *         only transient - a transient object exists in the current form alone and cannot be
	 *         found again.
	 */
	@Label("Identifier of an object")
	@SideEffectFree
	public static String id(@Mandatory TLObject object) {
		if (object == null || object.tTransient()) {
			return null;
		}
		return IdentifierUtil.toExternalForm(object.tIdLocal());
	}

	/**
	 * The object of the given type with the given identifier.
	 *
	 * @param type
	 *        The type of the object to find.
	 * @param id
	 *        The identifier of the object, as delivered by the function identifying an object.
	 * @return The object with that identifier, or <code>null</code> if the type has no such object -
	 *         an identifier that never existed, one of an object that has been deleted, or one
	 *         belonging to an object of another type.
	 */
	@Label("Object with an identifier")
	@SideEffectFree
	public static TLObject resolve(@Mandatory TLStructuredType type, @Mandatory String id) {
		if (type == null || id == null || id.isEmpty()) {
			return null;
		}

		MOStructure table;
		try {
			table = TLModelUtil.getTable(type);
		} catch (Exception ex) {
			Logger.info("No table storing instances of '" + type + "'.", ex, ObjectFunctions.class);
			return null;
		}

		TLID name;
		try {
			name = IdentifierUtil.fromExternalForm(id);
		} catch (RuntimeException ex) {
			// Not an identifier this application produces: nothing it could name.
			return null;
		}

		TLObject result = WrapperFactory.getWrapper(name, table.getName());
		if (result == null || !TLModelUtil.isCompatibleInstance(type, result)) {
			// A table stores the instances of more than one type, so an identifier can name an
			// object that is not of the requested type.
			return null;
		}
		return result;
	}
}
