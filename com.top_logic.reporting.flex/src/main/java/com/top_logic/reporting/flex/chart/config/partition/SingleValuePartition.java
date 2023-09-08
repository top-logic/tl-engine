/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.flex.chart.config.model.Partition;

/**
 * {@link PartitionFunction} that partitions by single-valued attributes.
 * 
 * @see MultiValuePartition
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SingleValuePartition extends AbstractAttributeBasedPartition {

	/**
	 * Config-interface for {@link SingleValuePartition}.
	 */
	public interface Config extends AbstractAttributeBasedPartition.Config {

		@Override
		@ClassDefault(SingleValuePartition.class)
		public Class<? extends SingleValuePartition> getImplementationClass();

	}

	public SingleValuePartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	protected void handleValue(Partition aParent, Map<Object, Partition> map, TLObject att, Object value) {
		addValue(aParent, map, getKey(value), att);
	}

	@Override
	protected void handleEmpty(Partition aParent, Map<Object, Partition> map, TLObject att) {
		addValue(aParent, map, getNotSetText(), att);
	}

}