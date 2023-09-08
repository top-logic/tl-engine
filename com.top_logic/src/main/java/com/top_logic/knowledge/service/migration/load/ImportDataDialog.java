/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.load;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLStreamException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.Oracle12Helper;
import com.top_logic.basic.sql.OracleHelper;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseFactoryConfig;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.migration.DumpReader;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLDumper;
import com.top_logic.knowledge.service.db2.migration.sql.SQLLoaderInsertWriter;
import com.top_logic.knowledge.service.migration.DataMigration;
import com.top_logic.knowledge.service.migration.MigrationInfo;
import com.top_logic.knowledge.service.migration.MigrationUtil;
import com.top_logic.knowledge.service.migration.Version;
import com.top_logic.knowledge.service.migration.VersionDescriptor;
import com.top_logic.knowledge.service.migration.dump.KBDumpHandler;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Zipper;
import com.top_logic.util.error.TopLogicException;

/**
 * Assistent dialog for loading or transforming application data dumps.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImportDataDialog extends AbstractFormPageDialog {

	private DataField _dumpField;

	private StringField _versionField;

	private StringField _migrationField;

	private DataMigration _migration;

	private CommandModel _startButton;

	private CommandModel _downloadSqlButton;

	private CommandModel _downloadLoaderButton;

	/**
	 * Creates a {@link ImportDataDialog}.
	 */
	public ImportDataDialog(DisplayDimension width, DisplayDimension height) {
		super(I18NConstants.UPLOAD_DUMP, width, height);
	}

	@Override
	protected IconControl createTitleIcon() {
		IconControl result = new IconControl();
		result.setSrcKey(Icons.DATA_IMPORT);
		return result;
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.UPLOAD_DUMP;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		context.addMember(_dumpField = FormFactory.newDataField("dump", false));
		context.addMember(_versionField = FormFactory.newStringField("version", true));
		context.addMember(_migrationField = FormFactory.newStringField("migration", true));

		_versionField.setControlProvider(MultiLineText.INSTANCE);
		_migrationField.setControlProvider(MultiLineText.INSTANCE);

		_dumpField.addConstraint(this::dumpUploaded);
		
		template(context, div(verticalBox(
			fieldBox("dump"),
			fieldBox("version"),
			fieldBox("migration"))));
	}

	private boolean dumpUploaded(@SuppressWarnings("unused") Object newValue) throws CheckException {
		BinaryData data = getDumpData();
		if (data == null) {
			_versionField.setValue(null);
			_migrationField.setValue(null);
			_migration = null;
			disableButtons();
			return true;
		}

		try (InputStream in = input(data)) {
			try (DumpReader reader = new DumpReader(null, in)) {
				VersionDescriptor dataVersion = reader.getVersion();
				MigrationInfo migrationInfo = dataVersion == null ? MigrationInfo.NO_MIGRATION
					: MigrationUtil.buildMigration(log(), dataVersion);

				if (dataVersion == null) {
					_versionField.setValue(Resources.getInstance().getString(I18NConstants.NO_DATA_VERSION));
				} else {
					StringBuilder versionBuffer = new StringBuilder();
					for (Version moduleVersion : dataVersion.getModuleVersions().values()) {
						versionBuffer.append(moduleVersion.getModule() + ": " + moduleVersion.getName() + "\n");
					}
					_versionField.setValue(versionBuffer.toString());
				}

				if (migrationInfo.isDownGrade()) {
					throw new CheckException(
						Resources.getInstance().getString(I18NConstants.ERROR_NO_DOWNGRADE_POSSIBLE));
				} else {
					_migration = new DataMigration(migrationInfo);
					_migration.build(log());

					if (!_migration.getProcessors().isEmpty()) {
						throw new CheckException(
							Resources.getInstance().getString(I18NConstants.ERROR_SQL_PREPROCESSING_REQUIRED));
					}

					if (dataVersion != null) {
						StringBuilder migrationBuffer = new StringBuilder();
						Map<String, Version> newVersion = new HashMap<>(_migration.getNewVersion());
						for (Version moduleVersion : newVersion.values()) {
							migrationBuffer.append(moduleVersion.getModule() + ": " + moduleVersion.getName() + "\n");
						}
						if (migrationBuffer.length() == 0) {
							_migrationField.setValue(Resources.getInstance().getString(I18NConstants.NO_MIGRATIONS));
						} else {
							_migrationField.setValue(migrationBuffer.toString());
						}
					} else {
						_migrationField.setValue(Resources.getInstance().getString(I18NConstants.NO_MIGRATIONS));
					}
				}
			}
		} catch (AbortExecutionException ex) {
			throw ex;
		} catch (ConfigurationException | XMLStreamException | IOException ex) {
			throw new CheckException(
					Resources.getInstance()
						.getString(I18NConstants.ERROR_EXTRACTING_DATA_VERSION__MESSAGE.fill(ex.getMessage())));
		}

		_startButton.setExecutable();
		_downloadSqlButton.setExecutable();
		_downloadLoaderButton.setExecutable();
		return true;
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return DefaultFormFieldControlProvider.INSTANCE.createControl(getFormContext());
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(
			_downloadSqlButton = MessageBox.button(I18NConstants.DOWNLOAD_SQL, com.top_logic.tool.export.Icons.DOWNLOAD,
				this::doDownloadSql));
		buttons.add(
			_downloadLoaderButton =
				MessageBox.button(I18NConstants.DOWNLOAD_SCRIPT, com.top_logic.tool.export.Icons.DOWNLOAD,
					this::doDownloadLoaderZip));
		buttons.add(
			_startButton = MessageBox.button(I18NConstants.START_IMPORT, Icons.START_IMPORT, this::startImport));
		buttons.add(MessageBox.button(ButtonType.CLOSE, getDiscardClosure()));

		disableButtons();
	}

	private void disableButtons() {
		_startButton.setNotExecutable(I18NConstants.DUMP_DATA_REQUIRED);
		_downloadSqlButton.setNotExecutable(I18NConstants.DUMP_DATA_REQUIRED);
		_downloadLoaderButton.setNotExecutable(I18NConstants.DUMP_DATA_REQUIRED);
	}

	private HandlerResult startImport(DisplayContext context) {
		return createImportProgress().open(context);
	}

	private HandlerResult doDownloadSql(DisplayContext context) {
		return createDownloadSqlProgress(getDumpData()).open(context);
	}

	private ProgressDialog createDownloadSqlProgress(BinaryData dumpData) {
		DataMigration migration = _migration;

		return new ProgressDialog(I18NConstants.DOWNLOAD_SQL, px(400), px(300)) {
			private File _sqlFile;

			@Override
			public void run(I18NLog log) {
				KnowledgeBaseConfiguration kbConfig = loadKbConfig();
				ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();

				_sqlFile = createSqlFile(log.asLog(), kbConfig, pool, dumpData, migration);
			}

			@Override
			public void handleCompleted(DisplayContext context) {
				context.getWindowScope().deliverContent(
					BinaryDataFactory.createBinaryData(_sqlFile, "application/binary", baseName(dumpData) + ".sql.gz"));
			}
		};
	}

	private HandlerResult doDownloadLoaderZip(DisplayContext context) {
		return createDownloadLoaderProgress().open(context);
	}

	private ProgressDialog createDownloadLoaderProgress() {
		String baseName = baseName(getDumpData()) + "-loader";
		DataMigration migration = _migration;

		return new ProgressDialog(I18NConstants.DOWNLOAD_SQL, px(400), px(300)) {
			private File _loaderZip;

			@Override
			public void run(I18NLog log) {
				KnowledgeBaseConfiguration kbConfig = loadKbConfig();
				ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();

				_loaderZip = createLoaderZip(log.asLog(), kbConfig, pool, getDumpData(), migration, baseName);
			}

			@Override
			public void handleCompleted(DisplayContext context) {
				context.getWindowScope().deliverContent(
					BinaryDataFactory.createBinaryData(_loaderZip, "application/binary", baseName + ".zip"));
			}
		};
	}

	static String baseName(BinaryData data) {
		return removeExt(removeExt(data.getName(), KBDumpHandler.GZ_EXT), KBDumpHandler.DUMP_EXT);
	}

	private static String removeExt(String name, String ext) {
		return name.endsWith(ext) ? name.substring(0, name.length() - ext.length()) : name;
	}

	private ProgressDialog createImportProgress() {
		LogProtocol importLog = log();
		KnowledgeBaseConfiguration kbConfig = loadKbConfig();
		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		BinaryData dumpData = getDumpData();
		DataMigration migration = _migration;

		return new ProgressDialog(I18NConstants.DOWNLOAD_SQL, px(400), px(300)) {
			private File _sqlFile;

			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				_sqlFile = createSqlFile(log.asLog(), kbConfig, pool, dumpData, migration);
			}

			@Override
			protected void handleCompleted(DisplayContext context) {
				MessageBox
					.newBuilder(MessageType.CONFIRM)
					.message(I18NConstants.CONFIRM_IMPORT)
					.buttons(MessageBox.button(ButtonType.OK, this::doImport), MessageBox.button(ButtonType.CANCEL))
					.confirm(context.getWindowScope());
			}

			// IGNORE FindBugs(UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS): Seems to be a bug in
			// spotbugs.
			private HandlerResult doImport(DisplayContext context) {
				// Load in the background.
				SchedulerService.getInstance().schedule(
					inSystemInteraction(() -> loadSql(importLog, kbConfig, pool, migration, _sqlFile)),
					2, TimeUnit.SECONDS);

				// Direct logout to prevent errors from being transported back to the UI.
				context.asRequest().getSession(false).invalidate();

				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	/**
	 * Wraps the given callback into a {@link Runnable} that sets up a system context for the
	 * execution.
	 */
	protected static Runnable inSystemInteraction(InContext runnable) {
		return () -> {
			ThreadContextManager.inSystemInteraction(ImportDataDialog.class, runnable);
		};
	}

	final BinaryData getDumpData() {
		return _dumpField.getDataItem();
	}

	static File createSqlFile(Log log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool,
			BinaryData data, DataMigration migration) {
		log.info("Creating import SQL.");
		try {
			DBHelper sqlDialect = pool.getSQLDialect();
			File sqlFile = File.createTempFile("data", ".sql.gz", Settings.getInstance().getTempDir());
			try (InsertWriter insertWriter = createSqlWriter(sqlDialect, sqlFile)) {
				processDumpData(log, kbConfig, pool, data, migration, insertWriter);
			}
			log.info("SQL generated.");
			return sqlFile;
		} catch (AbortExecutionException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED, ex);
		}
	}

	static File createLoaderZip(Log log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool,
			BinaryData data, DataMigration migration, String baseName) {
		log.info("Creating SQL loader scripts.");
		try {
			DBHelper sqlDialect = pool.getSQLDialect();
			if (!(sqlDialect instanceof OracleHelper)) {
				// For "cross-compiling" application data.
				sqlDialect = TypedConfigUtil.createInstance(Oracle12Helper.Config.class);
			}
			File loaderDir = File.createTempFile(baseName, "", Settings.getInstance().getTempDir());

			// Allow creating directory with tmp file name.
			loaderDir.delete();
			loaderDir.mkdir();

			InsertWriter insertWriter = new SQLLoaderInsertWriter(sqlDialect, loaderDir);
			processDumpData(log, kbConfig, pool, data, migration, insertWriter);

			log.info("Zipping loader scripts.");
			File loaderFile = File.createTempFile(baseName, ".zip", Settings.getInstance().getTempDir());
			try (Zipper zip = new Zipper(loaderFile)) {
				zip.addFolder(loaderDir, baseName);
			}
			return loaderFile;
		} catch (AbortExecutionException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED, ex);
		}
	}

	private static void processDumpData(Log log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool,
			BinaryData data, DataMigration migration, InsertWriter insertWriter) throws IOException, Exception {
		try (InputStream in = input(data)) {
			insertWriter.beginDump();
			migration.createInsertSQL(log, kbConfig, pool, 1L, in, insertWriter);
			insertWriter.endDump();
		}
	}

	private static InsertWriter createSqlWriter(DBHelper sqlDialect, File sqlFile) throws IOException {
		return SQLDumper.createInsertWriter(sqlDialect, SQLDumper.DEFAULT_INSERT_CHUNK_SIZE, writer(sqlFile));
	}

	static void loadSql(LogProtocol log, KnowledgeBaseConfiguration kbConfig, ConnectionPool pool,
			DataMigration migration, File sqlFile) {
		log.info("Shutting down system for import.");

		try {
			ModuleUtil.INSTANCE.restart(KnowledgeBaseFactory.Module.INSTANCE, () -> {
				log.info("Starting import.");

				try (Reader in = reader(sqlFile)) {
					PooledConnection connection = pool.borrowWriteConnection();
					try {
						migration.dropAndRecreateDatabase(connection, kbConfig, in);
					} finally {
						pool.releaseWriteConnection(connection);
					}
					log.info("Import finished successfully.");
				} catch (Exception ex) {
					log.error("Data import reported errors, trying to restart anyway.", ex);
				}

				log.info("Restarting services.");
			});
			log.info("System up and running with loaded data.");
		} catch (RestartException ex) {
			throw new TopLogicException(I18NConstants.ERROR_RESTART_FAILED, ex);
		}
	}

	private LogProtocol log() {
		return new LogProtocol(ImportDataDialog.class);
	}

	static final KnowledgeBaseConfiguration loadKbConfig() {
		try {
			KnowledgeBaseFactoryConfig serviceConfiguration =
				(KnowledgeBaseFactoryConfig) ApplicationConfig.getInstance()
					.getServiceConfiguration(KnowledgeBaseFactory.class);
			KnowledgeBaseConfiguration kbConfig =
				serviceConfiguration.getKnowledgeBases().get(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME);
			return kbConfig;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	private static Reader reader(File sqlFile) throws IOException {
		return new InputStreamReader(new GZIPInputStream(new FileInputStream(sqlFile)), "utf-8");
	}

	private static Writer writer(File sqlFile) throws IOException {
		return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(sqlFile)), "utf-8");
	}

	private static InputStream input(BinaryData data) throws IOException {
		InputStream in = data.getStream();
		if (data.getName().endsWith(".gz")) {
			return gunzip(in);
		}
		return in;
	}

	private static InputStream gunzip(InputStream in) throws IOException {
		try {
			return new GZIPInputStream(in);
		} catch (Throwable ex) {
			in.close();
			throw ex;
		}
	}

}
