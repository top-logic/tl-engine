/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.basic.Configuration.IterableConfiguration;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMain;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DatabaseContent;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.basic.util.ComputationEx2;

/**
 * Command line tool for {@link KnowledgeBase} operations on the command line.
 * 
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class DBMain extends XMain {

	/** The Writer evtually used to create the output */
	protected PrintWriter _out;

	/**
	 * Empty CTor for the Commandline.
	 */
	public DBMain() {
		section = PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME;
	}

	public DBMain(String section) {
		super(section);
	}

	/**
	 * Constructor using Properties.
	 */
	public DBMain(Properties prop) {
		super(!INTERACTIVE);
		section = PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME;
		properties    = prop;
	}

	/**
	 * Constructor using XMLProperties.
	 * 
	 * @param section
	 *            part of XMLProperties to extract, usually
	 *            "Knowledbase&lt;h&gt;".
	 */
	public DBMain(XMLProperties xProp, String section) {
		super(!INTERACTIVE);
		Properties tmpProps = xProp.getProperties(section);
		IterableConfiguration iConf = new IterableConfiguration(tmpProps);
		properties = iConf.getProperties();
	}

	/** This is called by showHelp to show the possible options */
	@Override
	protected void showHelpOptions() {
		super.showHelpOptions();
        info("\t-o | --output <file> Write output to this file intead of Database.");
        info("\tDefault is:");
		info("\t-m <path-to-webapp-root>" + ModuleLayoutConstants.META_CONF_RESOURCE + " -s KnowledgeBase0");
	}

	/** This is called by showHelp to show the possible arguments */
	@Override
	protected void showHelpArguments() {
		super.showHelpArguments();
        info("\tcreateTables     - Create Tables as given by MetaData");
        info("\tdropTables       - Remove Tables created by createTables");
        info("\tinit             - Import Data from XML-File specified in Properties");
        info("\tcheck            - Check if MetaData matched Current Database");
        info("\tdump <filename>  - Extract the DB Data as XML");
        info("\tsql <filename>   - Extract the DB Data as SQL-Inserts");
        info("\tload <filename>  - Load Data as XML (Metadata will -NOT- work)");
        info("");
        info("\t<dsname> is usually something like");
        info("\t  toplogicsystem://kbases/KBStorage.xml");
	}

	/**
	 * Care about the --output option.
	 */
	@Override
	protected int longOption(String option, String[] args, int i) {
		if ("output".equals(option)) {
			setOutputFile(args[i++]);
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

    /** Set the OutputFile form the -o/--output option */
	protected void setOutputFile(String fname) {
		try {
			if (_out != null)
				_out.close();
			_out = new PrintWriter(new FileWriter(fname));
		} catch (IOException iox) {
			iox.printStackTrace();
		}
	}

	/**
	 * Care about the -o[utput] option.
	 */
	@Override
	protected int shortOption(char c, String[] args, int i) {
		switch (c) {
    		case 'o':
    			setOutputFile(args[i++]);
    			break;
    		default:
    			return super.shortOption(c, args, i);
		}
		return i;
	}

	@Override
	protected int parameter(String args[], int i) throws Exception {
		// Deactivate automatic table setup, because this class creates the tables.
		System.setProperty(InitialTableSetup.NO_DATABASE_TABLE_SETUP, String.valueOf(true));
		String arg = args[i];
		if (arg.equals("createTables")) {
			doCreateTables(_out);
		} else if (arg.equals("dropTables")) {
			doDropTables(_out);
		} else {
			return super.parameter(args, i);
		}

		return i + 1;
	}

	CreateTablesContext newCreateContext() {
		CreateTablesContext context = new CreateTablesContext(getProtocol(), section);
		context.setInteractive(interactive);
		return context;
	}

	DropTablesContext newDropContext() {
		DropTablesContext context = new DropTablesContext(getProtocol(), section);
		context.setInteractive(interactive);
		return context;
	}

	/**
	 * Eventually close Output file.
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (_out != null)
			_out.close();
	}

	/**
	 * Acessor to set the interactive.Flag.
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * Entry point for the VM.
	 */
	public static void main(String[] args) throws Exception {
		new DBMain().runMainCommandLine(args);
	}

	/**
	 * Delegates to {@link DBSetupActions#createTables(CreateTablesContext)}.
	 */
	private void doCreateTables(final PrintWriter out) throws Exception {
		final AtomicReference<Map<String, DatabaseContent>> databaseContent =
			new AtomicReference<>(Collections.<String, DatabaseContent> emptyMap());
		BinaryData h2InMemFile = null;
		if (out != null) {
			initFileManager();
			h2InMemFile = FileManager.getInstance().getData("/WEB-INF/conf/devel-setupWithH2.config.xml");
			addAdditionalConfig(h2InMemFile);
		}
		inContext(new ComputationEx<Void, Exception>() {
			@Override
			public Void run() throws Exception {
				CreateTablesContext createContext = DBMain.this.newCreateContext();
				DBSetupActions.newInstance().createTables(createContext);
				if (out != null) {
					KnowledgeBase kb = createContext.getKnowledgeBase();
					SchemaSetup schemaSetup = kb.getSchemaSetup();
					ConnectionPool pool = createContext.getConnectionPool();
					PooledConnection connection = pool.borrowReadConnection();
					try {
						DBSchema dbSchema = schemaSetup.buildSchema(kb.getMORepository());
						Map<String, DatabaseContent> data = DBSchemaUtils.getData(connection, dbSchema);
						databaseContent.set(data);
					} finally {
						pool.releaseReadConnection(connection);
					}
				}
				return null;
			}
		});

		if (out != null) {
			removeAdditionalConfig(h2InMemFile);
			addAdditionalConfig(
				FileManager.getInstance().getData("/WEB-INF/conf/devel-connectionPoolDryRun.config.xml"));
			XMLProperties.restartXMLProperties(getXMLPropertiesConfig());
			inContext(new ComputationEx<Void, Exception>() {

				@Override
				public Void run() throws Exception {
					KnowledgeBase kb = DBMain.this.newCreateContext().getKnowledgeBase();
					kb.getSchemaSetup().printCreateTables(out, kb.getMORepository(),
						KBUtils.getConnectionPool(kb).getSQLDialect());

					DBHelper outputDialect = ConnectionPoolRegistry.getDefaultConnectionPool().getSQLDialect();
					for (DatabaseContent content : databaseContent.get().values()) {
						outputDialect.dumpAsInsert(out, content);
					}
					return null;
				}

			});
		}
	}

	/**
	 * Delegates to {@link DBSetupActions#dropTables(DropTablesContext, PrintWriter)}.
	 */
	private void doDropTables(final PrintWriter out) throws Exception {
		inContext(new ComputationEx<Void, Exception>() {
			@Override
			public Void run() throws Exception {
				DBSetupActions.newInstance().dropTables(DBMain.this.newDropContext(), out);
				return null;
			}
		});
	}

	private <E1 extends Exception, E2 extends Exception> void inContext(ComputationEx2<Void, E1, E2> computation)
			throws E1, E2 {
		initXMLProperties();

		ThreadContext.inSystemContextWithModules(DBMain.class, computation, ConnectionPoolRegistry.Module.INSTANCE);
	}

	/**
	 * The current dump output.
	 */
	public PrintWriter getOut() {
		return _out;
	}

}
