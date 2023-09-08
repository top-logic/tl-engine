/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * Interface for everyone having a getter for a {@link SingleSelectionModel}. This interface is
 * required to handle them uniformly.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public interface SingleSelectionModelProvider {

	/** The name says everything. */
	public SingleSelectionModel getSingleSelectionModel();

}
