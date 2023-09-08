/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.provider.generic;

import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.generic.TableConfigModelInfoImpl;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLTypeContext;

/**
 * {@link TableConfigModelInfoImpl} for elements just available in tl-element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementTableConfigModelInfo extends TableConfigModelInfoImpl {

	/**
	 * Creates a new {@link ElementTableConfigModelInfo}.
	 */
	protected ElementTableConfigModelInfo(Collection<? extends TLClass> classes) {
		super(classes);
	}

	@Override
	protected ColumnInfo createReferenceColumn(TLTypeContext type, ResKey headerI18NKey, Accessor<?> accessor) {
		return new CompositionSupportingReferenceColumn(type, headerI18NKey, visibility(type), accessor);
	}

}

