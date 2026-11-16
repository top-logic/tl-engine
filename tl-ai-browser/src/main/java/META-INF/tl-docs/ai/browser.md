---
description: Read before giving agents a real web browser - the module tl-ai-browser with the tools web-browser.* (browserNavigate, browserSnapshot, browserClick, browserType, browserBack, browserScreenshot, browserSaveCookies, browserUseCookies), the skill TL_webBrowser, BrowserToolsService (domain lists, timeouts, session pool, headless, sandbox), the Chromium that Playwright provisions, and the encrypted cookie pools of CookiePoolService (tl.ai.cookiepool:CookiePool).
order: 67
---

# The browser tools

`webFetch` and `webSearch` of `tl-ai-core` read the web without running a page's JavaScript. The optional module `tl-ai-browser` gives agents a real Chromium in addition: it reads pages that are rendered in the browser, and it clicks, types, logs in and walks multi-step flows. It is a module of its own because the browser comes with Playwright, whose driver bundle is a large part of an application's size; an application without the module has no browser and no Playwright on its class path.

## Adding it to an application

The application depends on the module:

```xml
<dependency>
	<groupId>com.top-logic</groupId>
	<artifactId>tl-ai-browser</artifactId>
</dependency>
```

That is all the configuration it needs. The module's `tl-ai-browser.conf.config.xml` adds, to the services `tl-ai-core` configures:

| What | Where |
|---|---|
| the services `BrowserToolsService` (the sessions) and `CookiePoolService` (saved logins) | `ModuleSystem` |
| the tool factory `web-browser` with the tools `browserNavigate`, `browserSnapshot`, `browserClick`, `browserType`, `browserBack`, `browserScreenshot`, `browserSaveCookies`, `browserUseCookies` | `ToolRegistryService` |
| the skill `TL_webBrowser`, which carries the browser tools and `view-document.viewDocument`, and gives it to the shipped chat agent `TL_SingleAgent` beside `TL_webAccess` | `TopLogicAgentDefinitions` |
| the model `tl.ai.cookiepool` and its table `CookiePool` | `ModelService`, `ApplicationTypes` |

An application's own agent gets the browser with `<skill name="TL_webBrowser"/>` in its `<skills>`, the same way it names `TL_webAccess` for `webFetch` and `webSearch`.

## The browser

Playwright provisions the Chromium it needs on first use, into its own browser cache (`~/.cache/ms-playwright` on Linux). It must be the **full** Chromium build, not the stripped `chrome-headless-shell`, because the guard against WebRTC traffic past the filtering proxy depends on it; `playwright install chromium` supplies both. A host that may not download at run time provides the cache in advance.

`BrowserToolsService` holds the settings; the comment above its configuration in `tl-ai-browser.conf.config.xml` describes each of them. In short:

| Setting | Default | What it decides |
|---|---|---|
| `allowed-domains`, `blocked-domains`, `allow-any-domains`, `apply-allowlist-to-all-requests` | empty, empty, `false`, `false` | which domains the browser may reach in this installation. A fresh installation browses **nothing** until an administrator names the domains. |
| `max-concurrent-sessions`, `session-idle-timeout-seconds`, `acquire-timeout-seconds` | `4`, `300`, `10` | how many browsers run at once (each 200-400 MB), and when an idle one is closed |
| `navigation-timeout-seconds`, `action-timeout-seconds`, `navigation-settle-millis` | `30`, `8`, `1500` | how long a page load, an action on an element and the settle after a load may take |
| `headless` | `true` | `false` opens a visible window on the application host, for local development |
| `browser-sandbox` | `AUTO` | whether Chromium runs in its own OS-level sandbox; `AUTO` uses it everywhere but inside a container |
| `evade-bot-detection` | `false` | whether the browser disguises its automation fingerprint, so that bot walls stop it less often |
| `cookie-pools-enabled` | `true` | whether `browserSaveCookies` and `browserUseCookies` work at all |

The installation's domain lists compose with the per-role browsing policy of `WebToolsService` and with the SSRF check; every connection of a browser session goes through a filtering proxy that applies all three. See [browsing governance](doc:ai/security/browsing-governance).

## Cookie pools

`browserSaveCookies(pool)` stores the browser's login state under a name, `browserUseCookies(pool)` starts a later session from it. A pool is a `tl.ai.cookiepool:CookiePool`, private to the account that saved it unless it was saved as shared. The state is encrypted at rest through the platform `EncryptionService`; without an installed encryption key, saving is refused rather than stored in plain text.

Reading the stored state through `viewDocument` follows the read policy of `tl-ai-browser.binarySecurity.config.xml`: the owner and administrators, see [administration](doc:ai/security/administration). Selecting a shared pool to drive a browser with is not reading it, and is open to every account.

## Removing the module again

The cookie pools are stored in the table `CookiePool` of the model module `tl.ai.cookiepool`. An application that drops the dependency on `tl-ai-browser` no longer declares either of them, but its database still holds them. It boots: at startup `DynamicModelService` removes the model module that is no longer declared in its incremental model upgrade, and `MigrationService` re-creates a schema definition for the orphaned table (`WEB-INF/autoconf/inapp-schema-N.meta.xml`), so the table and its rows stay in the database.

To remove them cleanly, the application writes a migration of its own, as `tl-ai-core` did for the module `tl.ai.chat` (`Issue_230_remove_module_tl_ai_chat.migration.xml`): `DeleteTableProcessor` drops the table, `<delete-class name="tl.ai.cookiepool:CookiePool"/>` removes the type and `<delete-module name="tl.ai.cookiepool" fail-on-existing-types="true"/>` the module. The saved logins are gone with it.

## Tests

The tests of the module that start a real Chromium are gated by `-Dtl.ai.browser.e2e=true`, see [testing](doc:ai/testing#opt-in-gates).
