/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

import java.util.ArrayList;

/**
 * Second override of {@link #compareTo(D)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class C extends A<D, ArrayList<D>> {

	@Override
	public int compareTo(D o) {
		return 99;
	}

}
