/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The class {@link LuceneUtils} contains method needed by {@link LuceneIndex} and
 * {@link LuceneThread}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LuceneUtils {
	
	private static final FieldType NO_INDEX = new FieldType();
	static {
		NO_INDEX.setIndexOptions(IndexOptions.NONE);
		NO_INDEX.setTokenized(false);
		NO_INDEX.setOmitNorms(true);
		NO_INDEX.setStored(true);
		NO_INDEX.freeze();
	}

	/**
	 * Configuration for {@link LuceneUtils}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * The <i>LockFactory</i> that is used by Lucene. When nothing is configured, the default
		 * factory is used.
		 * <p>
		 * <b>WARNING: If you make the wrong choice here, your Lucene index may silently get
		 * corrupted.</b>
		 * </p>
		 * 
		 * @implNote Default lock factory is currently
		 *           {@link org.apache.lucene.store.NativeFSLockFactory} as stated in:
		 *           {@link FSDirectory#open(Path)}
		 */
		Class<? extends LockFactory> getLockFactory();
	}

	private LuceneUtils() {
		// no instantiation
	}

	private static Maybe<LockFactory> newLockFactory() {
		Config config = ApplicationConfig.getInstance().getConfig(LuceneUtils.Config.class);
		Class<? extends LockFactory> lockFactory = config.getLockFactory();
		if (lockFactory == null) {
			return Maybe.none();
		}

		try {
			return Maybe.<LockFactory> some(ConfigUtil.getInstance(lockFactory));
		}
		catch (Exception exception) {
			throw new RuntimeException("Failed to instantiate configured LockFactory for Lucene.", exception);
		}
	}
	
	/**
	 * Creates a lucene document that can be added to a lucene index. This
	 * method does not take the content from the physical resource but from the
	 * param 'content'.
	 * 
	 * @param aDoc
	 *        The knowledge object (document) to be added.
	 * @param content
	 *        The content for the document. Must not be <code>null</code>
	 * @param description
	 *        The description of the document, may be <code>null</code>. If
	 *        <code>null</code> description is computed from
	 *        <code>content</code>
	 * @return A lucene document
	 */
	static Document createLuceneDocument(KnowledgeObject aDoc, String content, String description) {
		if (content == null) {
			throw new IllegalArgumentException("'content' must not be 'null'.");
		}

		Document theDoc = new Document();

		// create id and doctype fields
		Field IDField = new StringField(LuceneIndex.FIELD_KO_ID, getObjectName(aDoc), Field.Store.YES);
		Field doctypeField = new TextField(LuceneIndex.FIELD_DOCTYPE, getTypeName(aDoc), Field.Store.YES);

		// create description field
		String myDescription = content;
		if (!StringServices.isEmpty(description)) {
			myDescription = description;
		}
		if (myDescription.length() >= LuceneIndex.FIELD_DESCRIPTION_MAXSIZE) {
			myDescription = myDescription.substring(0, LuceneIndex.FIELD_DESCRIPTION_MAXSIZE - 1);
		}
		Field DescriptionField = new Field(LuceneIndex.FIELD_DESCRIPTION, myDescription, NO_INDEX);

		// create content field
		Field ContentField = new TextField(LuceneIndex.FIELD_CONTENTS, content, Field.Store.YES);

		theDoc.add(IDField);
		theDoc.add(doctypeField);
		theDoc.add(DescriptionField);
		theDoc.add(ContentField);
		return theDoc;
	}


	private static String getObjectName(KnowledgeObject doc) {
		try {
			return IdentifierUtil.toExternalForm(doc.getObjectName());
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Failed to get the name of the object!", exception);
		}
	}


	private static String getTypeName(KnowledgeObject doc) {
		try {
			return doc.tTable().getName();
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Failed to get the name of the type!", exception);
		}
	}

	/**
	 * Checks if the given document is known in the Lucene index.
	 * 
	 * @param key
	 *        the knowledge object that to be checked
	 * @param reader
	 *        the reader to ask for the document. not <code>null</code>
	 * @return true if the given document is known in the Lucene index.
	 */
	static boolean containsDocument(ObjectKey key, IndexReader reader) {
		String theKOId = getObjectName(key);

		Term theTerm = new Term(LuceneIndex.FIELD_KO_ID, theKOId);
		try {
			return reader.totalTermFreq(theTerm) > 0;
		} catch (IOException ioe) {
			throw new IndexException("containsDocument() failed for document '" + key + "'.", ioe);
		}
	}

	/**
	 * Delete document from Lucene index.
	 * 
	 * @param key
	 *        A document to be deleted from the Lucene index
	 * @param indexWriter
	 *        the {@link IndexWriter} to delete the document
	 */
	static void deleteDocument(ObjectKey key, IndexWriter indexWriter) {
		try {
			try {
				indexWriter.deleteDocuments(new Term(LuceneIndex.FIELD_KO_ID, getObjectName(key)));
			} catch(OutOfMemoryError err) {
				indexWriter.rollback();
				throw err;
			}
		}
		catch (IOException exception) {
			throw new IndexException("Deleting document failed! Key of object: '" + key + "'", exception);
		}
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
	 * Opens the {@link Directory} for the given file.
	 * 
	 * @see FSDirectory#open(Path)
	 */
	public static Directory openDirectory(Path location) throws IOException {
		// We need a lockFactory instance for every call. Otherwise we might get Exceptions like that:
		// "You can set the lock directory for this factory only once."
		Maybe<LockFactory> lockFactory = newLockFactory();
		if (lockFactory.hasValue()) {
			return FSDirectory.open(location, lockFactory.get());
		} else {
			return FSDirectory.open(location);
		}
	}

	/**
	 * Opens a reader for the given directory.
	 * 
	 * @param location
	 *        the file of the {@link Directory} to open
	 * 
	 * @return Returns an {@link IndexReader} reading the index in the given directory location.
	 * 
	 * @throws CorruptIndexException
	 *         if the index is corrupt
	 * @throws IOException
	 *         if there is a low-level IO error
	 */
	public static IndexReader openIndexReader(final File location) throws CorruptIndexException, IOException {
		return DirectoryReader.open(openDirectory(location.toPath()));
	}
	
	/**
	 * Forces the current {@link Thread} to sleep the given milliseconds.
	 */
	public static void internalSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException wakeupCall) {
			Logger.warn("Thread was interrupted!", wakeupCall, LuceneUtils.class);
		}
	}
	
}
