import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { renderToStaticMarkup } from 'react-dom/server'
import { resetTLBridge, setTLState } from '../test/tl-react-bridge'
import { TLChatSources } from './chat-sources'

afterEach(() => resetTLBridge())

describe('the css-class of the element', () => {
  it('is written on the root after the classes of the section itself', () => {
    setTLState({
      chatId: 'sources-css-class',
      windowId: 'w1',
      authenticated: true,
      language: 'en',
      cssClass: 'appSources',
    })
    const html = renderToStaticMarkup(<TLChatSources controlId="sources-css-class" state={{}} />)
    expect(html).toMatch(/^<div id="sources-css-class" class="tl-chat-section appSources"/)
  })
})
