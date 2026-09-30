# FAQ: Fixing TLDoclet JavaDoc build warnings

The `TLDoclet` runs in the `maven-javadoc-plugin` lifecycle and turns JavaDoc problems into build warnings that make the Jenkins build UNSTABLE. House rules for the common ones:

## Never downgrade a `{@link}` to `{@code}` to silence a warning

A reference to a resolvable symbol (type / method / field) must stay a `{@link}`. If the doclet rejects it *where it currently sits* (e.g. a member link in a configured type's in-app documentation), **relocate the link** — move it into `@implNote`, or reword to link the class instead of the member — keeping it a link. `{@code …}` is only ever for genuine non-symbols.

## "Invalid camel case word"

Flags a bare `camelCase` word in JavaDoc. Wrap it in a `{@link}` when the word is a resolvable symbol; fall back to `{@code …}` only for genuine non-symbols (literals, external JS identifiers like `pushState`, words like `JavaDoc`).

## "Invalid configuration reference"

Raised on configured classes / config properties, whose JavaDoc becomes in-app configuration documentation. The main description may reference other classes or config properties only *without a label* — no method `{@link X#method()}` links. Move the technical / method-level detail into an `@implNote` section (which is not part of the UI documentation), not `@see`.

A class counts as "configured" if it has a `Config` / `PolymorphicConfiguration` (e.g. a `ViewCommand`, a `ConfiguredManagedClass`), so the no-method-link rule applies to its class JavaDoc too, not just to config-property getters.

## "Invalid XML: The entity 'X' was referenced, but not declared"

The doclet parses JavaDoc as XML and does not declare HTML named entities. Never write `&mdash;` / `&rarr;` / `&hellip;` etc. — use the literal Unicode character (`—`, `→`, `…`) or a numeric entity (`&#8212;`). (`&lt;` / `&gt;` / `&amp;` are fine.)

## "Missing comment for record component X"

A Java `record` needs a `@param X` in its JavaDoc for **every** component, exactly like a constructor.
