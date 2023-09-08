/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;

import com.google.inject.name.Named;

import com.top_logic.basic.Environment;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.StackedEventWriter;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig.Step;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig.TableTransformerStep;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;
import com.top_logic.knowledge.service.db2.migration.db.TableContent;
import com.top_logic.knowledge.service.db2.migration.db.transformers.StackedRowTransformer;
import com.top_logic.knowledge.service.db2.migration.sql.InsertWriter;
import com.top_logic.knowledge.service.db2.migration.sql.SQLDumper;
import com.top_logic.knowledge.service.db2.migration.sql.SQLLoaderInsertWriter;
import com.top_logic.knowledge.service.migration.MigrationService;
import com.top_logic.util.model.ModelService;

/**
 * {@link Tool} that loads a knowledge base dump, applies a migration and creates SQL for inserting
 * the data.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DumpLoader extends Tool {

	private static final String GZIP_CONTENT_TYPE = "application/gzip";

	/**
	 * Option specifying the name of the dump XML file to load.
	 */
	public static final String OPTION_IN = "in";

	/**
	 * Option specifying the name of migration configuration file.
	 */
	public static final String MIGRATION_IN = "migrationConf";

	/**
	 * Option specifying that the input XML is GZip compressed.
	 * 
	 * <p>
	 * This is the default, if the input file name ends with ".gz".
	 * </p>
	 */
	public static final String OPTION_GZIP = "gzip";

	/**
	 * Option specifying the SQL file name of the dump.
	 */
	public static final String OPTION_OUT = "out";

	/**
	 * Option specifying encoding of the output SQL file contents.
	 */
	public static final String OPTION_ENCODING = "encoding";

	/**
	 * Option specifying the tables of the target DB not to load.
	 */
	public static final String OPTION_NO_TABLE_LOAD = "no-table-load";

	/**
	 * Option to configure the maximum numbers of rows to insert within one statement.
	 */
	public static final String INSERT_CHUNK_SIZE = MigrationService.Config.INSERT_CHUNK_SIZE;

	/**
	 * Option to configure the maximal number of items to hold in cache before the insert SQL is
	 * created.
	 */
	public static final String BUFFER_SIZE = MigrationService.Config.BUFFER_SIZE;

	/**
	 * Name of the injection parameter that announces the start revision to the configured
	 * {@link EventRewriter}s.
	 * 
	 * @see Named
	 */
	public static final String INJECTION_START_REVISION = "startRevision";

	private static final String GZIP_EXTENSION = ".gz";

	private BinaryContent _migrationScript;

	private BinaryContent _inputData;

	private File _dumpFile;

	private String _encoding = "utf-8";

	private int _insertChunkSize = SQLDumper.DEFAULT_INSERT_CHUNK_SIZE;

	private int _bufferSize = SQLDumper.DEFAULT_BUFFER_SIZE;

	private Set<String> _noTableLoad = Collections.emptySet();

	private boolean _gzip = false;

	volatile Date _canceled = null;

	/**
	 * Creates a {@link DumpLoader}.
	 * 
	 */
	public DumpLoader() {
		setInputDataFile("dump.xml");
		setMigrationScriptFile("migration.xml");
		setDumpFile(new File("dump.sql"));
	}

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (OPTION_IN.equals(option)) {
			setInputDataFile(args[i++]);
			return i;
		}
		if (OPTION_GZIP.equals(option)) {
			setGzip(true);
			return i;
		}
		if (OPTION_OUT.equals(option)) {
			setDumpFile(new File(args[i++]));
			return i;
		}
		if (MIGRATION_IN.equals(option)) {
			setMigrationScriptFile(args[i++]);
			return i;
		}
		if (OPTION_ENCODING.equals(option)) {
			setEncoding(args[i++]);
			return i;
		}
		if (OPTION_NO_TABLE_LOAD.equals(option)) {
			setNoTableLoad(new HashSet<>(Arrays.asList(args[i++].split("\\s*,\\s*"))));
			return i;
		}
		if (INSERT_CHUNK_SIZE.equals(option)) {
			setInsertChunkSize(Integer.parseInt(args[i++]));
			return i;
		}
		if (BUFFER_SIZE.equals(option)) {
			setBufferSize(Integer.parseInt(args[i++]));
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void runTool() throws Exception {
		migrate();
	}

	private void migrate() throws Exception {
		Protocol log = getProtocol();
		StopWatch sw = StopWatch.createStartedWatch();
		log.info("Migration started.");
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dumpFile()), getEncoding())) {
			tryMigrate(log, writer);
		}
		log.info("Migration finished in " + sw + ".");
	}

	private void tryMigrate(Protocol log, OutputStreamWriter writer) throws Exception {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		MigrationImpl migration = new MigrationImpl(log);
		GuiceContext context = new GuiceContext(new DefaultInstantiationContext(log));
		MORepository typeRepository = new MigrationRepository(kb);
		context.bind(MORepository.class, typeRepository);
		context.bind(KnowledgeBase.class, kb);
		context.bind(ModelService.class, ModelService.getInstance());
		context.bind(Migration.class, migration);
		context.bind(Log.class, migration);
		context.bind(Long.class, INJECTION_START_REVISION, kb.getHistoryManager().getLastRevision() + 1);

		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		DBHelper sqlDialect = pool.getSQLDialect();
		try (InsertWriter insertWriter = useSqlLoader() ? new SQLLoaderInsertWriter(sqlDialect)
			: SQLDumper.createInsertWriter(sqlDialect, insertChunkSize(), writer)) {

			insertWriter.beginDump();
			try (SQLDumper sqlDumper = new SQLDumper(migration, pool, typeRepository, bufferSize(), insertWriter)) {
				sqlDumper.loadCurrent(kb, getNoTableLoad());
				ConfigurationDescriptor migrationDescriptor =
					TypedConfiguration.getConfigurationDescriptor(ReplayConfig.class);
				ConfigurationReader configReader =
					new ConfigurationReader(context, Collections.singletonMap(ReplayConfig.TAG_NAME,
						migrationDescriptor));
				ReplayConfig migrationConfig = (ReplayConfig) configReader.setSource(migrationScript()).read();

				// Creating rewriter for versioned types.
				Collection<Step> rewriteSteps = migrationConfig.getSteps().values();
				List<EventRewriter> rewriters = new ArrayList<>();
				for (Step step : rewriteSteps) {
					rewriters.addAll(TypedConfiguration.getInstanceList(context, step.getRewriters()));
				}
				EventWriter versionedWriter = StackedEventWriter.createWriter(0, sqlDumper, rewriters);

				// Creating rewriter for unversioned types.
				EventWriter unversionedOut = sqlDumper.getUnversionedWriter();
				EventWriter unversionedWriter = StackedEventWriter.createWriter(0, unversionedOut, rewriters);

				// Creating rewriter for table types.
				Collection<TableTransformerStep> transformerSteps = migrationConfig.getTransformerSteps();
				List<RowTransformer> transformers = new ArrayList<>();
				for (TableTransformerStep step : transformerSteps) {
					transformers.addAll(TypedConfiguration.getInstanceList(context, step.getTransformers()));
				}

				context.inject();

				RowWriter tableOut = sqlDumper.getRowWriter();
				RowWriter tableWriter = StackedRowTransformer.createRowWriter(transformers, tableOut);

				try (InputStream in = getDumpInputStream();
						DumpReader reader = new DumpReader(migrationConfig.getTypeMapping(), in)) {
					if (checkCanceled(log)) {
						return;
					}
					log.info("Processing change sets.");
					writeChangeSets(migration, reader, versionedWriter);
					log.info("Change sets processed.");

					if (checkCanceled(log)) {
						return;
					}
					log.info("Processing unversioned types.");
					writeUnversiondTypes(log, reader, unversionedWriter);
					log.info("Unversioned types processed.");

					if (checkCanceled(log)) {
						return;
					}
					log.info("Processing non KB tables.");
					writeRows(log, reader, tableWriter);
					log.info("Non KB tables processed.");
				}
			}
			insertWriter.endDump();
		}
	}

	private boolean useSqlLoader() {
		return Environment.getSystemPropertyOrEnvironmentVariable(SQLLoaderInsertWriter.USE_SQL_LOADER_SYSTEM_PROPERTY,
							false);
	}

	private boolean checkCanceled(Log log) {
		if (_canceled != null) {
			log.info("dump load was canceled at " + _canceled + ".", Protocol.WARN);
			return true;
		} else {
			return false;
		}
	}

	private InputStream getDumpInputStream() throws FileNotFoundException, IOException {
		InputStream directIn = inputData().getStream();
		InputStream in;
		if (isGZIP()) {
			in = new GZIPInputStream(directIn);
		} else {
			in = directIn;
		}
		return in;
	}

	private boolean isGZIP() {
		if (isGzip()) {
			return true;
		}
		BinaryContent inputData = inputData();
		if (inputData instanceof BinaryDataSource) {
			BinaryData data = BinaryData.cast(inputData);
			return data.getName().endsWith(GZIP_EXTENSION)
				|| data.getContentType().equals(GZIP_CONTENT_TYPE);
		}
		return false;
	}

	private void writeRows(Log log, DumpReader reader, RowWriter tableWriter) {
		Iterator<TableContent> tables = reader.getTables();
		while (tables.hasNext()) {
			TableContent table = tables.next();
			Iterator<RowValue> rows = table.getRows();
			while (rows.hasNext()) {
				tableWriter.write(rows.next());
			}
			if (checkCanceled(log)) {
				break;
			}
		}
	}

	private void writeUnversiondTypes(Log log, DumpReader reader, EventWriter writer) {
		long eventRev = Revision.CURRENT_REV;
		Iterator<UnversionedCreations> unversioned = reader.getUnversionedObjects(eventRev);
		ChangeSet cs = new ChangeSet(eventRev);
		cs.setCommit(new CommitEvent(eventRev, "migration", System.currentTimeMillis(),
			"Migration for unversioned types"));

		StopWatch readWatch = new StopWatch(), writeWatch = new StopWatch();
		readWatch.start();
		while (unversioned.hasNext()) {
			UnversionedCreations unversionedType = unversioned.next();
			Iterator<ObjectCreation> items = unversionedType.getItems();
			while (items.hasNext()) {
				cs.addCreation(items.next());
			}
			if (checkCanceled(log)) {
				break;
			}
		}
		readWatch.stop();
		writeWatch.start();
		writer.write(cs);
		writeWatch.stop();
		log.info(
			"Processing unversioned types needed " + readWatch + " for reading and " + writeWatch + " for writing.");
	}

	private void writeChangeSets(MigrationImpl migration, DumpReader reader, EventWriter writer) {
		long heartBeat_ms = 10000;
		int longCSTime_ms = 1000;

		AtomicLong lastLogTime = new AtomicLong(0);
		AtomicInteger count = new AtomicInteger(0);
		SubSessionContext subSession = ThreadContextManager.getSubSession();
		ScheduledFuture<?> schedule = SchedulerService.getInstance().scheduleWithFixedDelay(() -> {
			ThreadContextManager.inContext(subSession, () -> {
				long now = System.currentTimeMillis();
				if (now - lastLogTime.get() > heartBeat_ms) {
					lastLogTime.set(now);
					migration.info("Processing change set number " + count.get());
				}
			});
		}, heartBeat_ms, heartBeat_ms, TimeUnit.MILLISECONDS);
		try {
			Iterator<ChangeSet> changes = reader.getChangeSets();
			StopWatch readWatch = new StopWatch(), writeWatch = new StopWatch();
			while (changes.hasNext()) {
				count.incrementAndGet();
				readWatch.start();
				ChangeSet cs = changes.next();
				readWatch.stop();
				migration.setChangeSet(cs);
				writeWatch.start();
				writer.write(cs);
				writeWatch.stop();
				long allOverTime = readWatch.getElapsedMillis() + writeWatch.getElapsedMillis();
				if (allOverTime > longCSTime_ms) {
					lastLogTime.set(System.currentTimeMillis());
					StringBuilder msg = new StringBuilder();
					msg.append("Processing change set needed ");
					msg.append(StopWatch.toStringMillis(allOverTime));
					msg.append(". Read: ");
					msg.append(readWatch);
					msg.append(", write: ");
					msg.append(writeWatch);
					migration.info(msg.toString());
				}
				readWatch.reset();
				writeWatch.reset();
				if (checkCanceled(migration)) {
					return;
				}
			}
			migration.info("Processed " + count + " change sets.");
		} finally {
			schedule.cancel(false);
		}
	}

	/**
	 * The {@link BinaryContent} containing the description of the migration steps to use.
	 */
	public BinaryContent migrationScript() {
		return _migrationScript;
	}

	/**
	 * Uses the {@link File} with the given name as {@link #migrationScript()}.
	 */
	public void setMigrationScriptFile(String fileName) {
		setMigrationScript(BinaryDataFactory.createBinaryData(new File(fileName)));
	}

	/**
	 * Setter for {@link #migrationScript()}.
	 */
	public void setMigrationScript(BinaryContent migrationScript) {
		_migrationScript = migrationScript;
	}

	/**
	 * The {@link BinaryContent} containing the dump for migration.
	 */
	public BinaryContent inputData() {
		return _inputData;
	}

	/**
	 * Uses the {@link File} with the givne name as {@link #inputData()}.
	 */
	public void setInputDataFile(String fileName) {
		setInputData(BinaryDataFactory.createBinaryData(new File(fileName)));
	}

	/**
	 * Setter for {@link #inputData()}.
	 */
	public void setInputData(BinaryContent inputData) {
		_inputData = inputData;
	}

	/**
	 * Whether the {@link #inputData()} are in GZIP format.
	 * 
	 * @see #OPTION_GZIP
	 */
	public boolean isGzip() {
		return _gzip;
	}

	/**
	 * Setter for {@link #isGZIP()}.
	 */
	public void setGzip(boolean gzip) {
		_gzip = gzip;
	}

	/**
	 * Sets the encoding of {@link #dumpFile()}.
	 * 
	 * @see #OPTION_ENCODING
	 */
	public String getEncoding() {
		return _encoding;
	}

	/**
	 * Setter for {@link #getEncoding()}.
	 */
	public void setEncoding(String encoding) {
		_encoding = encoding;
	}

	/**
	 * Sets the names of the tables not to load.
	 * 
	 * @see #OPTION_NO_TABLE_LOAD
	 */
	public Set<String> getNoTableLoad() {
		return _noTableLoad;
	}

	/**
	 * Setter for {@link #getNoTableLoad()}
	 */
	public void setNoTableLoad(Set<String> noTableLoad) {
		_noTableLoad = noTableLoad;
	}

	/**
	 * The file to write the SQL dump to.
	 * 
	 * @see #OPTION_OUT
	 */
	public File dumpFile() {
		return _dumpFile;
	}

	/**
	 * Setter for {@link #dumpFile()}
	 */
	public void setDumpFile(File dumpFile) {
		_dumpFile = dumpFile;
	}

	/**
	 * The insert chunk size.
	 * 
	 * @see SQLDumper#SQLDumper(Log, ConnectionPool, MORepository, int, InsertWriter)
	 */
	public int insertChunkSize() {
		return _insertChunkSize;
	}

	/**
	 * Setter for {@link #insertChunkSize()}.
	 */
	public void setInsertChunkSize(int insertChunkSize) {
		_insertChunkSize = insertChunkSize;
	}

	/**
	 * The used buffer size.
	 * 
	 * @see SQLDumper#SQLDumper(Log, ConnectionPool, MORepository, int, InsertWriter)
	 */
	public int bufferSize() {
		return _bufferSize;
	}

	/**
	 * Setter for {@link #bufferSize()}.
	 */
	public void setBufferSize(int bufferSize) {
		_bufferSize = bufferSize;
	}

	/**
	 * Cancel loading of dump.
	 */
	public void cancelDumpLoad() {
		_canceled = new Date();
	}

	public static void main(String[] args) throws Exception {
		new DumpLoader().runMainCommandLine(args);
	}

}
