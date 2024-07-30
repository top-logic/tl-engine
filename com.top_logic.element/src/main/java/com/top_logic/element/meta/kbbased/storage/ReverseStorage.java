/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AbstractStorageBase;
import com.top_logic.element.meta.AssociationStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.NoPreload;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * {@link StorageImplementation} for an attribute representing the inverse navigation of another
 * reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReverseStorage<C extends ReverseStorage.Config<?>> extends AbstractStorageBase<C>
		implements /* optional */ AssociationStorage {

	private AssociationSetQuery<KnowledgeAssociation> _incomingQuery;

	private AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> _outgoingQuery;

	private String _table;

	private boolean _monomorphic;

	private PreloadContribution _preload = NoPreload.INSTANCE;

	private PreloadContribution _reversePreload = NoPreload.INSTANCE;

	/**
	 * Configuration options for {@link ReverseStorage}.
	 */
	@TagName("reverse-storage")
	public interface Config<I extends ReverseStorage<?>> extends AbstractStorageBase.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link ReverseStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReverseStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * New default configuration for an {@link ReverseStorage}.
	 */
	public static Config<?> defaultConfig() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute)
			throws AttributeException {
		TLAssociationEnd sourceEnd = ((TLReference) attribute).getEnd();
		TLAssociationEnd destinationEnd = TLModelUtil.getOtherEnd(sourceEnd);
		TLReference _destinationReference = destinationEnd.getReference();

		Set<? extends TLObject> result = object.tReferers(_destinationReference);
		if (sourceEnd.isMultiple()) {
			return result;
		} else {
			return CollectionUtil.getSingleValueFromCollection(result);
		}
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		TLAssociationEnd sourceEnd = reference.getEnd();
		TLAssociationEnd destinationEnd = TLModelUtil.getOtherEnd(sourceEnd);
		TLStructuredTypePart destinationReference = destinationEnd.getReference();
		StorageImplementation destStorage = AttributeOperations.getStorageImplementation(self, destinationReference);

		Object result;
		try {
			result = destStorage.getAttributeValue(self, destinationReference);
		} catch (AttributeException ex) {
			throw new RuntimeException(ex);
		}
		if (destinationEnd.isMultiple()) {
			return CollectionUtil.toSet((Collection<? extends TLObject>) result);
		} else {
			return CollectionUtilShared.singletonOrEmptySet((TLObject) result);
		}
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		TLAssociationEnd sourceEnd = ((TLReference) attribute).getEnd();
		TLAssociationEnd destinationEnd = TLModelUtil.getOtherEnd(sourceEnd);
		TLStructuredTypePart forwardRef = destinationEnd.getReference();
		StorageImplementation forwardStorage = AttributeOperations.getStorageImplementation(forwardRef);
		if (forwardStorage instanceof AssociationStorage) {
			AssociationStorage linkStorage = (AssociationStorage) forwardStorage;
			_outgoingQuery = linkStorage.getIncomingQuery();
			_table = _outgoingQuery.getAssociationTypeName();
			_monomorphic = linkStorage.monomophicTable();

			AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> otherOutgoing =
					linkStorage.getOutgoingQuery();
			if (otherOutgoing instanceof AssociationSetQuery<?>) {
				@SuppressWarnings("unchecked")
				AssociationSetQuery<KnowledgeAssociation> incomingQuery =
					(AssociationSetQuery<KnowledgeAssociation>) otherOutgoing;
				_incomingQuery = incomingQuery;
			} else {
				_incomingQuery = AssociationQuery.createOutgoingQuery(
					_outgoingQuery.getCacheKey().asString() + "Reverse",
					_table,
					_outgoingQuery.getAttributeQuery());
			}

			_preload = new AssociationNavigationPreload(_outgoingQuery);
			_reversePreload = new AssociationNavigationPreload(_incomingQuery);
		}

		super.init(attribute);
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
		return _table;
	}

	@Override
	public boolean monomophicTable() {
		return _monomorphic;
	}

	@Override
	public AbstractAssociationQuery<KnowledgeAssociation, ? extends Collection<KnowledgeAssociation>> getOutgoingQuery() {
		return _outgoingQuery;
	}

	@Override
	public AssociationSetQuery<KnowledgeAssociation> getIncomingQuery() {
		return _incomingQuery;
	}

	@Override
	public void checkUpdate(AttributeUpdate update) {
		return;
	}

	@Override
	public void update(AttributeUpdate update)
			throws AttributeException {
		// Ignore.
	}

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new UnsupportedOperationException("Reverse attributes cannot be updated directly.");
	}

	@Override
	public Object getUpdateValue(AttributeUpdate update)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new UnsupportedOperationException("Reverse attributes cannot be updated directly.");
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		throw new UnsupportedOperationException("Reverse attributes cannot be updated directly.");
	}

	@Override
	public void internalSetAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValues)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		throw new UnsupportedOperationException("Reverse attributes cannot be updated directly.");
	}

}
