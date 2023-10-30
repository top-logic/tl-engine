/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} for {@link Enum} instances based on resource keys derived form the class
 * name of the enum and the enum name.
 * 
 * <p>
 * A enum constant <code>BAR</code> in enum class <code>my.package.Foo</code> is translated through
 * a key <code>my.package.Foo.BAR</code>.
 * </p>
 * 
 * <p>
 * A enum constant <code>BAR</code> in enum class <code>Foo</code> that is an inner enum of class
 * <code>my.package.Xyz</code> is translated through a key <code>my.package.Xyz.Foo.BAR</code>.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumLabelProvider implements LabelProvider {

	/**
	 * Singleton {@link EnumLabelProvider} instance.
	 * 
	 * @deprecated Better use {@link EnumResourceProvider#INSTANCE}.
	 */
	@Deprecated
	public static final EnumLabelProvider INSTANCE = new EnumLabelProvider();

	/** Singleton constructor. */
	protected EnumLabelProvider() {
		super();
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return "";
		}

		Enum<?> enumInstance = (Enum<?>) object;
		return Resources.getInstance().getString(ResKey.forEnum(enumInstance));
	}

}
