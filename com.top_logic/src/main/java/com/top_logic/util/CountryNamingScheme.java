/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.util.CountryNamingScheme.CountryName;

/**
 * Identifies a {@link Country} by its ISO country code.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class CountryNamingScheme extends AbstractModelNamingScheme<Country, CountryName> {

	/** @see CountryNamingScheme */
	public interface CountryName extends ModelName {

		/** @see CountryNamingScheme */
		public String getIsoCountryCode();

		/** @see CountryNamingScheme */
		public void setIsoCountryCode(String isoCountryCode);

	}

	@Override
	public Class<CountryName> getNameClass() {
		return CountryName.class;
	}

	@Override
	public Class<Country> getModelClass() {
		return Country.class;
	}

	@Override
	public Country locateModel(ActionContext context, CountryName name) {
		return new Country(name.getIsoCountryCode());
	}

	@Override
	protected void initName(CountryName name, Country model) {
		name.setIsoCountryCode(model.getCode());
	}

}
