/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import java.util.List;
import java.util.Objects;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link DesignSource} of a view stored in an attribute of a model object, an attribute of the
 * datatype {@code tl.view:ViewDefinition}.
 *
 * <p>
 * The tree is built on a copy of the stored configuration, so that editing it leaves the value of
 * the attribute untouched until it is saved. An object without a stored view starts with an empty
 * {@code <view>}, to which the user adds the content.
 * </p>
 *
 * <p>
 * A save stores a configuration read from the saved content, a configuration of its own that the
 * tree does not modify further, in a transaction of its own. Writing the attribute is checked like
 * every write of an attribute in the view layer: the current user needs the right to write it on
 * the object, see {@link Update#checkWritePermission(TLObject, TLStructuredTypePart)}.
 * </p>
 */
public final class AttributeDesignSource implements DesignSource {

	private final TLObject _object;

	private final TLStructuredTypePart _attribute;

	/**
	 * Creates an {@link AttributeDesignSource}.
	 *
	 * @param object
	 *        See {@link #getObject()}.
	 * @param attribute
	 *        See {@link #getAttribute()}.
	 */
	public AttributeDesignSource(TLObject object, TLStructuredTypePart attribute) {
		_object = Objects.requireNonNull(object, "object");
		_attribute = Objects.requireNonNull(attribute, "attribute");
	}

	/**
	 * The object the view is stored on.
	 */
	public TLObject getObject() {
		return _object;
	}

	/**
	 * The attribute of the {@link #getObject() object} holding the view.
	 */
	public TLStructuredTypePart getAttribute() {
		return _attribute;
	}

	@Override
	public String getId() {
		return TLModelUtil.qualifiedName(_attribute) + "@" + IdentifierUtil.toExternalForm(_object.tIdLocal());
	}

	@Override
	public String getLabel() {
		return Resources.getInstance().getString(I18NConstants.DESIGNER_STORED_VIEW__OBJECT_ATTRIBUTE.fill(
			MetaLabelProvider.INSTANCE.getLabel(_object), MetaLabelProvider.INSTANCE.getLabel(_attribute)));
	}

	@Override
	public ViewElement.Config load() throws ConfigurationException {
		Object value = _object.tValue(_attribute);
		if (value == null) {
			return TypedConfiguration.newConfigItem(ViewElement.Config.class);
		}
		if (!(value instanceof ViewElement.Config stored)) {
			throw new ConfigurationException(I18NConstants.ERROR_NOT_A_STORED_VIEW__ATTRIBUTE.fill(getLabel()),
				TLModelUtil.qualifiedName(_attribute), String.valueOf(value));
		}
		return TypedConfiguration.copy(stored);
	}

	@Override
	public void save(String content) {
		Update.checkWritePermission(_object, _attribute);

		ViewElement.Config view;
		try {
			view = ViewLoader.parseConfig(List.of(CharacterContents.newContent(content, getId())));
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SAVE_VIEW_FAILED__PATH.fill(getLabel()), ex);
		}

		Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(I18NConstants.DESIGNER_SAVED_STORED_VIEW__VIEW.fill(getLabel()));
		try {
			_object.tUpdate(_attribute, view);
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	@Override
	public void discard() {
		// Every load copies the stored configuration, nothing is kept.
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof AttributeDesignSource other && _object.equals(other._object)
			&& _attribute.equals(other._attribute);
	}

	@Override
	public int hashCode() {
		return 31 * _object.hashCode() + _attribute.hashCode();
	}

	/**
	 * The {@link #getLabel() label}, as a breadcrumb shows the source.
	 */
	@Override
	public String toString() {
		return getLabel();
	}
}
