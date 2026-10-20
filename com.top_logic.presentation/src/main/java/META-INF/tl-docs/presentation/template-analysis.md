---
description: Read before adapting PowerPoint generation to a new corporate template or checking a generated deck visually - pptAnalyze (slide size, masters, layouts, placeholders, theme colors and fonts, sample slide shapes, charts), pptRenderLayouts and pptRender (PNG previews), pptProfile, pptMeasureText; the analyse - render - profile - generate - render - correct loop for an agent, fonts installed on the server, and the known differences of the preview from PowerPoint (soft shadows, dotted lines, flat gradients).
order: 40
---

# Analyzing and previewing a template

A presentation looks right only if it uses the template's layouts, colors and styles as intended. The functions
of this article let a developer - or an AI agent that cannot open PowerPoint - see a template and the generated
slides as data and as images.

## The functions

- `pptAnalyze($template)` describes a presentation or template as JSON-compatible data: `slideSize`, the
  `masters` with their theme (colors and fonts), background and shapes such as logos, the `layouts` in the order
  PowerPoint shows them (index, name, type, placeholders with type, index, position, size, text style and prompt
  text), and the sample `slides` with their shapes (kind, name, position, geometry, fill, outline, shadow, text,
  tables and charts). A theme color is reported with its name and modifications, so a shape can follow the color
  scheme.
- `pptRenderLayouts($template)` renders each slide layout into a PNG image (`layout-NN.png`); placeholders show
  their prompt text or type, so position and text style of each become visible.
- `pptRender($deck)` renders the slides of a presentation (`slide-NN.png`), e.g. the sample slides of a template
  or a generated deck. `slides:` selects slides by index, `width:` sets the image width in pixels.
- `pptProfile($template)` reports the [template profile](doc:presentation/template-profile) derived from the
  layouts.
- `pptMeasureText(text, font, size, width: ...)` measures a text exactly as it is laid out on a slide (width of
  the widest line, number of lines, height); `fontAvailable` tells whether the font is installed.

## The loop for a new template

1. **Analyse**: `pptAnalyze($t)` - read the slide size, the layout names and placeholders, the theme colors and
   fonts.
2. **Look at the layouts**: `pptRenderLayouts($t)` - identify the title, content, section and agenda layouts and
   the content area visually.
3. **Check the profile**: `pptProfile($t)` - compare the derived `layouts`, `contentArea`s and `agendaLayouts`
   with what the images show; collect overrides for wrong or missing roles.
4. **Look at the sample slides**: `pptRender($t)` - find shapes whose style the presentation should use (cards,
   banners) and copy them from `pptAnalyze($t)["slides"]` into `styles` (see the template profile article);
   define color `roles` for the template's palette.
5. **Generate**: `pptDeck($t, $slides, profile: $override)`.
6. **Render and check**: `pptRender($deck)` - look for cut-off texts ("…"), too small fonts, crowded or empty
   slides, wrong colors.
7. **Correct** the description or the profile and repeat from step 5.

## Fonts

Text is measured and drawn with the fonts installed on the server. A template font that is missing is replaced
by a standard font: the preview then differs from PowerPoint, and - more important - the fitting of texts
(shrinking, cutting, row heights, automatic heights) is computed with the wrong metrics, so texts may overflow or
be shrunk needlessly in PowerPoint. Install the template's fonts on the server (or a metric-compatible
substitute, e.g. Carlito for Calibri). `pptRender()` and `pptRenderLayouts()` log the names of missing fonts as a
warning; `pptMeasureText()` reports `fontAvailable`.

## Known differences of the preview

The images are drawn by Apache POI, not by PowerPoint. Expected differences:

- Soft (blurred) shadows are drawn as hard offset shadows.
- Dotted and dashed outlines may look different or solid.
- Gradients are drawn simplified; radial and multi-stop gradients may look flat.
- Bar and column charts are drawn from their data; other kinds of charts appear as a frame naming the chart
  kind. SmartArt diagrams and some effects (glow, reflection, 3D) are not drawn.
- Vector images (SVG) are drawn from their raster alternative.

These differences concern the preview only - the `.pptx` contains the full formatting.
