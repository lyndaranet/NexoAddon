# Particle Aura

> Rendert eine dauerhafte, animierte Partikel-Aura aus einem oder mehreren unabhängigen "Layern" um den Spieler, solange ein Item im konfigurierten Slot getragen wird.

| | |
|---|---|
| **Config-Key** | `Mechanics.particle_aura` |
| **Gilt für** | Item (beliebiger Ausrüstungsslot je nach `slot`) |
| **Listener-Klasse** | `ParticleAuraMechanic.ParticleAuraMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein manuelles Toggle – automatisch aktiv, solange das Item im richtigen Slot sitzt und `conditions` erfüllt sind |

**Abgrenzung:** Es gibt bereits die einfacheren Mechaniken `aura` (`Mechanics.aura` – ein einzelner Partikel plus Nexo-eigener Formel-String) und `block_aura` (`Mechanics.block_aura` – Partikel-Ambiente an platzierten Nexo-Blöcken/Furniture). `particle_aura` ist die eigenständige, deutlich mächtigere Variante für am Spieler getragene Items: mehrere unabhängige, animierte Layer mit eigener Form (`ring`, `helix`, `sphere`, `tornado`, `orbit`, `vortex`, `wings`, `random`), eigenen Bedingungen und eigener Partikel-Konfiguration.

## Was macht sie?

Bei jedem Slot-relevanten Ereignis (Item gewechselt, Hand getauscht, Inventar-Klick, Login) wird geprüft, welches Item mit `particle_aura`-Mechanic aktuell im konfigurierten `slot` liegt. Ist eines aktiv, läuft ein wiederkehrender Task (alle `interval_ticks`), der pro Layer dessen Bedingungen (`only_on_sprint`/`only_on_sneak`) prüft und dann die passende Form um den Spieler rendert. Layer mit `only_on_damage` rendern stattdessen einmalig bei erlittenem Schaden. Ein gemeinsamer Rotations-Phasenwert treibt alle Layer synchron an, jeweils skaliert mit der eigenen `rotation_speed`. Globale `conditions` (`require_sneaking`, `require_sprinting`, `worlds`) pausieren die gesamte Aura, ohne den Task zu beenden.

## Wann einsetzen?

- Kosmetische Trageeffekte (Flügel, Feuer-/Eis-Auren, Wirbel)
- Visuelle Statusanzeigen, die nur beim Sprinten oder Schleichen sichtbar werden
- Boss- oder Event-Item-Flair mit mehreren kombinierten Effektschichten

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.particle_aura.slot` | `String` | `"offhand"` | Slot, in dem das Item aktiv sein muss (wie bei `passive_effect`: `mainhand`, `offhand`, `armor_*`, `any`) |
| `Mechanics.particle_aura.interval_ticks` | `int` | `2` | Render-Intervall in Ticks für alle nicht-schadensgebundenen Layer |
| `Mechanics.particle_aura.conditions.require_sneaking` | `bool` | `false` | Aura pausiert, solange der Spieler nicht schleicht |
| `Mechanics.particle_aura.conditions.require_sprinting` | `bool` | `false` | Aura pausiert, solange der Spieler nicht sprintet |
| `Mechanics.particle_aura.conditions.worlds` | `List<String>` | `[]` (alle Welten) | Aura nur in diesen Welten aktiv |
| `Mechanics.particle_aura.layers` | `List<Map>` | `[]` | **(Pflicht)** Mindestens ein Layer mit gültigem `particle`, sonst lädt die Mechanic nicht. Felder s. Tabelle unten |

