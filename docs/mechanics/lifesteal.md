# LifeSteal

> Heilt den Spieler bei Treffern mit der Waffe um einen Prozentsatz des verursachten Schadens.

| | |
|---|---|
| **Config-Key** | `Mechanics.lifesteal` |
| **Gilt für** | Item (Waffe) |
| **Listener-Klasse** | `LifeSteal.LifeStealListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – greift automatisch bei jedem Treffer |

## Was macht sie?

Verursacht ein Spieler mit einer LifeSteal-Waffe in der Haupthand Schaden an einer lebenden Entity (`EntityDamageByEntityEvent`), berechnet die Mechanic aus dem finalen Schaden (`getFinalDamage()`) einen Heilbetrag als Prozentsatz (`percentage`), begrenzt diesen optional nach unten (`min_heal`) und oben (`max_heal`) und heilt den Spieler entsprechend, ohne über sein Maximal-Leben hinauszugehen. Standardmäßig heilt die Mechanic nicht gegen Untote-Gegner, sofern nicht `affect_undead: true` gesetzt ist.

## Wann einsetzen?

- Vampir-/Blutthemen-Waffen im Shop oder als Boss-Drop
- Sustain-Ausrüstung für PvP-/Dungeon-Kits mit klar begrenztem Heil-Fenster (`min_heal`/`max_heal`)
- Balancing gegen Untote-Farmen: Standard-Verhalten schließt Zombies/Skelette etc. von der Heilung aus

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.lifesteal.percentage` | `double` | `10.0` | **(Pflicht, aktiviert die Mechanic)** Prozentsatz des verursachten Schadens, der als Heilung gutgeschrieben wird (`10.0` = 10 %) |
| `Mechanics.lifesteal.min_heal` | `double` | `0.0` | Mindest-Heilbetrag pro Treffer. `0` = keine Untergrenze |
| `Mechanics.lifesteal.max_heal` | `double` | `0.0` | Maximaler Heilbetrag pro Treffer. `0` = keine Obergrenze |
| `Mechanics.lifesteal.affect_undead` | `bool` | `false` | Ob die Mechanic auch bei Treffern gegen Untote (Zombie, Skelett, Wither, Phantom, …) heilt |

## Beispiel

```yaml
vampire_sword:
  material: DIAMOND_SWORD
  Mechanics:
    lifesteal:
      percentage: 10.0
      min_heal: 0.5
      max_heal: 5.0
      affect_undead: false
```

## Hinweise & Besonderheiten

- Läuft mit `EventPriority.MONITOR` und `ignoreCancelled = true` – gecancelte Schadens-Events lösen keine Heilung aus, und die Heilung selbst beeinflusst den Schaden nicht mehr.
- Die Liste der als "untot" behandelten Entity-Typen ist fest im Code hinterlegt (u. a. Zombie, Husk, Drowned, Skeleton, Stray, Wither Skeleton, Phantom, Wither, Zoglin, Zombified Piglin) und nicht per Konfiguration anpassbar.
- Wirkt nur, solange die Waffe in der **Haupthand** geführt wird.
- Die Heilung kann das Maximal-Leben des Spielers nie überschreiten.
