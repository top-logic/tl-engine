/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link AbstractFormMemberNaming} navigating to the matched {@link FormMember} starting from a
 * globally identifyable {@link FormHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFormMemberNaming extends AbstractFormMemberNaming<DefaultFormMemberNaming.Name> {

	/**
	 * Description of a {@link FormMember} interpreted by {@link DefaultFormMemberNaming}.
	 */
	public interface Name extends ModelName {
		/**
		 * Description of the {@link FormHandler} searched for the {@link FormMember}.
		 */
		ModelName getForm();

		/** @see #getForm() */
		void setForm(ModelName formHandlerName);

		/**
		 * Path of {@link FieldMatcher}s navigating to the identified {@link FormMember}.
		 * 
		 * <p>
		 * The first {@link FieldMatcher} in the path is applied to all members of the form's
		 * {@link FormContext}. Subsequent matchers are applied to {@link FormMember}s found in
		 * previous steps.
		 * </p>
		 */
		@InstanceFormat
		List<FieldMatcher> getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(List<FieldMatcher> value);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	static class Result {
		private final Name _name;

		private int _bestIndex;

		private FormMember _bestMatch;

		private int _ambiguousIndex;

		private FormMember _ambiguousMatch;

		private List<FormMember> _candidates = new ArrayList<>();

		public Result(Name name, FormContainer container) {
			_name = name;
			_bestIndex = -1;
			_ambiguousIndex = -2;
			_bestMatch = container;
		}

		public void offerOption(int index, FormMember member) {
			if (index > _bestIndex) {
				_candidates.add(member);
			}
		}

		public void offer(int index, FormMember member) {
			if (index < _bestIndex) {
				return;
			}
			if (index > _bestIndex) {
				_bestIndex = index;
				_bestMatch = member;
				_ambiguousMatch = null;
				_candidates.clear();
			} else {
				_ambiguousIndex = index;
				_ambiguousMatch = member;
			}
		}

		public FormMember getResult(int index) {
			if (_bestIndex == index) {
				// A match was found.
				if (index > _ambiguousIndex) {
					return _bestMatch;
				} else {
					throw ApplicationAssertions.fail(_name, "Ambiguous form member found: " + _bestMatch
						+ " vs. " + _ambiguousMatch);
				}
			} else {
				StringBuilder msg = new StringBuilder();
				msg.append("Form member not found: " + _name.getPath().get(_bestIndex + 1));
				if (_bestIndex != -1) {
					msg.append(" Best match was '");
					msg.append(_bestMatch);
					msg.append("' for step ");
					msg.append(_bestIndex);
					msg.append(" (");
					msg.append(_name.getPath().get(_bestIndex));
					msg.append(").");
				} else {
					msg.append(" No best match found.");
				}
				msg.append(" Candidates were '");
				msg.append(_candidates);
				msg.append("'.");
				throw ApplicationAssertions.fail(_name, msg.toString());
			}
		}
	}

	@Override
	public FormMember locateModel(ActionContext context, Name name) {
		FormHandler form = (FormHandler) ModelResolver.locateModel(context, name.getForm());
		FormContainer container = form.getFormContext();

		Result result = new Result(name, container);
		locateMember(context, name, container, result, 0);
		return result.getResult(name.getPath().size() - 1);
	}

	private void locateMember(ActionContext context, Name name, FormMember container, Result result, int index) {
		List<FieldMatcher> path = name.getPath();
		if (index == path.size()) {
			return;
		}

		if (container instanceof FormContainer) {
			Filter<? super FormMember> filter = path.get(index).createFilter(context);
			for (Iterator<? extends FormMember> it = ((FormContainer) container).getMembers(); it.hasNext();) {
				FormMember member = it.next();
				if (filter.accept(member)) {
					result.offer(index, member);
					locateMember(context, name, member, result, index + 1);
				} else {
					result.offerOption(index, member);
				}
			}
		}
	}

	@Override
	public Maybe<Name> buildName(FormMember model) {
		FormHandler handler = handler(model);
		if (handler == null) {
			return Maybe.none();
		}

		Name name = createName();
		name.setForm(ModelResolver.buildModelName(handler));
		name.setPath(path(model));
		return Maybe.some(name);
	}

	private List<FieldMatcher> path(FormMember model) {
		ArrayList<FieldMatcher> result = new ArrayList<>();

		FormMember current = model;
		FormContainer parent;
		while ((parent = current.getParent()) != null) {
			result.add(createMatcher(current));
			current = parent;
		}

		Collections.reverse(result);

		return result;
	}

	private FormHandler handler(FormMember model) {
		FormContext formContext = model.getFormContext();
		if (formContext == null) {
			return null;
		}
		return formContext.getOwningModel();
	}

}
