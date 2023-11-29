/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.config.HTMLTagFormat;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.formeditor.parts.ForeignObjectsTemplateProvider;
import com.top_logic.layout.formeditor.parts.I18NConstants;
import com.top_logic.layout.formeditor.parts.template.RenderedObjectsTemplateProvider.Config.TypeTemplate;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link FormElementTemplateProvider} creating a custom rendering for a list of objects.
 */
@Label("Rendered objects")
public class RenderedObjectsTemplateProvider
		extends AbstractFormElementProvider<RenderedObjectsTemplateProvider.Config<?>> {

	/**
	 * Configuration options for {@link RenderedObjectsTemplateProvider}.
	 */
	@TagName("rendered-objects")
	@DisplayInherited(DisplayStrategy.PREPEND)
	@DisplayOrder({
		Config.TEMPLATE,
		Config.VARIABLES,
		Config.VALUE_TEMPLATES,
	})
	public interface Config<I extends RenderedObjectsTemplateProvider> extends FormElement<I>, TemplateConfig {

		/** @see #getValueTemplates() */
		String VALUE_TEMPLATES = "value-templates";

		/**
		 * Templates for additional object types that are rendered.
		 */
		@Name(VALUE_TEMPLATES)
		@Key(TypeTemplate.TYPE)
		Map<TLModelPartRef, TypeTemplate> getValueTemplates();

		/**
		 * Configuration associating a {@link HTMLTemplateFragment} with a {@link TLType}.
		 */
		@DisplayOrder({
			TypeTemplate.TYPE,
			TypeTemplate.TEMPLATE,
			TypeTemplate.VARIABLES,
		})
		interface TypeTemplate extends TemplateConfig {
			/**
			 * @see #getType()
			 */
			String TYPE = "type";

			/**
			 * The type that uses the given {@link #getTemplate()}.
			 */
			@Name(TYPE)
			TLModelPartRef getType();

			@Override
			@DerivedRef(TYPE)
			TLModelPartRef getFormContextType();
		}
	}

	/**
	 * Options for defining a template.
	 */
	@Abstract
	public interface TemplateConfig extends FormContextDefinition {
		/**
		 * @see #getTemplate()
		 */
		String TEMPLATE = "template";

		/**
		 * @see #getVariables()
		 */
		String VARIABLES = "variables";

		/**
		 * The template to expand for object being rendered.
		 * 
		 * <p>
		 * The template has access to the {@link #getVariables()} defined below.
		 * </p>
		 */
		@Name(TEMPLATE)
		@ControlProvider(CodeEditorControl.CPHtml.class)
		@ItemDisplay(ItemDisplayType.VALUE)
		@Format(HTMLTagFormat.class)
		HTMLTemplate getTemplate();

		/**
		 * Additional variables to bind for the template evaluation.
		 * 
		 * <p>
		 * By default, all attributes of the rendered object are available as expressions in the
		 * template.
		 * </p>
		 */
		@Name(VARIABLES)
		@Key(VariableDefinition.Config.NAME)
		@DefaultContainer
		Map<String, VariableDefinition.Config<?>> getVariables();
	}

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.FORM_EDITOR__REFERENCE);

	private Map<TLType, Template> _templateByType = new HashMap<>();

	private LayoutComponent _component;

	private Template _template;

	/**
	 * Creates a new {@link ForeignObjectsTemplateProvider}.
	 */
	public RenderedObjectsTemplateProvider(InstantiationContext context, Config<?> config) {
		super(context, config);

		_component =
			DefaultDisplayContext.getDisplayContext().getSubSessionContext().getLayoutContext().getMainLayout();

		_template = new Template(context, config);

		for (TypeTemplate typeTemplate : config.getValueTemplates().values()) {
			TLType type = typeTemplate.getType().resolveType();
			_templateByType.put(type, new Template(context, typeTemplate));
		}
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return true;
	}

	@Override
	public boolean getIsTool() {
		return true;
	}

	@Override
	public ImageProvider getImageProvider() {
		return IMAGE_PROVIDER;
	}

	@Override
	public DisplayDimension getDialogWidth() {
		return DisplayDimension.dim(600, DisplayUnit.PIXEL);
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(800, DisplayUnit.PIXEL);
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.RENDERED_OBJECTS_LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		if (context.getFormMode() == FormMode.DESIGN) {
			return designTemplate();
		} else {
			return displayTemplate(context);
		}
	}

	private HTMLTemplateFragment displayTemplate(FormEditorContext form) {
		TLObjectFragment objectFragment = new TLObjectFragment(form, _template, form.getModel());

		return contentBox(htmlTemplate((displayContext, out) -> objectFragment.write(displayContext, out)));
	}

	private static class Template {
	
		private HTMLTemplate _fragment;

		private HashMap<String, VariableDefinition<?>> _params;

		/**
		 * Creates a {@link RenderedObjectsTemplateProvider.Template}.
		 */
		public Template(InstantiationContext context, TemplateConfig config) {
			_fragment = config.getTemplate();
			_params = new HashMap<>();
			for (VariableDefinition.Config<?> entry : config.getVariables().values()) {
				_params.put(entry.getName(), context.getInstance(entry));
			}
		}
	}

	/**
	 * {@link HTMLFragment} displaying a {@link TLObject} using a {@link HTMLTemplateFragment}.
	 */
	private final class TLObjectFragment extends AbstractVisibleControl {
		private final TLObject _obj;

		private final HTMLTemplate _fragment;

		private final Map<String, Object> _params = new HashMap<>();

		/**
		 * Creates a {@link TLObjectFragment}.
		 */
		public TLObjectFragment(FormEditorContext form, Template template, TLObject model) {
			_fragment = template._fragment;
			for (String varName : _fragment.getVariables()) {
				VariableDefinition<?> varDef = template._params.get(varName);
				if (varDef == null) {
					TLStructuredTypePart part = model.tType().getPart(varName);
					if (part != null) {
						Object value = model.tValue(part);
						_params.put(varName, wrap(form, value));
					}
				} else {
					Object value = varDef.eval(_component, form, model);
					_params.put(varName, wrap(form, value));
				}
			}
			_obj = model;
		}

		private Object wrap(FormEditorContext form, Object value) {
			if (value instanceof TLObject) {
				TLObject obj = (TLObject) value;
				TLStructuredType valueType = obj.tType();
				Template valueTemplate = _templateByType.get(valueType);
				if (valueTemplate != null) {
					return new TLObjectFragment(form, valueTemplate, obj);
				} else {
					return value;
				}
			} else if (value instanceof Collection<?>) {
				return new CollectionFragment(
					((Collection<?>) value).stream().map(e -> wrap(form, e)).collect(Collectors.toList()));
			} else {
				return value;
			}
		}

		@Override
		public Object getModel() {
			return _obj;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			_fragment.write(context, out, this);
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
			Object varValue = _params.get(propertyName);
			if (varValue != null) {
				render(context, out, varValue);
				return;
			}

			super.renderProperty(context, out, propertyName);
		}

		private void render(DisplayContext context, TagWriter out, Object value) throws IOException {
			ExpressionTemplate.renderValue(context, out, value);
		}

		@Override
		public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
			Object varValue = _params.get(propertyName);
			if (varValue != null) {
				return varValue;
			}

			TLStructuredTypePart part = _obj.tType().getPart(propertyName);
			if (part != null) {
				Object value = _obj.tValue(part);
				return value;
			}

			return super.getPropertyValue(propertyName);
		}

		@Override
		public Optional<Collection<String>> getAvailableProperties() {
			Optional<Collection<String>> superProperties = super.getAvailableProperties();
			HashSet<String> result = superProperties.isEmpty() ? new HashSet<>() : new HashSet<>(superProperties.get());
			result.addAll(_params.keySet());
			result.addAll(_obj.tType().getAllParts().stream().map(p -> p.getName()).collect(Collectors.toList()));
			return Optional.of(result);
		}

		@Override
		public RuntimeException errorNoSuchProperty(String propertyName) {
			throw new IllegalArgumentException(
				"No such property '" + propertyName + "' in '" + this + "', available model properties: "
					+ _obj.tType().getAllParts().stream().map(p -> p.getName()).collect(Collectors.joining(", ")));
		}

		@Override
		public String toString() {
			return "Object of type '" + _obj.tType() + "'";
		}
	}

	/**
	 * {@link HTMLFragment} displaying a collection of values by rendering the elements one after
	 * the other..
	 */
	private final class CollectionFragment implements HTMLFragment {

		private Collection<?> _collection;

		/**
		 * Creates a {@link CollectionFragment}.
		 */
		public CollectionFragment(Collection<?> collection) {
			_collection = collection;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			for (Object element : _collection) {
				if (element instanceof HTMLFragment) {
					((HTMLFragment) element).write(context, out);
				} else {
					ResourceRenderer.INSTANCE.write(context, out, element);
				}
			}
		}
	}

	private HTMLTemplateFragment designTemplate() {
		HTMLTemplateFragment legend = resource(I18NConstants.HTML_TEMPLATE);
		HTMLTemplateFragment contentTemplate;
		contentTemplate = resource(I18NConstants.RENDERED_OBJECTS_LABEL);

		/* Lock content of the preview fieldset box. It must not be possible to drop elements in the
		 * box. */
		return Templates.fieldsetBox(legend, contentTemplate, ConfigKey.none()).setCssClass("locked");
	}

}
