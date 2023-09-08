/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link DisplayProvider} that can handle most attribute types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFormTagProvider extends IndirectDisplayProvider {

	/**
	 * Singleton {@link DefaultFormTagProvider} instance.
	 */
	public static final DefaultFormTagProvider INSTANCE = new DefaultFormTagProvider();

	private DefaultFormTagProvider() {
		// Singleton constructor.
	}

	@Override
	public ControlProvider getControlProvider(EditContext editContext) {
		int type = AttributeOperations.getMetaAttributeType(editContext);
		switch (type) {
			case LegacyTypeCodes.TYPE_COMPLEX: {
				return com.top_logic.element.meta.form.tag.SelectTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_CLASSIFICATION: {
				return com.top_logic.element.meta.form.tag.EnumerationTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_DATE: {
				return com.top_logic.element.meta.form.tag.DateTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_STRING_SET: {
				return com.top_logic.element.meta.form.tag.StringSetTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_FLOAT: {
				return newStringTagProvider(10).getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_LONG: {
				return newStringTagProvider(10).getControlProvider(editContext);
			}
			// we shouldn't need this any longer as the input tag is rendered dynamically
			// case MetaAttributeFactory.TYPE_CALCULATED:
			case LegacyTypeCodes.TYPE_STRING: {
				return newStringTagProvider(TextInputTag.NO_COLUMNS).getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_LIST:
			case LegacyTypeCodes.TYPE_COLLECTION:
			case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
			case LegacyTypeCodes.TYPE_SINGLE_STRUCTURE:
			case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
			case LegacyTypeCodes.TYPE_WRAPPER:
			case LegacyTypeCodes.TYPE_TYPEDSET:
			case LegacyTypeCodes.TYPE_STRUCTURE:
			case LegacyTypeCodes.TYPE_HISTORIC_WRAPPER: {
				return com.top_logic.element.meta.form.tag.ReferenceTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_DAP:
			case LegacyTypeCodes.TYPE_DAP_FALLB:
			case LegacyTypeCodes.TYPE_DAP_COLLECTION: {
				return com.top_logic.element.meta.form.tag.DAPTagProvider.INSTANCE.getControlProvider(editContext);
			}
			case LegacyTypeCodes.TYPE_COMPOSITION:
				return new CustomInputTag();

			case LegacyTypeCodes.TYPE_GALLERY:
				return com.top_logic.element.meta.form.tag.GalleryTagProvider.INSTANCE.getControlProvider(editContext);

			default:
				throw new UnsupportedOperationException("Cannot create tag for type "
					+ AttributeSettings.getInstance().getTypeAsString(type));
		}
	}

	private com.top_logic.element.meta.form.tag.StringTagProvider newStringTagProvider(int columns) {
		StringTagProvider.Config config = TypedConfiguration.newConfigItem(StringTagProvider.Config.class);
		config.setColumns(columns);
		return TypedConfigUtil.createInstance(config);
	}

}
