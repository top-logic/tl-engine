/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import com.top_logic.knowledge.wrap.list.FastListElement;

/**
 * Indicates how classification values should be partitioned.
 * 
 * @see #VALUE
 * @see #CLASSIFICATION
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public enum PartitionKind {

	/**
	 * <code>CLASSIFICATION</code> partitions will be added based on fix classifying keys, e.g.
	 * {@link FastListElement}. With this setting, an object with a list-valued attribute is added
	 * to all partitions identified by any of the keys in the attribute value. The result may
	 * contain empty partitions if no value for a key exists. Collection-valued attribute values are
	 * partitioned by the elements of the collection. The same object may be part of more than one
	 * partition.
	 */
	CLASSIFICATION,

	/**
	 * The value is directly used as key. Objects are added to the same partition, if they have
	 * equal values. The result will only contain partitions for existing values.
	 */
	VALUE
}