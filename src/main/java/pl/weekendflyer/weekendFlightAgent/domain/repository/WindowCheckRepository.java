package pl.weekendflyer.weekendFlightAgent.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.weekendflyer.weekendFlightAgent.domain.model.WindowCheck;

import java.util.List;
import java.util.Optional;

@Repository
public interface WindowCheckRepository extends JpaRepository<WindowCheck, Long> {

    Optional<WindowCheck> findByProviderAndWindowKey(String provider, String windowKey);

    List<WindowCheck> findByProviderAndWindowKeyIn(String provider, List<String> windowKeys);
}

