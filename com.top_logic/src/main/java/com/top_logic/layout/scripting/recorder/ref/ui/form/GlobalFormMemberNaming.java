/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.Control;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FuzzyFormMemberNaming;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link ModelNamingScheme} that allows identifying a {@link FormMember} without the need for
 * having a reachable {@link FormHandler}.
 * 
 * <p>
 * Instead, the FormMember is found by iterating all visible models and matching the
 * {@link FormMember} by applying a number of {@link FieldMatcher}s to the member and its containers
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GlobalFormMemberNaming extends AbstractFormMemberNaming<GlobalFormMemberNaming.Name> {

	private static final class VisibleModels implements Iterable<Object> {

		private static final class ListenerIterator implements Iterator<Object> {

			private FrameScope _scope;

			Iterator<? extends CommandListener> _controls;

			Object _next = null;

			boolean _found = false;

			/** 
			 * Creates a {@link ListenerIterator}.
			 */
			public ListenerIterator(FrameScope scope) {
				_scope = scope;
				_controls = _scope.getCommandListener().iterator();
			}

			@Override
			public boolean hasNext() {
				prepareNext();
				return _next != null;
			}

			@Override
			public Object next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				Object result = _next;
				_next = null;
				return result;
			}

			private void prepareNext() {
				if (_next == null) {
					_next = findNext();
				}
			}

			private Object findNext() {
				while (true) {
					Object result = innerFindNext();
					if (result != null) {
						_found = true;
						return result;
					}

					if (!_found) {
						// Search outer scope.
						FrameScope enclosingScope = _scope.getEnclosingScope();
						if (enclosingScope != null) {
							_scope = enclosingScope;
							_controls = _scope.getCommandListener().iterator();
							continue;
						}
					}

					// Finally not found.
					return null;
				}
			}

			private Object innerFindNext() {
				while (_controls.hasNext()) {
					CommandListener listener = _controls.next();
					
					if (listener instanceof Control) {
						Control control = (Control) listener;
						
						if (control.isViewDisabled()) {
							// In background.
							continue;
						}

						if (!control.isVisible()) {
							// Not displayed.
							continue;
						}
						
						Object model = control.getModel();
						if (model == null) {
							// Control without model.
							continue;
						}
						return model;
					}
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		}

		private final LayoutComponentScope _scope;

		public VisibleModels(LayoutComponentScope scope) {
			_scope = scope;
		}

		@Override
		public Iterator<Object> iterator() {
			return new ListenerIterator(_scope);
		}
	}

	/**
	 * {@link ModelName} created by {@link FuzzyFormMemberNaming}.
	 */
	public interface Name extends ModelName {

		/**
		 * The top-level scope to search in.
		 * 
		 * @return Description of the scope to search in, <code>null</code> to search in global
		 *         scope ({@link MainLayout}).
		 */
		ModelName getComponent();

		/**
		 * @see #getComponent()
		 */
		void setComponent(ModelName value);

		/**
		 * Path of matchers that must accept the field and its containers.
		 * 
		 * <p>
		 * The first {@link FieldMatcher} is applied to the displayed field. The second to its
		 * container and so on.
		 * </p>
		 */
		@InstanceFormat
		List<FieldMatcher> getMatchers();

	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Maybe<Name> buildName(FormMember model) {
		LayoutComponent component = getComponent(model);
		if (component == null) {
			component = DefaultDisplayContext.getDisplayContext().getLayoutContext().getMainLayout();
		}

		// Check, whether the model can be found by iterating the visible part of the UI.
		for (Object visibleModel : getModels(component)) {
			if (visibleModel == model) {
				Maybe<? extends ModelName> componentName = ModelResolver.buildModelNameIfAvailable(component);
				if (!componentName.hasValue()) {
					return Maybe.none();
				}
				Name name = createName();
				name.setComponent(componentName.get());

				List<FieldMatcher> path = name.getMatchers();
				fillPath(path, model);

				return Maybe.some(name);
			}
		}

		return Maybe.none();
	}

	private LayoutComponent getComponent(FormMember model) {
		FormHandler handler = FormComponent.formHandlerForMember(model);
		if (handler instanceof LayoutComponent) {
			return (LayoutComponent) handler;
		} else {
			return null;
		}
	}

	private void fillPath(List<FieldMatcher> path, FormMember model) {
		FieldMatcher matcher = createMatcher(model);
		path.add(matcher);
		FormContainer parent = model.getParent();
		if (parent != null) {
			fillPath(path, parent);
		}
		return;
	}

	@Override
	public FormMember locateModel(ActionContext context, Name name) {
		ModelName componentName = name.getComponent();
		LayoutComponent component = null;
		if (componentName != null) {
			component = (LayoutComponent) ModelResolver.locateModel(context, componentName);
		}

		if (component == null) {
			component = context.getMainLayout();
		}

		return findMember(context, component, name);
	}

	private FormMember findMember(ActionContext context, LayoutComponent component, Name name) {
		List<FieldMatcher> matchers = name.getMatchers();
		List<Filter<? super FormMember>> path = new ArrayList<>(matchers.size());
		for (FieldMatcher matcher : matchers) {
			path.add(matcher.createFilter(context));
		}
		FormMember uniqueMember = null;
		for (Object model : getModels(component)) {
			if (!(model instanceof FormMember)) {
				continue;
			}

			FormMember candidate = (FormMember) model;
			if (candidate == uniqueMember) {
				// Already found.
				continue;
			}
			if (matches(path, candidate)) {
				if (uniqueMember != null) {
					throw ApplicationAssertions.fail(name, "Form member not unique: " + uniqueMember
						+ ", " + candidate);
				}

				uniqueMember = candidate;
			}
		}
		if (uniqueMember == null) {
			throw ApplicationAssertions.fail(name, "Form member not found.");
		}
		return uniqueMember;
	}

	private Iterable<Object> getModels(LayoutComponent component) {
		LayoutComponentScope scope = component.getEnclosingFrameScope();
		return new VisibleModels(scope);
	}

	private boolean matches(List<Filter<? super FormMember>> path, FormMember candidate) {
		for (Filter<? super FormMember> matcher : path) {
			if (candidate == null) {
				return false;
			}
			if (!matcher.accept(candidate)) {
				return false;
			}
			candidate = candidate.getParent();
		}
		return true;
	}

}
