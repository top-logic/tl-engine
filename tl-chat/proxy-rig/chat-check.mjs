// Sends one chat message the way a user does, through the proxy, and waits for the answer.
//
// Usage: node chat-check.mjs <label> <out-dir> [--direct] [--classic]
// Prints one JSON line: { label, login, answer, seconds, transport, eventStreamMessages, classic }.
//
// --classic: the classic UI with its embedded "Albert" tab (tl-chat-classic), the shape of most
// customer applications. Engine 8 signs in only through the React login, so the user signs in
// first, then RIG_HOSTILE_CMD (if set) turns the proxy hostile, and the classic UI is loaded fresh
// through it. "classic" in the result says whether a classic navigation (AJAX) still worked.
// Writes <label>.png (the page at the end) and <label>.log (requests, console) to <out-dir>.

import { execSync } from 'node:child_process'
import { writeFileSync } from 'node:fs'
import { chromium } from 'playwright-core'

const [label, outDir] = process.argv.slice(2)
const direct = process.argv.includes('--direct')
const classicMode = process.argv.includes('--classic')
const proxyPort = process.env.RIG_PROXY_PORT ?? '8232'
const appPort = process.env.RIG_APP_PORT ?? '8231'
const answerTimeout = Number(process.env.RIG_ANSWER_TIMEOUT ?? '60') * 1000
const browserPath = process.env.RIG_CHROMIUM ?? '/usr/bin/chromium'

// "tlapp" resolves only inside the proxy container. Without the proxy (the control run) the
// browser maps it to the loopback interface itself.
const args = direct
  ? [`--host-resolver-rules=MAP tlapp 127.0.0.1`]
  : [`--proxy-server=http://127.0.0.1:${proxyPort}`, '--proxy-bypass-list=<-loopback>']
const url = `http://tlapp:${appPort}/tl-ai-demo/`
// Plain HTTP under a host name other than localhost: not a secure context, as on an intranet
// installation without TLS. The chat has to work there too (no crypto.randomUUID, #940).

const log = []
const note = (line) => log.push(`${new Date().toISOString()} ${line}`)
const result = { label, login: false, answer: false, seconds: null, seconds2: null, transport: 'stream', eventStreamMessages: 0 }
if (classicMode) result.classic = false
else result.nav = false

