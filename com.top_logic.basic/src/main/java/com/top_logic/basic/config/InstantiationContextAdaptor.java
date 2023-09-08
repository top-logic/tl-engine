/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;

/**
 * Allows to specify a different {@link Protocol} than the one given in the original {@link InstantiationContext}.
 * 
 * @author    <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public class InstantiationContextAdaptor extends InstantiationContextImpl {

	private final InstantiationContextSPI _context;

	/**
	 * Creates a new {@link InstantiationContextAdaptor}.
	 * 
	 * @param log
	 *        The {@link Protocol} that should overrule the one in the {@link InstantiationContext}.
	 * @param context
	 *        The original {@link InstantiationContext}.
	 */
	public InstantiationContextAdaptor(Log log, InstantiationContext context) {
		super(log);
		_context = impl(context);
	}

	private static InstantiationContextSPI impl(InstantiationContext context) {
		if (context instanceof InstantiationContextAdaptor) {
			return ((InstantiationContextAdaptor) context)._context;
		} else {
			return (InstantiationContextSPI) context;
		}
	}

	@Override
	public <T> T getInstance(InstantiationContext self, PolymorphicConfiguration<T> configuration) {
		return _context.getInstance(self, configuration);
	}

	@Override
	public <T> void resolveReference(Object id, Class<T> scope, ReferenceResolver<T> setter) {
		_context.resolveReference(id, scope, setter);
	}

	@Override
	public <T> T deferredReferenceCheck(InstantiationContext self, Supplier<T> r) {
		return _context.deferredReferenceCheck(self, r);
	}

	@Override
	public void checkErrors() throws ConfigurationException {
		super.checkErrors();
		_context.checkErrors();
	}

	@Override
	public Throwable getFirstProblem() {
		Throwable firstProblem = super.getFirstProblem();
		if (firstProblem != null) {
			return firstProblem;
		}
		return _context.getFirstProblem();
	}

	@Override
	public boolean hasErrors() {
		return super.hasErrors() || _context.hasErrors();

	}

	@Override
	public void error(String message) {
		preserveFirstProblem();
		super.error(message);

	}

	@Override
	public void error(String message, Throwable ex) {
		preserveFirstProblem();
		super.error(message, ex);

	}

	private void preserveFirstProblem() {
		if (super.getFirstProblem() != null) {
			return;
		}
		Throwable firstProblem = _context.getFirstProblem();
		if (firstProblem == null) {
			return;
		}
		super.error(firstProblem.getMessage(), firstProblem);
	}

}
