/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.ExpressionTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.StartTagTemplate;
import com.top_logic.html.template.TagAttributeTemplate;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.html.template.TemplateSequence;
import com.top_logic.html.template.config.HTMLTagFormat;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.html.template.expr.LiteralText;
import com.top_logic.html.template.expr.VariableExpression;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DispatchingRenderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.formeditor.parts.I18NConstants;
import com.top_logic.layout.formeditor.parts.template.HTMLTemplateFormProvider.Config.TypeTemplate;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FormContextDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link FormElementTemplateProvider} creating a custom rendering using a HTML template with
 * arbitrary embedded contents.
 */
@Label("HTML template")
public class HTMLTemplateFormProvider
		extends AbstractFormElementProvider<HTMLTemplateFormProvider.Config<?>> {

	/**
	 * Configuration options for {@link HTMLTemplateFormProvider}.
	 */
	@TagName("html-template")
	@DisplayInherited(DisplayStrategy.PREPEND)
	@DisplayOrder({
		Config.TEMPLATE,
		Config.VARIABLES,
		Config.VALUE_TEMPLATES,
	})
	public interface Config<I extends HTMLTemplateFormProvider> extends FormElement<I>, TemplateConfig {

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
		 * The HTML template for the rendered object.
		 * 
		 * <p>
		 * The template has access to all properties of the rendered object and the additional
		 * {@link #getVariables()} defined below.
		 * </p>
		 * 
		 * <p>
		 * To access a property or variable named <code>var</code> use the template expression
		 * <code>{var}</code>.
		 * </p>
		 */
		@Name(TEMPLATE)
		@ControlProvider(CodeEditorControl.CPHtml.class)
		@ItemDisplay(ItemDisplayType.VALUE)
		@Format(HTMLTagFormat.class)
		@Mandatory
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
		@CollapseEntries
		Map<String, VariableDefinition.Config<?>> getVariables();
	}

	private static final ImageProvider IMAGE_PROVIDER =
		(any, flavor) -> Icons.FORM_EDITOR__REFERENCE;

	private Map<TLType, Template> _templateByType = new HashMap<>();

	private Template _template;

	/**
	 * Creates a {@link HTMLTemplateFormProvider}.
	 */
	public HTMLTemplateFormProvider(InstantiationContext context, Config<?> config) {
		super(context, config);

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
	
		private static final TagAttributeTemplate CONTEXT_MENU_ATTR =
			new TagAttributeTemplate(0, 0, HTMLConstants.TL_CONTEXT_MENU_ATTR,
				new ExpressionTemplate(new VariableExpression(0, 0, Control.ID)));

		private HTMLTemplate _fragment;

		private HashMap<String, VariableDefinition<?>> _params;

		private List<CommandHandler> _contextMenuCommands;

		/**
		 * Creates a {@link HTMLTemplateFormProvider.Template}.
		 */
		public Template(InstantiationContext context, TemplateConfig config) {
			HTMLTemplate template = config.getTemplate();
			_params = new HashMap<>();
			for (VariableDefinition.Config<?> entry : config.getVariables().values()) {
				_params.put(entry.getName(), context.getInstance(entry));
			}

			_contextMenuCommands = _params.values().stream()
				.flatMap(v -> v instanceof MenuVariable ? ((MenuVariable) v).getCommands().stream()
					: (v instanceof ButtonVariable
						? Collections.singletonList(((ButtonVariable) v).getCommand()).stream()
						: Collections.<CommandHandler> emptyList().stream()))
				.collect(Collectors.toList());

			if (!_contextMenuCommands.isEmpty()) {
				HTMLTemplateFragment fragment = template.getTemplate();

				if (fragment instanceof TagTemplate) {
					TagTemplate tag = (TagTemplate) fragment;
					StartTagTemplate start = tag.getStart();
					
					StartTagTemplate enhancedStart = new StartTagTemplate(start.getLine(), start.getColumn(), start.getName());
					enhancedStart.addAttribute(CONTEXT_MENU_ATTR);
					boolean hasClass = false;
					for (var attr : start.getAttributes()) {
						if (attr.getName().equals(HTMLConstants.TL_CONTEXT_MENU_ATTR)) {
							continue;
						} else if (attr.getName().equals(HTMLConstants.CLASS_ATTR)) {
							hasClass = true;
							TagAttributeTemplate classAttr =
								new TagAttributeTemplate(attr.getLine(), attr.getColumn(), attr.getName(),
									new TemplateSequence(
										Arrays.asList(
											new LiteralText(0, 0, AbstractControlBase.IS_CONTROL_CSS_CLASS),
											new LiteralText(0, 0, " "),
											attr.getContent())));
							enhancedStart.addAttribute(classAttr);
						} else {
							enhancedStart.addAttribute(attr);
						}
					}
					if (!hasClass) {
						enhancedStart.addAttribute(new TagAttributeTemplate(0, 0, HTMLConstants.CLASS_ATTR,
							new LiteralText(0, 0, AbstractControlBase.IS_CONTROL_CSS_CLASS)));
					}
					
					template = new HTMLTemplate(
						new TagTemplate(enhancedStart, tag.getContent()), template.getVariables(), template.getHtml());
				}
			}

			_fragment = template;
		}
	}

	private static Map<String, ControlCommand> COMMANDS = TLObjectFragment.createCommandMap(ContextMenuOpener.INSTANCE);

	/**
	 * {@link HTMLFragment} displaying a {@link TLObject} using a {@link HTMLTemplateFragment}.
	 */
	private final class TLObjectFragment extends AbstractVisibleControl
			implements ModelListener, ContextMenuOwner, Renderer<Object> {

		private final FormEditorContext _form;

		private final HashMap<String, VariableDefinition<?>> _vars;

		private final HTMLTemplate _fragment;

		private final TLObject _model;

		private Map<String, Object> _args;

		private List<CommandHandler> _contextMenuCommands;

		/**
		 * Creates a {@link TLObjectFragment}.
		 */
		public TLObjectFragment(FormEditorContext form, Template template, TLObject model) {
			super(COMMANDS);
			_form = form;
			_fragment = template._fragment;
			_vars = template._params;
			_contextMenuCommands = template._contextMenuCommands;
			_model = model;

			computeTemplateArguments();
		}

		@Override
		public Menu createContextMenu(String contextInfo) {
			LayoutComponent component = FormComponent.componentForMember(_form.getFormContext());
			Map<String, Object> args = ContextMenuUtil.createArguments(_model);
			Stream<CommandModel> buttonsStream =
				ContextMenuUtil.toButtonsStream(component, args, _contextMenuCommands);
			return ContextMenuUtil.toContextMenu(buttonsStream);
		}

		private void computeTemplateArguments() {
			_args = new HashMap<>();
			for (String varName : _fragment.getVariables()) {
				VariableDefinition<?> varDef = _vars.get(varName);
				if (varDef == null) {
					TLStructuredTypePart part = _model.tType().getPart(varName);
					if (part != null) {
						Object value = _model.tValue(part);
						_args.put(varName, value);
					}
				} else {
					LayoutComponent component = FormComponent.componentForMember(_form.getFormContext());
					Object value = varDef.eval(component, _form, _model);
					_args.put(varName, value);
				}
			}
		}

		@Override
		public Object getModel() {
			return _model;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			Renderer<Object> before = context.set(ExpressionTemplate.RENDERER, this);
			try {
				_fragment.write(context, out, this);
			} finally {
				if (before == null) {
					context.reset(ExpressionTemplate.RENDERER);
				} else {
					context.set(ExpressionTemplate.RENDERER, before);
				}
			}
		}

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			writeObject(context, out, value);
		}

		private void writeObject(DisplayContext context, TagWriter out, Object value) throws IOException {
			if (value instanceof TLObject) {
				applyTemplates(context, out, (TLObject) value);
			} else if (value instanceof Collection<?>) {
				for (Object element : (Collection<?>) value) {
					writeObject(context, out, element);
				}
			} else {
				writeValue(context, out, value);
			}
		}

		private void applyTemplates(DisplayContext context, TagWriter out, TLObject obj) throws IOException {
			TLStructuredType valueType = obj.tType();
			Template valueTemplate = _templateByType.get(valueType);
			if (valueTemplate != null) {
				new TLObjectFragment(_form, valueTemplate, obj).write(context, out);
			} else {
				writeValue(context, out, obj);
			}
		}

		private void writeValue(DisplayContext context, TagWriter out, Object obj) throws IOException {
			DispatchingRenderer.INSTANCE.write(context, out, obj);
		}

		@Override
		protected void attachRevalidated() {
			super.attachRevalidated();

			getScope().getFrameScope().getModelScope().addModelListener(_model, this);
		}

		@Override
		protected void detachInvalidated() {
			getScope().getFrameScope().getModelScope().removeModelListener(_model, this);

			super.detachInvalidated();
		}

		@Override
		public void notifyChange(ModelChangeEvent change) {
			computeTemplateArguments();
			requestRepaint();
		}

		@Override
		public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
			Object varValue = _args.get(propertyName);
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
			Object varValue = _args.get(propertyName);
			if (varValue != null) {
				return varValue;
			}

			if (_model != null) {
				TLStructuredTypePart part = _model.tType().getPart(propertyName);
				if (part != null) {
					Object value = _model.tValue(part);
					return value;
				}
			}

			return super.getPropertyValue(propertyName);
		}

		@Override
		public Optional<Collection<String>> getAvailableProperties() {
			Optional<Collection<String>> superProperties = super.getAvailableProperties();
			HashSet<String> result = superProperties.isEmpty() ? new HashSet<>() : new HashSet<>(superProperties.get());
			result.addAll(_args.keySet());
			result.addAll(_model.tType().getAllParts().stream().map(p -> p.getName()).collect(Collectors.toList()));
			return Optional.of(result);
		}

		@Override
		public RuntimeException errorNoSuchProperty(String propertyName) {
			throw new IllegalArgumentException(
				"No such property '" + propertyName + "' in '" + this + "', available model properties: "
					+ _model.tType().getAllParts().stream().map(p -> p.getName()).collect(Collectors.joining(", ")));
		}

		@Override
		public String toString() {
			return "Object of type '" + _model.tType() + "'";
		}
	}

	/**
	 * {@link HTMLFragment} displaying a collection of values by rendering the elements one after
	 * the other.
	 * 
	 * <p>
	 * Note: Implement {@link Collection} to decide whether it is empty in boolean expressions.
	 * </p>
	 */
	private final class CollectionFragment extends AbstractCollection<Object> implements HTMLFragment {

		private Collection<Object> _collection;

		/**
		 * Creates a {@link CollectionFragment}.
		 */
		public CollectionFragment(Collection<Object> collection) {
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

		@Override
		public Iterator<Object> iterator() {
			return _collection.iterator();
		}

		@Override
		public int size() {
			return _collection.size();
		}
	}

	private HTMLTemplateFragment designTemplate() {
		HTMLTemplateFragment legend = resource(I18NConstants.HTML_TEMPLATE);
		HTMLTemplateFragment contentTemplate;
		contentTemplate = resource(I18NConstants.RENDERED_OBJECTS_PREVIEW);

		/* Lock content of the preview fieldset box. It must not be possible to drop elements in the
		 * box. */
		return Templates.fieldsetBox(legend, contentTemplate, ConfigKey.none()).setCssClass(ReactiveFormCSS.RF_LOCKED);
	}

}
