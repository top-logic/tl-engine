/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.KeyValueBuffer;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.kbbased.PersistentObjectImpl;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractKnowledgeItemFactory;
import com.top_logic.knowledge.service.db2.AbstractQueryCache;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.knowledge.service.db2.MOKnowledgeItem;
import com.top_logic.knowledge.service.db2.QueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.db2.StaticImmutableKnowledgeObject;
import com.top_logic.knowledge.service.db2.StaticKnowledgeObject;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.internal.AbstractPersistentQuery;

/**
 * Utilities for linking singleton objects to {@link PersistentModule}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersistentModuleSingletons extends AbstractPersistentQuery implements TLModuleSingletons {

	/**
	 * Name of the association associating {@link TLModule}s with their singletons.
	 */
	public static final String OBJECT_NAME = TlModelFactory.KO_NAME_TL_MODULE_SINGLETONS;

	/**
	 * Attribute of the {@link #OBJECT_NAME association} pointing to the {@link TLModule}.
	 */
	public static final String SOURCE_ATTR = "module";

	/**
	 * Attribute of the {@link #OBJECT_NAME association} pointing to the singleton.
	 */
	public static final String DEST_ATTR = "singleton";

	/**
	 * Attribute of the {@link #OBJECT_NAME association} storing the technical singleton name.
	 */
	public static final String NAME_ATTR = "name";

	/**
	 * Assigns the given singleton to the given {@link TLModule} with the given name.
	 */
	private static void createLink(TLModule module, String name, TLObject singleton) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try {
			kb.createKnowledgeItem(OBJECT_NAME,
				new KeyValueBuffer<String, Object>()
					.put(SOURCE_ATTR, module.tHandle())
					.put(DEST_ATTR, singleton.tHandle())
					.put(NAME_ATTR, name),
				KnowledgeItem.class);
		} catch (DataObjectException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	/**
	 * Factory creating {@link com.top_logic.model.TLModuleSingleton}s.
	 */
	@FrameworkInternal
	public static final class Factory extends AbstractKnowledgeItemFactory {

		/**
		 * Singleton {@link Factory} instance.
		 */
		public static final Factory INSTANCE = new Factory();

		private Factory() {
			// Singleton constructor.
		}

		@Override
		protected LinkImpl internalNewItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
			return new LinkImpl(kb, staticType);
		}

		@Override
		protected ImmutableLink internalNewImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
			return new ImmutableLink(kb, staticType);
		}
	}

	/**
	 * Handle for mutable {@link com.top_logic.model.TLModuleSingleton} objects.
	 */
	public static final class LinkImpl extends StaticKnowledgeObject implements TLModuleSingleton {

		/**
		 * Creates a {@link LinkImpl}.
		 */
		public LinkImpl(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
			super(kb, staticType);
		}

		@Override
		public TLModule getModule() {
			return LinkImpl.getModule(this);
		}

		static TLModule getModule(KnowledgeItem self) {
			try {
				return (TLModule) ((KnowledgeItem) self.getAttributeValue(SOURCE_ATTR)).getWrapper();
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		@Override
		public TLObject getSingleton() {
			return LinkImpl.getSingletons(this);
		}

		static TLObject getSingletons(KnowledgeItem self) {
			try {
				return ((KnowledgeItem) self.getAttributeValue(DEST_ATTR)).getWrapper();
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		@Override
		public String getName() {
			return LinkImpl.getName(this);
		}

		static String getName(KnowledgeItem self) {
			try {
				return (String) self.getAttributeValue(NAME_ATTR);
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		@Override
		public TLClass tType() {
			return TlModelFactory.getTLModuleSingletonsType();
		}

		@Override
		public Set<String> getAllAttributeNames() {
			Set<String> theResult = super.getAllAttributeNames();

			for (TLStructuredTypePart part : tType().getAllParts()) {
				theResult.add(part.getName());
			}

			return theResult;
		}

		@Override
		public TLObject tContainer() {
			return PersistentObjectImpl.tContainer(this);
		}

		@Override
		public TLReference tContainerReference() {
			return PersistentObjectImpl.tContainerReference(this);
		}

		@Override
		public Object tValue(TLStructuredTypePart part) {
			return PersistentObjectImpl.getValue(this, part);
		}

		@Override
		public void tUpdate(TLStructuredTypePart part, Object value) {
			PersistentObjectImpl.setValue(this, part, value);
		}

	}

	/**
	 * Handle for immutable {@link com.top_logic.model.TLModuleSingleton} objects.
	 */
	public static final class ImmutableLink extends StaticImmutableKnowledgeObject implements TLModuleSingleton {

		/**
		 * Creates a new {@link ImmutableLink}.
		 */
		public ImmutableLink(DBKnowledgeBase kb, MOKnowledgeItem type) {
			super(kb, type);
		}

		@Override
		public TLModule getModule() {
			return LinkImpl.getModule(this);
		}

		@Override
		public TLObject getSingleton() {
			return LinkImpl.getSingletons(this);
		}

		@Override
		public String getName() {
			return LinkImpl.getName(this);
		}

		@Override
		public TLClass tType() {
			return TlModelFactory.getTLModuleSingletonsType();
		}

		@Override
		public Set<String> getAllAttributeNames() {
			Set<String> theResult = super.getAllAttributeNames();

			for (TLStructuredTypePart part : tType().getAllParts()) {
				theResult.add(part.getName());
			}

			return theResult;
		}

		@Override
		public TLObject tContainer() {
			return PersistentObjectImpl.tContainer(this);
		}

		@Override
		public TLReference tContainerReference() {
			return PersistentObjectImpl.tContainerReference(this);
		}

		@Override
		public Object tValue(TLStructuredTypePart part) {
			return PersistentObjectImpl.getValue(this, part);
		}

		@Override
		public void tUpdate(TLStructuredTypePart part, Object value) {
			PersistentObjectImpl.setValue(this, part, value);
		}

	}

	private static final AssociationSetQuery<TLModuleSingleton> LINK_FOR_SINGLETON_ATTR = AssociationQuery.createQuery(
		"linkForSingleton",
		TLModuleSingleton.class,
		OBJECT_NAME,
		DEST_ATTR
		);

	private static final AssociationSetQuery<TLModuleSingleton> LINK_FOR_MODULE_ATTR = AssociationQuery.createQuery(
		"linkForModule",
		TLModuleSingleton.class,
		OBJECT_NAME,
		SOURCE_ATTR
		);
	
	private static final NamedConstant LINK_BY_NAME_IDENTIFIER = new NamedConstant("linkByName");

	private static final IndexedLinkQuery<String, TLModuleSingleton> LINK_BY_NAME_LIVE = IndexedLinkQuery.indexedLinkQuery(
		LINK_BY_NAME_IDENTIFIER, TLModuleSingleton.class, OBJECT_NAME, SOURCE_ATTR, NAME_ATTR, String.class,
		null, true);

	private QueryCache<TLModuleSingleton> _allSingletons;

	@Override
	public void init(KnowledgeBase kb) throws DataObjectException {
		super.init(kb);

		MetaObject linkType = kb.getMORepository().getType(OBJECT_NAME);
		SimpleQuery<TLModuleSingleton> query = SimpleQuery.queryUnresolved(TLModuleSingleton.class, linkType, literal(true));
		_allSingletons = AbstractQueryCache.newQueryCache(kb, query);
	}

	@Override
	public Collection<TLModuleSingleton> getAllSingletons() {
		return _allSingletons.getValue();
	}

	@Override
	public Collection<TLModuleSingleton> getSingletons(TLModule module) {
		KnowledgeObject moduleKI = (KnowledgeObject) module.tHandle();
		return kb().resolveLinks(moduleKI, LINK_FOR_MODULE_ATTR);
	}

	@Override
	public TLObject getSingleton(TLModule module, String name) {
		TLModuleSingleton link = links(module).get(name);
		if (link == null) {
			return null;
		}

		return link.getSingleton();
	}

	@Override
	public TLObject removeSingleton(TLModule module, String name) {
		TLModuleSingleton oldLink = links(module).remove(name);
		if (oldLink == null) {
			return null;
		}
		TLObject oldSingleton = oldLink.getSingleton();
		oldLink.tDelete();
		return oldSingleton;
	}

	@Override
	public TLObject addSingleton(TLModule module, String name, TLObject newSingleton) {
		TLObject oldSingleton = removeSingleton(module, name);
		createLink(module, name, newSingleton);
		return oldSingleton;
	}

	private BidiMap<String, TLModuleSingleton> links(TLModule module) {
		KnowledgeObject moduleKI = (KnowledgeObject) module.tHandle();
		return kb().resolveLinks(moduleKI, LINK_BY_NAME_LIVE);
	}

	@Override
	public String getSingletonName(TLModule module, TLObject singleton) {
		TLModuleSingleton link = getModuleAndName(singleton);
		if (link != null && link.getModule() == module) {
			return link.getName();
		}
		return null;
	}

	@Override
	public TLModuleSingleton getModuleAndName(TLObject singleton) {
		KnowledgeObject singletonKI = (KnowledgeObject) singleton.tHandle();
		Set<TLModuleSingleton> links = kb().resolveLinks(singletonKI, LINK_FOR_SINGLETON_ATTR);
		return CollectionUtil.getSingleValueFromCollection(links);
	}

	/** 
	 * Access {@link Expression} to source attribute. 
	 */
	public static Expression sourceAttribute() {
		return reference(OBJECT_NAME, SOURCE_ATTR);
	}

	/** 
	 * Access {@link Expression} to dest attribute. 
	 */
	public static Expression destAttribute() {
		return reference(OBJECT_NAME, DEST_ATTR);
	}

	/** 
	 * Access {@link Expression} to name attribute. 
	 */
	public static Expression nameAttribute() {
		return attribute(OBJECT_NAME, NAME_ATTR);
	}

	/** 
	 * Access {@link Expression} to name attribute. 
	 */
	public static SetExpression allOfType() {
		return allOf(OBJECT_NAME);
	}

}
