/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.Part;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FileListControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.table.ColumnProviderService;

/**
 * Tests the upload of the file chips ({@link FileListControl}): the control publishes the upload
 * limit and reports a failed upload until the next one succeeds.
 */
public class TestFileListUpload extends AbstractModelAccessTest {

	/** The message of the error a broken upload fails with. */
	private static final String BROKEN = "connection reset";

	private FileListControl _files;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));

		becomeUser(_responsible);
		FormControl form = new FormControl(context, _project, "no model", NoTokenHandling.INSTANCE);
		_files = new FileListControl(context, form, STEPS, null);
		_files.init();
		assertTrue(form.enterEditMode());
	}

	/**
	 * The client learns the upload limit, which it checks before transmitting a file.
	 */
	public void testPublishesUploadLimit() {
		assertEquals(UploadSupport.maxUploadSize(), state(UploadSupport.MAX_UPLOAD_SIZE));
	}

	/**
	 * A failed upload is reported with the message of its error; the next successful upload clears
	 * the report.
	 */
	public void testErrorUntilNextSuccessfulUpload() {
		assertNull(state(FileListControl.ERROR));

		upload(new MemoryPart(null));
		assertEquals(BROKEN, state(FileListControl.ERROR));
		assertEquals("A file that cannot be read adds no row.", 0, rowCount());

		upload(new MemoryPart(new byte[] { 1, 2, 3 }));
		assertNull("A successful upload clears the error.", state(FileListControl.ERROR));
		assertEquals(1, rowCount());
	}

	private void upload(Part part) {
		_files.handleUpload(null, List.of(part));
	}

	private int rowCount() {
		return _files.session().currentRows().size();
	}

	private Object state(String key) {
		return _files.scriptingScalarState().get(key);
	}

	/**
	 * An uploaded file in memory, sent in the part {@value UploadSupport#FILE_PART}.
	 */
	private static final class MemoryPart implements Part {

		private final byte[] _content;

		/**
		 * Creates a {@link MemoryPart}.
		 *
		 * @param content
		 *        The content of the file, <code>null</code> for a part whose content cannot be
		 *        read.
		 */
		MemoryPart(byte[] content) {
			_content = content;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (_content == null) {
				throw new IOException(BROKEN);
			}
			return new ByteArrayInputStream(_content);
		}

		@Override
		public String getContentType() {
			return "text/plain";
		}

		@Override
		public String getName() {
			return UploadSupport.FILE_PART;
		}

		@Override
		public String getSubmittedFileName() {
			return "notes.txt";
		}

		@Override
		public long getSize() {
			return _content == null ? 0 : _content.length;
		}

		@Override
		public void write(String fileName) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete() throws IOException {
			// Nothing stored.
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			return List.of();
		}

		@Override
		public Collection<String> getHeaderNames() {
			return List.of();
		}
	}

	/**
	 * The test suite, starting the theme, resources and label providers the composition control
	 * needs, as {@link TestCompositionCell} does.
	 */
	public static Test suite() {
		return suiteWith(TestFileListUpload.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
