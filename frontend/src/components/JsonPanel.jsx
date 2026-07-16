export function JsonPanel({ result }) {
  if (!result) {
    return <div className="json-panel json-panel-empty">No requests yet.</div>;
  }

  const { status, ok, method, path, data } = result;

  return (
    <div className={`json-panel ${ok ? "json-panel-ok" : "json-panel-error"}`}>
      <div className="json-panel-meta">
        <span className="badge">{method}</span>
        <span className="json-panel-path">{path}</span>
        <span className={`status-code ${ok ? "status-ok" : "status-error"}`}>{status}</span>
      </div>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}
