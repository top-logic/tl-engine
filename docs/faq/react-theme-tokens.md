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

## The two radius tiers

Rounding comes in exactly two tiers, and every rounded box takes one of them:

| Tier | Token | Default | Applies to |
| --- | --- | --- | --- |
| Control | `corner-radius` | `0.25rem` | buttons, input fields, drop-down and colour/icon fields, toggles, table and calendar cells, small chips, tree toggles, palette cells, sliders and handles, reset buttons, scrollbar thumbs, drop markers |
| Surface | `border-radius-02` | `0.5rem` | panels, cards, windows, drawers, menus, drop-down popups, flyouts, the snackbar, colour and icon popups, tooltips, dashboard tiles, form groups |

The rule of thumb: a box the pointer acts *on* is a control, a box that *holds* other boxes is a
surface.

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

A shadow that draws a ring rather than an elevation — a focus ring, a button's inset frame, the
outline of a colour handle — is not part of this scale and keeps its own value.

## Writing a flat theme

A theme that wants square corners and no elevation overrides the two radius tokens and the three
shadow tokens and inherits everything else, the overlay included:

```xml
<theme name="flat"
	extends="default"
>
	<label>
		<en>Flat</en>
		<de>Flach</de>
	</label>
	<length name="corner-radius" value="0"/>
	<length name="border-radius-02" value="0"/>
	<text name="shadow-raised" value="none"/>
	<text name="shadow-menu" value="none"/>
	<text name="shadow-dialog" value="none"/>
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
- a `border-radius` written as a literal length instead of one of the two radius tokens.

`ThemeTokenAudit.SHAPE_RADII` holds the literals that stay literal (`0`, `50%`, `999px`, `9999px`);
a selector that rounds for a reason of its own is passed in the audit's allow-list.

The audit is written against a theme configuration and a stylesheet handed to it, both loaded from
web application resources (`ThemeTokenAudit.themeTokens`, `ThemeTokenAudit.stylesheet`), so an
application module runs it over its own sheets and its own theme through the test-jar of
`tl-layout-react`.
