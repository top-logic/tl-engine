/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.storage.LinkStorage.LinkStorageConfig;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.LinkTable;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.export.SinglePreloadContribution;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * {@link AbstractStorage} that stores a singleton references as association link.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Storage in separate table")
public class SingletonLinkStorage<C extends SingletonLinkStorage.Config<?>> extends TLItemStorage<C>
		implements AssociationStorage, CompositeStorage {

	/**
	 * Configuration options for {@link SingletonLinkStorage}.
	 */
	@TagName("singleton-link-storage")
	public interface Config<I extends SingletonLinkStorage<?>> extends TLItemStorage.Config<I>, LinkStorageConfig {
		// Pure marker interface.
	}

	private AssociationSetQuery<KnowledgeAssociation> _outgoingQuery;

	private AssociationSetQuery<KnowledgeAssociation> _incomingQuery;

	private PreloadContribution _preload;

	private PreloadContribution _reversePreload;

	/**
	 * Creates a {@link SingletonLinkStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SingletonLinkStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * Creates a configuration for {@link SingletonLinkStorage}.
	 *
	 * @param composite
	 *        Whether the reference is a composition.
	 * @param historyType
	 *        The history type of the value of the reference.
	 * @return The storage configuration.
	 */
	public static Config<?> singletonLinkConfig(boolean composite, HistoryType historyType) {
		return LinkStorage.defaultConfig(Config.class, composite, historyType);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_outgoingQuery = LinkStorageUtil.createOutgoingQuery(attribute, this);
		_incomingQuery = LinkStorageUtil.createIncomingQuery(attribute, this);

		_preload = new SinglePreloadContribution(new AssociationNavigationPreload(getOutgoingQuery()));
		_reversePreload = new SinglePreloadContribution(new AssociationNavigationPreload(getIncomingQuery()));
	}

	@Override
	protected Object getReferencedTLObject(TLObject self, TLStructuredTypePart attribute) {
		Iterator<? extends TLObject> it = AbstractWrapper.resolveWrappers(self, getOutgoingQuery()).iterator();
		if (it.hasNext()) {
			return it.next();
		} else {
			return null;
		}
	}

	@Override
	protected void checkSetValue(TLObject object, TLStructuredTypePart attribute, Object simpleValue) {
		super.checkSetValue(object, attribute, simpleValue);
		// Check attribute existence
		if (object != null) {
			AttributeUtil.checkHasAttribute(object, attribute);
		}
	}

	@Override
	protected void storeReferencedTLObject(TLObject object, TLStructuredTypePart attribute, Object oldValue,
			Object newValue) throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		try {
			// get old association if existing
			KnowledgeAssociation link = null;
			Iterator<KnowledgeAssociation> theKAs =
				AbstractWrapper.resolveLinks(object, getOutgoingQuery()).iterator();
			if (theKAs.hasNext()) {
				link = theKAs.next();
			}

			// delete it
			if (link != null) {
				DBKnowledgeAssociation.clearDestinationAndRemoveLink(link);
			}

			// create new association
			if (newValue != null) {
				LinkStorageUtil.createWrapperAssociation(attribute, object, (TLObject) newValue, this);
			}
		} catch (DataObjectException e) {
			Logger.error("Problem setting attribute " + this
				+ " to value " + newValue, e, this);
			throw new AttributeException("Problem setting attribute " + this
				+ " to value " + newValue, e);
		}
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		return AbstractWrapper.resolveWrappersTyped(self, getIncomingQuery(), TLObject.class);
	}

	@Override
	public PreloadContribution getPreload() {
		return _preload;
	}

	@Override
	public PreloadContribution getReversePreload() {
		return _reversePreload;
	}

	@Override
	public String getTable() {
		return ((Config<?>) getConfig()).getTable();
	}

	@Override
	public boolean monomophicTable() {
		return ((Config<?>) getConfig()).isMonomorphicTable();
	}

	@Override
	public AssociationSetQuery<KnowledgeAssociation> getIncomingQuery() {
		return _incomingQuery;
	}

	@Override
	public AssociationSetQuery<KnowledgeAssociation> getOutgoingQuery() {
		return _outgoingQuery;
	}

	@Override
	public ContainerStorage getContainerStorage(TLReference reference) {
		return new LinkTable(getTable());
	}

}
