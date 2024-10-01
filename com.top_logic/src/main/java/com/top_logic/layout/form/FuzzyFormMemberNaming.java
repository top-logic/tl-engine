/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Iterator;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.util.Resources;

/**
 * {@link ModelNamingScheme} of {@link FormMember}s using the tab path and their plain UI label as
 * identifier.
 * 
 * @see FuzzyFormMemberNaming.Name
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FuzzyFormMemberNaming extends GlobalModelNamingScheme<FormMember, FuzzyFormMemberNaming.Name> {

	/**
	 * {@link ModelName} created by {@link FuzzyFormMemberNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * {@link ModelName} of the owner of the {@link FormMember}, or one of its parents.
		 */
		ModelName getComponent();

		/**
		 * @see #getComponent()
		 */
		void setComponent(ModelName value);

		/**
		 * The UI label of the identified {@link FormMember}.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		void setLabel(String value);

	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<FormMember> getModelClass() {
		return FormMember.class;
	}

	@Override
	public Maybe<Name> buildName(FormMember model) {
		if (model.hasStableIdSpecialCaseMarker()) {
			return Maybe.none();
		}
		if (!model.hasLabel()) {
			return Maybe.none();
		}

		String label = Resources.getInstance().getString(model.getLabel());
		if (StringServices.isEmpty(label)) {
			return Maybe.none();
		}
		FormHandler handler = getFormContextOwner(model);
		if (handler == null) {
			return Maybe.none();
		}

		SearchResult<FormMember> result = new SearchResult<>();
		locate(handler, label, result);
		if (result.getResults().size() != 1) {
			return Maybe.none();
		}

		Maybe<? extends ModelName> handlerName = ModelResolver.buildModelNameIfAvailable(handler);
		if (!handlerName.hasValue()) {
			return Maybe.none();
		}

		Name name = createName();
		name.setComponent(handlerName.get());
		name.setLabel(label);
		return Maybe.some(name);
	}

	private FormHandler getFormContextOwner(FormMember model) {
		FormContext formContext = model.getFormContext();
		return formContext == null ? null : formContext.getOwningModel();
	}

	@Override
	public FormMember locateModel(ActionContext context, Name name) {
		Object owner = ModelResolver.locateModel(context, name.getComponent());
		return locateElseFail(name, owner);
	}

	private FormMember locateElseFail(Name name, Object owner) {
		SearchResult<FormMember> result = new SearchResult<>();
		locate(owner, name.getLabel(), result);
		return result.getSingleResult(getFailedMessagePrefix(name) + " in '" + owner + "'.");
	}

	/**
	 * Prefix of the failure message when given {@link Name} can not be resolved.
	 */
	public static String getFailedMessagePrefix(Name name) {
		return "Failed to find FormMember '" + name.getLabel() + "'";
	}

	private void locate(Object owner, String label, SearchResult<FormMember> result) {
		if (owner instanceof LayoutComponent) {
			locateInComponentContext((LayoutComponent) owner, label, result);
		} else {
			locateInFormHandler((FormHandler) owner, label, result);
		}
	}

	private void locateInComponentContext(LayoutComponent component, String label, SearchResult<FormMember> result) {
		if (component instanceof FormHandler) {
			locateInFormHandler((FormHandler) component, label, result);
		}

		if (component instanceof LayoutContainer) {
			locateInChildren((LayoutContainer) component, label, result);
		}
	}

	private void locateInChildren(LayoutContainer parent, String label, SearchResult<FormMember> result) {
		for (LayoutComponent child : parent.getChildList()) {
			locateInComponentContext(child, label, result);
		}
	}

	private void locateInFormHandler(FormHandler owner, String label, SearchResult<FormMember> result) {
		FormContainer formContext = owner.getFormContext();
		locateInFormContainer(formContext, label, result);
	}

	private void locateInFormContainer(FormContainer container, String label, SearchResult<FormMember> result) {
		Resources resources = Resources.getInstance();
		for (Iterator<? extends FormMember> it = container.getMembers(); it.hasNext();) {
			FormMember member = it.next();
			String candidateLabel = getCandidateLabel(resources, member);
			if (candidateLabel == null) {
				continue;
			}
			result.addCandidate(candidateLabel);

			if (label.equals(candidateLabel)) {
				result.add(member);
			}

			if (member instanceof FormContainer) {
				locateInFormContainer((FormContainer) member, label, result);
			}
		}
	}

	private String getCandidateLabel(Resources resources, FormMember member) {
		return resources.getStringOptional(member.getLabel());
	}

}
