---
description: Read before adapting PowerPoint generation to a template with pptProfile() and the profile argument of pptDeck() - layout roles (title, content, section, closing, agenda), slideLayouts with subtitle, footer, entries and contentArea, agendaLayouts, color roles (topicA..topicF, accent, risk, warning, done, muted, text, inverse), named styles (card, stat, banner, badge, arrow, timeline), rating styles of pptRating, gap, minFontSize, continuationSuffix, and copying a shape style from pptAnalyze().
order: 30
---

# Template profile

The *profile* tells `pptDeck()` what it needs to know about a template beyond its slide layouts. `pptProfile($template)`
reports it as a dictionary; it is derived from the layouts and placeholders of the template and completed by
defaults. The `profile` argument of `pptDeck()` overrides parts of it for one presentation:

```
pptDeck($template, $slides, profile: {
	"layouts": {"content": 10},
	"roles": {"topicA": "accent1", "risk": "C00000", "brand": "7A3DB8"},
	"styles": {"card": $cardStyle},
	"agendaLayouts": {"3": 3, "4": 4, "5": 5}
})
```

The override is merged into the derived profile entry by entry: an overridden role replaces the color of that role,
an overridden property of a style replaces only this property of the style; everything else keeps its derived
value. Errors in the override are reported with paths starting with `profile`.

## Layouts

- `layouts`: the index of the slide layout for each role. The roles and how they are derived:
  - `title`: the first layout of type "Title Slide", otherwise the first layout with a centered-title placeholder.
  - `content`: the first layout of type "Title and Content"; otherwise the layout with a title and the largest
    single content placeholder; otherwise a layout with a title and no large placeholder.
  - `section`: the first layout of type "Section Header".
  - `closing`: the layout of the title slide.
  - `agenda`: only if set explicitly; see `agendaLayouts`.

  A slide refers to a role by name (`pptSlide(layout: "section")`), to any layout by index or name. A role
  without a layout is an error when a slide asks for it.
- `slideLayouts`: for each layout by its index its `name`, the index of the placeholder taking the `subtitle`,
  the index of the `footer` placeholder, the `entries` placeholders of an agenda slide in reading order, and the
  `contentArea` (`x`, `y`, `width`, `height`) in which content elements without position are arranged. The
  content area is the area of the layout's large content placeholder, otherwise the area between the subtitle
  (or title) and the footer.
- `agendaLayouts`: the layout of an agenda slide by its number of entries, e.g. `{"3": 3, "4": 4}`. A layout with
  a title, no large placeholder and at least two entry placeholders of the same size is derived as the agenda
  layout for their number; `"none"` removes a derived entry. An agenda with another number of entries is shown on
  the content layout as a list with numbered badges.

## Colors, styles and ratings

- `roles`: the color for each color role. Defaults: `accent` and `topicA` ... `topicF` are the theme colors
  `accent1` ... `accent6`; `risk`, `warning`, `done` are orange, yellow and green; `muted` is a lighter text color;
  `text` is `tx1`, `inverse` is `bg1`. Any name can be added. Wherever a color is expected, a role name may be
  used; the `role` argument of a building block colors it (its style paints parts in the role `accent`, which then
  stands for the block's role).
- `styles`: the shape styles by name, used by the building blocks: `card`, `stat`, `banner`, `badge`, `arrow`,
  `timeline`. A style has the entries of a shape: `geometry`, `fill`, `line`, `shadow` and the text format
  (`size`, `color`, `bold`, `align`, `anchor`, `insets`, ...). A block takes another style with `style:`, e.g.
  `pptCard("A", "B", style: "highlight")` with `"styles": {"highlight": {...}}` in the profile.
- `ratings`: the cell styles of `pptRating(word, text)` by rating word: `met`, `partial`, `unmet`, `low`,
  `medium`, `high`, `none` (each a `fill`, a text `color`, `bold`, `align`). Own words can be added.

## Spacing and text

- `gap`: the distance between the children of rows and columns and between the content elements of a slide
  (default 17 pt).
- `minFontSize`: the smallest size a text is shrunk to (default 10 pt), see
  [Layout and fitting](doc:presentation/layout-and-fitting).
- `continuationSuffix`: appended to the title of a continuation slide (default "(cont.)" in the user's
  language).

## Taking a style from the template

The sample slides of a corporate template usually show the intended look of cards, boxes and banners. Their
shapes are reported by `pptAnalyze()` with `geometry`, `fill`, `line` and `shadow` in exactly the form a style
takes, so a style can be copied:

```
t -> {
	slide = pptAnalyze($t)["slides"][1];
	sample = $slide["shapes"].filter(s -> $s["name"] == "Rounded Rectangle 89")[0];
	card = {
		"geometry": $sample["geometry"],
		"fill": $sample["fill"],
		"shadow": $sample["shadow"],
		"line": {"color": {"role": "accent"}, "width": 1}
	};
	pptDeck($t, $slides, profile: {"styles": {"card": $card, "stat": $card}})
}
```

Shapes inside a group are listed under `children` of the group. The text format of the default style is kept,
since only the given properties are replaced. See [Analyzing and previewing a template](doc:presentation/template-analysis)
for finding the right sample.
