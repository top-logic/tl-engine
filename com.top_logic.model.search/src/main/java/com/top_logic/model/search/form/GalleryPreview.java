/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import java.awt.Dimension;
import java.util.Collection;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.element.model.imagegallery.ImageGalleryFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.control.DisplayImageControl;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.image.gallery.GalleryImage;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.form.definition.NamedPartNames;
import com.top_logic.model.search.tiles.AbstractPreviewContent;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.AllTypeAttributes;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * Preview displaying the first image of an gallery attribute as preview.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class GalleryPreview extends AbstractPreviewContent<GalleryPreview.Config> {

	/**
	 * {@link AllClasses} where only those {@link TLClass} with an
	 * {@link ImageGalleryFactory#GALLERY_IMAGE_NODE gallery attribute} are selectable.
	 */
	public static class AllGalleryTypes extends AllClasses {

		@Override
		protected Filter<? super TLModelPart> modelFilter() {
			return AllGalleryTypes::hasGalleryAttribute;
		}

		private static boolean hasGalleryAttribute(TLModelPart part) {
			if (!(part instanceof TLClass)) {
				return false;
			}
			return ((TLClass) part).getAllClassParts()
				.stream()
				.filter(AllGalleryTypes::isGalleryAttribute)
				.findAny()
				.isPresent();
		}

		static boolean isGalleryAttribute(TLTypePart part) {
			return isGalleryType(part.getType());
		}

		static boolean isGalleryType(TLType type) {
			if (!ImageGalleryFactory.GALLERY_IMAGE_NODE.equals(type.getName())) {
				return false;
			}
			return ImageGalleryFactory.TL_IMAGEGALLERY_STRUCTURE.equals(type.getModule().getName());
		}
	}

	/**
	 * {@link AllTypeAttributes} filtering the attributes such that only attributes of type
	 * {@link ImageGalleryFactory#GALLERY_IMAGE_NODE} are contained in the result.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllGalleryAttributes extends AllTypeAttributes {

		@Override
		public Collection<? extends TLStructuredTypePart> apply(TLModelPartRef typeRef) {
			return super.apply(typeRef)
				.stream()
				.filter(AllGalleryTypes::isGalleryAttribute)
				.collect(Collectors.toList());
		}

	}

	/**
	 * Typed configuration interface definition for {@link GalleryPreview}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@DisplayOrder({
		Config.TYPE,
		Config.GALLERY_ATTRIBUTE,
		Config.ICON,
	})
	public interface Config extends AbstractPreviewContent.Config<GalleryPreview>, TypeTemplateParameters {

		/** Configuration name for the value of {@link #getGalleryAttribute()}. */
		String GALLERY_ATTRIBUTE = "gallery-attribute";

		/** Configuration name for the value of {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * Icon that is displayed when no image can be determined from the model.
		 * 
		 * <p>
		 * That may happen when the given model is not of the correct {@link #getType()}, or the
		 * gallery attribute is currently not filled.
		 * </p>
		 */
		@Name(ICON)
		ThemeImage getIcon();

		/**
		 * The type from which the gallery attribute is taken.
		 * 
		 * @see com.top_logic.layout.editor.config.TypeTemplateParameters#getType()
		 */
		@Override
		@Options(fun = AllGalleryTypes.class, mapping = TLModelPartRef.PartMapping.class)
		TLModelPartRef getType();

		/**
		 * Name of the gallery attribute whose value is used to create the preview.
		 */
		@Options(fun = AllGalleryAttributes.class, args = @Ref(TYPE), mapping = NamedPartNames.class)
		@Name(GALLERY_ATTRIBUTE)
		@Nullable
		@Mandatory
		String getGalleryAttribute();

	}

	/**
	 * Create a {@link GalleryPreview}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public GalleryPreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		GalleryImage galleryImage = findGalleryImage(model);
		if (galleryImage == null) {
			return imagePreview(getConfig().getIcon());
		}
		return createGalleryContent(galleryImage);
	}

	/**
	 * Service method to create a default preview for the given {@link GalleryPreview}.
	 */
	public static HTMLFragment createGalleryContent(GalleryImage galleryImage) {
		BinaryDataSource image = galleryImage.getImage();

		Dimension maximalSize = null;
		return defaultPreview(new DisplayImageControl(image, maximalSize), "galleryPreview");
	}

	private GalleryImage findGalleryImage(Object model) {
		TLObject typedModel = getModel(model);
		if (typedModel == null) {
			return null;
		}
		Object value = typedModel.tValueByName(getConfig().getGalleryAttribute());
		if (value instanceof Collection<?>) {
			// multiple attribute: Fetch first or null when empty.
			value = CollectionUtil.getFirst((Collection<?>) value);
		}
		return (GalleryImage) value;
	}

	private TLObject getModel(Object model) {
		TLClass type = OptionalTypeTemplateParameters.resolve(getConfig());
		TLObject object;
		if (model instanceof TLObject) {
			object = (TLObject) model;
		} else {
			return null;
		}
		if (!TLModelUtil.isCompatibleInstance(type, object)) {
			return null;
		}
		return object;
	}

}
