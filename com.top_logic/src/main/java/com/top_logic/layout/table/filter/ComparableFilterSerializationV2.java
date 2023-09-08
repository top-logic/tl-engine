/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ConfiguredFilterSerialization} of comparable filters of format version 2.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class ComparableFilterSerializationV2 implements ConfiguredFilterSerialization<ComparableFilterConfiguration> {

	/** Static instance of {@link ComparableFilterSerializationV2} */
	public static final ConfiguredFilterSerialization<ComparableFilterConfiguration> INSTANCE =
		new ComparableFilterSerializationV2();

	@Override
	public List<Object> serialize(ComparableFilterConfiguration configuration) {
		List<Object> serializedState = new ArrayList<>(3);
		serializedState.add(Collections.singletonList(2));
		serializedState.add(configuration.getFilterPattern());
		serializedState.add(ComparableFilterSerializationV1.INSTANCE.serialize(configuration));
		return serializedState;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(ComparableFilterConfiguration configuration, List<Object> serializedState) {
		configuration.setFilterPattern((List<Object>) serializedState.get(1));
		ComparableFilterSerializationV1.INSTANCE.deserialize(configuration, (List<Object>) serializedState.get(2));
	}
}