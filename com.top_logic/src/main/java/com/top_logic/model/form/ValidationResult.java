/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.util.ResKey;

/**
 * Result of constraint evaluation for a single attribute.
 */
public class ValidationResult {

	/** Singleton for a valid result with no errors or warnings. */
	public static final ValidationResult VALID = new ValidationResult(Collections.emptyList(), Collections.emptyList());

	private final List<ResKey> _errors;
	private final List<ResKey> _warnings;

	/**
	 * Creates a {@link ValidationResult}.
	 */
	public ValidationResult(List<ResKey> errors, List<ResKey> warnings) {
		_errors = List.copyOf(errors);
		_warnings = List.copyOf(warnings);
	}

	/**
	 * Error messages from ERROR-type constraints.
	 */
	public List<ResKey> getErrors() {
		return _errors;
	}

	/**
	 * Warning messages from WARNING-type constraints.
	 */
	public List<ResKey> getWarnings() {
		return _warnings;
	}

	/**
	 * Whether there are no errors.
	 */
	public boolean isValid() {
		return _errors.isEmpty();
	}

	/**
	 * Creates a result with a single error.
	 */
	public static ValidationResult error(ResKey error) {
		return new ValidationResult(List.of(error), Collections.emptyList());
	}

	/**
	 * Creates a result with a single warning.
	 */
	public static ValidationResult warning(ResKey warning) {
		return new ValidationResult(Collections.emptyList(), List.of(warning));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ValidationResult)) return false;
		ValidationResult other = (ValidationResult) obj;
		return _errors.equals(other._errors) && _warnings.equals(other._warnings);
	}

	@Override
	public int hashCode() {
		return 31 * _errors.hashCode() + _warnings.hashCode();
	}
}
