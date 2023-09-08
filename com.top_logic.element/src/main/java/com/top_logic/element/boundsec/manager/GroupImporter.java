/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.xml.XMLAttributeHelper;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class GroupImporter implements PartialImporter {

    private Stack boStack;
    private List  problems;
    private Map   groups;

    /** 
     * Creates a {@link GroupImporter}.
     */
    public GroupImporter() {
    }

    /**
     * @see com.top_logic.element.boundsec.manager.PartialImporter#getHandlers()
     */
    @Override
	public Map getHandlers() {
        return (new MapBuilder())
            .put(ElementAccessExportHelper.XML_TAG_GROUPS,   new DefaultHandler())
            .put(ElementAccessExportHelper.XML_TAG_GROUP,    new GroupHandler())
            .put(ElementAccessExportHelper.XML_TAG_LANGUAGE, new LanguageHandler())
            .put(ElementAccessExportHelper.XML_TAG_ENTRY,    new EntryHandler())
            .put(ElementAccessExportHelper.XML_TAG_MEMBER,   new MemberHandler())
            .toMap();
    }

    /**
     * @see com.top_logic.element.boundsec.manager.PartialImporter#getResult()
     */
    @Override
	public Object getResult() {
        return this.groups;
    }

    /**
     * @see com.top_logic.element.boundsec.manager.PartialImporter#init(java.util.Stack, java.util.List)
     */
    @Override
	public void init(Stack aStack, List someProblems) {
        this.boStack  = aStack;
        this.problems = someProblems;
        this.groups = new HashMap();
    }
    
    public void addProblem(String aProblem) {
        this.problems.add(aProblem);
    }
    
    public class GroupHandler extends DefaultHandler {
        private GroupInfo groupInfo;
        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes) throws SAXException {
            super.startElement(aUri, aLocalName, aName, aAttributes);
            String theName = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_NAME);
            this.groupInfo = new GroupInfo();
            this.groupInfo.name = theName;
            boStack.push(this);
        }
        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException {
            boStack.pop();
            GroupImporter.this.groups.put(groupInfo.name, groupInfo);
        }
        public void addLanguage(String aLanguage, Map someEntries) {
            this.groupInfo.languages.put(aLanguage, someEntries);
        }
        public void addGroup(String aGroup) {
            this.groupInfo.groups.add(aGroup);
        }
        public void addPerson(String aPerson) {
            this.groupInfo.persons.add(aPerson);
        }
    }
    public class LanguageHandler extends DefaultHandler {
        private String name;
        private Map entries;
        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes) throws SAXException {
            super.startElement(aUri, aLocalName, aName, aAttributes);
            String theName = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_NAME);
            this.name = theName;
            this.entries = new HashMap();
            boStack.push(this);
        }
        /**
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
		public void endElement(String aUri, String aLocalName, String aName)
                throws SAXException {
            boStack.pop();
            GroupHandler theParent = (GroupHandler) boStack.peek();
            theParent.addLanguage(this.name, this.entries);
            super.endElement(aUri, aLocalName, aName);
        }
        public void addEntry(String aKey, String aValue) {
            this.entries.put(aKey, aValue);
        }
    }
    public class EntryHandler extends DefaultHandler {
        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes) throws SAXException {
            super.startElement(aUri, aLocalName, aName, aAttributes);
            String theKey   = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_KEY);
            String theValue = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_VALUE);
            LanguageHandler theParent = (LanguageHandler) boStack.peek();
            theParent.addEntry(theKey, theValue);
        }
    }
    
    public class MemberHandler extends DefaultHandler {
        /**
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
		public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes) throws SAXException {
            super.startElement(aUri, aLocalName, aName, aAttributes);
            String theName    = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_NAME);
            String theIsGroup = XMLAttributeHelper.getAsStringOptional(aAttributes, ElementAccessExportHelper.XML_ATTRIBUTE_IS_GROUP);
            GroupHandler theParent = (GroupHandler) boStack.peek();
            if (StringServices.isEmpty(theIsGroup) || ! Boolean.valueOf(theIsGroup).booleanValue()) {
                theParent.addPerson(theName);
            } else {
                theParent.addGroup(theName);
            }
        }
    }
    
    
    public static class GroupInfo {
        private Map    languages = new HashMap();
        private Set    persons   = new HashSet();
        private Set    groups    = new HashSet();
        private String name;
    }

}

