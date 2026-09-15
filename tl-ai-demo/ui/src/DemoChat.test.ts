import { describe, expect, it } from 'vitest'
import { invoiceOf, parseInvoices } from './invoice-model'
// `?raw` rather than `node:fs`: no extra type package, and the path is resolved by the bundler that
// also builds the composition, so a renamed file fails here too.
import SOURCE from './DemoChat.tsx?raw'

/**
 * The composition's seams, read off its own source.
 *
 * `composerAddons.referenceCandidates` is what makes "@" offer anything: without it the picker
 * never opens and the placeholder stops inviting it, in silence. It was deleted once by a commit
 * that was tidying the rail sections beside it, and nothing failed. This is what fails now.
 */
describe('the demo composition declares its seams', () => {
  it('offers its invoices to the "@" picker', () => {
    expect(SOURCE).toContain('composerAddons')
    expect(SOURCE).toContain('referenceCandidates')
  })

  it('turns a passage marked in the pane into a reference at the caret', () => {
    expect(SOURCE).toContain('ctx.insertReference(')
    expect(SOURCE).toContain('onQuote')
  })

  it('lists its invoices beside the framework\'s sources, and only there', () => {
    expect(SOURCE).toContain('sourceTabs')
    expect(SOURCE).not.toContain('railSections')
  })

  it('opens an invoice as a document', () => {
    expect(SOURCE).toContain("mode: 'document'")
  })
})

describe('invoices', () => {
  const rows = '[{"id":"a","name":"Reise Berlin","meta":"12.03.2026","state":"184,90 €"}]'

  it('reads the list the control put into the state', () => {
    expect(parseInvoices(rows).map((i) => i.name)).toEqual(['Reise Berlin'])
    expect(parseInvoices(undefined)).toEqual([])
    expect(parseInvoices('{')).toEqual([])
  })

  it('resolves a citation by id, then by name', () => {
    const list = parseInvoices(rows)
    expect(invoiceOf(list, 'a', '')?.name).toBe('Reise Berlin')
    // The same object written three ways: the control's ID(n), a model tool's GenericObject:n, a
    // script's bare n.
    const one = parseInvoices('[{"id":"ID(5371)","name":"Software-Lizenz","meta":"","state":""}]')
    expect(invoiceOf(one, 'GenericObject:5371', '')?.name).toBe('Software-Lizenz')
    expect(invoiceOf(one, '5371', '')?.name).toBe('Software-Lizenz')
    expect(invoiceOf(one, 'ID(5371)', '')?.name).toBe('Software-Lizenz')
    expect(invoiceOf(list, 'Reise Berlin', '')?.name).toBe('Reise Berlin')
    expect(invoiceOf(list, 'nope', 'Reise Berlin')?.name).toBe('Reise Berlin')
    expect(invoiceOf(list, '', '')).toBeUndefined()
  })
})
