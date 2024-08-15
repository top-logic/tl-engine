/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.jdbc;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.element.model.jdbcBinding.api.ImportRow;
import com.top_logic.element.model.jdbcBinding.api.TypeSelector;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Returns a configured {@link TLClass}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@InApp
public class ConstantTypeSelector extends AbstractConfiguredInstance<ConstantTypeSelector.Config>
		implements TypeSelector {

	/** {@link ConfigurationItem} for the {@link ConstantTypeSelector}. */
	public interface Config extends PolymorphicConfiguration<ConstantTypeSelector> {

		/** The {@link TLClass} to be returned. */
		@Mandatory
		TLModelPartRef getType();

	}

	private final TLClass _type;

	/** {@link TypedConfiguration} constructor for {@link ConstantTypeSelector}. */
	public ConstantTypeSelector(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_type = config.getType().resolveClass();
	}

	@Override
	public TLClass getType(ImportRow row) {
		return _type;
	}

}
