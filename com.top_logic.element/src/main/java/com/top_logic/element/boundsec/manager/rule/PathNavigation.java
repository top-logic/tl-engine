/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.I18NConstants;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations;
import com.top_logic.model.util.TLModelUtil;

/**
 * One node in a role rule path that navigates a {@link TLReference}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class PathNavigation extends AbstractConfiguredInstance<PathElementConfig> implements PathElement {

    /** the meta attribute defining the content */
	private final TLReference _reference;

	private Set<TLStructuredTypePart> _relevantParts;
    
	/**
	 * Create a {@link PathNavigation}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PathNavigation(InstantiationContext context, PathElementConfig config) {
		super(context, config);

		TLModelPart part = config.getAttribute().resolve();
		if (!(part instanceof TLReference reference)) {
			throw new ConfigurationError(I18NConstants.NOT_A_REFERENCE__PART.fill(part));
		}
		List<TLStructuredTypePart> untrackable = untrackableParts(reference);
		if (!untrackable.isEmpty()) {
			context.error("Path-navigation references attribute '"
					+ TLModelUtil.qualifiedName(part)
					+ "' that is derived (computed) in "
					+ untrackable.stream().map(TLModelUtil::qualifiedName).collect(Collectors.joining(", "))
					+ ". Derived attributes do not fire change notifications and cannot be tracked"
					+ " for role-rule invalidation.");
		}

		_relevantParts = Stream.concat(
			concreteParts(reference).filter(Predicate.not(TLStructuredTypePart::isDerived)),
			Stream.of(reference))
			.collect(Collectors.toSet());
		_reference = reference;
	}

	/**
	 * Whether a {@link PathNavigation} can navigate the given part.
	 *
	 * <p>
	 * A role rule navigating a part must be invalidated when the value of the part changes. This is
	 * not possible for a derived (computed) part, because it does not fire change notifications.
	 * An abstract part has no values of its own: Its values are those of its concrete overrides.
	 * Therefore, the part is navigable, if neither the part itself nor any of its overrides is a
	 * concrete derived part.
	 * </p>
	 *
	 * @see #untrackableParts(TLStructuredTypePart)
	 */
	public static boolean isNavigable(TLStructuredTypePart part) {
		return untrackableParts(part).isEmpty();
	}

	/**
	 * The parts holding values of the given part that do not fire change notifications.
	 *
	 * @return The given part and its overrides that are derived (computed) but not abstract.
	 */
	public static List<TLStructuredTypePart> untrackableParts(TLStructuredTypePart part) {
		return concreteParts(part)
			.filter(TLStructuredTypePart::isDerived)
			.collect(Collectors.toList());
	}

	/**
	 * The given part and all its overrides that are not abstract, i.e. that hold values.
	 */
	private static Stream<TLStructuredTypePart> concreteParts(TLStructuredTypePart part) {
		TLModelOperations operations = TLModelCacheService.getOperations();
		return Stream.concat(Stream.of(part), operations.getOverrides(part).stream())
			.filter(Predicate.not(TLStructuredTypePart::isAbstract));
	}

	@Override
	public Collection<TLStructuredTypePart> getRelevantParts() {
		return _relevantParts;
	}
    
	/**
	 * The {@link TLReference} this step navigates.
	 *
	 * @see #isInverse()
	 */
	public TLReference getReference() {
		return _reference;
	}

	/**
	 * Whether {@link #getReference()} is navigated backwards.
	 *
	 * <p>
	 * A forward step reaches the values of the reference, a backwards step the objects referring to
	 * the base object through it. The type reached by a step is therefore the reference's target
	 * type in forward direction and its owner type in backwards direction.
	 * </p>
	 *
	 * @see PathElementConfig#isInverse()
	 */
	public boolean isInverse() {
		return getConfig().isInverse();
    }
    
	@Override
	public Collection<? extends TLObject> getValues(TLObject base) {
		return getValues(base, true);
    }

	private Collection<? extends TLObject> getValues(TLObject base, boolean isForward) {
		Collection<? extends TLObject> result;
        
		if (isInverse() == isForward) {
			result = base.tReferers(_reference);
		} else {
			Object value = base.tValue(_reference);
			if (value instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<? extends TLObject> cast = (Collection<? extends TLObject>) value;
				result = cast;
			} else if (value != null) {
				result = Collections.singleton((TLObject) value);
			} else {
				result = Collections.emptySet();
			}
        }
		return result != null ? result : Collections.emptySet();
    }
    
	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getSources(TLObject destination) {
		return BaseObjects.of(getValues(destination, false));
    }

	@Override
	public BaseObjects<? extends Collection<? extends TLObject>> getPathBase(TLObject element,
			TLStructuredTypePart part, Supplier<?> partValue) {
		Collection<? extends TLObject> baseElements;
		if (isInverse()) {
			@SuppressWarnings("unchecked")
			Collection<? extends TLObject> refValue =
				(Collection<? extends TLObject>) CollectionUtil.asCollection(partValue.get());
			baseElements = refValue;
		} else {
			baseElements = Collections.singleton(element);
		}
		return BaseObjects.of(baseElements);
	}

	@Override
	public void appendForTooltip(Appendable out) throws IOException {
		out.append("MA: ");
		out.append(TagUtil.encodeXML(TLModelUtil.qualifiedName(_reference)));
		out.append("; Inverse: ");
		out.append(String.valueOf(isInverse()));
	}
}
