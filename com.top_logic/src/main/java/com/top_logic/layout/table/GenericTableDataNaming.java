/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming;

/**
 * {@link GenericModelOwnerNaming} for {@link GenericTableDataOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericTableDataNaming extends
		GenericModelOwnerNaming<TableData, GenericTableDataOwner, GenericTableDataNaming.GenericTableDataName> {

	/**
	 * {@link com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming.GenericModelName}
	 * for {@link GenericTableDataNaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GenericTableDataName extends GenericModelOwnerNaming.GenericModelName<TableData> {
		// Marker interface to have correct namespace.
	}

	@Override
	public Class<GenericTableDataOwner> getModelClass() {
		return GenericTableDataOwner.class;
	}

	@Override
	public Class<GenericTableDataName> getNameClass() {
		return GenericTableDataName.class;
	}

	@Override
	protected GenericTableDataOwner createOwner(Object reference, Mapping<Object, TableData> algorithm) {
		return new GenericTableDataOwner(reference, algorithm);
	}

}
