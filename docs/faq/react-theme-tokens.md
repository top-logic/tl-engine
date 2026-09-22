# FAQ: Theme tokens in React stylesheets

## How a token reaches the CSS

The design tokens of the React UI are declared in
`com.top_logic.layout.react/src/main/webapp/WEB-INF/conf/tl-react-theme.config.xml`, the
configuration of the `UIThemeService`. Each `<theme>` holds a list of tokens; a token is a `<color>`
(`#rrggbb`), a `<length>` (`0` or a number with a unit), a `<number>`, a `<text>` (a raw CSS value,
e.g. a shadow) or a `<ref>` naming another token.

`UIThemeService.writeThemeStyles` emits every token of every theme verbatim as `--<name>:<value>`
into an inline `<style>` element of the React page, in a block scoped by the theme's
`data-theme` attribute; the default theme is bound to `:root` in addition. So a token named
`corner-radius` is a custom property `--corner-radius` and is consumed as `var(--corner-radius)`.

A theme `extends` another one and overrides only the tokens that differ; the emitted block carries
the inherited tokens as well, so every theme declares the full set.

The stylesheets under `src/main/webapp/style/` are served as plain files — there is no build step
between the sheet and the browser, so what is written there is what the browser sees.

**Write `var(--name)` without a fallback.** A `var(--name, 0.5rem)` hides the fact that the theme is
missing the token: the page renders, silently ignoring the theme. A token the theme does not declare
is a defect to fix in the theme. The only custom properties that carry a fallback are the ones a
stylesheet declares itself (`--page-inset`, `--cal-ev-bg`, …), where the fallback is the value of the
default case rather than a stand-in for a missing token.

## Naming a token in a model configuration

A model configuration names a token where it asks for a design value. The `<color>` annotation of an
enumeration classifier is such a place:

```xml
<classifier name="open">
	<annotations>
		<color token="support-warning"/>
	</annotations>
</classifier>
```

The pill a value of that classifier is drawn as takes the token as its colour: the value is sent as
`var(--support-warning)` and set into the custom property `--tlPill-color`, so the pill follows a
theme switch.

The legal names are the colour tokens of the themes: the union, over the `<theme>` elements of
`tl-react-theme.config.xml`, of the `<color>` tokens and of the `<ref>` tokens ending in one. A token
of another kind is not a colour: `spacing-02` is a `<length>`, `shadow-menu` a `<text>`, and neither
is accepted where a colour is asked for.

The colour tokens are offered as the options of the annotation, so the model editor shows a
drop-down of the names the application emits rather than a free-text field. A token the application
does not emit is kept alongside them: the form shows it, and saving the form writes it back
unchanged, so an annotation written against a theme the editing application does not install
survives. A name can therefore be chosen or kept, but not newly typed.

A name no theme emits is reported as a warning naming the token and listing the known names
(`WARNING_NO_SUCH_COLOR_TOKEN__TOKEN_KNOWN`) in the boot log, where the annotation of a
`*.model.xml` is read. The warning does not block: the application starts, and the value is
displayed without a colour.

The vocabulary comes from `DesignTokenService`. An application with the React UI answers it with
`UIThemeDesignTokens`, the tokens of `UIThemeService`; an application without it with
`ThemeDesignTokens`, the CSS variable block of the classic `Theme` settings. The two are different
sets, because a page of the React UI carries the custom properties of the `UIThemeService` themes
and a classic page those of its `Theme`. A name valid in one is therefore not necessarily valid in
the other: `support-danger` is a classic theme setting, `support-error` the React token for the same
job. An application that declares no vocabulary of a kind is not checked for it — there is then
nothing to tell a valid name from an invalid one.

A `<ref>` takes the kind of the token it names, followed along the chain of references and across
the theme inheritance, so an alias of a colour is a colour. A `<ref>` naming no token, and a cycle
of references, are reported as errors while the themes are read.

