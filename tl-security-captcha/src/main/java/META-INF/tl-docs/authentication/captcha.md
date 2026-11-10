---
description: Read before protecting a form that anonymous visitors submit (self-registration, contact, password reset request) against bots with a captcha - the module tl-security-captcha, the self-hosted proof-of-work captcha ALTCHA (MIT licensed library org.altcha:altcha, no external service, no API key), the CaptchaService options hmac-key (secret source, random key per node without it) / cost / max-number / expires, its operations createChallenge(), verify(payload) and check(payload) (TopLogicException ERROR_CAPTCHA_INVALID), and the per-node replay protection.
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
| `cost` | `5000` | The iterations of the key derivation function in one attempt. |
| `max-number` | `10000` | The maximum number of attempts a challenge needs. Each challenge needs a random number of attempts from the upper half of this value. |
| `expires` | `10min` | How long a created challenge can be solved and submitted. |

`cost` times the number of attempts is the work of the browser; the defaults are the values of the
ALTCHA examples and take a few seconds.

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

Without a configured key, the service creates a random key when it starts and logs a warning. A
challenge is then only accepted by the node that created it, and only until this node restarts.
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
