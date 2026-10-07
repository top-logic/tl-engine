/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DeleteObjectAction;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.PersistTransientAction;
import com.top_logic.layout.view.command.UploadCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommitMessage;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.Resources;

/**
 * Tests the commit messages of the changes the view actions perform, see {@link ViewCommitMessage}.
 *
 * <p>
 * Each test asserts the message stored with the revision the change is committed in.
 * </p>
 */
public class TestViewCommitMessage extends AbstractModelAccessTest {

	/** Name of the channel holding the container of a created object. */
	private static final String CHANNEL = "container";

	/** Tag name of a {@link GenericViewCommand} in the test configurations. */
	private static final String COMMAND = "generic-command";

	/** Tag name of an {@link UploadCommand} in the test configurations. */
	private static final String UPLOAD = "upload-command";

	/** Tag name of a {@link WithTransactionAction} in the test configurations. */
	private static final String TRANSACTION = "with-transaction";

	/** The English label of the command in the test configurations. */
	private static final String LABEL = "Rename";

	/** The command label element of the test configurations. */
	private static final String LABEL_XML = "<label><en>" + LABEL + "</en></label>";

	/**
	 * The label of the project before a change, which the commit message names: the message is
	 * built when the transaction begins.
	 */
	private static final String ORIGINAL = "project";

	/** The name the {@link #RENAME_XML} script gives the project. */
	private static final String RENAMED = "renamed";

	/** A script action renaming its input to {@value #RENAMED}. */
	private static final String RENAME_XML =
		"<execute-script function=\"x -> $x.set(`" + MODULE + ":" + PROJECT + "#" + NAME + "`, '" + RENAMED
			+ "')\"/>";

	private ViewContext _context;

