/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.filter.SimpleComparableFilterProvider;
import com.top_logic.layout.table.filter.SimpleDateFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.FileUploadComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.commandhandlers.NonClosingDialogUploadHandler;

/**
 * This class is a file upload component as step of an assistent. The uploaded files 
 * are stored in the assistant data (key: {@link #FILE_FOLDER}.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class AssistantMultiFileUploadComponent extends FileUploadComponent {

	/**
	 * Configuration for the {@link AssistantMultiFileUploadComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FileUploadComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@StringDefault(NonClosingDialogUploadHandler.COMMAND)
		String getUploadHandlerName();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FileUploadComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	final class NameAccessor extends ReadOnlyAccessor<File> {
		@Override
		public Object getValue(File file, String property) {
			return file.getName();
		}
	}

	final class LastModifiedAccessor extends ReadOnlyAccessor<File> {
		@Override
		public Object getValue(File file, String property) {
			return new Date(file.lastModified());
		}
	}

	final class SizeAccessor extends ReadOnlyAccessor<File> {
		@Override
		public Object getValue(File file, String property) {
			return Long.valueOf(file.length());
		}
	}

	final class TypeAccessor extends ReadOnlyAccessor<File> {
		@Override
		public Object getValue(File file, String property) {
			MimeTypes theMime = MimeTypes.getInstance();
			String theType = theMime.getMimeType(file.getName());
			return (theMime.getDescription(theType));
		}
	}

	/** The attribute for displaying the name of a file. */
	private static final String NAME = "name";

	/** The attribute for displaying the size of a file. */
	private static final String SIZE = "size";

	/** The attribute for displaying the last changed date of a file. */
	private static final String DATE = "date";

	/** The attribute for displaying the last changed date of a file. */
	private static final String TYPE = "type";

	private static final String[] COLUMN_NAMES = new String[] { NAME, TYPE, DATE, SIZE };

    /** The key of the assistant data map where the files are stored. */
    public static final String FILE_FOLDER = "FILES";

	public static final String UPLOAD_COMMAND_FIELD = "uploadCommandField";

	/**
	 * Name of the table field containing the files.
	 */
	public static final String FILE_TABLE_FIELD_NAME = "fileTable";

	/**
	 * Name of the {@link TableConfiguration} to configure table field in XML file.
	 */
	public static final String FILE_TABLE_CONFIG = FILE_TABLE_FIELD_NAME;

    /** The temporary folder for storing the documents during assistent mode. */
    private File folder;

    /**
	 * The layout framework uses this constructor to create new instances of
	 * this class.
	 */
    public AssistantMultiFileUploadComponent(InstantiationContext context, Config atts) throws ConfigurationException {
    	super(context, atts);
    }

    @Override
	public FormContext createFormContext() {
        FormContext theContext = super.createFormContext();
        SelectField languageField = createLanguageField();
        if (languageField != null) {
            theContext.addMember(languageField);
        }
        
        // Render button for upload current selected file
		final CommandHandler uploadCommand = getCommandById(getUploadHandlerName());
		CommandField upload = FormFactory.newCommandField(UPLOAD_COMMAND_FIELD, uploadCommand, this);
        // store is taken from assistantMulitFileUploadPage.jsp
        upload.setLabel(getResString("store"));
        theContext.addMember(upload);
		theContext.addMember(newTableField(FILE_TABLE_FIELD_NAME));
        
        return theContext;
    }

    @Override
	public boolean getUseChangeHandling() {
        return false;
    }

    /**
	 * Store the file from the FileUploadItem to a temporary folder.
	 * 
	 * The folder will then be put into the assistent data map. If this method will be called more
	 * than one time, the new files will be located in that folder too.
	 * 
	 * @see com.top_logic.knowledge.gui.layout.upload.UploadAware#receiveFile(BinaryData)
	 */
    @Override
	public void receiveFile(BinaryData aFileItem) throws IOException {
		String fileName = aFileItem.getName();
        AssistentComponent component = AssistentComponent.getEnclosingAssistentComponent(this);

        final FormContext formContext = getFormContext();
		if (this.checkFileName(fileName) == null) {
			File file = this.getFile(aFileItem, null);
            
            if (formContext.hasMember(FIELD_NAME_LANGUAGE)) { 
            	final SelectField languageField = (SelectField) formContext.getMember(FIELD_NAME_LANGUAGE);
            	String  language  = (String) languageField.getSingleSelection();
                component.setData(fileName, language);
            }

			component.setData(AssistantMultiFileUploadComponent.FILE_FOLDER, file);
        }
        else {
            component.setData(AssistentComponent.SHOW_FAILTURE, Boolean.TRUE);
        }

		updateFiles(getTableData(FILE_TABLE_FIELD_NAME));
    }

	private TableData getTableData(String fieldName) {
		return (TableField) getFormContext().getMember(fieldName);
	}

	private void updateFiles(TableData tableData) {
		List<File> files;
		try {
			files = Arrays.asList(FileUtilities.listFiles(this.getFolder()));
		} catch (IOException ex) {
			Logger.error("Cannot read upload folder.", ex, AssistantMultiFileUploadComponent.class);
			files = Collections.emptyList();
		}

		((EditableRowTableModel) tableData.getTableModel()).setRowObjects(files);
	}

	private TableField newTableField(String fieldName) {
		TableConfiguration config = createTableConfiguration();
		ConfigKey configKey = ConfigKey.part(this, "fileTable");
		TableField tableField = FormFactory.newTableField(fieldName, configKey);
		tableField.setSelectable(true);
		ObjectTableModel tableModel = new ObjectTableModel(COLUMN_NAMES, config, new ArrayList<>());
		tableField.setTableModel(tableModel);
		return tableField;
	}

	private TableConfiguration createTableConfiguration() {
		TableConfigurationProvider configuredTable = lookupTableConfigurationBuilder(FILE_TABLE_FIELD_NAME);
		TableConfigurationProvider provider;
		if (configuredTable == null) {
			provider = createProvider();
		} else {
			provider = TableConfigurationFactory.combine(createProvider(), configuredTable);
		}

		return TableConfigurationFactory.build(provider);
	}

	/**
	 * Creates the {@link TableConfigurationProvider}.
	 */
	protected TableConfigurationProvider createProvider() {
		return new TableConfigurationProvider() {

			@Override
			public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
				// Do nothing
			}

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(getResPrefix());
				table.declareColumn(NAME).setAccessor(new NameAccessor());
				ColumnConfiguration sizeCol = table.declareColumn(SIZE);
				sizeCol.setAccessor(new SizeAccessor());
				sizeCol.setFilterProvider(SimpleComparableFilterProvider.INSTANCE);
				table.declareColumn(TYPE).setAccessor(new TypeAccessor());
				ColumnConfiguration dateCol = table.declareColumn(DATE);
				dateCol.setAccessor(new LastModifiedAccessor());
				dateCol.setFilterProvider(SimpleDateFilterProvider.INSTANCE);

				table.setDefaultSortOrder(Collections.singletonList(SortConfigFactory.ascending(NAME)));
			}
		};
	}

	/**
     * Cleanup tmp-Folder when FromContext is st to null.
     */
	@Override
	public synchronized void removeFormContext() {
		super.removeFormContext();
		if (this.folder != null) {
			File[] files = this.folder.listFiles();

			if (files != null) {
				for (int thePos = 0; thePos < files.length; thePos++) {
					files[thePos].delete();
				}
                
			}

			this.folder.delete();

			this.folder = null;
        }
	}
    
    /**
	 * This method returns the tmp folder where the documents are uploaded. If
	 * no folder exists a new folder will be created.
	 */
    public File getFolder() throws IOException {
        if (this.folder == null) {
			File theFolder = File.createTempFile("upload", "tmp", Settings.getInstance().getTempDir());

            theFolder.delete();

            if (theFolder.mkdirs()) {
                this.folder = theFolder;
            }
            else {
                throw new IOException("Cannot create folder " + theFolder);
            }
        }

        return (this.folder);
    }

    
    /**
	 * This method returns the file for the given file item and folder.
	 * 
	 * @param aFileItem
	 *            The file item is NEVER <code>null</code>.
	 * @param aFolder
	 *            The folder.
	 */
	private File getFile(BinaryData aFileItem, File aFolder) throws IOException {
		String theName = aFileItem.getName();
		try (InputStream theStream = aFileItem.getStream()) {
			File theFolder = (aFolder != null) ? aFolder : this.getFolder();
			File theDest = new File(theFolder, theName);

			FileUtilities.copyToFile(theStream, theDest);

			return (theFolder);
		}
    }
}
