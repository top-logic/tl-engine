/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;

/**
 * Parameterizable variant of {@link TransformIterator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TransformIterators {

	/**
	 * Creates a {@link TransformIterators}.
	 * 
	 * @param source
	 *        The source of elements.
	 * @param sourceFilter
	 *        The {@link Filter} that must match elements taken from the source
	 *        {@link Iterator}
	 * @param transformation
	 *        The {@link Mapping} to apply to the source elements.
	 * @param destinationFilter
	 *        The {@link Filter} that must match the results of the
	 *        transformation.
	 */
	public static <S, D> Iterator<D> transform(
		Iterator<? extends S> source, 
		final Filter<? super S> sourceFilter,
		final Mapping<? super S, ? extends D> transformation,
		final Filter<? super D> destinationFilter
	) {
		return new TransformIterator<S, D>(source) {
			@Override
			protected boolean test(S value) {
				return sourceFilter.accept(value);
			}
			
			@Override
			protected D transform(S value) {
				return transformation.map(value);
			}
			
			@Override
			protected boolean acceptDestination(D value) {
				return destinationFilter.accept(value);
			}
		};
	}
	
	/**
	 * Creates a {@link TransformIterators}.
	 * 
	 * @param source
	 *        The source of elements.
	 * @param transformation
	 *        The {@link Mapping} to apply to the source elements.
	 * @param destinationFilter
	 *        The {@link Filter} that must match the results of the
	 *        transformation.
	 */
	public static <S, D> Iterator<D> transform(
		Iterator<? extends S> source, 
		final Mapping<? super S, ? extends D> transformation,
		final Filter<? super D> destinationFilter
	) {
		return new TransformIterator<S, D>(source) {
			@Override
			protected boolean test(S value) {
				return true;
			}
			
			@Override
			protected D transform(S value) {
				return transformation.map(value);
			}
			
			@Override
			protected boolean acceptDestination(D value) {
				return destinationFilter.accept(value);
			}
		};
	}

	/**
	 * Creates a {@link TransformIterators}.
	 * 
	 * @param source
	 *        The source of elements.
	 * @param sourceFilter
	 *        The {@link Filter} that must match elements taken from the source
	 *        {@link Iterator}
	 * @param transformation
	 *        The {@link Mapping} to apply to the source elements.
	 */
	public static <S, D> Iterator<D> transform(
		Iterator<? extends S> source, final Filter<? super S> sourceFilter,
		final Mapping<? super S, ? extends D> transformation
	) {
		return new TransformIterator<S, D>(source) {
			@Override
			protected boolean test(S value) {
				return sourceFilter.accept(value);
			}
			
			@Override
			protected D transform(S value) {
				return transformation.map(value);
			}
			
			@Override
			protected boolean acceptDestination(D value) {
				return true;
			}
		};
	}

	/**
	 * Creates a {@link TransformIterators}.
	 * 
	 * @param source
	 *        The source of elements.
	 * @param transformation
	 *        The {@link Mapping} to apply to the source elements.
	 */
	public static <S, D> Iterator<D> transform(
			Iterator<? extends S> source,
			final Mapping<? super S, ? extends D> transformation
	) {
		return new MappingIterator<S, D>(transformation, source);
	}
	
	/**
	 * Creates a {@link TransformIterators}.
	 * 
	 * @param source
	 *        The source of elements.
	 * @param Filter
	 *        The {@link Filter} that must match elements taken from the source
	 *        {@link Iterator}
	 */
	public static <S> Iterator<S> transform(
			Iterator<? extends S> source, final Filter<? super S> Filter
	) {
		return new TransformIterator<S, S>(source) {
			@Override
			protected boolean test(S value) {
				return Filter.accept(value);
			}
			
			@Override
			protected S transform(S value) {
				return value;
			}
			
			@Override
			protected boolean acceptDestination(S value) {
				return true;
			}
		};
	}
	
}
