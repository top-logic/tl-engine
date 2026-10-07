---
description: Read before showing text, pictures or values in a .view.xml - <text> variant/tone/appearance, <image>, <overlay>, <avatar>, <html>, colored values and progress display.
order: 30
---

# Displaying values

## Text: variant, tone and appearance

A `<text>` says what it is *for* rather than which font and colour it is drawn in. Three properties
carry that, each one a role filled from the design tokens of the active theme, so a theme restyles
every text of a role at once — see
[react-theme-tokens.md](doc:view-layer/theme-tokens#typography-tokens) for the token per variant.

```xml
<stack>
	<text
		label="Quarterly report"
		variant="headline"
	/>
	<text
		label="Figures as of yesterday."
		tone="helper"
		variant="caption"
	/>
	<text
		appearance="pill"
		label="Overdue"
		tone="error"
	/>
</stack>
```

- **`variant`** — what the text is for: `body` (the default), `title`, `headline`, `display`,
  `label`, `caption`. The family, size, line height and weight come from the tokens.
- **`tone`** — what its colour means: `primary` (the default), `secondary`, `helper`, `accent`,
  `success`, `warning`, `error`, `on-color`.
- **`appearance`** — the shape it is drawn in: `text` (the default) or `pill`.

Every text carries a class per role — `tlText--<variant>`, `tlText--tone-<tone>`, and
`tlText--pill` where the appearance asks for one — beside `tlText` and the element's own
`css-class`:

```html
<span class="tlText tlText--caption tlText--tone-helper">Figures as of yesterday.</span>
```

The classes are a stable contract the stylesheet of the application may read, not something to
write into a `css-class`: a display option of the element is a property of the element.

The colour of a tone travels as the custom property `--tlText-tone`, which the text colour is taken
from and which tints a pill that has no colour of its own — one declaration per tone. That is also
how a text keeps its tone where the surrounding control re-maps the text colour: the primary app bar
re-maps `.tlAppBar--primary .tlText--tone-primary`, so a text that follows the default tone reads in
the on-accent colour while a text that states a tone of its own — an error, a success — keeps it.

## Pictures: `<image>`, `<overlay>`, `<avatar>`

`<image>` (`ImageElement`) shows one picture, and it takes that picture from either of two places.

- **From a channel** (`input`). The channel value is either **picture data** — a `BinaryData` whose content type starts with `image/`, e.g. the binary attribute of a model object or the result of an upload — or a **text naming an address**. Who serves the bytes differs: picture data is served by the control itself through its data endpoint (`ImageSource`, `hasData` plus a `dataRevision` the client appends so a replaced picture is not taken from the browser cache), while an address is loaded by the browser directly. Any other value — no value, binary data that is no picture, an unrelated object — shows no picture.
- **From a resource of the web application** (`resource`, e.g. `/images/logo.svg`, resolved against the context path). On its own it *is* the picture; together with `input` it is the placeholder shown as long as the channel holds no picture.

The box the picture is shown in is described by `aspect-ratio` (`16/9`, so a row of pictures of differing originals stays even), `width` and `height` (CSS lengths); with none of them the box takes the size of the picture, limited to the width available. `fit` decides what a picture whose proportions differ from the box does with it: `cover` (the default) crops it to fill the box, `contain` fits the whole picture into it. `lazy="true"` lets the browser postpone the loading until the box comes close to the visible part of the page — right for the thumbnails of a long card grid, wrong for a picture the user sees at once. `alt` says what the picture shows for a reader who cannot see it, and `css-class` adds a class to the box.

`<overlay>` stacks content over a base: its **first child is the base**, every further child is a layer over it. The base gives the overlay its height; its width is what the surrounding layout grants, and a base sized relative to it (`width="100%"`) fills it. A base of fixed width wants a container that does not stretch its items (`<stack align="start">`), or the overlay is stretched past the base and anchors its layers to the free space beside it. A `<layer position="fill|top-left|top|top-right|left|center|right|bottom-left|bottom|bottom-right" css-class="…">` brings the position its content takes and a class of its own; a child written without a layer covers the base as a whole. A layer passes the pointer through wherever it shows nothing (`.tlOverlay__layer` is `pointer-events: none`, its content `auto`), so the base stays usable below the free space of a layer that only anchors a badge. Placement comes from the element, the look from application CSS on the layer's class — a badge pill, a caption scrim.

`<avatar input="ch" image="photoCh" size="small|medium|large|x-large"/>` shows the picture of the `image` channel circle-cropped, and the initials of the `input` value's label while there is none, on one of the eight category roles of the design system (`tl-avatar--category-<n>`, derived from the label, the same two tokens a pill of that category reads). The four sizes are words (`medium` when absent); the client maps them to the classes `tl-avatar--sm|lg|xl` and the tokens `size-avatar-sm` to `size-avatar-xl`, and they do not follow the density; the initials carry the type class of their size. The picture follows its channel, so a photo replaced elsewhere appears without the avatar being built anew.

```xml
<overlay>
	<image
		aspect-ratio="4/3"
		fit="cover"
		input="photo"
		resource="/images/no-picture.svg"
		width="20rem"
	/>
	<layer
		css-class="tlDemoBadge"
		position="top-left"
	>
		<text>
			<label>
				<en>Preview</en>
			</label>
		</text>
	</layer>
	<layer
		css-class="tlDemoCaption"
		position="bottom"
	>
		<text input="node"/>
	</layer>
</overlay>
```

The client classes an application styles against are `.tlImage` / `.tlImage__image`, `.tlOverlay` / `.tlOverlay__layer` / `.tlOverlay__layer--<anchor>` and `.tl-avatar` / `.tl-avatar--<size>` / `.tl-avatar--category-<n>` / `.tl-avatar__image` (design system, `avatar.css`). The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/image-demo.view.xml` with `style/tl-demo-react.css`.

## Ready-made HTML: `<html>`

`<html input="ch">` (`HtmlElement`) displays HTML the application did not compose itself — the answer of an agent, a generated exposé, an imported page. The channel carries that content in one of three shapes, and `HtmlValues` reduces all of them to the source text to display:

- a `String` holding the source,
- an `HTMLFragment` — what an HTML literal `{{{ … }}}` of a script expression evaluates to — rendered to its source,
- a `BinaryData` of content type `text/html`, read with the charset that content type declares. `binary('expose.html', $source, 'text/html')` builds one in TL-Script, and an uploaded file arrives as one anyway.

A value of any other type has no HTML representation; the element reports that in its place, the same way it reports content a check refuses. Nothing on the channel is no content and no failure.

`display` decides how the content is shown, and with it what stands between it and the reader.

**`inline`** (the default) inserts the fragment into the page where the element stands, after `SafeHTML` has checked it against the application's whitelist — a script, an attribute carrying one, anything else the check refuses is not inserted, and the message of the check takes its place. The fragment brings its structure and the page gives it typography: the stylesheet gives `.tlHtml--inline` the theme's text color and font and spaces the elements a fragment is made of, so an answer reads as a section of the page it lands in. `css-class` adds a class of the application's own beside it.

```xml
<html input="answer"/>
```

**`document`** shows the content as a page of its own, in a sandboxed frame filling the space the element is given. The frame is not handed the source: it fetches it from the control's data endpoint (`ReactHtmlControl` is the `DataProvider`), with the current `dataRevision` in its URL, so the document crosses the wire once and a replaced one is fetched rather than taken from the browser cache. The sandbox runs no script, which is why a document needs no whitelist check — it keeps the styles it brings along and takes none of the page's. `print="true"` puts a button on it that hands the frame to the browser's print dialog, which is also where the browser offers saving the document as a PDF file, with the document's own styles.

```xml
<html
	display="document"
	input="current"
	print="true"
/>
```

**`thumbnail`** shows that same isolated document as a picture of itself: a card-sized preview for a grid of documents. The frame is laid out at `thumbnail-width` × `thumbnail-height` CSS pixels — 800 × 1130 by default, a portrait page at that width — and scaled down to the width the preview box measures (a `ResizeObserver` on the box, `transform: scale(…)` from its top left corner), so the document appears in its own proportions instead of being reflowed into a small one. The box keeps the aspect ratio of the two sizes and cuts off what is taller, as a page does. A preview is looked at rather than used: it takes no clicks, no focus and no print button, and it is fetched only once it comes near the viewport, so a grid loads the previews the reader actually reaches. Selecting one is therefore the business of whatever carries it — a button in the card writing the element to a channel, for instance.

```xml
<object-list inputs="exposes" items="exposes -> $exposes" layout="grid" max-columns="3">
	<item>
		<card padding="compact">
			<html display="thumbnail" input="element"/>
			<text css-class="tlText--strong" input="element"/>
			<button appearance="link">
				<action class="com.top_logic.layout.view.command.GenericViewCommand" input="element">
					<label><en>Show</en></label>
					<write-channel name="expose"/>
				</action>
			</button>
		</card>
	</item>
</object-list>
```

The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/html-demo.view.xml`: an inline agent answer and a refused fragment above, the three exposés as a grid of previews beside the selected one below.

**PDF, server-side.** The print dialog is the reader's way to a PDF. An application that produces the file itself — to store it, mail it, attach it — converts the HTML in TL-Script instead: `pdfFile($html, name: "expose.pdf")` yields a `BinaryData` that `<pdf input="ch"/>` displays and a download hands out. That conversion is Flying Saucer rather than a browser, so it takes well-formed XHTML and CSS 2.1 and renders a document written for it, not any page a browser shows.

## Colored values

A value's color is part of the model: two annotations say where it comes from, and one seam answers it.

- **`<color>`** on an enumeration literal (`TLColor`) gives that literal its color as a **role** of the design system (`<color role="success"/>`): `neutral`, `brand`, the four meanings `error`, `warning`, `success`, `info`, or one of eight categories `category-1` … `category-8` that only tell values apart. Neither a literal color value nor a token name is a valid specification; how a role looks, in each theme and mode, is the design system's decision. A literal without the annotation has no color.
- **`<dynamic-color>`** on a type (`TLDynamicColor`) holds the `ValueColorProvider` computing the color of its instances, in the shape `<dynamic-icon>` and `<label>` use for icons and labels. `ColorByExpression` states that algorithm as a TL-Script expression over the object: the demo ticket takes its color from its status, `<dynamic-color><color-by-expression color="t -> $t.get(`demo.tickets:Ticket#status`)"/></dynamic-color>`, so a ticket appears in the color of the status literal it holds. An enumeration literal as the result is colored by its own `<color>`, the name of a role (`x -> 'category-3'`) is that role, any other result — a `Color` value included — and `null` leave the object uncolored. Specializations inherit the annotation.
- **`AnnotationValueColorProvider.INSTANCE`** (a `ValueColorProvider`) answers both: `colorOf(value)` returns the `ValueColor` — the role — of a classifier or an object, and `null` for everything the model gives no color to. `ValueColor` is an `ExternallyNamed` enum; its external name is what reaches the client.

A colored value is displayed as a **pill** in its role, an uncolored one as plain text. The role travels as the single state / descriptor field `ReactValueColor.ROLE` (`colorRole`), filled by `ReactValueColor.putRole(descriptor, value)`; never CSS. One shared presentational component `TLPill` (`react-src/controls/pill/TLPill.tsx`) draws it everywhere as `tl-pill tl-pill--<role>` with a `tl-pill__label`, and nothing but that class is set. The design-system stylesheet (`style/tl-design-system/components.css`, `pill.css` in the `tl-design-system` repository) gives each role its two tokens — the tinted surface and its line — per mode; the text on it is `text-primary`. Decision and reasoning: `docs/skalen.md`, "Pill: Rolle statt Farbe".

`<text appearance="pill">` asks for a pill whether or not the value carries a colour: a badge, a
status, a tag written as a text rather than read off the model. The colour then follows a
precedence — the colour of the value when the model gives it one, and the colour of the element's
`tone` otherwise — so a status that *is* an enumeration literal keeps the literal's colour, and a
text that is a badge of the application's own making takes its tone. `appearance="text"` (the
default) leaves it as it was: a pill for a value with a colour, plain text for one without. A pill
asked for by the element reads in the size of the text's own `variant`; one the value's colour
produced reads in the pill's own size.

