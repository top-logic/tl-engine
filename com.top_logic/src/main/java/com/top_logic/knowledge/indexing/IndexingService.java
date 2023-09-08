/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing;

import java.util.Collection;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Interface for indexing information associated with knowledge objects.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface IndexingService {
    
    /**
     * Prepare the service for operation. Preparation could include 
     * creation of index if not already available or unlocking an index.
     * 
     * @return <code>true</code> if some preparation tasks have been done
     */ 
    public boolean prepareService();

    /**
     * Check if the indexing service is available. In case the service 
     * is not available a fallback service could be used by the caller.
     * 
     * @return <code>true</code> if service is availabe
     */
    public boolean serviceAvailable();

    /**
     * Get the knowledge base for the knowledge objects to be indexed.
     * 
     * @return the knowledge base 
     */ 
    public KnowledgeBase getKnowledgeBase();
    
    /**
     * Get the KO types to index
     * 
     * @return the type name collection. <code>null</code> indicates all types
     */
	public Collection<String> getTypesToIndex();

	/**
	 * Check if the index contains this document.
	 * 
	 * @param content
	 *        the document to be checked for existence
	 */
    public void indexContent(ContentObject content) throws IndexException;
    
//    public void indexContents(Collection contentObjects) throws IndexException;

	/**
	 * Remove the document from the index.
	 * 
	 * @param key
	 *        the document to be removed from index
	 * @throws IndexException
	 *         in case deleting document from index fails
	 */
    public void removeContent(ObjectKey key) throws IndexException;
    
    /**
	 * Whether the index knows a document with the given key.
	 * 
	 * @param key
	 *        The key of the indexed content
	 * @return Whether some document with the given key is known.
	 * @throws IndexException
	 *         is thrown sometimes
	 */
    public boolean containsContent(ObjectKey key) throws IndexException; 
    
    /**
     * Reindex all MetaObjects of configured types
     * 
     * @param typesToIndex 		optional type list. If not <code>null</code> it contains Strings denoting KO-types to index
     * @param objectsToIndex 	optional object list. If not <code>null</code> it contains a list of KOs and Wrappers to index
     * @param onlyNew 			if true only objects not in the index will be indexed
     * 
     * @return the number of indexed objects
     */
	public int reindex(Collection<String> typesToIndex, Collection<Wrapper> objectsToIndex, boolean onlyNew)
			throws IndexException;

	/**
	 * Deletes the index and re-index the {@link KnowledgeBase}. <br/>
	 * <b>Note for implementers:</b> This method has to work even if the index got corrupted.
	 */
	public void rebuildIndex();

	/**
	 * Reset the index to an clean and empty state. <br/>
	 * <b>Note for implementers:</b> This method has to work even if the index got corrupted.
	 */
	public void resetIndex();

	/**
	 * Start the index. If the index is not started, it must not hold any file handles to the index.
	 */
	public void startIndex();

	/**
	 * Stop the index. If the index is stopped, it must not hold any file handles to the index.
	 */
	public void stopIndex();

}
