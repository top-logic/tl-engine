/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link Filter} for {@link FormMember}s matching the top-level member.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormContextMatcher extends AbstractFieldMatcher<FormContextMatcher.Config> implements Filter<FormMember> {

	/**
	 * Configuration options for {@link FormContextMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<FormContextMatcher> {

		/**
		 * Short-cut tag name for {@link FormContextMatcher}s.
		 */
		String TAG_NAME = "form-context";

	}

	/**
	 * Creates a {@link FormContextMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FormContextMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	/**
	 * Creates a {@link FormContextMatcher}.
	 */
	public FormContextMatcher() {
		super(config());
	}

	private static Config config() {
		Config result = TypedConfiguration.newConfigItem(Config.class);
		return result;
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		return this;
	}

	@Override
	public boolean accept(FormMember candidate) {
		return isRootFormContext(candidate);
	}

	static boolean isRootFormContext(FormMember candidate) {
		return candidate instanceof FormContext && candidate.getParent() == null;
	}

	/**
	 * {@link FieldAnalyzer} creating {@link FormContextMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			if (FormContextMatcher.isRootFormContext(field)) {
				return new FormContextMatcher();
			}
			return null;
		}
	}
}