/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.layout.LabelProvider;
import com.top_logic.model.util.TLModelUtil;

/**
 * Singleton assignment link.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLModuleSingleton extends com.top_logic.model.impl.generated.TLModuleSingletonsBase {

	/**
	 * The name under which the singleton is known in {@link TLModuleSingletons}.
	 * 
	 * @see com.top_logic.model.TLNamed#getName()
	 */
	@Override
	String getName();

	/**
	 * The {@link TLModule} for which {@link #getSingleton()} is a singleton.
	 */
	TLModule getModule();

	/**
	 * The actual singleton object.
	 */
	TLObject getSingleton();

	/**
	 * {@link LabelProvider} for {@link TLModuleSingleton}s.
	 */
	class Label implements LabelProvider {

		/**
		 * Singleton {@link TLModuleSingleton.Label} instance.
		 */
		public static final TLModuleSingleton.Label INSTANCE = new TLModuleSingleton.Label();

		private Label() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			TLModuleSingleton link = (TLModuleSingleton) object;
			return TLModelUtil.qualifiedName(link.getModule()) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR
				+ link.getName();
		}
	}
}
