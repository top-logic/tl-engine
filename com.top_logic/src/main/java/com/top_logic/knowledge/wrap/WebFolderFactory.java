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

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Creates the web folders used to store uploaded documents.
 *
 * @author    <a href=mailto:fma@top-logic.com>Frank Mausz</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	// Documents derive their content type from the MIME types.
	MimeTypes.Module.class,
})
@Label("Web folder factory")
public class WebFolderFactory extends ManagedClass {
    
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

	/**
	 * Configuration for {@link WebFolderFactory}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<WebFolderFactory> {

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
	 */
	public WebFolderFactory(InstantiationContext context, Config config) {
		super(context, config);

		allowedFolderTypes = Collections.unmodifiableCollection(createAllowedFolderTypes(config));
	}

    /** creates a new Webfolder with given name and type
     * @param aName the name of the new Folder, may not  be null
     * @param aFolderType the type of the new folder, must be one of the allowed types
     * The allowed types can be retrieved via WebFolderFactory.getInstance().getPossibleTypes()
     * @return the new Folder
     * @throws IllegalArgumentException if
     * <ul>
     * <li> any argument is null
     * <li> aFolderType is not an allowed type
     * </ul>
     */
	public WebFolder createNewWebFolder(String aName, String aFolderType) throws IllegalArgumentException {
        if(StringServices.isEmpty(aName)){
            throw new IllegalArgumentException("Empty name not allowed for folder");
        }
        if(! isAllowedType(aFolderType)){
			throw new IllegalArgumentException(aFolderType + " is not an allowed type for folder: Allowed: "
				+ getAllowedFolderTypes());
        }
        
		WebFolder theFolder = WebFolder.createFolder(WebFolder.getDefaultKnowledgeBase(), aName);
		theFolder.setFolderType(aFolderType);
		return theFolder;
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
