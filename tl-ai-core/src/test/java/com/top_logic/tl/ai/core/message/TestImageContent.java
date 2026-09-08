/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link ImageContent}.
 */
public class TestImageContent extends TestCase {

	/** ImageContent stores url and mediaType. */
	public void testFields() {
		ImageContent img = new ImageContent("https://example.com/img.png", "image/png");
		assertEquals("https://example.com/img.png", img.url());
		assertEquals("image/png", img.mediaType());
	}

	/** Null url is rejected. */
	@SuppressWarnings("unused")
	public void testNullUrlRejected() {
		try {
			new ImageContent(null, "image/png");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Null mediaType is rejected. */
	@SuppressWarnings("unused")
	public void testNullMediaTypeRejected() {
		try {
			new ImageContent("https://example.com/img.png", null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/**
	 * The accepted set is exactly the four types the providers take, in the order a refusal names
	 * them. Anything added here has to be a type all four request builders can render.
	 */
	public void testAcceptedMediaTypesAreTheFourProviderTypes() {
		assertEquals(List.of("image/png", "image/jpeg", "image/gif", "image/webp"),
			List.copyOf(ImageContent.ACCEPTED_MEDIA_TYPES));
		assertEquals("image/png, image/jpeg, image/gif, image/webp", ImageContent.acceptedMediaTypes());
	}

	/** The {@code jpg} spelling, which no provider takes, resolves to the one they all take. */
	public void testJpgIsNormalizedToJpeg() {
		assertEquals("image/jpeg", ImageContent.normalizeMediaType("image/jpg"));
		assertEquals("image/jpeg", ImageContent.normalizeMediaType("IMAGE/JPG"));
		assertEquals("image/jpeg", ImageContent.normalizeMediaType(" image/jpg ; charset=binary"));
		assertTrue(ImageContent.isAccepted("image/jpg"));
	}

	/** Normalization drops case and MIME parameters, so a data URI states a type as sent. */
	public void testNormalizationStripsCaseAndParameters() {
		assertEquals("image/png", ImageContent.normalizeMediaType("Image/PNG"));
		assertEquals("image/png", ImageContent.normalizeMediaType("image/png; name=shot.png"));
		assertEquals("", ImageContent.normalizeMediaType(null));
		assertEquals("", ImageContent.normalizeMediaType("  "));
	}

	/** An image type no provider takes is not accepted, whatever it is called. */
	public void testUnacceptedImageTypes() {
		assertFalse(ImageContent.isAccepted("image/bmp"));
		assertFalse(ImageContent.isAccepted("image/svg+xml"));
		assertFalse(ImageContent.isAccepted("image/tiff"));
		assertFalse(ImageContent.isAccepted("image/avif"));
		assertFalse(ImageContent.isAccepted("application/pdf"));
		assertFalse(ImageContent.isAccepted(null));
	}

	/**
	 * A media type outside the accepted set still builds: a conversation persisted before a type was
	 * narrowed must stay readable, so the record validates nothing the deserializer would then choke
	 * on. What may be sent is decided where a payload is built.
	 */
	public void testAnUnacceptedTypeStillDeserializes() {
		ImageContent img = new ImageContent("data:image/bmp;base64,Qk0=", "image/bmp");

		assertEquals("image/bmp", img.mediaType());
		assertFalse(ImageContent.isAccepted(img.mediaType()));
	}
}
