/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.ReferenceStorage;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.composite.CompositeStorage;
import com.top_logic.model.composite.ContainerStorage;
import com.top_logic.model.composite.MonomorphicContainerColumn;
import com.top_logic.model.composite.PolymorphicContainerColumn;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CollectionStorage} for references which is stored in the target.
 * 
 * <p>
 * If an element A refers to B1, B2, then this relation is saved by storing an inline reference to A
 * in the table of B1 and B2.
 * </p>
 * 
 * <p>
 * Especially, is it not possible to have A1, A2 both pointing to the same B with the same
 * attribute.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class InlineCollectionStorage<C extends InlineCollectionStorage.Config<?>> extends CollectionStorage<C>
		implements ReferenceStorage, CompositeStorage {

	/**
	 * Configuration options to store the referrer of an object inline in the table of the target.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		StoreInTargetConfig.CONTAINER_COLUMN,
		StoreInTargetConfig.REFERENCE_COLUMN,
	})
	public interface StoreInTargetConfig extends ConfigurationItem {

		/**
		 * Configuration name of {@link #getContainerColumn()}.
		 */
		String CONTAINER_COLUMN = "container-column";

		/**
		 * Configuration name of {@link #getReferenceColumn()}.
		 */
		String REFERENCE_COLUMN = "reference-column";

		/**
		 * Name of the column in the target table which holds the referencing object.
		 */
		@Name(CONTAINER_COLUMN)
		@Mandatory
		String getContainerColumn();

		/**
		 * Setter for {@link #getContainerColumn()}.
		 */
		void setContainerColumn(String value);

		/**
		 * Name of the column in the target table that holds the {@link TLReference} defining the
		 * association between referrer and referenced object.
		 * 
		 * <p>
		 * May be <code>null</code>. In this case the container column must not be used by multiple
		 * references to store their values.
		 * </p>
		 */
		@Name(REFERENCE_COLUMN)
		@Nullable
		String getReferenceColumn();

		/**
		 * Setter for{@link #getReferenceColumn()}.
		 */
		void setReferenceColumn(String value);
	}

	/**
	 * Configuration options for {@link InlineCollectionStorage}.
	 */
	public interface Config<I extends InlineCollectionStorage<?>>
			extends CollectionStorage.Config<I>, StoreInTargetConfig {

		// no additional options here.

	}

	/**
	 * Creates a {@link InlineCollectionStorage}.
	 */
	public InlineCollectionStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Set<? extends TLObject> getReferers(TLObject self, TLReference reference) {
		return CollectionUtil.singletonOrEmptySet(resolveContainer(getConfig(), getTable(), self, reference));
	}

	/**
	 * Name of the table in which this storage stores the values.
	 */
	protected abstract String getTable();

	/**
	 * Determines the container of the given item via the given reference.
	 *
	 * @param config
	 *        Configuration of the storage strategy.
	 * @param tableName
	 *        Expected super class of the table in which the given item is stored.
	 * @param item
	 *        Element to get container for.
	 * @param reference
	 *        Composite reference in which the given item is stored.
	 * @return The container which points to the given item via the given reference, or
	 *         <code>null</code> if there is no such container.
	 */
	public static TLObject resolveContainer(StoreInTargetConfig config, String tableName,
			TLObject item, TLReference reference) {
		if (!TLModelUtil.isCompatibleInstance(reference.getType(), item)) {
			/* Given item has incorrect type. */
			return null;
		}

		MOStructure table = item.tTable();
		if (!table.isSubtypeOf(tableName)) {
			return null;
		}
		MOAttribute containerColumn = table.getAttribute(config.getContainerColumn());
		if (!(containerColumn instanceof MOReference)) {
			throw new IllegalStateException("Expected '" + config.getContainerColumn() + "' to be a "
					+ MOReference.class.getName() + " holding the container of " + item + ", but got "
					+ containerColumn + ".");
		}
		TLObject container = item.tGetDataReference(TLObject.class, containerColumn.getName());
		if (container == null) {
			return null;
		}
		if (config.getReferenceColumn() == null) {
			return container;
		}
		MOAttribute referenceColumn = table.getAttribute(config.getReferenceColumn());
		if (!(referenceColumn instanceof MOReference)) {
			throw new IllegalStateException("Expected '" + config.getReferenceColumn() + "' to be a "
					+ MOReference.class.getName() + " holding the container reference of " + item + ", but got "
					+ referenceColumn + ".");
		}
		if (reference.getDefinition().tId()
			.equals(item.tHandle().getReferencedKey((MOReference) referenceColumn))) {
			return container;
		} else {
			/* self is part of a different attribute. */
			return null;
		}
	}

	@Override
	protected void checkSetValues(TLObject object, TLStructuredTypePart attribute, Collection values)
			throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);

		if (values != null) {
			// Check the single values
			values.forEach(value -> TLItemStorage.checkCorrectType(attribute, value, null));
		}
	}

	@Override
	protected boolean supportsLiveCollectionsInternal() {
		return true;
	}

	@Override
	public abstract Collection<TLObject> getLiveCollection(TLObject object, TLStructuredTypePart attribute);

	@Override
	public void addAttributeValue(TLObject object, TLStructuredTypePart attribute, Object newValue)
			throws NoSuchAttributeException, IllegalArgumentException, AttributeException {
		this.checkAddValue(object, attribute, newValue);

		getLiveCollection(object, attribute).add((TLObject) newValue);
	}

	@Override
	protected void checkAddValue(TLObject object, TLStructuredTypePart attribute, Object collectionAddUpdate)
			throws TopLogicException {
		AttributeUtil.checkHasAttribute(object, attribute);
		TLItemStorage.checkCorrectType(attribute, collectionAddUpdate, null);
	}

	@Override
	public void removeAttributeValue(TLObject object, TLStructuredTypePart attribute, Object aValue)
			throws NoSuchAttributeException, AttributeException {
		this.checkRemoveValue(object, attribute, aValue);

		Collection<?> links = getLiveCollection(object, attribute);
		if (!links.contains(aValue)) {
			/* value is not contained. */
			return;
		}
		checkMandatoryRemove(attribute, links.size());
		links.remove(aValue);
	}

	@Override
	protected void checkRemoveValue(TLObject object, TLStructuredTypePart attribute, Object collectionRemoveUpdate)
			throws TopLogicException {
		// Check attribute
		AttributeUtil.checkHasAttribute(object, attribute);
	}

	@Override
	public ContainerStorage getContainerStorage(TLReference reference) {
		String containerColumn = getConfig().getContainerColumn();
		String referenceColumn = getConfig().getReferenceColumn();
		if (referenceColumn == null) {
			return new MonomorphicContainerColumn(containerColumn, reference);
		} else {
			return new PolymorphicContainerColumn(containerColumn, referenceColumn);
		}
	}

}
