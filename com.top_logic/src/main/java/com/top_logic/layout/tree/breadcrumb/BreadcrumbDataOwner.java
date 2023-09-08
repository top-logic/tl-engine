/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

/**
 * This class is used by the scripting framework to find the owner of the {@link BreadcrumbData}.
 * The owner is needed to retrieve/find the {@link BreadcrumbData} when the script is executed.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public interface BreadcrumbDataOwner {

	/**
	 * This class is used if there is no {@link BreadcrumbDataOwner}. (For whatever
	 * reason.)
	 */
	public static final class NoBreadcrumbDataOwner implements BreadcrumbDataOwner {

		/**
		 * The instance of the {@link NoBreadcrumbDataOwner}. This is not a singleton, as
		 * (potential) subclasses can create further instances.
		 */
		public static final NoBreadcrumbDataOwner INSTANCE = new NoBreadcrumbDataOwner();

		/**
		 * Only subclasses may need to instantiate it. Everyone else should use the
		 * {@link #INSTANCE} constant directly.
		 */
		protected NoBreadcrumbDataOwner() {
			// See JavaDoc above.
		}

		@Override
		public BreadcrumbData getBreadcrumbData() {
			throw new UnsupportedOperationException("There is no " + BreadcrumbDataOwner.class.getSimpleName()
				+ " for this " + BreadcrumbData.class.getSimpleName() + "!");
		}

	}

	/** Getter for the owned {@link BreadcrumbData}. */
	public BreadcrumbData getBreadcrumbData();

}
