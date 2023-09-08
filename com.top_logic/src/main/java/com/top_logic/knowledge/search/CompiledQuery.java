/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


import java.util.List;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * A compiled variant of a {@link RevisionQuery} which can be executed directly, without expensive
 * preprocessing.
 * 
 * <p>
 * Utility methods to execute such queries are found in {@link KBUtils}.
 * </p>
 * 
 * @see KnowledgeBase#compileQuery(RevisionQuery)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompiledQuery<E> {

	/**
	 * Executes the compiled query in streaming mode without arguments.
	 * 
	 * @return {@link CloseableIterator} that iterates over the search results.
	 * 
	 * @see #searchStream(RevisionQueryArguments)
	 * @see #search()
	 */
	CloseableIterator<E> searchStream();

	/**
	 * Executes the compiled query without arguments and returns the full result as list.
	 * 
	 * @return list that contains the search results.
	 * 
	 * @see #search(RevisionQueryArguments)
	 * @see #searchStream()
	 */
	List<E> search();

	/**
	 * Executes the compiled query in streaming mode with the given {@link RevisionQueryArguments}.
	 * 
	 * @param arguments
	 *        the actual arguments of the query.
	 * 
	 * @return {@link CloseableIterator} that iterates over the search results.
	 * 
	 * @see #search(RevisionQueryArguments)
	 */
	CloseableIterator<E> searchStream(RevisionQueryArguments arguments);

	/**
	 * Executes the compiled query with the given {@link RevisionQueryArguments} and returns the
	 * full result as list.
	 * 
	 * @param arguments
	 *        the actual arguments of the query.
	 * 
	 * @return list that contains the search results.
	 * 
	 * @see #searchStream(RevisionQueryArguments)
	 */
	List<E> search(RevisionQueryArguments arguments);

	/**
	 * Executes the compiled query in streaming mode.
	 * 
	 * @param connection
	 *        the connection to the database to use.
	 * @param arguments
	 *        the actual arguments of the query.
	 * 
	 * @return {@link CloseableIterator} that iterates over the database results.
	 * 
	 * @see #search(PooledConnection, RevisionQueryArguments) Alternative method to retrieve the
	 *      complete result.
	 */
	CloseableIterator<E> searchStream(PooledConnection connection, RevisionQueryArguments arguments);

	/**
	 * Executes the compiled query and returns the full result as list.
	 * 
	 * @param connection
	 *        the connection to the database to use.
	 * @param arguments
	 *        the actual arguments of the query.
	 * 
	 * @return list that contains the database results.
	 * 
	 * @see #searchStream(PooledConnection, RevisionQueryArguments) Alternative method to retrieve
	 *      the result in streaming mode.
	 */
	List<E> search(PooledConnection connection, RevisionQueryArguments arguments);

}

