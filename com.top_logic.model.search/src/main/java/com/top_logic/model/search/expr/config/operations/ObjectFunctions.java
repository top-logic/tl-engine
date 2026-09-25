/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
import java.util.Collections;

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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * TL-Script functions identifying a persistent object by a text and finding it again.
 *
 * <p>
 * Together the functions make an object addressable from outside the application: its identifier
 * can be put into a URL, a file name or a message, and the object it names can be looked up when
 * such a text comes back - by the type of the object, or by the table storing it.
 * </p>
 */
@ScriptPrefix("object")
public class ObjectFunctions extends TLScriptFunctions {

	/**
	 * The name of the table storing the given object.
	 *
	 * <p>
	 * Normally an object is found again by its type and its identifier ({@code objectId(...)}). The
	 * table is for a use that knows no type at all, such as a fully generic route like
	 * {@code item/:table/:id}: together with the identifier, the table names any persistent object,
	 * whatever its type is, and {@code objectId($x).objectResolve(objectTable($x))} finds it
	 * again.
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
	 * The object is found again from its identifier and its type:
	 * {@code $id.objectResolve(`my:Type`)}. The type may also be a supertype of the object, so a
	 * view over a supertype finds the instances of all its subtypes with the supertype alone, for
	 * example in a route like {@code ticket/:id}. A use that knows no type at all carries the table
	 * storing the object ({@code objectTable(...)}) in addition to its identifier, for example in a
	 * route like {@code item/:table/:id}.
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
	 * The object of the given type or in the given table with the given identifier.
	 *
	 * <p>
	 * Normally, the object is looked up by its type: {@code $id.objectResolve(`my:Type`)} finds the
	 * object with the identifier {@code $id} delivered by {@code objectId(...)}, if it is an instance
	 * of that type. The type may be a supertype: the instances of all its subtypes are found as well, even if
	 * they are stored in different tables. So a view over a supertype needs nothing but the
	 * identifier in its URL.
	 * </p>
	 *
	 * <p>
	 * A use that knows no type at all, such as a fully generic route like {@code item/:table/:id},
	 * looks the object up by the name of the table storing it instead:
	 * {@code objectId($x).objectResolve(objectTable($x))} is the object {@code $x}, whatever its
	 * type is.
	 * </p>
	 *
	 * <p>
	 * Only an object the current user may read is found: for an object the user has no read access
	 * to, the result is <code>null</code>, exactly as for an identifier that names no object. So the
	 * result does not reveal whether an object exists that the user must not see. When the script is
	 * evaluated without access checks, every existing object is found.
	 * </p>
	 *
	 * @param id
	 *        The identifier of the object.
	 * @param typeOrTable
	 *        The type of the object to find, or the name of the table storing it.
	 * @param usesSecurity
	 *        Whether the call is evaluated with the access rights of the current user.
	 * @return The object with that identifier, or <code>null</code> if there is none - an identifier
	 *         that never existed, one of an object that has been deleted, one of an object of another
	 *         type or in another table, an unknown table, or one of an object the current user may
	 *         not read.
	 */
	@Label("Object with an identifier")
	@SideEffectFree
	public static TLObject resolve(@Mandatory String id, @Mandatory Object typeOrTable,
			@UsesSecurity boolean usesSecurity) {
		if (id == null || id.isEmpty()) {
			return null;
		}

		TLID name;
		try {
			name = IdentifierUtil.fromExternalForm(id);
		} catch (RuntimeException ex) {
			// Not an identifier this application produces: nothing it could name.
			return null;
		}

		TLObject result;
		if (typeOrTable instanceof TLStructuredType type) {
			result = lookupByType(type, name);
		} else if (typeOrTable instanceof String table) {
			result = lookup(table, name);
		} else {
			return null;
		}

		if (result == null) {
			return null;
		}
		if (usesSecurity && !ModelAccessRights.getInstance().isReadAllowed(result)) {
			// Indistinguishable from an identifier that names nothing.
			return null;
		}
		return result;
	}

	/**
	 * The instance of the given type (or one of its subtypes) with the given identifier.
	 */
	private static TLObject lookupByType(TLStructuredType type, TLID name) {
		Collection<String> tables;
		if (type instanceof TLClass classType) {
			tables = TLModelUtil.potentialTables(classType, false).keySet();
		} else {
			String table = CompatibilityService.getInstance().getTableFor(type);
			tables = table == null ? Collections.emptyList() : Collections.singletonList(table);
		}

		// Identifiers are allocated from one sequence for all tables, so at most one of the tables
		// stores an object with the given identifier: the first compatible hit is the only one.
		for (String table : tables) {
			TLObject candidate = lookup(table, name);
			if (candidate != null && TLModelUtil.isCompatibleInstance(type, candidate)) {
				// A table may store instances of other types than the requested one.
				return candidate;
			}
		}
		return null;
	}

	/**
	 * The object stored in the given table with the given identifier.
	 */
	private static TLObject lookup(String table, TLID name) {
		if (table.isEmpty()) {
			return null;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		MetaObject type = kb.getMORepository().getTypeOrNull(table);
		if (!(type instanceof MOKnowledgeItem) || ((MOKnowledgeItem) type).isAbstract()) {
			// No table storing objects.
			return null;
		}

		long branch = kb.getHistoryManager().getContextBranch().getBranchId();
		KnowledgeItem item = kb.resolveObjectKey(new DefaultObjectKey(branch, Revision.CURRENT_REV, type, name));
		if (item == null) {
			return null;
		}
		return item.getWrapper();
	}
}
