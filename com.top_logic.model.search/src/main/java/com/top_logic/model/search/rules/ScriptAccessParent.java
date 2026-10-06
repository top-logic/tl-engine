/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.rules;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.AccessParentDefinition;
import com.top_logic.model.security.AccessParentFunction;

/**
 * {@link AccessParentDefinition} computing the access parent of an object by a TL-Script function.
 *
 * <p>
 * The function receives the object and yields its access parent. A result of exactly one object, or
 * a collection holding exactly one object, is the access parent. <code>null</code> or an empty
 * collection means that the object has no access parent and is therefore not accessible. Any other
 * result - several objects, a value that is no object - and a failure of the function deny access,
 * too, and are logged as an error: the author of the function is responsible for it yielding at
 * most one object, also when it navigates a multi-valued reference.
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
	public interface Config extends PolymorphicConfiguration<ScriptAccessParent> {

		/** Configuration name for {@link #getExpr()}. */
		String EXPR = "expr";

		/**
		 * Function computing the access parent of the object it receives.
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

	@Override
	public TLObject resolve(TLObject object) {
		Object result;
		try {
			result = _function.execute(object);
		} catch (RuntimeException ex) {
			Logger.error("Access parent script failed for " + object + " of type " + object.tType()
				+ ", access is denied: " + _source, ex, ScriptAccessParent.class);
			return null;
		}
		if (result == null) {
			return null;
		}
		if (result instanceof TLObject parent) {
			return parent;
		}
		if (result instanceof Collection<?> collection) {
			if (collection.isEmpty()) {
				return null;
			}
			if (collection.size() == 1 && collection.iterator().next() instanceof TLObject parent) {
				return parent;
			}
		}
		Logger.error("Access parent script yields no single object for " + object + " of type " + object.tType()
			+ ", access is denied: " + result + " (" + _source + ")", ScriptAccessParent.class);
		return null;
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
