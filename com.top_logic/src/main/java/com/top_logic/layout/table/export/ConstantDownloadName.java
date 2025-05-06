/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DownloadNameProvider} that can be configured with a constant value.
 */
public class ConstantDownloadName extends AbstractConfiguredInstance<ConstantDownloadName.Config<?>>
		implements DownloadNameProvider {

	/**
	 * Configuration options for {@link ConstantDownloadName}.
	 */
	public interface Config<I extends ConstantDownloadName> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getDownloadName()}. */
		String DOWNLOAD_NAME = "downloadName";

		/**
		 * Resource key for the name of the download file.
		 */
		@Name(DOWNLOAD_NAME)
		@Mandatory
		ResKey getDownloadName();
	}

	/**
	 * Creates a {@link ConstantDownloadName} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstantDownloadName(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public ResKey createDownloadName(LayoutComponent component, Object model) {
		return getConfig().getDownloadName();
	}

}
