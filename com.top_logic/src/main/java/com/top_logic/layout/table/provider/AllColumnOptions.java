/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.func.Function3;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.AllTypesAttributes;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Option provider returning all valid columns for the given {@link TLModelPart}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllColumnOptions extends
		Function3<Collection<ColumnOption>, Collection<TLModelPartRef>, Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>>, Object> {

	/**
	 * Singleton {@link AllColumnOptions} instance.
	 */
	public static AllColumnOptions INSTANCE = new AllColumnOptions();

	@Override
	public Collection<ColumnOption> apply(Collection<TLModelPartRef> typeRefs,
			Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>> providers, Object touchOnly) {
		Collection<? extends TLStructuredTypePart> parts = AllTypesAttributes.INSTANCE.apply(typeRefs);

		return TableUtil.createColumnOptions(parts, providers);
	}

	/**
	 * All columns excluding those defined by table configuration plug-ins.
	 */
	public Collection<ColumnOption> apply(Collection<TLModelPartRef> typeRefs) {
		return apply(typeRefs, Collections.emptyList(), null);
	}

}
