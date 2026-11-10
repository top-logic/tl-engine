---
description: Read before protecting a form that anonymous visitors submit (self-registration, contact, password reset request) against bots with a captcha - the module tl-security-captcha, the self-hosted proof-of-work captcha ALTCHA (MIT licensed library org.altcha:altcha, no external service, no API key), the CaptchaService options hmac-key (secret source, random key per node without it) / cost / max-number / expires, its operations createChallenge(), verify(payload) and check(payload) (TopLogicException ERROR_CAPTCHA_INVALID), the per-node replay protection, and the React view element <captcha payload="..." start="..."/> of the module tl-security-captcha-view (CaptchaElement, CaptchaControl, the ALTCHA widget of the npm package altcha) writing the solution into a channel.
order: 30
---

# Captcha

The module `tl-security-captcha` protects a form an anonymous visitor submits against automated
submissions. It uses [ALTCHA](https://altcha.org), a proof-of-work captcha: the browser solves a
computational puzzle the server has signed, without any interaction of the visitor (no images, no
puzzles to click). The challenge is created and checked by the application itself:

- No external service is called and no API key is needed. Nothing about the visitor leaves the
  application.
- The server side is the library `org.altcha:altcha` (`altcha-lib-java`, MIT license), its
  version is managed in `tl-bom`. The browser side is the ALTCHA widget (npm package `altcha`, MIT
  license). The paid products of ALTCHA (ALTCHA Cloud, Sentinel) are not used.
- The challenges follow version 2 of the ALTCHA protocol with the key derivation function
  `PBKDF2/SHA-256`, the default of the widget.

A proof of work does not tell humans from machines; it makes each submission cost computing time,
so mass submissions become expensive.

## Enabling the captcha

Add the dependency `com.top-logic:tl-security-captcha` to the application. The module starts the
service `com.top_logic.security.captcha.CaptchaService`.

## Options of `CaptchaService`

| Option | Default | Meaning |
|---|---|---|
| `hmac-key` | none | The secret key signing the challenges, a [secret source](doc:credentials/secret-sources). |
| `cost` | `1000` | The iterations of the key derivation function in one attempt. |
| `max-number` | `6000` | The maximum number of attempts a challenge needs. Each challenge needs a random number of attempts from the upper half of this value. |
| `expires` | `10min` | How long a created challenge can be solved and submitted. |

`cost` times the number of attempts is the work of the browser: with the defaults 3 to 6 million
iterations of PBKDF2/SHA-256. A current desktop browser computes them in about one second (measured
with the widget in Chrome: 0.7 s for 3.75 million iterations, independent of 4 or 8 workers), a
slower device in a few seconds. The values of the ALTCHA examples (`cost` 5000, `max-number` 10000)
took the same browser 6 to 7 seconds, too long for a visitor waiting to submit a form. Each solved
challenge still costs a bot about a second of computing time, and the callers of the captcha
throttle requests in addition (e.g. the self-registration per address and per client).

### The key

The key is a secret of the installation, best given by name:

```xml
<config service-class="com.top_logic.security.captcha.CaptchaService">
	<instance class="com.top_logic.security.captcha.CaptchaService">
		<hmac-key name="tl_captcha_hmac_key"/>
	</instance>
</config>
```

The secret store of the installation provides it, by default the variable `tl_captcha_hmac_key`.
The key is read whenever a challenge is created or checked, so a changed key takes effect without a
restart; challenges created with the former key are refused from then on.

Without a configured key, the service creates a random key when it starts and logs a warning the
first time it creates or checks a challenge, so an application that never shows a captcha logs
nothing. A challenge is then only accepted by the node that created it, and only until this node restarts.
This is fine for a single development node; a production installation, and every installation with
several nodes, configures the key.

## Operations

- `CaptchaService.getInstance().createChallenge()` creates a challenge and returns it as JSON, the
  challenge data of the widget.
- `verify(payload)` checks what the widget submits with the form, the Base64 encoded JSON of the
  challenge and its solution, and returns whether it is accepted.
- `check(payload)` does the same and throws a `TopLogicException` with the message
  `I18NConstants.ERROR_CAPTCHA_INVALID` ("The security check was not completed or has expired")
  when it is not.

A solution is accepted if it solves a challenge signed with the current key, the challenge has not
expired, and no solution of the challenge was accepted before. An accepted solution is used up:
the service remembers the challenge until it expires, and refuses it when it is submitted again.

This memory belongs to the node. In a cluster sharing the key, a solution submitted to two
different nodes is accepted once by each of them.

## The captcha in the React view UI

The module `tl-security-captcha-view` adds the element `<captcha>` (`CaptchaElement`) to the
`.view.xml` layer. It shows the ALTCHA widget (npm package `altcha` 3.5.0, MIT license, bundled
into the module's script `tl-security-captcha-view.js`) and writes the solution into a channel, so
that any form can carry a captcha and the action submitting the form reads it:

```xml
<view>
	<channels>
		<channel name="model"/>
		<channel name="captcha"/>
	</channels>
	...
	<form input="model">
		<field attribute="email"/>
		<captcha payload="captcha"/>
		<commands>
			<generic-command>
				...
				<my-action/>  <!-- reads the channel "captcha" -->
			</generic-command>
		</commands>
	</form>
</view>
```

| Attribute | Default | Meaning |
|---|---|---|
| `payload` | (mandatory) | The channel receiving the solution, the text `CaptchaService.check(payload)` takes; empty while the captcha is not solved. |
| `start` | `load` | When the browser starts to solve the challenge: `load` as soon as the captcha is displayed, `focus` when the visitor focuses a field of the surrounding HTML form, `click` when the visitor checks the box. |

How the element works (`CaptchaControl`, client component `TLCaptcha`):

- The server creates the challenge with `createChallenge()` and passes it to the widget in the
  state of the control; the widget fetches nothing and needs no servlet.
- When the widget has solved the challenge, the solution reaches the server with the command
  `solved` and is written into the channel. A solution the widget gives up again (reset, error)
  clears the channel.
- A new challenge is issued when the control is created, when the challenge expires (the widget
  reports it with the command `expired`, the channel is cleared) and when someone else writes the
  channel. Since a solution is accepted once only, the action checking it clears the channel after
  the check - whether the check or the rest of the action failed or not -, which gives the
  widget a new challenge for the next attempt.
- The texts of the widget are TL resources (`I18NConstants.WIDGET_*` of
  `com.top_logic.security.captcha.view`, English and German), registered with the widget for the
  language of the user.
- No external calls: the challenge is in the page, the widget runs its proof of work in Web Workers
  created from the bundle itself (`blob:` URLs), the attribution link to the ALTCHA website
  (`hideFooter`) and the collection of pointer events (`humanInteractionSignature`, only sent to a
  challenge URL) are switched off. The paid features (code challenges, ALTCHA Sentinel) are not
  used.
- The widget needs a secure context (`https`, or `http` on `localhost`) for the Web Crypto API.

`tl-security-captcha` itself does not depend on the view layer, so that an application with the
classic UI only uses the service without the React UI. The self-registration of
`tl-layout-view-selfservice` (article "Self-service" of this chapter) shows its captcha with this
element.
