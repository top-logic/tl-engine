/**
 * An amount of money as the chat shows it: in the user's language, with four places for an amount
 * below ten cents - what a short run costs - and the currency's own places otherwise.
 */
export function formatMoney(amount: number, currency: string, language: string): string {
  try {
    return new Intl.NumberFormat(language, {
      style: 'currency',
      currency,
      maximumFractionDigits: amount < 0.1 ? 4 : 2,
    }).format(amount)
  } catch {
    return `${amount.toFixed(4)} ${currency}`
  }
}
