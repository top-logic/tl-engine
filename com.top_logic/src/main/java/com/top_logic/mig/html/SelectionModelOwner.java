/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;

/**
 * This class is used by the scripting framework to find the owner of the {@link SelectionModel}.
 * The owner is needed to retrieve/find the {@link SelectionModel} when the script is executed.
 * <p>
 * When there is no owner, use {@link #NO_OWNER}. But you have to either register another
 * {@link ModelNamingScheme} for the {@link SelectionModel} or override
 * {@link SelectionModel#getOwner()}. If you do neither of that, the {@link SelectionModel} cannot
 * be recorded for scripted tests.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface SelectionModelOwner {

	/**
	 * When there is no {@link SelectionModelOwner} for a {@link SelectionModel}.
	 * <p>
	 * If this is used, the selection cannot be recorded for scripted tests. (See
	 * {@link SelectionModelOwner} for details.)
	 * </p>
	 */
	public static final SelectionModelOwner NO_OWNER = new SelectionModelOwner() {

		@Override
		public SelectionModel getSelectionModel() {
			throw new UnsupportedOperationException("There is no owner and therefore no selection model.");
		}
	};

	/** Getter for the owned {@link SelectionModel}. */
	public SelectionModel getSelectionModel();

}
