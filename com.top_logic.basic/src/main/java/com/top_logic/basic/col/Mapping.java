/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.function.Function;

/**
 * A {@link Mapping} is a function from a source domain <code>S</code> to a destination domain
 * <code>D</code>.
 *
 * One use-case of a {@link Mapping} is the {@link MappingIterator}, which translates the objects
 * returned by a source {@link java.util.Iterator} into result objects from the destination domain.
 * The actual translation is done by a {@link Mapping} object.
 * 
 * @param <S>
 *        The source/argument type
 * @param <D>
 *        The destination/result type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FunctionalInterface
public interface Mapping<S, D> extends Function<S, D> {

    /**
     * Generic function that translates input objects into output objects.
     *
     * @see MappingIterator
     *
     * @param input
     *     The source object, which is the input of the function.
     * @return the output object, which is the result of the function.
     */
	D map(S input);

	@Override
	default D apply(S input) {
		return map(input);
	}

}
