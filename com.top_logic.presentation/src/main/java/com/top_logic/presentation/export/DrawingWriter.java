/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import org.apache.poi.util.Units;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTEffectList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuideList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStopList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTOuterShadowEffect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPathShadeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRelativeRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathShadeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STRectAlignment;
import org.openxmlformats.schemas.drawingml.x2006.main.STSchemeColorVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.ColorSpec;
import com.top_logic.presentation.model.GeometrySpec;
import com.top_logic.presentation.model.LineSpec;
import com.top_logic.presentation.model.PaintSpec;
import com.top_logic.presentation.model.ShadowSpec;
import com.top_logic.presentation.template.ColorInfo;
import com.top_logic.util.error.TopLogicException;

/**
 * Writes colors, fills, outlines, shadows and geometries of a presentation description as
 * the drawing XML of Office files.
 *
 * <p>
 * A {@link ColorSpec.Scheme theme color} is written as reference to the theme
 * (<code>schemeClr</code>), so that it follows the theme of the slide master, an
 * {@link ColorSpec.Rgb RGB color} as fixed color (<code>srgbClr</code>). A
 * {@link ColorSpec.Role color role} is replaced by the color defined for the role. The
 * modifications given with the role replace the modifications of the definition with the same
 * name.
 * </p>
 *
 * <p>
 * The <code>path</code> arguments name the written entry in the presentation description for
 * error messages.
 * </p>
 */
public final class DrawingWriter {

	/**
	 * A {@link DrawingWriter} for descriptions without color roles.
	 */
	public static final DrawingWriter WITHOUT_ROLES = new DrawingWriter(Map.of());

	/**
	 * The value the drawing XML uses for 100% in percentages.
	 */
	private static final double PERCENT_100 = 100000;

	/**
	 * The drawing XML unit of angles: 1/60000 degree.
	 */
	private static final int ANGLE_UNIT = 60000;

	/**
	 * The drawing XML unit of font sizes and text spacing: 1/100 point.
	 */
	static final int POINT_100 = 100;

	/**
	 * The name of the adjust value of a rounded rectangle.
	 */
	private static final String ROUND_RECT_ADJUST = "adj";

	/**
	 * The largest adjust value of a rounded rectangle: half the shorter side.
	 */
	private static final double ROUND_RECT_MAX_ADJUST = 50000;

	/**
	 * The namespace of the drawing XML.
	 */
	private static final String DRAWINGML_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";

	/**
	 * The name of the value attribute of a color modification.
	 */
	private static final String VAL = "val";

	private final Map<String, ColorSpec> _roles;

	/**
	 * Creates a {@link DrawingWriter}.
	 *
	 * @param roles
	 *        The colors of the color roles by the role names.
	 */
	public DrawingWriter(Map<String, ColorSpec> roles) {
		_roles = roles;
	}

	/**
	 * A {@link DrawingWriter} for the content of an element with the given color role.
	 *
	 * <p>
	 * In the result, the role {@link PresentationData#ROLE_ACCENT} stands for the given role, see
	 * {@link com.top_logic.presentation.model.ElementSpec#role()}.
	 * </p>
	 *
	 * @param role
	 *        The color role of the element, <code>null</code> for none.
	 * @param path
	 *        The path of the role in the description, for error messages.
	 * @return The writer for the element.
	 * @throws TopLogicException
	 *         If the role is not defined.
	 */
	public DrawingWriter forRole(String role, String path) {
		if (role == null || role.equals(PresentationData.ROLE_ACCENT)) {
			return this;
		}
		ColorSpec color = new ColorSpec.Role(role, Map.of());
		resolve(color, path);
		Map<String, ColorSpec> roles = new HashMap<>(_roles);
		roles.put(PresentationData.ROLE_ACCENT, color);
		return new DrawingWriter(roles);
	}

	/**
	 * Factory for the color element of an element taking a color.
	 *
	 * <p>
	 * All drawing XML elements taking a color (solid fills, gradient stops, shadows, bullet colors)
	 * offer the same choice of color elements but have no common type.
	 * </p>
	 *
	 * @param scheme
	 *        Adds a theme color element.
	 * @param rgb
	 *        Adds an RGB color element.
	 */
	public record ColorTarget(Supplier<CTSchemeColor> scheme, Supplier<CTSRgbColor> rgb) {
		// Pure data.
	}

