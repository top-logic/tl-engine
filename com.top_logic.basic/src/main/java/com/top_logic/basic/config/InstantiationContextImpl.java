/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogAdaptor;
import com.top_logic.basic.Protocol;

/**
 * Abstract implementation of {@link InstantiationContext} that handles the logging interface.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class InstantiationContextImpl extends LogAdaptor implements InstantiationContextSPI {

	private final Log _log;

	private StringBuilder _problem;

	/**
	 * Creates a new {@link InstantiationContextImpl} using the given {@link Log} for error
	 * reporting.
	 */
	public InstantiationContextImpl(Log log) {
		_log = log;
	}

	@Override
	protected Log impl() {
		return _log;
	}

	@Override
	public final <T> T getInstance(PolymorphicConfiguration<T> configuration) {
		return getInstance(this, configuration);
	}

	@Override
	public final <T> T deferredReferenceCheck(Supplier<T> r) {
		return deferredReferenceCheck(this, r);
	}

	@Override
	public void checkErrors() throws ConfigurationException {
		if (_log instanceof Protocol) {
			((Protocol) _log).checkErrors();
		} else {
			if (_log.hasErrors()) {
				throw toConfigurationException(_log.getFirstProblem());
			}
		}
	}

	@Override
	public void error(String message) {
		super.error(message);

		onError(message, null);
	}

	@Override
	public void error(String message, Throwable ex) {
		super.error(message, ex);

		onError(message, ex);
	}

	private void onError(String message, Throwable ex) {
		if (_problem == null) {
			_problem = new StringBuilder();
		} else {
			_problem.append(" ");
		}
		_problem.append(message);
		if (ex != null) {
			_problem.append(": ");
			if (ex.getMessage() != null) {
				_problem.append(ex.getMessage());
			} else {
				_problem.append(ex.getClass().getName());
			}
		}
	}

	private ConfigurationException toConfigurationException(Throwable ex) {
		if (ex instanceof ConfigurationException) {
			return (ConfigurationException) ex;
		}
		return new ConfigurationException("Errors in context: " + _problem, ex);
	}

}

