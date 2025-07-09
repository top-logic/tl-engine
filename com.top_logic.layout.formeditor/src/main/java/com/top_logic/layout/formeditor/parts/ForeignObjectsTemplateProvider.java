/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.boxes.reactive_tag.AbstractGroupSettings;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.FieldSetBoxTemplate;
import com.top_logic.layout.form.template.model.Self;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;

/**
 * {@link AbstractFormElementProvider} for {@link ForeignObjects}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ForeignObjectsTemplateProvider extends AbstractFormElementProvider<ForeignObjects> {

	private static final ImageProvider IMAGE_PROVIDER =
		(any, flavor) -> Icons.FORM_EDITOR__REFERENCE;

	private TLClass _targetType;

	private QueryExecutor _itemsExpr;

	private QueryExecutor _readOnlyExpr;

	private QueryExecutor _labelExpr;

	private List<CommandHandler> _buttons;

	/**
	 * Creates a new {@link ForeignObjectsTemplateProvider}.
	 */
	public ForeignObjectsTemplateProvider(InstantiationContext context, ForeignObjects config) {
		super(context, config);

		_targetType = OptionalTypeTemplateParameters.resolve(config);
		_itemsExpr = QueryExecutor.compileOptional(config.getItems());
		_readOnlyExpr = QueryExecutor.compileOptional(config.getReadOnly());
		_labelExpr = QueryExecutor.compileOptional(config.getLabel());

		_buttons = TypedConfigUtil.createInstanceList(getConfig().getButtons());
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
	protected DisplayDimension getDialogWidth() {
		return DisplayDimension.dim(650, DisplayUnit.PIXEL);
	}

	@Override
	protected DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(650, DisplayUnit.PIXEL);
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.FOREIGN_OBJECTS_LABEL;
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		ForeignObjects config = getConfig();

		FormDefinitionTemplateProvider globalLayout = TypedConfigUtil.createInstance(config.getLayout());
		TLObject model = context.getModel();
		Collection<?> objects = SearchExpression.asCollection(_itemsExpr.execute(model));
		FormContainer contentGroup = context.getContentGroup();

		String objectsName = uniqueName(contentGroup);
		FormGroup objectsGroup = new FormGroup(objectsName, ResPrefix.NONE);
		contentGroup.addMember(objectsGroup);

		ConfigKey personalizationKey = ConfigKey.field(objectsGroup);

		boolean noSeparateGroup = config.isNoSeparateGroup();

		ResKey title = config.getTitle();
		boolean hasTitle = title != null && !noSeparateGroup;

		boolean initiallyCollapsed = config.initiallyCollapsed() && !noSeparateGroup;

		for (Object obj : objects) {
			TLObject item = SearchExpression.asTLObjectNonNull(_itemsExpr.getSearch(), obj);
			String itemID = IdentifierUtil.toExternalForm(item.tIdLocal());

			FormContainer innerGroup = new FormGroup("object-" + itemID, ResPrefix.NONE);
			innerGroup.setStableIdSpecialCaseMarker(obj);
			innerGroup.setImmutable(displayReadOnly(_readOnlyExpr, item, model));
			if (initiallyCollapsed) {
				innerGroup.setCollapsed(true);
			}
			objectsGroup.addMember(innerGroup);

			HTMLTemplateFragment legend = text(label(_labelExpr, item));
			HTMLTemplateFragment contentTemplate;
			if (globalLayout != null) {
				FormEditorContext innerContext = new FormEditorContext.Builder(context)
					.formType(_targetType)
					.concreteType(null)
					.model(item)
					.contentGroup(innerGroup)
					.build();
				contentTemplate = globalLayout.createContentTemplate(innerContext);
			} else {
				TypedForm typedForm = TypedForm.lookup(null, item);
				FormDefinitionTemplateProvider localLayout =
					TypedConfigUtil.createInstance(typedForm.getFormDefinition());
				FormEditorContext innerContext = new FormEditorContext.Builder(context)
					.formType(typedForm.getFormType())
					.concreteType(typedForm.getDisplayedType())
					.model(item)
					.contentGroup(innerGroup)
					.build();
				contentTemplate = localLayout.createContentTemplate(innerContext);
			}
			Self content = self(contentTemplate);
			HTMLTemplateFragment objectTemplate;
			if (noSeparateGroup) {
				objectTemplate = content;
			} else {
				FieldSetBoxTemplate fieldsetBox = Templates.fieldsetBoxWrap(legend, content, ConfigKey.none())
					.setHasBorder(Boolean.valueOf(!hasTitle))
					.setGroupName(".");
				addButtons(fieldsetBox, item, false);

				objectTemplate = fieldsetBox;
			}
			template(innerGroup, objectTemplate);
		}

		HTMLTemplateFragment objectsTemplate;
		if (hasTitle) {
			objectsTemplate = Templates
				.fieldsetBoxWrap(resource(title), items(self()), personalizationKey)
				.setContainer(true);
		} else {
			objectsTemplate = items(self());
		}

		template(objectsGroup, div(objectsTemplate));
		return contentBox(member(objectsGroup));
	}

	private String uniqueName(FormContainer contentGroup) {
		String id = getConfig().getId();
		if (id == null) {
			// For legacy compatibility.
			int index = 1;
			while (contentGroup.hasMember("objects-" + index)) {
				index++;
			}
			return "objects-" + index;
		}
		return "objects-" + id;
	}

	private void addButtons(AbstractGroupSettings<?> template, TLObject targetModel, boolean designMode) {
		if (_buttons.isEmpty()) {
			return;
		}

		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		LayoutComponent component = MainLayout.getComponent(displayContext);

		Map<String, Object> args = ContextMenuUtil.createArguments(targetModel);
		Stream<CommandModel> buttonsStream = ContextMenuUtil.toButtonsStream(component, args, _buttons);
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

	@Override
	protected HTMLTemplateFragment createDesignTemplate(FormEditorContext context) {
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

}

