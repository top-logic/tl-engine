/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.rules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.rule.BaseObjects;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.BinaryOperation;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.Intersection;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Referers;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.UnaryOperation;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;
import com.top_logic.model.search.expr.visit.GenericDescendingVisitor;
import com.top_logic.model.search.expr.visit.ToString;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link PathElement} implementation that uses a TL-Script {@link Lambda} expression to navigate,
 * from a given base object, to the objects that serve as input to the next step in a role-rule
 * path.
 *
 * <p>
 * At construction time the expression is analysed once to extract two things:
 * </p>
 * <ul>
 * <li><b>Relevant parts</b>: all {@link TLStructuredTypePart}s referenced anywhere in the
 * expression. The access manager uses this set to decide which attribute changes can affect the
 * rule at all.</li>
 * <li><b>Invertible chain</b>: a sequence of {@link Step}s that mirrors the expression body and
 * supports backward navigation. This enables {@link #getSources sources} and {@link #getPathBase
 * path base} to return precise, partial invalidation sets instead of always requiring a full
 * rebuild. If the expression cannot be decomposed into a supported chain, both methods fall back to
 * {@link BaseObjects#all() "recompute all"}.</li>
 * </ul>
 *
 * <p>
 * The expression body may contain {@link Access} ({@code .get(...)}), {@link Referers}
 * ({@code .referers(...)}), {@link Filter} ({@code .filter(...)}), {@link IfElse}, and
 * {@link Union}. Inside filter predicates, equality and containment comparisons ({@code ==},
 * {@code containsElement}, {@code containsSome}, {@code containsAll}) between expressions rooted in
 * the lambda parameter and expressions rooted in outer captured variables are recognised and
 * contribute to the backward-navigation map, allowing precise pivot computation when either side of
 * such a comparison changes.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PathByExpression extends AbstractConfiguredInstance<PathByExpression.Config> implements PathElement {

	/**
	 * {@link DefaultDescendingVisitor} that checks whether a {@link Var} is contained with a key
	 * different to the argument key.
	 */
	private static final class UsesUndefinedVariable extends DefaultDescendingVisitor<Boolean, Set<NamedConstant>> {

		static final UsesUndefinedVariable INSTANCE = new UsesUndefinedVariable();

		public boolean check(SearchExpression expr) {
			Set<NamedConstant> allowed = new HashSet<>();
			return expr.visit(this, allowed);
		}

		@Override
		public Boolean visitLambda(Lambda expr, Set<NamedConstant> arg) {
			NamedConstant key = expr.getKey();
			boolean added = arg.add(key);
			if (!added) {
				throw new IllegalStateException("Key '" + key + "' is also used by a different Lambda");
			}
			try {
				return super.visitLambda(expr, arg);
			} finally {
				arg.remove(key);
			}
		}

		@Override
		public Boolean visitVar(Var expr, Set<NamedConstant> arg) {
			return !arg.contains(expr.getKey());
		}

		@Override
		protected Boolean combine(Boolean result1, Boolean result2) {
			return result1.booleanValue() || result2.booleanValue();
		}

		@Override
		protected Boolean none() {
			return Boolean.FALSE;
		}
	}

	/**
	 * Typed configuration interface definition for {@link PathByExpression}.
	 */
	@TagName("script-step")
	public interface Config extends PolymorphicConfiguration<PathByExpression> {

		/** Configuration name for {@link #getExpression()}. */
		String EXPRESSION = "expr";

		/**
		 * The expression calculates, based on a base object for the path element, the objects that
		 * serve as base elements for the next step.
		 */
		@Mandatory
		@Name(EXPRESSION)
		Expr getExpression();

		/**
		 * Setter for {@link #getExpression()}.
		 */
		void setExpression(Expr expr);
	}

	/**
	 * A single step in a navigation chain that supports inverse traversal.
	 */
	private interface Step {

		/**
		 * Applies the inverse of this step to every object in {@code objects} and collects all
		 * results.
		 *
		 * @return the union of all inverse results, or {@code null} if any individual inversion
		 *         failed (causes fallback to {@link BaseObjects#all()})
		 */
		Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects);

		/**
		 * Whether this step directly navigates via the given part, making this step the relevant
		 * split point when that part changes.
		 */
		boolean usesPart(TLStructuredTypePart part);

		/**
		 * Determines the "pivot" objects to apply preceding inverse steps to when {@code part}
		 * changed on {@code element}.
		 *
		 * <p>
		 * Only called when {@link #usesPart(TLStructuredTypePart)} returned {@code true}.
		 * </p>
		 *
		 * @return the pivot objects, or {@code null} if they cannot be determined
		 */
		Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part, Supplier<?> partValue);

	}

	/**
	 * Forward attribute access: {@code obj.tValue(part)}.
	 *
	 * <p>
	 * Inverse: {@code obj.tReferers(part)} -- only possible when {@code part} is a
	 * {@link TLReference}.
	 * </p>
	 */
	private record AccessStep(TLStructuredTypePart part) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			if (part instanceof TLReference ref) {
				return objects.stream()
					.map(obj -> obj.tReferers(ref))
					.flatMap(Set::stream)
					.collect(Collectors.toSet());
			}
			return null; // non-reference attribute, not invertible
		}

		@Override
		public boolean usesPart(TLStructuredTypePart p) {
			return matchesPart(part, p);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart changedPart,
				Supplier<?> partValue) {
			// element is the object on which tValue(part) was called -- it is the pivot itself
			return Collections.singleton(element);
		}

	}

	/**
	 * Backward reference navigation: {@code obj.tReferers(reference)}.
	 *
	 * <p>
	 * Inverse: {@code obj.tValue(reference)}.
	 * </p>
	 */
	private record ReferersStep(TLReference reference) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			return objects.stream()
				.map(obj -> asObjects(obj.tValue(reference)))
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		}

		@Override
		public boolean usesPart(TLStructuredTypePart p) {
			return matchesPart(reference, p);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			// element changed its reference; the new reference targets are the pivots
			return asObjects(partValue.get());
		}

	}

	/**
	 * Filter step: passes objects through unchanged during inverse navigation.
	 *
	 * <p>
	 * When navigating backwards, objects that reach this point already passed the filter in the
	 * forward direction, so the inverse is the identity.
	 * </p>
	 *
	 * <p>
	 * {@code predicateChain} is the step sequence extracted from the filter lambda's body. It
	 * encodes how to navigate from a changed object (reached via the filter parameter) back to the
	 * filter element. Parts from outer-side expressions in equality/containment comparisons are
	 * tracked separately via {@link LetBindingStep}s at position&nbsp;0 of the enclosing chain.
	 * </p>
	 */
	private record FilterStep(Lambda filterLambda, List<Step> predicateChain) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			if (filterLambda == null) {
				return objects;
			}

			Set<TLObject> result = new HashSet<>();
			EvalContext evalContext = null;
			for (TLObject obj : objects) {
				if (evalContext == null) {
					evalContext = new EvalContext(false, obj.tKnowledgeBase(), obj.tType().getModel(), null, null);
				}
				if (SearchExpression.isTrue(filterLambda.eval(evalContext, obj))) {
					result.add(obj);
				}
			}
			return result;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(predicateChain, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			return chainPivot(predicateChain, element, part, partValue);
		}

	}

	/**
	 * Branching step for expressions with two parallel sub-chains, e.g. {@code if/else} or
	 * {@code union}.
	 *
	 * <p>
	 * Inverse navigation collects results from both branches and returns their union.
	 * </p>
	 *
	 * <ul>
	 * <li>For {@link Union}: the union is exact -- both branches always contribute in the forward
	 * direction.</li>
	 * <li>For {@link IfElse}: the union is an over-approximation -- objects from the branch that
	 * was NOT taken may be included. For role-rule evaluation this is safe: at most unnecessary
	 * re-evaluations occur, but no sources are missed.</li>
	 * </ul>
	 */
	private record BranchStep(List<Step> left, List<Step> right) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			Set<TLObject> result = new HashSet<>();
			for (TLObject obj : objects) {
				Collection<? extends TLObject> fromLeft = applyChainInverse(left, obj);
				Collection<? extends TLObject> fromRight = applyChainInverse(right, obj);
				if (fromLeft == null || fromRight == null) {
					return null;
				}
				result.addAll(fromLeft);
				result.addAll(fromRight);
			}
			return result;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(left, part) || chainUsesPart(right, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			Collection<? extends TLObject> fromLeft = chainPivot(left, element, part, partValue);
			Collection<? extends TLObject> fromRight = chainPivot(right, element, part, partValue);
			if (fromLeft == null || fromRight == null) {
				return null;
			}
			Set<TLObject> result = new HashSet<>(fromLeft);
			result.addAll(fromRight);
			return result;
		}

	}

	/**
	 * Transparent step inserted at index 0 of the chain for each TL-Script let-binding ({@code {
	 * var = value; body }}) whose binding expression ({@code value}) is rooted in the outer lambda
	 * parameter.
	 *
	 * <p>
	 * The {@code bindingChain} is the step-sequence extracted from the binding expression itself
	 * (by calling {@link #extractSubChain} on it). It supports precise base-object computation when
	 * any part referenced inside the binding changes:
	 * </p>
	 * <ul>
	 * <li>When a {@link Referers} reference (e.g. {@code Assembly#area} in
	 * {@code $areaInstance.referers(Assembly#area)}) changes on element {@code A} to new value
	 * {@code I_new}: {@link #pivot} delegates to {@link #chainPivot} on the binding chain, which
	 * returns {@code asObjects(I_new)} -- the new value IS the relevant base object.</li>
	 * <li>When an {@link Access} attribute (e.g. {@code Assembly#plant}) on an intermediate object
	 * changes: {@link #chainPivot} traces backwards through the binding chain to reach the outer
	 * lambda parameter value (e.g. the ZAreaInstance).</li>
	 * </ul>
	 *
	 * <p>
	 * {@link #applyInverse} is the identity -- the step is invisible during backward navigation
	 * through the main chain ({@link #getSources}).
	 * </p>
	 */
	private record LetBindingStep(List<Step> bindingChain) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			return objects;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(bindingChain, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			return chainPivot(bindingChain, element, part, partValue);
		}

	}

	/**
	 * {@link Step} representing a navigation sub-chain rooted in a fixed {@link Literal}
	 * {@link TLObject}, followed by an arbitrary sequence of navigation {@link Step}s accumulated
	 * by {@link PathByExpression#extractSubChain} before reaching the literal terminus.
	 *
	 * <p>
	 * Used when one branch of a union/intersection is not reachable from the lambda parameter but
	 * evaluates to a deterministic constant value. This allows the branch to participate in both
	 * inverse navigation and change-tracking without forcing a full {@link BaseObjects#all()}
	 * fallback.
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #applyInverse}: navigates backward from the given object through the inverse of
	 * each step and checks whether {@link #root} is reachable. If yes (obj is in the constant
	 * result), every base object contributes obj through this branch, so {@code null}
	 * ({@link BaseObjects#all()}) is returned. If no, an empty set is returned.</li>
	 * <li>{@link #pivot}: for {@link AccessStep}s in the chain, precisely identifies the anchor
	 * object whose attribute must change to affect the result. For all other step types (e.g.
	 * {@link ReferersStep}, {@link FilterStep}), falls back conservatively to {@code null}
	 * ({@link BaseObjects#all()}).</li>
	 * </ul>
	 *
	 * <p>
	 * {@link #steps} is stored in the same accumulation order as the enclosing
	 * {@link #extractSubChain} walk (outermost step first, innermost last). Both
	 * {@link #applyInverse} and {@link #pivot} navigate backward through these steps using
	 * {@link Step#applyInverse}.
	 * </p>
	 */
	private record ConstantBranchStep(TLObject root, List<Step> steps) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			for (TLObject obj : objects) {
				Collection<? extends TLObject> current = Collections.singleton(obj);
				for (int i = 0; i < steps.size(); i++) {
					current = steps.get(i).applyInverse(current);
					if (current == null) {
						// Can't determine whether obj is in the result: fall back to
						// BaseObjects.all().
						return null;
					}
				}
				// obj IS in the constant result: every base object contributes obj through this
				// branch.
				if (current.contains(root)) {
					return null;
				}
			}
			return Collections.emptySet();
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(steps, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			for (int i = 0; i < steps.size(); i++) {
				if (!steps.get(i).usesPart(part)) {
					continue;
				}
				// Check whether element is the anchor for step i: navigate backward from element
				// through the steps that precede step i in forward order (indices i+1..size-1 in
				// accumulation order) and check whether root is reachable.
				Collection<? extends TLObject> current = Collections.singleton(element);
				for (int j = i + 1; j < steps.size(); j++) {
					current = steps.get(j).applyInverse(current);
					if (current == null) {
						return null; // cannot determine -> conservative
					}
				}
				if (current.contains(root)) {
					return null; // element is the anchor; the change is relevant
				}
			}
			return Collections.emptySet();
		}

	}

	/**
	 * Step for a comparison or boolean predicate where one operand can be traced back to the
	 * filter-lambda parameter (the {@link #knownChain}) while the other operand references
	 * {@link TLStructuredTypePart}s that are not reachable from any enclosing lambda parameter (the
	 * {@link #externalParts}).
	 *
	 * <p>
	 * Constant sub-expressions (those rooted in a {@link Literal} {@link TLObject}) on the external
	 * side are handled by {@link #extractSubChain} returning a {@link ConstantBranchStep}, which
	 * causes the predicate to be represented as a {@link BranchStep} instead of an
	 * {@link ExternalPredicateStep}. This step therefore only appears when the external side is
	 * genuinely dynamic (not a known constant).
	 * </p>
	 *
	 * <p>
	 * When a part from {@link #knownChain} changes, the affected filter elements are determined
	 * precisely via {@link #chainPivot}. When a part from {@link #externalParts} changes it is
	 * impossible to decide which filter elements are affected -- {@link #pivot} returns
	 * {@code null} so that the caller falls back to {@link BaseObjects#all()}.
	 * </p>
	 *
	 * <p>
	 * {@link #applyInverse} is the identity: this step is transparent during backward navigation
	 * ({@link #getSources}).
	 * </p>
	 */
	private record ExternalPredicateStep(List<Step> knownChain, Collection<TLStructuredTypePart> externalParts)
			implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			return objects;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(knownChain, part) || externalParts.contains(part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			if (chainUsesPart(knownChain, part)) {
				return chainPivot(knownChain, element, part, partValue);
			}
			// External part: any filter element could be affected by this change.
			return null;
		}

	}

	/**
	 * Step for an {@link IfElse} expression where the condition and/or branches reference model
	 * attributes.
	 *
	 * <p>
	 * When a condition attribute changes, the condition chain ({@link #condChain}) is navigated
	 * backwards to find the specific base objects whose branch selection may have changed. If the
	 * condition expression is not analyzable ({@link #condChain} is {@code null}), any base object
	 * may be affected and {@link #pivot} returns {@code null} -&gt; {@link BaseObjects#all()}. When
	 * a branch attribute changes, the pivot is computed from that branch's chain.
	 * </p>
	 *
	 * <p>
	 * {@link #applyInverse} returns the union of both branch chains' inverse results when both are
	 * analyzable, and {@code null} (fall back to {@link BaseObjects#all()}) otherwise.
	 * </p>
	 */
	private record IfElseStep(
			List<Step> ifChain,
			List<Step> elseChain,
			List<Step> condChain,
			Collection<TLStructuredTypePart> condExternalParts,
			Collection<TLStructuredTypePart> ifExternalParts,
			Collection<TLStructuredTypePart> elseExternalParts) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			if (ifChain == null || elseChain == null) {
				// One branch not fully analyzable: cannot determine all sources.
				return null;
			}
			Set<TLObject> result = new HashSet<>();
			for (TLObject obj : objects) {
				Collection<? extends TLObject> fromIf = applyChainInverse(ifChain, obj);
				if (fromIf == null) {
					return null;
				}
				Collection<? extends TLObject> fromElse = applyChainInverse(elseChain, obj);
				if (fromElse == null) {
					return null;
				}
				result.addAll(fromIf);
				result.addAll(fromElse);
			}
			return result;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return (condChain != null ? chainUsesPart(condChain, part) : condExternalParts.contains(part))
				|| (ifChain != null ? chainUsesPart(ifChain, part) : ifExternalParts.contains(part))
				|| (elseChain != null ? chainUsesPart(elseChain, part) : elseExternalParts.contains(part));
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			if (condExternalParts.contains(part) || ifExternalParts.contains(part)
				|| elseExternalParts.contains(part)) {
				return null;
			}
			Set<TLObject> result = new HashSet<>();
			if (condChain != null && chainUsesPart(condChain, part)) {
				Collection<? extends TLObject> condPivot = chainPivot(condChain, element, part, partValue);
				if (condPivot == null) {
					return null;
				}
				result.addAll(condPivot);
			}
			if (ifChain != null && chainUsesPart(ifChain, part)) {
				Collection<? extends TLObject> ifPivot = chainPivot(ifChain, element, part, partValue);
				if (ifPivot == null) {
					return null;
				}
				result.addAll(ifPivot);
			}
			if (elseChain != null && chainUsesPart(elseChain, part)) {
				Collection<? extends TLObject> elsePivot = chainPivot(elseChain, element, part, partValue);
				if (elsePivot == null) {
					return null;
				}
				result.addAll(elsePivot);
			}
			return result;
		}

	}

	/**
	 * Step for a {@link Foreach} ({@code .map(elem -> ...)}) expression.
	 *
	 * <p>
	 * Forward: applies the inner chain to each element of the base collection and flattens results.
	 * Inverse: delegates to {@link #applyChainInverse} on the inner chain -- finds all base
	 * elements that map to the given destination object.
	 * </p>
	 */
	private record ForeachStep(List<Step> innerChain) implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			Set<TLObject> result = new HashSet<>();
			for (TLObject obj : objects) {
				Collection<? extends TLObject> inv = applyChainInverse(innerChain, obj);
				if (inv == null) {
					return null;
				}
				result.addAll(inv);
			}
			return result;
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(innerChain, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			return chainPivot(innerChain, element, part, partValue);
		}

	}

	/**
	 * Step representing an {@code all(module:Type)} source expression, optionally followed by a
	 * chain of steps applied to the enumerated objects.
	 *
	 * <p>
	 * {@link #steps} are the forward steps applied to the output of {@code all(instanceType)} (in
	 * application order). {@link #outerLetBindings} captures {@link LetBindingStep}s collected from
	 * the enclosing lambda's pending-let-binding list during filter-predicate analysis; each such
	 * step represents a navigation path starting from the outer lambda parameter (the base object)
	 * to an attribute that appears on the outer-variable side of a filter comparison.
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #applyInverse}: returns {@code null} (all base objects may be relevant) when any
	 * input object is reachable from {@code all(instanceType)} via the inverse of {@link #steps},
	 * and an empty set when none is.</li>
	 * <li>{@link #usesPart}: checks both the steps over enumerated instances ({@link #steps}) and
	 * the outer-variable attribute paths ({@link #outerLetBindings}).</li>
	 * <li>{@link #pivot}: for parts used within {@link #steps} (enumerated-instance side), returns
	 * {@code null} conservatively when any enumerated instance is affected, or an empty set when
	 * none is. For parts used only via {@link #outerLetBindings} (base-object side), computes the
	 * precise set of affected base objects via {@link #chainPivot}.</li>
	 * </ul>
	 */
	private record AllStep(TLStructuredType instanceType, List<Step> steps, List<Step> outerLetBindings)
			implements Step {

		@Override
		public Collection<? extends TLObject> applyInverse(Collection<? extends TLObject> objects) {
			for (TLObject obj : objects) {
				Collection<? extends TLObject> current = applyChainInverse(steps, obj);
				if (current == null) {
					return null;
				}
				boolean anyCompatible = current.stream()
					.anyMatch(result -> TLModelUtil.isCompatibleInstance(instanceType, result));
				if (anyCompatible) {
					return null;
				}
			}
			return Collections.emptySet();
		}

		@Override
		public boolean usesPart(TLStructuredTypePart part) {
			return chainUsesPart(steps, part) || chainUsesPart(outerLetBindings, part);
		}

		@Override
		public Collection<? extends TLObject> pivot(TLObject element, TLStructuredTypePart part,
				Supplier<?> partValue) {
			if (chainUsesPart(steps, part)) {
				// The part is used within the steps that process the enumerated instances (e.g. a
				// filter-predicate attribute). If backward navigation through those steps from the
				// changed element yields an empty set, no enumerated instance is affected and no
				// base object needs re-evaluation.
				Collection<? extends TLObject> innerPivot = chainPivot(steps, element, part, partValue);
				if (innerPivot != null && innerPivot.isEmpty()) {
					return Collections.emptySet();
				}
				// At least one enumerated instance is affected (or the result is indeterminate),
				// but without further context we cannot determine which base objects correspond.
				return null;
			}
			// The part is referenced on the base-object side via an outer-variable expression
			// captured in outerLetBindings. Backward navigation through those chains yields the
			// precise set of affected base objects.
			return chainPivot(outerLetBindings, element, part, partValue);
		}

	}

	/** The compiled expression used by {@link #getValues(TLObject)} for forward navigation. */
	private QueryExecutor _expression;

	/**
	 * All {@link TLStructuredTypePart}s referenced anywhere in {@link #_expression}. Returned by
	 * {@link #getRelevantParts()} so that the access manager knows which attribute changes can
	 * affect this path step.
	 */
	private Collection<TLStructuredTypePart> _relevantParts;

	/**
	 * Invertible chain of steps derived from {@link #_expression}, in application order (first
	 * applied first), or {@code null} when the expression body cannot be decomposed into a
	 * supported chain. When {@code null}, {@link #getSources} and {@link #getPathBase} fall back to
	 * {@link BaseObjects#all()}.
	 */
	private List<Step> _chain;

	/**
	 * Create a {@link PathByExpression}.
	 *
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PathByExpression(InstantiationContext context, Config config) {
		super(context, config);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLModel model = ModelService.getApplicationModel();
		_expression = QueryExecutor.compile(kb, model, config.getExpression());

		/* Do not use QueryExecutor#getSearch(), because access to attributes may have been removed
		 * to create KBQuery expressions. */
		SearchExpression search = SearchBuilder.toSearchExpression(model, config.getExpression());
		search = QueryExecutor.resolve(model, search);
		_relevantParts = extractPartsAddOverrides(search);
		for (TLStructuredTypePart part : _relevantParts) {
			if (part.isDerived()) {
				context.error("Script-step expression navigates through derived (computed) attribute '"
					+ TLModelUtil.qualifiedName(part)
					+ "'. Derived attributes do not fire change notifications and cannot be tracked"
					+ " for role-rule invalidation.");
			}
		}
		_chain = extractChain(search);
	}

	private static boolean hasParts(SearchExpression expression) {
		return !extractParts(expression).isEmpty();
	}

	private static Collection<TLStructuredTypePart> extractPartsAddOverrides(SearchExpression expression) {
		TLModelOperations modelCacheOperations = modelCacheOperations();
		Predicate<TLStructuredTypePart> notDerived = notDerived();
		return extractParts(expression)
			.stream()
			.flatMap(part -> Stream.concat(
				modelCacheOperations.getOverrides(part).stream().filter(notDerived),
				Stream.of(part)))
			.collect(Collectors.toSet());
	}

	private static Collection<TLStructuredTypePart> extractParts(SearchExpression expression) {
		HashSet<TLStructuredTypePart> parts = new HashSet<>();
		expression.visit(new GenericDescendingVisitor<Void, Void>() {
			@Override
			protected Void wrap(Object value) {
				if (value instanceof TLStructuredTypePart part) {
					parts.add(part);
				}
				return null;
			}

			@Override
			protected Void compose(Class<?> type, Void arg, List<Void> contents) {
				return null;
			}
		}, null);
		return parts;
	}

	/**
	 * Tries to decompose {@code expression} into an invertible chain of steps.
	 *
	 * <p>
	 * The expression must be a {@link Lambda} whose body consists of {@link Access},
	 * {@link Referers}, {@link Filter}, {@link IfElse}, {@link Union}, and
	 * {@link Call}{@code (}{@link Lambda}{@code , ...)} let-bindings (TL-Script block expressions
	 * {@code { var = value; body }}) ending in the lambda's own {@link Var}. Any other structure
	 * causes the method to return {@code null}.
	 * </p>
	 *
	 * @return the steps in application order (first applied first), or {@code null}
	 */
	private static List<Step> extractChain(SearchExpression expression) {
		if (!(expression instanceof Lambda lambda)) {
			/* In this case getValues() does not depend on the input. When any attribute value
			 * changes, it is necessary to re-compute this path for all objects. */
			return null;
		}
		return extractSubChain(lambda.getBody(), lambda.getKey(), new HashMap<>());
	}

	/**
	 * Extracts a chain of steps from {@code body} up to the lambda variable {@code paramName},
	 * accumulating let-bindings encountered along the way into {@code bindings}.
	 *
	 * <p>
	 * TL-Script block expressions {@code { var = value; body }} compile to
	 * {@code Call(Lambda("var", body), value)}. Whenever such a node is encountered, the binding
	 * {@code var -> value} is added to {@code bindings} (for use by filter back-navigation
	 * analysis) and the walk continues with the lambda body.
	 * </p>
	 *
	 * @param body
	 *        the expression to decompose
	 * @param paramName
	 *        {@link Lambda#getKey()} of the outermost lambda; the chain terminates when a
	 *        {@link Var} whose {@link Var#getKey()} matches this constant is found
	 * @param bindings
	 *        mutable map of let-bindings accumulated so far; updated in place as new let-bindings
	 *        are encountered; the caller must ensure each branch receives an independent copy
	 * @return steps in application order, or {@code null} if the body is not a supported chain
	 */
	private static List<Step> extractSubChain(SearchExpression body, NamedConstant paramName,
			Map<NamedConstant, SearchExpression> bindings) {
		return extractSubChain(body, paramName, Collections.emptyMap(), bindings);
	}

	/**
	 * @param body
	 *        the expression to decompose
	 * @param paramName
	 *        {@link Lambda#getKey()} of the innermost active lambda; the walk terminates when a
	 *        {@link Var} whose {@link Var#getKey()} is identical to this constant is encountered
	 * @param outerAccumulators
	 *        maps each enclosing lambda's {@link Lambda#getKey()} to the {@link LetBindingStep} list that
	 *        collects outer-side chains found at <em>any</em> nesting depth below that lambda. When
	 *        a filter predicate comparison references a variable from an outer scope, the extracted
	 *        chain is added to the list for the matching outer parameter and ends up at
	 *        position&nbsp;0 of that outer lambda's chain after all filter levels unwind. Because
	 *        the same list object is shared across nesting levels, a deeply nested reference (e.g.
	 *        two filter levels deep) writes directly into the correct level's accumulator without
	 *        extra propagation.
	 * @param bindings
	 *        mutable map from a let-binding lambda's {@link Lambda#getKey()} to its binding
	 *        expression, accumulated as {@code Call(Lambda("var", body), value)} nodes are
	 *        traversed; used to substitute variable references encountered later in the walk; the
	 *        caller must pass an independent copy for each branch so that bindings from one branch
	 *        do not leak into another
	 * @return steps in application order, or {@code null} if the body is not a supported chain
	 */
	private static List<Step> extractSubChain(SearchExpression body, NamedConstant paramName,
			Map<NamedConstant, List<LetBindingStep>> outerAccumulators,
			Map<NamedConstant, SearchExpression> bindings) {
		SearchExpression current = body;
		// Collect from outermost (last applied) to innermost (first applied).
		List<Step> steps = new ArrayList<>();
		// LetBindingSteps are collected separately and appended last (before reverse), so they end
		// up at index 0 in the final chain. Index 0 is required so that no preceding inverse steps
		// are applied to the pivot objects they produce.
		List<LetBindingStep> pendingLetBindings = new ArrayList<>();
		while (true) {
			if (current instanceof Access access) {
				steps.add(new AccessStep(access.getPart()));
				current = access.getSelf();
			} else if (current instanceof Referers referers) {
				steps.add(new ReferersStep(referers.getReference()));
				current = referers.getTarget();
			} else if (current instanceof Filter filter) {
				if (!(filter.getFunction() instanceof Lambda filterLambda)) {
					return null;
				}
				// Register this lambda's paramName in the outer-accumulator map so that
				// comparisons inside the predicate (at any depth) can write their outer-side
				// LetBindingSteps directly into filterOuterSteps, which is then appended to
				// pendingLetBindings below. All deeper levels already in outerAccumulators are
				// passed through unchanged so they remain reachable even from nested filters.
				List<LetBindingStep> filterOuterSteps = new ArrayList<>();
				Map<NamedConstant, List<LetBindingStep>> innerAccumulators = new HashMap<>(outerAccumulators);
				innerAccumulators.put(paramName, filterOuterSteps);
				List<Step> predicateChain = extractSubChain(filterLambda.getBody(), filterLambda.getKey(),
					innerAccumulators, new HashMap<>(bindings));
				pendingLetBindings.addAll(filterOuterSteps);
				// If the predicate couldn't be fully analysed AND it contains TLStructuredTypePart
				// references, any part change could affect the filter for ALL elements -- we must
				// not silently drop those parts by substituting an empty predicateChain.
				if (predicateChain == null && hasParts(filterLambda.getBody())) {
					return null;
				}

				Lambda l;
				if (hasOuterVariableAccess(filterLambda)) {
					l = null;
				} else {
					l = filterLambda;
				}

				steps.add(new FilterStep(l, predicateChain != null ? predicateChain : Collections.emptyList()));
				current = filter.getBase();
			} else if (current instanceof Foreach foreach) {
				if (!(foreach.getFunction() instanceof Lambda innerLambda)) {
					return null;
				}
				// Register the current paramName so that filters inside the map() body can track
				// references to variables from this scope.
				List<LetBindingStep> foreachOuterSteps = new ArrayList<>();
				Map<NamedConstant, List<LetBindingStep>> foreachAccumulators = new HashMap<>(outerAccumulators);
				foreachAccumulators.put(paramName, foreachOuterSteps);
				List<Step> innerChain = extractSubChain(innerLambda.getBody(), innerLambda.getKey(),
					foreachAccumulators, new HashMap<>(bindings));
				pendingLetBindings.addAll(foreachOuterSteps);
				if (innerChain == null) {
					return null;
				}
				steps.add(new ForeachStep(innerChain));
				current = foreach.getBase();
			} else if (current instanceof UnaryOperation op) {
				current = op.getArgument();
			} else if (current instanceof Call call && call.getFunction() instanceof Lambda letLambda) {
				// Let-binding: TL-Script { varName = call.getArgument(); letLambda.getBody() }.
				// Record the binding so that filter predicates can resolve the variable.
				bindings.put(letLambda.getKey(), call.getArgument());
				// Extract an invertible sub-chain for the binding expression itself. This allows
				// getPathBase to compute precise base objects when any part in the binding changes.
				List<Step> bindingChain = extractSubChain(call.getArgument(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				if (bindingChain != null) {
					pendingLetBindings.add(new LetBindingStep(bindingChain));
				}
				current = letLambda.getBody();
			} else if (current instanceof IfElse ifElse) {
				// Each branch/condition gets an independent snapshot of the current bindings.
				List<Step> condSteps = extractSubChain(ifElse.getCondition(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				List<Step> ifSteps = extractSubChain(ifElse.getIfClause(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				List<Step> elseSteps = extractSubChain(ifElse.getElseClause(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				Collection<TLStructuredTypePart> condExternalParts =
					condSteps == null ? extractPartsAddOverrides(ifElse.getCondition()) : Collections.emptySet();
				Collection<TLStructuredTypePart> ifExternalParts =
					ifSteps == null ? extractPartsAddOverrides(ifElse.getIfClause()) : Collections.emptySet();
				Collection<TLStructuredTypePart> elseExternalParts =
					elseSteps == null ? extractPartsAddOverrides(ifElse.getElseClause()) : Collections.emptySet();
				if (condSteps == null && condExternalParts.isEmpty() && ifSteps == null
					&& ifExternalParts.isEmpty() && elseSteps == null && elseExternalParts.isEmpty()) {
					return null;
				}
				steps.add(new IfElseStep(ifSteps, elseSteps, condSteps, condExternalParts,
					ifExternalParts, elseExternalParts));
				steps.addAll(pendingLetBindings);
				Collections.reverse(steps);
				return steps;
			} else if (current instanceof Union union) {
				// Each branch gets an independent snapshot of the current bindings.
				List<Step> left = extractSubChain(union.getLeft(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				List<Step> right = extractSubChain(union.getRight(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				if (left == null || right == null) {
					return null;
				}
				steps.add(new BranchStep(left, right));
				steps.addAll(pendingLetBindings);
				// Both sub-chains terminate at Var -- the whole spine ends here.
				Collections.reverse(steps); // first applied -> last applied
				return steps;
			} else if (current instanceof Intersection intersection) {
				List<Step> left = extractSubChain(intersection.getLeft(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				List<Step> right = extractSubChain(intersection.getRight(), paramName,
					outerAccumulators, new HashMap<>(bindings));
				if (left == null || right == null) {
					return null;
				}
				steps.add(new BranchStep(left, right));
				steps.addAll(pendingLetBindings);
				Collections.reverse(steps);
				return steps;
			} else if (current instanceof BinaryOperation binaryOp) {
				// Union and Intersection are handled above with strict || semantics because they
				// are
				// navigation operators whose results must fully derive from paramName.
				// All remaining BinaryOperations (And, Or, IsEqual, ContainsElement, ContainsSome,
				// ContainsAll, Compare, CompareOp, StringOperation subclasses, ArithmeticExpr, ...)
				// are predicate or value operators handled uniformly via binaryPredicateStep.
				return binaryPredicateStep(binaryOp.getLeft(), binaryOp.getRight(), paramName,
					outerAccumulators, bindings, steps, pendingLetBindings);
			} else if (current instanceof Var var) {
				if (paramName == var.getKey()) {
					steps.addAll(pendingLetBindings);
					Collections.reverse(steps);
					return steps;
				}
				// Substitute let-binding variable references so the chain can continue through
				// them.
				SearchExpression bound = bindings.get(var.getKey());
				if (bound != null) {
					current = bound;
				} else {
					// Outer-lambda parameter reference: register the current path as a
					// LetBindingStep
					// in the enclosing level's accumulator and return an empty chain as a terminal.
					List<LetBindingStep> outerList = outerAccumulators.get(var.getKey());
					if (outerList != null) {
						List<Step> outerChain = new ArrayList<>(steps);
						outerChain.addAll(pendingLetBindings);
						Collections.reverse(outerChain);
						if (!outerChain.isEmpty()) {
							outerList.add(new LetBindingStep(outerChain));
						}
						return Collections.emptyList();
					}
					return null;
				}
			} else if (current instanceof Literal literal && literal.getValue() instanceof TLObject literalObj) {
				// The walk has reached a constant root (a Literal holding a TLObject). Wrap the
				// accumulated steps in a ConstantBranchStep so the caller treats this constant
				// sub-chain just like a parameter-rooted chain.
				// pendingLetBindings here would be for variables not used by the constant literal;
				// they are discarded because the constant output does not depend on them.
				return Collections.singletonList(new ConstantBranchStep(literalObj, new ArrayList<>(steps)));
			} else if (current instanceof All all) {
				ArrayList<Step> outerSteps = new ArrayList<>(steps);
				Collections.reverse(outerSteps);
				AllStep allStep =
					new AllStep(all.getInstanceType(), outerSteps, new ArrayList<>(pendingLetBindings));
				return Collections.singletonList(allStep);
			} else {
				return null; // unsupported expression node
			}
		}
	}

	/**
	 * Checks whether some part of the {@link Lambda} has access to variables that are not defined
	 * within the Lambda
	 */
	private static boolean hasOuterVariableAccess(Lambda filterLambda) {
		return UsesUndefinedVariable.INSTANCE.check(filterLambda);
	}

	/**
	 * Builds the step for a symmetric binary predicate operator (comparison or boolean combinator)
	 * after both sub-chains have been extracted.
	 *
	 * <ul>
	 * <li>Both non-null: {@link BranchStep}. This includes the case where one side is a
	 * {@link ConstantBranchStep} returned by {@link #extractSubChain} for a constant
	 * {@link Literal}-rooted sub-expression.</li>
	 * <li>Both null: {@code null} (unanalysable).</li>
	 * <li>One null with no {@link TLStructuredTypePart} references: {@link BranchStep} with an
	 * empty side (safe, no external parts to miss).</li>
	 * <li>One null with parts: {@link ExternalPredicateStep} -- precise for the known chain, falls
	 * back to {@link BaseObjects#all()} when any external part changes.</li>
	 * </ul>
	 */
	private static List<Step> binaryPredicateStep(SearchExpression left, SearchExpression right,
			NamedConstant paramName, Map<NamedConstant, List<LetBindingStep>> outerAccumulators,
			Map<NamedConstant, SearchExpression> bindings, List<Step> steps,
			List<LetBindingStep> pendingLetBindings) {
		List<Step> leftSteps = extractSubChain(left, paramName, outerAccumulators, new HashMap<>(bindings));
		List<Step> rightSteps = extractSubChain(right, paramName, outerAccumulators, new HashMap<>(bindings));
		if (leftSteps == null && rightSteps == null) {
			return null;
		}
		Step step;
		Collection<TLStructuredTypePart> leftParts, rightParts;
		if (leftSteps == null && !(leftParts = extractPartsAddOverrides(left)).isEmpty()) {
			step = new ExternalPredicateStep(rightSteps, leftParts);
		} else if (rightSteps == null && !(rightParts = extractPartsAddOverrides(right)).isEmpty()) {
			step = new ExternalPredicateStep(leftSteps, rightParts);
		} else {
			step = new BranchStep(
				leftSteps != null ? leftSteps : Collections.emptyList(),
				rightSteps != null ? rightSteps : Collections.emptyList());
		}
		steps.add(step);
		steps.addAll(pendingLetBindings);
		Collections.reverse(steps);
		return steps;
	}

	/** Whether any step in {@code chain} uses {@code part}. */
	private static boolean chainUsesPart(List<Step> chain, TLStructuredTypePart part) {
		for (Step step : chain) {
			if (step.usesPart(part)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether {@code partInExpr} (as stored in the step) matches {@code changedPart} (as reported
	 * by the access manager).
	 *
	 * <p>
	 * A match occurs when {@code changedPart} is {@code partInExpr} itself or is a non-derived
	 * override of it. The check uses
	 * {@link TLModelOperations#getOverrides(TLStructuredTypePart)}, which returns all parts with
	 * the same name whose owner is a specialisation of {@code partInExpr}'s owner.
	 * </p>
	 *
	 * <p>
	 * Example: if the expression stores {@code A:c} and the access manager reports a change to
	 * {@code B:c} (where {@code B} extends {@code A} and overrides {@code c}), this returns
	 * {@code true}. But if {@code V:c} (where {@code V} is an unrelated subtype that does not
	 * extend {@code A}) changes, this returns {@code false}, even though both {@code A:c} and
	 * {@code V:c} may share the same root definition.
	 * </p>
	 */
	private static boolean matchesPart(TLStructuredTypePart partInExpr, TLStructuredTypePart changedPart) {
		if (changedPart == partInExpr) {
			return true;
		}
		return modelCacheOperations().getOverrides(partInExpr)
			.stream()
			.filter(notDerived())
			.anyMatch(Predicate.isEqual(changedPart));
	}

	private static TLModelOperations modelCacheOperations() {
		return TLModelCacheService.getOperations();
	}

	private static Predicate<TLStructuredTypePart> notDerived() {
		return Predicate.not(TLStructuredTypePart::isDerived);
	}

	/**
	 * Applies the inverse of all steps in {@code chain} (last to first) to {@code obj}.
	 *
	 * @return the result set, or {@code null} if any step is not invertible
	 */
	private static Collection<? extends TLObject> applyChainInverse(List<Step> chain, TLObject obj) {
		return applyChainInverse(chain, chain.size() - 1, Collections.singleton(obj));
	}

	/**
	 * Applies the inverse of steps in {@code chain} (from {@code from} to first) to {@code objs}.
	 *
	 * @return the result set, or {@code null} if any step is not invertible
	 */
	private static Collection<? extends TLObject> applyChainInverse(List<Step> chain, int from,
			Collection<? extends TLObject> objs) {
		for (int i = from; i >= 0; i--) {
			objs = chain.get(i).applyInverse(objs);
			if (objs == null) {
				return null;
			}
		}
		return objs;
	}

	/**
	 * Finds all steps in {@code chain} that use {@code part}, computes their pivots, navigates
	 * backwards through the preceding steps, and returns the union of all results.
	 *
	 * <p>
	 * Multiple steps may use the same part (e.g. when the part appears in several filter
	 * predicates). All matching steps contribute to the result.
	 * </p>
	 *
	 * @return the union of path-base objects reached via this chain, an empty set if {@code part}
	 *         is not used in this chain, or {@code null} if the computation cannot be completed
	 */
	private static Collection<? extends TLObject> chainPivot(List<Step> chain, TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		Set<TLObject> result = null;
		for (int k = 0; k < chain.size(); k++) {
			Step step = chain.get(k);
			if (!step.usesPart(part)) {
				continue;
			}
			Collection<? extends TLObject> pivot = step.pivot(element, part, partValue);
			if (pivot == null) {
				return null;
			}
			pivot = applyChainInverse(chain, k - 1, pivot);
			if (pivot == null) {
				return null;
			}
			if (result == null) {
				result = new HashSet<>(pivot);
			} else {
				result.addAll(pivot);
			}
		}
		return result == null ? Collections.emptySet() : result;
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		return _relevantParts;
	}

	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return asObjects(_expression.execute(base));
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		if (_chain == null) {
			return BaseObjects.all();
		}
		// Apply inverse steps from last applied to first applied.
		Collection<? extends TLObject> current = applyChainInverse(_chain, destination);
		if (current == null) {
			return BaseObjects.all();
		}
		return BaseObjects.of(current);
	}

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		if (_chain == null) {
			return BaseObjects.all();
		}
		// Collect results from ALL steps that use the part (the same part may appear in multiple
		// filter predicates, each contributing different base objects).
		Set<TLObject> allBases = null;
		for (int k = 0; k < _chain.size(); k++) {
			Step step = _chain.get(k);
			if (!step.usesPart(part)) {
				continue;
			}
			Collection<? extends TLObject> pivot = step.pivot(element, part, partValue);
			if (pivot == null) {
				return BaseObjects.all();
			}
			// Navigate backwards through all preceding steps to reach the chain input.
			pivot = applyChainInverse(_chain, k - 1, pivot);
			if (pivot == null) {
				return BaseObjects.all();
			}
			if (allBases == null) {
				allBases = new HashSet<>(pivot);
			} else {
				allBases.addAll(pivot);
			}
		}
		if (allBases == null) {
			// Part not reachable from the chain input via any invertible path.
			return BaseObjects.all();
		}
		return BaseObjects.of(allBases);
	}

	/**
	 * Converts an arbitrary value returned by {@link TLObject#tValue(TLStructuredTypePart)} to a
	 * collection of {@link TLObject}s.
	 */
	@SuppressWarnings("unchecked")
	private static Collection<? extends TLObject> asObjects(Object value) {
		if (value == null) {
			return Collections.emptySet();
		} else if (value instanceof Collection<?> c) {
			return (Collection<? extends TLObject>) c;
		} else if (value instanceof TLObject obj) {
			return Collections.singleton(obj);
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public void appendId(Appendable out) throws IOException {
		StringBuilder builder = searchAsString();

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] input = builder.toString().getBytes(StandardCharsets.UTF_8);
			String output = Base64.getEncoder().encodeToString(digest.digest(input));
			out.append("exprHash:");
			out.append(output);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}

	}

	private StringBuilder searchAsString() {
		SearchExpression search = _expression.getSearch();
		StringBuilder builder = new StringBuilder();
		search.visit(ToString.INSTANCE, builder);
		return builder;
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		out.append("expr: ");
		out.append(TagUtil.encodeXML(searchAsString().toString()));
	}

}
