package pl.weekendflyer.weekendFlightAgent.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "price_observation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "origin", nullable = false, columnDefinition = "bpchar(3)")
    private String origin;

    @Column(name = "destination", nullable = false, columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "depart_date", nullable = false)
    private LocalDate departDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "full_days", nullable = false)
    private Short fullDays;

    @Column(name = "departure_month", nullable = false)
    private LocalDate departureMonth;

    @Column(name = "window_key", nullable = false)
    private String windowKey;

    @Column(name = "offer_key", nullable = false)
    private String offerKey;

    @Column(name = "price_pln", nullable = false)
    private Integer pricePln;

    @PrePersist
    void prePersist() {
        if (observedAt == null) {
            observedAt = Instant.now();
        }
    }
}
