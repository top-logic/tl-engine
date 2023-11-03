/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.KBBasedManagedClass;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Utility methods for {@link TLStructuredTypePart}s with {@link TLObject}s as values.
 * 
 * @author <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class WrapperMetaAttributeUtil extends KBBasedManagedClass<WrapperMetaAttributeUtil.Config> {

	/**
	 * Configuration of the {@link WrapperMetaAttributeUtil}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends KBBasedManagedClass.Config<WrapperMetaAttributeUtil> {
		// no special attributes here
	}

	/** Abstract KA being the root of all such derived wrapper attribute associations. */
	public static final String WRAPPER_ATTRIBUTE_ASSOCIATION_BASE =
		ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE;

    /** KA used to connect MetaElemented KO to the Wrapper KO. */
	public static final String WRAPPER_ATTRIBUTE_ASSOCIATION = ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION;

	/**
	 * KA attribute used to store {@link TLStructuredTypePart}.
	 * <p>
	 * <em>Important:</em>Only the IDs of the {@link #getDefinitionId(TLStructuredTypePart) "root"
	 * attribute} is used in the database. "root" means the top-most attribute which is not
	 * overriding another attribute. This simplifies and therefore speeds up queries a lot.
	 * </p>
	 */
	public static final String META_ATTRIBUTE_ATTR = ApplicationObjectUtil.META_ATTRIBUTE_ATTR;

	private static final RevisionQuery<KnowledgeAssociation> HAS_VALUE_IN_ATTRIBUTES_QUERY =
		queryUnresolved(
			BranchParam.single,
			RangeParam.first,
			params(
				paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "value"),
				paramDecl(MOCollectionImpl.createSetType(IdentifierTypes.REFERENCE_MO_TYPE), "attrIds")),
			filter(
				anyOf(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE),
				and(
					eqBinary(
						reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME),
						param("value")),
					inSet(
						identifier(reference(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE, META_ATTRIBUTE_ATTR)),
						setParam("attrIds")))),
			null,
			KnowledgeAssociation.class);

	private final Map<String, CompiledQuery<KnowledgeAssociation>> _hasValueInAttributeQuery;

	private final Map<String, CompiledQuery<KnowledgeAssociation>> _hasValueInAnyAttributeQuery;

	private final Map<String, CompiledQuery<KnowledgeAssociation>> _getWithValueInAttributeQuery;

	private final Map<String, CompiledQuery<KnowledgeAssociation>> _getWithValueInAnyAttributeQuery;

	private List<? extends MetaObject> _referenceAssociationTypes;

	/**
	 * Creates a new {@link WrapperMetaAttributeUtil} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WrapperMetaAttributeUtil}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public WrapperMetaAttributeUtil(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		try {
			TypeSystem typeSystem = (TypeSystem) kb().getMORepository();
			MetaObject baseType = typeSystem.getType(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
			_referenceAssociationTypes = typeSystem.getConcreteSubtypes(baseType);
			_hasValueInAttributeQuery = MapUtil.newMap(_referenceAssociationTypes.size());
			_hasValueInAnyAttributeQuery = MapUtil.newMap(_referenceAssociationTypes.size());
			_getWithValueInAttributeQuery = MapUtil.newMap(_referenceAssociationTypes.size());
			_getWithValueInAnyAttributeQuery = MapUtil.newMap(_referenceAssociationTypes.size());
			for (MetaObject mo : _referenceAssociationTypes) {
				String typeName = mo.getName();
				_hasValueInAttributeQuery.put(typeName, createHasValueInAttributeQuery(kb(), typeName));
				_hasValueInAnyAttributeQuery.put(typeName, createHasValueInAnyAttributeQuery(kb(), typeName));
				_getWithValueInAttributeQuery.put(typeName, createGetWithValueInAttributeQuery(kb(), typeName));
				_getWithValueInAnyAttributeQuery.put(typeName, createGetWithValueInAnyAttributeQuery(kb(), typeName));
			}
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}

	}

	private CompiledQuery<KnowledgeAssociation> createHasValueInAnyAttributeQuery(KnowledgeBase kb, String typeName) {
		return kb.compileQuery(createHasValueInAnyAttributeQuery(typeName));
	}

	private RevisionQuery<KnowledgeAssociation> createHasValueInAnyAttributeQuery(String typeName) {
		return queryUnresolved(
			BranchParam.single,
			RangeParam.first,
			params(paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "value")),
			filter(
				allOf(typeName),
				eqBinary(
					reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME),
					param("value"))),
			null,
			KnowledgeAssociation.class);
	}

	private CompiledQuery<KnowledgeAssociation> hasValueInAnyAttributeQuery(String associationName) {
		CompiledQuery<KnowledgeAssociation> query = _hasValueInAnyAttributeQuery.get(associationName);
		if (query == null) {
			throw new IllegalArgumentException("There is no query for association '" + associationName + "'. '"
				+ associationName + "' might not be a subtype of '" + WRAPPER_ATTRIBUTE_ASSOCIATION_BASE + "'.");
		}
		return query;
	}

	private CompiledQuery<KnowledgeAssociation> createHasValueInAttributeQuery(KnowledgeBase kb, String typeName) {
		return kb.compileQuery(createHasValueInAttributeQuery(typeName));
	}

	private RevisionQuery<KnowledgeAssociation> createHasValueInAttributeQuery(String typeName) {
		return queryUnresolved(
			BranchParam.single,
			RangeParam.first,
			params(
				paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "value"),
				paramDecl(IdentifierTypes.REFERENCE_MO_TYPE, "attrId")),
			filter(
				allOf(typeName),
				and(
					eqBinary(
						reference(BasicTypes.ASSOCIATION_TYPE_NAME, DBKnowledgeAssociation.REFERENCE_DEST_NAME),
						param("value")),
					eqBinary(
						identifier(reference(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE, META_ATTRIBUTE_ATTR)),
						param("attrId")))),
			null,
			KnowledgeAssociation.class);
	}

	private CompiledQuery<KnowledgeAssociation> hasValueInAttributeQuery(String associationName) {
		CompiledQuery<KnowledgeAssociation> query = _hasValueInAttributeQuery.get(associationName);
		if (query == null) {
			throw new IllegalArgumentException("There is no query for association  '" + associationName + "'. '"
				+ associationName + "' might not be a subtype of '" + WRAPPER_ATTRIBUTE_ASSOCIATION_BASE + "'.");
		}
		return query;
	}

	private CompiledQuery<KnowledgeAssociation> createGetWithValueInAnyAttributeQuery(KnowledgeBase kb, String typeName) {
		return kb.compileQuery(createGetWithValueInAnyAttributeQuery(typeName));
	}

	private RevisionQuery<KnowledgeAssociation> createGetWithValueInAnyAttributeQuery(String typeName) {
		return queryUnresolved(
			params(paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "value")),
			filter(
				allOf(typeName),
				eqBinary(destination(), param("value"))),
			null,
			KnowledgeAssociation.class);
	}

	private CompiledQuery<KnowledgeAssociation> getWithValueInAnyAttributeQuery(String associationName) {
		CompiledQuery<KnowledgeAssociation> query = _getWithValueInAnyAttributeQuery.get(associationName);
		if (query == null) {
			throw new IllegalArgumentException("There is no query for association '" + associationName + "'. '"
				+ associationName + "' might not be a subtype of '" + WRAPPER_ATTRIBUTE_ASSOCIATION_BASE + "'.");
		}
		return query;
	}

	private CompiledQuery<KnowledgeAssociation> createGetWithValueInAttributeQuery(KnowledgeBase kb, String typeName) {
		return kb.compileQuery(createGetWithValueInAttributeQuery(typeName));
	}

	private RevisionQuery<KnowledgeAssociation> createGetWithValueInAttributeQuery(String typeName) {
		return queryResolved(
			params(
				paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "value"),
				paramDecl(IdentifierTypes.REFERENCE_MO_TYPE, "attrId")),
			filter(
				allOf(typeName),
				and(
					eqBinary(
						destination(),
						param("value")),
						eqBinary(
							identifier(reference(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE, META_ATTRIBUTE_ATTR)),
							param("attrId")))),
			null,
			KnowledgeAssociation.class);
	}

	private CompiledQuery<KnowledgeAssociation> getWithValueInAttributeQuery(String associationName) {
		CompiledQuery<KnowledgeAssociation> query = _getWithValueInAttributeQuery.get(associationName);
		if (query == null) {
			throw new IllegalArgumentException("There is no query for association  '" + associationName + "'. '"
				+ associationName + "' might not be a subtype of '" + WRAPPER_ATTRIBUTE_ASSOCIATION_BASE + "'.");
		}
		return query;
	}

	private static WrapperMetaAttributeUtil getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}
    
	/**
	 * Get all {@link MetaObject}s used as
	 * {@link WrapperMetaAttributeUtil#isAttributeReferenceAssociation(MetaObject)}.
	 * 
	 * @return the list of such meta objects, never null.
	 */
	public static List<? extends MetaObject> getReferenceAssociationTypes() {
		return getInstance()._referenceAssociationTypes;
	}

	/**
	 * Check if the given {@link MetaObject} is a subtype of
	 * {@link #WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}.
	 */
	public static boolean isAttributeReferenceAssociation(MetaObject mo) {
		return mo.isSubtypeOf(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
	}

	/**
	 * Check if the given {@link KnowledgeItem} is a an instance of
	 * {@link #WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}.
	 */
	public static boolean isAttributeReferenceAssociation(KnowledgeItem item) {
		return item.isInstanceOf(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
	}

	public static Collection getWrappersWithValueFromGlobalME(String aMEType, String attributeName, TLObject aValue)
			throws NoSuchAttributeException {
        TLClass theME = MetaElementFactory.getInstance().getGlobalMetaElement(aMEType);
		TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, attributeName);

		return (WrapperMetaAttributeUtil.getWrappersWithValue(theMA, aValue));
    }

    /**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #getWrappersWithValue(TLStructuredTypePart, String, TLObject)} instead. See
	 *             {@link WrapperMetaAttributeUtil#hasWrappersWithValue(TLID, TLObject)} for further
	 *             explanations.
	 */
	@Deprecated
	public static Collection getWrappersWithValue(TLID aMetaAttributeObjectName, TLObject aValue) {
		TLStructuredTypePart ma = lookupMetaAttribute(aMetaAttributeObjectName);
		return getWrappersWithValue(ma, aValue);
	}

	/**
	 * Helper for external use (filters) to get all Wrappers that have the given one as value for a
	 * given {@link TLStructuredTypePart}.
	 * 
	 * @param ma
	 *        the {@link TLStructuredTypePart}. Must not be <code>null</code> to indicate any attribute.
	 * @param value
	 *        the value. Must not be <code>null</code>.
	 * @return all wrappers that have the given one as value for the attribute. May be empty but not
	 *         <code>null</code>.
	 */
	public static Collection getWrappersWithValue(TLStructuredTypePart ma, TLObject value) {
		String associationName = WrapperMetaAttributeUtil.getAssociationName(ma);
		return getWrappersWithValue(ma, associationName, value);
	}

	/**
	 * Helper for external use (filters) to get all Wrappers that have the given one as value for
	 * any attribute.
	 * 
	 * @param aValue
	 *        the value. Must not be <code>null</code>.
	 * @return all wrappers that have the given one as value for the attribute. May be empty but not
	 *         <code>null</code>.
	 */
	public static Collection<Wrapper> getWrappersWithValue(TLObject aValue) {
		Collection<Wrapper> result = new HashSet<>();
		for (MetaObject mo : getReferenceAssociationTypes()) {
			result.addAll(getWrappersWithValue((TLStructuredTypePart) null, mo.getName(), aValue));
		}
		return result;
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #getWrappersWithValue(TLStructuredTypePart, String, TLObject)} instead. See
	 *             {@link WrapperMetaAttributeUtil#hasWrappersWithValue(TLID, TLObject)} for further
	 *             explanations.
	 */
	@Deprecated
	public static Collection<Wrapper> getWrappersWithValue(TLID metaAttribute, String associationName,
			TLObject aValue) {
		return getWrappersWithValue(lookupMetaAttribute(metaAttribute), associationName, aValue);
	}

	/**
	 * Helper for external use (filters) to get all Wrappers that have the given one as value for a
	 * given {@link TLStructuredTypePart}.
	 * 
	 * @param metaAttribute
	 *        May be <code>null</code> to indicate any attribute.
	 * @param associationName
	 *        the name of the knowledge association used to store the references. Must not be
	 *        <code>null</code>.
	 * @param aValue
	 *        the value. Must not be <code>null</code>.
	 * @return all wrappers that have the given one as value for the attribute. May be empty but not
	 *         <code>null</code>.
	 */
	public static Collection<Wrapper> getWrappersWithValue(TLStructuredTypePart metaAttribute, String associationName,
			TLObject aValue) {
		if (aValue == null) {
			// Questionable semantics but for compatibility reasons.
			return Collections.emptySet();
		}

		{
			WrapperMetaAttributeUtil instance = getInstance();
			List<KnowledgeAssociation> dbResult;
			if (metaAttribute == null) {
				CompiledQuery<KnowledgeAssociation> query = instance.getWithValueInAnyAttributeQuery(associationName);
				dbResult = query.search(contextArgs(aValue).setArguments(aValue.tHandle()));
			} else {
				TLID definitionId = getDefinitionId(metaAttribute);
				CompiledQuery<KnowledgeAssociation> query = instance.getWithValueInAttributeQuery(associationName);
				dbResult = query.search(contextArgs(aValue).setArguments(aValue.tHandle(), definitionId));
			}
			switch (dbResult.size()) {
				case 0: {
					return new ArrayList<>();
				}
				case 1: {
					ArrayList<Wrapper> result = new ArrayList<>(1);
					result.add(WrapperFactory.getWrapper(dbResult.get(0).getSourceObject()));
					return result;
				}
				default: {
					BulkIdLoad bulkIdLoad = new BulkIdLoad(instance.kb());
					for (KnowledgeAssociation ass : dbResult) {
						bulkIdLoad.add(ass.getSourceIdentity());
					}
					List items = bulkIdLoad.load();
					return WrapperFactory.getWrappersForKOsGeneric(items);
				}
			}
		}
    }

	/**
	 * The {@link KnowledgeObject}s of the given type, that points via any
	 *         {@link #WRAPPER_ATTRIBUTE_ASSOCIATION_BASE} association to the the given value.
	 */
	public static List getIncomingWrappers(TLObject dest, String koType) {
		ArrayList result = new ArrayList();
		for (MetaObject referenceType : getReferenceAssociationTypes()) {
			result.addAll(((AbstractWrapper) dest).getIncomingWrappers(referenceType.getName(), koType));
		}
		return result;
	}

	/**
	 * Returns an {@link Iterator} over any association of type
	 * {@link #WRAPPER_ATTRIBUTE_ASSOCIATION_BASE} (including subtypes) that has the given
	 * {@link TLObject} as source.
	 */
	public static Iterator<KnowledgeAssociation> getOutgoingAssociations(TLObject source) {
		ArrayList<KnowledgeAssociation> result = new ArrayList<>();
		for (MetaObject referenceType : getReferenceAssociationTypes()) {
			Iterator<KnowledgeAssociation> outgoingAssociations =
				((KnowledgeObject) source.tHandle()).getOutgoingAssociations(referenceType.getName());
			while (outgoingAssociations.hasNext()) {
				result.add(outgoingAssociations.next());
			}
		}
		return result.iterator();
	}

	/**
	 * Returns an {@link Iterator} over any association of type
	 * {@link #WRAPPER_ATTRIBUTE_ASSOCIATION_BASE} (including subtypes) that has the given
	 * {@link TLObject} as destination.
	 */
	public static Iterator<KnowledgeAssociation> getIncomingAssociations(TLObject dest) {
		ArrayList<KnowledgeAssociation> result = new ArrayList<>();
		for (MetaObject referenceType : getReferenceAssociationTypes()) {
			Iterator<KnowledgeAssociation> outgoingAssociations =
				((KnowledgeObject) dest.tHandle()).getIncomingAssociations(referenceType.getName());
			while (outgoingAssociations.hasNext()) {
				result.add(outgoingAssociations.next());
			}
		}
		return result.iterator();
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #hasWrappersWithValue(TLStructuredTypePart, String, TLObject)} instead, as the
	 *             {@link TLStructuredTypePart} object is needed. If only the id is given, the object will
	 *             be looked up internally, which can normally be avoided as the callers already
	 *             knows the object.
	 */
	@Deprecated
	public static boolean hasWrappersWithValue(TLID aMetaAttributeID, TLObject aValue) {
		TLStructuredTypePart ma = lookupMetaAttribute(aMetaAttributeID);
		TLStructuredTypePart definition = getDefinition(ma);
		return hasWrappersWithValue(definition, aValue);
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #hasWrappersWithValue(TLStructuredTypePart, String, TLObject)} instead. See
	 *             {@link WrapperMetaAttributeUtil#hasWrappersWithValue(TLID, TLObject)} for further
	 *             explanations.
	 */
	@Deprecated
	public static boolean hasWrappersWithValue(TLID attributeId, String anAssociationName, TLObject aValue) {
		return hasWrappersWithValue(lookupMetaAttribute(attributeId), anAssociationName, aValue);
	}

	/**
	 * Helper for external use (filters) to check if there are Wrappers that have the given one as
	 * value for an attribute given by its ID.
	 * 
	 * @param attribute
	 *        May be <code>null</code> to indicate any attribute.
	 * @param anAssociationName
	 *        the name of the knowledge association used to store the references. Must not be
	 *        <code>null</code>.
	 * @param aValue
	 *        the value. Must not be <code>null</code>.
	 * @return true if there is at least one wrapper that has the given one as value for the
	 *         attribute.
	 */
	public static boolean hasWrappersWithValue(TLStructuredTypePart attribute, String anAssociationName, TLObject aValue) {
		return getInstance().internalHasWrappersWithValue(attribute, anAssociationName, aValue);
	}

	private boolean internalHasWrappersWithValue(TLStructuredTypePart attribute, String anAssociationName, TLObject aValue) {
		{
			try (CloseableIterator<KnowledgeAssociation> result = query(anAssociationName, attribute, aValue)) {
				return result.hasNext();
			}
		}
    }

	private CloseableIterator<KnowledgeAssociation> query(String associationName, TLStructuredTypePart attribute,
			TLObject value) {
		KnowledgeItem valueItem = value.tHandle();

		if (attribute == null) {
			CompiledQuery<KnowledgeAssociation> query = hasValueInAnyAttributeQuery(associationName);
			return query.searchStream(contextArgs(value).setArguments(valueItem));
		} else {
			TLID definitionId = getDefinitionId(attribute);
			CompiledQuery<KnowledgeAssociation> query = hasValueInAttributeQuery(associationName);
			return query.searchStream(contextArgs(value).setArguments(valueItem, definitionId));
		}
	}
    
	/**
	 * Checks whether there are any objects that points to the given value over an
	 * {@link #getReferenceAssociationTypes() reference attribute association}.
	 */
	public static boolean hasWrappersWithValue(TLObject value) {
		return !CollectionUtil.isEmptyOrNull(getWrappersWithValue(value));
	}

	/**
	 * Helper for external use (filters) to check if there are Wrappers that have the given one as
	 * value for an attribute given by its ID.
	 * 
	 * @param ma
	 *        the attribute. Must not be null.
	 * @param value
	 *        the value. Must not be <code>null</code>.
	 * @return true if there is at least one wrapper that has the given one as value for the
	 *         attribute.
	 */
	public static boolean hasWrappersWithValue(TLStructuredTypePart ma, TLObject value) {
		String associationName = WrapperMetaAttributeUtil.getAssociationName(ma);
		return hasWrappersWithValue(ma, associationName, value);
	}

	/**
	 * The order of the arguments is switched compared to #hasWrappersWithValues(Wrapper, Iterable),
	 * to solve a conflict caused by equal raw-type-signatures of these two methods.
	 * <p>
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * </p>
	 * 
	 * @deprecated Use {@link #hasWrappersWithValues(TLObject, Iterable)} instead, as the
	 *             {@link TLStructuredTypePart} object is needed. If only the id is given, the object will
	 *             be looked up internally, which can normally be avoided as the callers already
	 *             knows the object.
	 */
	@Deprecated
	public static boolean hasWrappersWithValues(Set<TLID> attributeIds, TLObject value) {
		return hasWrappersWithValues(value, lookupMetaAttributes(attributeIds));
	}

	/**
	 * Helper for external use (filters) to check if there are Wrappers that have the given one as
	 * value in any of the given {@link TLStructuredTypePart}s.
	 * 
	 * @param attributes
	 *        May be <code>null</code> to indicate any {@link TLStructuredTypePart}
	 * @param aValue
	 *        the value. Must not be <code>null</code>.
	 * @return true if there is at least one wrapper that has the given one as value in any of the
	 *         given {@link TLStructuredTypePart}s.
	 */
	public static boolean hasWrappersWithValues(TLObject aValue, Iterable<? extends TLStructuredTypePart> attributes) {
		if (attributes == null) {
			return hasWrappersWithValue(aValue);
		}
		{
			KnowledgeBase kb = aValue.tKnowledgeBase();
			KnowledgeItem valueItem = aValue.tHandle();

			Set<TLID> definitionIds = getDefinitionIds(attributes);
			RevisionQueryArguments args = contextArgs(aValue).setArguments(valueItem, definitionIds);
			try (CloseableIterator<KnowledgeAssociation> result =
				kb.searchStream(HAS_VALUE_IN_ATTRIBUTES_QUERY, args)) {
				return result.hasNext();
			}
		}
    }

	private static Set<TLStructuredTypePart> lookupMetaAttributes(Iterable<? extends TLID> attributeIds) {
		Set<TLStructuredTypePart> result = set();
		for (TLID id : attributeIds) {
			result.add(lookupMetaAttribute(id));
		}
		return result;
	}

	private static TLStructuredTypePart lookupMetaAttribute(TLID aMetaAttributeObjectName) {
		return (TLStructuredTypePart) WrapperFactory.getWrapper(aMetaAttributeObjectName, KBBasedMetaAttribute.OBJECT_NAME);
	}

    /**
     * Get the validity state of the given attributed.<br/>
     *
     * The returned value will be an overall value. When at least one attribute is invalid
     * (according to the validity time), the returned value will be red.<br/>
     * The rang is STATE_RED > STATE_YELLOW > STATE_GREEN > STATE_WHITE.
     *
     * @param anAttributed
     *        The attributed to be inspected, must not be <code>null</code>.
     * @return One value out of {@link MetaElementUtil#STATE_MAP}, never <code>null</code>.
     */
    public static String getValidityState(Wrapper anAttributed) {
        String theResult = MetaElementUtil.STATE_WHITE;
		Collection<? extends TLStructuredTypePart> parts = anAttributed.tType().getAllParts();

		for (Iterator theIt = parts.iterator(); !MetaElementUtil.STATE_RED.equals(theResult) && theIt.hasNext();) {
            TLStructuredTypePart theMA    = (TLStructuredTypePart) theIt.next();
            String        theState = WrapperMetaAttributeUtil.getValidityState(anAttributed, theMA);
            if (MetaElementUtil.STATE_RED.equals(theState)) {
                theResult = theState;
            }
            else if (MetaElementUtil.STATE_YELLOW.equals(theState)) {
                theResult = theState;
            }
            else if (MetaElementUtil.STATE_GREEN.equals(theState)) {
                if (MetaElementUtil.STATE_WHITE.equals(theResult)) {
                    theResult = theState;
                }
            }
        }
        return theResult;
    }


    /**
     * Get the validity state of the given attributed for the given meta attribute.
     *
     * If the given meta attribute is <code>null</code>, the returned value will be an
     * {@link #getValidityState(Wrapper) overall value}.
     *
     * @param aAttributed
     *        The attributed to be inspected, must not be <code>null</code>
     * @param aMA
     *        The meta attribute to check, may be <code>null</code>
     * @return One value out of {@link MetaElementUtil#STATE_MAP}, never <code>null</code>
     * @see #getValidityState(Wrapper)
     */
    public static String getValidityState(Wrapper aAttributed, TLStructuredTypePart aMA) {
        if (aMA == null) {
            return (WrapperMetaAttributeUtil.getValidityState(aAttributed));
        }
        ValidityCheck theValidityCheck = AttributeOperations.getValidityCheck(aMA);
        Date theNextTimeout = getNextTimeout(aAttributed, aMA);
        return getValidityState(theValidityCheck, theNextTimeout);
    }


    /**
     * Get the validity state of the given ValidityCheck for the given timeout date.
     *
     * @param aValidityCheck
     *        the ValidityCheck to use for the state computation
     * @param aTimeout
     *        the date on which the validity times out
     * @return One value out of {@link MetaElementUtil#STATE_MAP}, never <code>null</code>
     */
    public static String getValidityState(ValidityCheck aValidityCheck, Date aTimeout) {
        String theState = MetaElementUtil.STATE_WHITE;
        if (aValidityCheck.isActive()) {
            if (aTimeout != null && !ValidityCheck.INVALID_DATE.equals(aTimeout)) {
                long thePreTime = aTimeout.getTime() - aValidityCheck.getCheckDuration();
                long now = System.currentTimeMillis();
                if (now > aTimeout.getTime()) {
                    theState = MetaElementUtil.STATE_RED;
                } 
                else if (now > thePreTime) {
                    theState = MetaElementUtil.STATE_YELLOW;
                } 
                else {
                    theState = MetaElementUtil.STATE_GREEN;
                }
            }
        }
        return theState;
    }


    /**
     * Gets the next expiration date of the given meta attribute for the given attributed.
     * 
     * @param aAttributed
     *        the attributed whose attribute to check
     * @param aMA
     *        the meta attribute to check
     * @return the next expiration date of the given attribute or <code>null</code>, if
     *         the attribute doesn't expire (lastTouch of the attribute is INVALID_DATE /
     *         doesn't have an active validity check
     */
    public static Date getNextTimeout(Wrapper aAttributed, TLStructuredTypePart aMA) {
		Date lastTouch = AttributeOperations.getLastChangeDate(aAttributed, aMA);
        if ((lastTouch == null) && AttributeOperations.getValidityCheck(aMA).isActive()) {
            Long theTime = (Long)aAttributed.getValue(LifecycleAttributes.CREATED);
            lastTouch = (theTime != null) ? new Date(theTime.longValue()) : null;
        }
        return AttributeOperations.getValidityCheck(aMA).getNextTimeout(lastTouch);
    }
    
	/**
	 * Returns the id of the {@link TLStructuredTypePart} referenced in attribute
	 * {@link #META_ATTRIBUTE_ATTR}.
	 * <p>
	 * <em>Important:</em>Only the IDs of the {@link #getDefinitionId(TLStructuredTypePart) "root"
	 * attribute} is used in the database. "root" means the top-most attribute which is not
	 * overriding another attribute. This simplifies and therefore speeds up queries a lot.
	 * </p>
	 */
	public static TLID getMetaAttributeID(KnowledgeItem item) {
		MOReference reference = getMetaAttributeReference(item.getKnowledgeBase());
		ObjectKey metaAttributeKey = item.getReferencedKey(reference);
		TLID id;
		if (metaAttributeKey == null) {
			id = null;
		} else {
			id = metaAttributeKey.getObjectName();
		}
		return id;
	}

	/**
	 * Returns the {@link TLStructuredTypePart} referenced in attribute {@link #META_ATTRIBUTE_ATTR}.
	 */
	public static TLStructuredTypePart getMetaAttribute(KnowledgeItem item) throws NoSuchAttributeException {
		KnowledgeObject metaAttribute = (KnowledgeObject) item.getAttributeValue(META_ATTRIBUTE_ATTR);
		if (metaAttribute == null) {
			return null;
		}
		return (TLStructuredTypePart) WrapperFactory.getWrapper(metaAttribute);
	}

	/**
	 * Returns the {@link MOReference} which stores the {@link TLStructuredTypePart} for which the value is
	 * set.
	 * 
	 * @see #getMetaAttributeReference(KnowledgeBase)
	 */
	public static MOReference getMetaAttributeReference() {
		return getMetaAttributeReference(PersistencyLayer.getKnowledgeBase());

	}

	/**
	 * Returns the {@link MOReference} which stores the {@link TLStructuredTypePart} for which the value is
	 * set.
	 * 
	 * @see #getMetaAttributeReference()
	 */
	public static MOReference getMetaAttributeReference(KnowledgeBase kb) {
		MORepository typeRepository = kb.getMORepository();
		try {
			MetaObject wrapperAttributeValueType = typeRepository.getMetaObject(WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
			return MetaObjectUtils.getReference(wrapperAttributeValueType, META_ATTRIBUTE_ATTR);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unknown type " + WRAPPER_ATTRIBUTE_ASSOCIATION_BASE, ex);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("Unknown attribute " + META_ATTRIBUTE_ATTR + " in type "
				+ WRAPPER_ATTRIBUTE_ASSOCIATION_BASE, ex);
		}
	}

	private static RevisionQueryArguments contextArgs(TLObject contextValue) {
		KnowledgeBase kb = contextValue.tKnowledgeBase();
		KnowledgeItem valueItem = contextValue.tHandle();
		RevisionQueryArguments args = revisionArgs();
		args.setRequestedBranch(kb.getHistoryManager().getBranch(valueItem.getBranchContext()));
		args.setRequestedRevision(valueItem.getHistoryContext());
		return args;
	}

	/**
	 * Returns the name of the association in which the data for the given attribute are stored.
	 * 
	 * @throws IllegalArgumentException
	 *         if the {@link TLStructuredTypePart#getStorageImplementation()} is not an
	 *         {@link AssociationStorage}.
	 * 
	 * @see #getAssociationNameOrNull(TLStructuredTypePart)
	 */
	public static String getAssociationName(TLStructuredTypePart attribute) {
		String table = getAssociationNameOrNull(attribute);
		if (table == null) {
			StringBuilder error = new StringBuilder();
			error.append("Attribute '");
			error.append(attribute);
			error.append("' does not store the data in a separate table.");
			throw new IllegalArgumentException(error.toString());
		}
		return table;
	}

	/**
	 * Returns the name of the association in which the data for the given attribute are stored.
	 * 
	 * @return may be <code>null</code> in case {@link TLStructuredTypePart#getStorageImplementation()} is
	 *         not an {@link AssociationStorage}.
	 * 
	 * @see #getAssociationName(TLStructuredTypePart)
	 */
	public static String getAssociationNameOrNull(TLStructuredTypePart attribute) {
		StorageDetail storageDetail = AttributeOperations.getStorageImplementation(attribute);
		String table;
		if (storageDetail instanceof AssociationStorage) {
			table = ((AssociationStorage) storageDetail).getTable();
		} else {
			table = null;
		}
		return table;
	}

	/** Convenience variant of {@link #getDefinitionId(TLStructuredTypePart)}. */
	public static Set<TLID> getDefinitionIds(Iterable<? extends TLStructuredTypePart> attributes) {
		Set<TLID> definitions = set();
		for (TLStructuredTypePart original : CollectionUtil.nonNull(attributes)) {
			definitions.add(getDefinitionId(original));
		}
		return definitions;
	}

	/**
	 * @see TLStructuredTypePart#getDefinition()
	 */
	public static TLID getDefinitionId(TLStructuredTypePart attribute) {
		TLStructuredTypePart definition = getDefinition(attribute);
		return KBUtils.getWrappedObjectName(definition);
	}

	/**
	 * null, if and only if the given {@link TLStructuredTypePart} is null.
	 * 
	 * @see TLStructuredTypePart#getDefinition()
	 */
	public static TLStructuredTypePart getDefinition(TLStructuredTypePart attribute) {
		return attribute.getDefinition();
	}

	/**
	 * Live view of the given {@link TLStructuredTypePart}.
	 * <p>
	 * Changes to this {@link Collection} change directly the attribute value. The caller has to
	 * take care of the transaction handling.
	 * </p>
	 */
	public static Collection<?> getLiveCollection(TLObject owner, TLStructuredTypePart typePart) {
		/* The parameter is to general and casted here to simplify the code generated by the
		 * WrapperGenerator, which uses this method, a bit. */
		StorageImplementation storage = AttributeOperations.getStorageImplementation(owner, typePart);
		return storage.getLiveCollection(owner, typePart);
	}

	/**
	 * Module for {@link WrapperMetaAttributeUtil}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<WrapperMetaAttributeUtil> {
		
		/** Singleton {@link WrapperMetaAttributeUtil.Module} instance. */
		public static final Module INSTANCE = new WrapperMetaAttributeUtil.Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<WrapperMetaAttributeUtil> getImplementation() {
			return WrapperMetaAttributeUtil.class;
		}

	}

}
