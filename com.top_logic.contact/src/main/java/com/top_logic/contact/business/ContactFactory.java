/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractQueryCache;
import com.top_logic.knowledge.service.db2.QueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.wrap.WrapperNameComparator;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;

/**
 * This factory is responsible for creating Contacts.
 * 
 * These contacts are either Instances of PersonContact or CompanyContact.
 * They are distinguished by the type attribute of the underlying Contact knowledge object.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ContactFactory extends AbstractWrapperResolver {

    /** Type name of underlying KnowledgeObject */
    public static final String OBJECT_NAME      = "Contact";
    
    /** Value of TYPE_ATTRIBUTE indicating a {@link PersonContact} */
    public static final String PERSON_TYPE     = "PersonContact";

    /** Value of TYPE_ATTRIBUTE indicating a {@link CompanyContact} */
    public static final String COMPANY_TYPE    = "CompanyContact";
    
    /** The name of the structure we use */
    public static final String STRUCTURE_NAME      = "Contacts";

	private final ConcurrentHashMap<String, QueryCache<?>> _caches = new ConcurrentHashMap<>();

	@Override
	public void shutDown() {
		for (QueryCache<?> cache : _caches.values()) {
			cache.invalidate();
		}
		_caches.clear();
		super.shutDown();
	}

    public static ContactFactory getInstance() {
		return (ContactFactory) DynamicModelService.getFactoryFor(STRUCTURE_NAME);
    }

    /**
     * Factory method to create a new PersonContact.
     *
     * @param aName the name of the new PersonContact
     * @param aFirstname the first name of the new PersonContact
     * @return the created PersonContact
     */
    public PersonContact createNewPersonContact (String aName, String aFirstname) {
        if (aName == null || aFirstname == null) {
            throw new IllegalArgumentException ("Name and firstname must not be null.");
        }
        PersonContact theContact = (PersonContact)createNewContact(aName, PERSON_TYPE);
        theContact.setValue(PersonContact.FIRST_NAME, aFirstname);
        return theContact;
    }   

    /**
     * Factory method to create a new CompanyContact.
     *
     * @param aName the name of the new CompanyContact
     * @param anAddress the address of the new CompanyContact
     * @return the created CompanyContact
     */
    public CompanyContact createNewCompanyContact (String aName, AddressHolder anAddress) {
        if(aName == null || anAddress == null) {
            throw new IllegalArgumentException ("Name and address must not be null.");
        }
        CompanyContact theContact = (CompanyContact)createNewContact(aName, COMPANY_TYPE);
        theContact.setAddress(anAddress);
        return theContact;
    }

    /**
     * Factory method to create a new Contact.
     *
     * @param aName the name of the new Contact
     * @param aElementType the element type of the new Contact
     * @return the created Contact
     */
    public AbstractContact createNewContact(String aName, String aElementType) {
        if (aName == null || aElementType == null) {
            throw new IllegalArgumentException ("Name and type must not be null.");
        }
        AbstractContact theContact = (AbstractContact)createNewWrapper(aElementType);
        theContact.setValue(AbstractContact.NAME_ATTRIBUTE, aName);
        return theContact;
    }

    /** 
     * Return the meta element for the given type.
     * 
     * A type may be {@link PersonContact#META_ELEMENT} or {@link CompanyContact#META_ELEMENT}.
     * 
     * @param    aName    The name of the requested meta element, must not be <code>null</code>.
     * @return   The requested meta element or <code>null</code>, if no such element found.
     */
    public TLClass getMetaElement(String aName) {
		return (MetaElementFactory.getInstance().lookupGlobalMetaElement(STRUCTURE_NAME, aName));
    }
    
    /**
     * @param aType the type of contact to collect, if <code>null</code> all
     *            contacts, person and company are collected.
     * @return a modifiable list of wrappers for the contacts, sorted by name.
     */
    public List getAllContacts (String aType) {

        List result = getAllContactsUnsorted(aType);
        Collections.sort(result,WrapperNameComparator.getInstance());

        return result;
    }
    
    /**
     * @param aType the type of contact to collect, if <code>null</code> all
     *            contacts, person and company are collected.
     * @return a modifiable list of wrappers for the contacts in no particular order.
     */
    public List getAllContactsUnsorted (String aType) {
        KnowledgeBase  kBase      = getKnowledgeBase();
		{
            if (null == aType) {
				return kBase.search(
					queryResolved(allOf(OBJECT_NAME), TLObject.class));
            } else {
				return lookupCache(aType).getValue();
            }
        }
    }

	private QueryCache<?> lookupCache(String aType) {
		QueryCache<?> cachedCache = _caches.get(aType);
		if (cachedCache != null) {
			return cachedCache;
		}

		try {
			return createCache(aType);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private QueryCache<?> createCache(String aType) throws UnknownTypeException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		TLType companyType = getModule().getType(aType);
		MetaObject table = kb.getMORepository().getMetaObject(OBJECT_NAME);
		SimpleQuery<TLObject> query =
			SimpleQuery.queryResolved(TLObject.class, table, eqBinary(typeRef(), literal(companyType.tHandle())));
		QueryCache<TLObject> newCache = AbstractQueryCache.newQueryCache(kb, query);
		QueryCache<?> usedCache = MapUtil.putIfAbsent(_caches, aType, newCache);
		if (newCache != usedCache) {
			newCache.invalidate();
		}
		return usedCache;
	}
    
    /**
     * Similar to as getAllContacts() but null FKeys will be ignored. 
     * 
     * @param aType the type of contact to collect, must not be null here.
     * @return a modifiable list of wrappers for the contacts with undefined sort order.
     */
    public List getAllContactsWithFKEY (String aType) {
		return this.getAllContactsWithAttribute(aType, AbstractContact.FKEY_ATTRIBUTE, null, true);
    }
    
    /**
     * Similar to as getAllContacts() but null FKeys will be ignored. 
     * 
     * @param aType the type of contact to collect, must not be null here.
     * @return a modifiable list of wrappers for the contacts with undefined sort order.
     */
    public List getAllContactsWithAttribute (String aType, String anAttName, String aValue, boolean negate) {
    	KnowledgeBase kBase = getKnowledgeBase();
		RevisionQuery<TLObject> query = queryResolved(
			filter(
				allOf(OBJECT_NAME),
				and(
					eqBinaryLiteral(typeName(), aType),
					negate ? not(attributeEqBinary(OBJECT_NAME, anAttName, aValue))
						: attributeEqBinary(OBJECT_NAME, anAttName, aValue))),
			TLObject.class);

		return kBase.search(query);
    }

    /**
     * If there is a PersonContact connected to the given person, this contact is returned,
     * <code>null</code> otherwise.
     *
     * @param aPerson the person to get the person contact for 
     * @return the (first if multiple) PersonContact or <code>null</code>
     */
    public PersonContact getContactForPerson(Person aPerson) {
		if (aPerson == null) {
			return null;
		}
		return (PersonContact) aPerson.getUser();
    }

}
