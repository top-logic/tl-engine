/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.ArrayList;

/**
 * Class implementing the {@link AbstractLine}. A Line contains a List of {@link LinePart}s,
 * representing changes made to the text in the Line.
 * 
 * @author     <a href="mailto:aru@top-logic.com">aru</a>
 */
public class Line extends AbstractLine {
	
	public Line(TempLine templine) {
		super(new ArrayList<>(templine.getLineParts()));
	}
	
}
