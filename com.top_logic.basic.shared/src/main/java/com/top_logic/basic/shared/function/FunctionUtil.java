/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.function;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utilities for working with the <code>java.util.function</code> package.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface FunctionUtil {

	/** A {@link Consumer} that does nothing. */
	static <T> Consumer<T> noOpConsumer() {
		return value -> { /* do nothing */ };
	}

	/**
	 * Creates a {@link Consumer} that calls the given {@link Consumer}s in the given order.
	 * 
	 * @param consumers
	 *        If null or empty, the {@link #noOpConsumer()} is returned. Null entries are filtered
	 *        out.
	 * @return Never null.
	 */
	static <T> Consumer<T> combine(Collection<? extends Consumer<? super T>> consumers) {
		if (consumers == null) {
			return noOpConsumer();
		}
		Consumer<T> result = noOpConsumer();
		for (Consumer<? super T> consumer : consumers) {
			if (consumer == null) {
				continue;
			}
			result = result.andThen(consumer);
		}
		return result;
	}

	/**
	 * Creates a {@link Consumer} that calls the given {@link Consumer}s one after another.
	 * 
	 * @param consumers
	 *        If null or empty, the {@link #noOpConsumer()} is returned. Null entries are filtered
	 *        out.
	 * @return Never null.
	 */
	static <T> Consumer<T> combine(Stream<? extends Consumer<? super T>> consumers) {
		return combine(consumers.collect(toList()));
	}

}
