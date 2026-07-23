# BetterMine

> Zwei unabhängig togglebare Abbau-Boni mit jeweils eigener Blockliste: schnelleres Abbauen (Haste) und eine Chance auf Bonus-Drops. Gedacht für Premium-Werkzeuge mit spürbarem, aber nicht überzogenem Vorteil.

| | |
|---|---|
| **Config-Key** | `Mechanics.better_mine` |
| **Gilt für** | Item |
| **Listener-Klasse** | `BetterMineMechanic.BetterMineMechanicListener` |
| **Toggle/Sneak-Verhalten** | Immer aktiv, solange gehalten; `faster_mining` und `bonus_drops` je einzeln per `enabled` schaltbar |

## Was macht sie?

`better_mine` bündelt zwei unabhängige Effekte, die jeweils eine eigene Blockliste (`blocks`) haben und komplett optional sind. **faster_mining** hört auf `BlockDamageEvent`: Ist der angeschlagene Block gelistet, bekommt der Spieler kurzzeitig Haste (`FAST_DIGGING`), gerundet auf Vanilla-20%-Stufen (`percentage=25` → Haste I). Der Effekt wird bei jedem Anschlag neu für 2 Sekunden angewendet und läuft aus, wenn nicht weiter am gelisteten Block gearbeitet wird. Ist bereits ein stärkerer Haste-Effekt aktiv (Beacon, Trank, anderes Item), wird er nicht abgeschwächt. **bonus_drops** hört auf `BlockDropItemEvent`: Ist der abgebaute Block gelistet, gibt es mit `chance`-%-Wahrscheinlichkeit zusätzliche Drops als Multiplikator (`multiplier`) auf die bereits fertig berechneten Drops – Fortune/Silk Touch wirken also bereits vorher und der Bonus kommt on top. Bei erfolgreichem Bonus-Drop gibt's Sound (nur für den Spieler selbst), Partikel am Block (für alle sichtbar) und eine Actionbar-Meldung.

## Wann einsetzen?

- Premium-/Shop-Werkzeuge, die Vanilla-Abbau spürbar, aber nicht übertrieben verbessern
- Seltene Erz-Pickaxen mit Bonus-Drop-Chance auf Diamant/Emerald statt pauschaler Fortune-Erhöhung
- Kombination mit anderen Abbau-Mechaniken (z. B. VeinMiner, BigMining) am selben Item, da `better_mine` unabhängig auf eigenen Events arbeitet

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.better_mine.enabled` | `bool` | `true` | Mechanic global an/aus |
| `Mechanics.better_mine.faster_mining.enabled` | `bool` | `true` | Haste-Effekt an/aus |
| `Mechanics.better_mine.faster_mining.percentage` | `double` | `0.0` | Ziel-Beschleunigung in %; wird auf Vanilla-Haste-Stufen (20%-Schritte) gerundet |
| `Mechanics.better_mine.faster_mining.blocks` | `List<Material>` | leer | Blöcke, auf denen Haste ausgelöst wird |
| `Mechanics.better_mine.bonus_drops.enabled` | `bool` | `true` | Bonus-Drop-Chance an/aus |
| `Mechanics.better_mine.bonus_drops.chance` | `double` (%) | `0.0` | Chance pro Block-Break auf zusätzliche Drops |
| `Mechanics.better_mine.bonus_drops.multiplier` | `double` | `2.0` | Multiplikator auf die bereits berechneten Drops bei Erfolg (`2.0` = doppelte Menge) |
| `Mechanics.better_mine.bonus_drops.blocks` | `List<Material>` | leer | Blöcke, auf denen die Bonus-Drop-Chance greift |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadBetterMineMechanic`). Ohne `Mechanics.better_mine`-Abschnitt lädt die Mechanic gar nicht; `faster_mining`/`bonus_drops` sind jeweils nur aktiv, wenn ihr eigener Abschnitt existiert.

## Beispiel

```yaml
better_mining_pickaxe:
  displayname: "<gold>Spitzhacke der Fülle"
  material: DIAMOND_PICKAXE
  Mechanics:
    better_mine:
      enabled: true
      faster_mining:
        enabled: true
        percentage: 25.0
        blocks:
          - OBSIDIAN
          - CRYING_OBSIDIAN
          - ANCIENT_DEBRIS
      bonus_drops:
        enabled: true
        chance: 15.0
        multiplier: 2.0
        blocks:
          - DIAMOND_ORE
          - DEEPSLATE_DIAMOND_ORE
          - EMERALD_ORE
```

## Hinweise & Besonderheiten

- `percentage` wird nicht exakt umgesetzt, sondern auf Vanilla-Haste-Stufen (20%-Schritte) gerundet – für exakte Werte wäre eine manuelle Abbauzeit-Simulation nötig (deutlich mehr Aufwand, aktuell nicht implementiert).
- Haste wird pro Anschlag für 2 Sekunden gesetzt; bei langsam abbaubaren Blöcken kann der Effekt vor Abschluss des Abbaus auslaufen, wenn der Spieler zwischenzeitlich pausiert.
- Ein bereits vorhandener, stärkerer Haste-Effekt (z. B. Beacon oder Trank) wird nicht überschrieben – es gilt immer der höhere Level.
- `bonus_drops` wirkt **nach** Fortune/Silk Touch, da `BlockDropItemEvent` die bereits fertig berechneten Drops liefert – der Multiplikator ist also ein reiner Bonus obendrauf.
- Bei kleinen Stacks (z. B. Amount=1) und `multiplier < 2.0` kann die gerundete Extra-Menge `0` ergeben – kein Bug, sondern erwartetes Verhalten.
- Feedback (Sound/Partikel/Actionbar) triggert ausschließlich bei tatsächlichem Bonus-Drop (Extra-Menge > 0), nicht bei jedem Break.
- Der Sound ist per `SoundCategory.PLAYERS` + `player.playSound(...)` nur für den brechenden Spieler hörbar; die Partikel sind world-basiert und für alle Nahestehenden sichtbar.
- Leere/fehlende Blocklisten lassen den jeweiligen Effekt nirgends greifen, ohne zu crashen.
