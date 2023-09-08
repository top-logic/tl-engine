/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * A {@link ConfigurationValueProvider} for {@link TLType}.
 * 
 * <p>
 * <b>Warning:</b> This configuration format is not generally usable. The consequence of using this
 * format is that a resolved type becomes part of a loaded configuration. In many cases, this is not
 * acceptable because:
 * <ul>
 * <li>The resolved type is a persistent object that depends on the {@link ModelService} being
 * started. while parsing layout configurations this cannot be assumed to be the case. During
 * deployment layout configurations are parsed for overlay resolution. During this step, the
 * application cannot be assumed to be started.</li>
 * 
 * <li>The resolved type is a persistent object. This object can become invalid, but a cached
 * configuration cannot take action to release the invalid reference. This may cause an application
 * crash requiring a reboot.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLTypeFormat extends AbstractConfigurationValueProvider<TLType> {

	/** The instance of the {@link TLTypeFormat}. */
	public static final TLTypeFormat INSTANCE = new TLTypeFormat();

	private TLTypeFormat() {
		super(TLType.class);
	}

	@Override
	protected TLType getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return TLModelUtil.findType(propertyValue.toString());
	}

	@Override
	protected String getSpecificationNonNull(TLType tlType) {
		return TLModelUtil.qualifiedName(tlType);
	}

}
