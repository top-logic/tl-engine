/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.LinkTable;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LinkStorage} that keeps the order of objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
@Label("Sorted storage in separate table")
public class ListStorage<C extends ListStorage.Config<?>> extends LinkStorage<C> implements CompositeStorage {

	/**
	 * Configuration options for {@link ListStorage}.
	 */
	@TagName("list-storage")
	public interface Config<I extends ListStorage<?>> extends LinkStorage.Config<I> {
		// Pure marker interface.
	}

	private OrderedLinkQuery<KnowledgeAssociation> _outgoingQuery;

	/**
	 * Index of the links by their destination, or <code>null</code> for a
	 * {@link TLStructuredTypePart#isBag() bag}.
	 * 
	 * @see #containsAttributeValue(TLObject, TLStructuredTypePart, Object)
	 */
	private IndexedLinkQuery<KnowledgeItem, KnowledgeAssociation> _liveQuery;

	/**
	 * Creates a {@link ListStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ListStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void initReference(TLStructuredTypePart attribute) {
		_outgoingQuery = createLiveListQuery(attribute);
		_liveQuery = attribute.isBag() ? null : LinkStorageUtil.createLiveQuery(attribute, this);
		super.initReference(attribute);
	}

	@Override
	public OrderedLinkQuery<KnowledgeAssociation> getOutgoingQuery() {
		return _outgoingQuery;
	}

	@Override
	public final void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		this.checkAddValue(object, attribute, newValue);

		newValue = toStorageObject(newValue);
		KnowledgeAssociation link =
			LinkStorageUtil.createWrapperAssociation(attribute, null, (TLObject) newValue, this);
		links(object).add(link);
	}

	private List<KnowledgeAssociation> links(TLObject base) {
		return AbstractWrapper.resolveLinks(base, this.getOutgoingQuery());
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		AttributeUtil.checkHasAttribute(object, attribute);
		this.checkBasicValue(object, attribute, aValue);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		this.checkRemoveValue(object, attribute, aValue);

		aValue = toStorageObject(aValue);

		if (!(aValue instanceof TLObject)) {
			return;
		}

		try {
			List<KnowledgeAssociation> links = links(object);
			KnowledgeItem item = ((TLObject) aValue).tHandle();
			ObjectKey itemKey = item.tId();

			boolean changed = false;
			for (KnowledgeAssociation link : new ArrayList<>(links)) {
				if (itemKey.equals(link.getDestinationIdentity())) {
					DBKnowledgeAssociation.clearReferencesAndRemoveLink(link);
					changed = true;
					break;
				}
			}

			if (changed) {
				AttributeOperations.touch(object, attribute);
			}
		} catch (DataObjectException ex) {
			throw new AttributeException("Failed to remove value from '" + attribute + "': " + aValue, ex);
		}
	}

	/**
	 * For an attribute that is no {@link TLStructuredTypePart#isBag() bag}, the value is looked up
	 * by its {@link TLObject#tHandle() handle} in the indexed live link map of the object, at
	 * constant cost per element (see
	 * {@link AbstractWrapper#resolveLinks(TLObject, com.top_logic.knowledge.service.db2.AbstractAssociationQuery)}).
	 * For a bag, the whole value is read.
	 * 
	 * <p>
	 * The index holds one link per destination, since a reference that is no bag holds each value
	 * at most once.
	 * </p>
	 */
	@Override
	public boolean containsAttributeValue(TLObject object, TLStructuredTypePart attribute, Object entry) {
		if (_liveQuery == null) {
			return super.containsAttributeValue(object, attribute, entry);
		}
		if (entry == null) {
			return false;
		}
		return LinkStorageUtil.linkTo(AbstractWrapper.resolveLinks(object, _liveQuery), toStorageObject(entry)) != null;
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check if the value exists in the current collection
		List<KnowledgeAssociation> links = links(object);
		aValue = toStorageObject(aValue);
		for (KnowledgeAssociation link : links) {
			if (Utils.equals(aValue, link.getDestinationObject().getWrapper())) {
				checkMandatoryRemove(attribute, links.size());
				return;
			}
		}
		throw new IllegalArgumentException("Given value is not used and therefore cannot be removed from " + this);
	}

