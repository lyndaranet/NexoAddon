# Grappling Hook

> Schießt einen Enterhaken auf den anvisierten Block und zieht den Spieler mit einem sichtbaren Partikel-Seil daran heran.

| | |
|---|---|
| **Config-Key** | `Mechanics.grappling_hook` |
| **Gilt für** | Item (Werkzeug/Waffe, i. d. R. `FISHING_ROD`) |
| **Listener-Klasse** | `GrapplingHook.GrapplingHookListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick schießt den Haken; ein erneuter Rechtsklick während des Zugs bricht ihn sofort ab |

## Was macht sie?

Rechtsklick (Luft oder Block) mit dem Item raytraced entlang der Blickrichtung bis `max_distance`. Trifft der Strahl einen Block, wird dort ein fester Ankerpunkt gesetzt, das reguläre Angel-Verhalten (Bobber, Wurf-Animation) unterdrückt, und der Spieler wird für maximal 80 Ticks pro Tick mit `pull_speed` Richtung Anker gezogen, bis er nahe genug ist (< 1.5 Blöcke) oder den Boden berührt. Währenddessen wird jeden Tick ein Partikel-Seil (DUST) zwischen Hand und Anker gezeichnet sowie optional eine Partikelspur um den Spieler erzeugt. Ein Cooldown pro Spieler verhindert Dauerfeuer, optional wird Haltbarkeit verbraucht.

## Wann einsetzen?

- Mobility-/Traversal-Items im Shop oder Kit
- Parkour- oder Bewegungs-fokussierte Server-Modi
- Alternative zum Elytra für kurze, gezielte Sprünge zu festen Punkten

## Konfiguration

> Kein einzelner Pflicht-Key – schon ein leerer `Mechanics.grappling_hook:`-Block aktiviert die Mechanic mit allen Defaults.

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.grappling_hook.enabled` | `bool` | `true` | Schaltet die Mechanic für dieses Item grundsätzlich ein/aus |
| `Mechanics.grappling_hook.max_distance` | `int` | `30` | Maximale Reichweite des Raycasts in Blöcken |
| `Mechanics.grappling_hook.pull_speed` | `double` | `0.8` | Zuggeschwindigkeit pro Tick während des Zugs |
| `Mechanics.grappling_hook.cooldown` | `int` | `3` | Abklingzeit in Sekunden zwischen zwei Würfen |
| `Mechanics.grappling_hook.particle.type` | `String` | `"CRIT"` | Partikel-Spur um den Spieler während des Zugs |
| `Mechanics.grappling_hook.particle.amount` | `int` | `3` | Partikelanzahl pro Tick |
| `Mechanics.grappling_hook.sound.type` | `String` | `"ENTITY_FISHING_BOBBER_THROW"` | Sound beim Abschuss |
| `Mechanics.grappling_hook.sound.volume` | `float` | `1.0` | Lautstärke |
| `Mechanics.grappling_hook.sound.pitch` | `float` | `1.2` | Tonhöhe |
| `Mechanics.grappling_hook.cooldown_message` | `String` | `"<red>Noch {time}s Abklingzeit!"` | Nachricht bei aktivem Cooldown (MiniMessage, `{time}` = verbleibende Sekunden) |
| `Mechanics.grappling_hook.durability_cost` | `int` | `1` | Haltbarkeitskosten pro Wurf (0 = keine) |
| `Mechanics.grappling_hook.rope.color` | `String` | `"139,90,43"` | Seilfarbe als `"r,g,b"` (0–255); bei ungültigem Wert Fallback auf braun |
| `Mechanics.grappling_hook.rope.size` | `float` | `0.5` | Partikelgröße des Seils (DUST) |
| `Mechanics.grappling_hook.rope.spacing` | `double` | `0.35` | Abstand zwischen den Seil-Partikeln (kleiner = dichter) |

## Beispiel

```yaml
example_grappling_hook:
  itemname: "<gold><bold>Greifhaken"
  material: FISHING_ROD
  Mechanics:
    grappling_hook:
      enabled: true
      max_distance: 30
      pull_speed: 0.8
      cooldown: 3
      durability_cost: 1
      cooldown_message: "<red>Noch <bold>{time}s</bold> Abklingzeit!"
      particle:
        type: "CRIT"
        amount: 3
      sound:
        type: "ENTITY_FISHING_BOBBER_THROW"
        volume: 1.0
        pitch: 1.2
      rope:
        color: "139,90,43"
        size: 0.5
        spacing: 0.35
```

## Hinweise & Besonderheiten

- Während des Zugs wird `setAllowFlight(true)` gesetzt, um Anti-Cheat-Plugins (z. B. Vulcan) nicht durch die künstliche Luftbewegung zu triggern; beim Abbruch wird der Zustand zurückgesetzt (außer in Creative/Spectator).
- `PlayerFishEvent` wird zusätzlich abgefangen und gecancelt, falls durch Client-Eigenheiten doch ein Angel-Bobber gespawnt wird.
- Der Zug bricht automatisch ab bei Bodenkontakt, Spieler-Logout oder nach spätestens 80 Ticks (~4 Sekunden).
- `rope.color` erwartet exakt drei kommagetrennte RGB-Werte; jedes andere Format fällt stillschweigend auf die braune Standardfarbe zurück.
