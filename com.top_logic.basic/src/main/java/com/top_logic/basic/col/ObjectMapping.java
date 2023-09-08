/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;




/**
 * Wrapper for associating an object with a substitution value.
 * 
 * <p>
 * Used for efficient sorting according to a {@link #getSubstitution()} value.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectMapping<S, D> {
	
	/**
	 * Mapping that unwraps objects wrapped with {@link ObjectMapping} instances.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class UnwrapMapping<S> implements Mapping<ObjectMapping<S, ?>, S> {
		
		/**
		 * Singleton {@link ObjectMapping.UnwrapMapping} instance.
		 */
		public static final UnwrapMapping<Object> INSTANCE = new UnwrapMapping<>();

		private UnwrapMapping() {
			// Singleton constructor.
		}
		
		@Override
		public S map(ObjectMapping<S, ?> input) {
			return input.getObject();
		}
		
		/**
		 * Type preserving getter for {@link #INSTANCE}.
		 * 
		 * @param <S> The source type of the {@link ObjectMapping}.
		 * @return A typed version of the {@link #INSTANCE}.
		 */
		@SuppressWarnings("unchecked") // Type safety is guaranteed by implementation.
		public static <S> Mapping<ObjectMapping<? extends S, ?>, S> unwrapMapping() {
			return (Mapping) INSTANCE;
		}
	}

	/**
	 * {@link Comparator} that compares {@link ObjectMapping}s according to their
	 * {@link ObjectMapping#getSubstitution()} using a {@link Comparator} delegate. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SubstitutionComparator<S, D> implements Comparator<ObjectMapping<S, D>> {
		
		private final Comparator<? super D> valueComparator;
		private final Comparator<? super S> globalOrder;

		/**
		 * Same as
		 * {@link ObjectMapping.SubstitutionComparator#SubstitutionComparator(Comparator, Comparator)}
		 * without global order.
		 */
		public SubstitutionComparator(Comparator<? super D> valueComparator) {
			this(valueComparator, Equality.INSTANCE);
		}

		/**
		 * Creates a new {@link ObjectMapping.SubstitutionComparator} that
		 * compares {@link ObjectMapping} values according to their
		 * {@link ObjectMapping#getSubstitution() substitution values}
		 * delegating to the given comparator.
		 * 
		 * @param valueComparator
		 *        {@link Comparator} that compares substitution values.
		 * @param globalOrder
		 *        {@link Comparator} to use, if the value comparator is not able
		 *        to distinguish objects.
		 */
		public SubstitutionComparator(Comparator<? super D> valueComparator, Comparator<? super S> globalOrder) {
			this.valueComparator = valueComparator;
			this.globalOrder = globalOrder;
		}
		
		@Override
		public int compare(ObjectMapping<S, D> o1, ObjectMapping<S, D> o2) {
			int valueComparison = valueComparator.compare(o1.getSubstitution(), o2.getSubstitution());
			if (valueComparison != 0) {
				return valueComparison;
			}
			
			return globalOrder.compare(o1.getObject(), o2.getObject());
		}
	}

	/**
	 * {@link Mapping} that produces an {@link ObjectMapping} for an object by
	 * delegating to another {@link Mapping} instance.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class WrapMapping<S, D> implements Mapping<S, ObjectMapping<S, D>> {
		private final Mapping<? super S, ? extends D> mappingDelegate;

		/**
		 * Constructs a {@link ObjectMapping.WrapMapping} that maps objects to
		 * {@link ObjectMapping} wrappers with
		 * {@link ObjectMapping#getSubstitution() substitution} values provided
		 * by the given {@link Mapping}
		 */
		public WrapMapping(Mapping<? super S, ? extends D> mappingDelegate) {
			this.mappingDelegate = mappingDelegate;
		}

		@Override
		public ObjectMapping<S, D> map(S input) {
			return new ObjectMapping<>(input, mappingDelegate.map(input));
		}
	}
	
	/**
	 * Mapping that unwraps {@link ObjectMapping} wrappers back to their
	 * {@link ObjectMapping#getObject() original objects}.
	 */
	public static final Mapping<ObjectMapping<Object, ?>, Object> UNWRAP_MAPPING = UnwrapMapping.INSTANCE;
	
	
	/**
	 * @see #getObject()
	 */
	private S obj;
	
	/**
	 * {@link #getSubstitution()}
	 */
	private D substitution;
	
	/**
	 * Creates a new {@link ObjectMapping} that associates the given
	 * substitution value with the given object.
	 */
	public ObjectMapping(S obj, D substitution) {
		this.obj = obj;
		this.substitution = substitution;
	}

	/**
	 * The substitution value of {@link #getObject() the object}.
	 */
	public D getSubstitution() {
		return substitution;
	}
	
	/**
	 * The underlying object.
	 */
	public S getObject() {
		return obj;
	}
}

