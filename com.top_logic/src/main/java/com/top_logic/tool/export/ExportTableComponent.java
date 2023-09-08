/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.webfolder.DownloadHandler;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ReadOnlyPropertyAccessor;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.ValueWithErrorControlProvider;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.export.Export.State;
import com.top_logic.util.ReferenceManager;
import com.top_logic.util.Resources;

/**
 * The {@link ExportTableComponent} lists all available {@link Export}
 * registered at the dialog opener component of this.
 * 
 * The component will refresh in a configurable interval to update the UI with
 * changes of the exports progresses. Default is
 * {@link #DEFAULT_REFRESH_INTERVAL}.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class ExportTableComponent extends FormComponent {

	/**
	 * Configuration of {@link ExportHandler}s.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ExportHandlersConfig extends ConfigurationItem {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration parameter for the value of {@link #getExportHandlers}. */
		String EXPORT_HANDLERS_NAME = "exportHandlers";

		/**
		 * Comma separated list of names of {@link ExportHandler}.
		 */
		@Name(EXPORT_HANDLERS_NAME)
		String getExportHandlers();

		/**
		 * The configured handlers, indexed by name.
		 */
		default Map<String, ExportHandler> getHandlers() {
			/* The format can not be used as format for the property, because it uses the
			 * ExportHandlerFactory internally. Parsing any layout file using this property without
			 * the started factory would fail. */
			try {
				return ExportHandlerFormat.INSTANCE.getValue(EXPORT_HANDLERS_NAME, getExportHandlers());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}

	}

	public interface Config extends FormComponent.Config, ExportHandlersConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Interval in seconds between a page reload
		 */
		@Name("refreshInterval")
		@IntDefault(DEFAULT_REFRESH_INTERVAL)
		int getRefreshInterval();

		@Override
		@BooleanDefault(true)
		boolean getResetInvisible();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			getHandlers().values().forEach(handler -> {
				additionalGroups.add(handler.getExportCommandGroup());
				additionalGroups.add(handler.getReadCommandGroup());
			});
		}
	}

	private static final int DEFAULT_REFRESH_INTERVAL = 10;

	public static final String EXPORT = "ExportRowModel";

	private static final String EXPORT_TABLE = "exports";
	private static final String COLUMN_HANDLER = "handler";
	private static final String COLUMN_DATE = "date";
	private static final String COLUMN_DOWNLOAD = "download";
	private static final String COLUMN_ENQUEUE = "enqueue";
	private static final String COLUMN_STATE = "state";

	public static final String[] COLUMNS = { COLUMN_HANDLER, COLUMN_DATE, COLUMN_DOWNLOAD, COLUMN_ENQUEUE, COLUMN_STATE };

	private ScheduledFuture<?> _timer;

	public ExportTableComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public FormContext createFormContext() {

		FormContext theContext = new FormContext(this);
		FormGroup theGroup = new FormGroup("exportRows", theContext.getResources());

		Accessor theAcc = new DispatchingAccessor(new MapBuilder().put(COLUMN_HANDLER, new ReadOnlyPropertyAccessor() {
			@Override
			public Object getValue(Object target) {
				return ((Export) target).getExportHandlerID();
			}
		}).toMap(), SimpleAccessor.INSTANCE);

		TableConfiguration theDesc = TableConfiguration.table();
		theDesc.setResPrefix(getResPrefix().append(EXPORT_TABLE));
		theDesc.setDefaultFilterProvider(null);
		theDesc.getDefaultColumn().setAccessor(theAcc);
		theDesc.getDefaultColumn().setFilterProvider(null);

		ColumnConfiguration column = theDesc.declareColumn(COLUMN_HANDLER);
		column.setFieldProvider(null);
		column.setShowHeader(false);

		column.setResourceProvider(new I18NResourceProvider(getResPrefix()));

		theDesc.getDefaultColumn().setFieldProvider(new ExportEntryFields());
		theDesc.getDefaultColumn().setControlProvider(ValueWithErrorControlProvider.INSTANCE);

		ObjectTableModel theTable = new ObjectTableModel(COLUMNS, theDesc, this.createRows());

		FormTableModel theFormTable = new FormTableModel(theTable, theGroup);
		TableField theTableField = FormFactory.newTableField(EXPORT_TABLE, theFormTable);
		theContext.addMember(theGroup);
		theContext.addMember(theTableField);
		return theContext;
	}

	/**
	 * Return a list of new {@link Export} objects
	 */
	protected List<Export> createRows() {
		List<Export> theList = new ArrayList<>();

		ExportRegistry theReg = ExportRegistryFactory.getExportRegistry();
		Object theModel = this.getModel();
		for (ExportHandler theHandler : config().getHandlers().values()) {
			theList.add(theReg.getExport(theHandler.getExportHandlerID(), theModel));
		}

		return theList;
	}

	@Override
	protected void becomingInvisible() {
		if (_timer != null) {
			_timer.cancel(false);
			_timer = null;
		}
		super.becomingInvisible();
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		long refreshInterval = config().getRefreshInterval();
		ScheduledExecutorService executor = getMainLayout().getWindowScope().getUIExecutor();
		_timer = executor.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				ExportTableComponent.this.updateTable();
			}
		}, refreshInterval, refreshInterval, TimeUnit.SECONDS);
	}

	private Config config() {
		return (Config) getConfig();
	}

	/*package protected*/ class ExportEntryFields extends AbstractFieldProvider {

		@Override
		public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
			{
				Export theExport = (Export) aModel;
				Document theDoc = theExport != null ? theExport.getDocument() : null;

				if (COLUMN_DATE.equals(aProperty)) {

					Date theDate = null;
					String theDateString = StringServices.EMPTY_STRING;
					if (theDoc != null) {
						DocumentVersion theCurrent = theDoc.getDocumentVersion();
						if (theCurrent != null) {
							theDate = new Date(theCurrent.getLastModified().longValue());
						} else {
							theDate = new Date(theDoc.getLastModified().longValue());
						}
					}
					if (theDate != null) {
						theDateString = HTMLFormatter.getInstance().getDateTimeFormat().format(theDate);
					}
					return FormFactory.newStringField(aProperty, theDateString, true);
				} else if (COLUMN_DOWNLOAD.equals(aProperty)) {
					Map theArgs = new MapBuilder().put(EXPORT, theExport).toMap();
					if (theDoc != null) {
						theArgs.put(ExportDownloadHandler.OBJECT_ID, ReferenceManager.getSessionInstance()
							.getReference(IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(theDoc))));
						theArgs.put(ExportDownloadHandler.TYPE, theDoc.tTable().getName());
					}

					ExportHandler theHandler = ExportHandlerRegistry.getInstance().getHandler(theExport.getExportHandlerID());
					CommandHandler theCommandHandler = ExportDownloadHandler.newInstanceDownload(theHandler.getReadCommandGroup());
					CommandField theCommandField = FormFactory.newCommandField(aProperty, theCommandHandler, ExportTableComponent.this, theArgs);
					theCommandField.setImage(Icons.DOWNLOAD);
					theCommandField.setNotExecutableImage(Icons.DOWNLOAD_DISABLED);
					return theCommandField;
				} else if (COLUMN_ENQUEUE.equals(aProperty)) {
					Map theArgs = new MapBuilder().put(EXPORT, theExport).toMap();

					ExportHandler theHandler = ExportHandlerRegistry.getInstance().getHandler(theExport.getExportHandlerID());
					CommandHandler theCommandHandler = EnqueueExportCommand.newInstanceExport(theHandler.getExportCommandGroup());
					CommandField theCommandField = FormFactory.newCommandField(aProperty, theCommandHandler, ExportTableComponent.this, theArgs);
					theCommandField.setImage(Icons.REFRESH);
					theCommandField.setNotExecutableImage(Icons.REFRESH_DISABLED);
					return theCommandField;
				} else if (COLUMN_STATE.equals(aProperty)) {
					State theState = theExport.getState();

					String theLabel = StringServices.EMPTY_STRING;
					switch (theState) {
					case INITIALIZED:
						break;
					case QUEUED:
						theLabel = getResMessage(theState.toString(), theExport.getTimeQueued());
						break;
					case RUNNING:
						theLabel = getResMessage(theState.toString(), theExport.getTimeRunning());
						break;
					case FINISHED:
						theLabel = getResMessage(theState.toString(), theExport.getTimeFinished());
						break;
					case FAILED:
						theLabel = Resources.getInstance().getString(theExport.getFailureKey());
						break;
					}

					return FormFactory.newStringField(aProperty, theLabel, true);
				}
			}

			throw new IllegalArgumentException("Unsupported column '" + aProperty + "'");
		}
	}

	void updateTable() {
		TableField theTable = (TableField) this.getFormContext().getField(EXPORT_TABLE);
		FormTableModel theModel = (FormTableModel) theTable.getTableModel();
		theModel.setRowObjects(createRows());
//		Logger.info("Reload", ExportTableComponent.class);
	}

	/**
	 * Enqueues an {@link Export} and {@link ExportTableComponent#updateTable() updates} the export
	 * table.
	 * 
	 * <p>
	 * The command is executable if the {@link Export} is not {@link State#RUNNING} or
	 * {@link State#QUEUED}.
	 * </p>
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 */
	public static class EnqueueExportCommand extends AJAXCommandHandler {

		public static final String COMMAND_ID = "EnqueueExportCommand";

		public EnqueueExportCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			Export export = (Export) someArguments.get(EXPORT);
			try {
				export.setStateQueued();
			} catch (ExportFailure ex) {
				HandlerResult error = new HandlerResult();
				error.addError(I18NConstants.ENQUEUE_FAILED__MSG.fill(ex.getMessage()));
				return error;
			}
			((ExportTableComponent) aComponent).updateTable();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return new ExecutabilityRule() {

				@Override
				public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
					Export export = (Export) someValues.get(EXPORT);

					if (export != null) {

						// check if export is already running.
						State theState = export.getState();
						switch (theState) {
						case RUNNING:
								return ExecutableState.createDisabledState(I18NConstants.ERROR_IN_RUNNING_STATE);
						case QUEUED:
								return ExecutableState.createDisabledState(I18NConstants.ERROR_IN_QUEUED_STATE);
						case FAILED:
						case INITIALIZED:
						case FINISHED:
						default:
						}
					}
					return ExecutableState.EXECUTABLE;
				}
			};
		}

		public static EnqueueExportCommand newInstanceExport(BoundCommandGroup exportCommandGroup) {
			EnqueueExportCommand result =
				newInstance(updateGroup(
					AbstractCommandHandler.<Config> createConfig(EnqueueExportCommand.class, COMMAND_ID),
					exportCommandGroup));
			return result;
		}
	}
	
	/**
	 * Download of the created {@link Export#getDocument() document} of an {@link Export}.
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 */
	public static class ExportDownloadHandler extends DownloadHandler {

		private static final String COMMAND_ID = "DownloadExport";

		public ExportDownloadHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo, Map<String, Object> arguments)
				throws IOException {
			return arguments.get(EXPORT);
		}

		@Override
		public void cleanupDownload(Object model, Object download) {
		}

		@Override
		public String getDownloadName(LayoutComponent aComponent, Object download) {
			StringBuilder theResult = new StringBuilder();
			Export theExport = (Export) download;
			Resources theRes = Resources.getInstance();

			String theName = theRes.decodeMessageFromKeyWithEncodedArguments(theExport.getDisplayNameKey());
			theResult.append(theName).append(Export.EXTENSION_SEPARATOR).append(theExport.getFileExtension());

			return theResult.toString();
		}

		@Override
		public BinaryDataSource getDownloadData(Object download) throws IOException {
			return ((Export) download).getDocument();
		}

		/**
		 * Overridden because we must return the components current object instead of the result document.
		 */
		@Override
		protected BoundObject getBoundObject(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (aComponent instanceof BoundChecker) {
				return ((BoundChecker) aComponent).getCurrentObject(this.getCommandGroup(), model);
			}
			return null;
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return new ExecutabilityRule() {

				@Override
				public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
					Export export = (Export) someValues.get(EXPORT);
					if (export != null && (export.getDocument() == null || !export.getDocument().tValid())) {
						return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_DOCUMENT);
					}
					return ExecutableState.EXECUTABLE;
				}
			};
		}

		@Override
		public long getMaxDirectDownloadTime() {
			// Never show a progress dialog.
			return 0L;
		}

		public static CommandHandler newInstanceDownload(BoundCommandGroup readCommandGroup) {
			return newInstance(updateGroup(
				AbstractCommandHandler.<Config> createConfig(ExportDownloadHandler.class, COMMAND_ID), readCommandGroup));
		}
	}

	/**
	 * <p>
	 * Quirks
	 * </p>
	 * <p>
	 * Overwritten because the semantics and handling of defaultI18N of
	 * {@link DialogInfo} is highly unclear nor does is work correctly.
	 * </p>
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 */
	public static class OpenExportTableCommand extends OpenModalDialogCommandHandler {

		public static final ResKey KEY_AND_RESOURCE_KEY = I18NConstants.EXPORT_TABLE_COMMAND;

		public OpenExportTableCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return KEY_AND_RESOURCE_KEY;
		}
	}
}
