/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.clipboard.ClipboardExecutable;
import com.top_logic.common.webfolder.ui.clipboard.Icons;
import com.top_logic.common.webfolder.ui.commands.NewFolderExecutable;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.common.webfolder.ui.commands.UploadExecutable;
import com.top_logic.common.webfolder.ui.commands.ZipDownloadExecutable;
import com.top_logic.common.webfolder.ui.commands.ZipDownloadLabelProvider;
import com.top_logic.common.webfolder.ui.commands.ZipFolderNameProvider;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Control;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.folder.FileDropHandler;
import com.top_logic.layout.folder.FolderControl;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.folder.FolderDataOwner;
import com.top_logic.layout.folder.FolderFileDropHandler;
import com.top_logic.layout.folder.FolderTableRenderer;
import com.top_logic.layout.folder.NoFileDrop;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbContentRenderer;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.DefaultBreadcrumbRenderer;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * The WebFolderUIFactory provides methods to create stuff for displaying a WebFolder
 * {@link WebFolder}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderUIFactory extends ConfiguredManagedClass<WebFolderUIFactory.Config> {

	/** {@link ConfigurationItem} for the {@link WebFolderUIFactory}. */
	public interface Config extends ConfiguredManagedClass.Config<WebFolderUIFactory> {

		/** Property name of {@link #getProvideZipDownload()}. */
		String PROVIDE_ZIP_DOWNLOAD = "provide-zip-download";

		/** Property name of {@link #getZipDownloadFileNameProvider()}. */
		String ZIP_DOWNLOAD_FILE_NAME_PROVIDER = "zip-download-file-name-provider";

		/** Property name of {@link #getZipDownloadSizeLimit()}. */
		String ZIP_DOWNLOAD_SIZE_LIMIT = "zip-download-size-limit";

		/** Property name of {@link #getZipFolderNameProvider()}. */
		String ZIP_FOLDER_NAME_PROVIDER = "zip-folder-name-provider";

		/** Property name of {@link #getBreadcrumbRenderer()}. */
		String BREADCRUMB_RENDERER = "breadcrumb-renderer";

		/** Property name of {@link #getManualLocking()}. */
		String MANUAL_LOCKING = "manual-locking";

		/** Configuration name for {@link #getMaxUploadSize()}. */
		String MAX_UPLOAD_SIZE = "max-upload-size";

		/** @see WebFolderUIFactory#getProvideZipDownload() */
		@BooleanDefault(true)
		@Name(PROVIDE_ZIP_DOWNLOAD)
		boolean getProvideZipDownload();

		/** @see WebFolderUIFactory#getZipDownloadFileNameProvider() */
		@Name(ZIP_DOWNLOAD_FILE_NAME_PROVIDER)
		@ItemDefault
		@ImplementationClassDefault(ZipDownloadLabelProvider.class)
		PolymorphicConfiguration<LabelProvider> getZipDownloadFileNameProvider();

		/** @see WebFolderUIFactory#getZipDownloadSizeLimit() */
		@LongDefault(1000)
		@Name(ZIP_DOWNLOAD_SIZE_LIMIT)
		long getZipDownloadSizeLimit();

		/** @see WebFolderUIFactory#getZipFolderNameProvider() */
		@Name(ZIP_FOLDER_NAME_PROVIDER)
		@ItemDefault
		@ImplementationClassDefault(ZipFolderNameProvider.class)
		PolymorphicConfiguration<LabelProvider> getZipFolderNameProvider();

		/** The {@link BreadcrumbRenderer} that will render the breadcrumb of web-folders. */
		@Name(BREADCRUMB_RENDERER)
		@ItemDefault
		@ImplementationClassDefault(DefaultBreadcrumbRenderer.class)
		PolymorphicConfiguration<BreadcrumbRenderer> getBreadcrumbRenderer();

		/** @see WebFolderUIFactory#getManualLocking() */
		@Name(MANUAL_LOCKING)
		boolean getManualLocking();

		/**
		 * Maximum size of a single file that can be uploaded. A value of <code>0</code> means that
		 * there is no limit.
		 */
		@LongDefault(0)
		@Format(MemorySizeFormat.class)
		@Name(MAX_UPLOAD_SIZE)
		long getMaxUploadSize();

	}

	private final boolean _provideZipDownload;

	private final LabelProvider _zipDownloadFileNameProvider;

	private final long _zipDownloadSizeLimit;

	private final LabelProvider _zipFolderNameProvider;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link WebFolderUIFactory}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public WebFolderUIFactory(InstantiationContext context, Config config) {
		super(context, config);
		_provideZipDownload = config.getProvideZipDownload();
		_zipDownloadFileNameProvider = requireNonNull(context.getInstance(config.getZipDownloadFileNameProvider()));
		_zipDownloadSizeLimit = config.getZipDownloadSizeLimit();
		_zipFolderNameProvider = requireNonNull(context.getInstance(config.getZipFolderNameProvider()));
	}

	/**
	 * Whether the a folder can be downloaded as a single zip file.
	 */
	public boolean getProvideZipDownload() {
		return _provideZipDownload;
	}

	/**
	 * The label provider configured for the zip download file name.
	 */
	public LabelProvider getZipDownloadFileNameProvider() {
		return _zipDownloadFileNameProvider;
	}

	/**
	 * Disable the zip download of a folder when the folder is larger than this amount of MiBi.
	 */
	public long getZipDownloadSizeLimit() {
		return _zipDownloadSizeLimit;
	}

	/**
	 * The provider for names of folders, as they are displayed inside a zip file.
	 */
	public LabelProvider getZipFolderNameProvider() {
		return _zipFolderNameProvider;
	}

	/** Command field for adding objects from the clipboard. */
    public static final String CLIPBOARD = "_fromClipboard";
    
    /** Command field for uploading a document. */
    public static final String NEW_FOLDER = "_newFolder";
    
	/** Command field for zip download the folder. */
	public static final String ZIP_DOWNLOAD_FOLDER = "_zipDownloadFolder";

    /** Command field for uploading a document. */
    public static final String UPLOAD_DOCUMENT = "_upload";

	/**
	 * Creates a {@link CommandField} based on a {@link ClipboardExecutable}
	 * 
	 * @param container
	 *        the container to add new {@link CommandField}
	 * @param selectionModel
	 *        the selectionModel dispatched to
	 *        {@link ClipboardExecutable#ClipboardExecutable(SingleSelectionModel)}
	 * @param canCreate
	 *        whether the command should be executable
	 */
	public static CommandField addClipboardCommand(FormContainer container, SingleSelectionModel selectionModel,
			ExecutableState canCreate) {
		CommandField theField;

		if (container.hasMember(CLIPBOARD)) {
            theField = (CommandField) container.getMember(CLIPBOARD);
        }
        else {
			ClipboardExecutable theExecutable = new ClipboardExecutable(selectionModel);

            theField = WebFolderFieldProvider.createField(CLIPBOARD, theExecutable, Icons.CLIPBOARD, Icons.CLIPBOARD_DISABLED);
			NotExecutableListener.createNotExecutableListener(I18NConstants.FIELD_DISABLED, theField).addAsListener(
				container);

            container.addMember(theField);
        }

		CommandModelUtilities.applyExecutability(canCreate, theField);

        return theField;
	}
    
	/**
	 * Creates a {@link CommandField} based on a {@link NewFolderExecutable}
	 * 
	 * @param container
	 *        the container to add new {@link CommandField}
	 * @param selectionModel
	 *        the selectionModel dispatched to
	 *        {@link NewFolderExecutable#NewFolderExecutable(SingleSelectionModel)}
	 * @param canCreate
	 *        whether the command should be executable
	 */
	public static CommandField addNewFolderCommand(FormContainer container, SingleSelectionModel selectionModel,
			ExecutableState canCreate) {
		CommandField theField;
        
        if (container.hasMember(NEW_FOLDER)) {
            theField = (CommandField) container.getMember(NEW_FOLDER);
        }
        else {
			NewFolderExecutable theExecutable = new NewFolderExecutable(selectionModel);
            
			theField = WebFolderFieldProvider.createField(NEW_FOLDER, theExecutable,
				com.top_logic.common.webfolder.ui.commands.Icons.NEW_FOLDER,
				com.top_logic.common.webfolder.ui.commands.Icons.NEW_FOLDER_DISABLED);
			NotExecutableListener.createNotExecutableListener(I18NConstants.FIELD_DISABLED, theField).addAsListener(
				container);
            
            container.addMember(theField);
        }

		CommandModelUtilities.applyExecutability(canCreate, theField);

        return theField;
	}

	public static CommandField addZipDownloadFolderCommand(FormContainer container,
			SingleSelectionModel selectionModel,
			ExecutableState executability) {
		CommandField theField;

		if (WebFolderUIFactory.getInstance().getProvideZipDownload()) {
			if (container.hasMember(ZIP_DOWNLOAD_FOLDER)) {
				theField = (CommandField) container.getMember(ZIP_DOWNLOAD_FOLDER);
			} else {
				theField =
					createZipDownloadField(selectionModel, ZIP_DOWNLOAD_FOLDER,
						com.top_logic.common.webfolder.ui.commands.Icons.DOWNLOAD_FOLDER,
						com.top_logic.common.webfolder.ui.commands.Icons.DOWNLOAD_FOLDER_DISABLED);

				container.addMember(theField);
			}
			CommandModelUtilities.applyExecutability(executability, theField);
			return theField;
		} else {
			return null;
		}
	}

	/**
	 * Creates a {@link CommandField} which enables a ZIP download of the folder selected in by the
	 * given {@link SingleSelectionModel}.
	 * 
	 * @param selectionModel
	 *        The model which determines the folder to download as ZIP file.
	 * @param fieldName
	 *        The name of the field to create.
	 * @param executableImage
	 *        A {@link ThemeImage} to display, when download is possible.
	 * @param disabledImage
	 *        A {@link ThemeImage} to display, when download is not possible.
	 */
	public static CommandField createZipDownloadField(SingleSelectionModel selectionModel, String fieldName,
			ThemeImage executableImage, ThemeImage disabledImage) {
		ZipDownloadExecutable executable = new ZipDownloadExecutable(selectionModel);
		CommandField field =
			WebFolderFieldProvider.createField(fieldName, executable, executableImage, disabledImage);
		field.setTooltip(Resources.getInstance().getString(I18NConstants.ZIP_DOWNLOAD_FOLDER_TOOLTIP));

		// When the corresponding form context is immutable it would be not
		// possible to execute the command. As download is essentially the
		// view of the document it must be also possible in that case.
		field.setInheritDeactivation(false);
		return field;
	}

	/**
	 * Creates a {@link CommandField} based on a {@link UploadExecutable}
	 * 
	 * @param container
	 *        the container to add new {@link CommandField}
	 * @param selectionModel
	 *        the selectionModel dispatched to
	 *        {@link UploadExecutable#UploadExecutable(SingleSelectionModel, UploadExecutor)}
	 * @param canCreate
	 *        whether the command should be executable
	 */
	public static CommandField addUploadCommand(FormContainer container, SingleSelectionModel selectionModel,
			ExecutableState canCreate, UploadExecutor executer) {
		CommandField theField;

        if (container.hasMember(UPLOAD_DOCUMENT)) {
            theField = (CommandField) container.getMember(UPLOAD_DOCUMENT);
        }
        else {
			UploadExecutable theExecutable = new UploadExecutable(selectionModel, executer);

			theField = WebFolderFieldProvider.createField(UPLOAD_DOCUMENT, theExecutable,
				com.top_logic.common.webfolder.ui.commands.Icons.UPLOAD,
				com.top_logic.common.webfolder.ui.commands.Icons.UPLOAD_DISABLED);
			NotExecutableListener.createNotExecutableListener(I18NConstants.FIELD_DISABLED, theField).addAsListener(
				container);

            container.addMember(theField);
        }

		CommandModelUtilities.applyExecutability(canCreate, theField);

        return theField;
	}

	/**
	 * Creates a {@link FolderData}.
	 * 
	 * @param folderColumns
	 *        the displayed columns in the table. If <code>null</code>, then the a default is used
	 * @param configKey
	 *        The key to store personal folder configuration with.
	 */
	public static FolderData createFolderData(FolderDataOwner owner, Object rootUserObject,
			TreeBuilder<FolderNode> treeBuilder, TableConfiguration tableConfiguration,
			FormContainer formContext, String[] folderColumns, ConfigKey configKey) {

		// setting no rows is okay, as the FolderData will compute and override them anyway.
		List<Object> rows = Collections.emptyList();
		ObjectTableModel folderTable =
			new ObjectTableModel(applyDefaultColumns(folderColumns), tableConfiguration, rows);
		EditableRowTableModel formTable = new FormTableModel(folderTable, formContext);
		return new FolderData(owner, rootUserObject, treeBuilder, formTable, configKey);
	}

	private static String[] applyDefaultColumns(String[] someColumns) {
		if (someColumns == null) {
			return defaultColumns();
		} else {
			return someColumns;
		}
	}

	/**
	 * Creates the full columns list.
	 */
	public static String[] defaultColumns() {
		boolean withAnalyze = DefaultAnalyzeService.isAvailable();
		boolean withClipboard = true;
		boolean withUpdate = true;
		boolean withDelete = true;
		return defaultColumns(withUpdate, withDelete, withClipboard, withAnalyze);
	}

	/**
	 * Creates a customized columns list.
	 */
	public static String[] defaultColumns(boolean withUpdate, boolean withDelete, boolean withClipboard,
			boolean withAnalyze) {
		if (withAnalyze && withClipboard && withDelete) {
			return WebFolderAccessor.DEFAULT_COLUMNS;
		}

		ArrayList<String> columns = CollectionUtil.toList(WebFolderAccessor.DEFAULT_COLUMNS);
		if (!withUpdate) {
			columns.remove(WebFolderAccessor.LOCK);
		}
		if (!withDelete) {
			columns.remove(WebFolderAccessor.DELETE);
		}
		if (!withClipboard) {
			columns.remove(WebFolderAccessor.CLIPBOARD);
		}
		if (!withAnalyze) {
			columns.remove(WebFolderAccessor.SIMILAR_DOCUMENTS);
			columns.remove(WebFolderAccessor.KEYWORDS);
		}
		return columns.toArray(new String[columns.size()]);
	}

	/**
	 * Creates a {@link Control} which displays the given data.
	 * 
	 * @param data
	 *        the {@link FolderData} containing most necessary information to
	 *        create control (e.g. the displayed {@link WebFolder}).
	 * @param aContext
	 *        a form to add commands to, esp. create folder, upload file, and
	 *        add from clipboard.
	 * @param fileDropHandler
	 *        A {@link FileDropHandler} for uploads of dropped files/folders. If the
	 *        {@link FileDropHandler} is <code>null</code>, an instance of {@link NoFileDrop} will
	 *        be created. In this case dropping files/folders into the {@link FolderControl} is not
	 *        supported. If the user is dragging a file/folder over a {@link FolderControl} with
	 *        {@link NoFileDrop} he will receive the information that the upload is not possible
	 *        before actually dropping it.
	 * 
	 * @return a control representing the folder of the given data on the client
	 */
	public static FolderControl createControl(BreadcrumbRenderer breadcrumbRenderer, FolderData data,
			FormContainer aContext, FileDropHandler fileDropHandler) {
		FolderControl theControl = 
			new FolderControl(data, breadcrumbRenderer, createTableRenderer(data), fileDropHandler);
		addButtonControl(theControl, aContext, CLIPBOARD);
		addButtonControl(theControl, aContext, NEW_FOLDER);
		addButtonControl(theControl, aContext, UPLOAD_DOCUMENT);
		addButtonControl(theControl, aContext, ZIP_DOWNLOAD_FOLDER);
		return theControl;
	}

	private static ITableRenderer createTableRenderer(FolderData folderData) {
		return FolderTableRenderer.newInstance();
	}

	private static void addButtonControl(FolderControl folderControl, FormContainer formContainer, String memberName) {
		if (formContainer.hasMember(memberName)) {
			folderControl.addTitleBarControl(
				new ButtonControl((CommandModel) formContainer.getMember(memberName)));
		}
	}

	/**
	 * Creates a {@link BreadcrumbRenderer}.
	 * <p>
	 * In case the written node is the root node and the given root name is not <code>null</code>,
	 * then that is the written node text.
	 * </p>
	 * 
	 * @param rootLabel
	 *        A special text to write for the root node or null if no special handling is desired.
	 */
	public BreadcrumbRenderer createBreadcrumbRenderer(String rootLabel) {
		PolymorphicConfiguration<? extends BreadcrumbRenderer> rendererConfig = getConfig().getBreadcrumbRenderer();
		if (rootLabel != null) {
			setRootLabel(rootLabel, rendererConfig);
		}
		return TypedConfigUtil.createInstance(rendererConfig);
	}

	private void setRootLabel(String rootLabel, PolymorphicConfiguration<? extends BreadcrumbRenderer> rendererConfig) {
		if (!(rendererConfig instanceof DefaultBreadcrumbRenderer.Config)) {
			return;
		}
		DefaultBreadcrumbRenderer.Config defaultRendererConfig = (DefaultBreadcrumbRenderer.Config) rendererConfig;
		BreadcrumbContentRenderer.Config contentRendererConfig = defaultRendererConfig.getContentRenderer();
		ResKey rootLabelKey = ResKey.text(rootLabel);
		TypedConfigUtil.setProperty(contentRendererConfig, BreadcrumbContentRenderer.Config.ROOT_LABEL, rootLabelKey);
	}

	/**
	 * Creates a {@link FolderControl} displaying the given {@link FolderField}.
	 * 
	 * @param field
	 *        The {@link FolderField} to display.
	 */
	public static FolderControl createControl(final FolderField field) {
		FileDropHandler fileDropHandler =
			new FolderFileDropHandler(false, !field.isImmutable(), WebFolderUIFactory.getInstance().getMaxUploadSize());
		final FolderControl control =
			createControl(field.getBreadcrumbRenderer(), field.getFolderData(), field, fileDropHandler);
		control.addVisibilityListenerFor(field);
		return control;
	}

	/**
	 * Whether the locks are requested and released manually by the user.
	 * <p>
	 * If true, the user has to explicitly request the lock before the update button is executable.
	 * </p>
	 * <p>
	 * If false, the locks are automatically requested and released when the user updates a
	 * document.
	 * </p>
	 */
	public boolean getManualLocking() {
		return getConfig().getManualLocking();
	}

	/**
	 * @see Config#getMaxUploadSize()
	 */
	public long getMaxUploadSize() {
		return getConfig().getMaxUploadSize();
	}

	/**
	 * The singleton {@link WebFolderUIFactory} instance.
	 */
	public static WebFolderUIFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link WebFolderUIFactory}.
	 */
	public static final class Module extends TypedRuntimeModule<WebFolderUIFactory> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<WebFolderUIFactory> getImplementation() {
			return WebFolderUIFactory.class;
		}

	}

}

