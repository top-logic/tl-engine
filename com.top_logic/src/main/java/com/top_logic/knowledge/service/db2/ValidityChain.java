/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

/**
 * Representation of a chain of objects that are valid for a certain time range.
 * 
 * <p>
 * A {@link ValidityChain} has a maximum and a minimum validity. Therefore in that range minimum
 * inclusive and maximum inclusive, this link is valid. the {@link ValidityChain} can hold a link to
 * an older validity range. Therefore this chain can be used to navigate from the current revision
 * to a given older one to find the correct validity range.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ValidityChain<T extends ValidityChain<T>> extends CleanupOldValues {

	/**
	 * Ensures that this link is not valid beyond (or at) the given validity.
	 */
	void updateMaxValidity(long validity);

	/**
	 * Ensures that this link is not valid before the given validity.
	 */
	void publishLocalValidity(long newMinValidity);

	/**
	 * Returns a revision such that for all revisions &gt;= the given revision and &lt;=
	 * {@link #maxValidity()} the value of this link is valid.
	 * 
	 * @see #maxValidity()
	 */
	long minValidity();

	/**
	 * Returns a revision such that for all revisions &lt;= the given revision and &gt;=
	 * {@link #minValidity()} the value of this link is valid.
	 * 
	 * @see #minValidity()
	 */
	long maxValidity();

	/**
	 * Pointer to a link with {@link #maxValidity()} smaller than the {@link #minValidity()}
	 * of this link.
	 * 
	 * <p>
	 * The {@link #maxValidity()} of the returned cache may be strict less than
	 * <code>{@link #minValidity()} - 1</code> of this cache, i.e. there may be a data gap.
	 * </p>
	 * 
	 * @return May be <code>null</code> in case no former link is available.
	 */
	T formerValidity();

	/**
	 * Sets value of {@link #formerValidity()}.
	 */
	void setFormerValidity(T formerValidity);

}

