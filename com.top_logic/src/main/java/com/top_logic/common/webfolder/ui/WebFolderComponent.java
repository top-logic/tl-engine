/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.folder.ui.FolderComponent;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.clipboard.ModifyClipboardExecutable;
import com.top_logic.common.webfolder.ui.commands.UploadDialog;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadCommand;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.folder.FileDropHandler;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.folder.FolderFileDropHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * FolderComponent to display a WebFolder.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderComponent extends FolderComponent implements WebFolderAware {


	/**
	 * Configuration of a {@link WebFolderComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FolderComponent.Config, UIOptions {

		@BooleanDefault(true)
		@Override
		boolean getHasNewFolderCommand();

		@BooleanDefault(true)
		@Override
		boolean getHasZipDownloadCommand();

		@BooleanDefault(true)
		@Override
		boolean getHasClipboardCommand();

		/**
		 * @see WebFolderUIFactory#getManualLocking()
		 */
		@Name(WebFolderUIFactory.Config.MANUAL_LOCKING)
		Decision getManualLocking();

	}

	/**
	 * In-app customizable options for {@link WebFolderComponent}.
	 */
	public interface UIOptions extends FolderComponent.UIOptions {
		/** Configuration name for {@link #getHasUploadCommand()}. */
		String HAS_UPLOAD_COMMAND = "hasUploadCommand";

		/**
		 * Whether new documents can be uploaded by the user into the displayed folder.
		 */
		@BooleanDefault(true)
		@Name(HAS_UPLOAD_COMMAND)
		@Label("Upload documents")
		boolean getHasUploadCommand();

		/**
		 * The maximum size of a single document.
		 * 
		 * <p>
		 * A value of <code>0</code> means that that the application-wide default applies.
		 * </p>
		 * 
		 * @see WebFolderUIFactory.Config#getMaxUploadSize()
		 */
		@Name(WebFolderUIFactory.Config.MAX_UPLOAD_SIZE)
		@Format(MemorySizeFormat.class)
		long getMaxUploadSize();

	}

	private final boolean _hasUploadCommand;

	private final long _maxUploadSize;

    /**
	 * Creates a new {@link WebFolderComponent}.
	 * 
	 * @param config
	 *        Configuration for this component.
	 * @throws ConfigurationException
	 *         in case of errors.
	 */
	public WebFolderComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_hasUploadCommand = config.getHasUploadCommand();

		long customSize = config.getMaxUploadSize();
		_maxUploadSize = (customSize > 0) ? customSize : WebFolderUIFactory.getInstance().getMaxUploadSize();
	}

    @Override
	protected Control createControlForContext(FormContext context) {
		long maxUploadSize = getMaxUploadSize();
		FileDropHandler fileDropHandler =
			new FolderFileDropHandler(getManualLocking(), !context.isImmutable(), maxUploadSize);
		return WebFolderUIFactory.createControl(getBreadcrumbRenderer(), getFolderData(), context, fileDropHandler);
    }
    
	/**
	 * Decide, whether objects in the given {@link WebFolder} can be added to the clipboard.
	 * 
	 * @param folder
	 *        The current {@link WebFolder}.
	 * @return {@link ExecutableState} of the add to clipboard buttons in the given folder.
	 */
	protected ExecutableState getClipability(WebFolder folder) {
		if (!getConfig().getHasClipboardCommand()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return defaultClipability(folder);
	}

	/**
	 * Return the default clipability for a {@link WebFolder}.
	 * 
	 * @see ModifyClipboardExecutable#getDefaultExecutability(com.top_logic.knowledge.wrap.Wrapper)
	 */
	public static ExecutableState defaultClipability(WebFolder folder) {
		return ModifyClipboardExecutable.getDefaultExecutability(folder);
	}

	/**
	 * Decide, whether the given {@link WebFolder} should be considered modifyable.
	 * 
	 * <p>
	 * Only in a modifyable folder, upload, create, and so on are displayed.
	 * </p>
	 * 
	 * @param folder
	 *        The current {@link WebFolder}.
	 * @return Whether the given folder is technically modifyable.
	 */
	protected ExecutableState getModifyability(WebFolder folder) {
		return defaultModifiability(folder);
	}

	public static ExecutableState defaultModifiability(WebFolder folder) {
		if (folder == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (WrapperHistoryUtils.getRevision(folder).isCurrent()) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}


	/**
	 * the {@link WebFolder} shown in this component
	 */
    @Override
	public WebFolder getWebFolder() {
		return (WebFolder) getBuilder().getModel(getModel(), this);
    }


	@Override
	protected ExecutableState getModifyability(FolderDefinition folder) {
		return getModifyability((WebFolder) folder);
	}

	@Override
	protected ExecutableState getClipability(FolderDefinition folder) {
		return getClipability((WebFolder) folder);
	}

	@Override
	protected FolderDefinition getFolderDefinition() {
		return getWebFolder();
	}

	@Override
	protected String getFolderSize() {
		String theSizeString = "0";
		WebFolder theFolder = this.getWebFolder();
		if (theFolder != null) {
			theSizeString = "" + theFolder.getContentSize();
		}
		return theSizeString;
	}

	@Override
	protected boolean isSupported(Object aModel) {
		return (aModel instanceof WebFolder) || (aModel instanceof Document) || (aModel instanceof DocumentVersion);
	}

	protected boolean hasUploadCommand() {
		return _hasUploadCommand;
	}

	/**
	 * Factory for {@link UploadDialog}s storing into a {@link WebFolder}.
	 */
	public static class WebFolderUploadExecutor implements UploadExecutor {

		private static final Consumer<Object> IGNORE = object -> {
			// ignore document
		};

		private final SingleSelectionModel _folderSelection;

		final Consumer<? super Document> _continuation;

		private long _maxUploadSize;

		public WebFolderUploadExecutor(SingleSelectionModel folderSelection, long maxUploadSize) {
			this(folderSelection, IGNORE, maxUploadSize);
		}

		public WebFolderUploadExecutor(SingleSelectionModel folderSelection, Consumer<? super Document> continuation, long maxUploadSize) {
			_folderSelection = folderSelection;
			_continuation = continuation;
			_maxUploadSize = maxUploadSize;
		}

		public SingleSelectionModel getFolderSelection() {
			return _folderSelection;
		}

		@Override
		public UploadDialog createUploadDialog(ResPrefix resourcePrefix, FolderDefinition folderDefinition) {
			return new UploadDialog(resourcePrefix, folderDefinition, getUploadWidth(), getUploadHeight(),
				_maxUploadSize) {
				@Override
				protected UploadCommand createUploadCommand(final UploadDialog dialog) {
					return new WebfolderUpload(dialog, getFolderSelection(), _continuation);
				}
			};
		}
	
		/**
		 * Width of the upload dialog.
		 */
		protected DisplayDimension getUploadWidth() {
			return DisplayDimension.dim(500, DisplayUnit.PIXEL);
		}
	
		/**
		 * Height of the upload dialog.
		 */
		protected DisplayDimension getUploadHeight() {
			return DisplayDimension.dim(500, DisplayUnit.PIXEL);
		}
	
		/**
		 * {@link UploadCommand} for storing into the currently displayed {@link WebFolder}.
		 */
		protected static final class WebfolderUpload extends UploadCommand {

			private final SingleSelectionModel _folderSelection;

			private final Consumer<? super Document> _continuation;

			/**
			 * Creates a {@link WebfolderUpload}.
			 * 
			 * @param dialog
			 *        See {@link #getDialog()}
			 * @param folderSelection
			 *        The currently displayed sub-folder.
			 * @param continuation
			 *        Continuation to call with the uploaded document.
			 */
			public WebfolderUpload(UploadDialog dialog, SingleSelectionModel folderSelection,
					Consumer<? super Document> continuation) {
				super(dialog);

				_folderSelection = folderSelection;
				_continuation = continuation;
			}

			@Override
			protected HandlerResult executeUpload(DisplayContext displayContext, List<BinaryData> files) {
				FolderNode node = (FolderNode) _folderSelection.getSingleSelection();
				WebFolder folder = (WebFolder) node.getBusinessObject();

				if (!ComponentUtil.isValid(folder)) {
					return ComponentUtil.errorObjectDeleted(displayContext);
				}
				return executeUpload(displayContext, folder, files);
			}

			/**
			 * Upload into the given folder.
			 */
			protected HandlerResult executeUpload(DisplayContext aContext, WebFolder folder, List<BinaryData> files) {
				Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
				try {
					List<Document> documents = new ArrayList<>();
					for (BinaryData file : files) {
						String theName = file.getName();
						if (folder.hasChild(theName)) {
							return UploadDialog.errorDocumentExists(aContext, file);
						}
						Document document = folder.createOrUpdateDocument(theName, file);
						FormContext formContext = getFormContext();
						WebFolderUtils.updateDescription(document, formContext);

						documents.add(document);
						
					}
					
					tx.commit();
					for (Document document : documents) {
						_continuation.accept(document);
					}
					return HandlerResult.DEFAULT_RESULT;
				} finally {
					tx.rollback();
				}
			}
		}
	}

	@Override
	protected TreeBuilder getTreeBuilder() {
		return WebFolderTreeBuilder.INSTANCE;
	}

	@Override
	protected TableConfiguration getTableConfiguration(ExecutableState canAddToClipboard, ExecutableState canUpdate,
			ExecutableState canDelete) {
		WebFolderColumnDescriptionBuilder builder = new WebFolderColumnDescriptionBuilder(canAddToClipboard, canUpdate, canDelete, getManualLocking());
		Config config = getConfig();
		builder.setAnalysis(config.analyzeDocuments());
		builder.setMail(config.sendDocuments());
		TableConfiguration columnDescriptions = builder.createWebFolderColumns();
		return columnDescriptions;
	}

	@Override
	protected final UploadExecutor getUploadExecuter(FolderData folderData) {
		if (!hasUploadCommand()) {
			return null;
		}
		return createUploadExecutor(folderData);
	}

	/**
	 * Creates the {@link UploadExecutor} for {@link #getUploadExecuter(FolderData)} in case of
	 * {@link #hasUploadCommand()}.
	 * 
	 * @param folderData
	 *        The displayed {@link FolderData}.
	 * @return Result of {@link #getUploadExecuter(FolderData)}.
	 */
	protected UploadExecutor createUploadExecutor(FolderData folderData) {
		return new WebFolderUploadExecutor(getFolderSelection(), getMaxUploadSize());
	}

	/**
	 * @see WebFolderUIFactory#getManualLocking()
	 */
	protected boolean getManualLocking() {
		return getConfig().getManualLocking().toBoolean(getManualLockingDefault());
	}

	/**
	 * The default value of {@link #getManualLocking()} which is used when it is not set in the
	 * configuration of this component.
	 */
	protected boolean getManualLockingDefault() {
		return WebFolderUIFactory.getInstance().getManualLocking();
	}

	/**
	 * @see WebFolderUIFactory#getMaxUploadSize()
	 */
	public final long getMaxUploadSize() {
		return _maxUploadSize;
	}
	
	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

}

