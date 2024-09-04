/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.overlay;

import java.util.function.Consumer;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.UpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Transient {@link TLObject} being edited or created in a form.
 * 
 * @see AttributeUpdateContainer#createObject(com.top_logic.model.TLStructuredType, String)
 * @see AttributeUpdateContainer#editObject(TLObject)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLFormObject extends TLFormObjectBase, UpdateFactory {

	/**
	 * Whether this is a create overlay for a new object.
	 */
	boolean isCreate();

	/**
	 * The type of the overlay object.
	 */
	TLStructuredType getType();

	/**
	 * The underlying base object, if this is an edit overlay.
	 * 
	 * @see #isCreate()
	 */
	TLObject getEditedObject();

	/**
	 * The name of the created object, if this is a {@link #isCreate() create overlay}.
	 * 
	 * <p>
	 * Legal values are <code>null</code> for the default/initial object creation, or any other
	 * string except the string value <code>"null"</code>.
	 * </p>
	 * 
	 * @see #isCreate()
	 */
	String getDomain();

	/**
	 * The {@link AttributeUpdateContainer} with all current edit operations.
	 */
	AttributeUpdateContainer getScope();

	/**
	 * The {@link AttributeUpdate} that represents the value of the given
	 * {@link TLStructuredTypePart} in this {@link TLObject}.
	 */
	AttributeUpdate getUpdate(TLStructuredTypePart part);

	/**
	 * All {@link AttributeUpdate}s for this {@link TLObject} overlay.
	 */
	Iterable<AttributeUpdate> getUpdates();

	/**
	 * Removes the {@link AttributeUpdate} for the given {@link TLStructuredTypePart}.
	 */
	AttributeUpdate removeUpdate(TLStructuredTypePart attribute);

	/**
	 * The {@link FormContainer} that was
	 * {@link AttributeFormContext#createFormContainerForOverlay(TLFormObject) created} for this
	 * overlay object.
	 *
	 * @see AttributeFormContext#createFormContainerForOverlay(TLFormObject)
	 */
	FormContainer getFormContainer();

	/**
	 * @see #getFormContainer()
	 */
	@FrameworkInternal
	void initContainer(FormContainer formContainer);

	/**
	 * Where the for is written to.
	 */
	default Media getOutputMedia() {
		return Media.BROWSER;
	}

	/**
	 * Registers a callback that is executed exactly once after an {@link AttributeUpdate} for the
	 * given attribute becomes available.
	 */
	void withUpdate(TLStructuredTypePart attribute, Consumer<AttributeUpdate> callback);

	/**
	 * ID used to identify form groups displaying information for this object.
	 */
	String getFormId();

}
