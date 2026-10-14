/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactBinaryFieldControl;
import com.top_logic.layout.react.control.form.ReactFileChipsControl;
import com.top_logic.layout.react.control.form.RemoveChipArguments;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the {@link ReactFileChipsControl}: a field holding several files, one chip per file, to
 * which files are added by upload and from which they are removed chip by chip.
 */
public class TestReactFileChipsControl extends TestCase {

	private static final BinaryData A = file("a.txt", "Hello");

	private static final BinaryData B = file("b.xml", "<b/>");

	/**
	 * Each file is a chip naming the file and stating its size; the files themselves are not part
	 * of the client state.
	 */
	public void testChipPerFile() {
		Chips chips = chips(new AbstractFieldModel(Arrays.asList(A, B)));

		List<Map<String, Object>> state = chips.chips();
		assertEquals(2, state.size());
		assertEquals("a.txt", state.get(0).get(ReactFileChipsControl.CHIP_NAME));
		assertEquals(Long.valueOf(5), state.get(0).get(ReactFileChipsControl.CHIP_SIZE));
		assertEquals(Boolean.TRUE, state.get(0).get(ReactFileChipsControl.CHIP_HAS_DATA));
		assertEquals("b.xml", state.get(1).get(ReactFileChipsControl.CHIP_NAME));
		assertEquals(Long.valueOf(4), state.get(1).get(ReactFileChipsControl.CHIP_SIZE));
		assertNull("The files are not sent to the client", chips.state(FieldState.VALUE__PROP));
	}

	/**
	 * A field holding nothing shows no chip.
	 */
	public void testEmpty() {
		assertEquals(0, chips(new AbstractFieldModel(null)).chips().size());
		assertEquals(0, chips(new AbstractFieldModel(Collections.emptyList())).chips().size());
	}