const browser = await chromium.launch({ executablePath: browserPath, args })
const page = await browser.newPage({ viewport: { width: 1400, height: 900 }, locale: 'de-DE' })
// Counts what the window's event stream delivers, without changing it.
await page.addInitScript(() => {
  const Original = window.EventSource
  window.__rigStreamMessages = 0
  const seen = new WeakSet()
  window.EventSource = class extends Original {
    addEventListener(type, listener, options) {
      super.addEventListener(type, (event) => {
        if (type !== 'open' && type !== 'error' && !seen.has(event)) {
          seen.add(event)
          window.__rigStreamMessages++
        }
        return typeof listener === 'function' ? listener.call(this, event) : listener.handleEvent(event)
      }, options)
    }
    set onmessage(listener) {
      this.addEventListener('message', listener)
    }
  }
})
page.on('request', (r) => {
  if (/\/(react-api|chat)\//.test(r.url())) note(`REQ ${r.method()} ${r.url().replace(/^.*tl-ai-demo/, '')}`)
})
page.on('response', (r) => {
  if (/\/(react-api|chat)\//.test(r.url())) note(`RES ${r.status()} ${r.url().replace(/^.*tl-ai-demo/, '')}`)
})
page.on('console', (m) => {
  note(`CONSOLE ${m.type()} ${m.text()}`)
  if (m.text().includes('[TLReact] polling')) result.transport = 'polling'
})
page.on('pageerror', (e) => note(`PAGEERROR ${e.message}`))
page.on('framenavigated', (f) => { if (f === page.mainFrame()) note(`NAVIGATED ${f.url()}`) })

try {
  await page.goto(url, { waitUntil: 'load', timeout: 60000 })
  await page.getByRole('button', { name: 'Anmelden' }).first().click()
  const user = page.locator('input[type=text]').last()
  await user.waitFor({ timeout: 15000 })
  await user.click()
  await user.pressSequentially('root')
  const password = page.locator('input[type=password]')
  await password.click()
  await password.pressSequentially('root1234')
  await page.getByRole('button', { name: 'Anmelden' }).last().click()
  let composer = page.locator('.token-input[contenteditable]').first()
  await composer.waitFor({ timeout: 20000 })
  result.login = true
  note('logged in, composer shown')
  if (classicMode) {
    if (process.env.RIG_HOSTILE_CMD) {
      execSync(process.env.RIG_HOSTILE_CMD, { stdio: 'ignore' })
      note('proxy turned hostile')
    }
    await page.goto(`${url}servlet/LayoutServlet`).catch(() => undefined)
    composer = page.locator('.token-input[contenteditable]').first()
    await composer.waitFor({ timeout: 30000 })
    note('classic UI with the Albert tab shown')
  }
  // Gives the chat time to attach to the window's stream, as a user reading the page would. The
  // idle profile waits longer, so that its timeout has a chance to strike first.
  await page.waitForTimeout(Number(process.env.RIG_WAIT_BEFORE_SEND ?? '3') * 1000)

  const token = Math.random().toString(36).slice(2, 8)
  await composer.click()
  await composer.pressSequentially(`ping ${token}`)
  const sent = Date.now()
  await page.keyboard.press('Enter')
  note(`sent "ping ${token}"`)
  try {
    await page.getByText(`pong ${token}`).first().waitFor({ timeout: answerTimeout })
    result.answer = true
    result.seconds = Math.round((Date.now() - sent) / 100) / 10
    note(`answer after ${result.seconds} s`)
    // A second message shows the latency once the transport has settled.
    const token2 = Math.random().toString(36).slice(2, 8)
    await page.getByText('vom Stub-Modell.').first().waitFor({ timeout: answerTimeout })
    await page.waitForTimeout(500)
    await composer.click()
    await composer.pressSequentially(`ping ${token2}`)
    const sent2 = Date.now()
    await page.keyboard.press('Enter')
    await page.getByText(`pong ${token2}`).first().waitFor({ timeout: answerTimeout })
    result.seconds2 = Math.round((Date.now() - sent2) / 100) / 10
    note(`second answer after ${result.seconds2} s`)
  } catch {
    note(`no answer within ${answerTimeout / 1000} s`)
  }
  await page.screenshot({ path: `${outDir}/${label}-chat.png` })
  if (!classicMode) {
    // A React navigation is a command whose result arrives on the window's event stream.
    await page.getByRole('button', { name: 'Veranstaltungen' }).first().click()
    try {
      await page.waitForURL(/\/view\/events/, { timeout: 15000 })
      result.nav = true
      note('React navigation worked')
    } catch {
      note('React navigation did not finish')
    }
  }
  if (classicMode) {
    // A classic navigation runs over the classic AJAX channel, not over the window's event stream.
    await page.getByText('Prozess Cockpit', { exact: true }).first().click()
    try {
      await page.locator('text=Prozess Cockpit').nth(1).waitFor({ timeout: 15000 })
      result.classic = true
      note('classic navigation worked')
    } catch {
      note('classic navigation did not finish')
    }
  }
} catch (error) {
  note(`FAILED ${error.message.split('\n')[0]}`)
}

try {
  result.eventStreamMessages = await page.evaluate(() => window.__rigStreamMessages ?? 0)
} catch {
  // The page is gone (a reload loop, say); the count is then unknown.
}
await page.screenshot({ path: `${outDir}/${label}.png` }).catch(() => {})
writeFileSync(`${outDir}/${label}.log`, log.join('\n') + '\n')
await browser.close()
console.log(JSON.stringify(result))
