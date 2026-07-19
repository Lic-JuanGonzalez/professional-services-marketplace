import { Link } from "react-router-dom";
import { StarRating } from "./StarRating";

export function ProfessionalCard({ professional }) {
  return (
    <Link to={`/professionals/${professional.id}`} className="card professional-card">
      <div className="professional-card-header">
        <h3>{professional.headline}</h3>
        {professional.verified && <span className="badge">Verified</span>}
      </div>
      <p className="professional-card-meta">
        {professional.category}
        {professional.location ? ` · ${professional.location}` : ""}
      </p>
      <StarRating rating={professional.avgRating} reviewCount={professional.reviewCount} />
      {professional.hourlyRate != null && (
        <p className="professional-card-rate">${professional.hourlyRate}/hr</p>
      )}
    </Link>
  );
}
