# Weekend Flight Agent

Agent wyszukujący i monitorujący tanie loty weekendowe z Polski do wybranych destynacji w Europie.

## Spis treści

- [Opis projektu](#opis-projektu)
- [Funkcje](#funkcje)
- [Technologie](#technologie)
- [Wymagania](#wymagania)
- [Instalacja](#instalacja)
- [Konfiguracja](#konfiguracja)
- [Uruchomienie](#uruchomienie)
- [Baza danych](#baza-danych)
- [Architektura](#architektura)
- [Modele domenowe](#modele-domenowe)
- [Ewaluacja ofert](#ewaluacja-ofert)
- [Testy](#testy)
- [Roadmap](#roadmap)

## Opis projektu

Weekend Flight Agent to aplikacja automatyzująca proces wyszukiwania okazji na weekendowe wyjazdy. System codziennie skanuje dostępne połączenia lotnicze, filtruje je według określonych kryteriów (pełna sobota w miejscu docelowym, rozsądne ceny, maksymalna liczba przesiadek) i wykrywa potencjalne okazje cenowe.

### Główne założenia
- **Pełna sobota**: przylot w piątek wieczorem, wylot w niedzielę rano
- **Automatyzacja**: codzienny scan bez interwencji użytkownika
- **Inteligentne filtrowanie**: tylko rzeczywiście atrakcyjne oferty
- **Baseline tracking**: porównanie z historycznymi cenami
- **Persystencja**: przechowywanie obserwacji cenowych w PostgreSQL

## Funkcje

### Zaimplementowane
- Konfiguracja wielokryterialna (strefy czasowe, lotniska, ograniczenia)
- Modele domenowe (FlightOffer, TripWindow, TripConstraints, CandidateWindow, PlannerResult)
- Encje JPA (PriceObservation, WindowCheck) z Lombok
- Ewaluator ofert z deterministyczną logiką:
  - Obliczanie pełnych dni w destynacji
  - Walidacja reguł soboty weekendowej
  - Sprawdzanie twardych ograniczeń (przesiadki, czas lotu, cena)
- **Generator okien podróży (TripWindowGenerator)**:
  - Generowanie kandydackich okien na podstawie horizonDays i fullDaysAllowed
  - Filtrowanie tylko okien z sobotą w środku
  - Limity per (destination, departDate) i globalny
- **Planner okien (WindowCheckPlanner)**:
  - Inteligentny wybór okien do sprawdzenia
  - Priorytet: nowe -> bliższe daty -> mniej sprawdzane
  - Budżet dzienny per provider
  - Tracking lastCheckedAt i checkCount w DB
- Scheduled job (codziennie o 07:10) z integracją generatora i planera
- Persystencja z Flyway migrations
- Repozytoria Spring Data JPA
- Kompleksowe testy jednostkowe i integracyjne

### W planach
- Integracja z API dostawców lotów (Skyscanner, Kiwi.com)
- Baseline tracking (śledzenie median cenowych)
- Filtrowanie kandydatów (porównanie z baseline)
- System alertów (Telegram/Email)

## Technologie

- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Data JPA** - persystencja
- **PostgreSQL 15** - baza danych
- **Flyway** - migracje bazy danych
- **Maven** - zarządzanie zależnościami
- **Lombok** - redukcja boilerplate code
- **JUnit 5** - testy jednostkowe
- **Docker Compose** - lokalne środowisko deweloperskie

## Wymagania

- Java 17 lub nowszy
- Maven 3.6+ (lub użyj dołączonego `mvnw`)
- Docker i Docker Compose (dla bazy danych)
- 512 MB RAM (minimalne)

## Instalacja

```bash
# Klonowanie repozytorium
git clone https://github.com/your-username/weekend-flight-agent.git
cd weekend-flight-agent

# Uruchomienie bazy danych
docker-compose up -d

# Build projektu
./mvnw clean install

# Lub na Windows
mvnw.cmd clean install
```

## Konfiguracja

Główny plik konfiguracyjny: `src/main/resources/config.yaml`

### Podstawowe ustawienia

```yaml
agent:
  timezone: "Europe/Warsaw"
  
  origins:
    - "WAW"  # Warszawa Okecie
  
  destinations:
    - "LIS"  # Lizbona
    - "BCN"  # Barcelona
    - "MAD"  # Madryt
    - "FCO"  # Rzym
```

### Parametry wyszukiwania

```yaml
search:
  horizonDays: 92              # Ile dni w przód skanować
  fullDaysAllowed: [2, 3, 4]  # Akceptowana liczba pełnych dni
```

### Reguła soboty weekendowej

```yaml
saturdayRule:
  requireNoFlightOnSaturday: true           # Żadnych lotów w sobotę
  latestArrivalOnFridayLocal: "22:00"      # Najpóźniejszy przylot w piątek
  earliestDepartureOnSundayLocal: "06:00"  # Najwcześniejszy wylot w niedzielę
```

### Ograniczenia komfortu

```yaml
constraints:
  maxStops: 1                           # Maksymalnie 1 przesiadka w jedną stronę
  maxTotalDurationMinutesOneWay: 480   # Maksymalnie 8h w jedną stronę
  hardCapPricePLN: 2000                # Twardy limit cenowy
  earliestDepartureFromOriginLocal: "08:00"
  latestArrivalToOriginLocal: "23:30"
```

### Filtrowanie kandydatów

```yaml
candidateFilter:
  minAbsoluteSavingPLN: 150          # Min. oszczędność względem mediany
  minPercentBelowMedian: 10          # Min. % poniżej mediany
  maxCandidatesPerRun: 20            # Max. alertów na jeden scan
```

### Alerty

```yaml
alerts:
  channel: "TELEGRAM"                          # lub "EMAIL"
  maxAlertsPerDay: 3                          # Limit alertów dziennie
  maxAlertsPerDestinationPerWeek: 1          # Limit per destynacja/tydzień
```

## Uruchomienie

### Tryb deweloperski

```bash
# Uruchomienie bazy danych
docker-compose up -d

# Uruchomienie aplikacji
./mvnw spring-boot:run
```

### Build i uruchomienie JAR

```bash
./mvnw clean package
java -jar target/weekend-flight-agent-0.0.1-SNAPSHOT.jar
```

### Uruchomienie z custom config

```bash
java -jar target/weekend-flight-agent-0.0.1-SNAPSHOT.jar \
  --agent.configPath=file:/path/to/custom-config.yaml
```

## Baza danych

### PostgreSQL z Docker Compose

Uruchomienie:
```bash
docker-compose up -d
```

Zatrzymanie:
```bash
docker-compose down
```

Usunięcie wraz z danymi:
```bash
docker-compose down -v
```

### Parametry połączenia

| Parametr | Wartość |
|----------|---------|
| Database | flight_agent |
| User | flight_agent |
| Password | flight_agent |
| Port | 5432 |
| JDBC URL | jdbc:postgresql://localhost:5432/flight_agent |

### Schemat bazy danych

Migracje Flyway tworzą następujące tabele:

- **window_check** - śledzenie sprawdzonych okien czasowych
- **price_observation** - obserwacje cenowe
- **baseline** - mediany cenowe (rolling 30 dni)
- **deal** - wykryte okazje

## Architektura

```
weekend-flight-agent/
├── src/main/java/pl/weekendflyer/weekendFlightAgent/
│   ├── WeekendFlightAgentApplication.java    # Klasa główna
│   ├── config/
│   │   ├── AgentProperties.java              # Mapowanie config.yaml
│   │   ├── AgentPropertiesLoader.java        # Loader dla config.yaml
│   │   ├── ClockConfig.java                  # Bean Clock (Europe/Warsaw)
│   │   └── PlannerConfig.java                # Beany TripWindowGenerator, WindowCheckPlanner
│   ├── scheduler/
│   │   └── DailyScanJob.java                 # Codzienne zadanie (7:10)
│   └── domain/
│       ├── model/                            # Modele domenowe
│       │   ├── FlightOffer.java              # Oferta lotu (round-trip)
│       │   ├── FlightSegment.java            # Segment lotu
│       │   ├── TripWindow.java               # Okno podróży (ZonedDateTime)
│       │   ├── CandidateWindow.java          # Kandydackie okno (LocalDate)
│       │   ├── PlannerResult.java            # Wynik planowania
│       │   ├── TripConstraints.java          # Ograniczenia
│       │   ├── PriceObservation.java         # Encja JPA - obserwacja ceny
│       │   └── WindowCheck.java              # Encja JPA - sprawdzone okna
│       ├── planner/                          # Generowanie i planowanie okien
│       │   ├── TripWindowGenerator.java      # Generator kandydackich okien
│       │   ├── WindowCheckPlanner.java       # Planner z priorytetami i budżetem
│       │   └── WindowKeyGenerator.java       # Generator kluczy okien
│       ├── eval/                             # Ewaluacja ofert
│       │   ├── TripEvaluator.java            # Główna logika oceny
│       │   └── TripConstraintsFactory.java   # Factory dla constraints
│       └── repository/                       # Spring Data JPA
│           ├── PriceObservationRepository.java
│           └── WindowCheckRepository.java
├── src/main/resources/
│   ├── application.yaml                      # Spring Boot config
│   ├── config.yaml                           # Agent config
│   └── db/migration/
│       └── V1__init.sql                      # Migracja Flyway
└── src/test/java/                            # Testy jednostkowe
```

## Modele domenowe

### FlightOffer (record)
Reprezentuje kompletną ofertę lotu w obie strony (round-trip).

```java
FlightOffer(
    String originIata,                    // "WAW"
    String destinationIata,               // "BCN"
    List<FlightSegment> outboundSegments, // Loty tam
    List<FlightSegment> inboundSegments,  // Loty z powrotem
    Integer pricePln,                     // Cena w PLN
    String provider,                      // Nazwa providera
    String deepLink                       // URL do oferty
)
```

Metody pomocnicze:
- `totalStops()` - łączna liczba przesiadek
- `outboundArrivalTime()` - czas przylotu do destynacji
- `inboundArrivalTime()` - czas powrotu do origin

### FlightSegment (record)
Pojedynczy segment lotu (może być częścią lotu z przesiadkami).

```java
FlightSegment(
    String departureAirport,    // Kod IATA
    String arrivalAirport,      // Kod IATA
    ZonedDateTime departureTime,
    ZonedDateTime arrivalTime
)
```

### TripWindow (record)
Okno podróży używane jako input do search providera.

```java
TripWindow(
    String originIata,
    String destinationIata,
    ZonedDateTime outboundDeparture,  // Planowany wylot
    ZonedDateTime inboundDeparture,   // Planowany powrót
    int desiredFullDays               // Liczba pełnych dni
)
```

### TripConstraints (record)
Twarde ograniczenia dla wyszukiwania.

```java
TripConstraints(
    int maxStops,
    int maxTotalDurationMinutesOneWay,
    Integer hardCapPricePln,
    LocalTime latestArrivalOnFridayLocal,
    LocalTime earliestDepartureOnSundayLocal,
    boolean requireNoFlightOnSaturday
)
```

### CandidateWindow (record)
Kandydackie okno podróży używane przez generator i planner (oparte na LocalDate).

```java
CandidateWindow(
    String origin,          // "WAW"
    String destination,     // "LIS"
    LocalDate departDate,   // Data wylotu
    LocalDate returnDate    // Data powrotu
)
```

Metody:
- `fullDays()` - oblicza `ChronoUnit.DAYS.between(departDate, returnDate) - 1`
- `windowKey()` - generuje klucz w formacie `origin-destination-departDate-returnDate` (np. `WAW-LIS-2026-01-16-2026-01-18`)
- `hasSaturdayInMiddle()` - sprawdza czy sobota ∈ (departDate, returnDate), tj. sobota nie może być dniem wylotu ani powrotu

Walidacja w konstruktorze:
- `returnDate` musi być po `departDate`
- `fullDays` musi być >= 1

### PlannerResult (record)
Wynik działania WindowCheckPlanner.

```java
PlannerResult(
    List<CandidateWindow> selected,   // Wybrane okna do sprawdzenia
    int totalCandidates,              // Łączna liczba kandydatów
    int skippedRecentlyChecked,       // Pominięte (sprawdzone zbyt niedawno)
    int skippedBudget,                // Pominięte (przekroczony budżet)
    int selectedCount                 // Liczba wybranych
)
```

### PriceObservation (encja JPA)
Obserwacja cenowa zapisywana w bazie danych.

### WindowCheck (encja JPA)
Śledzenie sprawdzonych okien czasowych.

Kluczowe pola:
- `windowKey` - unikalny klucz okna (format: `origin-destination-departDate-returnDate`)
- `lastCheckedAt` - timestamp ostatniego sprawdzenia
- `checkCount` - licznik sprawdzeń

## Ewaluacja ofert

### TripEvaluator

Klasa odpowiedzialna za deterministyczną ocenę ofert lotów.

#### fullDaysOnSite(FlightOffer) -> int

Oblicza liczbę pełnych dni spędzonych w destynacji.

Algorytm:
1. Pobiera czas przylotu (ostatni segment outbound)
2. Pobiera czas wylotu powrotnego (pierwszy segment inbound)
3. Konwertuje na LocalDate w strefie destynacji
4. Liczy dni między (dzień_po_przylocie) a (dzień_wylotu) [exclusive]

Przykłady:
- Przylot pt 21:00, wylot nd 10:00 -> 1 dzień (sobota)
- Przylot pt 21:00, wylot pn 10:00 -> 2 dni (sobota + niedziela)
- Przylot czw 23:00, wylot pn 06:00 -> 3 dni (pt + sob + nd)

#### isSaturdayFull(FlightOffer, TripConstraints) -> boolean

Sprawdza czy sobota spełnia reguły weekendowe. Zwraca TRUE tylko gdy spełnione są WSZYSTKIE warunki:

1. Przylot w piątek (w strefie destynacji)
2. Przylot nie później niż 22:00 (lokalnie, włącznie)
3. Wylot w niedzielę (w strefie destynacji)
4. Wylot nie wcześniej niż 06:00 (lokalnie, włącznie)
5. Jeśli `requireNoFlightOnSaturday=true`: żaden segment nie ma departure/arrival w sobotę

Wszystkie sprawdzenia wykonywane w strefie czasowej destynacji.

#### meetsHardConstraints(FlightOffer, TripConstraints) -> boolean

Sprawdza czy oferta spełnia twarde ograniczenia:

- Przesiadki: `outbound_stops <= maxStops` AND `inbound_stops <= maxStops`
- Czas lotu: czas od departure pierwszego segmentu do arrival ostatniego <= max
- Cena: `price <= hardCapPricePln` (jeśli cap nie-null)

## Testy

Projekt zawiera testy jednostkowe z pokryciem logiki ewaluacji.

### Struktura testów

```
src/test/java/
└── pl/weekendflyer/weekendFlightAgent/domain/eval/
    ├── FlightOfferTestHelper.java            # Helper do tworzenia testowych ofert
    ├── TripEvaluatorFullDaysTest.java        # Testy fullDaysOnSite
    ├── TripEvaluatorSaturdayRuleTest.java    # Testy isSaturdayFull
    └── TripEvaluatorHardConstraintsTest.java # Testy meetsHardConstraints
```

### Uruchomienie testów

```bash
# Wszystkie testy
./mvnw test

# Grupa testów
./mvnw test -Dtest=TripEvaluator*Test

# Konkretna klasa testowa
./mvnw test -Dtest=TripEvaluatorFullDaysTest
```

## Roadmap

### Faza 1: Core (zrealizowane)
- [x] Struktura projektu i konfiguracja
- [x] Modele domenowe (records i encje JPA)
- [x] Ewaluator ofert z pełną logiką
- [x] Persystencja z PostgreSQL i Flyway
- [x] Repozytoria Spring Data JPA
- [x] Testy jednostkowe

### Faza 2: Generowanie okien i planowanie - ETAP 4 (zrealizowane)

#### Opis

ETAP 4 implementuje generowanie kandydackich okien podróży oraz inteligentne planowanie, które okna sprawdzić danego dnia.

#### Kluczowe koncepty

**CandidateWindow** - kandydackie okno oparte na `LocalDate`:
- `fullDays = daysBetween(departDate, returnDate) - 1`
- Sobota w środku: `Saturday ∈ (departDate, returnDate)` - sobota musi być w przedziale otwartym, nie może być dniem wylotu ani powrotu
- `windowKey` format: `{origin}-{destination}-{departDate}-{returnDate}` (np. `WAW-LIS-2026-01-16-2026-01-18`)

**TripWindowGenerator** - generuje wszystkie możliwe okna:
- Iteruje po origins × destinations × departDate × fullDaysAllowed
- Filtruje tylko okna z `hasSaturdayInMiddle() == true`
- Limity:
  - `maxWindowsPerDestinationPerDepartDate` - max okien dla pary (destination, departDate)
  - `maxWindowsGlobal` - globalny limit wygenerowanych okien
- Deterministyczne sortowanie (origin, destination, departDate, returnDate)

**WindowCheckPlanner** - wybiera podzbiór okien do sprawdzenia:
- `dailyBudgetPerProvider` - max okien do sprawdzenia dziennie per provider
- `minRecheckIntervalHours` - minimalny czas od ostatniego sprawdzenia
- Priorytet wyboru:
  1. Nowe okna (brak w DB) przed już sprawdzonymi
  2. Bliższe `departDate` wyżej
  3. Niższy `checkCount` wyżej
  4. `windowKey` jako tie-breaker (deterministycznie)
- Stan w DB (tabela `window_check`):
  - `lastCheckedAt` - aktualizowane przy każdym sprawdzeniu
  - `checkCount` - inkrementowane przy każdym sprawdzeniu

#### Konfiguracja (config.yaml)

```yaml
agent:
  planner:
    maxWindowsPerDestinationPerDepartDate: 3   # Max okien per (destination, departDate)
    maxWindowsGlobal: 500                      # Globalny limit kandydatów
    minRecheckIntervalHours: 12                # Min. przerwa między sprawdzeniami
    dailyBudgetPerProvider: 100                # Dzienny budżet per provider
```

#### Integracja z DailyScanJob

Job codziennie o 7:10:
1. Generuje kandydatów: `tripWindowGenerator.generate(origins, destinations, horizonDays, fullDaysAllowed)`
2. Planuje: `windowCheckPlanner.plan("default", candidates)`
3. Loguje statystyki: `totalCandidates`, `selectedCount`, `skippedRecentlyChecked`, `skippedBudget`
4. Aktualizuje `window_check` w DB (lastCheckedAt, checkCount)

#### Checklist Done - ETAP 4

- [x] `CandidateWindow` record z metodami `fullDays()`, `windowKey()`, `hasSaturdayInMiddle()`
- [x] `WindowKeyGenerator` - generator kluczy okien
- [x] `PlannerResult` record ze statystykami
- [x] `TripWindowGenerator` z limitami i filtrowaniem soboty
- [x] `WindowCheckPlanner` z priorytetami i budżetem
- [x] `WindowCheckRepository` rozszerzony o `findByProviderAndWindowKeyIn()`
- [x] Konfiguracja `AgentProperties.Planner`
- [x] Bean `Clock` (Europe/Warsaw)
- [x] Beany `TripWindowGenerator` i `WindowCheckPlanner`
- [x] `DailyScanJob` integruje generator i planner
- [x] Testy `TripWindowGeneratorTest` (14 scenariuszy)
- [x] Testy `WindowCheckPlannerTest` (10 scenariuszy)
- [x] Testy `CandidateWindowTest` i `WindowKeyGeneratorTest`
- [x] Test integracyjny `DailyScanJobIntegrationTest`

### Faza 3: Integracja z providerami
- [ ] Adapter dla Skyscanner API
- [ ] Adapter dla Kiwi.com API
- [ ] Rate limiting i retry logic
- [ ] Mapowanie odpowiedzi na FlightOffer

### Faza 4: Baseline tracking
- [ ] Obliczanie median dla segmentów (origin-destination-month-fullDays)
- [ ] Persystencja w tabeli baseline

### Faza 5: Filtrowanie i alerty
- [ ] Porównanie z baseline
- [ ] Filtrowanie kandydatów (minSaving, minPercent)
- [ ] Integracja z Telegram Bot API
- [ ] Email notifications
- [ ] Rate limiting alertów

### Faza 6: Produkcja
- [ ] CI/CD pipeline
- [ ] Monitoring i metryki
- [ ] Dashboard (opcjonalnie)

## Licencja

Projekt demo - brak określonej licencji.

---

**Status:** W aktywnym rozwoju | **Wersja:** 0.0.1-SNAPSHOT | **Java:** 17 | **Spring Boot:** 4.0.1
