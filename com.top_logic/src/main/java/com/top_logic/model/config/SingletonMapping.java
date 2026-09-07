/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.Logger;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.AllSingletons;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link OptionMapping} for the singleton of a {@link TLModule} providing its qualified name as
 * selection.
 *
 * <p>
 * The qualified name of a singleton is the qualified name of its {@link TLModule}, followed by
 * {@link TLModelUtil#QUALIFIED_NAME_PART_SEPARATOR} and the name of the singleton within that
 * module.
 * </p>
 *
 * @see AllSingletons The matching option provider.
 * @see TLModelUtil#resolveQualifiedName(String)
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingletonMapping implements OptionMapping {

	/** Singleton {@link SingletonMapping} instance. */
	public static final SingletonMapping INSTANCE = new SingletonMapping();

	private SingletonMapping() {
		// singleton instance
	}

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		if (selection == null) {
			return null;
		}
		try {
			return TLModelUtil.resolveQualifiedName(ModelService.getApplicationModel(), (String) selection);
		} catch (TopLogicException ex) {
			Logger.error("Cannot resolve singleton for '" + selection + "'.", ex, SingletonMapping.class);
			return null;
		}
	}

	@Override
	public Object toSelection(Object option) {
		if (option == null) {
			return null;
		}
		TLObject singleton = (TLObject) option;
		TLModuleSingleton link = ModelService.getApplicationModel()
			.getQuery(TLModuleSingletons.class)
			.getModuleAndName(singleton);
		if (link == null) {
			// Not a singleton, therefore it cannot be named.
			return null;
		}
		return TLModelUtil.qualifiedName(link.getModule()) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR
			+ link.getName();
	}

}