	private ViewChannel _channel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(null);
		_channel = new DefaultViewChannel(CHANNEL);
		_context.registerChannel(CHANNEL, _channel);
		becomeUser(_root);
	}

	@Override
	protected void tearDown() throws Exception {
		_channel = null;
		_context = null;
		super.tearDown();
	}

	/**
	 * Without a configured message, a transaction is committed with a message naming the label of
	 * the command and the input of the transaction.
	 */
	public void testTransactionDefault() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + ">" + LABEL_XML + "<" + TRANSACTION + ">" + RENAME_XML + "</" + TRANSACTION + "></"
				+ COMMAND + ">");

		command.execute(_context, _project);

		assertEquals(RENAMED, _project.tValueByName(NAME));
		assertLastMessage("Performed operation \"" + LABEL + "\" on \"" + ORIGINAL + "\".");
	}

	/**
	 * A configured message is filled with the label of the input of the transaction.
	 */
	public void testTransactionCustom() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + ">" + LABEL_XML + "<" + TRANSACTION + "><commitMessage><en>Renamed {0}</en></commitMessage>"
				+ RENAME_XML + "</" + TRANSACTION + "></" + COMMAND + ">");

		command.execute(_context, _project);

		assertLastMessage("Renamed " + ORIGINAL);
	}

	/**
	 * The command is found through actions the transaction is nested in.
	 */
	public void testTransactionNested() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + ">" + LABEL_XML + "<if test=\"x -> true\"><then><" + TRANSACTION + ">" + RENAME_XML
				+ "</" + TRANSACTION + "></then></if></" + COMMAND + ">");

		command.execute(_context, _project);

		assertLastMessage("Performed operation \"" + LABEL + "\" on \"" + ORIGINAL + "\".");
	}

	/**
	 * An action run by an {@link UploadCommand} names the label of that command.
	 */
	public void testTransactionInUploadCommand() throws Exception {
		UploadCommand command = (UploadCommand) instantiate(UPLOAD, UploadCommand.Config.class,
			"<" + UPLOAD + ">" + LABEL_XML + "<" + TRANSACTION + ">" + RENAME_XML + "</" + TRANSACTION + "></"
				+ UPLOAD + ">");

		command.getActions().get(0).execute(_context, _project);

		assertLastMessage("Performed operation \"" + LABEL + "\" on \"" + ORIGINAL + "\".");
	}

	/**
	 * A command without a label leaves the operation unnamed.
	 */
	public void testTransactionUnlabeledCommand() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + "><" + TRANSACTION + ">" + RENAME_XML + "</" + TRANSACTION + "></" + COMMAND + ">");

		command.execute(_context, _project);

		assertLastMessage("Updated object: " + ORIGINAL);
	}

	/**
	 * A transaction created outside of a command names its input only, or nothing without input.
	 */
	public void testTransactionWithoutCommand() throws Exception {
		ViewAction transaction = action("<" + TRANSACTION + ">" + RENAME_XML + "</" + TRANSACTION + ">");

		transaction.execute(_context, _project);
		assertLastMessage("Updated object: " + ORIGINAL);

		ViewAction create = action("<" + TRANSACTION + "><execute-script function=\"x -> new(`" + MODULE + ":"
			+ CATEGORY + "`)\"/></" + TRANSACTION + ">");
		TLObject category = (TLObject) create.execute(_context, null);
		try {
			assertLastMessage("Performed changes.");
		} finally {
			delete(category);
		}
	}

	/**
	 * A deletion is committed with a message naming the deleted object, regardless of the command.
	 */
	public void testDeleteDefault() throws Exception {
		GenericViewCommand command = command("<" + COMMAND + ">" + LABEL_XML + "<delete-object/></" + COMMAND + ">");

		command.execute(_context, _task);

		assertFalse(_task.tValid());
		assertLastMessage("Deleted object: task");
	}

	/**
	 * A configured message of a deletion is filled with the label of the deleted object.
	 */
	public void testDeleteCustom() throws Exception {
		ViewAction delete = action("<delete-object><commitMessage><en>Removed {0}</en></commitMessage></delete-object>");

		delete.execute(_context, _task);

		assertLastMessage("Removed task");
	}

	/**
	 * The message of a transaction a deletion is nested in is the message of the commit.
	 */
	public void testDeleteInTransaction() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + ">" + LABEL_XML + "<" + TRANSACTION + "><delete-object/></" + TRANSACTION + "></"
				+ COMMAND + ">");

		command.execute(_context, _task);

		assertLastMessage("Performed operation \"" + LABEL + "\" on \"task\".");
	}

	/**
	 * A creation is committed with a message naming the created object.
	 */
	public void testPersistDefault() throws Exception {
		GenericViewCommand command = command(
			"<" + COMMAND + ">" + LABEL_XML + "<persist-transient container='" + CHANNEL + "' reference='" + TASKS
				+ "'/></" + COMMAND + ">");
		_channel.set(_project);

		command.execute(_context, draft("created"));

		assertEquals(2, ((List<?>) _project.tValueByName(TASKS)).size());
		assertLastMessage("Created object: created");
	}

	/**
	 * A configured message of a creation is filled with the label of the created object.
	 */
	public void testPersistCustom() throws Exception {
		ViewAction persist = action("<persist-transient container='" + CHANNEL + "' reference='" + TASKS
			+ "'><commitMessage><en>Added {0}</en></commitMessage></persist-transient>");
		_channel.set(_project);

		persist.execute(_context, draft("created"));

		assertLastMessage("Added created");
	}

	private TLObject draft(String name) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type(TASK));
		result.tUpdateByName(NAME, name);
		return result;
	}

	private static void assertLastMessage(String expected) {
		HistoryManager history = kb().getHistoryManager();
		ResKey log = history.getRevision(history.getLastRevision()).getLog();
		assertEquals(expected, Resources.getInstance(Locale.ENGLISH).getString(log));
	}

	private static GenericViewCommand command(String xml) throws ConfigurationException {
		return (GenericViewCommand) instantiate(COMMAND, GenericViewCommand.Config.class, xml);
	}

	private static ViewAction action(String xml) throws ConfigurationException {
		return (ViewAction) instantiate(null, null, xml);
	}

	private static Object instantiate(String rootTag, Class<?> rootConfig, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestViewCommitMessage.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		if (rootTag == null) {
			descriptors.put(TRANSACTION, descriptor(WithTransactionAction.Config.class));
			descriptors.put(DeleteObjectAction.Config.TAG_NAME, descriptor(DeleteObjectAction.Config.class));
			descriptors.put(PersistTransientAction.Config.TAG_NAME, descriptor(PersistTransientAction.Config.class));
		} else {
			descriptors.put(rootTag, descriptor(rootConfig));
		}
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) reader.read();
		context.checkErrors();
		Object result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static ConfigurationDescriptor descriptor(Class<?> configType) {
		return TypedConfiguration.getConfigurationDescriptor(configType);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestViewCommitMessage.class);
	}

}
