/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import static java.util.Objects.*;

import java.util.function.Consumer;

import com.top_logic.layout.DisplayContext;

/**
 * An adapter from {@link Consumer} to {@link Success}.
 * <p>
 * This class is named "Functional"Success, as it makes it possible to create a {@link Success} the
 * functional way: By passing a lambda expression.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class FunctionalSuccess extends Success {

	private final Consumer<? super DisplayContext> _consumer;

	/** Creates a {@link FunctionalSuccess} from a {@link Consumer}. */
	public FunctionalSuccess(Consumer<? super DisplayContext> consumer) {
		_consumer = requireNonNull(consumer);
	}

	@Override
	protected void doExecute(DisplayContext context) {
		_consumer.accept(context);
	}

}
