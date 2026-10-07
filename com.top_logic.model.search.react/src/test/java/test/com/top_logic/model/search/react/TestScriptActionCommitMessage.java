/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.react;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.react.EvaluateDynamicScriptAction;
import com.top_logic.model.search.react.EvaluateScriptAction;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.Resources;

/**
 * Tests the commit messages of the changes {@link EvaluateScriptAction} and
 * {@link EvaluateDynamicScriptAction} perform.
 *
 * <p>
 * Each test asserts the message stored with the revision the change is committed in. The tests use
 * the type {@value #ITEM} of the test model of this module.
 * </p>
 */
public class TestScriptActionCommitMessage extends AbstractSearchExpressionTest {

	/** The test type the scripts change. */
	private static final String ITEM = "TestScriptActions:Item";

	/** The TL-Script literal of the name attribute of an {@link #ITEM}. */
	private static final String NAME_ATTRIBUTE = "`" + ITEM + "#name`";

	/** The name of the item before a change, which the commit message names. */
	private static final String ORIGINAL = "item";

	/** Tag name of a {@link GenericViewCommand} in the test configurations. */
	private static final String COMMAND = "generic-command";

	/** Tag name of an {@link EvaluateScriptAction} in the test configurations. */
	private static final String EVALUATE = "evaluate-script";

	/** The English label of the command in the test configurations. */
	private static final String LABEL = "Rename";

	/** A script renaming its argument. */
	private static final String RENAME = "x -> $x.set(" + NAME_ATTRIBUTE + ", 'renamed')";

	private ViewContext _context;

	private TLObject _item;

	private Person _formerUser;

	private String _formerContextId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext context = TLContext.getContext();
		_formerUser = context.getCurrentPersonWrapper();
		_formerContextId = context.getContextId();
		becomeUser(PersonManager.getManager().getRoot());

		_context = new DefaultViewContext(null);
		_item = newObject(ITEM, ORIGINAL);
	}

	@Override
	protected void tearDown() throws Exception {
		_item = null;
		_context = null;
		super.tearDown();

		TLContext context = TLContext.getContext();
		context.setCurrentPerson(_formerUser);
		if (_formerUser == null) {
			// Without a person, the context is identified by its explicit context id.
			context.setContextId(_formerContextId);
		}
	}

	/**
	 * Without a configured message, an evaluation is committed with a message naming the label of
	 * the command and the input of the script.
	 */
	public void testEvaluateDefault() throws Exception {
		GenericViewCommand command = (GenericViewCommand) instantiate(COMMAND, GenericViewCommand.Config.class,
			"<" + COMMAND + "><label><en>" + LABEL + "</en></label><" + EVALUATE + " script=\"" + RENAME + "\"/></"
				+ COMMAND + ">");

		command.execute(_context, _item);

		assertEquals("renamed", _item.tValueByName("name"));
		assertLastMessage("Performed operation \"" + LABEL + "\" on \"" + ORIGINAL + "\".");
	}

	/**
	 * A configured message is filled with the label of the input of the script.
	 */
	public void testEvaluateCustom() throws Exception {
		ViewAction action = (ViewAction) instantiate(EVALUATE, EvaluateScriptAction.Config.class,
			"<" + EVALUATE + " script=\"" + RENAME + "\"><commitMessage><en>Renamed {0}</en></commitMessage></"
				+ EVALUATE + ">");

		action.execute(_context, _item);

		assertEquals("renamed", _item.tValueByName("name"));
		assertLastMessage("Renamed " + ORIGINAL);
	}

	/**
	 * A script of the TL-Script console is committed with a message naming the script.
	 */
	public void testConsoleDefault() throws Exception {
		EvaluateDynamicScriptAction.Config config =
			TypedConfiguration.newConfigItem(EvaluateDynamicScriptAction.Config.class);
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestScriptActionCommitMessage.class);
		ViewAction console = context.getInstance(config);
		context.checkErrors();

		String source = "all(`" + ITEM + "`).foreach(" + RENAME + ")";
		console.execute(_context, source);

		assertEquals("renamed", _item.tValueByName("name"));
		assertLastMessage("Executed custom script: " + source);
	}

	private static Object instantiate(String rootTag, Class<?> rootConfig, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestScriptActionCommitMessage.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(rootTag, TypedConfiguration.getConfigurationDescriptor(rootConfig));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) reader.read();
		context.checkErrors();
		Object result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static void assertLastMessage(String expected) {
		HistoryManager history = kb().getHistoryManager();
		ResKey log = history.getRevision(history.getLastRevision()).getLog();
		assertEquals(expected, Resources.getInstance(Locale.ENGLISH).getString(log));
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(KBSetup.getSingleKBTest(TestScriptActionCommitMessage.class,
			ServiceTestSetup.createStarterFactoryForModules(getModules(
				TypeIndex.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE))));
	}

}
