/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FormObjectOverlay} representing a {@link TLObject} being currently edited.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class ObjectEditing extends FormObjectOverlay {

	private final TLObject _base;

	/**
	 * Creates a {@link ObjectEditing}.
	 * 
	 * @param scope
	 *        The {@link AttributeUpdateContainer} with all current edit operations.
	 * @param base
	 *        See {@link #getEditedObject()}.
	 */
	public ObjectEditing(AttributeUpdateContainer scope, TLObject base) {
		super(scope, base.tType(), scope.newObjectID());
		_base = base;
	}

	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public KnowledgeItem tHandle() {
		return _base.tHandle();
	}

	@Override
	public ObjectKey tId() {
		return _base.tId();
	}

	@Override
	public TLObject getEditedObject() {
		return _base;
	}

	@Override
	public String getDomain() {
		return null;
	}

	@Override
	protected Object defaultValue(TLStructuredTypePart part) {
		return _base.tValue(part);
	}

	@Override
	public TLObject tContainer() {
		TLObject baseContainer = _base.tContainer();
		if (baseContainer == null) {
			return null;
		}
		TLFormObject containerOverlay = getScope().getOverlay(baseContainer, null);
		if (containerOverlay == null) {
			return baseContainer;
		}
		return containerOverlay;
	}

}
