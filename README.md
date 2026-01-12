# Weekend Flight Agent 🛫

Agent wyszukujący i monitorujący tanie loty weekendowe z Polski do wybranych destynacji w Europie.

## 📋 Spis treści

- [Opis projektu](#-opis-projektu)
- [Funkcje](#-funkcje)
- [Technologie](#-technologie)
- [Wymagania](#-wymagania)
- [Instalacja](#-instalacja)
- [Konfiguracja](#-konfiguracja)
- [Uruchomienie](#-uruchomienie)
- [Architektura](#-architektura)
- [Modele domenowe](#-modele-domenowe)
- [Ewaluacja ofert](#-ewaluacja-ofert)
- [Testy](#-testy)
- [Roadmap](#-roadmap)

## 🎯 Opis projektu

Weekend Flight Agent to aplikacja automatyzująca proces wyszukiwania okazji na weekendowe wyjazdy. System codziennie skanuje dostępne połączenia lotnicze, filtruje je według określonych kryteriów (pełna sobota w miejscu docelowym, rozsądne ceny, maksymalna liczba przesiadek) i wykrywa potencjalne okazje cenowe.

### Główne założenia
- **Pełna sobota**: przylot w piątek wieczorem, wylot w niedzielę rano
- **Automatyzacja**: codzienny scan bez interwencji użytkownika
- **Inteligentne filtrowanie**: tylko rzeczywiście atrakcyjne oferty
- **Baseline tracking**: porównanie z historycznymi cenami

## ✨ Funkcje

### Zaimplementowane
- ✅ Konfiguracja wielokryterialna (strefy czasowe, lotniska, ograniczenia)
- ✅ Modele domenowe (FlightOffer, TripWindow, TripConstraints)
- ✅ Ewaluator ofert z determinestyczną logiką:
  - Obliczanie pełnych dni w destynacji
  - Walidacja reguł soboty weekendowej
  - Sprawdzanie twardych ograniczeń (przesiadki, czas lotu, cena)
- ✅ Scheduled job (codziennie o 07:10)
- ✅ Kompleksowe testy jednostkowe (32 testy)

### W planach
- 🔄 Generowanie okien podróży (TripWindow)
- 🔄 Integracja z API dostawców lotów (Skyscanner, Kiwi.com)
- 🔄 Baseline tracking (śledzenie median cenowych)
- 🔄 Filtrowanie kandydatów (porównanie z baseline)
- 🔄 System alertów (Telegram/Email)
- 🔄 Persystencja wyników (baza danych)

## 🛠 Technologie

- **Java 17**
- **Spring Boot 4.0.1**
- **Maven** - zarządzanie zależnościami
- **Lombok** - redukcja boilerplate code
- **JUnit 5** - testy jednostkowe
- **SLF4J + Logback** - logowanie

## 📦 Wymagania

- Java 17 lub nowszy
- Maven 3.6+ (lub użyj dołączonego `mvnw`)
- 512 MB RAM (minimalne)

## 🚀 Instalacja

```bash
# Klonowanie repozytorium
git clone https://github.com/your-username/weekend-flight-agent.git
cd weekend-flight-agent

# Build projektu
./mvnw clean install

# Lub na Windows
mvnw.cmd clean install
```

## ⚙️ Konfiguracja

Główny plik konfiguracyjny: `src/main/resources/config.yaml`

### Podstawowe ustawienia

```yaml
agent:
  timezone: "Europe/Warsaw"
  
  origins:
    - "WAW"  # Warszawa Okęcie
    # - "WMI"  # Warszawa Modlin (opcjonalnie)
  
  destinations:
    - "LIS"  # Lizbona
    - "BCN"  # Barcelona
    - "MAD"  # Madryt
    - "FCO"  # Rzym
    # ... więcej destynacji
```

### Parametry wyszukiwania

```yaml
search:
  horizonDays: 92              # Ile dni w przód skanować
  fullDaysAllowed: [2, 3, 4]  # Akceptowana liczba pełnych dni (pt-nd=2, czw-pn=3, etc.)
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

## 🎮 Uruchomienie

### Tryb deweloperski

```bash
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

### Testy

```bash
# Wszystkie testy
./mvnw test

# Tylko testy TripEvaluator
./mvnw test -Dtest=TripEvaluator*Test

# Konkretna klasa testowa
./mvnw test -Dtest=TripEvaluatorFullDaysTest
```

## 🏗 Architektura

```
weekend-flight-agent/
├── src/main/java/pl/weekendflyer/weekendFlightAgent/
│   ├── WeekendFlightAgentApplication.java    # Klasa główna
│   ├── config/
│   │   └── AgentProperties.java              # Mapowanie config.yaml
│   ├── scheduler/
│   │   └── DailyScanJob.java                 # Codzienne zadanie (7:10)
│   ├── domain/
│   │   ├── model/                            # Modele domenowe
│   │   │   ├── FlightOffer.java              # Oferta lotu (round-trip)
│   │   │   ├── FlightSegment.java            # Segment lotu
│   │   │   ├── TripWindow.java               # Okno podróży
│   │   │   └── TripConstraints.java          # Ograniczenia
│   │   └── eval/                             # Ewaluacja ofert
│   │       ├── TripEvaluator.java            # Główna logika oceny
│   │       └── TripConstraintsFactory.java   # Factory dla constraints
│   └── resources/
│       ├── application.yaml                   # Spring Boot config
│       └── config.yaml                        # Agent config
└── src/test/java/                             # Testy jednostkowe (32)
```

## 📊 Modele domenowe

### FlightOffer
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

**Metody pomocnicze:**
- `totalStops()` - łączna liczba przesiadek
- `outboundArrivalTime()` - czas przylotu do destynacji
- `inboundArrivalTime()` - czas powrotu do origin

### FlightSegment
Pojedynczy segment lotu (może być częścią lotu z przesiadkami).

```java
FlightSegment(
    String departureAirport,    // Kod IATA
    String arrivalAirport,      // Kod IATA
    ZonedDateTime departureTime,
    ZonedDateTime arrivalTime
)
```

### TripWindow
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

### TripConstraints
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

## 🎯 Ewaluacja ofert

### TripEvaluator

Klasa odpowiedzialna za deterministyczną ocenę ofert lotów.

#### 1. `fullDaysOnSite(FlightOffer)` → int

Oblicza liczbę pełnych dni spędzonych w destynacji.

**Algorytm:**
- Pobiera czas przylotu (ostatni segment outbound)
- Pobiera czas wylotu powrotnego (pierwszy segment inbound)
- Konwertuje na LocalDate w strefie destynacji
- Liczy dni między (dzień_po_przylocie) a (dzień_wylotu) [exclusive]

**Przykłady:**
- Przylot pt 21:00, wylot nd 10:00 → **1 dzień** (sobota)
- Przylot pt 21:00, wylot pn 10:00 → **2 dni** (sobota + niedziela)
- Przylot czw 23:00, wylot pn 06:00 → **3 dni** (pt + sob + nd)

#### 2. `isSaturdayFull(FlightOffer, TripConstraints)` → boolean

Sprawdza czy sobota spełnia reguły weekendowe (zwraca TRUE tylko gdy spełnione są WSZYSTKIE warunki).

**Warunki (wszystkie muszą być spełnione):**
1. Przylot w **piątek** (w strefie destynacji)
2. Przylot nie później niż **22:00** (lokalnie, **włącznie: <=**)
3. Wylot w **niedzielę** (w strefie destynacji)
4. Wylot nie wcześniej niż **06:00** (lokalnie, **włącznie: >=**)
5. Jeśli `requireNoFlightOnSaturday=true`: **ŻADEN** segment nie ma departure/arrival w sobotę

**Strefy czasowe:**
Wszystkie sprawdzenia wykonywane w strefie czasowej destynacji. Przykład:
- Lot WAW pt 23:00 → BCN sob 01:00 (czas BCN) = wykryty jako lot w sobotę ❌

#### 3. `meetsHardConstraints(FlightOffer, TripConstraints)` → boolean

Sprawdza czy oferta spełnia twarde ograniczenia.

**Sprawdzane:**
- **Przesiadki**: `outbound_stops ≤ maxStops` AND `inbound_stops ≤ maxStops`
  - Przesiadki = liczba segmentów - 1
- **Czas lotu**: czas od departure pierwszego segmentu do arrival ostatniego ≤ max (osobno dla każdego kierunku)
- **Cena**: `price ≤ hardCapPricePln` (jeśli cap nie-null)

**Przykłady:**
- 1 segment 2h, cena 1500, limit 2000 → ✅
- 2 segmenty (1 przesiadka), 7h, limit 1 przesiadka i 8h → ✅
- 3 segmenty (2 przesiadki), limit 1 → ❌
- 1 segment 9h, limit 8h → ❌

## 🧪 Testy

Projekt zawiera **33 testy jednostkowe** z pełnym pokryciem logiki ewaluacji.

### Struktura testów

```
src/test/java/
└── pl/weekendflyer/weekendFlightAgent/domain/eval/
    ├── TripEvaluatorFullDaysTest.java          (7 testów)
    ├── TripEvaluatorSaturdayRuleTest.java     (13 testów)
    └── TripEvaluatorHardConstraintsTest.java  (13 testów)
```

### Uruchomienie testów

```bash
# Wszystkie testy
./mvnw test

# Grupa testów
./mvnw test -Dtest=TripEvaluator*Test

# Z raportem pokrycia
./mvnw test jacoco:report
```

### Przykładowe case'y testowe

**fullDaysOnSite:**
- ✅ Przylot pt 21:00, wylot nd 10:00 → 1 dzień
- ✅ Przylot pt 21:00, wylot pn 10:00 → 2 dni
- ✅ Przylot czw 23:00, wylot pn 06:00 → 3 dni
- ✅ Przylot sob 01:00, wylot pn 10:00 → 1 dzień

**isSaturdayFull:**
- ✅ Przylot pt 21:59, wylot nd 06:00 → true (granica)
- ✅ Przylot pt 22:00, wylot nd 06:00 → true (granica włączona <=)
- ✅ Przylot pt 22:01 → false (po progu)
- ✅ Wylot nd 05:59 → false (przed progiem)
- ✅ Segment z przylotem w sobotę → false

**meetsHardConstraints:**
- ✅ 1 segment, 2h, 1500 PLN → true
- ✅ 2 segmenty (1 stop), 7h → true
- ✅ 3 segmenty (2 stops) → false
- ✅ 9h (przekroczenie limitu 8h) → false

## 🗺 Roadmap

### Faza 1: Core (✅ Zrealizowane)
- [x] Struktura projektu i konfiguracja
- [x] Modele domenowe
- [x] Ewaluator ofert z pełną logiką
- [x] Testy jednostkowe

### Faza 2: Generowanie okien (🔄 W toku)
- [ ] Generator TripWindow na podstawie horizonDays i fullDaysAllowed
- [ ] Filtrowanie według earliestDeparture/latestArrival z origin
- [ ] Optymalizacja liczby zapytań do API

### Faza 3: Integracja z providerami
- [ ] Adapter dla Skyscanner API
- [ ] Adapter dla Kiwi.com API
- [ ] Rate limiting i retry logic
- [ ] Mapowanie odpowiedzi na FlightOffer

### Faza 4: Baseline tracking
- [ ] Model danych dla historii cen
- [ ] Obliczanie median dla segmentów (origin-destination-month-fullDays)
- [ ] Persystencja w bazie danych

### Faza 5: Filtrowanie i alerty
- [ ] Porównanie z baseline
- [ ] Filtrowanie kandydatów (minSaving, minPercent)
- [ ] Integracja z Telegram Bot API
- [ ] Email notifications
- [ ] Rate limiting alertów

### Faza 6: Produkcja
- [ ] Dockeryzacja
- [ ] CI/CD pipeline
- [ ] Monitoring i metryki
- [ ] Dashboard (opcjonalnie)

## 📝 Licencja

Projekt demo - brak określonej licencji.

## 🤝 Kontakt

Projekt powstał jako Weekend Flight Agent dla automatyzacji wyszukiwania tanich lotów weekendowych.

---

**Status:** 🚧 W aktywnym rozwoju | **Wersja:** 0.0.1-SNAPSHOT | **Java:** 17 | **Spring Boot:** 4.0.1

# PostgreSQL Development Database

## Uruchomienie bazy danych

Aby uruchomić bazę danych Postgres w Docker:

```bash
docker-compose up -d
```

## Zatrzymanie bazy danych

```bash
docker-compose down
```

## Parametry połączenia

- **Database**: flight_agent
- **User**: flight_agent
- **Password**: flight_agent
- **Port**: 5432
- **JDBC URL**: jdbc:postgresql://localhost:5432/flight_agent

## Dane

Dane są przechowywane w trwałym volume `postgres_data` i przetrwają restart kontenera.

## Zarządzanie

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



