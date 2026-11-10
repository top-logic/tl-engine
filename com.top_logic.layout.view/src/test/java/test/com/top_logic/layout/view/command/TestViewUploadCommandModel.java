/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import static test.com.top_logic.layout.view.command.TestConfiguredActionArguments.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.UploadCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.command.ViewUploadCommandModel;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the upload of files through an {@link UploadCommand}: that uploading a file whose action
 * chain fails reports the failure to the user and continues with the files that follow, and that
 * the configured rules of the command decide over its inputs before any file exists.
 */
public class TestViewUploadCommandModel extends AbstractModelAccessTest {

	/** The file whose action chain fails. */
	private static final String FAILING_FILE = "broken.txt";

	/** The file uploaded after {@link #FAILING_FILE}. */
	private static final String OTHER_FILE = "plain.txt";

	/** The names of the files the action chain ran through, in upload order. */
	private List<String> _uploaded;

	/** The messages shown to the user. */
	private List<HTMLFragment> _shown;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_uploaded = new ArrayList<>();
		_shown = new ArrayList<>();
		ArgumentsProbe.reset();
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		super.tearDown();
	}

	@Override
	protected Person initialUser() {
		return _root;
	}

	/**
	 * Tests that a rule deciding over a single value sees the first input of the command, not the
	 * file that does not exist before the upload: the button is disabled while the input is empty,
	 * and files are uploaded once it holds a value.
	 */
	public void testNullInputDisabledDecidesOverFirstInput() throws Exception {
		DefaultViewContext context = context(Collections.singletonMap(SELECTION, null));
		ViewUploadCommandModel model = model(context,
			"<upload-command inputs='" + SELECTION + "'>"
				+ "<executability><null-input-disabled/></executability>"
				+ PROBE
				+ "</upload-command>");
		model.attach(null);

		assertTrue(model.getExecutableState().isVisible());
		assertFalse("Disabled while the input is empty.", model.getExecutableState().isExecutable());
		CommandFailures.failure("The upload is refused while the input is empty.",
			() -> model.uploadFiles(context, List.of(file(OTHER_FILE))));
		assertEquals("A refused upload runs no action.", List.of(), ArgumentsProbe.SEEN);

		context.resolveChannel(new ChannelRef(SELECTION)).set("s");
		assertTrue("Executable as soon as the input holds a value.", model.getExecutableState().isExecutable());

		model.uploadFiles(context, List.of(file(OTHER_FILE)));
		assertEquals(1, ArgumentsProbe.SEEN.size());
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals("The actions run with the inputs and the file.",
			List.of(SELECTION, UploadCommand.FILE_ARGUMENT), seen.names());
		assertEquals("The file is the default argument of the actions.", UploadCommand.FILE_ARGUMENT,
			seen.defaultName());
		assertEquals(OTHER_FILE, ((BinaryData) seen.value()).getName());
		assertEquals("s", seen.get(SELECTION));
		model.detach();
	}

	/**
	 * Tests that a script rule uses the inputs of the command by their names, and that the reason
	 * it gives for refusing the upload is the tooltip of the button.
	 */
	public void testScriptRuleOverInputs() throws Exception {
		String reason = "The ticket is locked.";
		DefaultViewContext context = context(Map.of(SELECTION, "locked", OTHER, "o"));
		ViewUploadCommandModel model = model(context,
			"<upload-command inputs='" + SELECTION + " as ticket, " + OTHER + "'>"
				+ "<executability>"
				+ "<disabled-if expr=\"if($ticket == 'locked' &amp;&amp; $" + OTHER + " == 'o', '" + reason + "')\"/>"
				+ "</executability>"
				+ PROBE
				+ "</upload-command>");
		model.attach(null);

		assertFalse(model.getExecutableState().isExecutable());
		assertEquals("The button explains why it is disabled.", reason, model.getTooltip());
		TopLogicException refusal = CommandFailures.failure("The upload is refused.",
			() -> model.uploadFiles(context, List.of(file(OTHER_FILE))));
		assertEquals(ErrorSeverity.WARNING, refusal.getSeverity());
		assertEquals(List.of(), ArgumentsProbe.SEEN);

		context.resolveChannel(new ChannelRef(SELECTION)).set("free");
		assertTrue(model.getExecutableState().isExecutable());
		assertNull("Without a refusal, the button has no tooltip.", model.getTooltip());

		model.uploadFiles(context, List.of(file(OTHER_FILE)));
		assertEquals(1, ArgumentsProbe.SEEN.size());
		model.detach();
	}

	/**
	 * Tests that the script editor offers the configured rules of an upload command the names of
	 * its inputs, and that a rule using the uploaded file, which does not exist when the rules
	 * decide, is refused.
	 */
	public void testFileIsNoArgumentOfTheRules() throws Exception {
		UploadCommand.Config config = parse(new DefaultInstantiationContext(TestViewUploadCommandModel.class),
			"<upload-command inputs='" + SELECTION + " as ticket'>"
				+ "<executability><disabled-if expr='$ticket == null'/></executability>"
				+ "</upload-command>");
		assertEquals(List.of("ticket"),
			ScriptContextVariablesResolver.resolve(new DefaultValueModel(config.getExecutability().get(0),
				config.getExecutability().get(0).descriptor().getProperty(DisabledIf.Config.EXPR))));

		assertRuleError("<upload-command inputs='" + SELECTION + "'>"
			+ "<executability><disabled-if expr='$" + UploadCommand.FILE_ARGUMENT + " == null'/></executability>"
			+ "</upload-command>");
		assertRuleError("<upload-command inputs='" + SELECTION + "' as='document'>"
			+ "<executability><disabled-if expr='$document == null'/></executability>"
			+ "</upload-command>");
		assertRuleError("<upload-command>"
			+ "<executability><disabled-if expr='$" + UploadCommand.FILE_ARGUMENT + " == null'/></executability>"
			+ "</upload-command>");
	}

	/**
	 * Tests that the file named in a failed upload and the reason of the failure both reach the
	 * user, while the files after it are still uploaded.
	 */
	public void testFailureIsShownToTheUser() throws IOException {
		upload(reactContext(sink()), failingWith(new TopLogicException(ResKey.text("The document is locked."))));

		assertEquals("Exactly the failed file is reported.", 1, _shown.size());
		String message = render(_shown.get(0));
		assertTrue("The message names the file that failed: " + message, message.contains(FAILING_FILE));
		assertTrue("The message gives the reason of the failure: " + message,
			message.contains("The document is locked."));
		assertEquals("The upload continues with the files after the failed one.", List.of(OTHER_FILE), _uploaded);
	}

	/**
	 * Tests that a failure without a message of its own is reported with the text of the exception.
	 */
	public void testPlainFailureIsShownToTheUser() throws IOException {
		upload(reactContext(sink()), failingWith(new RuntimeException("No space left.")));

		assertEquals("Exactly the failed file is reported.", 1, _shown.size());
		String message = render(_shown.get(0));
		assertTrue("The message names the file that failed: " + message, message.contains(FAILING_FILE));
		assertTrue("The message gives the text of the exception: " + message, message.contains("No space left."));
		assertEquals("The upload continues with the files after the failed one.", List.of(OTHER_FILE), _uploaded);
	}

	/**
	 * Tests that a failure in a context without a window to report to neither escapes nor stops the
	 * remaining uploads.
	 */
	public void testFailureWithoutErrorSink() {
		upload(reactContext(null), failingWith(new TopLogicException(ResKey.text("The document is locked."))));

		assertEquals("The upload continues with the files after the failed one.", List.of(OTHER_FILE), _uploaded);
	}

	/**
	 * Tests that an upload that succeeds reports nothing.
	 */
	public void testSuccessReportsNothing() {
		upload(reactContext(sink()), (context, input) -> record(input));

		assertEquals("Every file is uploaded.", List.of(FAILING_FILE, OTHER_FILE), _uploaded);
		assertEquals("A successful upload has nothing to report.", List.of(), _shown);
	}

	/**
	 * Tests that an upload the command's rule refuses processes no file and throws the refusal.
	 */
	public void testRefusedUploadProcessesNothing() {
		TopLogicException refusal = CommandFailures.failure("The refusal is reported.",
			() -> upload(reactContext(sink()), (context, input) -> record(input), input -> ExecutableState.NO_EXEC_NO_MODEL));

		assertEquals("A refused upload processes no file.", List.of(), _uploaded);
		assertEquals(ErrorSeverity.WARNING, refusal.getSeverity());
	}

	/**
	 * The model of the upload command of the given configuration, displayed in the given context.
	 */
	private static ViewUploadCommandModel model(DefaultViewContext context, String xml)
			throws ConfigurationException {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestViewUploadCommandModel.class);
		UploadCommand.Config config = parse(instantiation, xml);
		UploadCommand command = (UploadCommand) instantiation.getInstance(config);
		instantiation.checkErrors();
		ViewCommandModel model = ViewCommandModel.forCommand(context, command, config);
		assertTrue(model instanceof ViewUploadCommandModel);
		return (ViewUploadCommandModel) model;
	}

	/** Asserts that the rules of the given upload command are refused. */
	private static void assertRuleError(String xml) throws ConfigurationException {
		UploadCommand.Config config = parse(new DefaultInstantiationContext(TestViewUploadCommandModel.class), xml);
		try {
			ViewExecutabilityRules.build(config, (String) null, new DefaultViewContext(null));
			fail("The rule is refused: " + xml);
		} catch (ConfigurationError ex) {
			// Expected.
		}
	}

	private static UploadCommand.Config parse(DefaultInstantiationContext context, String xml)
			throws ConfigurationException {
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("upload-command",
			TypedConfiguration.getConfigurationDescriptor(UploadCommand.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		UploadCommand.Config config = (UploadCommand.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/** Uploads {@link #FAILING_FILE} and {@link #OTHER_FILE} through a chain of the given action. */
	private void upload(ReactContext context, ViewAction action) {
		upload(context, action, ViewExecutabilityRule.ALWAYS_EXECUTABLE);
	}

	/**
	 * Uploads {@link #FAILING_FILE} and {@link #OTHER_FILE} through a chain of the given action,
	 * for a command deciding by the given rule.
	 */
	private void upload(ReactContext context, ViewAction action, ViewExecutabilityRule rule) {
		UploadCommand.Config config = TypedConfiguration.newConfigItem(UploadCommand.Config.class);
		UploadCommand command =
			new UploadCommand(new DefaultInstantiationContext(TestViewUploadCommandModel.class), config) {
				@Override
				public List<ViewAction> getActions() {
					return List.of(action);
				}
			};

		ViewUploadCommandModel model =
			new ViewUploadCommandModel(command, config, (ViewChannel) null, rule);

		model.upload(context, List.of(file(FAILING_FILE), file(OTHER_FILE)));
	}

	/** An action that fails for {@link #FAILING_FILE} and records every other file. */
	private ViewAction failingWith(RuntimeException failure) {
		return (context, input) -> {
			if (FAILING_FILE.equals(((BinaryData) input).getName())) {
				throw failure;
			}
			return record(input);
		};
	}

	/** Notes the given file as uploaded. */
	private Object record(Object input) {
		_uploaded.add(((BinaryData) input).getName());
		return input;
	}

	/** A sink collecting the messages shown to the user. */
	private ErrorSink sink() {
		return new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				_shown.add(content);
			}

			@Override
			public void showWarning(HTMLFragment content) {
				_shown.add(content);
			}

			@Override
			public void showInfo(HTMLFragment content) {
				_shown.add(content);
			}
		};
	}

	/** A context reporting to the given sink, <code>null</code> for a context without one. */
	private static ReactContext reactContext(ErrorSink sink) {
		return new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"))) {
			@Override
			public ErrorSink getErrorSink() {
				return sink;
			}
		};
	}

	/** An uploaded file of the given name. */
	private static BinaryData file(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(), BinaryData.CONTENT_TYPE_OCTET_STREAM, name);
	}

	/** The text of a message shown to the user. */
	private static String render(HTMLFragment message) throws IOException {
		TagWriter out = new TagWriter();
		message.write(new DummyDisplayContext(), out);
		return out.toString();
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestViewUploadCommandModel.class);
	}

}
