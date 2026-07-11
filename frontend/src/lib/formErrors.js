// Backend errors are a single string. This maps that string to the form field
// it most likely refers to, so we can highlight the field instead of only
// showing a generic banner. `mapping` is an ordered list of [keyword, field]
// pairs — more specific keywords should come first.
export function fieldFromMessage(message, mapping) {
  if (!message) return null;
  const lower = message.toLowerCase();
  for (const [keyword, field] of mapping) {
    if (lower.includes(keyword)) return field;
  }
  return null;
}
