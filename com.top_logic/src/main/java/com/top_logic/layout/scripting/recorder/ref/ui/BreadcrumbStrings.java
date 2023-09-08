/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ListConfigValueProvider;

/**
 * A path in the gui as "Breadcrumb" String: <br/>
 * <code>First Level Tab > Second Level Tab > Third Level Tab</code>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class BreadcrumbStrings extends ListConfigValueProvider<String> {

	/** Singleton instance of {@link BreadcrumbStrings} */
	public static final ConfigurationValueProvider<List<String>> INSTANCE = new BreadcrumbStrings();

	private BreadcrumbStrings() {
		// Singleton constructor.
	}

	@Override
	public String getSpecificationNonNull(List<String> configValue) {
		return StringServices.join(configValue, " > ");
	}

	@Override
	public List<String> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		return new ArrayList<>(Arrays.asList(propertyValue.toString().split("\\s*\\>\\s*")));
	}

}
