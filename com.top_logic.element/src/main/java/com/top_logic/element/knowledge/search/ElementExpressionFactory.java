/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mappings;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.core.TLElementVisitor;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Factory for {@link Expression}s related to element functionality.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementExpressionFactory {

	/**
	 * All associations used for meta attributes.
	 */
	public static final SetExpression anyAttributeAssociation() {
		return anyOf(WrapperMetaAttributeUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
	}

	/**
	 * All associations used for meta attributes.
	 * 
	 * @deprecated All associations storing in the same table as the given {@link TLStructuredTypePart}.
	 *             Use {@link #allAttributeAssociations(TLStructuredTypePart)} to find the associations for
	 *             the given {@link TLStructuredTypePart}.
	 */
	@Deprecated
	public static final SetExpression anyAttributeAssociation(TLStructuredTypePart ma) {
		return allOf(WrapperMetaAttributeUtil.getAssociationName(ma));
	}

	/**
	 * Expression that finds all links that holds the values for the given {@link TLStructuredTypePart}.
	 */
	public static final SetExpression allAttributeAssociations(TLStructuredTypePart ma) {
		return filter(allOf(WrapperMetaAttributeUtil.getAssociationName(ma)), holdsValuesOf(ma));
	}

	/**
	 * Expression that matches links that store values for the given {@link TLStructuredTypePart}.
	 */
	public static final Expression holdsValuesOf(TLStructuredTypePart ma) {
		TLStructuredTypePart definition = WrapperMetaAttributeUtil.getDefinition(ma);
		return eqBinary(
			reference(WrapperMetaAttributeUtil.getAssociationName(definition),
				WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR),
			literal(definition.tHandle()));
	}

	/**
	 * Returns a flat representation of the structure starting at the specified node.
	 * 
	 * @param node
	 *        the {@link StructuredElement} to start at
	 * @param includeNode
	 *        {@code true} to include the given node, {@code false} to consider only its
	 *        substructure
	 * @return a (possibly empty) {@link List} sorted by the natural order of elements in the
	 *         structure
	 */
	public static final List<StructuredElement> flat(StructuredElement node, boolean includeNode) {
		final List<StructuredElement> elements = new ArrayList<>();
	
		TraversalFactory.traverse(node, new TLElementVisitor() {
	
			@Override
			public boolean onVisit(final StructuredElement element, final int depth) {
				if (element instanceof StructuredElement) {
					elements.add((StructuredElement) element);
				}
				return true;
			}
		}, TraversalFactory.DEPTH_FIRST, !includeNode);
	
		return elements;
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param targetCondition
	 *        The {@link Expression} to be used for evaluation.
	 * @return The {@link Expression} evaluating if a {@code source} has outgoing references
	 *         matching the given condition.
	 */
	public static final Expression hasReferenceTo(TLStructuredTypePart attribute, Expression targetCondition) {
		return inSet(
			context(),
			map(
				filter(
					allAttributeAssociations(attribute),
					targetCondition),
				source()));
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param targets
	 *        A {@link Collection} of referenced {@link Wrapper}s.
	 * @return The {@link Expression} evaluating if a {@code source} references one of the given
	 *         {@code targets} using the given {@code attribute}.
	 */
	public static final Expression hasReferenceTo(TLStructuredTypePart attribute, Collection<? extends Wrapper> targets) {
		switch (targets.size()) {
			case 0:
				return literal(Boolean.FALSE);
			case 1:
				return hasReferenceTo(attribute, targets.iterator().next());
			default:
				return hasReferenceTo(
					attribute,
					inLiteralSet(
						destination(),
						toKO(targets)));
		}
	}

	private static List<KnowledgeObject> toKO(Collection<? extends Wrapper> targets) {
		return Mappings.map(Wrapper.KO_MAPPING, targets);
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param target
	 *        The {@link StructuredElement} defining the referenced structure's root.
	 * @param includeRoot
	 *        {@code true} to include the given node as well, {@code false} to consider only the
	 *        substructure.
	 * @return The {@link Expression} evaluating if a {@code source} references the given
	 *         {@code target structure} using the given {@code attribute}.
	 */
	public static final Expression hasReferenceTo(TLStructuredTypePart attribute, final StructuredElement target,
			boolean includeRoot) {
		return hasReferenceTo(
			attribute,
			flat(target, includeRoot));
	}

	/**
	 * @param attribute
	 *        the wrapper valued {@link TLStructuredTypePart} defining the reference to be checked
	 * @param target
	 *        the referenced {@link Wrapper}
	 * @return the {@link Expression} evaluating if a {@code source} references the given
	 *         {@code target} using the given {@code attribute}
	 */
	public static final Expression hasReferenceTo(TLStructuredTypePart attribute, Wrapper target) {
		return hasReferenceTo(
			attribute,
			eqBinary(
				destination(),
				literal(target.tHandle())));
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param targetMOType
	 *        The referenced object's {@link MetaObject} name.
	 * @return The {@link Expression} evaluating if a {@code source} references a {@code target} of
	 *         the given type using the given {@code attribute}.
	 */
	public static final Expression hasReferenceToType(TLStructuredTypePart attribute, String targetMOType) {
		return hasReferenceTo(
			attribute,
			eval(
				destination(),
				instanceOf(targetMOType)));
	}

	/**
	 * @param values
	 *        A {@link Collection} of {@link Wrapper}s to be converted to identifiers.
	 * @return A {@link Set} of {@link KnowledgeObject#getObjectName() version independent
	 *         identifier}.
	 */
	public static final Set<TLID> identifiers(Collection<? extends Wrapper> values) {
		Set<TLID> ids = new HashSet<>(values.size());
	
		for (Wrapper wrapper : values) {
			ids.add(KBUtils.getWrappedObjectName(wrapper));
		}
	
		return ids;
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param sourceCondition
	 *        The {@link Expression} to be used for evaluation.
	 * @return The {@link Expression} evaluating if a {@code target} has incoming references
	 *         matching the given condition.
	 */
	public static final Expression isReferenced(TLStructuredTypePart attribute, Expression sourceCondition) {
		return inSet(
			context(),
			map(
				filter(
					allAttributeAssociations(attribute),
					sourceCondition),
				destination()));
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param sources
	 *        A {@link Collection} of referencing {@link Wrapper}s.
	 * @return The {@link Expression} evaluating if a {@code target} is referenced by one of the
	 *         given {@code sources} using the given {@code attribute}.
	 */
	public static final Expression isReferencedBy(TLStructuredTypePart attribute, Collection<? extends Wrapper> sources) {
		switch (sources.size()) {
			case 0:
				return literal(Boolean.FALSE);
			case 1:
				return isReferencedBy(attribute, sources.iterator().next());
			default:
				return isReferenced(
					attribute,
					inLiteralSet(
						source(),
						toKO(sources)));
		}
	}

	/**
	 * @param attribute
	 *        The wrapper valued {@link TLStructuredTypePart} defining the reference to be checked.
	 * @param source
	 *        The {@link StructuredElement} defining the referencing structure's root.
	 * @param includeRoot
	 *        {@code true} to include the given node as well, {@code false} to consider only the
	 *        substructure.
	 * @return The {@link Expression} evaluating if a {@code target} is referenced by the given
	 *         {@code source structure} using the given {@code attribute}.
	 */
	public static final Expression isReferencedBy(TLStructuredTypePart attribute, StructuredElement source,
			boolean includeRoot) {
		return isReferencedBy(
			attribute,
			flat(source, includeRoot));
	}

	/**
	 * @param attribute
	 *        the wrapper valued {@link TLStructuredTypePart} defining the reference to be checked
	 * @param source
	 *        the referencing {@link Wrapper}
	 * @return the {@link Expression} evaluating if a {@code target} is referenced by the given
	 *         {@code source} using the given {@code attribute}
	 */
	public static final Expression isReferencedBy(TLStructuredTypePart attribute, Wrapper source) {
		return isReferenced(
			attribute,
			eqBinary(
				source(),
				literal(source.tHandle())));
	}

	/**
	 * @param attribute
	 *        the wrapper valued {@link TLStructuredTypePart} defining the reference to be checked
	 * @param sourceMOType
	 *        the referencing {@link Wrapper}s' {@link MetaObject} name
	 * @return the {@link Expression} evaluating if a {@code target} is referenced by
	 *         {@code sources} of the given type using the given {@code attribute}
	 */
	public static final Expression isReferencedByType(TLStructuredTypePart attribute, String sourceMOType) {
		return isReferenced(
			attribute,
			eval(
				source(),
				instanceOf(sourceMOType)));
	}

	/**
	 * @param meType
	 *        the global {@link TLClass}'s name
	 * @param attribute
	 *        the {@link TLStructuredTypePart} name to lookup
	 * @return the {@link TLStructuredTypePart} with the given name defined in the global
	 *         {@link TLClass} with the given name or one of its super-types
	 * @throws IllegalArgumentException
	 *         if an attribute with the given name was not defined
	 */
	public static final TLStructuredTypePart metaAttribute(String meType, String attribute)
			throws IllegalArgumentException {
		try {
			return MetaElementUtil.getMetaAttribute(metaElement(meType), attribute);
		} catch (NoSuchAttributeException e) {
			throw new IllegalArgumentException("Attribute not found: 'me:" + meType + "." + attribute);
		}
	}

	/**
	 * @param meType
	 *        the global {@link TLClass}'s name
	 * @return the global {@link TLClass} with the given name
	 */
	public static final TLClass metaElement(final String meType) {
		return MetaElementFactory.getInstance().getGlobalMetaElement(meType);
	}

}
