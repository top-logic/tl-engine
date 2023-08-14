/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.layout.formeditor.builder.TypedFormDefinition;
import com.top_logic.element.layout.formeditor.implementation.GroupDefinitionTemplateProvider;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ComponentCommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.boxes.reactive_tag.AbstractGroupSettings;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.form.template.model.Member;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.util.css.CssUtil;

/**
 * {@link AbstractFormElementProvider} for {@link ForeignObjects}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ForeignObjectsTemplateProvider extends AbstractFormElementProvider<ForeignObjects> {

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.FORM_EDITOR__REFERENCE);

	/**
	 * Creates a new {@link ForeignObjectsTemplateProvider}.
	 */
	public ForeignObjectsTemplateProvider(InstantiationContext context, ForeignObjects config) {
		super(context, config);
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
		return DisplayDimension.dim(650, DisplayUnit.PIXEL);
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(650, DisplayUnit.PIXEL);
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.FOREIGN_OBJECTS_LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		if (context.getFormMode() == FormMode.DESIGN) {
			return designTemplate(context);
		} else {
			return displayTemplate(context);
		}
	}

	private HTMLTemplateFragment displayTemplate(FormEditorContext context) {
		QueryExecutor itemsExpr = QueryExecutor.compile(getConfig().getItems());
		QueryExecutor readOnlyExpr = QueryExecutor.compileOptional(getConfig().getReadOnly());
		TLObject model = context.getModel();
		Collection<?> objects = SearchExpression.asCollection(itemsExpr.execute(model));
		QueryExecutor labelExpr = QueryExecutor.compileOptional(getConfig().getLabel());
		HTMLTemplateFragment[] templates = new HTMLTemplateFragment[objects.size()];
		FormContainer contentGroup = context.getContentGroup();
		ConfigKey personalizationKey = ConfigKey.field(contentGroup);
		personalizationKey = ConfigKey.derived(personalizationKey, "foreignObjects");
		boolean noSeparateGroup = getConfig().isNoSeparateGroup();
		int index = 0;
		for (Object obj : objects) {
			TLObject item = SearchExpression.asTLObjectNonNull(itemsExpr.getSearch(), obj);
			String itemID = IdentifierUtil.toExternalForm(item.tIdLocal());

			FormContainer innerGroup = new FormGroup("foreignObjects-" + itemID, ResPrefix.NONE);
			innerGroup.setStableIdSpecialCaseMarker(obj);
			innerGroup.setImmutable(displayReadOnly(readOnlyExpr, item, model));
			contentGroup.addMember(innerGroup);

			HTMLTemplateFragment legend = text(label(labelExpr, item));
			HTMLTemplateFragment contentTemplate;
			TypedFormDefinition configuredForm = configuredForm(item);
			if (configuredForm != null) {
				FormDefinitionTemplateProvider localLayout =
					TypedConfigUtil.createInstance(configuredForm.getFormDefinition());
				FormEditorContext innerContext = new FormEditorContext.Builder(context)
					.formType(OptionalTypeTemplateParameters.resolve(configuredForm))
					.concreteType(item.tType())
					.model(item)
					.contentGroup(innerGroup)
					.build();
				contentTemplate = localLayout.createContentTemplate(innerContext);
			} else {
				TypedForm typedForm = TypedForm.lookup(null, item);
				FormDefinitionTemplateProvider globalLayout =
					TypedConfigUtil.createInstance(typedForm.getFormDefinition());
				FormEditorContext innerContext = new FormEditorContext.Builder(context)
					.formType(typedForm.getFormType())
					.concreteType(typedForm.getDisplayedType())
					.model(item)
					.contentGroup(innerGroup)
					.build();
				contentTemplate = globalLayout.createContentTemplate(innerContext);
			}
			ConfigKey derivedKey = ConfigKey.derived(personalizationKey, itemID);
			Member content = member(innerGroup, contentTemplate);
			HTMLTemplateFragment finalTemplate;
			if (noSeparateGroup) {
				finalTemplate = content;
			} else {
				FieldSetBoxTemplate fieldsetBox = Templates.fieldsetBoxWrap(legend, content, derivedKey);
				addButtons(fieldsetBox, item, false);
				finalTemplate = fieldsetBox;
			}
			templates[index] = finalTemplate;
			index++;
		}
		return contentBox(div(templates));
	}

	private TypedFormDefinition configuredForm(TLObject item) {
		Map<TLModelPartRef, TypedFormDefinition> specializedLayouts = getConfig().getLayoutSpecializations();
		if (specializedLayouts.isEmpty()) {
			return fromConfig();
		}
		TLStructuredType type = item.tType();
		while (type != null) {
			TypedFormDefinition specializedLayout = specializedLayouts.get(TLModelPartRef.ref(type));
			if (specializedLayout != null) {
				return specializedLayout;
			}
			type = TLModelUtil.getPrimaryGeneralization(type);
		}
		return fromConfig();
	}

	private TypedFormDefinition fromConfig() {
		FormDefinition form = getConfig().getLayout();
		if (form == null) {
			return null;
		}
		TypedFormDefinition result = TypedConfiguration.newConfigItem(TypedFormDefinition.class);
		result.setType(getConfig().getType());
		result.setFormDefinition(form);
		return result;
	}

	private void addButtons(AbstractGroupSettings<?> template, TLObject targetModel, boolean designMode) {
		List<CommandHandler> buttons = TypedConfigUtil.createInstanceList(getConfig().getButtons());
		if (buttons.isEmpty()) {
			return;
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		LayoutComponent component = MainLayout.getComponent(displayContext);

		Map<String, Object> args = ContextMenuUtil.createArguments(targetModel);
		Stream<ComponentCommandModel> buttonsStream = ContextMenuUtil.toButtonsStream(component, args, buttons);
		Menu menu;
		if (designMode) {
			List<List<? extends CommandModel>> deactivated = ContextMenuUtil.groupAndSort(buttonsStream)
				.map(DeactivatedCommandModel::deactivateCommands)
				.collect(Collectors.toList());
			menu = Menu.create(deactivated);
		} else {
			menu = ContextMenuUtil.toContextMenu(buttonsStream);
		}
		template.setMenu(menu);
	}

	private boolean displayReadOnly(QueryExecutor readOnlyExpr, TLObject item, TLObject baseModel) {
		boolean readOnly;
		if (readOnlyExpr == null) {
			readOnly = false;
		} else {
			readOnly = SearchExpression.asBoolean(readOnlyExpr.execute(item, baseModel));
		}
		return readOnly;
	}

	private static String label(QueryExecutor labelExpr, TLObject item) {
		return StringServices.nonNull(labelRaw(labelExpr, item));
	}

	private static String labelRaw(QueryExecutor labelExpr, TLObject item) {
		if (labelExpr == null) {
			return MetaLabelProvider.INSTANCE.getLabel(item);
		} else {
			return MetaLabelProvider.INSTANCE.getLabel(labelExpr.execute(item));
		}
	}

	private HTMLTemplateFragment designTemplate(FormEditorContext context) {
		ForeignObjects config = getConfig();

		TLClass targetType = OptionalTypeTemplateParameters.resolve(config);
		FormDefinitionTemplateProvider layout = TypedConfigUtil.createInstance(config.getLayout());

		HTMLTemplateFragment legend;
		if (targetType != null) {
			legend = resource(I18NConstants.FOREIGN_OBJECTS_LEGEND_KEY_PREVIEW__TYPE.fill(targetType));
		} else {
			legend = empty();
		}
		HTMLTemplateFragment contentTemplate;
		if (targetType == null) {
			contentTemplate = resource(I18NConstants.FOREIGN_OBJECTS_NO_TYPE_DEFINED);
		} else if (layout == null) {
			contentTemplate = resource(I18NConstants.FOREIGN_OBJECTS_NO_DISPLAY_DEFINED);
		} else {
			/* The input elements for objects of a different type are created, without having the
			 * foreign object. Therefore a domain must be set. */
			context = new FormEditorContext.Builder(context)
				.formType(targetType)
				.model(null)
				.locked(true)
				.domain(StringServices.randomUUID())
				.build();
			contentTemplate = layout.createContentTemplate(context);
		}
		boolean noSeparateGroup = getConfig().isNoSeparateGroup();
		HTMLTemplateFragment finalTemplate;
		if (noSeparateGroup) {
			finalTemplate =
				div(css(CssUtil.joinCssClasses(ReactiveFormCSS.RF_LOCKED, ReactiveFormCSS.RF_WRAPPER)),
				div(css(ReactiveFormCSS.RF_CONTAINER), contentTemplate));
		} else {
			FieldSetBoxTemplate boxTemplate = Templates.fieldsetBox(legend, contentTemplate, ConfigKey.none());
			/* Lock content of the preview fieldset box. It must not be possible to drop elements in
			 * the box. */
			boxTemplate.setCssClass(ReactiveFormCSS.RF_LOCKED);
			addButtons(boxTemplate, null, true);
			finalTemplate = boxTemplate;
		}
		return finalTemplate;
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext) throws IOException {
		QueryExecutor itemsExpr = QueryExecutor.compile(getConfig().getItems());
		Collection<?> objects = SearchExpression.asCollection(itemsExpr.execute(renderContext.getModel()));
		QueryExecutor labelExpr = QueryExecutor.compileOptional(getConfig().getLabel());
		boolean noSeparateGroup = getConfig().isNoSeparateGroup();
		for (Object obj : objects) {
			TLObject item = SearchExpression.asTLObjectNonNull(itemsExpr.getSearch(), obj);
			FormElementTemplateProvider layout;
			FormEditorContext innerContext;
			TypedFormDefinition configuredForm = configuredForm(item);
			if (configuredForm != null) {
				layout = TypedConfigUtil.createInstance(configuredForm.getFormDefinition());
				innerContext = new FormEditorContext.Builder(renderContext)
					.formType(OptionalTypeTemplateParameters.resolve(configuredForm))
					.concreteType(item.tType())
					.model(item)
					.build();
			} else {
				TypedForm typedForm = TypedForm.lookup(null, item);
				layout = TypedConfigUtil.createInstance(typedForm.getFormDefinition());
				innerContext = new FormEditorContext.Builder(renderContext)
					.formType(typedForm.getFormType())
					.concreteType(typedForm.getDisplayedType())
					.model(item)
					.build();
			}

			if (noSeparateGroup) {
				layout.renderPDFExport(context, out, innerContext);
			} else {
				HTMLUtil.beginDiv(out, GroupDefinitionTemplateProvider.PDF_EXPORT_CSS);

				HTMLUtil.beginDiv(out, GroupDefinitionTemplateProvider.PDF_HEADER_CSS);
				out.writeText(label(labelExpr, item));
				out.endTag(HTMLConstants.DIV);

				HTMLUtil.beginDiv(out, GroupDefinitionTemplateProvider.PDF_CONTENT_CSS);
				layout.renderPDFExport(context, out, innerContext);
				out.endTag(HTMLConstants.DIV);

				out.endTag(HTMLConstants.DIV);
			}

		}
	}

}

