---
description: Read before writing a TL-Script that creates a PowerPoint presentation with pptDeck() - the workflow, units in points, pptTitle / pptAgenda / pptContent / pptSection / pptClosing slides with notes and footer, slide content from HTML / structured text with pptHtml (supported tags, images of a tl.model.wysiwyg:Html value), storing a description as JSON (toJson / parseJson), a complete example and offering the file as a download from a .view.xml command (<execute-script> plus <download/>).
order: 10
---

# Generating a presentation

## Workflow

1. Describe the slides with the slide functions: `pptTitle()`, `pptAgenda()`, `pptSection()`, `pptContent()`,
   `pptClosing()`, or the general `pptSlide()`. A slide names its slide layout by a role (`title`, `content`,
   `section`, `closing`, `agenda`), by its index or by its name; the [template profile](doc:presentation/template-profile)
   maps the roles to the layouts of the template.
2. Fill the content of a slide with elements: building blocks (`pptCard`, `pptStat`, `pptBanner`, `pptTimeline`,
   ...), tables, charts, images, shapes and text boxes, arranged in rows and columns (see
   [Layout and fitting](doc:presentation/layout-and-fitting)).
3. Create the file with `pptDeck(template, slides, footer: ..., profile: ..., name: ...)`. The result is a
   `BinaryData` (`.pptx`) that can be stored in a `tl.core:Binary` attribute, attached to a mail or downloaded.
4. Check the result: `pptRender($deck)` renders the slides into PNG images, see
   [Analyzing and previewing a template](doc:presentation/template-analysis).

## Template

`template` is a `.pptx` or `.potx` file whose slide masters and layouts give the backgrounds, logos, fonts and
colors. Its own sample slides are not part of the result. Without a template (`null`), `pptDeck()` creates the
slides on the standard layouts of a blank presentation in the format 16:9 ("Title Slide", "Title and Content",
"Section Header", ...). `pptProfile()` without argument reports the profile of this blank presentation.

A layout without a placeholder for the subtitle (e.g. "Title and Content") shows the subtitle of a slide as the
first content element.

## Units and values

- All lengths are points (1/72 inch), positions relative to the top left corner of the slide. A 16:9 slide is
  usually 960 x 540 points; `pptAnalyze($template)["slideSize"]` tells the size of a template.
- A text is a string (each line a paragraph), a `pptParagraph()`, a `pptRun()` or a list of those. Numbers are
  accepted as text. Model objects are not: pass their name, e.g. ``$m.get(`my.module:Milestone#name`)``.
- A color is a theme color (`accent1`, `tx1`, `bg1`, ...), a hexadecimal color (`RRGGBB`), a color role of the
  profile (`topicA`, `risk`, `done`, `muted`, ...) or a `pptColor()` dictionary.
- Every slide takes speaker notes (`notes:`), a text or a list of lines. `pptDeck(footer: ...)` fills the footer
  placeholder of every layout that has one.

## Content from HTML

