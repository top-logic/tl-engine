/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;

/**
 * Base class for {@link ModelNamingScheme}s for {@link FormMember}s based on {@link FieldMatcher}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMemberNaming<N extends ModelName> extends GlobalModelNamingScheme<FormMember, N> {

	private List<FieldAnalyzer> _analyzers;

	/**
	 * Creates a {@link GlobalFormMemberNaming}.
	 */
	@CalledByReflection
	public AbstractFormMemberNaming() {
		_analyzers = ApplicationConfig.getInstance().getConfig(FieldAnalyzers.class).getAnalyzers();
	}

	@Override
	public Class<FormMember> getModelClass() {
		return FormMember.class;
	}

	/**
	 * Heuristics to create a {@link FieldMatcher} for the given {@link FormMember} that should be
	 * identified.
	 * 
	 * @param model
	 *        The The {@link FormMember} being recorded.
	 * @return The matcher that most accurately can identify this member during replay.
	 */
	protected FieldMatcher createMatcher(FormMember model) {
		// Process in reverse order to find special analyzers (registered in custom modules) first.
		for (int n = _analyzers.size() - 1; n >= 0; n--) {
			FieldAnalyzer analyzer = _analyzers.get(n);

			FieldMatcher matcher = getMatcherSafe(model, analyzer);
			if (matcher == null) {
				continue;
			}

			return matcher;
		}
		throw new IllegalArgumentException("No matcher found for: " + model);
	}

	private FieldMatcher getMatcherSafe(FormMember formMember, FieldAnalyzer analyzer) {
		try {
			return analyzer.getMatcher(formMember);
		} catch (RuntimeException ex) {
			/* Unable to use this ModelNamingScheme. This can happen, as not every NamingScheme can
			 * be used in every situation. The caller will try the next one. */
			return null;
		}
	}

}