The sites that fill the field:

- **`ReactDropdownSelectControl`** — every option and every selected value passes through its one descriptor factory, so the pill appears in the read-only display of a reference or enumeration attribute (a `<table>` cell, a view-mode `<form>` field), on the chips of the selection while editing, and on the rows of the open dropdown.
- **`ReactResourceCellControl`** — resource cells and tree nodes, beside the type icon.
- **`ReactTextControl`** / `<text>` (`TextElement`) — a channel value rendered through `MetaLabelProvider`; `setText(text, cssColor)` updates both in one patch.

## Progress

A fraction between 0 and 1 is displayed as a bar with an optional label beside it. `ReactProgressControl` (`TLProgress`) holds the two state entries `FRACTION` and `LABEL` and nothing else - what the fraction counts is the caller's business. A number outside the range is drawn at the end it exceeds, so two counts that disagree give a full or an empty bar rather than one running past its track; `setProgress(fraction, label)` updates both in one patch.

`<progress>` (`ProgressElement`) states the bar one of two ways, never both:

- `<progress input="ch" fraction="x -> …"/>` — the filled part directly. Such a bar carries no label unless `label="x -> …"` gives it one.
  A fraction expression that answers nothing at all leaves the bar without a share: the client marks the bar `aria-busy` and the design-system stylesheet (`tl-progress`, `progress.css`) sweeps a partial fill over the track instead of filling a share of it, which is how a bar over an operation that does not know how far it has come is written — and an operation that learns its share later switches between the two displays by reporting a number again.
- `<progress input="ch" done="x -> …" total="x -> …"/>` — the two counts the fraction is the ratio of, which are also the label (`3 / 7`) unless `label=` replaces it. A total of zero leaves the bar empty.

Every expression is called with the current value of the `input` channel, which is optional: a bar counting the model as a whole needs none. The bar recomputes on a new channel value, on a change of the object the channel holds, and on a create / change / delete of an `observed-types` type — the last is what a bar counting all objects of a type needs, since no channel value changes when one is added. The observation is the shared `ChannelObjectObserver`, attached and detached with the control.

A table cell needs nothing new: a `CellRenderer` yields `new CellContent.Raw((CellControlFactory) ctx -> new ReactProgressControl(ctx, fraction, label))`, the escape hatch `CellContentReactAdapter` already resolves.
