/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.business;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.element.meta.kbbased.AbstractAttributedWrapperJournalDelegate;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.journal.AssociationCreatedDeletedJournalEntryGenerator;
import com.top_logic.knowledge.journal.JournalAttributeEntry;
import com.top_logic.knowledge.journal.JournalEntry;
import com.top_logic.knowledge.journal.Journallable;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.CompiledQueryCache;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * Abstract superclass for PersonContacts and CompanyContacts.
 * 
 * @author <a href=mailto:jco@top-logic.com>jco </a>
 */
public abstract class AbstractContact extends AttributedWrapper implements Journallable {
	
	private static final NamedConstant FKEY2_CACHE_KEY = new NamedConstant("FKEY2");

	private static final NamedConstant FKEY_CACHE_KEY = new NamedConstant("FKEY");

    /**
	 * Attribute for an arbitrary, indexed foreign key.
	 * 
	 * (used e.g. for Dun & Bradstreet references in {@link CompanyContact}s).
	 */
	public static final String FKEY_ATTRIBUTE = "FKey";

	/**
	 * Attribute for a second arbitrary, indexed foreign key.
	 * 
	 * (used for SAP-Numbers by some customers).
	 */
    public static final String FKEY2_ATTRIBUTE = "FKey2";

    /** 
     * Attribute for a som textual remarks (255 characters).
     */
    public static final String REMARKS_ATTRIBUTE = "remarks";

    public static final String ABSTRACT_CONTACT_ME = "Contact.all";

