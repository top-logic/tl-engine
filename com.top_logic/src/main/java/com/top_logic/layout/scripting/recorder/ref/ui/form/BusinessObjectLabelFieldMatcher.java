/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link FieldMatcher} that identifies {@link FormMember}s based on the label of their
 * {@link FormMember#getStableIdSpecialCaseMarker() annotated business object}.
 * 
 * @see FormMember#getStableIdSpecialCaseMarker()
 */
public class BusinessObjectLabelFieldMatcher extends AbstractFieldMatcher<BusinessObjectLabelFieldMatcher.Config> {

	/**
	 * Configuration options for {@link BusinessObjectLabelFieldMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<BusinessObjectLabelFieldMatcher> {

		/**
		 * Short-cut tag name for {@link BusinessObjectLabelFieldMatcher}.
		 */
		String TAG_NAME = "object-label";

		/**
		 * Label of the business object linked to a matching {@link FormMember}.
		 * 
		 * @see FormMember#getStableIdSpecialCaseMarker()
		 * @see FormMember#setStableIdSpecialCaseMarker(Object)
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String label);

	}

	/**
	 * Creates a {@link BusinessObjectLabelFieldMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BusinessObjectLabelFieldMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		return new Filter<>() {
			@Override
			public boolean accept(FormMember anObject) {
				if (!anObject.hasStableIdSpecialCaseMarker()) {
					return false;
				}
				return Utils.equals(getConfig().getLabel(), label(anObject.getStableIdSpecialCaseMarker()));
			}
		};
	}

	private static String label(Object object) {
		return MetaLabelProvider.INSTANCE.getLabel(object);
	}

	/**
	 * {@link FieldAnalyzer} creating {@link BusinessObjectLabelFieldMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			Object marker = field.getStableIdSpecialCaseMarker();
			if (marker == null) {
				return null;
			}
			String label = label(marker);
			if (StringServicesShared.isEmpty(label)) {
				return null;
			}

			Config config = TypedConfiguration.newConfigItem(Config.class);
			config.setLabel(label);
			return new BusinessObjectLabelFieldMatcher(null, config);
		}
	}

}