	/**
	 * Factory for the fill element of an element that can be filled.
	 *
	 * <p>
	 * Shapes, outlines and table cells offer the same choice of fill elements but have no common
	 * type.
	 * </p>
	 */
	public interface FillTarget {

		/**
		 * Adds an element filling nothing.
		 */
		void addNoFill();

		/**
		 * Adds a solid fill.
		 */
		CTSolidColorFillProperties addSolidFill();

		/**
		 * Adds a gradient fill.
		 */
		CTGradientFillProperties addGradientFill();

		/**
		 * Adds a picture fill.
		 *
		 * @return The picture fill, <code>null</code> if the element cannot be filled with a
		 *         picture.
		 */
		CTBlipFillProperties addPictureFill();

		/**
		 * The fill target of a shape.
		 */
		static FillTarget of(CTShapeProperties spPr) {
			return new FillTarget() {
				@Override
				public void addNoFill() {
					spPr.addNewNoFill();
				}

				@Override
				public CTSolidColorFillProperties addSolidFill() {
					return spPr.addNewSolidFill();
				}

				@Override
				public CTGradientFillProperties addGradientFill() {
					return spPr.addNewGradFill();
				}

				@Override
				public CTBlipFillProperties addPictureFill() {
					return spPr.addNewBlipFill();
				}
			};
		}

		/**
		 * The fill target of a table cell.
		 */
		static FillTarget of(CTTableCellProperties tcPr) {
			return new FillTarget() {
				@Override
				public void addNoFill() {
					tcPr.addNewNoFill();
				}

				@Override
				public CTSolidColorFillProperties addSolidFill() {
					return tcPr.addNewSolidFill();
				}

				@Override
				public CTGradientFillProperties addGradientFill() {
					return tcPr.addNewGradFill();
				}

				@Override
				public CTBlipFillProperties addPictureFill() {
					return tcPr.addNewBlipFill();
				}
			};
		}

		/**
		 * The fill target of an outline, which cannot be filled with a picture.
		 */
		static FillTarget of(CTLineProperties ln) {
			return new FillTarget() {
				@Override
				public void addNoFill() {
					ln.addNewNoFill();
				}

				@Override
				public CTSolidColorFillProperties addSolidFill() {
					return ln.addNewSolidFill();
				}

				@Override
				public CTGradientFillProperties addGradientFill() {
					return ln.addNewGradFill();
				}

				@Override
				public CTBlipFillProperties addPictureFill() {
					return null;
				}
			};
		}
	}

	/**
	 * Writes a color.
	 *
	 * @param target
	 *        The element taking the color.
	 * @param color
	 *        The color to write.
	 * @param path
	 *        The path of the color in the description.
	 */
	public void writeColor(ColorTarget target, ColorSpec color, String path) {
		ColorSpec resolved = resolve(color, path);
		XmlObject element;
		if (resolved instanceof ColorSpec.Scheme scheme) {
			CTSchemeColor schemeColor = target.scheme().get();
			schemeColor.setVal(STSchemeColorVal.Enum.forString(scheme.name()));
			element = schemeColor;
		} else {
			CTSRgbColor rgbColor = target.rgb().get();
			rgbColor.setVal(rgbBytes(((ColorSpec.Rgb) resolved).rgb()));
			element = rgbColor;
		}
		writeModifiers(element, resolved.modifiers());
	}

