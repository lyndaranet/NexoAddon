# GlassBreaker

> Baut Glasblöcke mit einem Klick sofort ab, ganz ohne Abbauzeit.

| | |
|---|---|
| **Config-Key** | `Mechanics.glassbreaker` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `GlassBreaker.GlassBreakerListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – immer aktiv, solange `enabled: true` |

## Was macht sie?

Beginnt ein Spieler (außerhalb des Kreativmodus) mit einem GlassBreaker-Werkzeug einen Block abzubauen (`BlockDamageEvent`), prüft die Mechanic zunächst über `ProtectionLib`, ob der Spieler an dieser Stelle überhaupt abbauen darf. Ist der Block ein Glasblock – entweder aus der konfigurierten `glass_types`-Liste oder, falls diese leer ist, aus einer eingebauten Standardliste aller Vanilla-Glasblöcke (Glas, alle gefärbten Glas- und Glasscheiben-Varianten, Tinted Glass) bzw. ein in `glass_types` gelisteter Nexo-Custom-Block – wird der Block per `setInstaBreak(true)` sofort ohne Abbauzeit zerstört. Anschließend wird dem Werkzeug, sofern `durability_cost` größer `0` ist, Durability abgezogen; eine vorhandene Unbreaking-Verzauberung reduziert dabei die Chance auf Durability-Verlust wie im Vanilla-Verhalten. Erreicht das Werkzeug seine maximale Durability, wird es aus der Hand entfernt und ein Bruch-Sound abgespielt.

## Wann einsetzen?

- Spezialwerkzeuge für Glasbauten/Gewächshäuser, bei denen das Abbauen vieler Glasblöcke sonst zu lange dauert
- Kombination mit eigenen Nexo-Glasblöcken (z. B. Fantasy-Fenster) über die `glass_types`-Liste
- Werkzeuge mit begrenztem Verschleiß pro Glasblock, um die Nutzung wirtschaftlich zu steuern

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.glassbreaker.enabled` | `bool` | `true` | Ob die Mechanic aktiv ist |
| `Mechanics.glassbreaker.durability_cost` | `int` | `1` | Durability-Kosten pro sofort abgebautem Glasblock. `0` = kein Verschleiß |
| `Mechanics.glassbreaker.glass_types` | `List<String>` | `[]` (= alle Standard-Glasblöcke) | Vanilla-Materials und/oder Nexo-IDs, die als "Glas" für den Insta-Break gelten. Leer = eingebaute Liste aller Vanilla-Glas- und Glasscheiben-Varianten inkl. Tinted Glass |

> Der Abschnitt `Mechanics.glassbreaker` muss lediglich existieren, damit die Mechanic geladen wird.

## Beispiel

```yaml
glass_hammer:
  material: IRON_HOE
  Mechanics:
    glassbreaker:
      enabled: true
      durability_cost: 1
      glass_types:
        - GLASS
        - GLASS_PANE
        - TINTED_GLASS
        - custom_nexo_glass
```

## Hinweise & Besonderheiten

- Funktioniert im Kreativmodus nicht (dort ist Insta-Break ohnehin Standard) – die Mechanic greift ausschließlich im Überlebensmodus.
- Respektiert Schutz-Plugins über `ProtectionLib`: Ist der Bereich geschützt, wird der Block gar nicht erst als abbaubar behandelt.
- Ist `glass_types` gefüllt, wird **ausschließlich** diese Liste geprüft – die eingebaute Standardliste greift dann nicht mehr zusätzlich.
