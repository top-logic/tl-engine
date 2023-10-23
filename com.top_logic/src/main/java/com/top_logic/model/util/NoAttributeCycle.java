/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link ConstraintCheck} that checks that the reference does not contain a cycle.
 * 
 * <p>
 * This check ensures that there is no sequence of objects "a_0, a_1,... ,a_n, a_0", so that a_i+1
 * is in the reference of a_i and a_0 is in the reference of a_n.
 * </p>
 */
@InApp
@Label("No cycle")
public class NoAttributeCycle<C extends NoAttributeCycle.Config<?>> extends AbstractConfiguredInstance<C>
		implements ConstraintCheck {

	/**
	 * Typed configuration interface definition for {@link NoAttributeCycle}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends NoAttributeCycle<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the configuration option {@link #getAdditionalObservedReferences()}.
		 */
		String REFERENCES = "additional-observed-references";

		/**
		 * Additional references that are used to check for a cycle.
		 * 
		 * <p>
		 * It is important that at each additional reference an corresponding constraint of type
		 * {@link NoAttributeCycle} is configured with this reference as
		 * {@link #getAdditionalObservedReferences()}. This is the only way to ensure that the
		 * constraint works correctly.
		 * </p>
		 */
		@Name(REFERENCES)
		List<TLReferenceConfig> getAdditionalObservedReferences();

		/**
		 * @see #getAdditionalObservedReferences()
		 */
		void setAdditionalObservedReferences(List<TLReferenceConfig> value);

	}

	private List<TLReference> _additionalReferences;

	/**
	 * Create a {@link NoAttributeCycle}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public NoAttributeCycle(InstantiationContext context, C config) {
		super(context, config);
		_additionalReferences = config.getAdditionalObservedReferences()
			.stream()
			.map(TLReferenceConfig::resolve)
			.collect(Collectors.toList());
	}

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Map<TLObject, Collection<AttributeValue>> valueCache = new HashMap<>();

		List<List<AttributeValue>> pathsToProcess = new ArrayList<>();
		pathsToProcess.add(new ArrayList<>(Arrays.asList(new AttributeValue(null, object))));
		boolean initial = true;
		while (!pathsToProcess.isEmpty()) {
			List<AttributeValue> path = pathsToProcess.remove(pathsToProcess.size() - 1);
			TLObject tail = path.get(path.size() - 1).value();
			Collection<AttributeValue> collection = valueCache.get(tail);
			if (collection == null) {
				collection = determineValues(tail, attribute, initial);
				initial = false;
				valueCache.put(tail, collection);
			}
			if (collection.isEmpty()) {
				// No cycle.
				continue;
			}
			Iterator<AttributeValue> iterator = collection.iterator();
			boolean hasNext;
			do {
				List<AttributeValue> extendedPath = path;
				AttributeValue value = iterator.next();
				hasNext = iterator.hasNext();
				if (hasNext) {
					extendedPath = new ArrayList<>(path);
				}
				extendedPath.add(value);
				for (int i = extendedPath.size() - 2; i >= 0; i--) {
					if (extendedPath.get(i).equals(value)) {
						return foundCycle(object, attribute, extendedPath);
					}
				}
				pathsToProcess.add(extendedPath);
			} while (hasNext);
		}
		return null;
	}

	private ResKey foundCycle(TLObject object, TLStructuredTypePart attribute, List<AttributeValue> valuePath) {
		Object path;
		if (_additionalReferences.isEmpty()) {
			path = valuePath
				.stream()
				.map(AttributeValue::value)
				.map(MetaLabelProvider.INSTANCE::getLabel)
				.collect(Collectors.joining(" -> "));
		} else {
			StringBuilder b = new StringBuilder(MetaLabelProvider.INSTANCE.getLabel(valuePath.get(0).value()));
			for (int i = 1; i < valuePath.size(); i++) {
				AttributeValue entry = valuePath.get(i);
				String attr = MetaLabelProvider.INSTANCE.getLabel(entry.attribute());
				String value = MetaLabelProvider.INSTANCE.getLabel(entry.value());
				b.append(" (").append(attr).append(")-> ").append(value);
			}
			path = b.toString();
		}
		return I18NConstants.ERROR_CYLCE_NOT_ALLOWED__OBJECT_ATTRIBUTE_VALUE_CYCLE.fill(object, attribute,
			object.tValue(attribute), path);
	}

	/**
	 * Determines the values of the given item for the given attribute and all appropriate
	 * {@link #_additionalReferences additional attributes}.
	 *
	 * @param item
	 *        The item to get values from.
	 * @param attribute
	 *        the original {@link TLStructuredTypePart} at which this constraint is annotated.
	 * @param initial
	 *        Whether it is the first step in the attribute chain, i.e. the given item is the item
	 *        given in {@link #check(TLObject, TLStructuredTypePart)} or
	 *        {@link #traceDependencies(TLObject, TLStructuredTypePart, Sink)}. In this case only
	 *        the given attribute is used, additional references are <b>not</b> navigated.
	 */
	private Set<AttributeValue> determineValues(TLObject item, TLStructuredTypePart attribute, boolean initial) {
		Set<AttributeValue> result = Collections.emptySet();

		if (TLModelUtil.isCompatibleInstance(attribute.getOwner(), item)) {
			Set<AttributeValue> attributeValue = attributeValue(item, attribute);
			result = add(result, attributeValue);
		}
		if (!initial) {
			for (TLReference additional : _additionalReferences) {
				if (TLModelUtil.isCompatibleInstance(additional.getOwner(), item)) {
					Set<AttributeValue> attributeValue = attributeValue(item, additional);
					result = add(result, attributeValue);
				}
			}
		}

		return result;
	}

	private Set<AttributeValue> add(Set<AttributeValue> result, Set<AttributeValue> attributeValue) {
		if (result.isEmpty()) {
			return attributeValue;
		}
		if (attributeValue.isEmpty()) {
			return result;
		}
		if (result.size() == 1) {
			result = new HashSet<>(result);
		}
		result.addAll(attributeValue);
		return result;
	}

	private static Set<AttributeValue> attributeValue(TLObject item, TLStructuredTypePart attribute) {
		Object value = item.tValue(attribute);
		if (value instanceof TLObject) {
			return Collections.singleton(new AttributeValue(attribute, (TLObject) value));
		}
		if (value == null) {
			return Collections.emptySet();
		}
		if (value instanceof Collection<?>) {
			return ((Collection<?>) value)
				.stream()
				.filter(TLObject.class::isInstance)
				.map(TLObject.class::cast)
				.map(v -> new AttributeValue(attribute, v))
				.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace) {
		Set<TLObject> seen = new HashSet<>();
		List<TLObject> remaining = new ArrayList<>();
		remaining.add(object);
		boolean initial = true;
		while (!remaining.isEmpty()) {
			TLObject item = remaining.remove(remaining.size() - 1);
			if (!seen.add(item)) {
				continue;
			}
			Set<AttributeValue> values = determineValues(item, attribute, initial);
			initial = false;
			remaining.addAll(values.stream().map(AttributeValue::value).collect(Collectors.toList()));
			values.stream()
				.map(AttributeValue::attribute)
				.distinct()
				.filter(attr -> {
					if (!attr.equals(attribute)) {
						return true;
					} else {
						return !item.equals(object);
					}
				})
				.map(attr -> Pointer.create(item, attr))
				.forEach(trace::add);
		}

	}

	private static final class AttributeValue {

		final TLStructuredTypePart _attribute;

		final TLObject _value;

		public AttributeValue(TLStructuredTypePart attribute, TLObject value) {
			_attribute = attribute;
			_value = Objects.requireNonNull(value);
		}

		public TLStructuredTypePart attribute() {
			return _attribute;
		}

		public TLObject value() {
			return _value;
		}

		@Override
		public int hashCode() {
			return value().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AttributeValue other = (AttributeValue) obj;
			return value().equals(other.value());
		}

		@Override
		public String toString() {
			return attribute() + " -> " + value();
		}

	}

}
