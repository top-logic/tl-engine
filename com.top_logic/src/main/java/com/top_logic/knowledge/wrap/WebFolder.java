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
import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.StringServices;
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
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.DCMetaData;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
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
     * Returns a string representation of this web folder.
     *
     * @return    A representation for debugging.
     */
    @Override
	protected String toStringValues() {
		String result = super.toStringValues();
		result = result + ", DSN: " + this.getDSN();

		return result;
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

		deleteFolderResource();
		tDelete();
		return true;
	}

	private boolean deleteContent(TLObject ownedContent, boolean force) {
		if (ownedContent instanceof WebFolder) {
			return ((WebFolder) ownedContent).delete(force);
		} else if (ownedContent instanceof Document) {
			return removeDocument((Document) ownedContent, force);
		} else {
			return false;
		}
	}

	private void deleteFolderResource() {
		try {
			KnowledgeObject folderKO = this.tHandle();
			// Note: In a versioned type, physical contents must not be deleted, because it is still
			// possible to access a historic version.
			if (!MetaObjectUtils.isVersioned(folderKO.tTable())) {
				String resourceName = (String) folderKO.getAttributeValue(KOAttributes.PHYSICAL_RESOURCE);
				if (!StringServices.isEmpty(resourceName)) {
					DataAccessProxy resource = new DataAccessProxy(resourceName);
					resource.delete(NOFORCE);
				}
			}
		} catch (Exception ex) {
			Logger.error("Physical deletion of folder '" + this + "' failed.", ex, this);
		}
	}

	/**
	 * Removes the given owned document from this folder.
	 * 
	 * @param ownedDocument
	 *        The document that is owned by this folder.
	 * @param force
	 *        Whether to break locks before removal.
	 */
	private boolean removeDocument(Document ownedDocument, boolean force) {
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
			return ownedDocument.delete(force);
		}
	}

	/**
	 * Retrieves the physical representation of this {@link WebFolder}. In contrast to
	 * {@link #getDAP()}, it will create a physical representation if it does not exist yet.
	 * Therefore this method must be called within transactions only.
	 * 
	 * @return physical representation of this {@link WebFolder}
	 */
	public DataAccessProxy createOrGetDAP() {
		if (!hasDAP()) {
			createDAP();
		}
		return getDAP();
	}

	/**
	 * true, if this {@link WebFolder} has an attached physical representation, false
	 *         otherwise
	 */
	public boolean hasDAP() {
		return resolveDAP() != null;
	}

	private void createDAP() {
		WebFolderFactory webFolderFactory = WebFolderFactory.getInstance();
		if (webFolderFactory.getCreateMode() == CreatePhysicalResource.DEFERRED) {
			this.setString(KOAttributes.PHYSICAL_RESOURCE, webFolderFactory.createUniqueFolderDSN());
		}
	}

	private Document createDocument(String aName, BinaryData content) {
		{
			DataAccessProxy theDAP = this.createOrGetDAP();

			if (!hasDAP()) { // At least show some reasonable message
				throw new NullPointerException("No Datasource in " + this);
			} else {
				DataAccessProxy theProxy = theDAP.getChildProxy(aName);
				String theDocDSN = theProxy.getPath();
				KnowledgeBase kBase = this.getKnowledgeBase();

				// Synchronized to avoid deadlocks with DBKB
				// See testWebFolder.testThreadUpload()
				synchronized (kBase) {
					Document theDocument = Document.createDocument(aName, theDocDSN, kBase);
					this.createLinkTo(theDocument, OWNER);

					if (!theDocument.update(content)) {
						theDocument.removeLocal();
						throw new WrapperRuntimeException("Unable to store new document physically");
					}
					return theDocument;
				}
			}
		}
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
		WebFolder subfolder;
		if (WebFolderFactory.getInstance().getCreateMode() == CreatePhysicalResource.IMMEDIATE) {
			subfolder = createFolder(getKnowledgeBase(), aName, getDSN());
		}
		else {
			subfolder = createRootFolderNoDSN(getKnowledgeBase(), aName);
		}
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

	/**
	 * Get the physical representation of this {@link WebFolder}.
	 * <p>
	 * <b>Note:</b> It must be ensured with a prior call of {@link #hasDAP()}, that this
	 * {@link WebFolder} has an attached physical resource. Within transactions
	 * {@link #createOrGetDAP()} can be called as alternative to this method.
	 * </p>
	 * 
	 * @return physical resource, never null
	 * 
	 * @see com.top_logic.knowledge.wrap.AbstractWrapper#getDAP()
	 */
	@Override
	public DataAccessProxy getDAP() {
		if (hasDAP()) {
			return resolveDAP();
		} else {
			throw new DataObjectException("Physical resource of wrapper is not present!");
		}
	}

	private DataAccessProxy resolveDAP() {
		return super.getDAP();
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
     * Create a KnowledgeObject of type WebFolder, which has the given
     * attributes and the access rights from this folder.
     *
     * @param    aName    The name of the WebFolder.
     * @param    anDSN    The DSN to the WebFolder.
     * @return   The new created WebFolder.
     */
	protected TLObject createWebFolderKO(String aName, String anDSN) {
		return this.createKO(this.getKnowledgeBase(), OBJECT_NAME, aName, anDSN);
    }

	/**
	 * Create a document entry in the data source represented by this WebFolder.
	 * 
	 * If the creation fails, the system expects, that the document already exists and tries to put
	 * the content into the existing document.
	 * 
	 * @param aName
	 *        The name of the document.
	 * @param aStream
	 *        The stream to be used for filling the data source. Must not be <code>null</code>.
	 * @return The DSN of the document or null, if creation or update failes.
	 */
    protected DataAccessProxy createDocumentDAP(
        String aName,
			InputStream aStream) {
        DataAccessProxy theDocument = null;
        String theDSN = this.getDSN();

        try {
            DataAccessProxy theProxy;

            theDocument = new DataAccessProxy(theDSN, aName);

            if (!theDocument.exists()) {
                if (Logger.isDebugEnabled(this)) {
                    Logger.debug("Document " + aName + " doesn't exists, creating!", this);
                }

                theProxy = new DataAccessProxy(theDSN);
                theDocument = new DataAccessProxy(theProxy.createEntry(aName, aStream));
            }
            else {
                if (Logger.isDebugEnabled(this)) {
                    Logger.debug("Document " + aName + " exists, updating!", this);
                }

                theDocument.putEntry(aStream);
            }
        }
        catch (Exception ex) {
            String theMessage =
                "Unable to create new entry with name '"
                    + aName
                    + "' in data source '"
                    + theDSN
                    + "'!";

            Logger.info(theMessage, ex, this);
        }

        return (theDocument);
    }

    /**
     * Create a KnowledgeObject of given type, which has the given
     * attributes and the access rights from this folder.
     *
     * @param    aType    The type of the new KnowledgeObject.
     * @param    aName    The name of the Document.
     * @param    aDSN     The DSN for the WebFolder
     * @return   The new created KnowledgeObject.
     */
	protected TLObject createKO(
        KnowledgeBase aBase,
			String aType, String aName, String aDSN) {

		TLObject theObject = WebFolder.createFolder(aBase, aType, aName, aDSN);
 
        if (Logger.isDebugEnabled(this)) {
            Logger.debug(
                "Created KnowledgeObject of type '" 
                + aType + "' with DSN '" + aDSN + "'!", this);
        }

		return theObject;
    }

    /**
     * Search for the WebFolder with the given DSN in the knowledge base.
     *
     * @param    aDSN  The DataSourceName of the folder (e.g. "file://folder")
     * @return   The found folder or null, if nothing found.
     */
	public static WebFolder findFolderByDSN(KnowledgeBase aBase, String aDSN) {
        KnowledgeObject theKO = (KnowledgeObject) aBase.getObjectByAttribute(OBJECT_NAME, KOAttributes.PHYSICAL_RESOURCE, aDSN);
		return (WebFolder) WrapperFactory.getWrapper(theKO);
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
     * Creates a new WebFolder including the KO and the DS-Entry.
     *
     * Use one of the <code>getInstance()</code> methods in case
     * you want to check for existance first.
     *
     * #deprecated The createFolder methods are dangerous and should not
     * be used. getChildFolder is no real replacement yet.
     *
     * @param    aDSN    The complete ds-name of the new webfolder
     */
	public static WebFolder createRootFolder(String aDSN) {
		return WebFolder.createRootFolder(getDefaultKnowledgeBase(), aDSN);
    }

    /**
     * Creates a new WebFolder including the KO and the DS-Entry.
     * 
     * Use one of the <code>getInstance()</code> methods in case 
     * you want to check for existance first.
     *
     * @param    aBase the KnowledgeBase used to create the Objects in.
     * @param    aDSN  The complete ds-name of the new webfolder.
     */
	public static WebFolder createRootFolder(KnowledgeBase aBase, String aDSN) {
		if (aBase == null) {
		    throw new NullPointerException ("aKB");
		}
		else if (aDSN == null) {
		    throw new NullPointerException ("aDSN");
		}
		
		{
		    DataAccessProxy theDSA = new DataAccessProxy(aDSN);
		
			return WebFolder.createFolder(aBase, WebFolder.OBJECT_NAME, theDSA.getName(), aDSN);
		} 
    }

    /**
	 * Creates a new WebFolder including the KO and a name.
	 * 
	 * Use one of the <code>getInstance()</code> methods in case you want to check for existence
	 * first.
	 * 
	 * @param aBase
	 *        the KnowledgeBase used to create the Objects in.
	 * @param aName
	 *        The name of the new webfolder.
	 */
	public static WebFolder createRootFolderNoDSN(KnowledgeBase aBase, String aName) {
		if (aBase == null) {
		    throw new NullPointerException ("aKB");
		}
		else if (aName == null) {
			throw new NullPointerException("aName");
		}
		
		return WebFolder.createFolder(aBase, WebFolder.OBJECT_NAME, aName, null);
    }

    /**
     * Creates a new wrapper for a WebFolder.
     *
     * #deprecated The createFolder methods are dangerous and should not
     * be used. getChildFolder is no real replacement yet.
     * 
     * @param    aName    The name of the folder to be created.
     * @param    aBase    The base within the data source to be used.
     */
	public static WebFolder createFolder(String aName, String aBase) {
        return WebFolder.createFolder(WebFolder.getDefaultKnowledgeBase(), aName, aBase);
    }

    /**
     * Creates a new wrapper for a WebFolder.
     *
     * @param    aName    The name of the folder to be created.
     * @param    aBase    The base within the data source to be used (DSN).
     *
     * #deprecated The createFolder methods are dangerous and should not
     * be used. getChildFolder is no real replacement yet.
     */
	public static WebFolder createFolder(KnowledgeBase aKBase, String aName, String aBase) {
		return (WebFolder) WebFolder._createKO(aKBase, aName, aBase);
    }

    /**
     * Creates a wrapper for a WebFolder with the given DSN.
     *
     * @param    aProxy    The DataAccessProxy of the WebFolder.
     */
	public static WebFolder getInstance(DataAccessProxy aProxy) {
        return WebFolder.getInstance(WebFolder.getDefaultKnowledgeBase(), aProxy);
    }

    /**
     * Creates a wrapper for a WebFolder with the given DSN.
     *
     * @param    aBase     The knowledge base to be used for finding object.
     * @param    aProxy    The DataAccessProxy of the WebFolder.
     */
	public static WebFolder getInstance(KnowledgeBase aBase, DataAccessProxy aProxy) {
        KnowledgeObject theKO = (KnowledgeObject) aBase.getObjectByAttribute(OBJECT_NAME, KOAttributes.PHYSICAL_RESOURCE, aProxy.getPath());

        return (theKO != null) ? (WebFolder) WrapperFactory.getWrapper(theKO) : null;
    }

    /**
     * Create a KnowledgeObject with with our type in the specified
     * KnowledgeBase with a certain name and parent DSN.
     *
     * @param    aKB      The KnowledgeBase, must not be <code>null</code>
     * @param    aName    The name, must not be <code>null</code>
     * @param    aBase    The parent DSN, must not be <code>null</code>
     * @return   The KnowledgeObject, never <code>null</code>
     */
	private static TLObject _createKO(KnowledgeBase aKB, String aName, String aBase) {
        String theDSN = WebFolder.createDSN(aName, aBase);

		return WebFolder.createFolder(aKB, OBJECT_NAME, aName, theDSN);
    }

	/**
	 * Create a KnowledgeObject of given type, which has the given attributes.
	 * <p>
	 * This method was extracted from the corresponding instance method createKO.
	 * </p>
	 * 
	 * @param aKB
	 *        The KnowledgeBase to use
	 * @param aType
	 *        The type of the new KnowledgeObject.
	 * @param aName
	 *        The name of the Document.
	 * @param aDSN
	 *        The DSN for the WebFolder
	 * 
	 * @return The new created KnowledgeObject.
	 */
	private static WebFolder createFolder(KnowledgeBase aKB, String aType, String aName, String aDSN)
	{
		KnowledgeObject theObject;
		{
			theObject = aKB.createKnowledgeObject(aType);

			theObject.setAttributeValue(NAME_ATTRIBUTE, aName);
			theObject.setAttributeValue(KOAttributes.PHYSICAL_RESOURCE, aDSN);
			if (MetaObjectUtils.hasAttribute(theObject.tTable(), DCMetaData.TITLE)) {
				// Better use Datasource to find out about this, mmh.
				String mimeType = MimeTypes.getInstance().getMimeType(aName);

				theObject.setAttributeValue(DCMetaData.TITLE, aName);
				if (mimeType != null) {
					theObject.setAttributeValue(DCMetaData.FORMAT, mimeType);
				}
			}
		}

		return (WebFolder) WrapperFactory.getWrapper(theObject);
    }

    /**
     * Create a unique DSN which is currently not used for the given name.
     *
     * The method first tries to create a folder in the default data source
     * with the given name. If there is already a folder with the name or
     * the name is not allowed in the data source, the method creates another
     * unique name.
     *
     * @param    aName The desired name of the folder.
     * @param	  aBase baseName (of parent Folder)
     *
     * @return   The unique DSN for the folder.
     */
	protected static String createDSN(String aName, String aBase) {
		{
            DataAccessProxy theParent = new DataAccessProxy(aBase);
            aName   = theParent.createNewEntryName(aName, null);
            return  theParent.createContainer(aName);
        } 
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

	@Override
	protected void handleDelete() {
		super.handleDelete();

		deleteFolderResource();
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

	private static TopLogicException errorCreateFailed(Throwable cause) {
		return new TopLogicException(WebFolder.class, "createFolder", cause);
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
	 * If there is already a document in the data source, which has the given name, it'll be updated
	 * with the content in the given stream.
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
	 * If there is already a document in the data source, which has the given name, it'll be updated
	 * with the content in the given stream.
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
