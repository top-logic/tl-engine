/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.load;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
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

	private KBLoad _load;

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
			_load = null;
			disableButtons();
			return true;
		}

		KBLoad load;
		try {
			load = KBLoad.open(log(), data);
		} catch (TopLogicException ex) {
			throw new CheckException(Resources.getInstance().getString(ex.getErrorKey()));
		}
		_load = load;

		if (load.hasDataVersion()) {
			_versionField.setValue(KBLoad.toText(load.getDataVersions()));
		} else {
			_versionField.setValue(Resources.getInstance().getString(I18NConstants.NO_DATA_VERSION));
		}

		String migrated = KBLoad.toText(load.getMigratedVersions());
		if (migrated.isEmpty()) {
			_migrationField.setValue(Resources.getInstance().getString(I18NConstants.NO_MIGRATIONS));
		} else {
			_migrationField.setValue(migrated);
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
		PersistencyMaintenance.requirePreconditions();
		return createImportProgress().open(context);
	}

	private HandlerResult doDownloadSql(DisplayContext context) {
		return createDownloadProgress(_load::createSqlScript).open(context);
	}

	private HandlerResult doDownloadLoaderZip(DisplayContext context) {
		return createDownloadProgress(_load::createLoaderScript).open(context);
	}

	private ProgressDialog createDownloadProgress(Function<Log, BinaryData> conversion) {
		return new ProgressDialog(I18NConstants.DOWNLOAD_SQL, px(400), px(300)) {
			private BinaryData _result;

			@Override
			public void run(I18NLog log) {
				_result = conversion.apply(log.asLog());
			}

			@Override
			public void handleCompleted(DisplayContext context) {
				context.getWindowScope().deliverContent(_result);
			}
		};
	}

	private ProgressDialog createImportProgress() {
		LogProtocol importLog = log();
		KBLoad load = _load;

		return new ProgressDialog(I18NConstants.DOWNLOAD_SQL, px(400), px(300)) {
			private BinaryData _sqlScript;

			@Override
			protected void run(I18NLog log) throws AbortExecutionException {
				_sqlScript = load.createSqlScript(log.asLog());
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
				KBLoad.replaceDatabase(importLog, _sqlScript);

				// Direct logout to prevent errors from being transported back to the UI.
				context.asRequest().getSession(false).invalidate();

				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	final BinaryData getDumpData() {
		return _dumpField.getDataItem();
	}

	private LogProtocol log() {
		return new LogProtocol(ImportDataDialog.class);
	}

}
