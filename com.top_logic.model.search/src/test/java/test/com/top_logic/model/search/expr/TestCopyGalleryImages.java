/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.element.model.imagegallery.GalleryImage;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.image.gallery.TransientGalleryImage;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test of persisting a transient object whose gallery holds images that are not stored yet.
 *
 * <p>
 * A new object edited in a form holds the images uploaded into its gallery as
 * {@link TransientGalleryImage}s, which are no model objects. Copying the object into the knowledge
 * base passes them on to the storage of the gallery, which stores each as a {@link GalleryImage},
 * for a plain gallery and for a gallery composed into its owner alike.
 * </p>
 *
 * @see CopyOperation#initial()
 */
@SuppressWarnings("javadoc")
public class TestCopyGalleryImages extends AbstractSearchExpressionTest {

	private static final String TYPE = "`TestSearchExpression:WithGallery`";

	private static final String NAME = "`TestSearchExpression:WithGallery#name`";

	private static final String GALLERY = "TestSearchExpression:WithGallery#gallery";

	private static final String OWNED_GALLERY = "TestSearchExpression:WithGallery#ownedGallery";

	private final List<TLObject> _persistent = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		for (TLObject object : _persistent) {
			if (object.tValid()) {
				delete(object);
			}
		}
		_persistent.clear();
		super.tearDown();
	}

	public void testPersistDraftStoresUploadedImages() throws ParseException {
		assertImagesStored(GALLERY);
	}

	public void testPersistDraftStoresUploadedImagesOfAComposedGallery() throws ParseException {
		assertImagesStored(OWNED_GALLERY);
	}

	private void assertImagesStored(String gallery) throws ParseException {
		BinaryData first = image("first.png");
		BinaryData second = image("second.png");
		TLObject draft = (TLObject) execute(search("new(" + TYPE + ", transient: true)..set(" + NAME + ", 'draft')"));
		set(draft, gallery, List.of(new TransientGalleryImage(first), new TransientGalleryImage(second)));

		TLObject created = persist(draft);

		assertFalse(created.tTransient());
		Collection<?> images = (Collection<?>) get(created, gallery);
		assertEquals(2, images.size());
		List<String> names = new ArrayList<>();
		for (Object image : images) {
			assertTrue("Stored as a gallery image: " + image, image instanceof GalleryImage);
			assertFalse(((GalleryImage) image).tTransient());
			names.add(((GalleryImage) image).getName());
		}
		assertEquals("The images keep their order.", List.of("first.png", "second.png"), names);
	}

	private TLObject persist(TLObject draft) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			CopyOperation operation = CopyOperation.initial();
			operation.setTransient(Boolean.FALSE);
			TLObject created = (TLObject) operation.copyReference(draft);
			operation.finish();
			tx.commit();
			_persistent.add(created);
			return created;
		}
	}

	private static BinaryData image(String name) {
		return BinaryDataFactory.createBinaryData(name.getBytes(StandardCharsets.UTF_8), "image/png", name);
	}

	private static void set(TLObject object, String part, Object value) {
		object.tUpdate(TLModelUtil.findPart(part), value);
	}

	private static Object get(TLObject object, String part) throws ParseException {
		return execute(search("x -> $x.get(`" + part + "`)"), object);
	}

	public static Test suite() {
		return suite(TestCopyGalleryImages.class);
	}

}
