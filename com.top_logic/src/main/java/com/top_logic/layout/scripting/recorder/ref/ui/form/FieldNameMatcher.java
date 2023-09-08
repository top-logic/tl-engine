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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link Filter} for {@link FormMember} base on its {@link FormMember#getName() technical name}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldNameMatcher extends AbstractFieldMatcher<FieldNameMatcher.Config> implements Filter<FormMember> {

	/**
	 * Configuration options for {@link FieldNameMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<FieldNameMatcher> {

		/**
		 * Short-cut tag name for {@link FieldNameMatcher}.
		 */
		String TAG_NAME = "named-member";

		/**
		 * The expected technical name.
		 */
		@Mandatory
		String getName();

		/**
		 * @see #getName()
		 */
		void setName(String name);

	}

	/**
	 * Creates a {@link FieldNameMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FieldNameMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	/**
	 * Creates a {@link FieldNameMatcher}.
	 * 
	 * @param name
	 *        See {@link Config#getName()}.
	 */
	public FieldNameMatcher(String name) {
		super(config(name));
	}

	private static Config config(String name) {
		Config result = TypedConfiguration.newConfigItem(Config.class);
		result.setName(name);
		return result;
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		return this;
	}

	@Override
	public boolean accept(FormMember candidate) {
		return getConfig().getName().equals(candidate.getName());
	}

	/**
	 * {@link FieldAnalyzer} creating {@link FieldNameMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			return new FieldNameMatcher(field.getName());
		}
	}

}