/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.ui.form.AbstractFieldMatcher;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FieldAnalyzer;
import com.top_logic.layout.scripting.recorder.ref.ui.form.FieldMatcher;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FieldMatcher} based on the binding of an {@link TLStructuredTypePart} to a
 * {@link FormMember}.
 * 
 * @see AttributeFormFactory#getAttributeUpdate(FormMember)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeFieldMatcher extends AbstractFieldMatcher<AttributeFieldMatcher.Config> {

	/**
	 * Configuration options for {@link AttributeFieldMatcher}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AttributeFieldMatcher> {

		/**
		 * Short-cut tag name for {@link AttributeFieldMatcher}.
		 */
		String TAG_NAME = "attribute";

		/**
		 * Reference to the base object, may be <code>null</code>, if the attribute field is used
		 * for object creation.
		 */
		ModelName getSelf();

		/** @see #getSelf() */
		void setSelf(ModelName self);

		/**
		 * Reference to the attribute itself.
		 */
		ModelName getAttribute();

		/**
		 * @see #getAttribute()
		 */
		void setAttribute(ModelName value);

		/**
		 * Some custom prefix for the field's name.
		 * 
		 * @see MetaAttributeGUIHelper#internalID(String, TLStructuredTypePart, TLObject, String)
		 */
		@StringDefault(MetaAttributeGUIHelper.ATT_PREFIX)
		String getPrefix();

		/**
		 * @see #getPrefix()
		 */
		void setPrefix(String value);

		/**
		 * @see MetaAttributeGUIHelper#internalID(String, TLStructuredTypePart, TLObject, String)
		 */
		@Nullable
		String getDomain();

		/**
		 * @see #getDomain()
		 */
		void setDomain(String value);
	}

	/**
	 * Creates a {@link AttributeFieldMatcher} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeFieldMatcher(InstantiationContext context, Config config) {
		super(config);
	}

	@Override
	public Filter<? super FormMember> createFilter(ActionContext context) {
		Config config = getConfig();
		TLStructuredTypePart attribute =
			(TLStructuredTypePart) ModelResolver.locateModel(context, config.getAttribute());
		TLObject self = (TLObject) ModelResolver.locateModel(context, config.getSelf());
		String prefix = config.getPrefix();
		String domain = config.getDomain();
		final String fieldName = MetaAttributeGUIHelper.internalID(prefix, attribute, self, domain);
		return new Filter<FormMember>() {
			@Override
			public boolean accept(FormMember anObject) {
				return fieldName.equals(anObject.getName());
			}
		};
	}

	/**
	 * {@link FieldAnalyzer} creating {@link AttributeFieldMatcher}s.
	 */
	public static class Analyzer implements FieldAnalyzer {
		@Override
		public FieldMatcher getMatcher(FormMember field) {
			AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(field);
			if (update == null) {
				return null;
			}

			String fieldName = field.getName();
			int sepIndex = fieldName.indexOf(AttributeUpdateContainer.ID_SEPARATOR);
			if (sepIndex < 0) {
				return null;
			}

			String prefix = fieldName.substring(0, sepIndex);

			String id = MetaAttributeGUIHelper.internalID(prefix, update.getAttribute(), update.getObject(),
				update.getDomain());
			if (!id.equals(fieldName)) {
				// Safety against nonsense out there.
				return null;
			}

			Config config = TypedConfiguration.newConfigItem(Config.class);

			// Do not pollute the XML.
			if (!prefix.equals(config.getPrefix())) {
				config.setPrefix(prefix);
			}

			config.setAttribute(ModelResolver.buildModelName(update.getAttribute()));
			config.setSelf(ModelResolver.buildModelName(update.getObject()));

			config.setDomain(update.getDomain());

			return new AttributeFieldMatcher(null, config);
		}
	}

}
