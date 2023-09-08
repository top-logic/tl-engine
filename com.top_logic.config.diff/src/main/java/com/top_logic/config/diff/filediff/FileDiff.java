/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.diff.filediff;

import java.util.LinkedList;
import java.util.List;

import com.top_logic.config.diff.google.diff_match_patch.Diff;

/**
 * A FileDiff represents the result of comparison of two texts, differences, structured in
 * {@link Region}s, {@link Line}s and {@link LinePart}s.
 * 
 * <p>
 * To get there, the two texts are processed by Googles diff_match_patch-library, generating a
 * {@link LinkedList} of {@link Diff}s. In a following normalization step, the list of {@link Diff}s
 * is processed to a list {@link LinePart}s. From this {@link LinePart}s, lines are computed.
 * {@link Line}s are organized in {@link Region}s. Those Regions can contain Lines with changes -
 * inserted and deleted Text - or those without. If a Region contains changed lines, a boolean flag,
 * <code>isChanged</code>, is set.
 * </p>
 * 
 * <p>
 * The information about changes in parts of lines and regions is used for documentation and to
 * support a better visualization of the differences at the UI.
 * </p>
 * 
 * @author <a href="mailto:aru@top-logic.com">aru</a>
 */
public class FileDiff {

	private List<Region> regions;

	public FileDiff(List<Region> regions) {
		this.regions = regions;
	}

	public List<Region> getRegions() {
		return regions;
	}
	
}
