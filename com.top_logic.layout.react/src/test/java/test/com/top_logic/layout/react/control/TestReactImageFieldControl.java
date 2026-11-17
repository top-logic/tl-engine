/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.top_logic.layout.react.control.form.ImageValues;
import com.top_logic.layout.react.control.form.ReactImageFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests the {@link ReactImageFieldControl}: where uploaded images go, and what the control tells
 * its client about the size of its stage and the room it takes in a form.
 */
public class TestReactImageFieldControl extends TestCase {

	private static final BinaryData A = image("a.png");

	private static final BinaryData B = image("b.png");

	private static final BinaryData C = image("c.png");

	/**
	 * Files uploaded after the image of a tile are inserted right after it, in the order they were
	 * sent.
	 */
	public void testUploadInsertsAfterNamedImage() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, B, C));
		Images images = images(model, true);

		images.handleUpload(null, Arrays.asList(
			TestReactFileChipsControl.part("x.png", "X"),
			TestReactFileChipsControl.part("y.png", "Y"),
			after(images.key(0))));

		assertEquals(List.of("a.png", "x.png", "y.png", "b.png", "c.png"), names(model));
	}

	/**
	 * Files uploaded after the last image are appended.
	 */
	public void testUploadAfterLastImageAppends() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, B));
		Images images = images(model, true);

		images.handleUpload(null, Arrays.asList(TestReactFileChipsControl.part("x.png", "X"), after(images.key(1))));

		assertEquals(List.of("a.png", "b.png", "x.png"), names(model));
	}

	/**
	 * Files uploaded without naming an image, or after an image no longer shown, are appended.
	 */
	public void testUploadWithoutShownImageAppends() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, B));
		Images images = images(model, true);

		images.handleUpload(null, Arrays.asList(TestReactFileChipsControl.part("x.png", "X")));
		images.handleUpload(null, Arrays.asList(TestReactFileChipsControl.part("y.png", "Y"), after("gone")));

		assertEquals(List.of("a.png", "b.png", "x.png", "y.png"), names(model));
	}

	/**
	 * A file uploaded for the image of a tile replaces that image, at its position.
	 */
	public void testUploadReplacesNamedImage() {
		FieldModel model = new AbstractFieldModel(Arrays.asList(A, B, C));
		Images images = images(model, true);

		images.handleUpload(null, Arrays.asList(TestReactFileChipsControl.part("x.png", "X"),
			new TextPart(ReactImageFieldControl.REPLACE_PART, ReactImageFieldControl.REPLACE_PART, images.key(1))));

		assertEquals(List.of("a.png", "x.png", "c.png"), names(model));
	}

	/**
	 * A field holding a single image takes the first uploaded file as its image, wherever the
	 * client puts it.
	 */
	public void testSingleImageIsReplaced() {
		FieldModel model = new AbstractFieldModel(A);
		Images images = images(model, false);

		images.handleUpload(null, Arrays.asList(TestReactFileChipsControl.part("x.png", "X"), after(images.key(0))));

		assertEquals("x.png", ((BinaryData) model.getValue()).getName());
	}

	/**
	 * The dimensions given to the control are the size of its stage; without them the client takes
	 * the width of the field and the height of the theme.
	 */
	public void testDimensions() {
		Images images = images(new AbstractFieldModel(null), false);
		assertNull(images.state(ReactImageFieldControl.WIDTH));
		assertNull(images.state(ReactImageFieldControl.HEIGHT));

		images.setDimensions("50%", "300px");

		assertEquals("50%", images.state(ReactImageFieldControl.WIDTH));
		assertEquals("300px", images.state(ReactImageFieldControl.HEIGHT));
	}

	/**
	 * An image is shown as wide as the form.
	 */
	public void testPrefersWholeLine() {
		assertTrue(images(new AbstractFieldModel(null), true).prefersWholeLine());
	}

	private static TextPart after(String key) {
		return new TextPart(ReactImageFieldControl.AFTER_PART, ReactImageFieldControl.AFTER_PART, key);
	}

	private static List<String> names(FieldModel model) {
		return ((List<?>) model.getValue()).stream().map(value -> ((BinaryData) value).getName()).toList();
	}

	private static BinaryData image(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "image/png", name);
	}

	private static Images images(FieldModel model, boolean multiple) {
		return new Images(context(), model, multiple);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * A {@link ReactImageFieldControl} of image files exposing its server-side state for
	 * assertions.
	 */
	private static final class Images extends ReactImageFieldControl {

		Images(ReactContext context, FieldModel model, boolean multiple) {
			super(context, model, ImageValues.FILES, multiple, true);
		}

		/** The key of the tile at the given position. */
		@SuppressWarnings("unchecked")
		String key(int index) {
			return (String) ((List<Map<String, Object>>) getState(IMAGES)).get(index).get(IMAGE_KEY);
		}

		/** The value of the given state property. */
		Object state(String key) {
			return getState(key);
		}
	}

	/**
	 * The test suite, started with the resource bundles a field's messages need.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactImageFieldControl.class, ResourcesModule.Module.INSTANCE));
	}
}
