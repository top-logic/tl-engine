/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;
import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.OperationRestriction;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link AccessParentDefinition} computing the access parent of an object by a TL-Script function.
 *
 * <p>
 * The function receives the object and yields its access parents: a single object or a collection
 * of objects, each of them an access parent of its own. <code>null</code> or an empty collection
 * means that the object has no access parent and is therefore not accessible; a <code>null</code>
 * in a collection is ignored. A value that is no object and an object without access control of its
 * own, like a transient object, is skipped and logged as an error, the other objects still being
 * access parents. A failure of the function denies access and is logged as an error, too.
 * </p>
 *
 * <p>
 * Only committed objects count: an object created in the current transaction is set up by whoever
 * creates it, without any check on the object whose access it would decide, and is skipped without
 * a verdict. An object whose type is without security is not access controlled and opens the object
 * to everybody.
 * </p>
 *
 * <p>
 * The function is evaluated without access checks, so that it can navigate through objects the
 * current user may not read and does not recurse into the access check it is part of. Since access
 * decisions are not kept beyond an interaction and a revision, the function may use any
 * navigation, derived attributes included.
 * </p>
 */
@Label("Script")
public class ScriptAccessParent extends AbstractConfiguredInstance<ScriptAccessParent.Config>
		implements AccessParentDefinition, AccessParentFunction {

	/**
	 * Configuration of {@link ScriptAccessParent}.
	 */
	@TagName("script")
	@DisplayOrder({ Config.EXPR, OperationRestriction.OPERATIONS })
	public interface Config extends PolymorphicConfiguration<ScriptAccessParent>, OperationRestriction {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * Function computing the access parents of the object it receives.
		 */
		@Name(EXPR)
		@Mandatory
		Expr getExpr();

		/**
		 * Setter for {@link #getExpr()}.
		 */
		void setExpr(Expr value);
	}

	private final QueryExecutor _function;

	private final String _source;

	/**
	 * Creates a {@link ScriptAccessParent} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations and reporting an invalid function.
	 * @param config
	 *        The configuration.
	 */
	public ScriptAccessParent(InstantiationContext context, Config config) {
		super(context, config);
		_source = ExprFormat.INSTANCE.getSpecification(config.getExpr());
		QueryExecutor function;
		try {
			function = QueryExecutor.compile(config.getExpr());
			function.disableSecurity();
		} catch (RuntimeException ex) {
			context.error("Invalid access parent script: " + _source, ex);
			function = null;
		}
		_function = function;
	}

	/**
	 * @return This instance, the function being independent of the type it is configured for;
	 *         <code>null</code> when the function could not be compiled.
	 */
	@Override
	public AccessParentFunction resolve(InstantiationContext context, TLClass type) {
		return _function == null ? null : this;
	}

	/**
	 * @return The first access parent the function yields.
	 */
	@Override
	public TLObject resolve(TLObject object) {
		List<TLObject> parents = parents(object);
		return parents.isEmpty() ? null : parents.get(0);
	}

	/**
	 * @return The access parents the function yields, computed when iterating.
	 */
	@Override
	public Iterable<TLObject> resolveAll(TLObject object, BoundCommandGroup operation) {
		if (!delegates(operation)) {
			return List.of();
		}
		return () -> parents(object).iterator();
	}

	/**
	 * Evaluates the function for the given object.
	 *
	 * @return The committed objects with access control of their own the function yields, empty
	 *         when the function fails.
	 */
	private List<TLObject> parents(TLObject object) {
		Object result;
		try {
			result = _function.execute(object);
		} catch (RuntimeException ex) {
			Logger.error("Access parent script failed for " + object + " of type " + object.tType()
				+ ", access is denied: " + _source, ex, ScriptAccessParent.class);
			return List.of();
		}
		if (result == null) {
			return List.of();
		}
		Collection<?> values = result instanceof Collection<?> collection ? collection : List.of(result);
		List<TLObject> parents = new ArrayList<>(values.size());
		for (Object value : values) {
			if (value == null) {
				continue;
			}
			if (value instanceof BoundObject parent) {
				if (CheckedObjects.isStored(parent)) {
					parents.add(parent);
				}
			} else if (value instanceof TLObject) {
				// An object without access control of its own - a transient object, for instance -
				// would open the object to every user, since nobody decides for it.
				Logger.error("Access parent script yields an object without access control for " + object
					+ " of type " + object.tType() + ", it is skipped: " + value + " (" + _source + ")",
					ScriptAccessParent.class);
			} else {
				Logger.error("Access parent script yields a value that is no object for " + object + " of type "
					+ object.tType() + ", it is skipped: " + value + " (" + _source + ")", ScriptAccessParent.class);
			}
		}
		return parents;
	}

	/**
	 * @return <code>null</code>: the types of the computed access parents are not known statically.
	 */
	@Override
	public Set<TLClass> getParentTypes(TLClass type) {
		return null;
	}

	@Override
	public ResKey getLabel() {
		return ResKey.text(_source);
	}

}
