/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming;

/**
 * {@link GenericModelOwnerNaming} for {@link GenericSelectionModelOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericSelectionModelNaming extends
		GenericModelOwnerNaming<SelectionModel, GenericSelectionModelOwner, GenericSelectionModelNaming.GenericSelectionModelName> {

	/**
	 * {@link com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming.GenericModelName}
	 * for {@link GenericSelectionModelNaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GenericSelectionModelName extends GenericModelOwnerNaming.GenericModelName<SelectionModel> {
		// Marker interface to have correct namespace.
	}

	@Override
	public Class<GenericSelectionModelOwner> getModelClass() {
		return GenericSelectionModelOwner.class;
	}

	@Override
	public Class<GenericSelectionModelName> getNameClass() {
		return GenericSelectionModelName.class;
	}

	@Override
	protected GenericSelectionModelOwner createOwner(Object reference, Mapping<Object, SelectionModel> algorithm) {
		return new GenericSelectionModelOwner(reference, algorithm);
	}

}
