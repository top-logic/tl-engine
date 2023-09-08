/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;

/**
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class MultiClassificationPartition extends MultiValuePartition {

	/**
	 * Config-interface for {@link MultiClassificationPartition}.
	 */
	@DisplayOrder({
		Config.PARTITION,
		Config.ADD_EMPTY,
		Config.ENSURE_OPTIONS,
	})
	public interface Config extends MultiValuePartition.Config {

		/**
		 * Property name of {@link #getEnsureOptions()}.
		 */
		public static final String ENSURE_OPTIONS = "ensure-options";

		@Override
		@ClassDefault(MultiClassificationPartition.class)
		public Class<MultiClassificationPartition> getImplementationClass();

		/**
		 * Whether all possible options based on the {@link FastList} should be displayed even if
		 * they are empty or not.
		 * 
		 * <p>
		 * By default, all {@link FastListElement}s are considered.
		 * </p>
		 */
		@BooleanDefault(true)
		@Name(ENSURE_OPTIONS)
		public boolean getEnsureOptions();

	}

	/**
	 * Config-constructor for {@link MultiClassificationPartition}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public MultiClassificationPartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected List<Object> getOptions(Partition aParent) {
		Config config = getConfig();
		if (config.getPartitionKind() == PartitionKind.CLASSIFICATION && config.getEnsureOptions()) {
			return CollectionUtil.dynamicCastView(Object.class, getFastListElements(getFastList()));
		}
		return Collections.emptyList();
	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config item(TLStructuredTypePart ma) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		item.setMetaAttribute(new MetaAttributeProvider(ma));
		item.setAddEmpty(!ma.isMandatory());
		return item;
	}

}