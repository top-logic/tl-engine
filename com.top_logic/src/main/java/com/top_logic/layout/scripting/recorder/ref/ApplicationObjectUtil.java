/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.col.Maybe;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.generated.TlModelFactory;

/**
 * Utilities for working with {@link KnowledgeObject}s and {@link TLObject}s.
 * 
 * @see "com.top_logic.element.layout.scripting.ScriptingElementUtil for further utilities that require classes from Element."
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ApplicationObjectUtil {

	/**
	 * Suffix to a {@link MetaObject} name for building the model class that represents this table.
	 */
	@Deprecated
	private static final String TABLE_TYPE_NAME_SUFFIX = "Table";

	private static final String TABLE_TYPE_INTERFACE_NAME_SUFFIX = TABLE_TYPE_NAME_SUFFIX + "Interface";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.model.PersistentModule#TL_LEGACY_TABLETYPES_MODULE"
	 */
	@Deprecated
	public static final String LEGACY_TABLE_TYPES_MODULE = "tl.legacy.tabletypes";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.model.PersistentModule#TL_LEGACY_TABLETYPES_INTERFACE_MODULE"
	 */
	@Deprecated
	public static final String TL_TABLES_MODULE = "tl.tables";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.model.PersistentModule#OBJECT_TYPE"
	 */
	public static final String MODULE_OBJECT_TYPE = TlModelFactory.KO_NAME_TL_MODULE;

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.model.PersistentModule#MODEL_ATTR"
	 */
	public static final String MODULE_MODEL_ATTR = "model";

	private static final String NAME_ATTRIBUTE = AbstractWrapper.NAME_ATTRIBUTE;

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.KBBasedMetaAttribute#META_ELEMENT_ATTR"
	 */
	public static final String META_ELEMENT_ATTR = "owner";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.KBBasedMetaAttribute#OBJECT_NAME"
	 */
	public static final String META_ATTRIBUTE_OBJECT_TYPE = "MetaAttribute";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.KBBasedMetaElement#META_ELEMENT_TYPE"
	 */
	public static final String META_ELEMENT_ME_TYPE_ATTR = "name";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.KBBasedMetaElement#SCOPE_REF"
	 */
	public static final String META_ELEMENT_SCOPE_REF = "scope";

	/**
	 * Table, where {@link TLClass} generalizations are stored.
	 */
	public static final String META_ELEMENT_GENERALIZATIONS = "MetaElement_generalizations";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.model.PersistentTypet#MODULE_REF"
	 */
	public static final String META_ELEMENT_MODULE_REF = "module";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.KBBasedMetaElement#META_ELEMENT_KO"
	 */
	public static final String META_ELEMENT_OBJECT_TYPE = "MetaElement";

	/**
	 * Constants actually defined in tl-element.
	 * 
	 * @see "com.top_logic.element.structured.wrap.StructuredElementWrapper#CHILD_ASSOCIATION"
	 */
	public static final String STRUCTURE_CHILD_ASSOCIATION = "hasStructureChild";

	/**
	 * Abstract KA being the root of all such derived wrapper attribute associations.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE"
	 */
	public static final String WRAPPER_ATTRIBUTE_ASSOCIATION_BASE = "hasWrapperAttValueBaseAssociation";

	/**
	 * KA used to connect MetaElemented KO to the Wrapper KO.
	 * 
	 * @see "com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil#WRAPPER_ATTRIBUTE_ASSOCIATION"
	 */
	public static final String WRAPPER_ATTRIBUTE_ASSOCIATION = "hasWrapperAttValue";

	/**
	 * Name of the table in which references to historic items are stored.
	 * 
	 * @see HistoryType#HISTORIC
	 */
	public static final String HISTORIC_WRAPPER_ATTRIBUTE_ASSOCIATION = "hasHistoricValue";

	/**
	 * Name of the table in which references to both historical and current items can be stored.
	 * 
	 * @see HistoryType#MIXED
	 */
	public static final String MIXED_WRAPPER_ATTRIBUTE_ASSOCIATION = "hasMixedValue";

	/**
	 * KA attribute used to store {@link TLStructuredTypePart}.
	 * <p>
	 * <em>Important:</em>Only the IDs of the "root" attribute} is used in the database. "root"
	 * means the top-most attribute which is not overriding another attribute. This simplifies and
	 * therefore speeds up queries a lot.
	 * </p>
	 * 
	 * @see "com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil#META_ATTRIBUTE_ATTR"
	 */
	public static final String META_ATTRIBUTE_ATTR = "metaAttribute";

	/** Default set containing the names of the {@link MetaObject} that hold {@link TLModelPart} */
	public static final Set<String> DEFAULT_MODEL_PART_TYPE_NAMES;
	static {
		Set<String> modelPartTypeNames = new HashSet<>();
		// TLEnumeration
		modelPartTypeNames.add(FastList.OBJECT_NAME);
		// TLClassifier
		modelPartTypeNames.add(FastListElement.OBJECT_NAME);
		// TLModule
		modelPartTypeNames.add(ApplicationObjectUtil.MODULE_OBJECT_TYPE);
		// TLStructuredType
		modelPartTypeNames.add(ApplicationObjectUtil.META_ELEMENT_OBJECT_TYPE);
		// TLStructuredTypePart
		modelPartTypeNames.add(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE);
		DEFAULT_MODEL_PART_TYPE_NAMES = Collections.unmodifiableSet(modelPartTypeNames);
	}

	private static final AssociationSetQuery<TLObject> META_ELEMENTS_QUERY =
		AssociationQuery.createQuery("definedTypes", TLObject.class, META_ELEMENT_OBJECT_TYPE, META_ELEMENT_SCOPE_REF);

	/**
	 * The association that classifies model elements for attribute security.
	 */
	public static final String KA_CLASSIFIED_BY = "classifiedBy";

	public static KnowledgeObject findGlobalType(KnowledgeBase kb, String typeName) throws UnknownTypeException {
		KnowledgeObject globalType = null;
		
		Iterator<KnowledgeItem> meIt = kb.getObjectsByAttribute(META_ELEMENT_OBJECT_TYPE, META_ELEMENT_ME_TYPE_ATTR, typeName);
		while (meIt.hasNext()) {
			KnowledgeObject type = (KnowledgeObject) meIt.next();
			
			if (getScope(type).tTable().getName().equals(MODULE_OBJECT_TYPE)) {
				assert globalType == null : "Ambiguous global type '" + typeName + "'.";
				globalType = type;
			}
		}

		if (globalType == null) {
			throw new RuntimeException("Global type '" + typeName + "' cannot be found.");
		}
		return globalType;
	}

	public static KnowledgeObject getScope(KnowledgeObject type) {
		try {
			return (KnowledgeObject) type.getAttributeValue(META_ELEMENT_SCOPE_REF);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static KnowledgeObject findType(KnowledgeObject holderKo, String typeName) throws NoSuchAttributeException,
			InvalidLinkException {
		Set<TLObject> definedTypes = holderKo.getKnowledgeBase().resolveLinks(holderKo, META_ELEMENTS_QUERY);
		for (TLObject type : definedTypes) {
			KnowledgeItem typeHandle = type.tHandle();
			if (typeName.equals(typeHandle.getAttributeValue(META_ELEMENT_ME_TYPE_ATTR))) {
				return (KnowledgeObject) typeHandle;
			}
		}

		throw new RuntimeException("Type '" + typeName + "' cannot be found in scope '" + holderKo + "'.");
	}

	public static KnowledgeObject navigateBackwards(KnowledgeObject ko, String associationName)
			throws InvalidLinkException {
		Iterator<KnowledgeAssociation> holdsMEIt = ko.getIncomingAssociations(associationName);
		if (holdsMEIt.hasNext()) {
			KnowledgeAssociation link = holdsMEIt.next();
			
			return link.getSourceObject();
		} else {
			return null;
		}
	}

	public static Maybe<TLObject> getChild(TLObject node, String name) {
		{
			Maybe<KnowledgeObject> child = getChild((KnowledgeObject) node.tHandle(), name);
			if (!child.hasValue()) {
				return Maybe.none();
			}
			return Maybe.toMaybe(WrapperFactory.getWrapper(child.get()));
		}
	}

	public static Maybe<KnowledgeObject> getChild(KnowledgeObject node, String name) {
		try {
			KnowledgeObject match = null;
			Iterator<KnowledgeAssociation> hasChildIterator = node.getOutgoingAssociations(STRUCTURE_CHILD_ASSOCIATION);
			while (hasChildIterator.hasNext()) {
				KnowledgeAssociation link = hasChildIterator.next();
				KnowledgeObject child = link.getDestinationObject();

				if (name.equals(child.getAttributeValue(NAME_ATTRIBUTE))) {
					if (match != null) {
						return Maybe.none();
					}
					match = child;
				}
			}
			return Maybe.toMaybe(match);
		} catch (InvalidLinkException exception) {
			throw new RuntimeException(exception);
		} catch (NoSuchAttributeException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Name of the table which the given table types uses.
	 * 
	 * @param tableType
	 *        Type with a name created by {@link #tableTypeName(String)}
	 * 
	 * @see #tableTypeName(String)
	 * @see #iTableName(TLType)
	 * @see #iTableTypeName(String)
	 * @see "com.top_logic.element.model.DynamicModelService"
	 */
	public static String tableName(TLType tableType) {
		return tableName(tableType.getName());
	}

	/**
	 * Name of the table which the table type with the given name uses.
	 * 
	 * @param tableTypeName
	 *        Name of the type with created by {@link #tableTypeName(String)}
	 */
	public static String tableName(String tableTypeName) {
		return tableTypeName.substring(0, tableTypeName.length() - TABLE_TYPE_NAME_SUFFIX.length());
	}

	/**
	 * Name of the table which the given table types uses.
	 * 
	 * @param tableType
	 *        Type with a name created by {@link #iTableTypeName(String)}
	 * 
	 * @see #tableName(TLType)
	 * @see #tableTypeName(String)
	 * @see #iTableTypeName(String)
	 * @see "com.top_logic.element.model.DynamicModelService"
	 */
	public static String iTableName(TLType tableType) {
		String name = tableType.getName();
		return name.substring(0, name.length() - TABLE_TYPE_INTERFACE_NAME_SUFFIX.length());
	}

	/**
	 * Name of the {@link TLType} that writes in the given table.
	 * 
	 * @see #tableName(TLType)
	 * @see #iTableName(TLType)
	 * @see #iTableTypeName(String)
	 * @see "com.top_logic.element.model.DynamicModelService"
	 */
	public static String tableTypeName(String tableName) {
		return tableName + TABLE_TYPE_NAME_SUFFIX;
	}

	/**
	 * Name of the {@link TLType} that writes in the given table.
	 * 
	 * @see #tableName(TLType)
	 * @see #tableTypeName(String)
	 * @see #iTableName(TLType)
	 * @see "com.top_logic.element.model.DynamicModelService"
	 */
	public static String iTableTypeName(String tableName) {
		return tableName + TABLE_TYPE_INTERFACE_NAME_SUFFIX;
	}

	/**
	 * Qualified name of the {@link TLType} that writes in the given table.
	 * 
	 * @see #tableTypeName(String)
	 */
	@Deprecated
	public static String tableTypeQName(MetaObject table) {
		return LEGACY_TABLE_TYPES_MODULE + ':' + tableTypeName(table.getName());
	}

}
