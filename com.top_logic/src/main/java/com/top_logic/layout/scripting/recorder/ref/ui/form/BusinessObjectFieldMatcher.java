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
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link FieldMatcher} that identifies {@link FormMember}s based on their
 * {@link FormMember#getStableIdSpecialCaseMarker() annotated business object}.
 * 
 * @see FormMember#getStableIdSpecialCaseMarker()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BusinessObjectFieldMatcher extends AbstractFieldMatcher<BusinessObjectFieldMatcher.Config> {

	/**
	 * Configuration options for {@link BusinessObjectFieldMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<BusinessObjectFieldMatcher> {

		/**
		 * Short-cut tag name for {@link BusinessObjectFieldMatcher}.
		 */
		String TAG_NAME = "object";

		/**
		 * Description of the business object linked to a matching {@link FormMember}.
		 * 
		 * @see FormMember#getStableIdSpecialCaseMarker()
		 * @see FormMember#setStableIdSpecialCaseMarker(Object)
		 */
		public ModelName getBusinessObject();

		/**
		 * @see #getBusinessObject()
		 */
		public void setBusinessObject(ModelName businessObject);

	}

	/**
	 * Creates a {@link BusinessObjectFieldMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BusinessObjectFieldMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		return new Filter<>() {
			@Override
			public boolean accept(FormMember anObject) {
				final Object businessObject =
					ModelResolver.locateModel(context, anObject.getStableIdSpecialCaseContext(),
					getConfig().getBusinessObject());
				return anObject.hasStableIdSpecialCaseMarker()
					&& anObject.getStableIdSpecialCaseMarker().equals(businessObject);
			}
		};
	}

	/**
	 * {@link FieldAnalyzer} creating {@link BusinessObjectFieldMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			Object marker = field.getStableIdSpecialCaseMarker();
			if (marker != null) {
				Object context = field.getStableIdSpecialCaseContext();
				Config config = TypedConfiguration.newConfigItem(Config.class);
				config.setBusinessObject(ModelResolver.buildModelName(context, marker));
				return new BusinessObjectFieldMatcher(null, config);
			}
			return null;
		}
	}

}
