/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.model.TLEnumeration;

/**
 * Configurable {@link TableFilterProvider} for enumerations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultClassificationTableFilterProvider extends AbstractClassificationTableFilterProvider {

	public interface Config extends PolymorphicConfiguration<TableFilterProvider> {
		@Name(CLASSIFICATIONS_PROPERTY)
		@Mandatory
		String getClassifications();

		@Name(MULTI_PROPERTY)
		@BooleanDefault(false)
		boolean getMulti();

		@Name(MANDATORY_PROPERTY)
		@BooleanDefault(false)
		boolean getMandatory();
	}

	/**
	 * Configuration option for the {@link #isMandatory()} attribute.
	 */
	public static final String MANDATORY_PROPERTY = "mandatory";

	/**
	 * Configuration option for the {@link #isMulti()} attribute.
	 */
	public static final String MULTI_PROPERTY = "multi";

	/**
	 * Configuration option for the {@link Config#getClassifications()} option.
	 */
	public static final String CLASSIFICATIONS_PROPERTY = "classifications";

	private final boolean _multi;

	private final boolean _mandatory;

	/**
	 * Creates a {@link DefaultClassificationTableFilterProvider} from configuration.
	 */
	public DefaultClassificationTableFilterProvider(InstantiationContext context, Config config) throws ConfigurationException {
		this(loadClassifications(listNamesValue(context, config)), multiValue(context, config),
			mandatoryValue(context, config));
	}

	private static List<String> listNamesValue(InstantiationContext context, Config config) throws ConfigurationException {
		return Arrays.asList(config.getClassifications().split("\\s*,\\s*"));
	}

	private static boolean multiValue(InstantiationContext context, Config config) {
		return config.getMulti();
	}

	private static boolean mandatoryValue(InstantiationContext context, Config config) {
		return config.getMandatory();
	}

	/**
	 * Creates a {@link DefaultClassificationTableFilterProvider}.
	 * 
	 * @param classifications
	 *        {@link TLEnumeration}s to display.
	 * @param multi
	 *        See {@link #isMulti()}.
	 */
	public DefaultClassificationTableFilterProvider(List<TLEnumeration> classifications, boolean multi,
			boolean mandatory) {
		super(classifications);
		_multi = multi;
		_mandatory = mandatory;
	}

	@Override
	protected boolean isMulti() {
		return _multi;
	}

	@Override
	protected boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * Creates a {@link DefaultClassificationTableFilterProvider}.
	 */
	public static DefaultClassificationTableFilterProvider create(List<String> names, boolean multiple,
			boolean mandatory) {
		return new DefaultClassificationTableFilterProvider(loadClassifications(names), multiple, mandatory);
	}

}
