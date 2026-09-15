/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.UploadCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewUploadCommandModel;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that uploading a file whose action chain fails reports the failure to the user and
 * continues with the files that follow.
 */
public class TestViewUploadCommandModel extends TestCase {

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
	}

	/**
	 * Tests that the file named in a failed upload and the reason of the failure both reach the
	 * user, while the files after it are still uploaded.
	 */
	public void testFailureIsShownToTheUser() throws IOException {
		upload(context(sink()), failingWith(new TopLogicException(ResKey.text("The document is locked."))));

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
		upload(context(sink()), failingWith(new RuntimeException("No space left.")));

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
		upload(context(null), failingWith(new TopLogicException(ResKey.text("The document is locked."))));

		assertEquals("The upload continues with the files after the failed one.", List.of(OTHER_FILE), _uploaded);
	}

	/**
	 * Tests that an upload that succeeds reports nothing.
	 */
	public void testSuccessReportsNothing() {
		upload(context(sink()), (context, input) -> record(input));

		assertEquals("Every file is uploaded.", List.of(FAILING_FILE, OTHER_FILE), _uploaded);
		assertEquals("A successful upload has nothing to report.", List.of(), _shown);
	}

	/** Uploads {@link #FAILING_FILE} and {@link #OTHER_FILE} through a chain of the given action. */
	private void upload(ReactContext context, ViewAction action) {
		UploadCommand.Config config = TypedConfiguration.newConfigItem(UploadCommand.Config.class);
		UploadCommand command =
			new UploadCommand(new DefaultInstantiationContext(TestViewUploadCommandModel.class), config) {
				@Override
				public List<ViewAction> getActions() {
					return List.of(action);
				}
			};

		ViewUploadCommandModel model =
			new ViewUploadCommandModel(command, config, null, ViewExecutabilityRule.ALWAYS_EXECUTABLE);

		model.uploadFiles(context, List.of(file(FAILING_FILE), file(OTHER_FILE)));
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
	private static ReactContext context(ErrorSink sink) {
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

	/** Suite requiring the resources the reported messages are resolved from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestViewUploadCommandModel.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
