/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.config.HTMLTagFormat;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.formeditor.parts.ForeignObjectsTemplateProvider;
import com.top_logic.layout.formeditor.parts.I18NConstants;
import com.top_logic.layout.formeditor.parts.template.RenderedObjectsTemplateProvider.Config.TypeTemplate;
import com.top_logic.layout.formeditor.parts.template.VariableDefinition.EvalResult;
import com.top_logic.layout.formeditor.parts.template.VariableDefinition.EvalResult.InvalidateListener;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
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
		Config.ITEMS,
		Config.LIST_TEMPLATE,
		Config.VALUE_TEMPLATES,
	})
	public interface Config<I extends RenderedObjectsTemplateProvider> extends FormElement<I>, TypeTemplateParameters {

		/** @see #getItems() */
		String ITEMS = "items";

		/** @see #getListTemplate() */
		String LIST_TEMPLATE = "list-template";

		/** @see #getValueTemplates() */
		String VALUE_TEMPLATES = "value-templates";

		/**
		 * Function computing the objects to display.
		 * 
		 * <p>
		 * The function expects the component's model as single argument. The
		 * {@link #getListTemplate()} is responsible for rendering the objects.
		 * </p>
		 */
		@Name(ITEMS)
		@ItemDisplay(ItemDisplayType.VALUE)
		@Mandatory
		@Label("Displayed objects")
		Expr getItems();

		/**
		 * HTML template that renders the {@link #getItems() objects to display}.
		 * 
		 * <p>
		 * The only property that can be accessed is <code>{items}</code> which renders all elements
		 * to display. Separate templates can be provided for each type of rendered object in the
		 * {@link #getValueTemplates()} section.
		 * </p>
		 */
		@Name(LIST_TEMPLATE)
		@ControlProvider(CodeEditorControl.CPHtml.class)
		@ItemDisplay(ItemDisplayType.VALUE)
		@FormattedDefault("<div>{items}</div>")
		HTMLTemplateFragment getListTemplate();

		/**
		 * Templates for additional object types that are rendered.
		 */
		@DefaultContainer
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
		interface TypeTemplate extends ConfigurationItem {

			/**
			 * @see #getType()
			 */
			String TYPE = "type";

			/**
			 * @see #getTemplate()
			 */
			String TEMPLATE = "template";

			/**
			 * @see #getVariables()
			 */
			String VARIABLES = "variables";

			/**
			 * The type that uses the given {@link #getTemplate()}.
			 */
			@Name(TYPE)
			TLModelPartRef getType();

			/**
			 * The template to expand for objects of the given {@link #getType()}.
			 */
			@Name(TEMPLATE)
			@ControlProvider(CodeEditorControl.CPHtml.class)
			@ItemDisplay(ItemDisplayType.VALUE)
			@Format(HTMLTagFormat.class)
			HTMLTemplateFragment getTemplate();

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
	}

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.FORM_EDITOR__REFERENCE);

	private Map<TLType, Template> _templateByType = new HashMap<>();

	private LayoutComponent _component;

	/**
	 * Creates a new {@link ForeignObjectsTemplateProvider}.
	 */
	public RenderedObjectsTemplateProvider(InstantiationContext context, Config<?> config)
			throws ConfigurationException {
		super(context, config);

		// TODO: Does not work because form definitions are instantiated lately.
		// context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c ->
		// _component = c);

		_component =
			DefaultDisplayContext.getDisplayContext().getSubSessionContext().getLayoutContext().getMainLayout();

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
		return contentBox(htmlTemplate((displayContext, out) -> writeContents(displayContext, out, form)));
	}

	private void writeContents(DisplayContext displayContext, TagWriter out, FormEditorContext form)
			throws IOException {
		Config<?> config = getConfig();
		HTMLTemplateFragment listTemplate = config.getListTemplate();

		WithProperties properties = new WithProperties() {
			@Override
			public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
				switch (propertyName) {
					case "items":
						QueryExecutor itemsExpr = QueryExecutor.compile(config.getItems());
						Object result = itemsExpr.execute(form.getModel());
						return toFragment(result);
				}

				return WithProperties.super.getPropertyValue(propertyName);
			}
		};
		listTemplate.write(displayContext, out, properties);
	}

	private static class Template {
	
		private final HTMLTemplateFragment _fragment;
	
		private final Map<String, VariableDefinition> _params;
	
		/**
		 * Creates a {@link RenderedObjectsTemplateProvider.Template}.
		 */
		public Template(InstantiationContext context, TypeTemplate config) {
			HTMLTemplateFragment template = config.getTemplate();

			_fragment = template;
			_params = new HashMap<>();
			for (VariableDefinition.Config<?> entry : config.getVariables().values()) {
				_params.put(entry.getName(), context.getInstance(entry));
			}
		}
	}

	/**
	 * {@link HTMLFragment} displaying a {@link TLObject} using a {@link HTMLTemplateFragment}.
	 */
	private final class TLObjectFragment extends AbstractVisibleControl implements ModelListener, InvalidateListener {
		private final TLObject _obj;

		private final Template _template;

		private final List<EvalResult> _observedValues = new ArrayList<>();

		/**
		 * Creates a {@link TLObjectFragment}.
		 */
		private TLObjectFragment(Template template, TLObject obj) {
			_obj = obj;
			_template = template;
		}

		@Override
		public Object getModel() {
			return _obj;
		}

		@Override
		protected void attachRevalidated() {
			super.attachRevalidated();

			getScope().getFrameScope().getModelScope().addModelListener(_obj, this);
		}

		@Override
		protected void detachInvalidated() {
			getScope().getFrameScope().getModelScope().removeModelListener(_obj, this);

			for (EvalResult result : _observedValues) {
				result.removeInvalidateListener(this);
			}
			_observedValues.clear();

			super.detachInvalidated();
		}

		@Override
		public void notifyChange(ModelChangeEvent change) {
			switch (change.getChange(_obj)) {
				case CREATED:
				case UPDATED:
					requestRepaint();
					break;
				case DELETED:
					String id = getID();
					addUpdate(new RangeReplacement(id, id, Fragments.empty()));
					break;
				case NONE:
			}
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			_template._fragment.write(context, out, this);
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
			VariableDefinition variableDefinition = _template._params.get(propertyName);
			if (variableDefinition != null) {
				// Could be passed through the WithProperties API in the future.
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();

				Object value = eval(displayContext, variableDefinition);
				ExpressionTemplate.renderValue(context, out, value);
				return;
			}

			TLStructuredTypePart part = _obj.tType().getPart(propertyName);
			if (part != null) {
				Object value = _obj.tValue(part);
				if (value instanceof TLObject || value instanceof Collection<?>) {
					ExpressionTemplate.renderValue(context, out, toFragment(value));
					return;
				}
				ExpressionTemplate.renderValue(context, out, value);
				return;
			}

			super.renderProperty(context, out, propertyName);
		}

		@Override
		public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
			VariableDefinition variableDefinition = _template._params.get(propertyName);
			if (variableDefinition != null) {
				// Could be passed through the WithProperties API in the future.
				DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();

				return eval(displayContext, variableDefinition);
			}

			TLStructuredTypePart part = _obj.tType().getPart(propertyName);
			if (part != null) {
				Object value = _obj.tValue(part);
				if (value instanceof TLObject || value instanceof Collection<?>) {
					return toFragment(value);
				}
				return value;
			}

			return super.getPropertyValue(propertyName);
		}

		private Object eval(DisplayContext displayContext, VariableDefinition variableDefinition) {
			EvalResult result = variableDefinition.eval(displayContext, _component, _obj);
			if (result.addInvalidateListener(this)) {
				_observedValues.add(result);
			}

			return result.getValue(displayContext);
		}

		@Override
		public void handleValueInvalidation(EvalResult variable) {
			requestRepaint();
		}

		@Override
		public Optional<Collection<String>> getAvailableProperties() {
			Optional<Collection<String>> superProperties = super.getAvailableProperties();
			HashSet<String> result = superProperties.isEmpty() ? new HashSet<>() : new HashSet<>(superProperties.get());
			result.addAll(_template._params.keySet());
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
				toFragment(element).write(context, out);
			}
		}

	}

	private HTMLFragment toFragment(Object value) {
		if (value instanceof TLObject) {
			TLObject obj = (TLObject) value;
			TLStructuredType valueType = obj.tType();
			Template valueTemplate = _templateByType.get(valueType);
			if (valueTemplate != null) {
				return new TLObjectFragment(valueTemplate, obj);
			} else {
				return Fragments.rendered(ResourceRenderer.INSTANCE, value);
			}
		} else if (value instanceof Collection<?>) {
			return new CollectionFragment(((Collection<?>) value));
		} else {
			return Fragments.rendered(ResourceRenderer.INSTANCE, value);
		}
	}

	private HTMLTemplateFragment designTemplate() {
		Config<?> config = getConfig();
		TLClass targetType = OptionalTypeTemplateParameters.resolve(config);

		HTMLTemplateFragment legend;
		if (targetType != null) {
			legend = resource(I18NConstants.FOREIGN_OBJECTS_LEGEND_KEY_PREVIEW__TYPE.fill(targetType));
		} else {
			legend = empty();
		}
		HTMLTemplateFragment contentTemplate;
		contentTemplate = resource(I18NConstants.RENDERED_OBJECTS_LABEL);

		/* Lock content of the preview fieldset box. It must not be possible to drop elements in the
		 * box. */
		return Templates.fieldsetBox(legend, contentTemplate, ConfigKey.none()).setCssClass("locked");
	}

}
