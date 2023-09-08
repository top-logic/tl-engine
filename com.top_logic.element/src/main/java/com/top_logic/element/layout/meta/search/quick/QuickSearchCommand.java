/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search.quick;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.provider.ProxyResourceProvider;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.GenericTableDataOwner;
import com.top_logic.layout.table.GenericTableDataOwner.FormMemberAlgorithm;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Search command to the quick full text search.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class QuickSearchCommand extends AbstractSearchCommand {

	/**
	 * Search result entry constant to indicate that no results were found.
	 */
	public static final Object NO_RESULTS_CHANGE_SEARCH = new NamedConstant("noResultsChangeSearch");

	/**
	 * Static configuration of the {@link QuickSearchCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class QuickSearchConfig extends SearchConfig {

		private AttributedSearchComponent _searchComponent;

		/**
		 * The component to display when to much results were found. May be <code>null</code>.
		 */
		public AttributedSearchComponent getSearchComponent() {
			return _searchComponent;
		}

		/**
		 * Setter for {@link #getSearchComponent()}.
		 */
		public void setSearchComponent(AttributedSearchComponent searchComponent) {
			_searchComponent = searchComponent;

		}

	}

	/**
	 * Configuration of the {@link QuickSearchCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Configuration name for the value {@link #getExcludeTypes()}.
		 */
		String EXCLUDE_TYPES = "exclude-types";

		/**
		 * Configuration name for the value {@link #getIncludeTypes()}.
		 */
		String INCLUDE_TYPES = "include-types";

		/**
		 * Configuration name for the value {@link #getSearchAttributes()}.
		 */
		String SEARCH_ATTRIBUTES = "search-attributes";

		/**
		 * List of full qualified {@link TLType} (if {@link TLClass} including subtypes) which an
		 * object must have to occur in search result.
		 * 
		 * @see #getExcludeTypes()
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(INCLUDE_TYPES)
		List<String> getIncludeTypes();

		/**
		 * List of full qualified {@link TLType} (if {@link TLClass} including subtypes) which an
		 * object must not have to occur in search result.
		 * 
		 * <p>
		 * {@link #getExcludeTypes()} win against {@link #getIncludeTypes()}.
		 * </p>
		 * 
		 * @see #getIncludeTypes()
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(EXCLUDE_TYPES)
		List<String> getExcludeTypes();

		/**
		 * Map of qualified class names and their configured attributes that should be used for the
		 * search exclusively.
		 */
		@Key(SearchTypeConfig.TYPE)
		@Name(SEARCH_ATTRIBUTES)
		Map<String, SearchTypeConfig> getSearchAttributes();

	}

	/**
	 * Configuration for {@link QuickSearchCommand.Config#getSearchAttributes()}.
	 *
	 * @author <a href=mailto:Diana.Pankratz@top-logic.com>Diana Pankratz</a>
	 */
	public interface SearchTypeConfig extends ConfigurationItem {

		/**
		 * Configuration name for the value {@link #getType()}.
		 */
		String TYPE = "type";

		/**
		 * Configuration name for the value {@link #getAttributes()}.
		 */
		String ATTRIBUTES = "attributes";

		/**
		 * Type of the {@link TLClass}.
		 */
		@Name(TYPE)
		String getType();

		/**
		 * List of attributes that should be used for the search of the given {@link #getType()}.
		 */
		@Format(CommaSeparatedStrings.class)
		@Name(ATTRIBUTES)
		List<String> getAttributes();
	}

	private final AbstractFormMemberControl _input;

	/**
	 * Creates a {@link QuickSearchCommand}.
	 */
	public QuickSearchCommand(AbstractFormMemberControl input, QuickSearchConfig searchConfig) {
		super(() -> ((StringField) input.getModel()).getAsString(), searchConfig);
		_input = input;
	}

	/**
	 * Sorts the list of search results by their label from a to z.
	 * 
	 * @param searchResult
	 *        List of found objects from the full text object search.
	 */
	@Override
	protected void sortResult(List<Object> searchResult) {
		Collections.sort(searchResult, LabelComparator.newInstance(MetaResourceProvider.INSTANCE));
	}

	/**
	 * Creates the popup dialog.
	 * 
	 * @param aContext
	 *        The context we act in.
	 * @param searchResult
	 *        The searched objects to be displayed.
	 * @return {@link HandlerResult#DEFAULT_RESULT} in this implementation.
	 */
	@Override
	protected HandlerResult displayResult(DisplayContext aContext, List<Object> searchResult) {
		if (searchResult.isEmpty()) {
			/* When no results are found, display a table with the option to change the search in
			 * the search component. */
			searchResult = Collections.singletonList(NO_RESULTS_CHANGE_SEARCH);
		}
		PopupDialogControl dialog = createPopupDialog(aContext, searchResult);
		aContext.getWindowScope().openPopupDialog(dialog);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Return the requested dialog control.
	 * 
	 * @param aContext
	 *        The context we act in, must not be <code>null</code>.
	 * @param searchResult
	 *        The searched objects to be displayed, must not be <code>null</code>.
	 * @return The requested control, never <code>null</code>.
	 */
	public PopupDialogControl createPopupDialog(DisplayContext aContext, List<Object> searchResult) {
		PopupDialogModel dialogModel = createDialogModel(searchResult.size());
		PopupDialogControl popupDialog =
			new PopupDialogControl(_input.getFrameScope(), dialogModel, _input.getInputId());

		popupDialog.setContent(getResultControl(aContext, dialogModel, searchResult));

		return popupDialog;
	}

	/**
	 * Return the inner control displaying the given searched objects (a table control in this
	 * implementation).
	 * 
	 * @param aContext
	 *        The context we act in, must not be <code>null</code>.
	 * @param dialogModel
	 *        The model of the popup dialog.
	 * @param searchResult
	 *        The objects to be displayed, must not be <code>null</code>.
	 * @return The requested control, never <code>null</code>.
	 */
	protected Control getResultControl(DisplayContext aContext, PopupDialogModel dialogModel,
			List<Object> searchResult) {
		TableModel tableModel = getTableModel(searchResult);
		FormMember reference = _input.getModel();
		GenericTableDataOwner owner = new GenericTableDataOwner(reference, FormMemberAlgorithm.INSTANCE);
		TableData table = DefaultTableData.createTableData(owner, tableModel, ConfigKey.none());
		customizeTableModel(aContext, dialogModel, table);
		TableControl tableControl = new TableControl(table, DefaultTableRenderer.newInstance());
		tableControl.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					FormMemberAlgorithm.INSTANCE.annotate(reference, table);
				} else {
					FormMemberAlgorithm.INSTANCE.resetAnnotation(reference);
				}
			}
		});
		return tableControl;
	}

	/**
	 * Append the goto handler as selection handler to the given table data.
	 * 
	 * @param aContext
	 *        The context we are working in.
	 * @param dialogModel
	 *        The popup model to be closed on click.
	 * @param table
	 *        The data to get the view model from.
	 */
	protected void customizeTableModel(DisplayContext aContext, PopupDialogModel dialogModel, TableData table) {
		TableViewModel theViewModel = table.getViewModel();
		SelectionListener theListener = getGotoHandling(aContext, theViewModel, dialogModel);

		table.getSelectionModel().addSelectionListener(theListener);
	}

	/**
	 * Provide the goto handling for the dialog content.
	 * 
	 * @param aContext
	 *        The context we are working in.
	 * @param tableViewModel
	 *        The view model to get the requested object from.
	 * @param aDialogModel
	 *        The popup model to be closed.
	 * @return The requested selection listener.
	 */
	protected SelectionListener getGotoHandling(final DisplayContext aContext, final TableViewModel tableViewModel,
			final PopupDialogModel aDialogModel) {
		return new SelectionListener() {

			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {

				if (!CollectionUtils.isEmpty(newSelection)) {
					Object theObject = CollectionUtil.getFirst(newSelection);

					if (!AbstractSearchCommand.MORE_RESULTS_REFINE_SEARCH.equals(theObject)
							&& !NO_RESULTS_CHANGE_SEARCH.equals(theObject)) {
						aDialogModel.setClosed();

						MainLayout layout = TLContext.getContext().getLayoutContext().getMainLayout();
						LayoutComponent gotoTargetForObject = gotoTargetForObject(theObject);
						ComponentName targetName;
						if (gotoTargetForObject != null) {
							targetName = gotoTargetForObject.getName();
						} else {
							targetName = null;
						}
						gotoHandler().executeGoto(aContext, layout, targetName, theObject);
					} else {
						// More results.
						AttributedSearchComponent searchComponent =
							((QuickSearchConfig) getSearchConfig()).getSearchComponent();
						if (searchComponent != null) {
							aDialogModel.setClosed();
							// Display search component.
							searchComponent.makeVisible();
							// Remove eventually set stored query
							searchComponent.setModel(null);
							// Remove former FormContext to have fresh input
							searchComponent.removeFormContext();
							// Force redraw.
							searchComponent.invalidate();
							// trigger creation of FormContext.
							searchComponent.getFormContext();
							// fill search field
							FormField fulltextField = searchComponent.getFullTextField();
							if (fulltextField != null) {
								fulltextField.setValue(getSearch());
							}
						}
					}
				}
			}
		};
	}

	/**
	 * Return the table model for the given objects.
	 * 
	 * @param searchResult
	 *        The searched objects to be displayed. May contain
	 *        {@link AbstractSearchCommand#MORE_RESULTS_REFINE_SEARCH} as a hint for more results,
	 *        or {@link #NO_RESULTS_CHANGE_SEARCH} if there are no results.
	 * @return The requested table model.
	 * 
	 * @see AbstractSearchCommand#applySecurityWithLimit(List)
	 */
	protected TableModel getTableModel(List<Object> searchResult) {
		TableConfiguration theConfig = TableConfigurationFactory.table();
		ColumnConfiguration theColumn = theConfig.declareColumn(Wrapper.NAME_ATTRIBUTE);

		theConfig.setShowTitle(false);
		theConfig.setShowColumnHeader(false);
		theConfig.setShowFooter(false);
		theColumn.setAccessor(IdentityAccessor.INSTANCE);
		theColumn.setResourceProvider(new ProxyResourceProvider() {

			@Override
			public String getLabel(Object anObject) {
				if (AbstractSearchCommand.MORE_RESULTS_REFINE_SEARCH.equals(anObject)) {
					return Resources.getInstance().getString(I18NConstants.MORE_RESULTS);
				} else if (NO_RESULTS_CHANGE_SEARCH.equals(anObject)) {
					return Resources.getInstance().getString(I18NConstants.NO_RESULTS);
				} else {
					return super.getLabel(anObject);
				}
			}

			@Override
			public String getLink(DisplayContext aContext, Object aObject) {
				return null;
			}

		});

		return new ObjectTableModel(Arrays.asList(Wrapper.NAME_ATTRIBUTE), theConfig, searchResult);
	}

	/**
	 * Return the layout model for the popup dialog (till 8 entries, all will be displayed, more
	 * than 8 will create a scroll bar).
	 * 
	 * @param resultCount
	 *        The number of hits to be displayed.
	 * @return The requested layout model.
	 */
	protected PopupDialogModel createDialogModel(int resultCount) {
		return new DefaultPopupDialogModel(
			new DefaultLayoutData(350, DisplayUnit.PIXEL, 100,
				(resultCount < 8) ? 0 : 200, DisplayUnit.PIXEL, 100,
				Scrolling.AUTO));
	}

	GotoHandler gotoHandler() {
		return (GotoHandler) CommandHandlerFactory.getInstance().getHandler(GotoHandler.COMMAND);
	}

	/**
	 * {@link Config} defined in the application configuration.
	 */
	public static Config quickSearchConfig() {
		ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
		return applicationConfig.getConfig(Config.class);
	}

}
