package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.LocalTime;

/**
 * Twarde ograniczenia dla wyszukiwania lotów weekendowych.
 * Definiuje maksymalne wartości i wymagania, które muszą być spełnione przez oferty.
 *
 * @param maxStops maksymalna liczba przesiadek w jedną stronę (np. 0 dla lotów bezpośrednich, 1 dla jednej przesiadki)
 * @param maxTotalDurationMinutesOneWay maksymalny czas trwania lotu w jedną stronę w minutach
 * @param hardCapPricePln sztywny limit cenowy w PLN (nullable - brak limitu jeśli null)
 * @param latestArrivalOnFridayLocal najpóźniejsza godzina przylotu w piątek (czas lokalny w destination)
 * @param earliestDepartureOnSundayLocal najwcześniejsza godzina wylotu w niedzielę (czas lokalny w destination)
 * @param requireNoFlightOnSaturday czy wymagać, aby w sobotę nie było lotów (cały dzień wolny)
 */
public record TripConstraints(
        int maxStops,
        int maxTotalDurationMinutesOneWay,
        Integer hardCapPricePln,
        LocalTime latestArrivalOnFridayLocal,
        LocalTime earliestDepartureOnSundayLocal,
        boolean requireNoFlightOnSaturday
) {
}

