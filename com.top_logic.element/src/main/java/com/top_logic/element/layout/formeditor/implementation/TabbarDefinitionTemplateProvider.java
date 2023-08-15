/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition.TabDefinition;
import com.top_logic.element.layout.formeditor.definition.WithUUID;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.FormDeckPaneModel;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.FormTabbarTemplate;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.util.TLContext;

/**
 * {@link FormElementTemplateProvider} creating a tabbar for grouping elements of a form under
 * different tabs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TabbarDefinitionTemplateProvider extends AbstractFormElementProvider<TabbarDefinition> {

	static final Property<Map<String, Integer>> SELECTED_TABS =
		TypedAnnotatable.propertyMap("selected tab indexes by field id");

	private static final ImageProvider IMAGE = ImageProvider.constantImageProvider(Icons.TABBAR);

	/**
	 * Creates a {@link TabbarDefinitionTemplateProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TabbarDefinitionTemplateProvider(InstantiationContext context, TabbarDefinition config) {
		super(context, config);

		initUUID(config);
	}

	@Override
	public HTMLTemplateFragment createDesignTemplate(FormEditorContext context) {
		return super.createDesignTemplate(context);
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		TabbarDefinition config = getConfig();

		String deckName = config.getUUID();

		DeckField deckField;
		FormContainer contentGroup = context.getContentGroup();
		boolean deckAlreadyExists = contentGroup.hasMember(deckName);
		if (deckAlreadyExists) {
			deckField = (DeckField) contentGroup.getMember(deckName);
		} else {
			deckField = new DeckField(deckName, contentGroup.getResources());
			contentGroup.addMember(deckField);
		}

		int id = 1;
		for (TabDefinition tabDef : config.getTabs()) {
			String tabName = "tab" + (id++);
			FormGroup tabGroup;
			if (deckField.hasMember(tabName)) {
				tabGroup = (FormGroup) deckField.getMember(tabName);
			} else {
				tabGroup = new FormGroup(tabName, contentGroup.getResources());
				deckField.addMember(tabGroup);
			}
			tabGroup.setLabel(tabDef.getLabel());
			TabDefinitionTemplateProvider tabProvider = TypedConfigUtil.createInstance(tabDef);
			context = new FormEditorContext.Builder(context).contentGroup(tabGroup).build();
			HTMLTemplateFragment tabTemplate = tabProvider.createTemplate(context);

			FormEditorUtil.template(tabGroup, Templates.div(tabTemplate));
		}

		// Remove left-over groups at the end.
		while (true) {
			String tabName = "tab" + (id++);
			if (deckField.hasMember(tabName)) {
				deckField.removeMember(deckField.getMember(tabName));
			} else {
				break;
			}
		}

		initTabSelection(deckField);
		if (!deckAlreadyExists) {
			// Only needed for newly created decks
			addTabSelectionListener(deckField);
		}

		return new FormTabbarTemplate(deckField.getName());
	}

	private void addTabSelectionListener(DeckField deckField) {
		FormDeckPaneModel deckPaneModel = deckField.getModel();
		deckPaneModel.addSingleSelectionListener(new SingleSelectionListener() {

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
					Object selectedObject) {
				Map<String, Integer> cache = TLContext.getContext().mkMap(SELECTED_TABS);
				cache.put(deckField.getName(), deckPaneModel.getSelectedIndex());
			}
		});
	}

	private void initTabSelection(DeckField deckField) {
		FormDeckPaneModel deckPaneModel = deckField.getModel();
		Map<String, Integer> cache = TLContext.getContext().get(SELECTED_TABS);
		Integer storedTabIndex = cache.get(deckField.getName());
		if (storedTabIndex == null) {
			// No tab selection recorded
			return;
		}
		int selectedTab = storedTabIndex.intValue();
		if (selectedTab >= deckPaneModel.getSelectableCards().size()) {
			// Number tabs have changed: cached index not longer valid
			return;
		}
		deckPaneModel.setSelectedIndex(selectedTab);
	}

	@Override
	protected InitializerIndex createConfigInitializers() {
		InitializerIndex result = super.createConfigInitializers();
		result.add(TabbarDefinition.class, TabbarDefinition.TABS, (model, property, value) -> {
			if (value instanceof WithUUID) {
				initUUID((TabbarDefinition) value);
			}
		});
		return result;
	}

	@Override
	protected void initConfiguration(TabbarDefinition model) {
		initUUID(model);
		super.initConfiguration(model);
	}

	private void initUUID(WithUUID model) {
		if (model.getUUID() == null) {
			model.setUUID(StringServices.randomUUID());
		}
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(80, DisplayUnit.PERCENT);
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
		return IMAGE;
	}

	@Override
	public boolean openDialog() {
		return true;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.TABBAR;
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext) throws IOException {
		List<TabDefinitionTemplateProvider> tabs = getConfig().getTabs()
			.stream()
			.map(TypedConfigUtil::createInstance)
			.collect(Collectors.toList());
		renderPlain(context, out, renderContext, tabs);
	}

}
