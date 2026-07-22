# Block Aura

> Lässt einen platzierten Custom Block oder ein Furniture-Objekt dauerhaft Partikel an seinem Standort erzeugen, bis er/es wieder abgebaut wird.

| | |
|---|---|
| **Config-Key** | `Mechanics.block_aura` |
| **Gilt für** | Custom Block (`custom_block.*`) und Furniture (`furniture.*`) |
| **Listener-Klasse** | `BlockAura.BlockAuraListener` |
| **Toggle/Sneak-Verhalten** | Keins – passiv aktiv, solange der Block/das Furniture existiert |

## Was macht sie?

Beim Platzieren eines Blocks oder Furnitures mit `Mechanics.block_aura` wird ein wiederkehrender Async-Task gestartet, der am Standort des Blocks (mit den konfigurierten Offsets) Partikel spawnt. Wird der Block/das Furniture abgebaut, wird der Task sofort gestoppt. Der aktive Zustand wird zusätzlich über `CustomBlockData` (PersistentDataContainer) am Block gespeichert, sodass die Aura beim Neuladen eines Chunks automatisch wieder gestartet wird (`ChunkLoadEvent`) – ein Serverneustart oder Entladen des Chunks unterbricht die Partikel also nicht dauerhaft. Verschwindet der Block unerwartet (z. B. durch Fremd-Plugin), erkennt der Task das selbst und beendet sich.

## Wann einsetzen?

- Dekorative Custom Blocks/Furniture, die dauerhaft leuchten, rauchen oder funkeln sollen (Fackeln, Altäre, Portale, Statuen)
- Ambiente-Elemente für Spawn- oder Eventbereiche ohne zusätzliche Redstone-/Scripting-Logik
- Kombination mit `signal`/`remember` an denselben Furniture-Objekten möglich, da unabhängige Mechaniken

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.block_aura.particle` | `String` (Bukkit `Particle`) | `FLAME` | **(Pflicht, aktiviert die Mechanic)** Partikeltyp |
| `Mechanics.block_aura.xOffset` | `String` | `"0.5"` | X-Versatz zum Blockursprung; unterstützt feste Werte oder Zufallsbereiche (`"0.2-0.8"` bzw. `"0.2..0.8"`) |
| `Mechanics.block_aura.yOffset` | `String` | `"0.5"` | Y-Versatz, gleiches Format wie `xOffset` |
| `Mechanics.block_aura.zOffset` | `String` | `"0.5"` | Z-Versatz, gleiches Format wie `xOffset` |
| `Mechanics.block_aura.amount` | `int` | `10` | Anzahl gespawnter Partikel pro Durchlauf |
| `Mechanics.block_aura.deltaX` | `double` | `0.6` | Partikel-Streuung in X-Richtung (Bukkit `spawnParticle`-Delta) |
| `Mechanics.block_aura.deltaY` | `double` | `0.6` | Partikel-Streuung in Y-Richtung |
| `Mechanics.block_aura.deltaZ` | `double` | `0.6` | Partikel-Streuung in Z-Richtung |
| `Mechanics.block_aura.speed` | `double` | `0.05` | Partikelgeschwindigkeit |
| `Mechanics.block_aura.force` | `bool` | `true` | Ob die Partikel auch auf Distanz/für alle Spieler erzwungen sichtbar sind (`force`-Flag von `spawnParticle`) |

## Beispiel

```yaml
mystic_altar:
  material: FURNITURE
  Mechanics:
    block_aura:
      particle: END_ROD
      xOffset: "0.3-0.7"
      yOffset: "0.8"
      zOffset: "0.3-0.7"
      amount: 6
      deltaX: 0.3
      deltaY: 0.3
      deltaZ: 0.3
      speed: 0.02
      force: true
```

## Hinweise & Besonderheiten

- Nicht zu verwechseln mit `aura` (hängt am Spieler/Item) oder `particle_aura` (eigenständige MMO-Mechanic mit Layern) – `block_aura` hängt ausschließlich an einem platzierten Block/Furniture-Standort.
- Gilt gleichzeitig für Custom Blocks und Furniture über denselben Config-Key, es gibt keine getrennte `custom_block.block_aura` bzw. `furniture.block_aura` Schreibweise.
- Das Tick-Intervall wird global über `config.yml` (`aura_mechanic_delay`, Default `10` Ticks für diese Mechanic) gesteuert, nicht pro Item.
- Pro aktivem Block läuft ein eigener asynchroner Task – bei sehr vielen platzierten Objekten mit `block_aura` auf die Serverlast achten.
