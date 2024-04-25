/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.filt.DOAttributeComparator;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.objects.DestinationIterator;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link LinkStorage} that does not preserve the order of values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = TLStorage.REFERENCE_CLASSIFIER)
public class SetStorage<C extends SetStorage.Config<?>> extends LinkStorage<C> {

	private AssociationSetQuery<KnowledgeAssociation> _outgoingQuery;

	private IndexedLinkQuery<KnowledgeItem, KnowledgeAssociation> _liveQuery;

	private boolean _multiple;

	private boolean _ordered;

	/**
	 * {@link Comparator} to sort links by {@link Config#getOrderAttribute()}. May be
	 * <code>null</code>, when attribute is not ordered.
	 */
	private Comparator<? super KnowledgeAssociation> _orderComparator;


	/**
	 * Configuration options for {@link SetStorage}.
	 */
	@TagName("set-storage")
	public interface Config<I extends SetStorage<?>> extends LinkStorage.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link SetStorage} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SetStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void initReference(TLStructuredTypePart attribute) {
		_outgoingQuery = LinkStorageUtil.createOutgoingQuery(attribute, this);
		_liveQuery = LinkStorageUtil.createLiveQuery(attribute, this);

		super.initReference(attribute);

		_ordered = attribute.isOrdered();
		if (_ordered) {
			_orderComparator = orderComparator();
		}
		_multiple = attribute.isMultiple();
	}

	private DOAttributeComparator orderComparator() {
		return new DOAttributeComparator(orderAttribute(), DOAttributeComparator.ASCENDING);
	}

	private String orderAttribute() {
		return getConfig().getOrderAttribute();
	}

	@Override
	public AssociationSetQuery<KnowledgeAssociation> getOutgoingQuery() {
		return _outgoingQuery;
	}

	@Override
	public final void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		this.internalAddAttributeValue(object, attribute, aValue);

		this.resort(object);
	}

	protected void internalAddAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		// Check params
		this.checkAddValue(object, attribute, aValue);

		aValue = toStorageObject(aValue);

		// Create the association
		LinkStorageUtil.createWrapperAssociation(attribute, object, (TLObject) aValue, this);
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check values
		this.checkBasicValue(object, attribute, aValue);

		if (AbstractWrapper.resolveWrappers(object, getOutgoingQuery()).contains(toStorageObject(aValue))) {
			throw new IllegalArgumentException("'" + aValue + "' is already used and must not be added again to '"
				+ this + "'");
		}

		// Check single selection
		if (!_multiple && (((Collection<?>) this.getAttributeValue(object, attribute)).size() > 0)) {
			throw new IllegalArgumentException("Cannot set more than one value for single-select classification list");
		}
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		// Check params
		this.checkRemoveValue(object, attribute, aValue);

		// Remove value
		try {
			DestinationIterator theKAs =
				new DestinationIterator(AbstractWrapper.resolveLinks(object, getOutgoingQuery()).iterator());
			aValue = toStorageObject(aValue);
			KnowledgeItem theKO = ((TLObject) aValue).tHandle();
			boolean found = false;
			while (theKAs.hasNext()) {
				// Remove if matched
				if (theKAs.nextKO().equals(theKO)) { // Should always be true because it has been
														// checked by checkRemoveValue
					// This should not happen...
					if (found) {
						Logger.warn("Found multiple KAs for value - removing", this);
					}
					DBKnowledgeAssociation.clearDestinationAndRemoveLink(theKAs.currentKA());
					found = true;
				}
			}
			if (found) {
				AttributeOperations.touch(object, attribute);
			}
		} catch (Exception ex) {
			Logger.warn("Failed to remove value: " + ex, this);
			throw new AttributeException("Failed to remove value: " + ex, ex);
		}
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object aValue) throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		// Check values
		this.checkBasicValue(object, attribute, aValue);

		// Check if the value exists in the current collection
		Collection theWrappers = AbstractWrapper.resolveWrappers(object, getOutgoingQuery());
		if (!theWrappers.contains(toStorageObject(aValue))) {
			throw new IllegalArgumentException("Given value is not used and therefore cannot be removed from " + this);
		}

		checkMandatoryRemove(attribute, theWrappers);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) {
		Collection<? extends TLObject> referencedTLObjects = getReferencedTLObjects(object);
		if (storageMapping() == null) {
			return referencedTLObjects;
		}
		return mapObjects(referencedTLObjects, this::toBusinessObject);
	}

	@Override
	public Set<TLObject> getLiveCollection(TLObject object, TLStructuredTypePart attribute) {
		BidiMap<KnowledgeItem, KnowledgeAssociation> links = AbstractWrapper.resolveLinks(object, _liveQuery);
		return new LiveTargets(links, object, attribute);
	}

	@Override
	protected boolean supportsLiveCollectionsInternal() {
		return true;
	}

	/**
	 * Determines the {@link TLObject}s that are referenced by the given {@link TLObject} via the
	 * {@link #getOutgoingQuery() outgoing query}.
	 * 
	 * @param self
	 *        The {@link TLObject} to get references for.
	 */
	protected Collection<? extends TLObject> getReferencedTLObjects(TLObject self) {
		// Check attribute
//		this.checkHasAttribute(aMetaAttributed); removed for performance reasons

		// Get values from cache
		if (!_ordered) {
			Set<? extends TLObject> theWrappers = AbstractWrapper.resolveWrappers(self, getOutgoingQuery());
			if (theWrappers.isEmpty()) {
				return new HashSet<>();
			}
			return theWrappers;
		}
		else {
			List<KnowledgeAssociation> links = new ArrayList<>();
			links.addAll(AbstractWrapper.resolveLinks(self, getOutgoingQuery()));
			Collections.sort(links, _orderComparator);
			
			List<TLObject> theResult = new ArrayList<>(links.size());
			DestinationIterator theKAIt = new DestinationIterator(links.iterator());
			while (theKAIt.hasNext()) {
				theResult.add(WrapperFactory.getWrapper(theKAIt.nextKO()));
			}

			return theResult;
		}
	}

	/**
	 * Maps the entries in the given collection to a new {@link Collection} using the given
	 * function.
	 * 
	 * @param src
	 *        The source collection whose entries must be mapped.
	 * @param mapping
	 *        Mapping function.
	 * @return Either a {@link Set} or a {@link List} (depending whether the source collection is a
	 *         {@link Set} or not) containing the mapped entries.
	 */
	protected Collection<?> mapObjects(Collection<?> src, Function<? super Object, ? extends Object> mapping) {
		if (src.isEmpty()) {
			return src;
		}
		Collection<Object> dest;
		if (src instanceof Set<?>) {
			dest = new HashSet<>(src.size());
		} else {
			dest = new ArrayList<>(src.size());
		}
		for (Object value : src) {
			dest.add(mapping.apply(value));
		}
		return dest;
	}

	@Override
	public void internalSetAttributeValue(TLObject aMetaAttributed,
			TLStructuredTypePart attribute, Object aValues) throws NoSuchAttributeException,
			IllegalArgumentException, AttributeException {
		if (aValues != null && !(aValues instanceof Collection)) {
			throw new IllegalArgumentException("Given value is no Collection");
		}
		Collection<?> newValues = (Collection<?>) aValues;

		// Check which values to remove or to add
		Collection<?> currentValues = this.getReferencedTLObjects(aMetaAttributed);

		if (newValues == null || newValues.isEmpty()) {
			setEmptyValues(currentValues, aMetaAttributed);
			return;
		}

		newValues = mapObjects(newValues, this::toStorageObject);

		boolean isResorted = this.checkResort(currentValues, newValues);
		boolean changed = setNonEmptyValues(currentValues, aMetaAttributed, attribute, newValues);
		if (!isResorted && !changed) {
			return; // Noop
		}

		try {
			this.resort(aMetaAttributed, newValues);
		} catch (Exception ex) {
			Logger.error("Failed to finish setAttributeValues ", ex, this);
		}
	}

	private boolean checkResort(Collection someOld, Collection someNew) {
		if (someOld == null || someNew == null || someOld.size() != someNew.size()) {
			return false;
		}
		Iterator theOldIt = someOld.iterator();
		Iterator theNewIt = someNew.iterator();
		while (theOldIt.hasNext() && theNewIt.hasNext()) {
			Object theOld = theOldIt.next();
			Object theNew = theNewIt.next();
			if (!Utils.equals(theOld, theNew)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set currVals to new Values by checking the KAs concerned.
	 * 
	 * @param currVals
	 *        the values currently set.
	 * @param newValues
	 *        the values to be set
	 * 
	 * @return true when there actually was a change.
	 */
	protected boolean setNonEmptyValues(Collection currVals, TLObject aMetaAttributed, TLStructuredTypePart attribute,
			Collection newValues) {

		Collection theToAdd = new HashSet(newValues);
		theToAdd.removeAll(currVals);

		Collection theToRem = new HashSet(currVals);
		theToRem.removeAll(newValues);
		if (theToAdd.isEmpty() && theToRem.isEmpty()) {
			return false; // No real change
		}

		// Remove values via KAs so we don't get into conflicts with
		// mandatory or single assignments ;)
		DestinationIterator theKAs = new DestinationIterator(
			AbstractWrapper.resolveLinks(aMetaAttributed, getOutgoingQuery()).iterator());
		while (theKAs.hasNext()) {
			try {
				if (theToRem.contains(WrapperFactory.getWrapper(theKAs.nextKO()))) {
					DBKnowledgeAssociation.clearDestinationAndRemoveLink(theKAs.currentKA());
				}
			} catch (DataObjectException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
		}

		// Add new values
		if (theToAdd != null && !theToAdd.isEmpty()) {
			// Add new values
			Iterator theValues = theToAdd.iterator();
			while (theValues.hasNext()) {
				TLObject theValue = (TLObject) theValues.next();
				LinkStorageUtil.createWrapperAssociation(attribute, aMetaAttributed, theValue, this);
			}
		}
		return true;
	}

	/**
	 * Set Current Values to empty by dropping all KAs.
	 * 
	 * @param aCurrVals
	 *        the values currently set.
	 * 
	 * @return true when there actually was a change.
	 */
	protected boolean setEmptyValues(Collection aCurrVals, TLObject aMetaAttributed) {
		if (aCurrVals == null || aCurrVals.isEmpty()) {
			return false; // no change
		}
		// Remove values via KAs so we don't get into conflicts with
		// mandatory or single assignemts ;)
		Iterator<KnowledgeAssociation> theKAs =
			AbstractWrapper.resolveLinks(aMetaAttributed, getOutgoingQuery()).iterator();
		while (theKAs.hasNext()) {
			try {
				DBKnowledgeAssociation.clearDestinationAndRemoveLink(theKAs.next());
			} catch (DataObjectException dox) {
				Logger.warn("Cannot remove KA fom KB", this);
			}
		}
		return true;
	}

	/**
	 * Set KA SORT_ORDER as defined by oder in aValues.
	 */
	protected void resort(TLObject object, Collection aValues) {
		if (!_ordered || aValues == null || aValues.isEmpty()) {
			return; // nothing to do
		}

		List theValues;
		if (aValues instanceof List) {
			theValues = (List) aValues;
		} else {
			theValues = new ArrayList(aValues);
		}

		Iterator theKAs = AbstractWrapper.resolveLinks(object, getOutgoingQuery()).iterator();
		while (theKAs.hasNext()) {
			KnowledgeAssociation theKA = (KnowledgeAssociation) theKAs.next();
			TLObject theWrapper = WrapperFactory.getWrapper(theKA.getDestinationObject());
			int theIndex = theValues.indexOf(theWrapper);
			if (theIndex == -1) {
				Logger.error("Invalid element in list which is not in the object's value list", this);
			}
			else {
				Integer theOrder = (Integer) theKA.getAttributeValue(orderAttribute());
				if (theOrder == null || theOrder.intValue() != theIndex) {
					theKA.setAttributeValue(orderAttribute(), Integer.valueOf(theIndex));
				}
			}
		}
	}

	/**
	 * Resort after a single new element was added.
	 * 
	 * @param object
	 *        resort
	 */
	protected void resort(TLObject object) throws NoSuchAttributeException {
		if (!_ordered) {
			return; // Nothing to do ;-)
		}

		// Find missing KA and add it at the end
		Iterator theKAs = AbstractWrapper.resolveLinks(object, getOutgoingQuery()).iterator();
		KnowledgeAssociation theMissingKA = null;
		int theMax = -1;
		while (theKAs.hasNext()) {
			KnowledgeAssociation theKA = (KnowledgeAssociation) theKAs.next();
			Integer theOrder = (Integer) theKA.getAttributeValue(orderAttribute());
			if (theOrder == null) {
				if (theMissingKA != null) {
					Logger.error("Duplicate unsorted entry found", this);
				}
				theMissingKA = theKA;
			}
			else if (theOrder.intValue() > theMax) {
				theMax = theOrder.intValue();
			}
		}

		if (theMissingKA == null) {
			Logger.error("Inserted element not found", this);
		}
		else {
			try {
				theMissingKA.setAttributeValue(orderAttribute(), Integer.valueOf(theMax + 1));
			} catch (NoSuchAttributeException e) {
				throw e;
			} catch (DataObjectException e) {
				Logger.error(
					orderAttribute() + " attribute of type " + getConfig().getTable() + " must be of type Integer!",
					SetStorage.class);
			}
		}
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection aValues)
			throws TopLogicException {
		if (object != null) {
			// Check attribute
			AttributeUtil.checkHasAttribute(object, attribute);

			if (aValues != null) {
				// Check for duplicates if not already using a Set
				if (!(aValues instanceof Set)) {
					Set theValueSet = new HashSet(aValues);
					if (theValueSet.size() != aValues.size()) {
						throw new IllegalArgumentException("Duplicates not allowed for " + attribute + ": " + aValues);
					}
				}

				// Check the single values
				Iterator theVals = aValues.iterator();
				while (theVals.hasNext()) {
					Object theValue = theVals.next();
					this.checkBasicValue(object, attribute, theValue);
				}
			}
		}
	}

	/**
	 * Creates a configuration for {@link SetStorage}.
	 * 
	 * @param tableName
	 *        Name of the table to store data. May be <code>null</code>. In this case, the table
	 *        name is derived from the history type.
	 * @param historyType
	 *        The history type of the value of the reference.
	 * @return The {@link SetStorage} configuration.
	 */
	public static Config<?> setConfig(String tableName, HistoryType historyType) {
		return defaultConfig(Config.class, tableName, historyType);
	}

	/**
	 * {@link AbstractSet} holding a {@link Map} from target items to their links and wraps the
	 * {@link Map#keySet() keys} of that map.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private final class LiveTargets extends AbstractSet<TLObject> {

		private final Map<KnowledgeItem, KnowledgeAssociation> _liveLinks;

		private final TLStructuredTypePart _attribute;

		private final TLObject _baseItem;

		LiveTargets(Map<KnowledgeItem, KnowledgeAssociation> liveLinks, TLObject baseItem,
				TLStructuredTypePart attribute) {
			_liveLinks = liveLinks;
			_baseItem = baseItem;
			_attribute = attribute;
		}

		@Override
		public int size() {
			return _liveLinks.size();
		}

		@Override
		public Iterator<TLObject> iterator() {
			return new Iterator<>() {

				private Iterator<Entry<KnowledgeItem, KnowledgeAssociation>> _base = _liveLinks.entrySet().iterator();
				private Entry<KnowledgeItem, KnowledgeAssociation> _last;

				@Override
				public boolean hasNext() {
					return _base.hasNext();
				}

				@Override
				public TLObject next() {
					_last = _base.next();
					KnowledgeItem key = _last.getKey();
					if (key != null) {
						return key.getWrapper();
					}
					return null;
				}

				@Override
				public void remove() {
					// _base.remove() does *not* remove the association itself, but only sets the
					// source attribute to null.
					_base.remove();
					DBKnowledgeAssociation.clearDestinationAndRemoveLink(_last.getValue());
				}

			};
		}

		@Override
		public boolean add(TLObject e) {
			if (contains(e)) {
				return false;
			}
			/* Create a new KnowledgeAssociation. The magic of the live links automatically
			 * "modifies" this set. */
			LinkStorageUtil.createWrapperAssociation(_attribute, _baseItem, e, SetStorage.this);
			return true;
		}

		@Override
		public boolean contains(Object o) {
			return linkForObject(o) != null;
		}

		@Override
		public boolean remove(Object o) {
			KnowledgeAssociation link = linkForObject(o);
			if (link == null) {
				return false;
			}
			/* Delete link. The magic of the live links automatically "modifies" this set. */
			DBKnowledgeAssociation.clearDestinationAndRemoveLink(link);
			return true;

		}

		private KnowledgeAssociation linkForObject(Object o) {
			if (!(o instanceof TLObject)) {
				return null;
			}
			return _liveLinks.get(((TLObject) o).tHandle());
		}
	}

}
