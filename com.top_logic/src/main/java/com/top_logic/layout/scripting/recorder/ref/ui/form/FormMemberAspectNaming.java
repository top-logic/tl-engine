/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * An {@link UnrecordableNamingScheme} about an aspect of a {@link FormMember}, like its label or
 * tooltip.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class FormMemberAspectNaming<M, N extends FormMemberAspectNaming.FormMemberAspectName>
		extends UnrecordableNamingScheme<M, N> {

	/** A {@link ModelName} about an aspect of a {@link FormMember}, like its label or tooltip. */
	@Abstract
	public interface FormMemberAspectName extends ModelName {

		/** The {@link FormMember} this is about. */
		ModelName getFormMember();

		/** @see #getFormMember() */
		void setFormMember(ModelName formMember);

	}

	/** Creates a {@link FormMemberAspectNaming}. */
	public FormMemberAspectNaming(Class<M> modelClass, Class<N> nameClass) {
		super(modelClass, nameClass);
	}

	@Override
	public final M locateModel(ActionContext context, N name) {
		FormMember formMember = (FormMember) context.resolve(name.getFormMember());
		return locateFormMemberAspect(context, name, formMember);
	}

	/**
	 * Locate the aspect of the given {@link FormMember}.
	 * 
	 * @param name
	 *        The name of the given form member.
	 */
	protected abstract M locateFormMemberAspect(ActionContext context, N name, FormMember formMember);

}
