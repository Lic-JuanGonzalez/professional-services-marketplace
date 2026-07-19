export function StarRating({ rating, reviewCount }) {
  const safeRating = typeof rating === "number" ? rating : 0;
  const full = Math.round(safeRating);

  return (
    <span className="star-rating" aria-label={`${safeRating.toFixed(1)} out of 5 stars`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= full ? "star star-filled" : "star"} aria-hidden="true">
          ★
        </span>
      ))}
      <span className="star-rating-value">{safeRating.toFixed(1)}</span>
      {typeof reviewCount === "number" && (
        <span className="star-rating-count">({reviewCount})</span>
      )}
    </span>
  );
}
