/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import java.util.Objects;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * Context information passed to the template generation of {@link FormElementTemplateProvider}.
 * 
 * @see FormElementTemplateProvider#createTemplate(FormEditorContext)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormEditorContext {

	private TLStructuredType _formType;

	private TLObject _model;

	private FormContext _formContext;

	private FormContainer _contentGroup;

	private FrameScope _frameScope;

	private FormEditorMapping _formEditorMapping;

	private String _formEditorControl;

	private boolean _attributeHidden;

	private boolean _isInEditMode;

	private FormMode _formMode;

	private boolean _isLocked;

	private String _domain;

	private TLStructuredType _concreteType;

	private LabelPlacement _labelPlacement;

	/**
	 * The mode how the in-app form is displayed.
	 */
	public FormMode getFormMode() {
		return _formMode;
	}

	/**
	 * The type the evaluated {@link FormDefinition} was designed for.
	 */
	public TLStructuredType getFormType() {
		return _formType;
	}

	/**
	 * The context model.
	 */
	public TLObject getModel() {
		return _model;
	}

	/**
	 * The {@link FormContext} for the {@link FormMember}s.
	 */
	public FormContext getFormContext() {
		return _formContext;
	}

	/**
	 * The group in the from to add new fields to.
	 */
	public FormContainer getContentGroup() {
		return _contentGroup;
	}

	/**
	 * The current {@link FrameScope}.
	 * 
	 * <p>
	 * May be used for ID generation, see {@link FrameScope#createNewID()}.
	 * </p>
	 */
	public FrameScope getFrameScope() {
		return _frameScope;
	}

	/**
	 * The mapping of the IDs and the {@link ConfigurationItem}s to identify elements of the GUI at
	 * the server side and vice versa.
	 */
	public FormEditorMapping getFormEditorMapping() {
		return _formEditorMapping;
	}

	/**
	 * The ID of the form editor.
	 */
	public String getFormEditorControl() {
		return _formEditorControl;
	}

	/**
	 * Whether the attribute is rendered hidden instead of the field.
	 */
	public boolean isAttributeHidden() {
		return _attributeHidden;
	}

	/**
	 * Whether the form editor is in edit mode.
	 */
	public boolean isInEditMode() {
		return _isInEditMode;
	}

	/**
	 * Whether the form is locked against changes.
	 */
	public boolean isLocked() {
		return _isLocked;
	}

	/**
	 * Domain to create attribute update for.
	 */
	public String getDomain() {
		return _domain;
	}

	/**
	 * The concrete type for which the form is displayed.
	 */
	public TLStructuredType getConcreteType() {
		return _concreteType;
	}

	/**
	 * The {@link LabelPlacement} determining the line where the label must be rendered.
	 */
	public LabelPlacement getLabelPlacement() {
		return _labelPlacement;
	}

	/**
	 * Executes the given {@link Function} with the given <code>labelPlacement</code> as
	 * {@link #getLabelPlacement()}.
	 * 
	 * @implNote When the given {@link LabelPlacement} is {@link LabelPlacement#DEFAULT}, the action
	 *           is executed without changing the {@link #getLabelPlacement() label placement}.
	 * 
	 * @param action
	 *        The {@link Function} that should be able to access the given label placement in
	 *        {@link #getLabelPlacement()}.
	 */
	public <T> T withLabelPlacement(LabelPlacement labelPlacement, Function<FormEditorContext, T> action) {
		if (labelPlacement == LabelPlacement.DEFAULT) {
			return action.apply(this);
		} else {
			LabelPlacement oldPlacement = _labelPlacement;
			_labelPlacement = Objects.requireNonNull(labelPlacement);
			try {
				return action.apply(this);
			} finally {
				_labelPlacement = oldPlacement;
			}
		}
	}

	/**
	 * Creates a {@link FormEditorContext}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static class Builder {

		private TLStructuredType _formType;

		private TLObject _model;

		private FormContext _formContext;

		private FormContainer _contentGroup;

		private FrameScope _frameScope;

		private FormEditorMapping _formEditorMapping;

		private String _formEditorControl;

		private boolean _attributeHidden;

		private boolean _isInEditMode;

		private FormMode _formMode;

		private boolean _isLocked;

		private String _domain;

		private TLStructuredType _concreteType;

		private LabelPlacement _labelPlacement = LabelPlacement.DEFAULT;

		/**
		 * Empty context builder.
		 */
		public Builder() {
			// Empty.
		}

		/**
		 * Preinitialize {@link Builder} with the given {@link FormEditorContext}.
		 */
		public Builder(FormEditorContext context) {
			this._formType = context._formType;
			this._model = context._model;
			this._formContext = context._formContext;
			this._contentGroup = context._contentGroup;
			this._frameScope = context._frameScope;
			this._formEditorMapping = context._formEditorMapping;
			this._formEditorControl = context._formEditorControl;
			this._attributeHidden = context._attributeHidden;
			this._isInEditMode = context._isInEditMode;
			this._formMode = context._formMode;
			this._domain = context._domain;
			this._isLocked = context._isLocked;
			this._concreteType = context._concreteType;
			this._labelPlacement = context._labelPlacement;
		}

		/**
		 * @see FormEditorContext#getFormType()
		 */
		public Builder formType(TLStructuredType type) {
			_formType = type;

			return this;
		}

		/**
		 * @see FormEditorContext#getModel()
		 */
		public Builder model(TLObject model) {
			_model = model;

			return this;
		}

		/**
		 * @see FormEditorContext#getFormContext()
		 */
		public Builder formContext(FormContext context) {
			_formContext = context;

			return this;
		}

		/**
		 * @see FormEditorContext#getContentGroup()
		 */
		public Builder contentGroup(FormContainer contentGroup) {
			_contentGroup = contentGroup;

			return this;
		}

		/**
		 * @see FormEditorContext#getFrameScope()
		 */
		public Builder frameScope(FrameScope scope) {
			_frameScope = scope;

			return this;
		}

		/**
		 * @see FormEditorContext#getFormEditorMapping()
		 */
		public Builder formEditorMapping(FormEditorMapping mapping) {
			_formEditorMapping = mapping;

			return this;
		}

		/**
		 * @see FormEditorContext#getFormEditorControl()
		 */
		public Builder formEditorControl(String formEditorControl) {
			_formEditorControl = formEditorControl;

			return this;
		}

		/**
		 * @see FormEditorContext#isAttributeHidden()
		 */
		public Builder attributeHidden(boolean attributeHidden) {
			_attributeHidden = attributeHidden;

			return this;
		}

		/**
		 * @see FormEditorContext#getFormMode()
		 */
		public Builder formMode(FormMode formMode) {
			_formMode = formMode;

			return this;
		}

		/**
		 * @see FormEditorContext#isInEditMode()
		 */
		public Builder editMode(boolean inEditMode) {
			_isInEditMode = inEditMode;

			return this;
		}

		/**
		 * @see FormEditorContext#isLocked()
		 */
		public Builder locked(boolean isLocked) {
			_isLocked = isLocked;

			return this;
		}

		/**
		 * @see FormEditorContext#getDomain()
		 */
		public Builder domain(String domain) {
			_domain = domain;

			return this;
		}

		/**
		 * @see FormEditorContext#getConcreteType()
		 */
		public Builder concreteType(TLStructuredType concreteType) {
			_concreteType = concreteType;

			return this;
		}

		/**
		 * @see FormEditorContext#getLabelPlacement()
		 */
		public Builder labelPlacement(LabelPlacement labelPlacement) {
			_labelPlacement = Objects.requireNonNull(labelPlacement);

			return this;
		}

		/**
		 * Creates the {@link FormEditorContext}.
		 */
		public FormEditorContext build() {
			FormEditorContext context = new FormEditorContext();

			context._formType = this._formType;
			context._model = this._model;
			context._formContext = this._formContext;
			context._contentGroup = this._contentGroup;
			context._frameScope = this._frameScope;
			context._formEditorMapping = this._formEditorMapping;
			context._formEditorControl = this._formEditorControl;
			context._attributeHidden = this._attributeHidden;
			context._isInEditMode = this._isInEditMode;
			context._formMode = this._formMode;
			context._isLocked = this._isLocked;
			context._domain = this._domain;
			context._concreteType = this._concreteType;
			context._labelPlacement = this._labelPlacement;

			return context;
		}

	}

}