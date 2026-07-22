# Spiderman

> Kombiniert Wandklettern per Schleichen und einen Netz-Schwung per Rechtsklick zu einer Spider-Man-artigen Bewegungs-Mechanic.

| | |
|---|---|
| **Config-Key** | `Mechanics.spiderman` |
| **Gilt für** | Item / Rüstungsteil (Klettern: Hauptvhand, Nebenhand oder beliebiger Rüstungsslot; Netzschuss: nur Hauptvhand) |
| **Listener-Klasse** | `SpiderMan.SpiderManListener` |
| **Toggle/Sneak-Verhalten** | Schleichen an einer Wand aktiviert das Klettern automatisch; Rechtsklick schießt das Netz, ein erneuter Rechtsklick während des Schwungs bricht ihn ab |

## Was macht sie?

Zwei unabhängige Teilfunktionen: **Wall-Climb** – solange der Spieler schleicht und horizontal (auf Fuß- oder Kopfhöhe, eine der vier Himmelsrichtungen) an einen soliden Block angrenzt, setzt ein Tick-Task die Y-Geschwindigkeit auf `wall_climb.climb_speed` und verhindert Fallschaden. **Web-Shot** – Rechtsklick raytraced bis `web_shot.max_distance`; trifft der Strahl einen Block, wird der Spieler wie beim Enterhaken mit `web_shot.pull_speed` zum Trefferpunkt gezogen, zusätzlich mit einem Aufwärts-Bogen (`arc_boost`) in den ersten 5 Ticks des Schwungs. Beide Funktionen nutzen `setAllowFlight`, um Anti-Cheat-Flags zu vermeiden, und werden beim Bodenkontakt, Logout oder erneuten Auslösen sauber zurückgesetzt.

## Wann einsetzen?

- Superhelden-/Fantasy-Kits mit vertikaler Bewegung
- Movement-Items für Parkour-Server
- PvE-Content auf Karten mit vielen Wänden/Klippen

## Konfiguration

> Kein einzelner Pflicht-Key – schon ein leerer `Mechanics.spiderman:`-Block aktiviert die Mechanic mit allen Defaults.

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.spiderman.enabled` | `bool` | `true` | Schaltet die gesamte Mechanic ein/aus |
| `Mechanics.spiderman.wall_climb.enabled` | `bool` | `true` | Aktiviert das Wandklettern |
| `Mechanics.spiderman.wall_climb.climb_speed` | `double` | `0.25` | Aufwärtsgeschwindigkeit pro Tick beim Klettern |
| `Mechanics.spiderman.wall_climb.check_slot` | `String` | `"ANY"` | Wird geladen, aktuell aber **nicht ausgewertet** (siehe Hinweise) |
| `Mechanics.spiderman.web_shot.enabled` | `bool` | `true` | Aktiviert den Netzschuss |
| `Mechanics.spiderman.web_shot.max_distance` | `int` | `20` | Maximale Reichweite des Raycasts in Blöcken |
| `Mechanics.spiderman.web_shot.pull_speed` | `double` | `1.0` | Zuggeschwindigkeit pro Tick während des Schwungs |
| `Mechanics.spiderman.web_shot.arc_boost` | `double` | `0.4` | Zusätzlicher Y-Boost in den ersten 5 Ticks des Schwungs |
| `Mechanics.spiderman.web_shot.cooldown` | `int` | `2` | Abklingzeit in Sekunden zwischen zwei Netzschüssen |
| `Mechanics.spiderman.particle.type` | `String` | `"CLOUD"` | Partikel für Netzlinie und Bewegungsspur |
| `Mechanics.spiderman.particle.amount` | `int` | `3` | Partikelanzahl pro Tick/Punkt |
| `Mechanics.spiderman.sound.type` | `String` | `"ENTITY_FISHING_BOBBER_THROW"` | Sound beim Abschuss |
| `Mechanics.spiderman.sound.volume` | `float` | `1.0` | Lautstärke |
| `Mechanics.spiderman.sound.pitch` | `float` | `1.5` | Tonhöhe |
| `Mechanics.spiderman.cooldown_message` | `String` | `"<red>Noch {time}s Abklingzeit!"` | Nachricht bei aktivem Cooldown (MiniMessage, `{time}` = verbleibende Sekunden) |
| `Mechanics.spiderman.durability_cost` | `int` | `0` | Haltbarkeitskosten pro Netzschuss (0 = keine) |

## Beispiel

```yaml
example_spiderman_suit:
  itemname: "<red><bold>Spider-Man Anzug"
  material: LEATHER_CHESTPLATE
  Mechanics:
    spiderman:
      enabled: true
      wall_climb:
        enabled: true
        climb_speed: 0.25
        check_slot: "ANY"
      web_shot:
        enabled: true
        max_distance: 20
        pull_speed: 1.0
        arc_boost: 0.4
        cooldown: 2
      particle:
        type: "CLOUD"
        amount: 3
      sound:
        type: "ENTITY_FISHING_BOBBER_THROW"
        volume: 1.0
        pitch: 1.5
      cooldown_message: "<red>Noch <bold>{time}s</bold> Abklingzeit!"
      durability_cost: 0
```

## Hinweise & Besonderheiten

- `wall_climb.check_slot` wird zwar aus der Config gelesen, von der aktuellen Logik aber nicht ausgewertet: Wandklettern greift, sobald das Item in Hauptvhand, Nebenhand **oder** irgendeinem Rüstungsslot liegt; der Netzschuss reagiert dagegen ausschließlich auf die Hauptvhand.
- `setAllowFlight` wird für beide Teilfunktionen genutzt und beim Loslassen/Abbrechen wieder zurückgesetzt (außer in Creative/Spectator).
- Klettern erfordert einen soliden Block seitlich auf Fuß- oder Kopfhöhe in einer der vier Himmelsrichtungen – keine Diagonalen.
- Der Netzschwung bricht automatisch ab bei Bodenkontakt, Spieler-Logout oder nach spätestens 80 Ticks (~4 Sekunden), analog zum Grappling Hook.