`pptHtml(html)` turns structured text into slide content: the value of an HTML attribute
(`tl.model.wysiwyg:Html`, with the images stored in it) or a string of HTML. The result is a column with
`split: true` (see [Layout and fitting](doc:presentation/layout-and-fitting#continuation-slides)), so a long
text continues on further slides:

```
pptContent("#3 Acceptance test", null, pptHtml($ticket.get(`my.module:Ticket#description`)))
```

The blocks of the text keep their order:

| HTML | Slide content |
|---|---|
| `p`, `div`, `br`, `h1`-`h6` | Paragraphs of a text box; `br` breaks the line, headings are bold and larger (`h1` 1.6 times the base size) and kept with the content following them (`keepWithNext`). |
| `b`/`strong`, `i`/`em`, `u`, `a href` | Bold, italic, underlined runs, hyperlinks. |
| `code`, `pre` | Monospace font ("Courier New"); `pre` keeps its lines. |
| `ul`, `ol`, `li` | Bullets and numbers (`start`, `type`, `value` respected), nested lists indented by level. |
| `blockquote` | Paragraphs indented by one level. |
| `table` | A `pptTable()`; leading rows in `thead` or of `th` cells are header rows, repeated on continuation slides. |
| `img` | A `pptImage()` as wide as its `width` attribute or the image itself (a pixel is 3/4 point), at most as wide as the column. |

- `src` of an image names an image of the structured text (as stored by the HTML editor, `ref:<name>`, or just
  `<name>`), or is a `data:` URI. An image that cannot be found is left out with a warning in the log.
- Text of other elements flows on as text; `script` and `style` are ignored. White space is collapsed as in a
  browser.
- A cell spanning several columns or rows (`colspan`, `rowspan`) is followed by empty cells; cells are not
  merged. Images and tables inside a table cell contribute their text only.
- `font`, `size` (default 16) and `color` set the base text style, `gap` the distance between text boxes,
  tables and images.
- `pptHtml(null)` and an empty text give a column without children, the slide shows no content.

## The description is data

The `ppt...` builder functions only assemble dictionaries; nothing is checked before `pptDeck()` reads the
complete description and reports errors with the path of the offending entry (e.g. `slides[3].content[0].fill`).
Consequences:

- A description can be computed by any TL-Script: `map`, `filter`, `concat`, conditionals.
- It can be stored and exchanged as JSON: `toJson($slides)` gives a string for a `tl.core:String` attribute,
  `parseJson($json)` reads it back for `pptDeck()`. An AI agent or another system can produce the same JSON.
- A dictionary written by hand is equivalent to the builder call, e.g. `{"type": "card", "heading": "A",
  "body": "B"}` for `pptCard("A", "B")`.

## Example

A status report of a project with title, agenda, key figures, a timeline, a chart and a table of open tickets that
continues on further slides when it is long (the full version is the "Status report (PowerPoint)" command in
`com.top_logic.demo.react`, `projects/overview.view.xml`):

```
p -> {
	tickets = $p.get(`my.module:Project#tickets`);
	open = $tickets.filter(t -> !$t.get(`my.module:Ticket#done`));
	pptDeck($p.get(`my.module:Project#reportTemplate`), [
		pptTitle($p.get(`my.module:Project#name`), "Status report", notes: "Status at a glance."),
		pptAgenda(["Key figures", "Open tickets"]),
		pptContent("Key figures", null, [
			pptRow([
				pptStat($open.size(), "open tickets", role: "risk"),
				pptStat($tickets.size() - $open.size(), "done tickets", role: "done")
			], height: "auto"),
			pptColumnChart(["open", "done"], [pptSeries("Tickets", [$open.size(), $tickets.size() - $open.size()])])
		], notes: "Open and done tickets."),
		pptContent("Open tickets", null, pptTable(
			concat([["Ticket", "Priority"]], $open.map(t -> [
				$t.get(`my.module:Ticket#title`),
				pptRating("high", "high")
			])),
			headerRows: 1, columnWeights: [4, 1]))
	], footer: "Status report", name: "status.pptx")
}
```

## Download from a view

In a `.view.xml`, a command runs the script with `<execute-script>` and hands the resulting file to the browser
with `<download/>` (`DownloadAction`):

```xml
<generic-command image="css:bi bi-file-earmark-slides" input="project" placement="TOOLBAR">
	<label>
		<en>Status report (PowerPoint)</en>
		<de>Statusbericht (PowerPoint)</de>
	</label>
	<executability>
		<null-input-disabled/>
	</executability>
	<execute-script>
		<function><![CDATA[ p -> pptDeck(...) ]]></function>
	</execute-script>
	<download/>
</generic-command>
```

Texts shown on the slides can follow the language of the user with internationalized literals:
`#('Open tickets'@en, 'Offene Tickets'@de).localize()`.
