/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.function.Predicate;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.Columns;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.LabelPlacement;
import com.top_logic.model.form.implementation.FormDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;

/**
 * Creates elements for the GUI based on a {@link TLFormDefinition} blueprint and a mapping between
 * them.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorEditorControl extends FormEditorDisplayControl {

	private static final boolean ATTRIBUTE_HIDDEN = true;

	private LabelPlacement _labelPlacement;

	private Columns _maxCols;

	private FormEditorPreviewControl _cPreview;

	/**
	 * Create a new {@link FormEditorEditorControl} by the given parameters and initializes a
	 * mapping between the representations on the GUI and the model.
	 * 
	 * @param model
	 *        The structure of the form.
	 * @param type
	 *        The class to get the elements.
	 * @param resPrefix
	 *        The {@link ResPrefix} to create an {@link AttributeFormContext}.
	 * @param isInEditMode
	 *        Whether the editor is in edit mode.
	 * @param cPreview
	 *        The {@link FormEditorPreviewControl} where this control was created for.
	 */
	public FormEditorEditorControl(FormDefinition model, TLStructuredType type, ResPrefix resPrefix,
			boolean isInEditMode, FormEditorPreviewControl cPreview) {
		super(type, model, resPrefix, isInEditMode);
		attachListenersToModel();
		_cPreview = cPreview;
	}

	private void attachListenersToModel() {
		FormDefinition model = getModel();
		PropertyDescriptor property = ContentDefinitionUtil.getContentProperty(model);
		new RepaintTrigger(property, true, kind -> kind == Kind.SET || kind == Kind.REMOVE, this).attachTo(model);
		new RepaintTrigger(property, true, kind -> kind == Kind.ADD, this).attachTo(model);
	}
	
	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		_labelPlacement = getModel().getLabelPlacement();
		_maxCols = getModel().getColumns();

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		if (_isInEditMode) {
			out.writeAttribute(ONDBLCLICK_ATTR,
				"services.form.FormEditorControl.editElement(event, '" + _cPreview.getID() + "', '" + _cPreview.getID()
					+ "', '" + "form" + "')");
		}
		out.endBeginTag();
		writeContent(context, out, _cPreview.getFormContext());
		out.endTag(DIV);
	}

	@Override
	protected void internalRequestRepaint() {
		_cPreview.requestRepaint();

		super.internalRequestRepaint();
	}

	@Override
	void writeContent(DisplayContext context, TagWriter out, AttributeFormContext formContext) throws IOException {
		createTemplate(formContext);
		writeTemplate(context, out, formContext);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(ReactiveFormCSS.RF_INNER_TARGET);

		if (_labelPlacement == LabelPlacement.IN_FRONT_OF_INPUT) {
			out.append("rf_labelInFrontOfInput");
		} else if (_labelPlacement == LabelPlacement.ABOVE) {
			out.append("rf_labelAbove");
		}

		switch (_maxCols) {
			case DEFAULT:
				break;
			case ONE:
				out.append("maxCols1");
				break;
			case TWO:
				out.append("maxCols2");
				break;
			case THREE:
				out.append("maxCols3");
				break;
			case FOUR:
				out.append("maxCols4");
				break;
			case FIVE:
				out.append("maxCols5");
				break;
		}
	}

	private void createTemplate(AttributeFormContext fc) {
		if (getModel() != null) {
			FormDefinition form = getModel();
			FormDefinitionTemplateProvider formImpl = TypedConfigUtil.createInstance(form);

			FormEditorContext formContext = new FormEditorContext.Builder()
				.formMode(FormMode.DESIGN)
				.formType(getType())
				.model(getContextModel())
				.formContext(fc)
				.contentGroup(fc)
				.frameScope(getFrameScope())
				.formEditorMapping(_cPreview.getFormEditorMapping())
				.formEditorControl(_cPreview.getID())
				.attributeHidden(ATTRIBUTE_HIDDEN)
				.editMode(_isInEditMode)
				.build();

			FormEditorUtil.template(fc, div(formImpl.createDesignTemplate(formContext)));
		} else {
			FormEditorUtil.template(fc, div());
		}
	}

	/**
	 * {@link ConfigurationListener} triggering {@link AbstractControlBase#requestRepaint()} if a
	 * relevant change occurs.
	 */
	static final class RepaintTrigger implements ConfigurationListener {
		private final AbstractControlBase _control;

		private final Predicate<Kind> _kindCheck;

		private PropertyDescriptor _property;

		private boolean _deep;

		/**
		 * Creates a {@link RepaintTrigger}.
		 * 
		 * @param property
		 *        The {@link PropertyDescriptor} to listen to.
		 * @param deep
		 *        Whether it goes recursively through all properties.
		 * @param kindCheck
		 *        The predicate that decides about invalidation.
		 * @param control
		 *        The control to invalidate.
		 */
		public RepaintTrigger(PropertyDescriptor property, boolean deep, Predicate<Kind> kindCheck,
				AbstractControlBase control) {
			_property = property;
			_deep = deep;
			_control = control;
			_kindCheck = kindCheck;
		}

		/**
		 * Adds this {@link ConfigurationListener} to the given model.
		 * 
		 * @param model
		 *        The {@link ContainerDefinition} to check for changes.
		 */
		public void attachTo(Object model) {
			if (model instanceof ContainerDefinition) {
				internalAttachTo((ContainerDefinition<?>) model);
			} else if (model instanceof TabbarDefinition) {
				for (ContainerDefinition<?> tab : ((TabbarDefinition) model).getTabs()) {
					internalAttachTo(tab);
				}
			}
		}

		private void internalAttachTo(ContainerDefinition<?> model) {
			model.addConfigurationListener(_property, this);
			if (_deep) {
				for (PolymorphicConfiguration<? extends FormElementTemplateProvider> content : model.getContent()) {
					attachTo(content);
				}
			}
		}

		public void detachFrom(Object model) {
			if (model instanceof ContainerDefinition) {
				internalDetachFrom((ContainerDefinition<?>) model);
			} else if (model instanceof TabbarDefinition) {
				for (ContainerDefinition<?> tab : ((TabbarDefinition) model).getTabs()) {
					internalDetachFrom(tab);
				}
			}
		}

		private void internalDetachFrom(ContainerDefinition<?> model) {
			model.removeConfigurationListener(_property, this);
			if (_deep) {
				for (PolymorphicConfiguration<? extends FormElementTemplateProvider> content : model.getContent()) {
					detachFrom(content);
				}
			}
		}

		@Override
		public void onChange(ConfigurationChange change) {
			if (_deep) {
				// Adjust listeners in content.

				switch (change.getKind()) {
					case SET:
						detachFrom(change.getOldValue());
						//$FALL-THROUGH$
					case ADD:
						attachTo(change.getNewValue());
						break;

					case REMOVE: {
						detachFrom(change.getOldValue());
						break;
					}
				}
			}

			Kind kind = change.getKind();
			if (_kindCheck.test(kind)) {
				_control.requestRepaint();
			}
		}

	}
}
