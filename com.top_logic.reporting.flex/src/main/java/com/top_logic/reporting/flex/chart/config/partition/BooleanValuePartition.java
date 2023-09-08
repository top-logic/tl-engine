/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class BooleanValuePartition extends SingleValuePartition {

	/**
	 * Config-interface for {@link BooleanValuePartition}.
	 */
	public interface Config extends SingleValuePartition.Config {

		@Override
		@ClassDefault(BooleanValuePartition.class)
		public Class<BooleanValuePartition> getImplementationClass();

	}

	/**
	 * Config-constructor for {@link BooleanValuePartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public BooleanValuePartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected List<Object> getOptions(Partition aParent) {
		return CollectionUtil.dynamicCastView(Object.class, CollectionUtil.createList(Boolean.TRUE, Boolean.FALSE));
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(TLStructuredTypePart ma) {
		assert !AttributeOperations.isCollectionValued(ma);
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setMetaAttribute(new MetaAttributeProvider(ma));
		item.setAddEmpty(!ma.isMandatory());
		return item;
	}

}