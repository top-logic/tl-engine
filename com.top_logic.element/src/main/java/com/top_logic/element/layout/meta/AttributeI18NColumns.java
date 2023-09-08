/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Locale;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.DynamicColumns;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.util.Resources;

/**
 * {@link TableConfigurationProvider} dynamically creating columns for all supported languages
 * containing the localized name of an attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeI18NColumns extends DynamicColumns {

	/**
	 * Creates a {@link AttributeI18NColumns} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AttributeI18NColumns(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		for (String language : ResourcesModule.getInstance().getSupportedLocaleNames()) {
			ColumnConfiguration column = table.declareColumn("name_" + language);
			adaptDefaultConfiguration(column);

			// Locale construction looks broken, but left as is for compatibility.
			final Locale locale = new Locale(language, language);
			column.setColumnLabelKey(I18NConstants.I18N_NAME_COLUMN__LOCALE.fill(
				Resources.getDisplayLanguage(locale)));
			column.setAccessor(new ReadOnlyAccessor<TLStructuredTypePart>() {
				@Override
				public Object getValue(TLStructuredTypePart object, String property) {
					ResKey labelKey = TLModelI18N.getI18NKey(object);
					return Resources.getInstance(locale).getString(labelKey);
				}
			});
		}
	}

}
