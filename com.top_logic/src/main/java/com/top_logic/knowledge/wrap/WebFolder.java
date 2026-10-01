/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import static com.top_logic.knowledge.wrap.WebFolder.LinkType.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Named;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Wrapper for {@link com.top_logic.knowledge.objects.KnowledgeObject KnowledgeObjects} of type
 * WebFolder.
 * 
 * <p>
 * A WebFolder is an object, which acts as a container for KnowledgeObjects. This container can hold
 * every KnowledgeObject in it. The associations between the folder (the parent) and the held
 * objects (the children) is a "{@link #CONTENTS_ASSOCIATION}" relation from the parent to the
 * child.
 * </p>
 * 
 * <p>
 * If the type (the "{@link #LINK_NAME linkType}") of the association is empty, the held object will
 * be removed, when it's been removed from the WebFolder, otherwise the wrapper will only remove the
 * association. This behavior is similar to the soft links in Unix file systems.
 * </p>
 * 
 * <p>
 * To append an object as link to this folder, use the {@link #add(TLObject) add} method, that will
 * do the correct handling automatically.
 * </p>
 * 
 * <p>
 * A folder is a plain object: its documents store their content themselves, a folder is not
 * backed by a directory of a data source. Folders and documents can be renamed.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class WebFolder extends AbstractContainerWrapper implements FolderDefinition {

	/** The type of KO wrapped by this class */
    public static final String OBJECT_NAME = "WebFolder";

	/** Full qualified name of the {@link TLType} of a {@link WebFolder}. */
	public static final String WEB_FOLDER_TYPE = "tl.folder:WebFolder";

	/**
	 * Resolves {@link #WEB_FOLDER_TYPE}.
	 * 
	 * @implNote Casts result of {@link TLModelUtil#resolveQualifiedName(String)} to
	 *           {@link TLStructuredType}. Potential {@link ConfigurationException} are wrapped into
	 *           {@link ConfigurationError}.
	 * 
	 * @return The {@link TLStructuredType} representing the {@link WebFolder}s.
	 * 
	 * @throws ConfigurationError
	 *         iff {@link #WEB_FOLDER_TYPE} could not be resolved.
	 */
	public static TLStructuredType getWebFolderType() throws ConfigurationError {
		return (TLStructuredType) TLModelUtil.resolveQualifiedName(WEB_FOLDER_TYPE);
	}

	/**
	 * Values of the {@link WebFolder#LINK_NAME} attribute.
     * 
     * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
     */
	public enum LinkType {
		
		/**
		 * Indicates ownership, implemented in the database as <code>null</code>.
		 */
		OWNER {
			@Override
			public boolean isLink() {
				return false;
			}

			@Override
			public String storageValue() {
				return null;
			}
		},

		/**
		 * Link to a contents of a foreign folder.
		 */
		SOFT_LINK {
			@Override
			public boolean isLink() {
				return true;
			}

			@Override
			public String storageValue() {
				return "SoftLink";
			}
		};

		/**
		 * Whether the content is linked or owned.
		 * 
		 * @return <code>true</code>, if the content is linked.
		 */
		public abstract boolean isLink();
		
		/**
		 * Value stored in the database in the {@link WebFolder#LINK_NAME} attribute to encode this {@link LinkType}.
		 */
		public abstract String storageValue();
		
		private static final Map<String, LinkType> BY_STORAGE_VALUE = 
			new MapBuilder<String, LinkType>()
				.put(OWNER.storageValue(), OWNER)
				.put("HardLink", OWNER) // For compatibility with pre 5.7 data sets.
				.put(SOFT_LINK.storageValue(), SOFT_LINK)
				.toMap();
		
		static LinkType byStorageValue(String storageValue) {
			return BY_STORAGE_VALUE.get(storageValue);
		}
	}

	/** Association between {@link WebFolder} and its contents. */
	public static final String CONTENTS_ASSOCIATION = "hasFolderContent";

	/**
	 * Attribute name holding the link type.
	 */
	public static final String LINK_NAME = "linkType";

	/** Name of attribute to hold the folderType. */
	public static final String FOLDER_TYPE = "folderType";

	/** Description of the {@link WebFolder} */
	public static final String DESCRIPTION = "description";

	private static final AssociationSetQuery<KnowledgeAssociation> CHILDREN =
		AssociationQuery.createOutgoingQuery("allContent", CONTENTS_ASSOCIATION);

	private static final AssociationSetQuery<KnowledgeAssociation> OWNED_CONTENT =
		AssociationQuery.createOutgoingQuery("ownedContent", CONTENTS_ASSOCIATION,
			Collections.singletonMap(LINK_NAME, null));
	
	private static final AssociationSetQuery<KnowledgeAssociation> OWNERS =
		AssociationQuery.createIncomingQuery("owners", WebFolder.CONTENTS_ASSOCIATION,
			Collections.singletonMap(WebFolder.LINK_NAME, null));

	private static final boolean NOFORCE = false;

	private static final boolean FORCE = true;

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Use always the getInstance() method of the wrappers.
     *  
     * @param    ko        The KnowledgeObject, must never be <code>null</code>.
     * 
     * @throws   NullPointerException  If the KO is <code>null</code>.
     */
	public WebFolder(KnowledgeObject ko) {
        super(ko);
    }

	/**
	 * Checks, whether the folder has no children (including linked ones).
	 * 
	 * @return true, if folder has children.
	 */
    @Override
	public boolean isEmpty() {
		return getContentLinks().isEmpty();
    }

	/**
	 * Create a new association between the folder and the given object.
	 * 
	 * @param newChild
	 *        The new object for the folder.
	 * @return true, if appending the object to folder succeeds.
	 */
    @Override
	protected boolean _add(TLObject newChild) {
		createLinkTo(newChild, SOFT_LINK);
		return true;
    }

	@Override
	public void clear() {
		for (KnowledgeAssociation link : getContentLinks()) {
			unlink(link);
		}
	}

    /**
     * Removes the association between the folder and the given object.
     *
     * If the link type of the association is not set, the object to be
     * removed is a real child from the folder, so it has to be removed
     * as well (depending on its type). If there is a link type defined,
     * the object is only linked to this folder and is localized somewhere
     * else, this folder is not responsible for the deletion of it.
     *
     * @param    oldChild    The object to be removed from the folder.
     * @return   true, if removing the object from folder succeeds.
     */
    @Override
	protected boolean _remove(TLObject oldChild) {
		boolean result = true;
		Iterator<KnowledgeAssociation> links = getLinksTo(oldChild);
		while (links.hasNext()) {
			KnowledgeAssociation link = links.next();
			result &= unlink(link);
		}
		return result;
	}

	/**
	 * Unlinks the object pointed to by the given link.
	 * 
	 * <p>
	 * If the link points to owned content, the content is deleted, if no longer owned by another
	 * container.
	 * </p>
	 */
	private boolean unlink(KnowledgeAssociation link) {
		LinkType linkType = getLinkType(link);
		if (linkType.isLink()) {
			// Remove only the link to the linked object.
			link.delete();
			return true;
		} else {
			// No link type, the referenced object is a true part of this folder. The part must
			// be removed as well.
			try {
				TLObject ownedContent = WrapperFactory.getWrapper(link.getDestinationObject());
				return this.deleteContent(ownedContent, NOFORCE);
			} catch (InvalidLinkException ex) {
				throw errorDeleteFailed(ex);
			}
		}
	}

	private boolean delete(boolean force) {
		if (force) {
			boolean result = true;
			for (TLObject ownedContent : this.getOwnedContent()) {
				result &= this.deleteContent(ownedContent, force);
			}
			if (!result) {
				return false;
			}
		} else {
			if (!this.isEmpty()) {
				throw errorDeleteNonEmpty();
			}
		}

		tDelete();
		return true;
	}

	private boolean deleteContent(TLObject ownedContent, boolean force) {
		if (ownedContent instanceof WebFolder) {
			return ((WebFolder) ownedContent).delete(force);
		} else if (ownedContent instanceof Document) {
			return removeDocument((Document) ownedContent);
		} else {
			return false;
		}
	}

	/**
	 * Removes the given owned document from this folder.
	 * 
	 * @param ownedDocument
	 *        The document that is owned by this folder.
	 */
	private boolean removeDocument(Document ownedDocument) {
		Iterator<KnowledgeAssociation> links = getLinksTo(ownedDocument);
		
		assert links.hasNext() : "Document in no relation to folder, from which it should be removed";
		
		Object currentFolderKey = KBUtils.getWrappedObjectKey(this);

		boolean stillOwned = false;
		while (links.hasNext()) {
			KnowledgeAssociation link = links.next();
			boolean ownLink = link.getSourceIdentity().equals(currentFolderKey);
			if (ownLink) {
				link.delete();
			} else {
				stillOwned |= !getLinkType(link).isLink();
			}

		}
		
		if (stillOwned) {
			// Document keeps alive.
			return true;
		} else {
			return ownedDocument.delete();
		}
	}

	private Document createDocument(String aName, BinaryData content) {
		KnowledgeBase kBase = this.getKnowledgeBase();
		Document theDocument = Document.createDocument(aName, kBase);
		this.createLinkTo(theDocument, OWNER);
		theDocument.update(content);
		return theDocument;
	}

	/**
	 * Return the owning object of this folder.
	 * 
	 * @return The owner of this folder or <code>null</code>, if no owner exists. If this folder is
	 *         owned by more than one container, an arbitrary one is returned.
	 * 
	 * @see #getOwners()
	 */
	public TLObject getOwner() {
		return CollectionUtil.getFirst(getOwners());
    }

	/**
	 * All objects that own this folder.
	 */
	public Set<? extends TLObject> getOwners() {
		return getOwners(this);
	}

	static LinkType getLinkType(KnowledgeAssociation link) {
		try {
			String linkLabel = (String) link.getAttributeValue(WebFolder.LINK_NAME);
			return LinkType.byStorageValue(linkLabel);
		} catch (NoSuchAttributeException ex) {
			throw new UnreachableAssertion("Missing link attribute.", ex);
		}
	}

    @Override
	public boolean isLinkedContent(Named content) {
		if (content == null) {
			return false;
		}
		Iterator<KnowledgeAssociation> iter = getLinksTo((TLObject) content);
		while (iter.hasNext()) {
			KnowledgeAssociation link = iter.next();
			LinkType linkType = getLinkType(link);
			if (linkType.isLink()) {
				return true;
			}
		}
		return false;
    }
    
    /** 
     * Creates a new Subfolder in the receiver with name aName.
     * 
     * the new Subfolder is added as a child to the receiver
     */
	public WebFolder createSubFolder(String aName) {
		WebFolder subfolder = createFolder(getKnowledgeBase(), aName);
		subfolder.setFolderType(WebFolderFactory.SUB_FOLDER);
		createLinkTo(subfolder, OWNER);
        return subfolder;
    }
    
	/**
     * Set the name of the folder.
     *
     * @param aName   the new name of the folder.
     */
	@Override
	public void setName(String aName) {
		this.tSetData(NAME_ATTRIBUTE, aName);
    }

	/**
	 * Setter for {@link #getDescription()}.
	 */
	public void setDescription(String description) {
		tSetDataString(DESCRIPTION, description);
	}

	/**
	 * Sets the type of the folder to aFolderType.
	 * 
	 * @param aFolderType
	 *        the type of the folder
	 */
	public void setFolderType(String aFolderType) {
		this.tSetData(FOLDER_TYPE, aFolderType);
	}

	/**
	 * The description of the {@link WebFolder}.
	 */
	public String getDescription() {
		return (tGetDataString(DESCRIPTION));
	}

	/**
	 * TODO FMA Folder types can be / should be / are ???
	 * 
	 * @return the foldertype
	 */
	public String getFolderType() {
		return (String) this.getValue(FOLDER_TYPE);
	}

	private Iterator<KnowledgeAssociation> getLinksTo(TLObject child) {
		{
			KnowledgeObject childKO = (KnowledgeObject) child.tHandle();
			KnowledgeObject folderKO = tHandle();
			return folderKO.getOutgoingAssociations(CONTENTS_ASSOCIATION, childKO);
		}
	}

	private KnowledgeAssociation createLinkTo(TLObject newChild, LinkType linkType) {
		{
			KnowledgeBase kb = this.getKnowledgeBase();
			KnowledgeObject thisKO = tHandle();
			KnowledgeObject newChildKO = (KnowledgeObject) newChild.tHandle();

			KnowledgeAssociation link = kb.createAssociation(thisKO, newChildKO, CONTENTS_ASSOCIATION);
			link.setAttributeValue(LINK_NAME, linkType.storageValue());

			return link;
		}
	}

    /**
     * Creates a wrapper for a WebFolder with given ID.
     *
     * @param    anID    The ID of the WebFolder.
     */
	public static WebFolder getInstance(TLID anID) {
		return getInstance(PersistencyLayer.getKnowledgeBase(), anID);
    }

    /**
     * Creates a wrapper for a WebFolder with given ID.
     *
     * @param    aBase    The knowledge base to be used for finding object.
     * @param    anID     The ID of the WebFolder.
     */
	public static WebFolder getInstance(KnowledgeBase aBase, TLID anID) {
		return (WebFolder) WrapperFactory.getWrapper(anID, OBJECT_NAME, aBase);
    }

	/**
	 * Creates a new {@link WebFolder} with the given name.
	 * 
	 * <p>
	 * Use one of the <code>getInstance()</code> methods in case you want to check for existence
	 * first.
	 * </p>
	 * 
	 * @param kb
	 *        the KnowledgeBase used to create the folder in.
	 * @param name
	 *        The name of the new folder.
	 */
	public static WebFolder createFolder(KnowledgeBase kb, String name) {
		if (kb == null) {
			throw new NullPointerException("kb");
		}
		if (name == null) {
			throw new NullPointerException("name");
		}

		KnowledgeObject theObject = kb.createKnowledgeObject(OBJECT_NAME);
		theObject.setAttributeValue(NAME_ATTRIBUTE, name);
		if (MetaObjectUtils.hasAttribute(theObject.tTable(), DCMetaData.TITLE)) {
			theObject.setAttributeValue(DCMetaData.TITLE, name);
		}
		return (WebFolder) WrapperFactory.getWrapper(theObject);
	}

	/**
	 * Recursively copies all contents from the given source folder to this folder.
	 * 
	 * @see #copyContents(WebFolder, WebFolder, boolean, boolean)
	 */
	public void copyDocsRecusiveFrom(WebFolder aSource, boolean useVersionLinks)
			throws DataObjectException {
		WebFolder.copyContents(aSource, this, false, useVersionLinks);
    }

	/**
	 * Recursively copies all contents from the given source folder to the given destination folder.
	 * 
	 * @param source
	 *        The source folder.
	 * @param destination
	 *        The destination folder.
	 * @param useSoftLinks
	 *        Whether direct contents in the source folder should be replaced with soft links.
	 * @param useVersionLinks
	 *        Whether documents in the source folder should be replaced with their respective
	 *        current versions in the destination folder.
	 */
	public static void copyContents(WebFolder source, WebFolder destination, boolean useSoftLinks,
			boolean useVersionLinks) throws DataObjectException {
		for (KnowledgeAssociation link : source.getContentLinks()) {
			TLObject content = WrapperFactory.getWrapper(link.getDestinationObject());
			if (WebFolder.getLinkType(link).isLink()) {
				if (content instanceof Document) {
					Document linkedDocument = (Document) content;
					destination.addDocumentLink(linkedDocument, SOFT_LINK, useVersionLinks);
				} else {
					destination.add(content);
				}
			} else {
				if (content instanceof Document) {
					Document sourceDocument = (Document) content;
					destination.addDocumentLink(sourceDocument, useSoftLinks ? SOFT_LINK : OWNER, useVersionLinks);
				} else if (content instanceof WebFolder) {
					WebFolder sourceSubFolder = (WebFolder) content;

					WebFolder destinationSubFolder = destination.createSubFolder(sourceSubFolder.getName());
					WebFolder.copyContents(sourceSubFolder, destinationSubFolder, useSoftLinks, useVersionLinks);
				}
			}
		}
	}

	private void addDocumentLink(Document aDocument, LinkType aLinkType, boolean useVersion) {
		if (useVersion) {
            DocumentVersion version = aDocument.getDocumentVersion();
			if (version != null) {
				createLinkTo(version, aLinkType);
			}
		} else {
			createLinkTo(aDocument, aLinkType);
        }
    }

	/**
	 * Removes the given {@link WebFolder} and all contained elements.
	 * 
	 * @param folder
	 *        the folder to delete.
	 * @return <code>true</code> iff removing all contained elements and the
	 *         given folder succeeded
	 */
	public static boolean deleteRecursively(WebFolder folder) {
		return folder.delete(FORCE);
	}

	@Override
	protected Modification notifyUpcomingDeletion() {
		Modification result = super.notifyUpcomingDeletion();

		Set<? extends TLObject> content = getOwnedContent();
		if (content.isEmpty()) {
			return result;
		}

		List<? extends TLObject> toBeDeleted = new ArrayList<>(content);
		return result.andThen(() -> {
			for (TLObject ownedContent : toBeDeleted) {
				ownedContent.tDelete();
			}
		});
	}

	/**
	 * The owning {@link ContainerWrapper} of the given document, or <code>null</code>, if the given
	 * document is not owned by any container.
	 * 
	 * @return If more than one container is owning the given document, an arbitrary one is
	 *         returned.
	 */
	public static ContainerWrapper getContainer(Document document) {
		for (TLObject container : getOwners(document)) {
			if (container instanceof ContainerWrapper) {
				return (ContainerWrapper) container;
			}
		}
		return null;
	}

	/**
	 * This method gets all sources of a child relation to the given object; note, that such
	 * relations not only result from web folders, but may also be a result of a relation in a
	 * structure relation!
	 * 
	 * @return never NULL
	 */
	private static Set<? extends TLObject> getOwners(AbstractWrapper contents) {
		if (contents == null) {
			return Collections.emptySet();
		} else {
			return contents.resolveWrappers(OWNERS);
		}
	}

	/**
	 * All links that point to the {@link #getContent()} objects.
	 */
	protected Set<KnowledgeAssociation> getContentLinks() {
		return resolveLinks(WebFolder.CHILDREN);
	}

	@Override
	public Collection<? extends TLObject> getContent() {
		return resolveWrappers(CHILDREN);
	}

	/**
	 * All {@link #getContent()} that is owned by this {@link WebFolder}.
	 * 
	 * <p>
	 * An object is owned, if it is not linked to the folder.
	 * </p>
	 */
	public Set<? extends TLObject> getOwnedContent() {
		return resolveWrappers(OWNED_CONTENT);
	}

	private static TopLogicException errorDeleteFailed(Throwable cause) {
		return new TopLogicException(WebFolder.class, "deleteFailed", cause);
	}

	private static TopLogicException errorDeleteNonEmpty() {
		return new TopLogicException(WebFolder.class, "deleteNonEmpty");
	}

	/**
	 * The owning {@link WebFolder} of the given document, or <code>null</code>, if the given
	 * document is not owned by any folder.
	 * 
	 * @return If more than one folder is owning the given document, an arbitrary one is returned.
	 * 
	 * @deprecated Result is not well-defined. The folder must be known from the context. Use {{@link WebFolder#getContainer(Document)}.
	 */
	@Deprecated
	public static WebFolder getWebFolder(Document document) {
		for (TLObject container : getOwners(document)) {
			if (container instanceof WebFolder) {
				return (WebFolder) container;
			}
		}
		return null;
	}

	/**
	 * Finds a folder with the given name in this folder and creates a new one, if none does exists.
	 * 
	 * @param aName
	 *        The name of the folder to be found or created.
	 * @return A folder with the given name.
	 * 
	 * @deprecated Result is undefined, if names are not unique.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public WebFolder getOrCreateChildFolder(String aName) {
		TLObject child = this.getChildByName(aName);
		if (child == null) {
			return this.createSubFolder(aName);
		} else {
			return (WebFolder) child;
		}
	}

	/**
	 * Create a new Document with the given content in this folder.
	 * 
	 * <p>
	 * If there is already a document in this folder, which has the given name, it'll be updated
	 * with the given content.
	 * </p>
	 * 
	 * @param aName
	 *        The name of the document.
	 * @param aStream
	 *        The stream containing the data for the document.
	 * @return The newly created Document
	 * 
	 * @deprecated Result is undefined, if names are not unique.
	 */
	@Deprecated
	public Document createOrUpdateDocument(String aName, InputStream aStream) {
		try {
			try {
				return createOrUpdateDocument(aName, BinaryDataFactory.createFileBasedBinaryData(aStream));
			} finally {
				aStream.close();
			}
		} catch (IOException ex) {
			throw new DataObjectException("Unable access input stream", ex);
		}
	}

	/**
	 * Create a new Document with the given content in this folder.
	 * 
	 * <p>
	 * If there is already a document in this folder, which has the given name, it'll be updated
	 * with the given content.
	 * </p>
	 * 
	 * @param aName
	 *        The name of the document.
	 * @param data
	 *        The stream containing the data for the document.
	 * @return The newly created Document
	 * 
	 * @deprecated Result is undefined, if names are not unique.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public Document createOrUpdateDocument(String aName, BinaryData data) {
		Document theDocument = (Document) this.getChildByName(aName);
		if (theDocument != null) {
			theDocument.update(data);
			return theDocument;
		} else { // Create a new Document
			return createDocument(aName, data);
		}
	}

	@Override
	public Collection<Named> getContents() {
		return new ArrayList(getContent());
	}

}
