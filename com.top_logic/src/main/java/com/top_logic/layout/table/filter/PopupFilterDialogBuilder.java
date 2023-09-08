/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.basic.PopupHandler;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.AbstractSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.table.I18NConstants;
import com.top_logic.layout.table.control.TableControl.SortCommand;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.ToBeValidated;

/**
 * A {@link FilterDialogBuilder} that uses {@link PopupDialogControl}s to create filter dialogs.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class PopupFilterDialogBuilder implements FilterDialogBuilder {
	
	private static final String CONTEXT_NAME = PopupFilterDialogBuilder.class.getSimpleName();

	static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(PopupFilterDialogBuilder.class);

	public static final ResKey STANDARD_TITLE = RES_PREFIX.key("title");

	public static final String INVERT_FIELD = "invert";

	private static final String SHOW_ALL_FIELD = "showAll";

	private static final String RESET_FIELD = "reset";

	private static final String APPLY_FIELD = "apply";

	private static final String BUTTON_FORM_GROUP = "buttons";

	public static final String FILTER_BUTTONS_CSS_CLASS = "fltButtons";

	public static final String FILTER_CONTENT_AREA_CSS_CLASS = "fltSubContent";

	public static final String FILTER_COMMAND_AREA_CSS_CLASS = "fltCommandContent";

	private static final String FILTER_CONTAINER_CLASS = "fltGlobalContainer";

	private static final String FILTER_SETTINGS_CLASS = "fltSettings";

	static final String FILTER_SETTINGS_FORM_GROUP = "filterList";

	private static final String FILTER_CONTEXT = "filterDialogContent";

	private static final Document BUTTON_TEMPLATE = DOMUtil.parseThreadSafe("<t:group"
		+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
		+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ ">"
		+ "<div class='" + FILTER_COMMAND_AREA_CSS_CLASS + "'>"
		+ "<p:field name='" + BUTTON_FORM_GROUP + "'>"
		+ "<t:list>"
		+ "<div class='" + FILTER_BUTTONS_CSS_CLASS + "'>"
		+ "<t:items />"
		+ "</div>"
		+ "</t:list>"
		+ "</p:field>"
		+ "</div>"
		+ "</t:group>");

	private final ResPrefix RESOURCES = RES_PREFIX;
	
	private PopupDialogModel dialogModel;
	
	public PopupFilterDialogBuilder(PopupDialogModel dialogModel) {
		assert dialogModel != null : "The dialog model must not be null!";
		this.dialogModel = dialogModel;
	}

	@Override
	public PopupDialogModel getDialogModel() {
		return dialogModel;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param sortControl
	 *        Optional {@link BlockControl} containing {@link SortCommand}s for column. If empty the column
	 *        is not sortable and only filter options will be displayed.
	 */
	@Override
	public Control createFilterDialogContent(TableFilterModel filterModel, DisplayContext context,
											 FormContext form, Optional<BlockControl> sortControl) {
		ArrayList<TableFilterCommand> tableFilterCommands = new ArrayList<>(3);
		tableFilterCommands.add(filterModel.createApplyFilterCommand());
		tableFilterCommands.add(filterModel.createResetFilterCommand());
		tableFilterCommands.add(filterModel.createShowAllFilterCommand());

		FormGroup content = new FormGroup(FILTER_CONTEXT, RESOURCES);
		form.addMember(content);

		BlockControl globalContainer =
			createBlockControl(createContainerRenderer(FILTER_CONTAINER_CLASS));
		BlockControl contentContainer =
			createBlockControl(createContainerRenderer(FILTER_SETTINGS_CLASS));

		if (sortControl.isPresent()) {
			BlockControl blockControl = sortControl.get();
			blockControl.addChild(Fragments.hr());
			BlockControl subContentContainer = createBlockControl(SubFilterContentRenderer.INSTANCE);
			subContentContainer.addChild(blockControl);
			contentContainer.addChild(subContentContainer);
		}
		addFilterControls(filterModel, context, tableFilterCommands, content, contentContainer);
		globalContainer.addChild(contentContainer);

		// Add button control for global commands
		globalContainer.addChild(createButtonGroupControl(form, content,
			tableFilterCommands.get(0), tableFilterCommands.get(1), tableFilterCommands.get(2)));

		return globalContainer;
	}

	private DefaultSimpleCompositeControlRenderer createContainerRenderer(String containerClass) {
		return DefaultSimpleCompositeControlRenderer.divWithCSSClass(containerClass);
	}

	public Control createSidebarContent(TableFilterModel filterModel, DisplayContext context,
			FormContext form) {
		ArrayList<TableFilterCommand> tableFilterCommands = new ArrayList<>(3);
		tableFilterCommands.add(filterModel.createApplyFilterCommand());
		
		FormGroup content = new FormGroup(FILTER_CONTEXT, RESOURCES);
		form.addMember(content);
		
		BlockControl globalContainer = createBlockControl(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
		addFilterControls(filterModel, context, tableFilterCommands, content, globalContainer);
		ValueListener valueListener = createValueListener(context, form, tableFilterCommands);
		Iterator<? extends FormField> descendantFields = content.getDescendantFields();
		while (descendantFields.hasNext()) {
			descendantFields.next().addValueListener(valueListener);
		}
		
		return globalContainer;
	}

	private ValueListener createValueListener(final DisplayContext context,
			final FormContext formContext, final ArrayList<TableFilterCommand> tableFilterCommands) {
		final LayoutContext layoutContext = context.getLayoutContext();
		final ToBeValidated toBeValidated = new ToBeValidated() {

			@Override
			public void validate(DisplayContext context) {
				if (hasNoErrors()) {
					tableFilterCommands.get(0).executeCommand(context);
				}
			}

			private boolean hasNoErrors() {
				formContext.checkAll();
				return !formContext.hasError();
			}
		};

		ValueListener valueListener = new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (shallUpdateFilterSettings()) {
					layoutContext.notifyInvalid(toBeValidated);
				}
			}

			private boolean shallUpdateFilterSettings() {
				return !formContext.isSet(FilterViewControl.PROGRAMMATIC_UPDATE);
			}
		};
		return valueListener;
	}

	private void addFilterControls(TableFilterModel filterModel, DisplayContext context,
			ArrayList<TableFilterCommand> tableFilterCommands, FormGroup content, BlockControl globalContainer) {
		BlockControl subContentContainer = createBlockControl(SubFilterContentRenderer.INSTANCE);
				
		// Iterate over filters and create individual content parts
		List<FilterGroup> filterGroups = filterModel.getInternalFilterGroups();
		int filterControlId = 0;
		for (FilterGroup filterGroup : filterGroups) {
			List<ConfiguredFilter> filters = filterGroup.getFilters();
			BlockControl filterGroupContainer = createBlockControl(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
			if (filterGroup.getLabel() != ResKey.NONE) {
				filterGroupContainer.addChild(new FilterGroupTitleControl(filterGroup.getLabel()));
			}
			for (int j = 0, groupSize = filters.size(); j < groupSize; j++, filterControlId++) {
				ConfiguredFilter configuredFilter = filters.get(j);
				FilterViewControl<?> filterControl =
					configuredFilter.getDisplayControl(context, content, filterControlId);
				registerTableFilterCommands(tableFilterCommands, filterControl);
				filterGroupContainer.addChild(filterControl);
			}
			subContentContainer.addChild(filterGroupContainer);
		}
		globalContainer.addChild(subContentContainer);
		
		// On demand add inversion option switch
		if (filterModel.isShowingInversionOption()) {
			FilterViewControl<?> inversionOptionControl = createInversionOptionControl(content, filterModel);
			registerTableFilterCommands(tableFilterCommands, inversionOptionControl);
			globalContainer.addChild(inversionOptionControl);
		}
	}


	private void registerTableFilterCommands(ArrayList<TableFilterCommand> tableFilterCommands,
			FilterViewControl<?> filterControl) {
		for (TableFilterCommand filterCommand : tableFilterCommands) {
			filterCommand.registerFilterControl(filterControl);
		}
	}

	private BlockControl createBlockControl(ControlRenderer<? super BlockControl> renderer) {
		BlockControl blockControl = new BlockControl();
		blockControl.setRenderer(renderer);
		return blockControl;
	}

	@Override
	public void openFilterDialog(Control contentControl, DisplayContext context, PopupHandler handler,
									TableFilterModel tableFilterModel, FormGroup filterContext) {
		PopupDialogControl popup = handler.createPopup(dialogModel);
		popup.setReturnCommand((CommandField) filterContext.getFirstMemberRecursively(APPLY_FIELD));
		popup.setContent(contentControl);
		handler.openPopup(popup);
	}
	
	private FormGroupControl createButtonGroupControl(FormContext formContext, FormGroup filterContent,
			Command applyCommand, Command resetCommand, Command showAllCommand) {
		
		FormGroup buttons = new FormContext(BUTTON_FORM_GROUP, RESOURCES);
		filterContent.addMember(buttons);
		
		CommandField applyButton = createCommandField(APPLY_FIELD, I18NConstants.FILTER_APPLY, Icons.FILTER_APPLY,
			createClosingDialogApplyCommand(applyCommand, formContext));
		buttons.addMember(applyButton);

		CommandField resetButton =
			createCommandField(RESET_FIELD, I18NConstants.FILTER_RESET, Icons.FILTER_RESET, resetCommand);
		buttons.addMember(resetButton);

		CommandField showAllButton =
			createCommandField(SHOW_ALL_FIELD, I18NConstants.FILTER_SHOW_ALL, Icons.FILTER_SHOW_ALL,
				createClosingDialogShowAllCommand(showAllCommand));
		buttons.addMember(showAllButton);
		
		DefaultFormFieldControlProvider provider = new DefaultFormFieldControlProvider() {
			
			@Override
			public Control visitCommandField(CommandField member, Void arg) {
				return new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
			}
		};
		
		return new FormGroupControl(filterContent,
									provider,
									DOMUtil.getFirstElementChild(getButtonsTemplate().getDocumentElement()),
									RES_PREFIX);
	}

	private CommandField createCommandField(String applyFieldName, ResKey applyLabel, ThemeImage applyImage,
			Command applyCommand) {
		CommandField applyButton = FormFactory.newCommandField(applyFieldName, applyCommand);
		applyButton.setLabel(Resources.getInstance().getString(applyLabel));
		applyButton.setImage(applyImage);
		return applyButton;
	}

	/**
	 * Creates the layout of the inversion option field
	 * 
	 * @param form
	 *        The parent filter dialog form group.
	 * @param filterModel
	 *        The model of the table filter
	 * 
	 * @return the button group, of the filter dialog
	 */
	private FilterViewControl<?> createInversionOptionControl(FormGroup form,
			TableFilterModel filterModel) {

		// Definition of the form group structure
		FormGroup filterSettings = new FormGroup(FILTER_SETTINGS_FORM_GROUP, RESOURCES);
		form.addMember(filterSettings);

		filterSettings.addMember(createInversionOptionFormField(filterModel));

		return new InversionOptionViewControl(filterModel, form);
	}

	private FormField createInversionOptionFormField(TableFilterModel filterModel) {
		BooleanField inversionOptionField =
			FormFactory.newBooleanField(INVERT_FIELD, filterModel.isInversionStateActive(), false);
		inversionOptionField.setLabel(Resources.getInstance().getString(I18NConstants.FILTER_INVERT));

		return inversionOptionField;
	}

	/**
	 *This method creates the document pattern for the button footer of the filter dialog
	 * 
	 * @return document pattern
	 */
	private Document getButtonsTemplate() {
		return BUTTON_TEMPLATE;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void closeFilterDialog() {
		dialogModel.setClosed();
	}
	
	private Command createClosingDialogApplyCommand(Command applyCommand, FormContext formContext) {
		return new ApplyCommandClosingDialog(applyCommand, formContext);
	}

	private Command createClosingDialogShowAllCommand(Command showAllCommand) {
		return new ShowAllCommandClosingDialog(showAllCommand);
	}

	private final class ApplyCommandClosingDialog implements Command {
		private final Command applyCommand;
		private final FormContext formContext;

		private ApplyCommandClosingDialog(Command applyCommand, FormContext formContext) {
			this.applyCommand = applyCommand;
			this.formContext = formContext;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			if (allFilterValuesValid()) {
				applyCommand.executeCommand(context);
				closeFilterDialog();
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		private boolean allFilterValuesValid() {
			formContext.checkAll();
			return !formContext.hasError();
		}
	}

	private final class ShowAllCommandClosingDialog implements Command {
		private final Command showAllCommand;

		private ShowAllCommandClosingDialog(Command showAllCommand) {
			this.showAllCommand = showAllCommand;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			showAllCommand.executeCommand(context);
			closeFilterDialog();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * This class is a renderer for contents of subfilters
	 * 
	 * @author    <a href=mailto:sts@top-logic.com>sts</a>
	 */
	private static class SubFilterContentRenderer extends AbstractSimpleCompositeControlRenderer {
		
		private final static Map<String, String> attributes = new HashMap<>();
		static {
			attributes.put(CLASS_ATTR, FILTER_CONTENT_AREA_CSS_CLASS);
		}
		
		final private static SubFilterContentRenderer INSTANCE = new SubFilterContentRenderer();
		
		private SubFilterContentRenderer() {
			super(DIV, attributes);
		}
		
		/** 
		 * {@inheritDoc}
		 */
		@Override
		public void writeChildren(DisplayContext context, TagWriter out,
				List<? extends HTMLFragment> views) throws IOException {
			
			int hrAmountThreshold = views.size() - 1;
			for (int i = 0, size = views.size(); i < size; i++) {
				views.get(i).write(context, out);
				if(i < hrAmountThreshold) {
					out.emptyTag(HTMLConstants.HR);
				}
			}
		}
	}
}
