/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.FilteredIterable;
import com.top_logic.basic.col.InlineSet;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.meta.MetaElementException;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.element.model.PersistentModule;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.QueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.db2.SimpleQueryCache;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.internal.PersistentType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.ReferencePreload;
import com.top_logic.util.model.ModelService;

/**
 * Create KBBasedMetaElements on (KB-stored) MetaElementHolders
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
@ServiceDependencies({
	MetaAttributeFactory.Module.class,
	WebFolderFactory.Module.class
	})
public abstract class KBBasedMetaElementFactory extends MetaElementFactory implements UpdateListener {

	/**
	 * Value of the {@link TLStructuredTypeColumns#META_ELEMENT_IMPL} attribute marking an association
	 * class.
	 * @deprecated Use {@link TLStructuredTypeColumns#ASSOCIATION_TYPE} instead
	 */
	@Deprecated
	public static final String ASSOCIATION_TYPE = TLStructuredTypeColumns.ASSOCIATION_TYPE;

	/**
	 * Value of the {@link TLStructuredTypeColumns#META_ELEMENT_IMPL} attribute marking a regular class.
	 * 
	 * @deprecated Use {@link TLStructuredTypeColumns#CLASS_TYPE} instead
	 */
	@Deprecated
	public static final String CLASS_TYPE = TLStructuredTypeColumns.CLASS_TYPE;

	/**
	 * Possible sub types of a {@link KBBasedMetaElement}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static enum SubtypeDefinition implements ExternallyNamed {

		/** A {@link TLClass} */
		CLASS(TLStructuredTypeColumns.CLASS_TYPE),

		/** An {@link TLAssociation} */
		ASSOCIATION(TLStructuredTypeColumns.ASSOCIATION_TYPE),

		;

		private final String _externalName;

		private SubtypeDefinition(String subType) {
			_externalName = subType;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	/** Local cache for global meta elements. */
    private Map<Tuple, ObjectKey> meCache = new HashMap<>(64); // Found 51 for DPM

	private QueryCache<TLClass> _allMetaElements;

	/**
	 * Creates a {@link KBBasedMetaElementFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KBBasedMetaElementFactory(InstantiationContext context, Config config) {
		super(context, config);

		this.getKnowledgeBase().addUpdateListener(this);
	}

    @Override
	public void notifyUpdate(KnowledgeBase aSender, UpdateEvent anEvent) {
		Object deletedMEKeys = getDeletedMetaElements(anEvent);
		
		if (!InlineSet.isEmpty(deletedMEKeys)) {
            synchronized (this.meCache) {
                Set<Entry<Tuple, ObjectKey>> theEntries = this.meCache.entrySet();
				Set<ObjectKey> deletedKeysAsSet = InlineSet.toSet(ObjectKey.class, deletedMEKeys);

                // Look up the deleted object keys for the meta elements in our cache.
                for (Iterator<Entry<Tuple, ObjectKey>> theInner = theEntries.iterator(); theInner.hasNext(); ) {
                    ObjectKey theValue = theInner.next().getValue();

					if (deletedKeysAsSet.contains(theValue)) {
                        theInner.remove();
                    }
                }
            }
        }
    }

	/**
	 * A {@link InlineSet} containing the keys of the deleted meta elements.
	 */
	private Object getDeletedMetaElements(UpdateEvent event) {
		Object deletedKeys = InlineSet.newInlineSet();

		// Check, if there are deleted meta elements.
		for (ObjectKey theKey : event.getDeletedObjectKeys()) {
			MetaObject theType = theKey.getObjectType();

			if (theType.getName().equals(KBBasedMetaElement.META_ELEMENT_KO)) {
				deletedKeys = InlineSet.add(ObjectKey.class, deletedKeys, theKey);
			}
		}
		return deletedKeys;
	}

    @Override
	public TLClass createMetaElement(TLModule module,
            TLScope aMetaElementHolder, String aMetaElementType, KnowledgeBase aKB) throws MetaElementException {
		KBBasedMetaElement me =
			newMetaElement(module, aMetaElementHolder, aMetaElementType, aKB, SubtypeDefinition.CLASS);
		((PersistentClass) me).setAbstract(false);
		((PersistentClass) me).setFinal(false);
		return me;
    }

	private KBBasedMetaElement newMetaElement(TLModule module, TLScope aMetaElementHolder,
			String aMetaElementType, KnowledgeBase aKB, SubtypeDefinition subType) {
        
        // Check type
        if (StringServices.isEmpty(aMetaElementType)) {
			throw new MetaElementException("Type type must not be null or empty!");
        }
        
        // Check duplicate
		if (aMetaElementHolder.getType(aMetaElementType) != null) {
			throw new MetaElementException("Type " + aMetaElementType + " is duplicate!");
        }
        
        // Create the ME physically
        KBBasedMetaElement theME = null;
        try {
			KnowledgeObject theKO =
				aKB.createKnowledgeObject(KBBasedMetaElement.META_ELEMENT_KO,
					new NameValueBuffer()
						.put(KBBasedMetaElement.META_ELEMENT_TYPE, aMetaElementType)
						.put(TLStructuredTypeColumns.META_ELEMENT_IMPL, subType.getExternalName())
						.put(KBBasedMetaElement.MODULE_REF, module.tHandle())
						.put(KBBasedMetaElement.SCOPE_REF, aMetaElementHolder.tHandle()));
			theME = (KBBasedMetaElement) WrapperFactory.getWrapper(theKO);
        } 
        catch (Exception ex) {
			Logger.error("Failed to create type.", ex, this);
			throw new MetaElementException("Failed to create type.", ex);
        }
        
		if (aMetaElementHolder instanceof PersistentModule) {
			Tuple key = key(module.getName(), ((PersistentModule) aMetaElementHolder).getName(), aMetaElementType);
			meCache.put(key, theME.tHandle().tId());
		}
		return theME;
	}

	@Override
	public TLClass createAssociationMetaElement(TLModule module, TLScope scope,
			String className, KnowledgeBase kb) throws MetaElementException {
		return newMetaElement(module, scope, className, kb, SubtypeDefinition.ASSOCIATION);
	}

    @Override
	public Set<TLClass> getAllMetaElements() {
		return new HashSet<>(internalGetAllMetaElements());
    }

	private List<TLClass> internalGetAllMetaElements() {
		return _allMetaElements.getValue();
	}

    @Override
	public Set<TLClass> getGlobalMetaElements() {
		Collection<TLClass> theMEs = this.internalGetAllMetaElements();
        Set<TLClass> theResult = new HashSet<>();

		ReferencePreload scopePreload = new ReferencePreload(PersistentType.TL_TYPE_KO, PersistentType.SCOPE_REF);
		PreloadContext context = new PreloadContext();
		scopePreload.prepare(context, theMEs);
		for (Iterator<TLClass> theIt = theMEs.iterator(); theIt.hasNext();) {
			TLClass theME = theIt.next();

			if (MetaElementUtil.isGlobal(theME)) {
                theResult.add(theME);
            }
        }
		context.close();
        return theResult;
    }

	@Override
	public TLClass lookupGlobalMetaElement(String moduleName, String className) {
		TLModule module = ModelService.getApplicationModel().getModule(moduleName);
		if (module == null) {
			return null;
		}
		return (TLClass) module.getType(className);
	}
    
	private Tuple key(String module, String scope, String name) {
		Object moduleKey = module;
		Object elementKey = scope;
		Branch contextBranch = this.getKnowledgeBase().getHistoryManager().getContextBranch();
		return TupleFactory.newTuple(name, moduleKey, elementKey, contextBranch);
	}

	@Override
	public TLClass findType(Object scopeBase, ScopeRef scopeRef, String moduleName) {
		String createType = scopeRef.getCreateType();
		TLClass theME = getMetaElementForClass(scopeBase, scopeRef.getScopeRef(), moduleName, createType);
        if (theME != null) {
			assert !theME.isAbstract() : "Tried to instantiate abstract type '" + TLModelUtil.qualifiedName(theME) + ".";
			return theME;
		} else {
			throw new IllegalArgumentException("Couldn't find type '" + createType + "' in module '" + moduleName
				+ "'. This may be caused by a misconfiguration or an intermediate DB reset.");
        }
    }

    /** 
     * Return the knowledge base to be used for finding {@link TLClass meta elements}.
     * 
     * This instance will also be attached as {@link UpdateListener} to the returned knowledge base.
     *  
     * @return    The requested knowledge base, never <code>null</code>
     */
    protected KnowledgeBase getKnowledgeBase() {
        return KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
    }

	/**
	 * Parse the parameter into the correct type. This will only work for types supporting
	 * conversion from string representation. If conversion fails an exception is thrown.
	 * 
	 * @param aType
	 *        one of MetaAttributeFactory.TYPE_*
	 * @param aValueString
	 *        the parameter value from the request
	 * @return a value of suitable type. May be null.
	 * @throws IllegalArgumentException
	 *         in case conversion can not be performed
	 */
    public static Object getTypedValueFromString(int aType, String aValueString) throws IllegalArgumentException {
        // Handle null and empty value
        if (aValueString == null ||
                !(aType == LegacyTypeCodes.TYPE_STRING) && StringServices.isEmpty(aValueString)) {
            return null;
        }
    
        Object theValue = null;
        switch (aType) {
            case LegacyTypeCodes.TYPE_BOOLEAN:
                try {
                    theValue =  Boolean.valueOf(aValueString);
                }
                catch (RuntimeException e) {
                    throw new IllegalArgumentException("Given string can not be parsed as boolean.");
                }
                break;
            case LegacyTypeCodes.TYPE_DATE:
                try {
                    theValue = CalendarUtil.getDateInstance(DateFormat.DEFAULT, Locale.GERMANY).parse(aValueString);
                }
                catch (ParseException pex) {
                    throw new IllegalArgumentException("Incompatible value for Date: " + aValueString);
                }
                break;
            case LegacyTypeCodes.TYPE_LONG:
                try {
                    theValue = Long.valueOf(aValueString);
                }
                catch (NumberFormatException nfx) {
                    throw new IllegalArgumentException("Incompatible value for Long: " + aValueString);
                }
                break;
            case LegacyTypeCodes.TYPE_FLOAT:
                try {
                    theValue = Float.valueOf(aValueString);
                }
                catch (NumberFormatException nfx) {
                    throw new IllegalArgumentException("Incompatible value for Float: " + aValueString);
                }
                break;
             case LegacyTypeCodes.TYPE_STRING:
                 theValue = aValueString;
                 break;
            default:
                throw new IllegalArgumentException("Meta attributes of type "+aType+" do not support conversion from string.");
        }
        return theValue;
    }

	@Override
	protected void startUp() {
		super.startUp();
		try {
			newAllMECache();
			loadCaches();
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} catch (InvalidLinkException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private void newAllMECache() throws UnknownTypeException {
		MetaObject meType = getKnowledgeBase().getMORepository().getMetaObject(KBBasedMetaElement.META_ELEMENT_KO);
		SimpleQuery<TLClass> query =
			SimpleQuery.queryResolved(TLClass.class, meType, 
				eqBinary(
					attribute(KBBasedMetaElement.META_ELEMENT_KO, TLStructuredTypeColumns.META_ELEMENT_IMPL), 
					literal(TLStructuredTypeColumns.CLASS_TYPE)));
		_allMetaElements = SimpleQueryCache.newQueryCache(getKnowledgeBase(), query);

	}

	private void loadCaches() throws InvalidLinkException {
		List<TLClass> allMetaElements = internalGetAllMetaElements();
		Mapping<TLClass, KnowledgeObject> koMapping = new Mapping<>() {

			@Override
			public KnowledgeObject map(TLClass input) {
				return (KnowledgeObject) input.tHandle();
			}
		};
		preloadAssociations(allMetaElements, koMapping);
		preloadClasses(allMetaElements, koMapping);
	}

	private void preloadClasses(List<TLClass> allMetaElements, Mapping<TLClass, KnowledgeObject> koMapping)
			throws InvalidLinkException {
		Iterable<KnowledgeObject> iterable =
			new MappedIterable<>(koMapping, new FilteredIterable<>(
				FilterFactory.createClassFilter(PersistentClass.class), allMetaElements));
		for (AbstractAssociationQuery<? extends TLObject, ?> query : PersistentClass.getAssociationQueries()) {
			getKnowledgeBase().fillCaches(iterable, query);
		}
	}

	private void preloadAssociations(List<TLClass> allMetaElements, Mapping<TLClass, KnowledgeObject> koMapping)
			throws InvalidLinkException {
		Iterable<KnowledgeObject> iterable =
			new MappedIterable<>(koMapping, new FilteredIterable<>(
				FilterFactory.createClassFilter(PersistentAssociation.class), allMetaElements));
		for (AbstractAssociationQuery<? extends TLObject, ?> associationQuery : PersistentAssociation
			.getAssociationQueries()) {
			getKnowledgeBase().fillCaches(iterable, associationQuery);
		}
	}

	@Override
	protected void shutDown() {
		_allMetaElements.invalidate();
		super.shutDown();
	}
}
