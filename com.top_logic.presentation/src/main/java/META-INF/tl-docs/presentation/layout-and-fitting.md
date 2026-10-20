---
description: Read before arranging the content of a PowerPoint slide built with pptContent / pptSlide - pptRow and pptColumn with gap, weight, weights, align and height auto, positioned elements (x, y, width, height), the content area, text overflow (shrink, ellipsis, visible, error, minFontSize), numbered paragraphs (bullet autoNumber, start), and continuation slides with headings kept with the next paragraph (keepWithNext) for long tables (pptTable), split texts (pptTextBox split, pptBullets split) and split columns (pptColumn split).
order: 20
---

# Layout and fitting

## Content area and arrangement

Each slide layout has a *content area* (see [Template profile](doc:presentation/template-profile)): the area of
its large content placeholder, or the area between title (or subtitle) and footer. The content elements of a
slide without a position are arranged one below the other in the content area, like the children of a
`pptColumn()`, separated by the `gap` of the profile (default 17 pt).

An element with `x` or `y` is placed there and not arranged with the others. A size it does not give extends to
the right or bottom border of the content area.

## Rows and columns

`pptRow(children)` arranges elements side by side, `pptColumn(children)` one below the other. They nest
arbitrarily.

- **Own size**: a child with `width` (in a row) or `height` (in a column) gets exactly this size.
- **Automatic height**: `height: "auto"` gives a child the height of its content at its width - the height of its
  text, of its table rows, of the highest child of a row. `pptBanner`, `pptLegend`, `pptTimeline` and `pptFlow`
  default to automatic height when they give neither height nor weight.
- **Weights**: all other children share the remaining space by their `weight` (default 1), or by the `weights`
  list of the container. `pptRow([a, b], weights: [2, 1])` gives `a` two thirds of the width left over.
- **Across the direction**: a child is stretched to the width of a column or the height of a row. In a row,
  `align: "start" | "center" | "end"` gives the children the height of their content instead. A child of a
  column is at most as wide as the column.
- `gap` sets the distance between the children; without it, the profile's `gap` applies.

If the children of a column with own or automatic height need more space than the column has, their automatic
heights are reduced proportionally - texts then shrink as described below.

Typical patterns:

```
pptContent("Key figures", null, [
	pptRow([pptStat(12, "open"), pptStat(30, "done"), pptStat(3, "milestones")], height: "auto"),
	pptRow([pptCard("A", "..."), pptCard("B", "...", weight: 2)]),   // takes the remaining height
	pptBanner("One message")                                         // automatic height
])
```

A row of `pptStat` cards without `height: "auto"` takes its weighted share of the content area and the cards
become tall; giving the row automatic height keeps them compact.

## Text overflow

Text that does not fit its element is handled by the element's `overflow`:

| `overflow` | Effect |
|---|---|
| `shrink` (default) | Reduces all font sizes of the text by a common factor until it fits, not below the profile's `minFontSize` (default 10 pt); if it still does not fit, cuts it off with "…". |
| `ellipsis` | Cuts the text off after the last fitting word with "…", without shrinking. |
| `visible` | Leaves the text as it is; it may extend beyond the element. |
| `error` | Stops the creation with an error naming the element's path. |

In a table cell, `overflow` applies to a single word wider than the column; rows grow with their text. The
measurement uses the fonts installed on the server, see
[Analyzing and previewing a template](doc:presentation/template-analysis); `pptMeasureText()` measures a text in
advance, e.g. to choose a width.

## Numbered paragraphs

A paragraph's `bullet` can be a number counted by PowerPoint: `pptParagraph("Step", bullet: {"autoNumber":
"arabicPeriod", "start": 3})` shows "3.". `autoNumber` names a numbering scheme (`arabicPeriod` 1., 2., ...,
`arabicParenRight` 1), 2), ..., `alphaLcPeriod` a., b., ..., `alphaUcPeriod`, `romanLcPeriod`, `romanUcPeriod`
and the other schemes of PowerPoint), `start` the number of the paragraph. A paragraph without `start` continues
the numbering of the preceding paragraph of the same scheme. `char` and `autoNumber` exclude each other;
`color` colors the number.

`pptHtml()` gives each item of an ordered list its number as `start`, so that the numbering stays correct when a
list continues on a further slide.

## Continuation slides

Three kinds of elements continue on further slides instead of being cut:

- A `pptTable()` whose rows do not fit into its height. The continuation slides repeat the header rows
  (`headerRows`) and the placeholder texts of the slide; the title gets the profile's `continuationSuffix`
  (default "(cont.)" in the user's language). On continuation slides the table fills the whole content area,
  below the subtitle if the layout shows the subtitle as content element (see
  [Generating a presentation](doc:presentation/generating-a-deck#template)).
- A text box or bullet list with `split: true` (`pptTextBox(..., split: true)`, `pptBullets(..., split: true)`),
  continued paragraph by paragraph.
- A column with `split: true` (`pptColumn(children, split: true)`), continued child by child. Each child takes
  its own height or the height of its content (weights do not apply). The children that fit stay on the slide;
  the first child that does not fit
  - shows the rows that fit if it is a table, the paragraphs that fit if it is a text with `split: true` - as
    long as at least its first row or paragraph fits, otherwise it starts on the next slide;
  - moves to the next slide as a whole otherwise (an image, a chart, a row, ...).

  The first child on a slide is always shown; one that is higher than the whole content area on its own is
  reduced to its height (an image keeps its aspect ratio). `pptHtml()` creates such a column.

A paragraph with `keepWithNext: true` (`pptParagraph(..., keepWithNext: true)`, set by `pptHtml()` on headings)
never ends a slide when its text continues: it moves to the next slide together with the paragraph after it. In a
split column, a text whose last paragraphs keep with the next one moves them along with the following child (a
table, an image, ...) when that child starts on the next slide. Only if the slide would become empty, the paragraph
stays.

Only one element of a slide can be continued; the other content elements and the notes appear on the first slide
only. A long table therefore belongs on a slide of its own or at the end of a column. Within a split column, the
children themselves are the parts that continue: a table nested in a row of a split column cannot continue.