**Felder pro `layers`-Eintrag:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `shape` | `String` | `ring` | Form: `ring`, `helix`, `sphere`, `tornado`, `orbit`, `vortex`, `wings`, `random` (unbekannt = keine Darstellung) |
| `particle` | `String` | – | **(Pflicht)** Partikeltyp (Bukkit-Enum-Name oder Minecraft-Wiki-Name) |
| `radius` | `double` | `1.0` | Radius/Ausdehnung je nach Form |
| `count` | `int` | `1` | Basis-Punktzahl bzw. Partikelmenge (wird je nach Form vervielfacht) |
| `rotation_speed` | `double` | `0.1` | Rotationsgeschwindigkeit pro Tick (negativ = Gegenrichtung) |
| `y_offset` | `double` | `0.0` | Vertikaler Versatz zum Spieler |
| `height` | `double` | `2.0` | Höhe (relevant für `helix`/`tornado`/`vortex`/`wings`) |
| `orb_count` | `int` | `3` | Anzahl Orbs (`orbit`) bzw. Spiralarme (`vortex`) |
| `only_on_sprint` | `bool` | `false` | Layer nur sichtbar während Sprinten |
| `only_on_sneak` | `bool` | `false` | Layer nur sichtbar während Schleichen |
| `only_on_damage` | `bool` | `false` | Layer rendert einmalig bei erlittenem Schaden statt im Intervall-Takt |
| `count_sprint_multiplier` | `double` | `1.0` | Multiplikator auf `count` während Sprinten (nur bei Wert > 1.0 wirksam) |
| `dust_color` | `String` | keiner | Farbe für `DUST`/`DUST_TRANSITION`-Partikel (Hex `#RRGGBB` oder Name wie `RED`, `AQUA`); für diese Partikeltypen praktisch Pflicht |
| `dust_color_to` | `String` | keiner | Zielfarbe für `DUST_TRANSITION`-Partikel |
| `dust_size` | `float` | `1.0` | Partikelgröße für `DUST`-Partikel |
| `top_radius` | `double` | `-1` (= `radius`) | Nur `shape: vortex` – Radius am oberen Ende (Trichter-/Konusform) |
| `turns` | `double` | `2.0` | Nur `shape: vortex` – volle Umdrehungen von unten nach oben |
| `clockwise` | `bool` | `false` | Nur `shape: vortex` – Wicklungsrichtung umkehren |
| `scatter` | `double` | `0.0` | Zufälliger Partikel-Offset-Radius pro Punkt |
| `scatter_count` | `int` | `0` (= 1) | Anzahl Partikel pro Punkt-Cluster |

## Beispiel

```yaml
FEUERFACKEL:
  displayname: "<red>Feuerfackel"
  Mechanics:
    particle_aura:
      slot: offhand
      interval_ticks: 2
      layers:
        - shape: helix
          particle: FLAME
          radius: 1.2
          count: 2
          rotation_speed: 0.18
          height: 2.0
        - shape: ring
          particle: ENCHANTMENT_TABLE
          radius: 1.0
          count: 3
          rotation_speed: -0.12
          y_offset: 0.5

KRIEGERSCHULTER:
  displayname: "<gold>Kriegerschulter"
  Mechanics:
    particle_aura:
      slot: armor_chest
      interval_ticks: 2
      conditions:
        require_sprinting: true
      layers:
        - shape: helix
          particle: CRIT
          radius: 1.0
          count: 2
          rotation_speed: 0.2
          height: 2.0
```

## Hinweise & Besonderheiten

- Mindestens ein Layer mit gültigem `particle` ist Pflicht – ist `layers` leer oder haben alle Einträge ein ungültiges/fehlendes `particle`, lädt die gesamte Mechanic nicht.
- `particle` akzeptiert sowohl Bukkit-Enum-Namen (`FLAME`, `END_ROD`, …) als auch Minecraft-Wiki-Namen (`soul_fire_flame`, `dust`, …).
- `only_on_damage`-Layer laufen unabhängig vom `interval_ticks`-Takt und feuern nur bei erlittenem Schaden (`EntityDamageEvent`).
- Das Rendering ist paketintern wiederverwendbar und wird auch von `block_trigger_launch` für dessen Buff-Auren genutzt – Formen und Partikel-Logik sind also mechanikübergreifend konsistent.
