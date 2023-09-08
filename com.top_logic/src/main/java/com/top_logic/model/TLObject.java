/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.TLID;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.TableTyped;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.impl.generated.TLObjectBase;

/**
 * Instance of a {@link TLStructuredType}, or {@link TLEnumeration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLObject extends IdentifiedObject, TableTyped, TLObjectBase {

	/**
	 * The object handle for accessing the persistency layer.
	 * 
	 * <p>
	 * Must only be called for objects that are not {@link #tTransient()}.
	 * </p>
	 * 
	 * @throws UnsupportedOperationException
	 *         If called for a {@link #tTransient()} object.
	 * 
	 * @see #tTransient()
	 * @see #tId()
	 */
	KnowledgeItem tHandle();

	/**
	 * The {@link TLType} of this {@link TLObject}
	 */
	default TLStructuredType tType() {
		// No dynamic type by default.
		return null;
	}

	/**
	 * The object holding a reference to this object in one of its composition references.
	 * 
	 * @return The container object managing the lifecycle of this object, or <code>null</code>, if
	 *         there is no such container object.
	 * 
	 * @see TLAssociationEnd#isComposite()
	 * @see #tContainerReference()
	 */
	default TLObject tContainer() {
		return null;
	}

	/**
	 * The the composition reference of the {@link #tContainer()} object containing this object.
	 * 
	 * @return The composition reference of {@link #tContainer()} containing this object, or
	 *         <code>null</code>, if {@link #tContainer()} is <code>null</code>.
	 * 
	 * @see #tContainer()
	 */
	default TLReference tContainerReference() {
		return null;
	}

	/**
	 * The value that is associated with the given {@link TLStructuredTypePart}.
	 * 
	 * @param part
	 *        The type part of this instance to access.
	 * @return The value associated with the given part in this instance.
	 * @throws IllegalArgumentException
	 *         If the given part is no part of {@link #tType()}.
	 */
	default Object tValue(TLStructuredTypePart part) {
		return tGetData(part.getName());
	}

	/**
	 * Resolves all objects pointing to this object through the given reference.
	 *
	 * @param ref
	 *        The reference to navigate backwards.
	 * @return The objects that contain this one in the given reference.
	 */
	default Set<? extends TLObject> tReferers(TLReference ref) {
		return ref.getReferers(this);
	}

	/**
	 * The value that is associated with the {@link TLStructuredTypePart} with the given name.
	 * 
	 * @param partName
	 *        Name of a part of {@link #tType()}.
	 * @return The value associated with the given part in this instance, or <code>null</code>, if
	 *         no such part exists.
	 */
	default Object tValueByName(String partName) {
		TLStructuredType type = tType();

		// Note: During system setup, legacy objects do not yet have a "dynamic" type.
		if (type != null) {
			TLStructuredTypePart part = type.getPart(partName);
			if (part != null) {
				return tValue(part);
			}
		}

		// Legacy support to be able to retrieve undefined attributes such as `tType`.
		return tGetData(partName);
	}

	/**
	 * Updates the value of the given {@link TLStructuredTypePart}.
	 * 
	 * @param part
	 *        The type part of this instance to update.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tValue(TLStructuredTypePart)
	 */
	default void tUpdate(TLStructuredTypePart part, Object value) {
		tSetData(part.getName(), value);
	}

	/**
	 * Updates the value of the {@link TLStructuredTypePart} with the given name.
	 * 
	 * @param partName
	 *        Name of a part of {@link #tType()}.
	 * @param value
	 *        The new value to set.
	 */
	default void tUpdateByName(String partName, Object value) {
		tUpdate(tType().getPartOrFail(partName), value);
	}

	/**
	 * Adds a new value to the given multiple reference.
	 * 
	 * <p>
	 * If the given reference is ordered, the new element is added to the end of the list.
	 * </p>
	 * 
	 * @param partName
	 *        The name of the multiple reference to a add a new element to.
	 * @param value
	 *        The new element to add to the values of the given reference.
	 * 
	 * @see #tAdd(TLStructuredTypePart, Object)
	 */
	default void tAddByName(String partName, Object value) {
		tAdd(tType().getPartOrFail(partName), value);
	}

	/**
	 * Adds a new value to the given multiple reference.
	 * 
	 * <p>
	 * If the given reference is ordered, the new element is added to the end of the list.
	 * </p>
	 * 
	 * @param part
	 *        The multiple reference to a add a new element to.
	 * @param value
	 *        The new element to add to the values of the given reference.
	 * 
	 * @see #tRemove(TLStructuredTypePart, Object)
	 */
	default void tAdd(TLStructuredTypePart part, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Removes an element from the values of the given multiple reference.
	 * 
	 * @param partName
	 *        The name of the multiple reference to a remove an element from.
	 * @param value
	 *        The new element to add to the values of the given reference.
	 * 
	 * @see #tRemove(TLStructuredTypePart, Object)
	 */
	default void tRemoveByName(String partName, Object value) {
		tRemove(tType().getPartOrFail(partName), value);
	}

	/**
	 * Removes an element from the values of the given multiple reference.
	 * 
	 * @param part
	 *        The multiple reference to a remove an element from.
	 * @param value
	 *        The new element to add to the values of the given reference.
	 * 
	 * @see #tAdd(TLStructuredTypePart, Object)
	 */
	default void tRemove(TLStructuredTypePart part, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	default ObjectKey tId() {
		return tHandle().tId();
	}

	/**
	 * Whether this instance is not stored in the persistency layer.
	 * 
	 * @see #tId()
	 * @see #tHandle()
	 */
	default boolean tTransient() {
		return tId() == null;
	}

	/**
	 * Branch-local identifier of this object.
	 * 
	 * <p>
	 * Must only be called for non-{@link #tTransient()} objects.
	 * </p>
	 * 
	 * @throws UnsupportedOperationException
	 *         If called for {@link #tTransient()} objects.
	 */
	default TLID tIdLocal() {
		return tHandle().getObjectName();
	}

	/**
	 * Check if the object is valid.
	 * 
	 * <p>
	 * An object is valid if it is {@link #tTransient()}, or its persistent item
	 * {@link KnowledgeItem#isAlive() is alive}.
	 * </p>
	 * 
	 * @return Whether the object can be legally accessed.
	 */
	default boolean tValid() {
		return tHandle().isAlive();
	}

	/**
	 * Deletes the underlying item in the persistency layer.
	 * 
	 * @see #tDeleteVeto()
	 * @see KBUtils#deleteAll(Iterable) When multiple {@link TLObject} must be deleted, use this
	 *      service method for performance reasons.
	 */
	default void tDelete() {
		if (!tValid()) {
			// Ignore double-deletes.
			return;
		}
		tHandle().delete();
	}

	/**
	 * Check, whether deletion is possible.
	 * 
	 * @return The {@link ResKey} describing, why this object cannot be deleted, or
	 *         {@link Optional#empty()} to signal that deletion is allowed.
	 * 
	 * @see #tDelete()
	 */
	public default Optional<ResKey> tDeleteVeto() {
		return Optional.empty();
	}

	/**
	 * Touches the underlying persistent item to ensure that a new revision is created.
	 */
	default void tTouch() {
		tHandle().touch();
	}

	@Override
	default MOStructure tTable() {
		return tHandle().tTable();
	}

	/**
	 * The persistency layer, {@link #tHandle()} is stored in.
	 */
	default KnowledgeBase tKnowledgeBase() {
		return tHandle().getKnowledgeBase();
	}

	/**
	 * Date of the last modification of the internal storage.
	 * 
	 * @see #tLastModifier()
	 */
	default Date tLastModificationDate() {
		return tGetDataTimestampAsDate(LifecycleAttributes.MODIFIED);
	}

	/**
	 * Time-stamp of the last modification of the internal storage.
	 * 
	 * @see #tLastModificationDate()
	 */
	default long tLastModificationTime() {
		return tGetDataLongValue(LifecycleAttributes.MODIFIED);
	}

	/**
	 * Author of the last modification of the internal storage.
	 * 
	 * @see #tLastModificationDate()
	 */
	default Person tLastModifier() {
		return tGetDataAuthor(LifecycleAttributes.MODIFIER, HistoryUtils.getLastUpdate(tHandle()));
	}

	/**
	 * Date of the initial creation of this object.
	 * 
	 * @see #tCreator()
	 */
	default Date tCreationDate() {
		return tGetDataTimestampAsDate(LifecycleAttributes.CREATED);
	}

	/**
	 * Time-stamp of the initial creation of this object.
	 * 
	 * @see #tCreationDate()
	 */
	default long tCreationTime() {
		return tGetDataLongValue(LifecycleAttributes.CREATED);
	}

	/**
	 * Author of the initial revision of this object.
	 * 
	 * @see #tCreationDate()
	 */
	default Person tCreator() {
		return tGetDataAuthor(LifecycleAttributes.CREATOR, HistoryUtils.getCreateRevision(tHandle()));
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default String tGetDataString(String property) {
		return ((String) tGetData(property));
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default int tGetDataInt(String property) {
		Object value = tGetData(property);
		return value == null ? 0 : ((Number) value).intValue();
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Integer tGetDataInteger(String property) {
		return (Integer) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default boolean tGetDataBooleanValue(String property) {
		Boolean value = tGetDataBoolean(property);

		return ((value != null) ? value.booleanValue() : false);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Boolean tGetDataBoolean(String property) {
		return (Boolean) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Date tGetDataDate(String property) {
		return (Date) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Date tGetDataTimestampAsDate(String property) {
		Long timestamp = tGetDataLong(property);
		return ((timestamp != null) ? new Date(timestamp.longValue()) : null);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Long tGetDataLong(String property) {
		return (Long) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default long tGetDataLongValue(String property) {
		Object value = tGetData(property);
		return value == null ? 0L : ((Number) value).longValue();
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Float tGetDataFloat(String property) {
		return (Float) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Double tGetDataDouble(String property) {
		return (Double) tGetData(property);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default double tGetDataDoubleValue(String property) {
		return tGetDataDoubleValue(property, 0.0);
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param defaultValue
	 *        The value to return, if no value is stored.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default double tGetDataDoubleValue(String property, double defaultValue) {
		Double result = tGetDataDouble(property);
		return result == null ? defaultValue : result.doubleValue();
	}

	/**
	 * Type safe access to the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param revision
	 *        The revision in which to return the result.
	 * @return Value of the storage attribute with the given name.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default Person tGetDataAuthor(String property, Revision revision) {
		{
			String contextId = tGetDataString(property);
			if (!contextId.startsWith(SessionContext.PERSON_ID_PREFIX)) {
				return null;
			}
			TLID personId =
				IdentifierUtil.fromExternalForm(contextId.substring(SessionContext.PERSON_ID_PREFIX.length()));
			return PersonManager.getManager().getPersonByIdentifier(
				personId, tKnowledgeBase(), revision.getCommitNumber());
		}
	}

	/**
	 * Access to untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @return The currently assigned value to the property with the given name.
	 */
	default Object tGetData(String property) {
		return tHandle().getAttributeValue(property);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataDate(String property, Date value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataLong(String property, long value) {
		tSetData(property, Long.valueOf(value));
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataLong(String property, Long value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataFloat(String property, Float value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataDouble(String property, Double value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataInteger(String property, int value) {
		tSetDataInteger(property, Integer.valueOf(value));
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataInteger(String property, Integer value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataBoolean(String property, boolean value) {
		tSetDataBoolean(property, value ? Boolean.TRUE : Boolean.FALSE);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataBoolean(String property, Boolean value) {
		tSetData(property, value);
	}

	/**
	 * Type safe update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * 
	 * @see #tGetData(String)
	 * @see #tSetData(String, Object)
	 */
	default void tSetDataString(String property, String value) {
		tSetData(property, value);
	}

	/**
	 * Access to a directly referenced object.
	 * 
	 * @param dynamicType
	 *        The expected return type.
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @return The referenced object, or <code>null</code>, if the reference is empty.
	 */
	default <T> T tGetDataReference(Class<T> dynamicType, String attributeName) {
		KnowledgeItem value = (KnowledgeItem) tGetData(attributeName);
		if (value == null) {
			return null;
		}
		return CollectionUtil.dynamicCast(dynamicType, value.getWrapper());
	}

	/**
	 * Sets a reference attribute.
	 * 
	 * @param attributeName
	 *        The name of the reference attribute.
	 * @param value
	 *        The referenced object, or <code>null</code> to clear the reference.
	 */
	default void tSetDataReference(String attributeName, TLObject value) {
		tSetData(attributeName, value == null ? null : value.tHandle());
	}

	/**
	 * Update of the untyped storage.
	 * 
	 * @param property
	 *        Name of the storage attribute to access.
	 * @param value
	 *        The new value to set.
	 * @return The old value that was overridden.
	 */
	default Object tSetData(String property, Object value) {
		return tHandle().setAttributeValue(property, value);
	}

	/**
	 * The {@link Revision} this object lives in.
	 */
	default Revision tRevision() {
		return HistoryUtils.getHistoryManager(tHandle()).getRevision(tHistoryContext());
	}

	/**
	 * Short-cut for {@link #tId()}.{@link ObjectKey#getHistoryContext() getHistoryContext()}.
	 */
	default long tHistoryContext() {
		return tId().getHistoryContext();
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @deprecated Use {@link #tHandle()} or one of the other short-cuts for accessing underlying
	 *             data, e.g. {@link #tGetData(String)}.
	 */
	@Deprecated
	default KnowledgeObject getWrappedObject() {
		return (KnowledgeObject) tHandle();
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @deprecated Use {@link #tIdLocal()} instead
	 */
	@Deprecated
	default TLID getWrappedObjectName() {
		return tIdLocal();
	}

	/**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @deprecated Use {@link #tValid()} instead.
	 */
	@Deprecated
	default boolean isValid() {
		return tValid();
	}
}
