package com.example.SwiftBid.repository.spec;

import com.example.SwiftBid.model.Auction;
import com.example.SwiftBid.model.enums.AuctionStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Dynamic filter building blocks for {@code GET /api/auctions/search} (FR-AUC-02).
 * Each method returns {@code null} when its filter isn't applicable, which JPA Specification
 * composition treats as "no restriction" — letting the caller {@code .and(...)} them unconditionally.
 */
public final class AuctionSpecifications {

    private AuctionSpecifications() {
    }

    public static Specification<Auction> hasStatus(AuctionStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Auction> hasCategory(String category) {
        return (root, query, cb) -> StringUtils.hasText(category)
                ? cb.equal(root.get("product").get("category"), category)
                : null;
    }

    public static Specification<Auction> matchesSearch(String q) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(q)) {
                return null;
            }
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("product").get("name")), like),
                    cb.like(cb.lower(root.get("product").get("description")), like)
            );
        };
    }
}
