/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model.evaluation;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;
import static java.util.Collections.*;

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
 * header} that displays the average of the values.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AdditionalHeaderControlProviderAverage implements ControlProvider {

	/** {@link ConfigurationItem} for the {@link AdditionalHeaderControlProviderAverage}. */
	@TagName(TAG_NAME)
	public interface Config extends PolymorphicConfiguration<AdditionalHeaderControlProviderAverage> {
		// Nothing to configure.
	}

	/** {@link TypedConfiguration} constructor for {@link AdditionalHeaderControlProviderAverage}. */
	@SuppressWarnings("unused")
	public AdditionalHeaderControlProviderAverage(InstantiationContext context, Config config) {
		/* Ignore the parameters. There is nothing to configure. This would be a singleton, if
		 * "TagName" or "Subtypes" would work for singletons. */
	}

	private static final String TAG_NAME = "average";

	@Override
	public Control createControl(Object model, String style) {
		return new SimpleAdditionalHeaderControl((AdditionalHeaderControlModel) model, emptyMap()) {

			@Override
			protected ResKey getStaticLabelPart() {
				return I18NConstants.HEADER_AVERAGE__VALUE;
			}

			@Override
			protected Object getDynamicLabelPart() {
				List<Object> values = getModel().getValues();
				CollectionUtil.cleanUp(values); // Removes all nulls.
				if (values.isEmpty()) {
					return null;
				}
				/* This compiler warning is justified, but almost impossible to prevent. */
				double sum = sum((List) values);
				return sum / values.size();
			}

			@Override
			protected ResKey getStaticTooltipPart() {
				return I18NConstants.HEADER_AVERAGE__VALUE.tooltip();
			}

			@Override
			protected Object getDynamicTooltipPart() {
				return null; // The tooltip is completely static.
			}

		};
	}

}
