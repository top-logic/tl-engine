/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.dsa.DAPropertyNames;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.analyze.AnalyzeException;
import com.top_logic.knowledge.analyze.AnalyzeService;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.indexing.IndexingService;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.error.TopLogicException;

/**
 * Wrapper for {@link com.top_logic.knowledge.objects.KnowledgeObject}s
 * of type Document.
 * 
 * TODO MGA/KHA support deleteKnowledgeObject().
 * 
 * Hereby, I declare the attribute "type" as free for use for 
 * whatever you need ... until somebody tells me that it is reserved. (dkh)
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class Document extends AbstractBoundWrapper implements BinaryData {

	/**
	 * KOAttribute which stores the version number of the content.
	 */
	private static final String VERSION_NUMBER = "versionNumber";

	/**
	 * Size of the document
	 */
	private static final String SIZE = "size";

	/**
	 * The number of the revision in which the content of this document was updated.
	 * 
	 * The value is stored explicit to ensure that the derived values are correct, also if someone
	 * just triggers a noop update (e.g. by locking the document).
	 */
	private static final String UPDATE_REVISION_NUMBER = "updateRevisionNumber";

	public static final String OBJECT_NAME = "Document";

	/** Full qualified name of the {@link TLType} of a {@link Document}. */
	public static final String DOCUMENT_TYPE = "tl.folder:Document";

	/**
	 * Resolves {@link #DOCUMENT_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link Document}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #DOCUMENT_TYPE} could not be resolved.
	 */
	public static TLStructuredType getDocumentType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(DOCUMENT_TYPE);
	}

   /** The description of this document. */
    private String desc;

    /**
     * Creates a Document wrapper for the given Knowledge Object.
     *
     * @param    ko    The object to be wrapped.
     */
	public Document(KnowledgeObject ko) {
    	super(ko);
    }

	@Override
	public String getName() {
		return super.getName();
	}

    /**
     * Returns a string representation of this document.
     *
     * @return    A representation for debugging.
     */
    @Override
	protected String toStringValues() {
		String result = super.toStringValues();
		result = result + ", DSN: " + getDSN();

		return result;
    }

	/**
	 * Method returns the content of the represented document.
	 * 
	 * @return The content of the document. Must be {@link InputStream#close() closed} after
	 *         reading. May be <code>null</code> if the underlying {@link #getDAP()} is invalid.
	 */
	public InputStream getContent() throws DatabaseAccessException {
		DataAccessProxy theDap = getDAP();
		if (theDap != null && theDap.exists() && theDap.isEntry()) {
			return dap.getEntry(String.valueOf(getVersionNumber()));
		} else {
			return null;
		}
	}

	@Override
	public void generateFullText(FullTextBuBuffer buffer) {
		super.generateFullText(buffer);
        addDocumentContent(buffer);
    }

	/**
	 * Converts the content of this document to character like content and adds it to the given
	 * {@link FullTextBuBuffer}.
	 */
	protected void addDocumentContent(FullTextBuBuffer buffer) {
		try {
            if (exists()) {
				String mimeType = getContentType();
                FormatConverterFactory factory = FormatConverterFactory.getInstance();
                FormatConverter converter = factory.getFormatConverter(mimeType);
                if(converter != null) {
					try (InputStream content = getStream()) {
						try (Reader reader = converter.convert(content, mimeType)) {
							buffer.add(reader);
						}
					}
                }else {
					Logger.info("Unable to find a matching format converter for the document ('" + getName() + "').",
						Document.class);
                }
            }
            else {
				Logger.error("Document does not exist yet.", Document.class);
            }
        }
        catch (Exception ex) {
			Logger.error("Failed to resolve full text.", ex, Document.class);
        }
	}

	/**
	 * Updates this document.
	 * 
	 * This method takes care of the physical existence of the document. If it's not present in the
	 * data source, it'll be created with the given data, if it exists, the entry will be updated
	 * using the {@link DataAccessProxy#putEntry(InputStream)} method.
	 * 
	 * @param data
	 *        The provider for the {@link InputStream} to be used for updating the document.
	 * 
	 * @return <code>true</code>, if updating succeeds, <code>false</code> if given parameter is
	 *         <code>null</code> or an error occurred in updating.
	 */
	public boolean update(BinaryData data) {
		if (data == null) {
			return (false);
		}

		if (tHandle().getHistoryContext() != Revision.CURRENT_REV) {
			throw new IllegalStateException("Unable to update historic element");
		}

		final DataAccessProxy theEntry = this.getDAP();

		if (theEntry == null) {
			String theMessage = "Unable to get DAP for Document '" + this.tHandle() + "'!";

			Logger.error(theMessage, Document.class);
			return false;
		}

		try {
			propertiesValid = false;

			boolean isRepos;
			if (theEntry.exists()) {
				isRepos = theEntry.isRepository();
				if (isRepos) {
					theEntry.lock();
				}
				final InputStream stream = data.getStream();
				try {
					theEntry.putEntry(stream);
				} finally {
					stream.close();
				}
			} else {
				DataAccessProxy theParent = theEntry.getParentProxy();
				isRepos = theParent.isRepository();
				String theName = theEntry.getName();

				final InputStream stream = data.getStream();
				try {
					theParent.createEntry(theName, stream);
				} finally {
					stream.close();
				}
			}

			updateKOValues(data.getSize());

			if (isRepos) { // Unlock and care for DocumentVersion
				theEntry.unlock();

				/* Read the new number of versions and store it. Must first unlock the entry to get
				 * correct number of versions */
				final String[] repositoryVersions = theEntry.getVersions();
				tSetData(VERSION_NUMBER, repositoryVersions == null ? 0 : repositoryVersions.length);

				DocumentVersion.createDocumentVersion(this);
			}
			return true;
		} catch (IOException ex) {
			String theMessage = "Unable to access stream for update!";
			Logger.error(theMessage, ex, Document.class);
			return false;
		}

	}

	/**
	 * Sets the base KO values, i.e. {@link #SIZE}.
	 * 
	 * @param size
	 *        the new size of the document
	 */
	private void updateKOValues(long size) {
		tSetData(SIZE, size);
		tSetData(UPDATE_REVISION_NUMBER, NextCommitNumberFuture.INSTANCE);
	}

	/**
	 * Returns the commit number of the revision in which the content of the document was updated.
	 * 
	 * @throws IllegalStateException
	 *         iff the content was updated but not yet committed
	 */
	private Long getUpdateRevisionNumber() {
		final Object updateRevNumber = tGetData(UPDATE_REVISION_NUMBER);
		if (updateRevNumber == NextCommitNumberFuture.INSTANCE) {
			throw new IllegalStateException(
				"The document was updated but not committed yet. Unable to get update revision");
		}
		return (Long) updateRevNumber;
	}

	/**
	 * Returns the updater of this {@link Document}
	 */
	public Person getUpdater() {
		final Revision updateRevision = getUpdateRevision();
		return WrapperHistoryUtils.getWrapper(updateRevision, this).getModifier();
	}

	/**
	 * Returns the time of this {@link Document}
	 */
	public Date getUpdateTime() {
		final Revision updateRevision = getUpdateRevision();
		return new Date(updateRevision.getDate());
	}

	/**
	 * Returns the {@link Revision} in which the content of the document was last updated
	 */
	private Revision getUpdateRevision() {
		final Long updateRevisionNumber = getUpdateRevisionNumber();
		return getKnowledgeBase().getHistoryManager().getRevision(updateRevisionNumber);
	}

	/**
	 * Updates this document.
	 * 
	 * This method takes care of the physical existence of the document. If it's not present in the
	 * data source, it'll be created with the given input stream, if it exists, the entry will be
	 * updated using the {@link DataAccessProxy#putEntry(InputStream)} method.
	 * 
	 * @param aStream
	 *        The stream to be used for updating the document.
	 * @return <code>true</code>, if updating succeeds, <code>false</code> if given parameter is
	 *         <code>null</code> or an error occurred in updating.
	 * 
	 * @deprecated use {@link #update(BinaryData)}
	 */
	@Deprecated
	public boolean update(InputStream aStream) {
		try {
			try {
				return update(BinaryDataFactory.createFileBasedBinaryData(aStream));
			} finally {
				aStream.close();
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Returns the version of the document, actually it is the number of updates of this document.
	 * I.e. initial it returns <code>0</code> after one update it returns <code>1</code> and so on.
	 * 
	 * @return the version of the document or <code>0</code> if the document is initially created
	 *         without content
	 */
	public int getVersionNumber() {
		return tGetDataInt(VERSION_NUMBER);
	}


	/**
	 * Returns a List with all {@link DocumentVersion} belonging to this document.
	 * 
	 * The list is ordered by the revision-attribute of the DocumentVersion.
	 */
	public List<? extends DocumentVersion> getAllDocumentVersions() {
		return DocumentVersion.getAllDocumentVersions(this);
    }
    
    /**
	 * Checking method, if the underlying DSA supports versioning of files.
     * 
     * @return    true, if versioning is supported by this document.
     */
	public boolean supportsVersions() {
        try {
            return (new DataAccessProxy(this.getDSN()).isRepository());
        }
        catch (UnknownDBException ex) {
            Logger.info("Unable to check, if versions supported!", ex, this);

            return (false);
        }
    }

	/**
	 * Returns the latest version number of this document.
	 * 
	 * @return The latest version number or an null, if versioning is not supported or another error
	 *         occured.
	 * 
	 * @deprecated use {@link #getVersionNumber()} as number of exiting versions
	 */
    @Deprecated
	public String getLatestVersionNumber() {
		return String.valueOf(getVersionNumber());
    }

	/**
	 * Rename this document.
	 * 
	 * If the renaming fails, the reason can be seen in the logging files.
	 * 
	 * @param aName
	 *        The new name of this document.
	 * @return true, if renaming succeeds.
	 */
	public boolean rename(String aName) {
		if (!this.supportsVersions()) {
			this.tHandle().setAttributeValue(NAME_ATTRIBUTE, aName);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes this document including its data source.
	 * 
	 * @param force
	 *        Whether to break locks before deletion.
	 * 
	 * @return true, if removing succeeds.
	 */
	public boolean delete(boolean force) {
		removeDataSource(force);
		removeKO();
        return true;
    }

	private void removeDataSource(boolean force) {
		// Note: In a versioned type, physical contents must not be deleted, because it is still
		// possible to access historic versions.
		if (!MetaObjectUtils.isVersioned(tHandle().tTable())) {
			DataAccessProxy physicalResource = this.getDAP();
			if (physicalResource.exists()) {
				physicalResource.delete(force);
			}
		}
	}

	/**
	 * Removes this {@link Document} object, leaving its data source untouched.
	 */
	public void removeLocal() {
		removeKO();
	}

	private void removeKO() {
		tHandle().delete();
	}

    /**
	 * Whether this document has a physical representation.
	 * 
	 * @see DataAccessProxy#exists()
	 */
	public boolean exists() {
        DataAccessProxy theDAP = getDAP();
		if (dap == null) {
			return false;
		}

		return theDAP.exists();
    }

    /**
     * Search for similar contents (information) within the system.
     * 
     * If there are similar contents, they will be contained in the
     * returned collection. The collection can only contain 
     * {@link com.top_logic.knowledge.analyze.KnowledgeObjectResult} search results,
     * no wrappers, because the type of returned contents can be different
     * from document!
     * 
     * @return    A collection of similar contents to this document with
     *            their similarity as precentage value.
     */
	public Collection getSimilar() {
        try {
            AnalyzeService theAnalyzer = DefaultAnalyzeService.getAnalyzeService();

            if (theAnalyzer != null)
                return (theAnalyzer.findSimilarRanked(this.tHandle()));
            else
                Logger.info("getSimilar(), no analyze service.",this);
        } 
        catch(AnalyzeException ex) {
            Logger.info("Unable to analyze document with analyze service!", 
                        ex, this);
        } 
        catch(UnsupportedOperationException ex) {
            Logger.debug("findSimilar() not supported!", this);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Get the keywords of this document.
     * 
     * @return    A collection of keywords for this document.
     */
	public String getKeywordsAsString() {
        String       theString;
        StringBuffer theResult = null;
        Iterator     theIt     = this.getKeywords().iterator();

        while (theIt.hasNext()) {
            theString = (String) theIt.next();

            if (theResult == null) {
                theResult = new StringBuffer(theString);
            }
            else {
                theResult.append(", ").append (theString);
            }
        }
        
        if (theResult != null) {
            return (theResult.toString());
        }
        else {
            return ("");
        }
    }

    /**
     * Get the keywords of this document.
     * 
     * @return    A collection of keywords for this document.
     */
	public Collection getKeywords() {
        try {
            AnalyzeService theAnalyzer = DefaultAnalyzeService.getAnalyzeService();

            if (theAnalyzer != null) 
                return theAnalyzer.extractKeywords(this.tHandle());
            else
                Logger.info("getKeywords(), no analyze service!", this);
        } 
        catch(AnalyzeException ex) {
            Logger.info("Unable to find keywords in analyze service!", 
                        ex, this);
        } 
        catch(UnsupportedOperationException ex) {
            Logger.debug("findKeywords() not supported!", this);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns true, if this document is searchable within the search
     * service.
     * 
     * If a document is searchable, the content can be found using a
     * full text search. Moreover the system is able to find similar
     * contents in the system (see {@link #getSimilar()}.
     * 
     * @return    true, if document is searchable.
     * @see       #getSimilar()
     */
	public boolean isSearchable() {
        try {
            IndexingService theIndexService = DefaultIndexingService.getIndexingService();

            if (theIndexService != null) {
				return theIndexService.containsContent(this.tHandle().tId());
            }
        } 
        catch(IndexException ex) {
            Logger.info("Unable to find document in search service!", 
                        ex, this);
        } 
        return false;
    }
    
    /**
     * @see com.top_logic.knowledge.wrap.Wrapper#getValue(java.lang.String)
     */
    @Override
	public Object getValue(String aKey) {
        if ("changed".equals(aKey)) {
            try {
                //return (this.getProperties().getAttributeValue(DAPropertyNames.LAST_MODIFIED));
                
                //dkh: do same as when AbstractWrapper tries to access LAST_MODIFIED, 
                //     otherwise we get NullPointerexceptions
                //     but why are props null?
                //TODO kha please review inserted code
                
				DataObject theProps = this.getProperties();

				// try via DAP/Properties first ... 
				if (theProps != null) {
					try {
						Long propertyValue = (Long) theProps.getAttributeValue(DAPropertyNames.LAST_MODIFIED);
						if (propertyValue != null) {
							return propertyValue;
						}
					}
					catch (NoSuchAttributeException ignored) {
						// e.g. happens with security://
					}
				}

				return tGetDataLong(LifecycleAttributes.MODIFIED);
                
            }
            catch (Exception ex) {
                Logger.error("Unable to get value for key '" + aKey + "'!", ex, this);

                return (null);
            }
        }
        else if ("size".equals(aKey)) {
            try {
				return (this.getSize());
            }
            catch (Exception ex) {
                Logger.error("Unable to get value for key '" + aKey + "'!", ex, this);

                return (null);
            }
        }
        else {
            return (super.getValue(aKey));
        }
    }

    /**
	 * Create a new Document with the specified name, physical resource, and size 0.
	 * 
	 * @see #createDocument(String, String, KnowledgeBase, long)
	 */
	public static Document createDocument(String name, String physicalResource, KnowledgeBase kb) {
		return createDocument(name, physicalResource, kb, 0);
	}

	/**
	 * Create a new Document with the specified name, physical resource, and size.
	 * 
	 * <p>
	 * The content type of the document is computed from the name of the document. If no useful
	 * content type can be determined (i.e. only {@link BinaryData#CONTENT_TYPE_OCTET_STREAM
	 * application/octet-stream}), nothing is stored.
	 * </p>
	 * 
	 * @see #createDocument(String, String, KnowledgeBase, long, String)
	 */
	public static Document createDocument(String name, String physicalResource, KnowledgeBase kb, long contentSize) {
		String contentType = MimeTypes.getInstance().getMimeType(name);
		if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(contentType)) {
			contentType = null;
		}
		return createDocument(name, physicalResource, kb, contentSize, contentType);
	}

	/**
	 * Create a new Document with the specified name and physical resource.
	 * 
	 * <p>
	 * This method only creates a KnowledgeObject. It is assumed that any entity represented by the
	 * physical resource is already available.
	 * </p>
	 * 
	 * @param name
	 *        the name to use for the email; must not be null
	 * @param physicalResource
	 *        the physical resource; must not be null
	 * @param kb
	 *        the KnowledgeBase in which to create the Email; must not be null
	 * @param contentSize
	 *        size of the content beyond a physical resource
	 * @param contentType
	 *        Content type of the document. May be <code>null</code>.
	 * @return the new Document wrapper; never null
	 * 
	 */
	public static Document createDocument(String name, String physicalResource, KnowledgeBase kb, long contentSize,
			String contentType) {
		Document theDocument = null;

		{
			KnowledgeObject theKO = kb.createKnowledgeObject(OBJECT_NAME);

			theKO.setAttributeValue(NAME_ATTRIBUTE, name);
			theKO.setAttributeValue(KOAttributes.PHYSICAL_RESOURCE, physicalResource);
			if (contentType != null) {
				theKO.setAttributeValue(DCMetaData.FORMAT, contentType);
			}

			theDocument = getInstance(theKO);
			theDocument.updateKOValues(contentSize);
		}

		return theDocument;
	}

    /**
     * Creates a wrapper for a Document with given ID.
     *
     * @param    anID    The ID of the Document.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
	public static Document getInstance(TLID anID) {
		return getInstance(getDefaultKnowledgeBase(), anID);
    }

    /**
     * Creates a wrapper for a Document with given ID.
     *
     * @param    anID    The ID of the Document.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
	public static Document getInstance(KnowledgeBase aBase, TLID anID) {
		final KnowledgeObject ko = aBase.getKnowledgeObject(OBJECT_NAME, anID);
		return getInstance(ko);
    }

    /**
     * Creates a wrapper for a Document with the given DataAccessProxy.
     *
     * @param    aProxy    The DataAccessProxy of the Document.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
	public static Document getInstance(DataAccessProxy aProxy) {
		return getInstance(getDefaultKnowledgeBase(), aProxy);
    }

    /**
     * Creates a wrapper for a Document with the given URL.
     *
     * @param    aProxy    The DataAccessProxy of the Document.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
	public static Document getInstance(KnowledgeBase aBase, DataAccessProxy aProxy) {
		KnowledgeObject ko =
			(KnowledgeObject) aBase.getObjectByAttribute(OBJECT_NAME, KOAttributes.PHYSICAL_RESOURCE, aProxy.getPath());
		return getInstance(ko);
    }

    /**
     * Returns the instance of the wrapper for the given KnowledgeObject.
     *
     * @param    anObject    The object to be wrapped.
     * @return   The wrapper for the object.
     * @throws   IllegalArgumentException    If the object is no Document.
     */
	public static Document getInstance(KnowledgeObject anObject) {
        return (Document) WrapperFactory.getWrapper(anObject);
    }

	/**
     * Return the size of this document.
	 */
	@Override
	public long getSize() {
		return tGetDataLongValue(SIZE);
	}

    /**
	 * Default security parent is the {@link WebFolder} which contains the document
	 * 
	 * @see com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper#getSecurityParent()
	 */
    @Override
	public BoundObject getSecurityParent() {
		return WebFolder.getWebFolder(this);
    }

	/**
	 * Returns the {@link DocumentVersion} corresponding to the content of this document.
	 */
	public DocumentVersion getDocumentVersion() {
		return DocumentVersion.getDocumentVersion(this, getVersionNumber());
    }

	/**
	 * Creates a list of {@link DocumentVersion} which represents the different versions of this
	 * {@link Document}. If this Document is historic the list only contains {@link DocumentVersion}
	 * representing documents up to this document.
	 * 
	 * @see DocumentVersion#getAllDocumentVersions(Document)
	 */
	public List<? extends DocumentVersion> getDocumentVersions() {
		final List<? extends DocumentVersion> allDocumentVersions = DocumentVersion.getAllDocumentVersions(this);
		if (HistoryUtils.getRevision(tHandle()).isCurrent()) {
			return allDocumentVersions;
		}
		final int version = getVersionNumber();

		int index = Collections.binarySearch(allDocumentVersions, null, new Comparator<DocumentVersion>() {

			@Override
			public int compare(DocumentVersion o1, DocumentVersion o2) {
				int revision1 = o1 == null ? version : o1.getRevision();
				int revision2 = o2 == null ? version : o2.getRevision();
				return revision2 - revision1;
			}
		});
		if (index >= 0) {
			return allDocumentVersions.subList(index, allDocumentVersions.size());
		} else {
			return allDocumentVersions.subList(-index - 1, allDocumentVersions.size());
		}
	}

	/**
	 * Get the document version with the given repository version string.
	 * 
	 * @param aRevision
	 *        a repository version string
	 * @return the document version with the given repository version string, <code>null</code>
	 *         otherwise
	 * 
	 * @deprecated use {@link DocumentVersion#getDocumentVersion(Document, Integer)}
	 */
    @Deprecated
	public DocumentVersion getVersion(String aRevision) {
		for (DocumentVersion dv : DocumentVersion.getAllDocumentVersions(this)) {
			if (StringServices.equals(dv.getVersion(), aRevision)) {
				return dv;
			}
		}
		return null;
	}

	@Override
	public InputStream getStream() throws IOException {
		InputStream resultStream;
		resultStream = getContent();

		if (resultStream == null) {
			throw noData(null);
		}
		return resultStream;
    }

	@Override
	public String getContentType() {

		try {
			KnowledgeObject theObject = this.tHandle();

			String storedContentType = (String) theObject.getAttributeValue(DCMetaData.FORMAT);
			if (storedContentType != null) {
				return storedContentType;
			}
		} catch (NoSuchAttributeException ex) {
			Logger.warn("Missing Dublin Core attribute in " + this, ex, Document.class);
		}

		String theType = null;
		{
			DataAccessProxy theDAP = this.getDAP();

			theType = MimeTypes.getInstance().getMimeType(theDAP);
		}

		// Cannot find correct mime type via DAP, try it via name.
		if (BinaryData.CONTENT_TYPE_OCTET_STREAM.equals(theType)) {
			theType = MimeTypes.getInstance().getMimeType(this.getName());
		}

		return theType;
	}

	private TopLogicException noData(Throwable cause) {
		if (cause == null) {
			return new TopLogicException(Document.class, "dataUnavailable(name)",
				new Object[] { getName() });
		} else {
			return new TopLogicException(Document.class, "dataUnavailable(name)",
				new Object[] { getName() }, cause);
		}
	}

}
