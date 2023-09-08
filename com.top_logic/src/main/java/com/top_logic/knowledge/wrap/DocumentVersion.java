/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;
import com.top_logic.util.model.ModelService;

/**
 * This class represents versions of a document
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class DocumentVersion extends AbstractBoundWrapper {

	/** The name of the {@link TLModule} of this type. */
	public static final String TL_FOLDER_MODULE = "tl.folder";

	/**
	 * The name of the {@link MOAttribute} to access the corresponding {@link Document}.
	 */
	private static final String DOCUMENT_ATTRIBUTE = "doc";

    public static final String DOC_ID="docID";

	/**
	 * all DocumentVersions of a Document have a revision number. The revision number is allways
	 * increased by one if a new DocumentVersion for a Document is created
	 */
    private static final String REVISION="revision";   

	/** See {@link #getDescription()} */
	public static final String DESCRIPTION = "description";

	/** Name of {@link #tTable()} of a {@link DocumentVersion}. */
	public static final String OBJECT_NAME = "DocumentVersion";

	private static final Comparator<DocumentVersion> REVISION_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(DocumentVersion o1, DocumentVersion o2) {
			return o2.getRevision() - o1.getRevision();
		}
	};


	public DocumentVersion(KnowledgeObject ko) {
        super(ko);
    }

	/**
	 * Create a new DocumentVersion with the specified documentID and version.
	 * @param document
	 *        the document of this version, must not be <code>null</code>
	 * 
	 * @return the new DocumentVersion;
	 * 
	 */
	static DocumentVersion createDocumentVersion(Document document) {
		DocumentVersion theVersion = null;

		{
			KnowledgeObject theKO = getDefaultKnowledgeBase().createKnowledgeObject(OBJECT_NAME);

			theKO.setAttributeValue(REVISION, document.getVersionNumber());
			theKO.setAttributeValue(DOCUMENT_ATTRIBUTE, document.tHandle());

			theVersion = (DocumentVersion) WrapperFactory.getWrapper(theKO);
		}

		return theVersion;
    }

	/**
	 * Returns a List with all {@link DocumentVersion} belonging to the given {@link Document}. If
	 * the document is historic the returned list contains also {@link DocumentVersion}s of later
	 * versions of the given {@link Document}.
	 * 
	 * The List is ordered by the revision-attribute of the {@link DocumentVersion}
	 * 
	 * @return a list with all document Versions belonging to the given {@link Document}
	 */
	public static List<? extends DocumentVersion> getAllDocumentVersions(Document doc) {
		KnowledgeBase kb = doc.getKnowledgeBase();
		MetaObject documentVersionType = kb.getMORepository().getMetaObject(OBJECT_NAME);

		SimpleQuery<DocumentVersion> query = SimpleQuery.queryResolved(DocumentVersion.class, documentVersionType,
			eqBinaryLiteral(
				reference(documentVersionType.getName(), DOCUMENT_ATTRIBUTE, ReferencePart.name),
				doc.tHandle().tId().getObjectName()));

		List<DocumentVersion> result = kb.compileSimpleQuery(query).search();

		Collections.sort(result, REVISION_COMPARATOR);
		return result;
    }
    
	/**
     * returns the document this version belongs to
     * 
     * @return the document of this version
     */
    public Document getDocument(){
		return tGetDataReference(Document.class, DOCUMENT_ATTRIBUTE);
    }
    
    /**
     * the revision number of this DocumentVersion
     */
    public int getRevision(){
        return getInteger(REVISION).intValue();
    }

	/**
	 * The description of the {@link DocumentVersion}.
	 */
	public String getDescription() {
		return (tGetDataString(DESCRIPTION));
	}

	/**
	 * Setter for {@link #getDescription()}.
	 */
	public void setDescription(String description) {
		tSetDataString(DESCRIPTION, description);
	}

	/**
	 * The version of this {@link DocumentVersion}. The same value is used in the repository.
	 * 
	 * @deprecated just returns {@link #getRevision()} as String
	 */
    @Deprecated
    public String getVersion(){
		return String.valueOf(getRevision());
    }

	/**
	 * The content of the represented {@link Document} as stream.
	 * 
	 * @see Document#getStream()
	 */
	public InputStream getStream() throws IOException {
		return getDocument().getStream();
	}

	/**
	 * the version string of this DocumentVersion
	 * 
	 * @deprecated just returns {@link #getRevision()} as String
	 */
    @Deprecated
    public String getVersionString(){
		return String.valueOf(getRevision());
    }

	/**
	 * @param document
	 *        the document to use
	 * @param versionNumber
	 *        the version number to look for
	 * 
	 * @return the {@link DocumentVersion} belonging to the given <code>document</code> and
	 *         <code>versionNumber</code> or <code>null</code> if no such {@link DocumentVersion}
	 *         exists
	 */
	public static DocumentVersion getDocumentVersion(Document document, Integer versionNumber) {
		if (document == null || versionNumber == null) {
			return null;
		}
		int version = versionNumber.intValue();
		for (DocumentVersion docVersion : getAllDocumentVersions(document)) {
			if (docVersion.getRevision() == version) {
				return docVersion;
            }
        }
        return null;
    }
    
    /**
     * the name of the underlying document
     */
    @Override
	public String getName() {
        return getDocument().getName();
    }

	/** The {@link TLClass} corresponding to this Java class. */
	public static TLClass getDocumentVersionType() {
		return (TLClass) ModelService.getApplicationModel()
			.getModule(DocumentVersion.TL_FOLDER_MODULE)
			.getType(DocumentVersion.OBJECT_NAME);
	}

}
