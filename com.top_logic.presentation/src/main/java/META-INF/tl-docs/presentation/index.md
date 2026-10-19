---
description: Read before creating PowerPoint presentations from TL-Script with the ppt functions of tl-presentation (pptDeck, pptProfile, pptAnalyze, pptRender, pptRenderLayouts, the slide and building-block descriptions pptSlide, pptContent, pptCard, pptTable, pptBarChart, ...) - overview of the chapter and of the function families.
order: 40
---

# PowerPoint presentations

The module `com.top_logic.presentation` (`tl-presentation`) creates PowerPoint presentations (`.pptx`) from
TL-Script. A presentation is described as data - dictionaries and lists built by the `ppt...` functions - and
created by `pptDeck()` on the slide layouts of a template, e.g. the corporate template of a customer. Without a
template, `pptDeck()` uses a blank 16:9 presentation.

An application that uses the functions depends on `tl-presentation`; the functions are found automatically (they
are `TLScriptFunctions` with the prefix `ppt`). The JavaDoc of each function is its reference and is shown in the
TL-Script editor.

## Function families

| Family | Functions | Purpose |
|---|---|---|
| Export | `pptDeck`, `pptProfile` | Create the presentation; report the profile of a template. |
| Slides | `pptSlide`, `pptTitle`, `pptAgenda`, `pptSection`, `pptContent`, `pptClosing` | Describe a slide on a layout role. |
| Elements | `pptShape`, `pptTextBox`, `pptTable`, `pptCell`, `pptImage`, `pptRow`, `pptColumn` | Describe the content of a slide. |
| Text and style | `pptParagraph`, `pptRun`, `pptColor`, `pptGradient`, `pptLine`, `pptShadow` | Format text and shapes. |
| Building blocks | `pptCard`, `pptBanner`, `pptBadge`, `pptStat`, `pptBullets`, `pptRating`, `pptLegend`, `pptArrow`, `pptTimeline`, `pptFlow`, `pptProCon` | Prepared elements styled by the template profile. |
| Charts | `pptBarChart`, `pptColumnChart`, `pptSeries` | Native, editable PowerPoint charts. |
| Templates | `pptAnalyze`, `pptRenderLayouts`, `pptRender`, `pptMeasureText` | Inspect a template, preview slides, measure text. |
| Images | `binaryImageSize` (`tl-model-search`) | Size of an image, e.g. to place it with its aspect ratio. |

## Articles

- [Generating a presentation](doc:presentation/generating-a-deck) - the workflow, units, the description as
  data, a complete example and the download from a view.
- [Layout and fitting](doc:presentation/layout-and-fitting) - rows, columns, weights, automatic heights,
  text overflow and continuation slides.
- [Template profile](doc:presentation/template-profile) - layout roles, content area, color roles, styles,
  ratings and agenda layouts; deriving and overriding them.
- [Analyzing and previewing a template](doc:presentation/template-analysis) - the analyse-render-generate loop
  for adapting a presentation to a new template, and the limits of the preview.
