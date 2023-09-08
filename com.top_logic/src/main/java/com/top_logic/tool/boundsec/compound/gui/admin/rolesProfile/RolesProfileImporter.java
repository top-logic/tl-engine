/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * XML parser to import roles profiles given as XML.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class RolesProfileImporter extends DispatchingHandler {

    /** the root of the layout the profil is impoted to */
    private MainLayout mainLayout;
    
    /** holds the current role during the import */
    private BoundedRole currentProfile;
    /** holds the name of the current view during the import */
    private String currentView;
    /** holds the name of the current command group during the import */
    private String currentCommandGroup;

	private ProfileHandler profileHandler;
    
    /**
     * Constructor
     * 
     * @param aMainLayout    the main layout the import works on
     */
    public RolesProfileImporter(MainLayout aMainLayout) {
        super();
        this.mainLayout = aMainLayout;
        this.registerHandlers();
    }

    /**
     * Register the used handlers
     */
    public void registerHandlers() {
		profileHandler = new ProfileHandler();
        registerHandler(RolesProfileHandler.PROFILES_TAG    , new DefaultHandler());
		registerHandler(RolesProfileHandler.PROFILE_TAG, profileHandler);
        registerHandler(RolesProfileHandler.VIEW_TAG        , new ViewHandler());
        registerHandler(RolesProfileHandler.COMMANDGROUP_TAG, new CommandGroupHandler());
    }

    /**
     * Whether any profile information was found in the parsed XML-file.
     */
	public boolean hasAnyProfile() {
		return profileHandler.hasAnyProfile();
	}
        
    /**
     * Resolve the role the profile is for
     */
    /*package protected*/ class ProfileHandler extends DefaultHandler {
    	
    	// used to store if any profile information was given to the handler
		private boolean hasAnyProfile = false;

        /** 
         * Set the current profile
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
			hasAnyProfile = true;
            String theCurrentProfileName = XMLAttributeHelper.getAsStringOptional(attributes, RolesProfileHandler.NAME_ATTRIBUTE);
            if (StringServices.isEmpty(theCurrentProfileName))
                throw new SAXException("Encountered profile tag without name attribute.");
			{
                currentProfile = BoundedRole.getRoleByName(theCurrentProfileName);
                if (currentProfile == null) {
                    Logger.warn("Encountered profile tag with invalid name attribute "+theCurrentProfileName+".", this);
//                    throw new SAXException("Encountered profile tag with invalid name attribute "+theCurrentProfileName+".");
                    return;
                }
                ProjectLayoutSecurityPreImportVisitor theVis = new ProjectLayoutSecurityPreImportVisitor(currentProfile);
                mainLayout.acceptVisitorRecursively(theVis);
            }
            super.startElement(uri, localName, qName, attributes);
        }

		protected boolean hasAnyProfile() {
			return hasAnyProfile;
		}
    }
    
    /**
     * Store the current view
     */
    /*package protected*/ class ViewHandler extends DefaultHandler {
        /** 
         * Set the current profile
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentView = XMLAttributeHelper.getAsStringOptional(attributes, RolesProfileHandler.NAME_ATTRIBUTE);
            if (StringServices.isEmpty(currentView))
                throw new SAXException("Encountered view tag without name attribute.");
            super.startElement(uri, localName, qName, attributes);
        }
    }
    
    /**
     * store the current command group, and set the access for the current
     * role, and command group on the current view.
     */
    /*package protected*/ class CommandGroupHandler extends DefaultHandler {
        /** 
         * Set the current profile
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentCommandGroup = XMLAttributeHelper.getAsStringOptional(attributes, RolesProfileHandler.NAME_ATTRIBUTE);
            if (StringServices.isEmpty(currentCommandGroup))
                throw new SAXException("Encountered commandGroup tag without name attribute.");
            super.startElement(uri, localName, qName, attributes);
        }

        /** 
         * Register the given role for the given command group on the given view.
         *
         * @see com.top_logic.basic.xml.DispatchingHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
			LayoutComponent theView;
			try {
				theView = mainLayout.getComponentByName(
					ComponentName.newConfiguredName(RolesProfileHandler.NAME_ATTRIBUTE, currentView));
			} catch (ConfigurationException ex) {
				Logger.warn("Invalid component name '" + currentView + "'.", ex);
//                throw new SAXException("Invalid component name '" + currentView + "'.", ex);
				return;
			}
            if (theView == null) {
            	Logger.warn("No Layout for '" + currentView + "' found", this);
//                throw new SAXException("No Layout for '" + currentView + "' found" );
            	return;
            }
            // TODO KBU/TSA/KHA better use an interface (e.g. CompoundBoundChecker) ?
            if (! (theView instanceof CompoundSecurityLayout)) {
            	Logger.warn(currentView + " is not a CompoundSecurityLayout " + theView, this);
//                throw new SAXException(currentView + " is not a CompoundSecurityLayout " + theView);
            	return;
            }
            CompoundSecurityLayout thePL = (CompoundSecurityLayout) theView;
            BoundCommandGroup      theCG = thePL.getCommandGroupById(currentCommandGroup);
            if (theCG == null) {
            	Logger.warn("No '" +  currentCommandGroup + "' CommandGroup in '" + currentView + "'", this);
//                throw new SAXException("No '" +  currentCommandGroup + "' CommandGroup in '" + currentView + "'");
            	return;
            }
            if (currentProfile != null) {
                thePL.addAccess(theCG, currentProfile);
            }
            super.endElement(namespaceURI, localName, qName);
        }
    }
    
    /**
     * This visitor removes the existing roles profiles form the 
     * project layouts so the import can work on a clean layout.
     */
    protected class ProjectLayoutSecurityPreImportVisitor extends DefaultDescendingLayoutVisitor {
    	
    	private BoundedRole role;
    	
    	public ProjectLayoutSecurityPreImportVisitor(BoundedRole aRole) {
    		super();
    		this.role = aRole;
    	}

        /** 
         * Remove all access if component is a Project Layout
         *
         * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
            if (aComponent instanceof CompoundSecurityLayout) {
                CompoundSecurityLayout thePL = (CompoundSecurityLayout) aComponent;
                thePL.removeAccess(null, this.role);
            }

            return true;
        }
    }

}
