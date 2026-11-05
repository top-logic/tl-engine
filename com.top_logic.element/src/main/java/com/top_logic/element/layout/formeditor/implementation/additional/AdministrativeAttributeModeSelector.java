/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation.additional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.TLContext;

/**
 * {@link ModeSelector} for an attribute of an {@link Person account} that only an administrator
 * changes, and none on his own account.
 *
 * <p>
 * While the account is edited, the attribute is {@link FormVisibility#DISABLED disabled} unless
 * the current user is an administrator editing another account (see
 * {@link #isChangeable(TLObject)}). An administrator therefore cannot revoke his own administrator
 * flag and lock himself out of the administration.
 * </p>
 *
 * @see TLDynamicVisibility
 */
@Label("Changeable by other administrators only")
public class AdministrativeAttributeModeSelector implements ModeSelector {

	/**
	 * Configuration of {@link AdministrativeAttributeModeSelector}.
	 */
	public interface Config<I extends AdministrativeAttributeModeSelector> extends PolymorphicConfiguration<I> {

		@Override
		@ClassDefault(AdministrativeAttributeModeSelector.class)
		Class<? extends I> getImplementationClass();
	}

	/**
	 * Creates a {@link AdministrativeAttributeModeSelector} from configuration.
	 */
	@CalledByReflection
	public AdministrativeAttributeModeSelector(InstantiationContext context, Config<?> config) {
		// No configuration options.
	}

	@Override
	public FormVisibility getMode(TLObject object, TLStructuredTypePart attribute, boolean editMode) {
		if (!editMode || isChangeable(object)) {
			return FormVisibility.DEFAULT;
		}
		return FormVisibility.DISABLED;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, boolean editMode,
			Sink<Pointer> trace, OverlayLookup overlays) {
		// Depends on the current user only, not on values of the edited object.
	}

	/**
	 * Whether the current user may change an administrative attribute of the given account: he is
	 * an administrator, and the account is not his own.
	 *
	 * @param account
	 *        The edited account, or a form object editing it.
	 */
	public static boolean isChangeable(TLObject account) {
		if (!ThreadContext.isAdmin()) {
			return false;
		}
		return editedObject(account) != TLContext.currentUser();
	}

	private static TLObject editedObject(TLObject object) {
		TLObject result = object;
		while (result instanceof TLFormObjectBase formObject && formObject.getEditedObject() != null) {
			result = formObject.getEditedObject();
		}
		return result;
	}

}
