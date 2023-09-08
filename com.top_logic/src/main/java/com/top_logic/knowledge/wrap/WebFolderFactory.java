/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.DuplicateEntryException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.PersistencyLayer;


/**
 * Factory for WebFolders.
 * 
 * TODO FMA DSN for rootDoc should be configurable, per type.
 *      (Sub)-Class should be configurable (See Wrapper Factory)
 * 
 * @author    <a href=mailto:fma@top-logic.com>Frank Mausz</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	// needed when WebFolder tries to create a KO
	MimeTypes.Module.class,
	// needed for creation of root folder.
	DataAccessService.Module.class
})
public class WebFolderFactory extends ManagedClass {
    
	/**
	 * Configuration parameter to define when the physical resource of a web folder should be
	 * created.
	 */
	public static final String XML_CONFIG_CREATE_PHYSICAL_RESOURCE = "createPhysicalResource";

    /** DAP for "repository://". */
	private final DataAccessProxy rootDoc;
    
	/**
	 * Collection of types that are allowed as folder types.
	 * 
	 * @see #isAllowedType(String)
	 */
	private final Collection<String> allowedFolderTypes;

    /** The standard folder of objects, f.e. THE WebFolder of a POSProject */
    public static final String STANDARD_FOLDER = "standardFolder";

    /** TODO FMA identifies a ... */
    public static final String POFFICE         = "poffice";

    /** TODO FMA identifies a ... */
    public static final String INITIAL_FOLDER  = "initialFolder";
    
    /** TODO FMA identifies a ... */
    public static final String SUB_FOLDER  = "subFolder";

	private final CreatePhysicalResource createMode;

	/**
	 * Configuration for {@link WebFolderFactory}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<WebFolderFactory> {
		/**
		 * When to create physical folder for each object.
		 */
		CreatePhysicalResource getCreatePhysicalResource();

		/**
		 * Allowed folder types.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getFolderTypes();
	}

	/**
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        Configuration for {@link WebFolderFactory}.
	 * @throws ModuleException
	 *         When "repository://" is invalid (not existent or not a folder).
	 */
	public WebFolderFactory(InstantiationContext context, Config config) throws ModuleException {
		super(context, config);

		createMode = config.getCreatePhysicalResource();
		rootDoc = createRootFolder(config);
		allowedFolderTypes = Collections.unmodifiableCollection(createAllowedFolderTypes(config));
	}

	/**
	 * Creates the DAP later returned by {@link #getDocFolderRoot()}.
	 * <p>
	 * <b>Note:</b> Method is called within constructor. Instance is not completely initialised.
	 * </p>
	 * 
	 * @return the DAP used to create folder within. Must not be <code>null</code>.
	 * 
	 * @throws ModuleException
	 *         when "repository://" is invalid (not existent or not a folder)
	 */
	protected DataAccessProxy createRootFolder(Config config) throws ModuleException {
		{
			DataAccessProxy root = new DataAccessProxy("repository://");
			{
				if (!root.exists()) {
					throw new ModuleException(root.toString() + " is Invalid (it does not exist)",
						WebFolderFactory.class);
				}
				if (!root.isContainer()) {
					throw new ModuleException(root.toString() + " is Invalid (it is not a container)",
						WebFolderFactory.class);
				}
				return root;
			}
		}
	}

    
    /** creates a new Webfolder with given name and type
     * @param aName the name of the new Folder, may not  be null
     * @param aFolderType the type of the new folder, must be one of the allowed types
     * The allowed types can be retrieved via WebFolderFactory.getInstance().getPossibleTypes()
     * @return the new Folder
     * @throws RuntimeException if any problem occurs
     * @throws IllegalArgumentException if
     * <ul>
     * <li> any argument is null
     * <li> aFolderType is not an allowed type
     * <li> a webfolder with type aType already exists for anOwner
     * </ul>
     */
    public WebFolder createNewWebFolder(String aName, String aFolderType)
    				throws RuntimeException, IllegalArgumentException{
        
        if(StringServices.isEmpty(aName)){
            throw new IllegalArgumentException("Empty name not allowed for folder");
        }
        if(! isAllowedType(aFolderType)){
			throw new IllegalArgumentException(aFolderType + " is not an allowed type for folder: Allowed: "
				+ getAllowedFolderTypes());
        }
        
        int       retry     = 12; // retry in a cluster or such
        while (retry-- > 0) { 
            try {
				WebFolder theFolder;
				if (this.getCreateMode() == CreatePhysicalResource.IMMEDIATE) {
					theFolder = WebFolder.createRootFolder(createUniqueFolderDSN());
				} else {
					theFolder = WebFolder.createRootFolderNoDSN(WebFolder.getDefaultKnowledgeBase(), aName);
				}
                theFolder.setName(aName);
                theFolder.setFolderType(aFolderType);
                return theFolder;
            } catch (DuplicateEntryException dux) {
                if (retry > 0) {
                    Logger.info("Failed to createFolder("+ aName + "," + aFolderType + ")#" + retry, this);
                    continue;
                }
                throw new RuntimeException("Problem creatingWebfolder",dux);
            } catch (Exception ex) {
                throw new RuntimeException("Problem creatingWebfolder",ex);
            }
        }
		throw new UnreachableAssertion("If creating WebFolder fails, an Exception is thrown.");
    }
    
    
    /** 
     * Returns true if the given folder type is an allowed type for WebFolders.
     * 
     * @param aFolderType the type to check
     * @return is the type allowed
     */
    public boolean isAllowedType(String aFolderType){
        return getAllowedFolderTypes().contains(aFolderType);
    }
    
    /**
	 * Unmodifiable collection of the allowed folder types
	 * 
	 * @return the allowed folder types
	 */
	public Collection<String> getAllowedFolderTypes() {
        return allowedFolderTypes;
    }

	/**
	 * the create mode. Never <code>null</code>.
	 */
	public CreatePhysicalResource getCreateMode() {
		return this.createMode;
	}
    
    /**
	 * collection with allowed folder types
	 */
	protected Collection<String> createAllowedFolderTypes(Config config) {
		Collection<String> result = new ArrayList<>(4);
        result.add(STANDARD_FOLDER);
        result.add(POFFICE);
        result.add(INITIAL_FOLDER);       
        result.add(SUB_FOLDER);       
        return result;
    }


    /** 
     * lazy return the DAP for "repository://".
     * 
     * @return the DAP for "repository://"
     */
	public final DataAccessProxy getDocFolderRoot() {
        return rootDoc;
    }
    
    /**
     * Returns a Unique datasource name for a new Folder.
     * 
     * @return Unique directory name based on getDocFolderRoot()
     */
    protected String createUniqueFolderDSN() throws DatabaseAccessException {

        DataAccessProxy theRoot   = getDocFolderRoot();
        String          theName   = theRoot.createNewEntryName("topl", null);
        DataAccessProxy theFolder = theRoot.createContainerProxy(theName);

        return theFolder.getPath();
    }

    /**
     * Standard Singleton pattern.
     * 
     * @return the instance
     */
    public static WebFolderFactory getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    
	/**
	 * Module for {@link WebFolderFactory}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public static final class Module extends TypedRuntimeModule<WebFolderFactory> {

		/**
		 * Module instance {@link WebFolderFactory}.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<WebFolderFactory> getImplementation() {
			return WebFolderFactory.class;
		}
	}

}
