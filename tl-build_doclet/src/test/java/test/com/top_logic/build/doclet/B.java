/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

/**
 * A new annotation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public @interface B {

	/**
	 * Some arbitrary text.
	 */
	String value() default "foo";

	/**
	 * Even more texts. Contains {@link C}s instead of only the {@link String}'s content to test
	 * suffixes to link tags.
	 */
	C[] other() default { @C("x"), @C("Y") };

	/**
	 * An inner annotation to be added to {@link B#other()}.
	 */
	@interface C {
		/**
		 * Additional text.
		 */
		String value();
	}
}
