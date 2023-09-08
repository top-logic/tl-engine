/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.kbbased.AttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * An {@link AbstractStorage} that is based on {@link AssociationQuery}s.
 * <p>
 * The concrete subclass may use more than one query.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AssociationQueryBasedStorage<C extends AbstractStorage.Config<?>> extends AbstractStorage<C> {

	/** The column for the owner of the values. */
	public static final String OBJECT_ATTRIBUTE_NAME = "object";

	/** The column for the attribute for which the values are stored. */
	public static final String META_ATTRIBUTE_ATTRIBUTE_NAME = "metaAttribute";

	private KnowledgeBase _knowledgeBase;

	/** {@link TypedConfiguration} constructor for {@link AssociationQueryBasedStorage}. */
	public AssociationQueryBasedStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);
		_knowledgeBase = attribute.tKnowledgeBase();
	}

	/**
	 * The query fetches those lines from the given table, where the attribute is the given one.
	 * <p>
	 * The attribute for which the value is stored, has to be stored in an {@link MOReference} with
	 * the name {@value #META_ATTRIBUTE_ATTRIBUTE_NAME}. And the owner of the value in an
	 * {@link MOReference} named {@value #OBJECT_ATTRIBUTE_NAME}.
	 * </p>
	 * 
	 * @param tableName
	 *        Name of the {@link MetaObject#getName() table} where the required objects are stored.
	 * @param attribute
	 *        Is not allowed to be null.
	 *        @param expectedType Concrete implementation class of the result objects.
	 * @return Never null.
	 */
	protected <T extends TLObject> AssociationSetQuery<T> createQuery(String tableName, TLStructuredTypePart attribute,
			Class<T> expectedType) {
		Map<String, Object> filterAttributes = singletonMap(META_ATTRIBUTE_ATTRIBUTE_NAME, attribute.tHandle());
		return AssociationQuery.createQuery(
			getQueryName(attribute), expectedType, tableName, OBJECT_ATTRIBUTE_NAME, filterAttributes);
	}

	private String getQueryName(TLStructuredTypePart attribute) {
		return getClass().getSimpleName() + "_" + qualifiedName(attribute);
	}

	/**
	 * Executes the query on the given base object.
	 * 
	 * @param owner
	 *        From which {@link TLObject} the query should start navigating. Is not allowed to be
	 *        null.
	 * @return An unmodifiable view. Never null.
	 */
	protected <T extends TLObject> Set<T> resolveQuery(TLObject owner, AssociationSetQuery<T> query) {
		return getKnowledgeBase().resolveLinks((KnowledgeObject) owner.tHandle(), query);
	}

	/**
	 * Creates a {@link KnowledgeItem} instance in the given table.
	 * 
	 * @param tableName
	 *        The name of the {@link MOClass} representing the table.
	 */
	protected KnowledgeItem createItem(String tableName) {
		return getKnowledgeBase().createKnowledgeObject(tableName);
	}

	/**
	 * The {@link KnowledgeBase} used by this storage.
	 * <p>
	 * This is the {@link KnowledgeBase} which contains the {@link TLStructuredTypePart attribute}
	 * for which this storage is responsible.
	 * </p>
	 */
	public KnowledgeBase getKnowledgeBase() {
		return _knowledgeBase;
	}

	/**
	 * Check if the given value may be used to update this attribute
	 * 
	 * @param context
	 *        The object to access.
	 * @param attribute
	 *        The attribute to access.
	 * @param value
	 *        the simple value of the given <code>update</code>
	 * @param container
	 *        The intended updates.
	 * @throws TopLogicException
	 *         If the value is not compatible with the given attribute.
	 */
	protected void checkSetValue(Wrapper context, TLStructuredTypePart attribute, Object value,
			AttributeUpdateContainer container) {
		if (context != null) {
			AttributeUtil.checkHasAttribute(context, attribute);
		}
		if (value == null) {
			return;
		}
		if (!getApplicationValueType().isInstance(value)) {
			throw new TopLogicException(I18NConstants.NOT_APPLICATION_VALUE_TYPE___EXPECTED_ACTUAL
				.fill(getApplicationValueType(), value.getClass()));
		}
	}

	/** The {@link Class} of the application objects created by this storage. */
	protected abstract Class<?> getApplicationValueType();

}
