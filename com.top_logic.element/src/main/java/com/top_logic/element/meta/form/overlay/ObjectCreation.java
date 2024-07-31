/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link FormObjectOverlay} that represents a new object currently being created.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class ObjectCreation extends FormObjectOverlay {

	private TLObject _container;

	private final String _domain;

	private final ObjectConstructor _constructor;

	private TLObject _created;

	/**
	 * Creates a {@link ObjectCreation}.
	 * 
	 * @param scope
	 *        The {@link AttributeUpdateContainer} with all current edit operations.
	 * @param type
	 *        The type of the transient object being created in the given form.
	 * @param domain
	 *        The name of the created object, see {@link AttributeUpdate#getDomain()}.
	 * @param constructor
	 *        A custom constructor function.
	 */
	public ObjectCreation(AttributeUpdateContainer scope, TLStructuredType type, String domain,
			ObjectConstructor constructor) {
		super(scope, type, domain == null ? scope.newCreateID() : domain);

		// Note: Create domains are encoded into GUI IDs. While null is a legal value encoded as
		// "null", the string "null" as domain would result in an ambiguity.
		if ("null".equals(domain)) {
			throw new IllegalArgumentException(
				"The string 'null' is not a valid create domain name, you may use the null literal instead.");
		}

		_domain = domain;
		_constructor = constructor;
	}

	@Override
	public boolean isCreate() {
		return true;
	}

	@Override
	public TLObject getEditedObject() {
		return _created != null && _created.tValid() ? _created : null;
	}

	@Override
	public String getDomain() {
		return _domain;
	}

	@Override
	public MOStructure tTable() {
		return TLModelUtil.getTable(tType());
	}

	@Override
	public KnowledgeBase tKnowledgeBase() {
		return tType().tHandle().getKnowledgeBase();
	}

	@Override
	public boolean tValid() {
		return true;
	}

	@Override
	public long tHistoryContext() {
		return Revision.CURRENT_REV;
	}

	@Override
	public void tTouch() {
		// Ignore.
	}

	@Override
	protected Object defaultValue(TLStructuredTypePart part) {
		DefaultProvider defaultProvider = DisplayAnnotations.getDefaultProvider(part);
		if (defaultProvider != null) {
			return defaultProvider.createDefault(_container, part, true);
		}
		return null;
	}

	@Override
	public TLObject tContainer() {
		return _container;
	}

	/**
	 * Initializes the container owning this object being created.
	 */
	public void initContainer(TLObject owner) {
		assert _container == null || _container == owner;
		_container = owner;
	}

	/**
	 * Performs the create operation.
	 */
	public void create() {
		if (_created == null || !_created.tValid()) {
			_created = _constructor.newInstance(this);
		}
	}

}
