/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.genericimport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.element.genericimport.interfaces.GenericCache;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class MetaElementBasedCache extends AbstractGenericDataImportBase implements GenericCache {

    private static final String PREFIX_CACHE = "cache:";
    private static final String PREFIX_META_ELEMENT = "me";
    private static final String PREFIX_META_OBJECT  = "mo";

    private Map<String, Map<Object, Object>> cache;
    
    private boolean doLazyInit;
    
    private boolean includeSubElements;

    private boolean smart;
    
    private int     initalSize;
    
    private final Map<String, String> fkeyForElementType = new HashMap<>();
    
    private final Map<String, String> typeOfElementType = new HashMap<>();
    
    /** 
     * Creates a {@link MetaElementBasedCache}.
     * 
     */
    public MetaElementBasedCache(Properties someProps) {
        super(someProps);
        this.cache              = new HashMap<>();
        this.doLazyInit         = Boolean.valueOf(someProps.getProperty("lazyInit",           "true")).booleanValue();
        this.includeSubElements = Boolean.valueOf(someProps.getProperty("includeSubElements", "true")).booleanValue();
        this.smart              = Boolean.valueOf(someProps.getProperty("smart", "true")).booleanValue();
        this.initalSize         = Integer.parseInt(someProps.getProperty("initialSize", "256"));
        
        for (Iterator theIter = someProps.keySet().iterator(); theIter.hasNext();) {
            String theKey = (String) theIter.next();
            if (theKey.startsWith(PREFIX_CACHE)) {
                String theConf = someProps.getProperty(theKey);
                
                String[] theParts = StringServices.split(theConf, ':');
                if (theParts.length != 3) {
                    throw new ConfigurationError("Configuration of a cache must be formatted like 'type:elementType:key'");
                }
                
                String theType = theParts[0];
                String theElementType = theParts[1];
                String theFKey = theParts[2];
                
                if (PREFIX_META_ELEMENT.equals(theType)) {
                    TLClass theElement = MetaElementBasedImportBase.getUniqueMetaElement(theElementType);
                    if (theElement == null) {
						throw new ConfigurationError("There is no unique type '" + theElementType + "'");
                    }
                    
                    if (! MetaElementUtil.hasMetaAttribute(theElement, theFKey)) {
						throw new ConfigurationError("Type '" + theElementType + "' has not attribute '" + theFKey
							+ "' which can be used as foreign key");
                    }
                    
                }
                else if (PREFIX_META_OBJECT.equals(theType)) {
                    try {
                        MetaObject theElement = PersistencyLayer.getKnowledgeBase().getMORepository().getMetaObject(theElementType);
                        
                        if (! MetaObjectUtils.hasAttribute(theElement, theFKey)) {
                            throw new ConfigurationError("MetaOject '" + theElementType + "' has not attribute '" + theFKey + "' which can be used as foreign key");
                        }
                    }
                    catch (UnknownTypeException e) {
                        throw new ConfigurationError("There is no unique MetaOject '" + theElementType + "'");
                    }
                }
                else {
                    throw new ConfigurationError("Type must be 'me' or 'mo'");
                }

                this.fkeyForElementType.put(theElementType, theFKey);
                this.typeOfElementType.put(theElementType, theType);
            }
        }
    }

    @Override
	public boolean add(String aType, Object aKey, Object aValue) {
        
        Map<Object, Object> theMap = this.getCache(aType);
                
        Object theOld = theMap.put(aKey, aValue);
        if (theOld != null) {
            theMap.put(aKey, theOld);
            return false;
        }
        return true;
    }

    @Override
	public boolean contains(String aType, Object aKey) {
        return this.getCache(aType).containsKey(aKey);
    }

    @Override
	public Object get(String aType, Object aKey) {
        return this.getCache(aType).get(aKey);
    }

    private Map<Object, Object> getCache(String aType) {
        Map<Object, Object> theMap = this.cache.get(aType);
        if (theMap == null) {
            theMap = new HashMap<>(this.initalSize);
            this.initCache(theMap, aType);
            this.cache.put(aType, theMap);
        }
        return theMap; // must not be null
    }
    
    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        boolean theResult = super.setImportConfiguration(aConfig, anInternalType);
        if (theResult) {
            this.reload();
        }
        return theResult;
    }
    
    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericCache#reload()
     */
    @Override
	public void reload() {

        this.cache.clear();
        
        if (doLazyInit) {
            return;
        }
        
        for (String type : this.typeOfElementType.keySet()) {
            this.getCache(type);
        }
    }
    
    protected void initCache(Map<Object, Object> aCache, String aType) {
        
        String theOrigin = this.typeOfElementType.get(aType);
        
        if (PREFIX_META_ELEMENT.equals(theOrigin)) {
            this.initCacheME(aCache, aType);
        }
        else if (PREFIX_META_OBJECT.equals(theOrigin)) {
            this.initCacheMO(aCache, aType);
        }
        else if (this.smart) {
            theOrigin = this.lookupType(aType);
            if (theOrigin != null) {
                
                GenericDataImportConfiguration config = this.getImportConfiguration();
                
                if (! config.getInternalTypes().contains(aType)) {
                    throw new IllegalArgumentException("There is no import configuration for element '" + aType + "'");
                }
                
                String theFKey = config.getForeignKey(aType);
                this.fkeyForElementType.put(aType, theFKey);
                this.typeOfElementType.put(aType, theOrigin);
                
                this.initCache(aCache, aType);
            }
            else {
				throw new IllegalArgumentException("There is no known type named '" + aType + "'");
            }
        }
        else {
            throw new IllegalArgumentException("There is no configuration for a cache for types of '" + aType + "'");
        }
    }
    
    private String lookupType(String aType) {
        try {
            TLClass theMeta = MetaElementBasedImportBase.getUniqueMetaElement(aType);
            if (theMeta != null) {
                return PREFIX_META_ELEMENT;
            }
            
            if (PersistencyLayer.getKnowledgeBase().getMORepository().getMetaObjectNames().contains(aType)) {
                return PREFIX_META_OBJECT;
            }
            
        } catch (Exception ex) {
            // ignore
        }
        return null;
    }
    
    private void initCacheME(Map<Object, Object> aCache, String aType) {
        String theFKey = fkeyForElementType.get(aType);
		{
            boolean success = false;
			for (TLClass theMeta : MetaElementFactory.getInstance().getAllMetaElements()) {
                if (aType.equals(theMeta.getName())) {
                    success = true;
					this.addAllAttributed((KBBasedMetaElement) theMeta, aCache, aType, theFKey);
                }
            }
            
            if (! success) {
				throw new IllegalArgumentException("Type " + aType + " must be unique.");
            }
        }
    }
    
    private void initCacheMO(Map<Object, Object> aCache, String aType) {
        String theFKey = fkeyForElementType.get(aType);
        
        Collection<KnowledgeObject> theKOs = PersistencyLayer.getKnowledgeBase().getAllKnowledgeObjects(aType);
        List<Wrapper> theWraps = WrapperFactory.getWrappersForKOsGeneric(theKOs);
        
        for (Wrapper wrapper : theWraps) {
            aCache.put(wrapper.getValue(theFKey), wrapper);
        }
    }
    
    private void addAllAttributed(KBBasedMetaElement aMeta, Map<Object, Object> aCache, String aCacheKey, String aForeignKeyAttr) {
		try (CloseableIterator<Wrapper> theIter = MetaElementUtil.iterateDirectInstances(aMeta, Wrapper.class)) {
			while (theIter.hasNext()) {
				Wrapper theWrap = theIter.next();
				Object theVal = theWrap.getValue(aForeignKeyAttr);
				if (theVal != null) {
					aCache.put(theVal, theWrap);
				}
			}
        }
        
        if (this.includeSubElements) {
			Iterator<TLClass> theSubs = aMeta.getSpecializations().iterator();
            while (theSubs.hasNext()) {
                this.addAllAttributed((KBBasedMetaElement) theSubs.next(), aCache, aCacheKey, aForeignKeyAttr); 
            }
        }
        
    }
    
    private Set/*<String>*/ getTypes() {
        return this.cache.keySet();
    }
    
    /**
     * @see com.top_logic.element.genericimport.interfaces.GenericCache#merge(com.top_logic.element.genericimport.interfaces.GenericCache)
     */
    @Override
	public void merge(GenericCache anotherCache) {
        MetaElementBasedCache theOtherCache = (MetaElementBasedCache)anotherCache;
        Set /*<String>*/      theTypes      = theOtherCache.getTypes();
        Iterator/*<String>*/  theTypeIt     = theTypes.iterator();
        
        while (theTypeIt.hasNext()) {
            String theType      = (String)theTypeIt.next();
            Map    theTypeCache = theOtherCache.getCache(theType);
            Set/*<Map.Entry>*/      theEntries = theTypeCache.entrySet();
            Iterator/*<Map.Entry>*/ theEntryIt = theEntries.iterator();
            while (theEntryIt.hasNext()) {
                Map.Entry theEntry = (Map.Entry)theEntryIt.next();
                this.add(theType, theEntry.getKey(), theEntry.getValue());
            }
        }
        
        if (theOtherCache.doLazyInit) {
            this.fkeyForElementType.putAll(theOtherCache.fkeyForElementType);
            this.typeOfElementType.putAll(theOtherCache.typeOfElementType);
        }
    }
}

