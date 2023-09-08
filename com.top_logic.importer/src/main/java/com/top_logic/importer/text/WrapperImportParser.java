/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.text;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.importer.excel.transformer.FastListElementTransformer;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperImportParser {

    /**
     * Return a {@link com.top_logic.knowledge.wrap.person.Person Person} identified by its login name.
     *  
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Person extends TextImportParser<com.top_logic.knowledge.wrap.person.Person> {

        public interface Config extends TextImportParser.Config {
        }

        public Person(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);
        }

        @Override
        public com.top_logic.knowledge.wrap.person.Person map(String input) {
            return PersonManager.getManager().getPersonByName(input);
        }
    }

    /**
     * Return a {@link com.top_logic.knowledge.wrap.person.Person Person} identified by its login name.
     *  
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class FastListElement extends TextImportParser<com.top_logic.knowledge.wrap.list.FastListElement> {

        public interface Config extends TextImportParser.Config {
            @MapBinding(key="value",attribute="element")
            Map<String, String> getMappings();
            
            @CharDefault(',')
            char getSeparator();
            
            String getListName();

            String getFallbackName();

            @BooleanDefault(false)
            boolean isMandatory();
            
            @BooleanDefault(false)
            boolean isCreate();
        }

        // Attributes

        private final FastListElementTransformer transformer;

        // Constructors

        /** 
         * Creates a {@link FastListElement}.
         */
        public FastListElement(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);

            this.transformer = new FastListElementTransformer(aConfig.isMandatory(), 
                                                              false, // We don't have column names in here
                                                              aConfig.isCreate(), 
                                                              aConfig.getMappings(), 
                                                              aConfig.getFallbackName(), 
                                                              aConfig.getListName(), 
                                                              aConfig.getSeparator());
        }

        // Overridden methods from TextImportParser

        @Override
        public com.top_logic.knowledge.wrap.list.FastListElement map(String input) {
			return this.transformer.getFastListElement(input);
        }
    }

    /**
     * Base class for returning an {@link com.top_logic.contact.business.AbstractContact AbstractContact} for the defined ID. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public abstract static class AbstractContact<T extends com.top_logic.contact.business.AbstractContact> extends TextImportParser<com.top_logic.contact.business.AbstractContact> {

        public interface Config extends TextImportParser.Config {

            /** List of attributes to create a unique ID for the contact . */
			@Name(TextFileImportParser.Config.UID)
            @Format(CommaSeparatedStrings.class)
            @Mandatory
            List<String> getUID();
        }

        // Attributes

        private final String type;

        private final List<String> ids;

        private List<T> contacts;

        // Constructors

        /** 
         * Creates a {@link AbstractContact}.
         */
        protected AbstractContact(InstantiationContext aContext, Config aConfig, String aType) {
            super(aContext, aConfig);

            this.type = aType;
            this.ids  = aConfig.getUID();
        }

        // Overridden methods from TextImportParser

        @Override
        public T map(String input) {
            return this.getContact(input);
        }

        // Protected methods

        @SuppressWarnings("unchecked")
        protected List<T> createContactList() {
            return ContactFactory.getInstance().getAllContacts(this.type);
        }

        protected T getContact(String aName) {
            for (T theContact : this.getAllContacts()) {
                if (aName.equals(this.getID(theContact))) {
                    return theContact;
                }
            }

            return null;
        }

        protected String getID(T aContact) {
            StringBuilder theBuilder = null;

            for (String theKey : this.ids) {
                Object theValue = aContact.getValue(theKey);

                if (theBuilder == null) {
                    theBuilder = new StringBuilder(theValue.toString());
                }
                else {
                    theBuilder.append(',');

                    if (theValue != null) {
                        theBuilder.append(theValue.toString());
                    }
                }
            }

            return theBuilder.toString();
        }

        protected List<T> getAllContacts() {
            if (this.contacts == null) {
                this.contacts = this.createContactList();
            }

            return this.contacts;
        }
    }

    /**
     * Return a {@link com.top_logic.contact.business.PersonContact PersonContact} for the defined ID. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class PersonContact<T extends com.top_logic.contact.business.PersonContact> extends AbstractContact<T> {

        public PersonContact(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig, ContactFactory.PERSON_TYPE);
        }
    }

    /**
     * Return a {@link com.top_logic.contact.business.CompanyContact CompanyContact} for the defined ID. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class CompanyContact<T extends com.top_logic.contact.business.CompanyContact> extends AbstractContact<T> {

        public CompanyContact(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig, ContactFactory.COMPANY_TYPE);
        }
    }
}

