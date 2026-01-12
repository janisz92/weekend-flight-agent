package pl.weekendflyer.weekendFlightAgent.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.weekendflyer.weekendFlightAgent.domain.model.PriceObservation;

@Repository
public interface PriceObservationRepository extends JpaRepository<PriceObservation, Long> {
}