## The three radius tiers

Rounding comes in exactly three tiers, and every rounded box takes one of them:

| Tier | Token | Default | Applies to |
| --- | --- | --- | --- |
| Control | `corner-radius` | `0.25rem` | buttons, input fields, drop-down and colour/icon fields, toggles, table and calendar cells, small chips, tree toggles, palette cells, sliders and handles, reset buttons, scrollbar thumbs, drop markers |
| Surface | `border-radius-02` | `0.5rem` | panels, cards, windows, drawers, menus, drop-down popups, flyouts, the snackbar, colour and icon popups, tooltips, dashboard tiles, form groups |
| Large surface | `border-radius-03` | `1rem` | a surface large enough for the surface radius to disappear on it: a hero section, a glass card, a full-bleed panel an application styles through its `css-class` |

The rule of thumb: a box the pointer acts *on* is a control, a box that *holds* other boxes is a
surface, and a surface that fills a good part of the viewport is a large one. The engine's own
controls take the first two tiers; the third is there so an application styling a surface of its own
rounds it by a token of the theme rather than by a literal of its own.

**A shape is not a rounding.** A circle stays `50%` and a pill stays `999px`, written literally, so
both keep their shape in a theme that squares every corner off. The same goes for a deliberate
`border-radius: 0` reset (a table's inline edit input, a menu item's button) and for the tiny radii
of an icon glyph drawn out of a box (the audio recorder's stop square, the camera icon) — those
corners belong to the glyph, not to a control.

## The elevation scale and the overlay

Drop shadows come in three steps, each a `<text>` token, and a backdrop takes the overlay token:

| Token | Default | Applies to |
| --- | --- | --- |
| `shadow-raised` | `0 0 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.08)` | a surface lifted off the page: an elevated app bar or card, the edge of a frozen or pinned table column, a palette cell under the pointer |
| `shadow-menu` | `0 2px 6px 0 rgba(0,0,0,0.32)` | a popup opened from a control: menus, toolbar drop-downs, sidebar flyouts, option lists, colour and icon palettes, tooltips |
| `shadow-dialog` | `0 8px 24px rgba(0,0,0,0.2)` | a layer covering the page: windows, drawers, the snackbar, the open sidebar drawer |
| `overlay` | `rgba(0,0,0,0.5)` | the backdrop such a layer dims the page with: the dialog backdrop, the sidebar backdrop |
| `surface-blur` | `0` | how far a backdrop blurs the page behind it, consumed as `backdrop-filter: blur(var(--surface-blur))`; the dialog backdrop takes it, and `0` leaves the page sharp |

A shadow that draws a ring rather than an elevation — a focus ring, a button's inset frame, the
outline of a colour handle — is not part of this scale and keeps its own value.

`shadow-glow` (`none`) stands beside the scale rather than in it: a glow marks a surface as
highlighted — the active step of a wizard, the card a drag is about to land on — and says nothing
about how far above the page it sits. A theme that wants one sets it, an application consumes it on
the surface it styles, and the default leaves every surface without one.

## Typography tokens

The `<text>` element states what a text is for as a
[variant](react-view-layer.md#text-variant-tone-and-appearance), and the stylesheet fills the role
from these tokens:

| Variant | Family | Size | Line height | Weight |
| --- | --- | --- | --- | --- |
| `display` | `font-family-display` | `display-01-font-size` (`2.5rem`) | `display-01-line-height` (`3rem`) | 600 |
| `headline` | `font-family-display` | `heading-04-font-size` (`1.75rem`) | `heading-04-line-height` (`2.25rem`) | 600 |
| `title` | `font-family-display` | `heading-03-font-size` (`1.25rem`) | `heading-03-line-height` (`1.625rem`) | 600 |
| `body` | `font-family` | `body-compact-01-font-size` (`0.875rem`) | `body-compact-01-line-height` (`1.125rem`) | inherited |
| `label` | `font-family` | `heading-compact-02-font-size` (`0.875rem`) | `heading-compact-02-line-height` (`1.125rem`) | 600 |
| `caption` | `font-family` | `label-01-font-size` (`0.75rem`) | `label-01-line-height` (`1rem`) | inherited |

`font-family-display` is a `<ref>` to `font-family`, so the headings read in the body face until a
theme gives them one of their own — which is the one change a theme needs for a display face.

The weights are literal, as everywhere else in the sheet: a weight is part of what a role *is*, not
a value a theme retunes.

## The page background

`body` takes `background-color: var(--background)` and `background-image:
var(--background-image)`. The image token is `none` by default, so the page is a flat colour; a
theme that wants a gradient or a texture behind the whole application sets it (`linear-gradient(…)`,
`url(…)`) and leaves the colour as what shows through.

## Writing a flat theme

A theme that wants square corners and no elevation overrides the three radius tokens and the three
shadow tokens and inherits everything else, the overlay included:

```xml
<theme name="flat"
	extends="default"
>
	<label>
		<en>Flat</en>
		<de>Flach</de>
	</label>
	<length name="border-radius-02" value="0"/>
	<length name="border-radius-03" value="0"/>
	<length name="corner-radius" value="0"/>
	<text name="shadow-dialog" value="none"/>
	<text name="shadow-menu" value="none"/>
	<text name="shadow-raised" value="none"/>
</theme>
```

Circles and pills stay round, which is what makes such a theme readable: an avatar is still a
circle, a badge is still a pill.

Keep the tokens of a theme sorted by name — the shipped themes are, and a sorted list is the only
way to see at a glance which token a theme overrides.

## The audit test

`test.com.top_logic.layout.react.theme.TestReactStylesheetTokens` runs
`test.com.top_logic.layout.react.theme.ThemeTokenAudit` over the shipped stylesheets with the
resolved tokens of the `default` theme and fails on

- a `var(--x)` whose `x` is neither a theme token nor a custom property the sheet declares itself,
  and
- a `border-radius` written as a literal length instead of one of the three radius tokens.

`ThemeTokenAudit.SHAPE_RADII` holds the literals that stay literal (`0`, `50%`, `999px`, `9999px`);
a selector that rounds for a reason of its own is passed in the audit's allow-list.

Besides the theme tokens, the audit accepts the tokens the design system's token sheet declares
(`/style/tl-design-system/tokens.css`, the `--tl-*` namespace), which a module's test names in
`tokenStylesheets()`. A sheet audited here reads those tokens; it never declares them.

The audit is written against a theme configuration and a stylesheet handed to it, both loaded from
web application resources (`ThemeTokenAudit.themeTokens`, `ThemeTokenAudit.stylesheet`), so an
application module runs it over its own sheets and its own theme through the test-jar of
`tl-layout-react`.

A module audits its own sheets by subclassing
`test.com.top_logic.layout.react.theme.AbstractStylesheetTokenTest`, which carries the audit and
the theme it runs against. The subclass supplies the resource paths its sheets are served under
(`stylesheets()`), a selector that rounds for a reason of its own (`allowedLiteralSelectors()`,
empty by default), the sheets declaring the design system's tokens (`tokenStylesheets()`, empty by
default) and a one-line suite:

```java
public class TestMyStylesheetTokens extends AbstractStylesheetTokenTest {

	private static final List<String> STYLESHEETS = List.of("/style/tlMyControl.css");

	@Override
	protected List<String> stylesheets() {
		return STYLESHEETS;
	}

	public static Test suite() {
		return AbstractStylesheetTokenTest.suite(TestMyStylesheetTokens.class);
	}
}
```

The module's POM needs the test-jar of `tl-layout-react` (and the one of `tl-basic`, which the
test setup comes from):

```xml
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-layout-react</artifactId>
	<type>test-jar</type>
	<scope>test</scope>
</dependency>
```
