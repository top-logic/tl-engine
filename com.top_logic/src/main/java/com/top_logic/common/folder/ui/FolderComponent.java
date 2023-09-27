/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.FolderTreeBuilder;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.Control;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.folder.FolderDataOwner;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DirectFormDisplay;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueListener;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProvider;
import com.top_logic.mig.html.layout.decoratedTabBar.DecorationValueProviderDelegate;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class FolderComponent extends BuilderComponent implements FormHandler, FolderDataOwner,
		DecorationValueProvider, ControlRepresentable {

	/** {@link ConfigurationItem} for the {@link FolderComponent}. */
	public interface Config extends BuilderComponent.Config, UIOptions {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Property name of {@link #getTreeBuilder()}. */
		String TREE_BUILDER = "treeBuilder";

		/**
		 * The columns to display.
		 */
		@Name(XML_CONF_KEY_COLUMNS)
		String getColumns();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/** The {@link TreeBuilder} is used to create a tree of {@link FolderNode} objects. */
		@Name(TREE_BUILDER)
		@NonNullable
		@ItemDefault
		@ImplementationClassDefault(FolderTreeBuilder.class)
		PolymorphicConfiguration<TreeBuilder<FolderNode>> getTreeBuilder();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			BuilderComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(SimpleBoundCommandGroup.CREATE);
			additionalGroups.add(SimpleBoundCommandGroup.WRITE);
			additionalGroups.add(SimpleBoundCommandGroup.DELETE);
		}

		@Override
		@ComplexDefault(NoFolderAvailableDefault.class)
		ResKey getNoModelKey();

		/**
		 * {@link DefaultValueProviderShared} returning {@link I18NConstants#NO_FOLDER_AVAILABLE}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public static class NoFolderAvailableDefault extends DefaultValueProviderShared {

			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return I18NConstants.NO_FOLDER_AVAILABLE;
			}

		}

		// Toolbars are not yet combined.
		//
//		@Override
//		@BooleanDefault(true)
//		boolean hasToolbar();
	}

	/**
	 * In-app customizable options of a {@link FolderComponent}.
	 */
	public interface UIOptions extends ConfigurationItem {

		/** Configuration name for {@link #getHasNewFolderCommand()}. */
		String HAS_NEW_FOLDER_COMMAND = "hasNewFolderCommand";

		/** Configuration name for {@link #getHasZipDownloadCommand()}. */
		String HAS_ZIP_DOWNLOAD_COMMAND = "hasZipDownloadCommand";

		/** Configuration name for {@link #getHasClipboardCommand()}. */
		String HAS_CLIPBOARD_COMMAND = "hasClipboardCommand";

		/**
		 * @see #analyzeDocuments()
		 */
		String ANALYZE_DOCUMENTS = "analyzeDocuments";

		/**
		 * @see #updateDocuments()
		 */
		String UPDATE_DOCUMENTS = "updateDocuments";

		/**
		 * @see #deleteDocuments()
		 */
		String DELETE_DOCUMENTS = "deleteDocuments";

		/**
		 * Whether a button is displayed that allow to create sub-folders.
		 */
		@Name(HAS_NEW_FOLDER_COMMAND)
		@Label("Sub-folders")
		boolean getHasNewFolderCommand();

		/**
		 * Whether the folder can be downloaded as ZIP archive.
		 */
		@Name(HAS_ZIP_DOWNLOAD_COMMAND)
		@Label("ZIP download")
		boolean getHasZipDownloadCommand();

		/**
		 * Whether clip-board management buttons for documents should be available.
		 */
		@Name(HAS_CLIPBOARD_COMMAND)
		@Label("Use clipboard")
		boolean getHasClipboardCommand();

		/**
		 * Whether a document can be updated with a new version.
		 * 
		 * <p>
		 * For a user to modify a document, additional permissions might be required.
		 * </p>
		 */
		@Name(UPDATE_DOCUMENTS)
		@BooleanDefault(true)
		boolean updateDocuments();

		/**
		 * Whether a document can be deleted.
		 * 
		 * <p>
		 * For a user to delete a document, additional permissions might be required.
		 * </p>
		 */
		@Name(DELETE_DOCUMENTS)
		@BooleanDefault(true)
		boolean deleteDocuments();

		/**
		 * Whether documents should be indexed and compared by keywords.
		 * 
		 * <p>
		 * The document analysis service hast to be configured also to provide this functionality.
		 * </p>
		 * 
		 * @see DefaultAnalyzeService#isAvailable()
		 */
		@Name(ANALYZE_DOCUMENTS)
		@BooleanDefault(true)
		boolean analyzeDocuments();
	}

	public static final String XML_CONF_KEY_COLUMNS = "columns";

	private final TreeBuilder<FolderNode> _treeBuilder;

	private FormContext _formContext;

	private FolderData _data;

	private final String[] _columns;

	private final DecorationValueProviderDelegate _decorationValueProviderDelegate =
		new DecorationValueProviderDelegate();

	private final boolean _hasClipboardCommand;

	private final boolean _hasZipDownloadCommand;

	private final boolean _hasNewFolderCommand;

	/** {@link TypedConfiguration} constructor for {@link FolderComponent}. */
	public FolderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_treeBuilder = context.getInstance(config.getTreeBuilder());
		_columns = StringServices.toArray(StringServices.nonEmpty(config.getColumns()), ',');
		_hasClipboardCommand = config.getHasClipboardCommand();
		_hasNewFolderCommand = config.getHasNewFolderCommand();
		_hasZipDownloadCommand = config.getHasZipDownloadCommand();
	}

	@Override
	public final FormContext getFormContext() {
		if (_formContext == null) {
			FormContext newFormContext = createFormContext();
			if (newFormContext != null) {
				FormComponent.initFormContext(this, this, newFormContext);
				_formContext = newFormContext;
			}
		}

		return _formContext;
	}

	@Override
	public boolean hasFormContext() {
		return _formContext != null;
	}

	@Override
	public Command getApplyClosure() {
		return null;
	}

	@Override
	public Command getDiscardClosure() {
		return null;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		_formContext = null;
		_data = null;
	}

	@Override
	protected boolean isChangeHandlingDefault() {
		return false;
	}

	/**
	 * Creates the {@link FormContext} for the webfolder display.
	 * 
	 * @return The newly created {@link FormContext}, or <code>null</code> when this component has
	 *         no {@link FolderDefinition}.
	 * 
	 * @see FormComponent#createFormContext()
	 * @see #getFolderDefinition()
	 */
	protected FormContext createFormContext() {
		FolderDefinition folder = getFolderDefinition();
		if (folder == null) {
			return null;
		}
		FormContext newFormContext = new FormContext(this);

		ExecutableState canAddToClipboard = getClipability(folder);
		ExecutableState canCreate;
		ExecutableState canUpdate;
		ExecutableState canDelete;

		ExecutableState modifyability = getModifyability(folder);
		if (modifyability.isExecutable()) {
			canCreate = permissionExecutability(this.allow(SimpleBoundCommandGroup.CREATE));
			canUpdate = permissionExecutability(this.allow(SimpleBoundCommandGroup.WRITE));
			canDelete = permissionExecutability(this.allow(SimpleBoundCommandGroup.DELETE));
		} else {
			// Must not try to modify historic object independent of security.
			canCreate = canUpdate = canDelete = modifyability;
		}

		if (!((Config) getConfig()).deleteDocuments()) {
			canDelete = ExecutableState.NOT_EXEC_HIDDEN;
		}

		if (!((Config) getConfig()).updateDocuments()) {
			canUpdate = ExecutableState.NOT_EXEC_HIDDEN;
		}

		TableConfiguration columnDescriptions = getTableConfiguration(canAddToClipboard, canUpdate, canDelete);

		_data = createFolderData(newFormContext, folder, getTreeBuilder(), columnDescriptions);

		SingleSelectionModel selectionModel = _data.getSingleSelectionModel();
		if (hasClipboardCommand()) {
			WebFolderUIFactory.addClipboardCommand(newFormContext, selectionModel, canCreate);
		}
		if (hasAddNewFolderCommand()) {
			WebFolderUIFactory.addNewFolderCommand(newFormContext, selectionModel, canCreate);
		}
		UploadExecutor executer = getUploadExecuter(_data);
		if (executer != null) {
			WebFolderUIFactory.addUploadCommand(newFormContext, selectionModel, canCreate, executer);
		}
		if (hasZipDownloadCommand()) {
			ExecutableState canRead = permissionExecutability(this.allow(SimpleBoundCommandGroup.READ));
			WebFolderUIFactory.addZipDownloadFolderCommand(newFormContext, selectionModel, canRead);
		}
		return newFormContext;
	}

	public BreadcrumbRenderer getBreadcrumbRenderer() {
		return WebFolderUIFactory.getInstance().createBreadcrumbRenderer(getRootLabel());
	}

	/**
	 * The label for the root node.
	 * <p>
	 * The label is used for the root node of the breadcrumb.
	 * </p>
	 */
	protected String getRootLabel() {
		return WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES.getStringResource("firstNode");
	}

	protected void resetFormContext() {
		_formContext = null;
	}

	protected TreeBuilder<FolderNode> getTreeBuilder() {
		return _treeBuilder;
	}

	protected boolean hasAddNewFolderCommand() {
		return _hasNewFolderCommand;
	}

	protected boolean hasZipDownloadCommand() {
		return _hasZipDownloadCommand;
	}

	protected boolean hasClipboardCommand() {
		return _hasClipboardCommand;
	}

	private FolderData createFolderData(FormContext newFormContext, Object rootUserObject,
			TreeBuilder<FolderNode> treeBuilder, TableConfiguration columnDescriptions) {
		return WebFolderUIFactory.createFolderData(this, rootUserObject, treeBuilder, columnDescriptions,
			newFormContext, getColumns(), ConfigKey.component(this));
	}

	/**
	 * The currently displayed (selected) sub-folder.
	 */
	protected final SingleSelectionModel getFolderSelection() {
		return getFolderData().getSingleSelectionModel();
	}

	@Override
	public FolderData getFolderData() {
		return _data;
	}

	private static ExecutableState permissionExecutability(boolean allow) {
		return allow ? ExecutableState.EXECUTABLE : ExecutableState.NO_EXEC_PERMISSION;
	}

	protected String[] getColumns(){
		if (_columns != null) {
			return _columns;
		}

		return getDefaultColumns();
	}

	/**
	 * The default columns to use, if not explicitly configured.
	 */
	protected String[] getDefaultColumns() {
		Config config = (Config) getConfig();
		boolean withUpdate = config.updateDocuments();
		boolean withDelete = config.deleteDocuments();
		boolean withClipboard = config.getHasClipboardCommand();
		boolean withAnalyze = config.analyzeDocuments() && DefaultAnalyzeService.isAvailable();

		return WebFolderUIFactory.defaultColumns(withUpdate, withDelete, withClipboard, withAnalyze);
	}

	@Override
	public String[] getTabBarDecorationValues() {
		return new String[] { getFolderSize() };
	}

	@Override
	public void registerDecorationValueListener(DecorationValueListener aListener) {
		_decorationValueProviderDelegate.registerDecorationValueListener(aListener);
	}

	@Override
	public boolean unregisterDecorationValueListener(DecorationValueListener aListener) {
		return _decorationValueProviderDelegate.unregisterDecorationValueListener(aListener);
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		_decorationValueProviderDelegate.fireDecorationValueChanged(this);
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object aSomeChangedBy) {
		if (isSupported(aModel)) {
			if (getFolderData() != null) {
				if (aModel instanceof DocumentVersion) {
					aModel = WrapperHistoryUtils.getCurrent(((DocumentVersion) aModel).getDocument());
				}
				return getFolderData().updateUserObject(aModel);
			}

			// ****** IMPORTANT *******
			// Must be done AFTER previous if statement (update of user object), due to possible
			// detachment of underlying folder data within the following event propagation,
			// which leads to unprocessed updates.
			if (aModel == this.getFolderDefinition()) {
				// Event only necessary, if direct contents of the displayed root folder has been
				// changed.
				_decorationValueProviderDelegate.fireDecorationValueChanged(this);
			}
		}


		return false;
	}

	protected abstract boolean isSupported(Object aModel);

	protected abstract String getFolderSize();

	protected abstract ExecutableState getModifyability(FolderDefinition folder);

	protected abstract ExecutableState getClipability(FolderDefinition folder);

	/**
	 * Determines the {@link FolderDefinition} for this component.
	 * 
	 * @return {@link FolderDefinition} to display, or <code>null</code>. In this case
	 *         {@link #getFormContext()} is also <code>null</code>.
	 */
	protected abstract FolderDefinition getFolderDefinition();

	/**
	 * @param folderData
	 *        the displayed {@link FolderData}
	 * @return the {@link UploadExecutor} used to perform uploads. May be <code>null</code> when no
	 *         uploads must be performed.
	 */
	protected abstract UploadExecutor getUploadExecuter(FolderData folderData);

	protected abstract TableConfiguration getTableConfiguration(ExecutableState canAddToClipboard,
			ExecutableState canUpdate,
			ExecutableState canDelete);

	@Override
	public final Control getRenderingControl() {
		FormContext formContext = getFormContext();
		if (formContext == null) { 
			return createControlWithoutFolder();
		} else {
			return createControlForContext(formContext);
		}
	}

	/**
	 * Creates a {@link Control} displaying the given non-null {@link FormContext}.
	 */
	protected abstract Control createControlForContext(FormContext context);

	/**
	 * Creates the control to display when no {@link FormContext} is available.
	 */
	protected Control createControlWithoutFolder() {
		DirectFormDisplay.Config formDisplayConfig = TypedConfiguration.newConfigItem(DirectFormDisplay.Config.class);
		HTMLFragment noModelHTML = new DirectFormDisplay(formDisplayConfig).createView(this, null, noModelKey());
		return new FragmentControl(noModelHTML);
	}

}
