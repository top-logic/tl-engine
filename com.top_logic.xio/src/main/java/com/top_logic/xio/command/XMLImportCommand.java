/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.codeedit.editor.ConfigXMLEditor;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.XmlImporter;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;
import com.top_logic.xio.importer.handlers.ConfiguredImportHandler;
import com.top_logic.xio.importer.handlers.DispatchingImporter;
import com.top_logic.xio.importer.handlers.Handler;

/**
 * Configurable command that allows to import XML data structures into TopLogic models.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class XMLImportCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link XMLImportCommand}.
	 */
	@DisplayOrder({
		Config.UPLOAD_TITLE,
		Config.UPLOAD_HEADER,
		Config.UPLOAD_MESSAGE,
		Config.UPLOAD_WIDTH,
		Config.UPLOAD_HEIGHT,
		Config.PROGRESS_TITLE,
		Config.PROGRESS_WIDTH,
		Config.PROGRESS_HEIGHT,
		Config.IMPORT_DEFINITION,
		Config.LOGGING,
	})
	public interface Config extends AbstractCommandHandler.Config {

		/** @see #getUploadTitle() */
		String UPLOAD_TITLE = "upload-title";

		/** @see #getUploadHeader() */
		String UPLOAD_HEADER = "upload-header";

		/** @see #getUploadMessage() */
		String UPLOAD_MESSAGE = "upload-message";

		/** @see #getUploadWidth() */
		String UPLOAD_WIDTH = "upload-width";

		/** @see #getUploadHeight() */
		String UPLOAD_HEIGHT = "upload-height";

		/** @see #getProgressTitle() */
		String PROGRESS_TITLE = "progress-title";

		/** @see #getProgressWidth() */
		String PROGRESS_WIDTH = "progress-width";

		/** @see #getProgressHeight() */
		String PROGRESS_HEIGHT = "progress-height";

		/** @see #getImportDefinition() */
		String IMPORT_DEFINITION = "import-definition";

		/** @see #getLogging() */
		String LOGGING = "logging";

		/**
		 * The title of the upload dialog.
		 * 
		 * <p>
		 * If noting is entered, a generic title is used.
		 * </p>
		 */
		@Name(UPLOAD_TITLE)
		ResKey getUploadTitle();

		/**
		 * The header text being displayed in the upload dialog.
		 * 
		 * <p>
		 * If noting is entered, a generic header is used.
		 * </p>
		 */
		@Name(UPLOAD_HEADER)
		ResKey getUploadHeader();

		/**
		 * The message text being displayed before the upload field.
		 * 
		 * <p>
		 * If noting is entered, a generic message is used.
		 * </p>
		 */
		@Name(UPLOAD_MESSAGE)
		ResKey getUploadMessage();

		/**
		 * The width of the upload dialog.
		 */
		@Name(UPLOAD_WIDTH)
		@FormattedDefault("500px")
		@NonNullable
		DisplayDimension getUploadWidth();

		/**
		 * The height of the upload dialog.
		 */
		@Name(UPLOAD_HEIGHT)
		@FormattedDefault("300px")
		@NonNullable
		DisplayDimension getUploadHeight();

		/**
		 * The title of the import progress dialog.
		 * 
		 * <p>
		 * If nothing is specified, a generic title is used.
		 * </p>
		 */
		@Name(PROGRESS_TITLE)
		ResKey getProgressTitle();

		/**
		 * The width of the import progress dialog.
		 */
		@Name(PROGRESS_WIDTH)
		@FormattedDefault("600px")
		DisplayDimension getProgressWidth();

		/**
		 * The height of the import progress dialog.
		 */
		@Name(PROGRESS_HEIGHT)
		@FormattedDefault("800px")
		DisplayDimension getProgressHeight();

		/**
		 * The XML import specification.
		 * 
		 * @implNote The contents is expected to be the XML serialization of an import
		 *           {@link DispatchingImporter} configuration.
		 */
		@ItemDefault
		@Name(IMPORT_DEFINITION)
		@NonNullable
		ImportSpec getImportDefinition();

		/**
		 * Whether to write import operations to the application log.
		 */
		@Name(LOGGING)
		boolean getLogging();

		@FormattedDefault("theme:ICONS_IMPORT")
		@Override
		ThemeImage getImage();

		@Override
		@FormattedDefault(CommandHandlerFactory.IMPORT_BUTTONS_GROUP)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

		/**
		 * Container for the import handler definition.
		 */
		interface ImportSpec extends ConfigurationItem {

			/**
			 * The configured import handler.
			 */
			@PropertyEditor(ConfigXMLEditor.class)
			@ControlProvider(ConfigXMLEditor.CP.class)
			@ItemDisplay(ItemDisplayType.VALUE)
			@RenderWholeLine
			@ItemDefault(DispatchingImporter.Config.class)
			@NonNullable
			@DefaultContainer
			ConfiguredImportHandler.Config<?> getHandler();

		}

	}

	private final ResKey _uploadTitle;

	private final ResKey _uploadHeader;

	private final ResKey _uploadMessage;

	private final DisplayDimension _uploadWidth;

	private final DisplayDimension _uploadHeight;

	private final ResKey _progressTitle;

	private final DisplayDimension _progressWidth;

	private final DisplayDimension _progressHeight;

	private final Handler _importDefinition;

	private final boolean _logging;

	/**
	 * Creates a {@link XMLImportCommand}.
	 */
	public XMLImportCommand(InstantiationContext context, Config config) {
		super(context, config);

		_uploadTitle = fallback(config.getUploadTitle(), I18NConstants.DIALOG_TITLE);
		_uploadHeader = fallback(config.getUploadHeader(), I18NConstants.DIALOG_HEADER);
		_uploadMessage = fallback(config.getUploadMessage(), I18NConstants.DIALOG_MESAGE);
		_uploadWidth = config.getUploadWidth();
		_uploadHeight = config.getUploadHeight();

		_progressTitle = fallback(config.getProgressTitle(), I18NConstants.PROGRESS_TITLE);
		_progressWidth = config.getProgressWidth();
		_progressHeight = config.getProgressHeight();
		_importDefinition = context.getInstance(config.getImportDefinition().getHandler());
		_logging = config.getLogging();
	}

	private static ResKey fallback(ResKey value, ResKey fallback) {
		return value == null ? fallback : value;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		return new SimpleFormDialog(_uploadTitle, _uploadHeader, _uploadMessage, _uploadWidth, _uploadHeight) {
			private DataField _dataField;

			@Override
			protected void fillFormContext(FormContext context) {
				_dataField = FormFactory.newDataField(INPUT_FIELD, false);
				context.addMember(_dataField);
			}

			@Override
			protected void fillButtons(List<CommandModel> buttons) {
				addCancel(buttons);
				buttons.add(MessageBox.button(ButtonType.OK, this::startImport));
			}

			protected HandlerResult startImport(DisplayContext displaycontext) {
				// IGNORE FindBugs(UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS): Bug in FindBugs.
				return openProgress(displaycontext, _dataField.getDataItem(), getDiscardClosure());
			}

		}.open(aContext);
	}

	/**
	 * Performs the actual import.
	 */
	protected HandlerResult openProgress(DisplayContext displaycontext, BinaryData dataItem, Command closeUpload) {
		Handler importDefinition = _importDefinition;
		boolean logging = _logging;

		long size = dataItem.getSize();
		boolean hasSize = size > 0;

		// Produce at least 1000 updates.
		long bytesPerStep = (size + 1000 - 1) / 1000;

		return new ProgressDialog(_progressTitle, _progressWidth, _progressHeight) {
			private boolean _success;

			@Override
			protected int getStepCnt() {
				if (hasSize) {
					// Reserve one step for the final commit.
					return (int) (size / bytesPerStep) + 1;
				}
				return super.getStepCnt();
			}

			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				try {
					XmlImporter importer = XmlImporter.newInstance(log, importDefinition);
					importer.setLogCreations(logging);

					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
					ModelBinding modelBinding = new ApplicationModelBinding(kb, ModelService.getApplicationModel());

					try (InputStream stream = dataItem.getStream()) {
						InputStream in =
							hasSize ? new ProgressReporter(this::setProgress, stream, bytesPerStep) : stream;
						Source source = new StreamSource(in);

						log.info(I18NConstants.STARTING_IMPORT);

						try (Transaction tx = kb.beginTransaction()) {
							importer.importModel(modelBinding, source);

							log.info(I18NConstants.COMMITTING_CHANGES);
							tx.commit();
						}
					}

					log.info(I18NConstants.IMPORT_COMPLETED);
					_success = true;
				} catch (IOException | XMLStreamException ex) {
					log.log(Level.ERROR, I18NConstants.ERROR_IMPORT_FAILED__MSG.fill(ex.getMessage()), ex);
				}
			}

			@Override
			protected void setProgress(int step) {
				super.setProgress(step);
			}

			boolean wasSuccessful() {
				return _success;
			}

			@Override
			public Command getDiscardClosure() {
				Command closeProgress = super.getDiscardClosure();
				return new Command() {
					@Override
					public HandlerResult executeCommand(DisplayContext context) {
						if (wasSuccessful()) {
							closeUpload.executeCommand(context);
						}
						return closeProgress.executeCommand(context);
					}
				};
			}

		}.open(displaycontext);
	}

	/**
	 * {@link InputStream} that observes the number of bytes read and produces progress updates.
	 */
	private final class ProgressReporter extends InputStream {
		private final InputStream _in;
	
		private final long _bytesPerStep;
	
		long _consumed = 0;
	
		private Consumer<Integer> _progress;
	
		/**
		 * Creates a {@link ProgressReporter}.
		 * 
		 * @param progress
		 *        The target of progress reports.
		 * @param in
		 *        The source of the data.
		 * @param bytesPerStep
		 *        Number of bytes to read before the next progress step is announced.
		 */
		public ProgressReporter(Consumer<Integer> progress, InputStream in, long bytesPerStep) {
			_progress = progress;
			_in = in;
			_bytesPerStep = bytesPerStep;
		}
	
		@Override
		public int read() throws IOException {
			int result = _in.read();
			if (result > 0) {
				consume(1);
			}
			return result;
		}
	
		@Override
		public int read(byte[] b) throws IOException {
			int direct = _in.read(b);
			consume(direct);
			return direct;
		}
	
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int direct = _in.read(b, off, len);
			consume(direct);
			return direct;
		}
	
		private void consume(int direct) {
			if (direct > 0) {
				_consumed += direct;
				_progress.accept((int) (_consumed / _bytesPerStep));
			}
		}
	}

}
