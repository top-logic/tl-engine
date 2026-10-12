/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.trace;

import java.util.List;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Sink;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.TracedValueComputation;
import com.top_logic.model.search.configured.QueryExecutorMethod;
import com.top_logic.model.search.configured.TracingScriptMethod;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.AccessLike;
import com.top_logic.model.search.expr.DynamicGet;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.interpreter.Rewriter;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.Pointer;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Rewriter} replacing expressions accessing model properties with those that create an
 * access trace in the context variable {@link TracingAccessRewriter#TRACE}.
 */
public final class TracingAccessRewriter extends Rewriter<Void> {

	/**
	 * Singleton {@link TracingAccessRewriter} instance.
	 */
	public static final TracingAccessRewriter INSTANCE =
		new TracingAccessRewriter();
	/**
	 * Variable identifier in the {@link EvalContext} that contains all accessed model references
	 * during an evaluation.
	 */
	public static final NamedConstant TRACE = new NamedConstant("trace");

	/**
	 * Variable identifier in the {@link EvalContext} that contains the
	 * {@link AttributeUpdateContainer} during an evaluation.
	 */
	public static final NamedConstant UPDATE_CONTAINER = new NamedConstant("updateContainer");

	private TracingAccessRewriter() {
		// Singleton constructor.
	}

	@Override
	public SearchExpression visitAccess(Access expr, Void arg) {
		SearchExpression self = descendPart(expr, arg, expr.getSelf());
		return new TracingAccess(self, expr.getPart(), expr.usesSecurity());
	}

	@Override
	public SearchExpression visitGenericMethod(GenericMethod expr, Void arg) {
		if (expr instanceof DynamicGet dynamicGet) {
			List<SearchExpression> argumentsList = descendParts(expr, arg, dynamicGet.getArguments());
			SearchExpression[] arguments = argumentsList.toArray(new SearchExpression[0]);
			return new TracingDynamicGet(dynamicGet.getName(), arguments, dynamicGet.usesSecurity());
		} else if (expr instanceof QueryExecutorMethod queryMethod) {
			List<SearchExpression> argumentsList = descendParts(queryMethod, arg, queryMethod.getArguments());
			SearchExpression[] arguments = argumentsList.toArray(new SearchExpression[0]);

			return new TracingScriptMethod(queryMethod.getName(), arguments, queryMethod.usesSecurity());
		} else {
			return super.visitGenericMethod(expr, arg);
		}
	}

	/**
	 * Reads an attribute of an object the way a script access does in the given evaluation.
	 *
	 * <p>
	 * In a tracing evaluation (the given context defines {@link #TRACE}), the access is reported
	 * and the value is looked up with
	 * {@link #lookupTraced(EvalContext, TLObject, TLStructuredTypePart, boolean)}. Otherwise, the
	 * value is looked up with {@link AccessLike#lookupValue(TLObject, TLStructuredTypePart, boolean)}.
	 * </p>
	 *
	 * @param definitions
	 *        The context of the current evaluation.
	 * @param self
	 *        The object to access, <code>null</code> results in <code>null</code>.
	 * @param part
	 *        The attribute to look up.
	 * @param withSecurity
	 *        Whether the read access to the attribute must be checked.
	 * @return The value of the given attribute.
	 *
	 * @see EvalContext#access(TLObject, TLStructuredTypePart, boolean)
	 */
	public static Object access(EvalContext definitions, TLObject self, TLStructuredTypePart part,
			boolean withSecurity) {
		if (self == null) {
			return null;
		}
		if (definitions.hasVar(TRACE)) {
			return lookupTraced(definitions, self, part, withSecurity);
		}
		return AccessLike.lookupValue(self, part, withSecurity);
	}

	/**
	 * Looks up the value of an attribute in a tracing evaluation.
	 *
	 * <p>
	 * The access itself is reported to the trace in the context variable {@link #TRACE}. The value
	 * is read from the overlay of the given object, if the {@link OverlayLookup} in the context
	 * variable {@link #UPDATE_CONTAINER} has one. If the attribute's {@link StorageDetail storage}
	 * is a {@link TracedValueComputation}, the value is computed through
	 * {@link TracedValueComputation#computeTraced(TLObject, TLStructuredTypePart, Sink, OverlayLookup)}
	 * with the same trace and overlay lookup, so that the accesses of the computation are reported
	 * as well and the computation is based on the edited values. Otherwise, the value is looked up
	 * with {@link AccessLike#lookupValue(TLObject, TLStructuredTypePart, boolean)}.
	 * </p>
	 *
	 * @param definitions
	 *        The tracing {@link EvalContext} defining {@link #TRACE} and {@link #UPDATE_CONTAINER}.
	 * @param self
	 *        The object to access.
	 * @param part
	 *        The attribute to look up.
	 * @param withSecurity
	 *        Whether the read access to the attribute must be checked.
	 * @return The value of the given attribute.
	 */
	public static Object lookupTraced(EvalContext definitions, TLObject self, TLStructuredTypePart part,
			boolean withSecurity) {
		traceAccess(definitions, self, part);

		OverlayLookup overlays = (OverlayLookup) definitions.getVar(UPDATE_CONTAINER);
		TLObject target = self;
		if (overlays != null) {
			TLObject overlay = overlays.getExistingOverlay(self);
			if (overlay != null) {
				target = overlay;
			}
		}

		TLStructuredTypePart concretePart = concretePart(target, part);
		if (concretePart != null
			&& concretePart.getStorageImplementation() instanceof TracedValueComputation computation) {
			if (withSecurity && !ModelAccessRights.getInstance().isReadAllowed(target, part)) {
				return TLModelUtil.getEmptyValue(part);
			}
			Object value = computation.computeTraced(target, concretePart, trace(definitions), overlays);
			return SearchExpression.normalizeValue(value);
		}

		return AccessLike.lookupValue(target, part, withSecurity);
	}

	/**
	 * The part of the type of the given object that implements the given (potentially abstract)
	 * part, or <code>null</code> if it cannot be resolved.
	 */
	private static TLStructuredTypePart concretePart(TLObject self, TLStructuredTypePart part) {
		if (!part.isAbstract()) {
			return part;
		}
		TLStructuredType type = self.tType();
		if (type == null) {
			return null;
		}
		return type.getPart(part.getName());
	}

	/**
	 * Marks the combination of {@link TLObject self} and {@link TLStructuredTypePart part} as
	 * relevant for the trace.
	 */
	public static void traceAccess(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
		trace(definitions).add(Pointer.create(self, part));
	}

	@SuppressWarnings("unchecked")
	private static Sink<Pointer> trace(EvalContext definitions) {
		return (Sink<Pointer>) definitions.getVar(TRACE);
	}
}