/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming;

/**
 * {@link GenericModelOwnerNaming} for {@link GenericCommandModelOwner}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GenericCommandModelNaming extends
		GenericModelOwnerNaming<CommandModel, GenericCommandModelOwner, GenericCommandModelNaming.GenericCommandModelName> {

	/**
	 * {@link com.top_logic.layout.scripting.recorder.ref.GenericModelOwnerNaming.GenericModelName}
	 * for {@link GenericCommandModelNaming}.
	 */
	public interface GenericCommandModelName extends GenericModelOwnerNaming.GenericModelName<CommandModel> {
		// Marker interface to have correct namespace.
	}

	@Override
	public Class<GenericCommandModelOwner> getModelClass() {
		return GenericCommandModelOwner.class;
	}

	@Override
	public Class<GenericCommandModelName> getNameClass() {
		return GenericCommandModelName.class;
	}

	@Override
	protected GenericCommandModelOwner createOwner(Object reference, Mapping<Object, CommandModel> algorithm) {
		return new GenericCommandModelOwner(reference, algorithm);
	}

}
