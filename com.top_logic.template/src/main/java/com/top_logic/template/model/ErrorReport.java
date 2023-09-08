/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a template check.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ErrorReport {

	private final List<TemplateProblem> errors;
	private final List<TemplateProblem> warnings;

	public ErrorReport() {
		this(new ArrayList<>(), new ArrayList<>());
	}
	
	public ErrorReport(List<TemplateProblem> errors, List<TemplateProblem> warnings) {
		assert errors != null;
		assert warnings != null;
		
		this.errors = errors;
		this.warnings = warnings;
	}

	public boolean isOk() {
		return !hasErrors() && !hasWarnings();
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public List<TemplateProblem> getErrors() {
		return errors;
	}

	public List<TemplateProblem> getWarnings() {
		return warnings;
	}

	public void addError(TemplateProblem error) {
		errors.add(error);
	}

	public void addWarning(TemplateProblem warning) {
		warnings.add(warning);
	}

}