	@Override
	public Object getAttributeValue(TLObject base, TLStructuredTypePart attribute) {
		try {
			return tryGetAttributeValue(base);
		} catch (InvalidLinkException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private Object tryGetAttributeValue(TLObject base) throws InvalidLinkException {
		// Get values from cache
		List result = new ArrayList<>(links(base));

		// Replace links with Wrappers in place
		for (int i = 0, size = result.size(); i < size; i++) {
			KnowledgeAssociation theKA = (KnowledgeAssociation) result.get(i);
			result.set(i, toBusinessObject(theKA.getDestinationObject().getWrapper()));
		}
		return result;
	}

	@Override
	public void internalSetAttributeValue(TLObject base,
			TLStructuredTypePart attribute, Object newValue) throws NoSuchAttributeException,
			IllegalArgumentException, AttributeException {
		try {
			trySetAttributeValue(base, attribute, newValue);
		} catch (DataObjectException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	/**
	 * Updates the links to the given new values.
	 * 
	 * <p>
	 * The longest prefix of the current links whose targets occur in the given values in the same
	 * order is kept. All other current links are deleted first. Then the links to the remaining new
	 * values are created and inserted into the sort-order gaps between the kept links. Therefore,
	 * at no point in time two links point to the same target, unless the new values contain a
	 * target more than once.
	 * </p>
	 */
	private void trySetAttributeValue(TLObject base, TLStructuredTypePart attribute, Object newValue)
			throws DataObjectException {
		if (newValue != null && !(newValue instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		Collection<?> newValues = newValue == null ? Collections.emptyList() : (Collection<?>) newValue;

		List<KnowledgeAssociation> links = links(base);

		// Match the new values against the current links: For each kept link, the new values in
		// front of it.
		List<List<TLObject>> valuesBeforeKept = list();
		List<TLObject> pending = list();
		int keptCount = 0;
		for (Object value : newValues) {
			TLObject src = (TLObject) toStorageObject(value);
			if (keptCount < links.size()
				&& links.get(keptCount).getDestinationIdentity().equals(src.tHandle().tId())) {
				valuesBeforeKept.add(pending);
				pending = list();
				keptCount++;
			} else {
				pending.add(src);
			}
		}

		// Delete the links that are not kept, before creating new links.
		for (int n = links.size() - 1; n >= keptCount; n--) {
			DBKnowledgeAssociation.clearReferencesAndRemoveLink(links.get(n));
		}

		// Insert the new values in front of the kept links, inserted together to space them evenly
		// in the sort-order gap before that link.
		int destPos = 0;
		for (List<TLObject> values : valuesBeforeKept) {
			destPos = insertAll(links, destPos, createLinks(attribute, values));
			destPos++;
		}
		links.addAll(createLinks(attribute, pending));
	}

	private List<KnowledgeAssociation> createLinks(TLStructuredTypePart attribute, List<TLObject> values) {
		List<KnowledgeAssociation> newLinks = list();
		for (TLObject value : values) {
			newLinks.add(LinkStorageUtil.createWrapperAssociation(attribute, null, value, this));
		}
		return newLinks;
	}

	/**
	 * Inserts the given new links at the given position.
	 * 
	 * @return The position behind the inserted links.
	 */
	private static int insertAll(List<KnowledgeAssociation> links, int destPos,
			List<KnowledgeAssociation> newLinks) {
		if (newLinks.isEmpty()) {
			return destPos;
		}
		links.addAll(destPos, newLinks);
		return destPos + newLinks.size();
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection aValues)
			throws TopLogicException {
		if (object != null) {
			// Check attribute
			AttributeUtil.checkHasAttribute(object, attribute);

			if (aValues != null) {
				// Check the single values
				Iterator theVals = aValues.iterator();
				while (theVals.hasNext()) {
					Object theValue = theVals.next();
					this.checkBasicValue(object, attribute, theValue);
				}
			}
		}
	}

	@Override
	protected boolean supportsLiveCollectionsInternal() {
		return true;
	}

	@Override
	public List<?> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		return AbstractWrapper.resolveOrderedValue(object, getOutgoingQuery(), TLObject.class);
	}

	private OrderedLinkQuery<KnowledgeAssociation> createLiveListQuery(TLStructuredTypePart attribute) {
		Class<KnowledgeAssociation> expectedType = KnowledgeAssociation.class;
		String associationTypeName = getTable();
		String associationEndName = DBKnowledgeAssociation.REFERENCE_SOURCE_NAME;
		String orderAttributeName = getConfig().getOrderAttribute();
		Map<String, ?> filter;
		if (monomophicTable()) {
			filter = Collections.emptyMap();
		} else {
			filter = Collections.singletonMap(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR,
				attribute.getDefinition().tHandle());
		}
		boolean liveResult = true;
		return AssociationQuery.createOrderedLinkQuery(attribute.toString(), expectedType,
			associationTypeName, associationEndName, orderAttributeName, filter, liveResult);
	}

	@Override
	public ContainerStorage getContainerStorage(TLReference reference) {
		return new LinkTable(getTable());
	}

	/**
	 * Creates a configuration for the {@link ListStorage}.
	 *
	 * @param composite
	 *        Whether the reference is a composition.
	 * @param historyType
	 *        The history type of the value of the reference.
	 * @param deletionPolicy
	 *        The deletion policy of the reference.
	 * @param unversioned
	 *        Whether reference values must be stored unversioned.
	 * @return The storage configuration.
	 */
	public static Config<?> listConfig(boolean composite, HistoryType historyType, DeletionPolicy deletionPolicy, boolean unversioned) {
		return defaultConfig(Config.class, composite, historyType, deletionPolicy, unversioned);
	}

}
