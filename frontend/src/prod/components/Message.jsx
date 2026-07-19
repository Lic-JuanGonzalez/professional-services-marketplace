export function Message({ tone = "muted", children }) {
  return <p className={`inline-message inline-message-${tone}`}>{children}</p>;
}
