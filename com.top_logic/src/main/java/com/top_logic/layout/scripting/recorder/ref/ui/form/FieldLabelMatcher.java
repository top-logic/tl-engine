/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.Iterator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.util.Resources;

/**
 * {@link Filter} for {@link FormMember} base on its label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldLabelMatcher extends AbstractFieldMatcher<FieldLabelMatcher.Config> implements Filter<FormMember> {

	/**
	 * Configuration options for {@link FieldLabelMatcher}
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<FieldLabelMatcher> {

		/**
		 * Short-cut tag name for {@link FieldLabelMatcher}s.
		 */
		String TAG_NAME = "labeled-member";

		/**
		 * The expected label.
		 */
		@Mandatory
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String value);

	}

	/**
	 * Creates a {@link FieldLabelMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FieldLabelMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	/**
	 * Creates a {@link FieldLabelMatcher}.
	 * 
	 * @param label
	 *        See {@link Config#getLabel()}.
	 */
	public FieldLabelMatcher(String label) {
		super(config(label));
	}

	private static Config config(String label) {
		Config result = TypedConfiguration.newConfigItem(Config.class);
		result.setLabel(label);
		return result;
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		return this;
	}

	@Override
	public boolean accept(FormMember candidate) {
		return hasLabel(candidate, getConfig().getLabel());
	}

	static boolean hasLabel(FormMember candidate, String label) {
		return candidate.hasLabel() && label.equals(label(candidate));
	}

	static String label(FormMember member) {
		return StringServices.nonNull(Resources.getInstance().getStringOptional(member.getLabel())).trim();
	}

	/**
	 * {@link FieldAnalyzer} creating {@link FieldLabelMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			if (!field.hasLabel()) {
				return null;
			}
			String label = label(field);
			if (label.isEmpty()) {
				return null;
			}
			FormContainer parent = field.getParent();
			if (parent != null) {
				for (Iterator<? extends FormMember> siblings = parent.getMembers(); siblings.hasNext();) {
					FormMember sibling = siblings.next();
					if (sibling == field) {
						// Regard only real siblings
						continue;
					}
					if (hasLabel(sibling, label)) {
						// Label is not unique.
						return null;
					}
				}
			}
			return new FieldLabelMatcher(label);
		}

	}

}