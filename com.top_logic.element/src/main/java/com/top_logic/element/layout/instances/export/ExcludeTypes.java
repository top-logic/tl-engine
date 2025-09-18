/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRefsFormat;

/**
 * {@link ExportCustomization} that excludes types from export.
 */
public class ExcludeTypes extends AbstractConfiguredInstance<ExcludeTypes.Config<?>> implements ExportCustomization {

	/**
	 * Configuration options for {@link ExcludeTypes}.
	 */
	@TagName("exclude-types")
	public interface Config<I extends ExcludeTypes> extends PolymorphicConfiguration<I> {
		/**
		 * The types to exclude from export.
		 */
		@Format(TLModelPartRefsFormat.class)
		@Mandatory
		List<TLModelPartRef> getTypes();
	}

	/**
	 * Creates a {@link ExcludeTypes} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExcludeTypes(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void perform(XMLInstanceExporter exporter) {
		for (TLModelPartRef type : getConfig().getTypes()) {
			exporter.addExclude(type.resolve());
		}
	}

}