	public AbstractContact(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public JournalEntry getJournalEntry(Map someChanged, Map someCreated, Map someRemoved) {
		JournalEntry theEntry = null;
		try {
			LocalJournalEntryDelegate theDelegate = new LocalJournalEntryDelegate();
			theEntry = theDelegate.getJournalEntry();
			Collection theAssociationEntries = AssociationCreatedDeletedJournalEntryGenerator.getEntries(this, someChanged, someCreated, someRemoved);
			Iterator theIt = theAssociationEntries.iterator();
			while (theIt.hasNext()) {
				JournalAttributeEntry theAE = (JournalAttributeEntry) theIt.next();
				theEntry.addAttribute(theAE);

			}
			// theEntry.addAttribute(new
			// MessageJournalAttributeEntryImpl("blubAtt","String","test",(new
			// Date()).toString()));
		} catch (Exception e) {
			Logger.error("Journal entry could not be generated.", e, this);
		}
		return theEntry;
	}

	@Override
	public String getJournalType() {
		return tType().getName();
	}

    /**
     * Allow setting of the name.
     */
	@Override
	public void setName(String aName) {
		this.tSetData(NAME_ATTRIBUTE, aName);
    }

    /**
     * Allow setting of the foreign key.
     */
	public void setFKey(String aFKey) {
		this.tSetData(FKEY_ATTRIBUTE, aFKey);
    }
    
    /**
     * Allow reading of the foreign key.
     * 
     * @return null when value was null or Wrapper is invalid.
     */
	public String getFKey() {
		return tGetDataString(FKEY_ATTRIBUTE);
    }
    
    /**
     * Allow setting of the second foreign key.
     */
	public void setForeignKey2(String aFKey) {
		this.tSetData(FKEY2_ATTRIBUTE, aFKey);
    }


    /**
     * Allow reading of the second foreign key.
     * 
     * @return null when value was null or Wrapper is invalid.
     */
    public String getForeignKey2() {
		return tGetDataString(FKEY2_ATTRIBUTE);
    }

    
    /**
     * Allow setting of the remarks.
     */
	public void setRemarks(String aFKey) {
		this.tSetData(REMARKS_ATTRIBUTE, aFKey);
    }

    /**
     * Allow reading of the remarks.
     * 
     * @return null when value was null or Wrapper is invalid.
     */
    public String getRemarks() {
		return tGetDataString(REMARKS_ATTRIBUTE);
    }

    /**
	 * Delegate class to provide journalling functionallity
	 */
	protected class LocalJournalEntryDelegate extends AbstractAttributedWrapperJournalDelegate {
		public LocalJournalEntryDelegate() {
			super();
		}

		@Override
		protected AbstractWrapper getWrapper() {
			return AbstractContact.this;
		}
	}

    /**
     * Fetch a AbstractContact by its identifier.
     */
    public static AbstractContact getByIdentifier(KnowledgeBase aBase, TLID id) {
        KnowledgeObject found = aBase.getKnowledgeObject(ContactFactory.OBJECT_NAME, id);
        if (found != null) { 
            return (AbstractContact) WrapperFactory.getWrapper(found);
        }
        return null;
    }

    /**
     * Fetch a AbstractContact by its identifier in default KBase.
     */
    public static AbstractContact getByIdentifier(TLID id) {
        return getByIdentifier(getDefaultKnowledgeBase(), id);
    }

    /**
     * Fast way to fetch an AbstractContacts by its name.
     * 
     * Be aware that the uniquness of contact names is <em>not</em> enforced
     * by the KBase ot current implemenations. In Addition you must not use
     * the result of getName() for this call, as this Kind of name is often
     * descriptive and does not match the single Attribute looked up, here.
     * 
     * @param  aName the name to use.
     */
    public static AbstractContact getByName (KnowledgeBase aBase, String aName) {
        DataObject found = aBase.getObjectByAttribute(ContactFactory.OBJECT_NAME,NAME_ATTRIBUTE,aName);
        if (found instanceof KnowledgeObject) { // includes check for null
            return (AbstractContact) WrapperFactory.getWrapper((KnowledgeObject) found);
        }
        return null;
    }

    /**
     * Fast way to fetch an AbstractContacts by its name.
     * 
     * Be aware that the uniquness of contact names is <em>not</em> enforced
     * by the KBase ot current implemenations
     * 
     * @param  aName the name to use.
     */
    public static AbstractContact getByName(String aName) {
        return getByName(getDefaultKnowledgeBase(), aName);
    }

    /**
	 * Fast way to fetch some AbstractContacts by their foreign key.
	 * 
	 * @param aFKey
	 *        must not be null.
	 */
	public static List getListByFKey(KnowledgeBase aBase, String aFKey) {
		return getListByFKey(aBase, FKEY_CACHE_KEY, FKEY_ATTRIBUTE, aFKey);
    }

	private static List getListByFKey(KnowledgeBase kb, NamedConstant cacheKey, String foreignKeyAttrName,
			String foreignKeyValue) {
		if (foreignKeyValue == null) {
			throw new IllegalArgumentException("Value for foreign key must not be null.");
		}
		CompiledQuery<KnowledgeObject> compiledQuery = getFKeyQuery(kb, cacheKey, foreignKeyAttrName);

		RevisionQueryArguments arguments = revisionArgs();
		arguments.setArguments(foreignKeyValue);
		List<KnowledgeObject> result = compiledQuery.search(arguments);
		return getWrappersFromCollection(result);
	}

	private static CompiledQuery<KnowledgeObject> getFKeyQuery(KnowledgeBase kb, NamedConstant cacheKey,
			String foreignKeyAttrName) {
		CompiledQueryCache queryCache = kb.getQueryCache();
		CompiledQuery<KnowledgeObject> compiledQuery = queryCache.getQuery(cacheKey);
		if (compiledQuery == null) {
			compiledQuery = queryCache.storeQuery(cacheKey, createFKeyQuery(kb, foreignKeyAttrName));
		}
		return compiledQuery;
	}
    
	private static CompiledQuery<KnowledgeObject> createFKeyQuery(KnowledgeBase kb, String fKeyAttrName) {
		try {
			MetaObject contactType = kb.getMORepository().getMetaObject(ContactFactory.OBJECT_NAME);
			MOAttribute fKeyAttribute = MetaObjectUtils.getAttribute(contactType, fKeyAttrName);
			List<ParameterDeclaration> params = params(paramDecl(fKeyAttribute.getMetaObject(), "attr"));
			SetExpression search =
				filter(allOf(ContactFactory.OBJECT_NAME),
					and(eqBinary(typeName(), literal(ContactFactory.COMPANY_TYPE)),
					eqBinary(attribute(fKeyAttribute), param("attr"))));
			RevisionQuery<KnowledgeObject> query = queryUnresolved(params, search, NO_ORDER);
			CompiledQuery<KnowledgeObject> compiledQuery = kb.compileQuery(query);
			return compiledQuery;
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

    /**
	 * Fast way to fetch some AbstractContacts by their second foreign key.
	 * 
	 * @param aFKey2
	 *        must not be null.
	 */
    public static List getListByForeignKey2 (KnowledgeBase aBase, String aFKey2) {
		return getListByFKey(aBase, FKEY2_CACHE_KEY, FKEY2_ATTRIBUTE, aFKey2);
    }

    /**
	 * Fast way to fetch an AbstractContact by its second foreign key.
	 * 
	 * In case of multiple AbstractContacts for the foreign key the KnowledgeBase will log a
	 * warning, and an undefined Member of the set of AbstractContacts will be returned.
	 * 
	 * <pre>
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * </pre>
	 * 
	 * @param aFKey
	 *        may be null and will be ignored then.
	 * 
	 * @deprecated returns an random contact. Use
	 *             {@link #getListByForeignKey2(KnowledgeBase, String)} instead.
	 */
	@Deprecated
    public static AbstractContact getByForeignKey2 (KnowledgeBase aBase, String aFKey) {
		List allResults = getListByForeignKey2(aBase, aFKey);
		if (allResults.isEmpty()) {
			return null;
		} else {
			return (AbstractContact) allResults.get(0);
		}
    }
    
}