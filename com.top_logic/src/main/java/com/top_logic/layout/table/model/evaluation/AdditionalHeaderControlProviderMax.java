/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model.evaluation;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.table.model.AdditionalHeaderControlModel;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.layout.table.model.SimpleAdditionalHeaderControl;

/**
 * The {@link ControlProvider} that for an {@link ColumnBaseConfig#getAdditionalHeaders() additional
 * header} that displays the maximum value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AdditionalHeaderControlProviderMax implements ControlProvider {

	/** {@link ConfigurationItem} for the {@link AdditionalHeaderControlProviderMax}. */
	@TagName(TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AdditionalHeaderControlProviderMax> {
		// Nothing to configure.
	}

	/** {@link TypedConfiguration} constructor for {@link AdditionalHeaderControlProviderMax}. */
	@SuppressWarnings("unused")
	public AdditionalHeaderControlProviderMax(InstantiationContext context, Config config) {
		/* Ignore the parameters. There is nothing to configure. This would be a singleton, if
		 * "TagName" or "Subtypes" would work for singletons. */
	}

	private static final String TAG_NAME = "max";

	@Override
	public Control createControl(Object model, String style) {
		return new SimpleAdditionalHeaderControl((AdditionalHeaderControlModel) model, Collections.emptyMap()) {

			@Override
			protected ResKey getStaticLabelPart() {
				return I18NConstants.HEADER_MAX__VALUE;
			}

			@Override
			protected Object getDynamicLabelPart() {
				/* Use CollectionUtil.max not Collections.max, as the former is null safe. */
				/* This compiler warning is justified, but almost impossible to prevent. */
				return CollectionUtil.max((List) getModel().getValues());
			}

			@Override
			protected ResKey getStaticTooltipPart() {
				return I18NConstants.HEADER_MAX__VALUE.tooltip();
			}

			@Override
			protected Object getDynamicTooltipPart() {
				return null; // The tooltip is completely static.
			}

		};
	}

}
