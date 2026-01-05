/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.vector;
//

//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
//import com.top_logic.basic.StringServices;
//import com.top_logic.basic.col.Maybe;
//import com.top_logic.basic.config.ApplicationConfig;
//import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.dob.identifier.ObjectKey;
//import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The class {@link VectorIndexUtils} contains method needed by {@link VectorIndex} and
 * {@link VectorThread}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class VectorIndexUtils {

	/**
	 * Configuration for {@link VectorIndexUtils}.
	 * 
	 * @author <a href="mailto:boris.nikolov@top-mapps.com">Boris Nikolov</a>
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * Empty configuration
		 */
	}

	private VectorIndexUtils() {
		// no instantiation
	}
	
	/**
	 * Creates a Vector Index document that can be added to a Vector Index index. This method does
	 * not take the content from the physical resource but from the param 'content'.
	 * 
	 * @param aDoc
	 *        The knowledge object (document) to be added.
	 * @param content
	 *        The content for the document. Must not be <code>null</code>
	 * @param description
	 *        The description of the document, may be <code>null</code>. If <code>null</code>
	 *        description is computed from <code>content</code>
	 * @return A vector document
	 */
	static String createVectorDocument(KnowledgeObject aDoc, String content, String description) {
		/**
		 * Embed the content and return the vectors
		 */
		return "";
	}

	/**
	 * Checks if the given document is known in the Vector index.
	 * 
	 * @param key
	 *        the knowledge object that to be checked
	 * @return true if the given document is known in the Vector index.
	 */
	static boolean containsDocument(ObjectKey key) {
		String theKOId = getObjectName(key);

		return false;
	}


	private static String getObjectName(ObjectKey key) {
		try {
			return IdentifierUtil.toExternalForm(key.getObjectName());
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Trying to get the object name from the key failed.", exception);
		}
	}
	
	/**
	 * Forces the current {@link Thread} to sleep the given milliseconds.
	 */
	public static void internalSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException wakeupCall) {
			Logger.warn("Thread was interrupted!", wakeupCall, VectorIndexUtils.class);
		}
	}
	
}