	/**
	 * Uploaded files are appended to the collection in the order they were sent, after the files
	 * already there.
	 */
	public void testUploadAppends() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A));
		Chips chips = chips(model);

		chips.handleUpload(null, Arrays.asList(part("c.txt", "C"), part("d.txt", "DD")));

		List<?> value = (List<?>) model.getValue();
		assertEquals(3, value.size());
		assertSame(A, value.get(0));
		assertEquals("c.txt", ((BinaryData) value.get(1)).getName());
		assertEquals("d.txt", ((BinaryData) value.get(2)).getName());
		assertEquals(3, chips.chips().size());
		assertEquals("d.txt", chips.chips().get(2).get(ReactFileChipsControl.CHIP_NAME));
	}

	/**
	 * The first upload into an empty field creates the collection.
	 */
	public void testUploadIntoEmptyField() {
		FieldModel model = new AbstractFieldModel(null);
		Chips chips = chips(model);

		chips.handleUpload(null, Arrays.asList(part("c.txt", "C")));

		assertEquals(1, ((List<?>) model.getValue()).size());
		assertEquals(1, chips.chips().size());
	}

	/**
	 * A part other than a file part is no file.
	 */
	public void testUploadIgnoresOtherParts() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A));
		Chips chips = chips(model);

		chips.handleUpload(null, Arrays.asList(new TestPart("other", "x.txt", "X")));

		assertEquals(Arrays.asList(A), model.getValue());
	}

	/**
	 * Removing a chip removes exactly its file; the remaining chips keep their keys.
	 */
	public void testRemoveChip() {
		BinaryData c = file("c.txt", "C");
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, B, c));
		Chips chips = chips(model);
		String keyA = chips.key(0);
		String keyC = chips.key(2);

		chips.remove(chips.key(1));

		assertEquals(Arrays.asList(A, c), model.getValue());
		assertEquals(2, chips.chips().size());
		assertEquals("A remaining file keeps its chip", keyA, chips.key(0));
		assertEquals("A remaining file keeps its chip", keyC, chips.key(1));
	}

	/**
	 * Of two chips showing the same file, removing one keeps the other.
	 */
	public void testRemoveOneOfTwoEqualFiles() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, A));
		Chips chips = chips(model);
		assertFalse("Each occurrence is a chip of its own", chips.key(0).equals(chips.key(1)));

		chips.remove(chips.key(1));

		assertEquals(Arrays.asList(A), model.getValue());
	}

	/**
	 * A key of no chip removes nothing.
	 */
	public void testRemoveUnknownChip() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A));
		Chips chips = chips(model);

		chips.remove("unknown");

		assertEquals(Arrays.asList(A), model.getValue());
	}

	/**
	 * A file is downloaded by the key of its chip.
	 */
	public void testDownload() {
		Chips chips = chips(new AbstractFieldModel(Arrays.asList(A, B)));

		assertSame(B, chips.getDownloadData(chips.key(1)));
		assertNull(chips.getDownloadData("unknown"));
	}

	/**
	 * A value changed from elsewhere is shown at once; a file still there keeps its chip.
	 */
	public void testModelChange() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A));
		Chips chips = chips(model);
		String keyA = chips.key(0);

		model.setValue(Arrays.asList(B, A));

		assertEquals(2, chips.chips().size());
		assertEquals("b.xml", chips.chips().get(0).get(ReactFileChipsControl.CHIP_NAME));
		assertEquals(keyA, chips.key(1));
	}

	/**
	 * A field that is not editable takes neither uploads nor removals.
	 */
	public void testNotEditable() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList(A));
		model.setEditable(false);
		Chips chips = chips(model);
		assertEquals(Boolean.FALSE, chips.state(FieldState.EDITABLE__PROP));

		chips.handleUpload(null, Arrays.asList(part("c.txt", "C")));
		chips.remove(chips.key(0));

		assertEquals(Arrays.asList(A), model.getValue());
	}

	/**
	 * The editability of the model is that of the chips.
	 */
	public void testEditabilityFollowsModel() {
		AbstractFieldModel model = new AbstractFieldModel(Arrays.asList(A));
		Chips chips = chips(model);
		assertEquals(Boolean.TRUE, chips.state(FieldState.EDITABLE__PROP));

		model.setEditable(false);

		assertEquals(Boolean.FALSE, chips.state(FieldState.EDITABLE__PROP));
	}

	/**
	 * The accepted file types reach the file picker in the form of the HTML accept attribute: a
	 * file pattern becomes a file extension.
	 */
	public void testAcceptedTypes() {
		Chips chips = chips(new AbstractFieldModel(null));
		assertNull("Every file is offered by default", chips.state(UploadSupport.ACCEPT));

		chips.setAcceptedTypes("*.xml");
		assertEquals(".xml", chips.state(UploadSupport.ACCEPT));

		chips.setAcceptedTypes("image/*");
		assertEquals("image/*", chips.state(UploadSupport.ACCEPT));

		chips.setAcceptedTypes(null);
		assertNull(chips.state(UploadSupport.ACCEPT));
	}

	/**
	 * The single binary field takes the accepted file types in the same form.
	 */
	public void testBinaryFieldAcceptedTypes() {
		Binary binary = new Binary(context(), new AbstractFieldModel(null));
		assertNull("Every file is offered by default", binary.state(UploadSupport.ACCEPT));

		binary.setAcceptedTypes("*.pdf, *.docx");

		assertEquals(".pdf,.docx", binary.state(UploadSupport.ACCEPT));
	}

	/**
	 * The conversion of accepted file types to the HTML accept attribute.
	 */
	public void testAcceptAttribute() {
		assertNull(UploadSupport.acceptAttribute(null));
		assertNull(UploadSupport.acceptAttribute(""));
		assertNull("Every file is accepted", UploadSupport.acceptAttribute("*.*"));
		assertNull("Every file is accepted", UploadSupport.acceptAttribute("*/*"));
		assertEquals(".xml", UploadSupport.acceptAttribute("*.xml"));
		assertEquals(".xml,.json", UploadSupport.acceptAttribute(" *.xml , *.json "));
		assertEquals("image/jpeg", UploadSupport.acceptAttribute("image/jpeg"));
		assertEquals("image/*,.xml", UploadSupport.acceptAttribute("*.xml,image/*"));
	}

	private static BinaryData file(String name, String content) {
		return BinaryDataFactory.createBinaryData(content.getBytes(StandardCharsets.UTF_8), "text/plain", name);
	}

	/**
	 * A {@value UploadSupport#FILE_PART} part of a multipart upload holding the given text.
	 */
	static Part part(String fileName, String content) {
		return new TestPart(UploadSupport.FILE_PART, fileName, content);
	}

	private static Chips chips(FieldModel model) {
		return new Chips(context(), model);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * A {@link ReactFileChipsControl} exposing its server-side state, and the gestures of its
	 * client, for assertions.
	 */
	private static final class Chips extends ReactFileChipsControl {

		Chips(ReactContext context, FieldModel model) {
			super(context, model);
		}

		/** The chips, in file order. */
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> chips() {
			return (List<Map<String, Object>>) getState(CHIPS);
		}

		/** The key of the chip at the given position. */
		String key(int index) {
			return (String) chips().get(index).get(CHIP_KEY);
		}

		/** The value of the given state property. */
		Object state(String key) {
			return getState(key);
		}

		/** Removes the file of the chip with the given key, as the client's remove button does. */
		void remove(String key) {
			executeClientCommand(CMD_REMOVE_CHIP, Map.of(RemoveChipArguments.KEY, key));
		}
	}

	/**
	 * A {@link ReactBinaryFieldControl} exposing its server-side state for assertions.
	 */
	private static final class Binary extends ReactBinaryFieldControl {

		Binary(ReactContext context, FieldModel model) {
			super(context, model);
		}

		/** The value of the given state property. */
		Object state(String key) {
			return getState(key);
		}
	}

	/**
	 * A {@link Part} of a multipart upload holding a text.
	 */
	static final class TestPart implements Part {

		private final String _name;

		private final String _fileName;

		private final byte[] _content;

		TestPart(String name, String fileName, String content) {
			_name = name;
			_fileName = fileName;
			_content = content.getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(_content);
		}

		@Override
		public String getContentType() {
			return "text/plain";
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public String getSubmittedFileName() {
			return _fileName;
		}

		@Override
		public long getSize() {
			return _content.length;
		}

		@Override
		public void write(String fileName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete() {
			// Nothing to delete.
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Collection<String> getHeaders(String name) {
			return new ArrayList<>();
		}

		@Override
		public Collection<String> getHeaderNames() {
			return new ArrayList<>();
		}
	}

	/**
	 * The test suite, started with the resource bundles a field's messages need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactFileChipsControl.class, ResourcesModule.Module.INSTANCE));
	}
}
