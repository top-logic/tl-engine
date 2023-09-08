/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.template.model.TemplateProblem.Type;

/**
 * A {@link TemplateException} encapsulates all {@link TemplateProblem}s that occurred during the
 * parsing and checking process of a template. The problems can be accessed via the
 * {@link #getErrors()} and {@link #getWarnings()} methods. Another option is dumping the problems
 * to the {@link Logger} using {@link #printProblems()}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateException extends Exception {

	private final ErrorReport problems;

	public TemplateException(List<TemplateProblem> someErrors, List<TemplateProblem> someWarnings) {
		this(new ErrorReport(someErrors, someWarnings));
	}

	public TemplateException(ErrorReport problems) {
		super(ResKey.encode(getMessage(problems)));
		this.problems = problems;
	}

	private static ResKey getMessage(ErrorReport problems) {
		if (problems.hasErrors()) {
			return problems.getErrors().get(0).getErrorDescription();
		}
		if (!problems.hasWarnings()) {
			return problems.getWarnings().get(0).getErrorDescription();
		}
		return null;
	}

	/**
	 * Returns a {@link List} of all errors occurred until this exception was thrown.
	 * 
	 * @return a {@link List} of {@link TemplateProblem} of type {@link Type#SYNTAX_ERROR} or
	 *         {@link Type#MODEL_ERROR}
	 */
	public List<TemplateProblem> getErrors() {
		return problems.getErrors();
	}
	
	/**
	 * Returns a {@link List} of all warnings occurred until this exception was thrown.
	 * 
	 * @return a {@link List} of {@link TemplateProblem} of type {@link Type#SYNTAX_WARNING} or
	 *         {@link Type#MODEL_WARNING}
	 */
	public List<TemplateProblem> getWarnings() {
		return problems.getWarnings();
	}
	
	/** 
	 * Displays all errors and warnings using the {@link Logger}.
	 */
	public void printProblems() {
		for(TemplateProblem p : getErrors()) {
			Logger.error(ResKey.encode(p.getErrorDescription()), TemplateException.class);
		}

		for(TemplateProblem p : getWarnings()) {
			Logger.warn(ResKey.encode(p.getErrorDescription()), TemplateException.class);
		}
	}
}
