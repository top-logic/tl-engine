/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;

/**
 * TL-Script functions naming a persistent object by texts and finding it again.
 *
 * <p>
 * A persistent object is named by two texts: the name of the table storing it and its identifier
 * within that table. Together they make an object addressable from outside the application: they
 * can be put into a URL, a file name or a message, and the object they name can be looked up when
 * such texts come back.
 * </p>
 */
@ScriptPrefix("object")
public class ObjectFunctions extends TLScriptFunctions {

	/**
	 * The name of the table storing the given object.
	 *
	 * <p>
	 * Together with the identifier of the object ({@code objectId(...)}), the table names the object,
	 * whatever its type is: {@code objectResolve(objectTable($x), objectId($x))} finds the object
	 * again. Objects of different types may be stored in different tables, so the table is needed
	 * wherever objects of more than one type occur in the same place - for example in a view over a
	 * supertype whose rows are instances of several subtypes: a route like
	 * {@code item/:table/:id} carries both texts.
	 * </p>
	 *
	 * @param object
	 *        The object whose table is requested.
	 * @return The name of the table storing the object, or <code>null</code> for no object and for
	 *         one that is only transient - a transient object exists in the current form alone and
	 *         cannot be found again.
	 */
	@Label("Table of an object")
	@SideEffectFree
	public static String table(@Mandatory TLObject object) {
		if (object == null || object.tTransient()) {
			return null;
		}
		return object.tTable().getName();
	}

	/**
	 * The identifier of the given object, as a text that can be part of a URL.
	 *
	 * <p>
	 * The identifier names the object within the table storing it ({@code objectTable(...)}), so
	 * finding the object again takes both texts:
	 * {@code objectResolve(objectTable($x), objectId($x))}. Where all objects that occur are stored
	 * in the same table, the table name can be written into the script as a constant and the
	 * identifier alone carried, for example in a route like {@code ticket/:id}. Where objects of
	 * different tables occur, for example in a view over a supertype whose subtypes are stored in
	 * different tables, both texts are carried, for example in a route like
	 * {@code item/:table/:id}.
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
	 * The object stored in the given table with the given identifier.
	 *
	 * <p>
	 * The table and the identifier together name an object, as delivered by
	 * {@code objectTable(...)} and {@code objectId(...)}: {@code objectResolve(objectTable($x),
	 * objectId($x))} is the object {@code $x}. The table is part of the name because objects of
	 * different types may be stored in different tables: in a view over a supertype whose subtypes
	 * are stored in different tables, both texts are carried, for example in a route like
	 * {@code item/:table/:id}.
	 * </p>
	 *
	 * <p>
	 * Only an object the current user may read is found: for an object the user has no read access
	 * to, the result is <code>null</code>, exactly as for an identifier that names no object. So the
	 * result does not reveal whether an object exists that the user must not see. When the script is
	 * evaluated without access checks, every existing object is found.
	 * </p>
	 *
	 * @param table
	 *        The name of the table storing the object to find.
	 * @param id
	 *        The identifier of the object within the table.
	 * @param usesSecurity
	 *        Whether the call is evaluated with the access rights of the current user.
	 * @return The object with that identifier, or <code>null</code> if there is none - an unknown
	 *         table, an identifier that never existed, one of an object that has been deleted, one
	 *         of an object stored in another table, or one of an object the current user may not
	 *         read.
	 */
	@Label("Object with an identifier")
	@SideEffectFree
	public static TLObject resolve(@Mandatory String table, @Mandatory String id, @UsesSecurity boolean usesSecurity) {
		if (table == null || table.isEmpty() || id == null || id.isEmpty()) {
			return null;
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		MetaObject type = kb.getMORepository().getTypeOrNull(table);
		if (!(type instanceof MOKnowledgeItem) || ((MOKnowledgeItem) type).isAbstract()) {
			// No table storing objects.
			return null;
		}

		TLID name;
		try {
			name = IdentifierUtil.fromExternalForm(id);
		} catch (RuntimeException ex) {
			// Not an identifier this application produces: nothing it could name.
			return null;
		}

		long branch = kb.getHistoryManager().getContextBranch().getBranchId();
		KnowledgeItem item = kb.resolveObjectKey(new DefaultObjectKey(branch, Revision.CURRENT_REV, type, name));
		if (item == null) {
			return null;
		}
		TLObject result = item.getWrapper();
		if (result == null) {
			return null;
		}
		if (usesSecurity && !ModelAccessRights.getInstance().isReadAllowed(result)) {
			// Indistinguishable from an identifier that names nothing.
			return null;
		}
		return result;
	}
}
