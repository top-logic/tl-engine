/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DispatchingHandler;
import com.top_logic.basic.xml.XMLAttributeHelper;
import com.top_logic.basic.xml.sax.SAXUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.RolesProfileHandler;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementAccessImporter extends DispatchingHandler {

    private Stack boStack;
    
	private List<ResKey> problems;
    
    private Map maClassifications;
    private Map classifierRoles;
    private int profileCount;
    
    private PartialImporter groupImporter;
    
    public ElementAccessImporter(ElementAccessManager accessManager) {
        super();
        this.boStack           = new Stack();
		this.problems = new ArrayList<>();
        this.registerHandlers(accessManager);
        this.maClassifications = new HashMap();
        this.classifierRoles   = new HashMap();
        this.profileCount = 0;
    }
    
	public void addProblem(ResKey aProblem) {
        this.problems.add(aProblem);
    }
    
    private void registerHandlers(ElementAccessManager accessManager) {
        ApplicationRoleHolder theRoleProvider = new ApplicationRoleHolder(accessManager);
        
        this.groupImporter    = this.initPartialImporter(new GroupImporter());
        
        
        this.registerHandler(ElementAccessExportHelper.XML_TAG_META_ELEMENT,    new MetaElementHandler());
        this.registerHandler(ElementAccessExportHelper.XML_TAG_META_ATTRIBUTE,  new NamedElementHandler(true));
        this.registerHandler(ElementAccessExportHelper.XML_TAG_CLASSIFIER,      new ClassifierHandler(theRoleProvider));
        this.registerHandler(ElementAccessExportHelper.XML_TAG_AUTHORIZATION,   new DefaultHandler());
        this.registerHandler(ElementAccessExportHelper.XML_TAG_CLASSIFICATION,  new DefaultHandler());
        this.registerHandler(ElementAccessExportHelper.XML_TAG_CLASSIFICATIONS, new DefaultHandler());
        this.registerHandler(ElementAccessExportHelper.XML_TAG_ROLE,            new NamedElementHandler(true));
        this.registerHandler(ElementAccessExportHelper.XML_TAG_ACCESS,          new NamedElementHandler(true));
        this.registerHandler(ElementAccessExportHelper.XML_TAG_PATH,            new DefaultHandler());
        this.registerHandler(RolesProfileHandler.PROFILES_TAG,                  new DefaultHandler());
        this.registerHandler(RolesProfileHandler.PROFILE_TAG,                   new CountProfileHandler());
        this.registerHandler(RolesProfileHandler.VIEW_TAG,                      new DefaultHandler());
        this.registerHandler(RolesProfileHandler.COMMANDGROUP_TAG,              new DefaultHandler());
    }
    
    private PartialImporter initPartialImporter(PartialImporter aPI) {
        aPI.init(this.boStack, this.problems);
        for (Iterator theIt = aPI.getHandlers().entrySet().iterator(); theIt.hasNext();) {
            Map.Entry theEntry = (Map.Entry) theIt.next();
            this.registerHandler((String) theEntry.getKey(), (ContentHandler) theEntry.getValue());
        }
        return aPI;
    }
    
    /*package protected*/ class CountProfileHandler extends DefaultHandler {
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            profileCount++;
        }
    }
    
    
    private class ClassifierHandler extends NamedElementHandler {
        private ApplicationRoleHolder roleProvider;
        public ClassifierHandler(ApplicationRoleHolder aRoleProvider) {
            super(false);
            this.roleProvider = aRoleProvider;
        }

        @Override
		protected void handleContent() {
            if ( ! this.children.isEmpty()) {
                if (ElementAccessHelper.getClassifier(this.name) == null) {
					addProblem(
						I18NConstants.AUTHORIZATION_PROBLEM_UNKNOWN_CLASSIFIER.fill(
						this.name));
                } else {
                    for (Iterator theRolesIt = this.children.entrySet().iterator(); theRolesIt.hasNext(); ) {
                        Map.Entry  theRoleEntry = (Map.Entry) theRolesIt.next();
                        Map        theRoleNames = (Map)    theRoleEntry.getValue();
                        for (Iterator theRNIt = theRoleNames.keySet().iterator(); theRNIt.hasNext();) {
                            String theRoleName = (String) theRNIt.next();
                            
                            if (this.roleProvider.getRole(theRoleName) == null) {
								addProblem(
									I18NConstants.AUTHORIZATION_PROBLEM_UNKNOWN_ROLE.fill(
									this.name, theRoleName));
                                theRolesIt.remove();
                            } else {
                                if ( ! this.roleProvider.isUniqueRoleRole(theRoleName) ) {
									addProblem(
										I18NConstants.DUPLICATE_ROLE.fill(
										this.name,
										theRoleName));
                                }
                            }
                        }
                    }
                    classifierRoles.put(this.name, this.children);
                }
            }
        }
    }
    
    private class MetaElementHandler extends NamedElementHandler {
        
        private Map uniqueMEs;
        public MetaElementHandler() {
            super(true);
            this.uniqueMEs    = ElementAccessHelper.getUniqueMetaElements();
        }

        @Override
		protected void handleContent() {
            TLClass theME = getMetaElement(this.name);
            if (theME == null) {
				addProblem(
					I18NConstants.DUPLICATE_META_ELEMENT.fill(this.name));
            } else {
                for (Iterator theIt = this.children.entrySet().iterator(); theIt.hasNext(); ) {
                    Map.Entry theEntry = (Map.Entry) theIt.next();
                    String theAttributeName = (String) theEntry.getKey();
                    if ( ! MetaElementUtil.hasMetaAttribute(theME, theAttributeName) ) {
						addProblem(
							I18NConstants.UNKNOWN_META_ATTRIBUTE.fill(
							this.name,
							theAttributeName));
                        theIt.remove();
                    } else {
                        for (Iterator theClassIt = ((Collection) theEntry.getValue()).iterator(); theClassIt.hasNext(); ) {
                            String theClassifierName = (String) theClassIt.next();
                            if (ElementAccessHelper.getClassifier(theClassifierName) == null) {
								addProblem(
									I18NConstants.CLASSIFICATIONS_PROBLEM_UNKNOWN_CLASSIFIER.fill(
									this.name,
									theAttributeName, theClassifierName));
                                theClassIt.remove();
                            }
                        }
                    }
                }
                maClassifications.put(this.name, this.children);
            }
        }
        
        private TLClass getMetaElement(String aMetaElementName) {
            return (TLClass) uniqueMEs.get(aMetaElementName);
        }
    }

    private class NamedElementHandler extends DefaultHandler {
        protected String  name;
        protected Map     children;
        protected boolean useChildKeySet;
        public NamedElementHandler(boolean aUseChildKeySet) {
            this.useChildKeySet = aUseChildKeySet;
        }
        @Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            this.children = new HashMap();
            super.startElement(uri, localName, qName, attributes);
            this.name = XMLAttributeHelper.getAsStringOptional(attributes, ElementAccessExportHelper.XML_ATTRIBUTE_NAME);
            boStack.push(this);
        }
        @Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
             boStack.pop();
             Object theParent = boStack.isEmpty() ? null : boStack.peek();
             if (theParent instanceof NamedElementHandler) {
                 ((NamedElementHandler) theParent).addChild(this.name, this.children);
             } else {
                 this.handleContent();
             }
        }
        public void addChild(String aChildName, Map aChildContent) {
            if (this.useChildKeySet) {
                this.children.put(aChildName, aChildContent.keySet()); 
            } else {
                this.children.put(aChildName, aChildContent);
            }
        }
        protected void handleContent() {}
    }
    
    public static ElementAccessImporter parse(InputStream anIS, ElementAccessManager accessManager) throws SAXException, IOException, ParserConfigurationException {
        ElementAccessImporter theEAI = new ElementAccessImporter(accessManager);
		SAXUtil.newSAXParser().parse(anIS, theEAI);
        return theEAI;
    }

	public List<ResKey> getProblems() {
        return problems;
    }
    
    public Map getGroups() {
        return (Map) this.groupImporter.getResult();
    }

    public Map getAttributeClassifications() {
        return maClassifications;
    }

    public Map getClassifierRoles() {
        return classifierRoles;
    }
    
    public int getProfileCount() {
        return (profileCount);
    }
    
    public static class ApplicationRoleHolder {
        private Map availableRoles;
        private Set duplicateRoles;
        
        public ApplicationRoleHolder(ElementAccessManager accessManager) {
            this.availableRoles    = new HashMap();
            this.duplicateRoles    = new HashSet();
            for (Iterator theIt = ElementAccessHelper.getAvailableRoles(null, accessManager).iterator(); theIt.hasNext(); ) {
                BoundedRole theRole = (BoundedRole) theIt.next();
                String      theName = theRole.getName();
                Object theDuplicate = this.availableRoles.put(theName, theRole);
                if (theDuplicate != null) {
                    this.duplicateRoles.add(theName);
                }
            }
        }
        
        public BoundedRole getRole(String aRoleName) {
            return (BoundedRole) this.availableRoles.get(aRoleName);
        }
        
        public boolean isUniqueRoleRole(String aRoleName) {
            return ! this.duplicateRoles.contains(aRoleName);
        }
        
    }

}
