/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.text.Format;
import java.text.ParseException;
import java.util.Date;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} of {@link Date} values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateNaming extends AbstractModelNamingScheme<Date, DateNaming.Name> {

	/**
	 * {@link ModelName} of {@link DateNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The literal value.
		 * 
		 * @see #getFormat()
		 */
		String getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(String value);

		/**
		 * The {@link Format} for parsing the {@link Date} specified in {@link #getValue()}
		 */
		@ClassDefault(XmlDateTimeFormat.class)
		Class<? extends Format> getFormat();

	}

	@Override
	public Class<Date> getModelClass() {
		return Date.class;
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Date locateModel(ActionContext context, Name name) {
		String spec = name.getValue();

		try {
			Format format = ConfigUtil.getInstance(name.getFormat());

			return (Date) format.parseObject(spec);
		} catch (ConfigurationException ex) {
			throw ApplicationAssertions.fail(name, "Cannot access configured date format '" + name.getFormat() + "'.",
				ex);
		} catch (ParseException ex) {
			throw ApplicationAssertions.fail(name, "Cannot parse configured date '" + spec + "'.", ex);
		}
	}

	@Override
	protected void initName(Name name, Date model) {
		name.setValue(XmlDateTimeFormat.INSTANCE.format(model));
	}

}
