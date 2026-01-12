package pl.weekendflyer.weekendFlightAgent.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "price_observation")
public class PriceObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "origin", nullable = false, length = 3)
    private String origin;

    @Column(name = "destination", nullable = false, length = 3)
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

    public PriceObservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(Instant observedAt) {
        this.observedAt = observedAt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartDate() {
        return departDate;
    }

    public void setDepartDate(LocalDate departDate) {
        this.departDate = departDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Short getFullDays() {
        return fullDays;
    }

    public void setFullDays(Short fullDays) {
        this.fullDays = fullDays;
    }

    public LocalDate getDepartureMonth() {
        return departureMonth;
    }

    public void setDepartureMonth(LocalDate departureMonth) {
        this.departureMonth = departureMonth;
    }

    public String getWindowKey() {
        return windowKey;
    }

    public void setWindowKey(String windowKey) {
        this.windowKey = windowKey;
    }

    public String getOfferKey() {
        return offerKey;
    }

    public void setOfferKey(String offerKey) {
        this.offerKey = offerKey;
    }

    public Integer getPricePln() {
        return pricePln;
    }

    public void setPricePln(Integer pricePln) {
        this.pricePln = pricePln;
    }
}

