import { describe, expect, it } from 'vitest'
import { selectAttachments, sizeLabel, typeAllowed } from './attachments'
import { bundle } from './i18n'

/**
 * The browser refuses a file the server would refuse, before it is uploaded: by the attachment
 * policy of the conversation, with the type checked as the server's `AcceptedTypesChecker` checks it.
 */

function file(name: string, type: string, size = 4): File {
  return new File([new Uint8Array(size)], name, { type })
}

describe('typeAllowed', () => {
  it('matches a MIME type exactly and by its main type', () => {
    expect(typeAllowed(file('a.csv', 'text/csv'), ['text/csv'])).toBe(true)
    expect(typeAllowed(file('a.txt', 'text/plain'), ['text/*'])).toBe(true)
    expect(typeAllowed(file('a.png', 'image/png'), ['text/*'])).toBe(false)
    expect(typeAllowed(file('a.jpg', 'image/jpg'), ['image/jpeg'])).toBe(true)
  })

  it('matches a file pattern by the ending of the name, ignoring case', () => {
    expect(typeAllowed(file('Archiv.ZIP', ''), ['*.zip'])).toBe(true)
    expect(typeAllowed(file('archiv.tar', ''), ['*.zip'])).toBe(false)
    expect(typeAllowed(file('README', ''), ['readme'])).toBe(true)
  })

  it('allows every type for no types or a pattern of all', () => {
    expect(typeAllowed(file('a.exe', ''), [])).toBe(true)
    expect(typeAllowed(file('a.exe', ''), ['*/*'])).toBe(true)
    expect(typeAllowed(file('a.exe', ''), ['image/png', '*.*'])).toBe(true)
  })
})

describe('sizeLabel', () => {
  it('names the size in the largest unit it fills', () => {
    expect(sizeLabel(100)).toBe('100 B')
    expect(sizeLabel(10 * 1024 * 1024)).toBe('10 MB')
    expect(sizeLabel(1536)).toBe('1.5 KB')
  })
})

describe('selectAttachments', () => {
  const labels = bundle('en')
  const policy = { accept: 'text/csv,.zip', types: ['text/csv', '*.zip'], maxFiles: 2, maxFileSize: 100, maxMessageSize: 150 }

  it('takes the files of the policy and refuses the others, each with its reason', () => {
    const { usable, problems } = selectAttachments(
      [file('a.csv', 'text/csv', 60), file('b.png', 'image/png'), file('c.zip', '', 101), file('d.zip', '', 60)],
      { count: 0, size: 0 },
      policy,
      labels,
    )
    expect(usable.map((f) => f.name)).toEqual(['a.csv', 'd.zip'])
    expect(problems).toEqual([
      `b.png: ${labels.typeNotAllowed('text/csv, *.zip')}`,
      `c.zip: ${labels.largerThan('100 B')}`,
    ])
  })

  it('counts the files attached already against the number and the size of the message', () => {
    expect(selectAttachments([file('a.csv', 'text/csv')], { count: 2, size: 8 }, policy, labels).problems).toEqual([
      `a.csv: ${labels.atMostFiles(2)}`,
    ])
    expect(selectAttachments([file('a.csv', 'text/csv', 60)], { count: 1, size: 100 }, policy, labels).problems).toEqual([
      `a.csv: ${labels.messageLargerThan('150 B')}`,
    ])
  })

  it('refuses every file where the conversation takes none', () => {
    const none = { ...policy, maxFiles: 0 }
    expect(selectAttachments([file('a.csv', 'text/csv')], { count: 0, size: 0 }, none, labels).problems).toEqual([
      `a.csv: ${labels.noAttachments}`,
    ])
  })
})