	/**
	 * Replaces a color role by its definition.
	 *
	 * @return A {@link ColorSpec.Scheme} or {@link ColorSpec.Rgb} color.
	 */
	public ColorSpec resolve(ColorSpec color, String path) {
		Set<String> seen = new HashSet<>();
		ColorSpec result = color;
		List<Map<String, Double>> modifiers = new ArrayList<>();
		while (result instanceof ColorSpec.Role role) {
			if (!seen.add(role.name())) {
				throw new TopLogicException(I18NConstants.ERROR_COLOR_ROLE_CYCLE__PATH_ROLE.fill(path, role.name()));
			}
			ColorSpec definition = _roles.get(role.name());
			if (definition == null) {
				throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__PATH_ROLE_ROLES.fill(path,
					role.name(), String.join(", ", _roles.keySet())));
			}
			modifiers.add(0, role.modifiers());
			result = definition;
		}
		if (modifiers.isEmpty()) {
			return result;
		}
		Map<String, Double> combined = new LinkedHashMap<>(result.modifiers());
		for (Map<String, Double> outer : modifiers) {
			combined.putAll(outer);
		}
		if (result instanceof ColorSpec.Scheme scheme) {
			return new ColorSpec.Scheme(scheme.name(), combined);
		}
		return new ColorSpec.Rgb(((ColorSpec.Rgb) result).rgb(), combined);
	}

	private static byte[] rgbBytes(String rgb) {
		int value = Integer.parseInt(rgb, 16);
		return new byte[] { (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	/**
	 * Appends the color modifications to a color element in the order of
	 * {@link ColorInfo#MODIFIERS}.
	 */
	private static void writeModifiers(XmlObject color, Map<String, Double> modifiers) {
		if (modifiers.isEmpty()) {
			return;
		}
		try (XmlCursor cursor = color.newCursor()) {
			cursor.toEndToken();
			for (String modifier : ColorInfo.MODIFIERS) {
				Double value = modifiers.get(modifier);
				if (value != null) {
					cursor.beginElement(new QName(DRAWINGML_NS, modifier));
					cursor.insertAttributeWithValue(VAL, Long.toString(percent(value.doubleValue())));
					cursor.toNextToken();
				}
			}
		}
	}

	/**
	 * Writes a fill.
	 *
	 * @param target
	 *        The element to fill.
	 * @param paint
	 *        The fill, not <code>null</code>.
	 * @param sheet
	 *        The slide the filled element is placed on, which refers to the image of a picture
	 *        fill.
	 * @param path
	 *        The path of the fill in the description.
	 */
	public void writeFill(FillTarget target, PaintSpec paint, XSLFSheet sheet, String path) {
		if (paint instanceof PaintSpec.None) {
			target.addNoFill();
		} else if (paint instanceof PaintSpec.Solid solid) {
			CTSolidColorFillProperties fill = target.addSolidFill();
			writeColor(new ColorTarget(fill::addNewSchemeClr, fill::addNewSrgbClr), solid.color(),
				path + "." + PresentationData.COLOR);
		} else if (paint instanceof PaintSpec.Gradient gradient) {
			writeGradient(target.addGradientFill(), gradient, path);
		} else {
			CTBlipFillProperties fill = target.addPictureFill();
			if (fill == null) {
				throw new TopLogicException(I18NConstants.ERROR_PICTURE_LINE__PATH.fill(path));
			}
			PaintSpec.Picture picture = (PaintSpec.Picture) paint;
			String dataPath = path + "." + PresentationData.DATA;
			Pictures.Picture data = Pictures.add(sheet.getSlideShow(), picture.data(), dataPath);
			if (data.isSvg()) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNSUPPORTED_IMAGE__PATH_NAME.fill(dataPath, picture.data().getName()));
			}
			CTBlip blip = fill.addNewBlip();
			blip.setEmbed(addImageRelation(sheet, data.data()));
			if (picture.opacity() != null) {
				blip.addNewAlphaModFix().setAmt(Integer.valueOf((int) percent(picture.opacity().doubleValue())));
			}
			fill.addNewStretch().addNewFillRect();
		}
	}

	/**
	 * Adds a relation from the given sheet to the given image.
	 *
	 * @return The relation ID.
	 */
	static String addImageRelation(XSLFSheet sheet, XSLFPictureData data) {
		return sheet.addRelation(null, XSLFRelation.IMAGES, data).getRelationship().getId();
	}

	private void writeGradient(CTGradientFillProperties fill, PaintSpec.Gradient gradient, String path) {
		fill.setRotWithShape(true);
		CTGradientStopList stops = fill.addNewGsLst();
		int index = 0;
		for (PaintSpec.Stop stop : gradient.stops()) {
			CTGradientStop element = stops.addNewGs();
			element.setPos(Integer.valueOf((int) percent(stop.position())));
			writeColor(new ColorTarget(element::addNewSchemeClr, element::addNewSrgbClr), stop.color(),
				path + "." + PresentationData.STOPS + "[" + index++ + "]");
		}
		if (gradient.path() != null) {
			CTPathShadeProperties shade = fill.addNewPath();
			shade.setPath(STPathShadeType.Enum.forString(gradient.path()));
			CTRelativeRect focus = shade.addNewFillToRect();
			Integer center = Integer.valueOf((int) (PERCENT_100 / 2));
			focus.setL(center);
			focus.setT(center);
			focus.setR(center);
			focus.setB(center);
		} else {
			fill.addNewLin().setAng(angle(gradient.angle().doubleValue()));
			fill.getLin().setScaled(false);
		}
	}

	/**
	 * Writes an outline.
	 *
	 * @param ln
	 *        The outline element.
	 * @param line
	 *        The outline description.
	 * @param sheet
	 *        The slide the outlined element is placed on.
	 * @param path
	 *        The path of the outline in the description.
	 */
	public void writeLine(CTLineProperties ln, LineSpec line, XSLFSheet sheet, String path) {
		if (line.width() != null) {
			ln.setW(Units.toEMU(line.width().doubleValue()));
		}
		if (line.paint() != null) {
			writeFill(FillTarget.of(ln), line.paint(), sheet, path);
		}
		if (line.dash() != null) {
			ln.addNewPrstDash().setVal(STPresetLineDashVal.Enum.forString(line.dash()));
		}
	}

	/**
	 * Writes an outer shadow into the effects of a shape.
	 *
	 * @param spPr
	 *        The shape properties.
	 * @param shadow
	 *        The shadow description.
	 * @param path
	 *        The path of the shadow in the description.
	 */
	public void writeShadow(CTShapeProperties spPr, ShadowSpec shadow, String path) {
		CTEffectList effects = spPr.isSetEffectLst() ? spPr.getEffectLst() : spPr.addNewEffectLst();
		CTOuterShadowEffect outer = effects.addNewOuterShdw();
		outer.setBlurRad(Units.toEMU(shadow.blur()));
		outer.setDist(Units.toEMU(shadow.distance()));
		outer.setDir(angle(shadow.direction()));
		outer.setAlgn(STRectAlignment.CTR);
		outer.setRotWithShape(false);
		writeColor(new ColorTarget(outer::addNewSchemeClr, outer::addNewSrgbClr), shadow.color(),
			path + "." + PresentationData.COLOR);
	}

	/**
	 * Writes a preset geometry.
	 *
	 * @param spPr
	 *        The shape properties.
	 * @param geometry
	 *        The geometry description.
	 * @param bounds
	 *        The bounds of the shape, to which a corner radius is related.
	 */
	public void writeGeometry(CTShapeProperties spPr, GeometrySpec geometry, Rectangle2D bounds) {
		CTPresetGeometry2D preset = spPr.isSetPrstGeom() ? spPr.getPrstGeom() : spPr.addNewPrstGeom();
		preset.setPrst(STShapeType.Enum.forString(geometry.preset()));
		Map<String, Double> adjust = new LinkedHashMap<>(geometry.adjust());
		if (geometry.cornerRadius() != null && GeometrySpec.ROUND_RECT.equals(geometry.preset())) {
			double shorter = Math.min(bounds.getWidth(), bounds.getHeight());
			double value = shorter <= 0 ? 0 : geometry.cornerRadius().doubleValue() / shorter * PERCENT_100;
			adjust.put(ROUND_RECT_ADJUST, Double.valueOf(Math.min(value, ROUND_RECT_MAX_ADJUST)));
		}
		CTGeomGuideList guides = preset.isSetAvLst() ? preset.getAvLst() : preset.addNewAvLst();
		guides.setGdArray(new CTGeomGuide[0]);
		for (Map.Entry<String, Double> entry : adjust.entrySet()) {
			CTGeomGuide guide = guides.addNewGd();
			guide.setName(entry.getKey());
			guide.setFmla("val " + Math.round(entry.getValue().doubleValue()));
		}
	}

	/**
	 * Converts a fraction to a drawing XML percentage (100000 for 100%).
	 */
	static long percent(double fraction) {
		return Math.round(fraction * PERCENT_100);
	}

	/**
	 * Converts an angle in degrees to the drawing XML unit, normalized to a positive angle below 360
	 * degrees.
	 */
	static int angle(double degrees) {
		double normalized = degrees % 360;
		if (normalized < 0) {
			normalized += 360;
		}
		return (int) Math.round(normalized * ANGLE_UNIT) % (360 * ANGLE_UNIT);
	}

}
