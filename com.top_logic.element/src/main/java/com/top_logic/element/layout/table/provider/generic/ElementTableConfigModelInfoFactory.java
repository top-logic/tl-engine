/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.provider.generic;

import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfo;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfoFactoryImpl;
import com.top_logic.model.TLClass;

/**
 * {@link TableConfigModelInfoFactoryImpl} for elements just available in tl-element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementTableConfigModelInfoFactory extends TableConfigModelInfoFactoryImpl {

	/**
	 * Creates a new {@link ElementTableConfigModelInfoFactory}.
	 */
	public ElementTableConfigModelInfoFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public TableConfigModelInfo create(Set<? extends TLClass> contentTypes) {
		return new ElementTableConfigModelInfo(contentTypes);
	}

}

