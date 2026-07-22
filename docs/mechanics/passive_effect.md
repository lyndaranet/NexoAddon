# Passive Effect

> Wendet dauerhaft Potion-Effekte und/oder Attribut-Boni an, solange ein Item im konfigurierten Ausrüstungsslot getragen wird.

| | |
|---|---|
| **Config-Key** | `Mechanics.passive_effect` |
| **Gilt für** | Item (beliebiger Ausrüstungsslot je nach `slot`) |
| **Listener-Klasse** | `PassiveEffectMechanic.PassiveEffectMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein manuelles Toggle – automatisch aktiv, solange das Item im richtigen Slot sitzt und `conditions` erfüllt sind |

`passive_effect` gehört zum generischen MMO-Ability-Baukasten, unterscheidet sich aber von den trigger-basierten Abilities (right_click/sneak): Es ist dauerhaft "an", solange das Item getragen wird, und nutzt ein eigenständiges `conditions`-Set (Sneak/Sprint/Health/Biom/Tageszeit) statt des gemeinsamen Ability-Bedingungssystems.

## Was macht sie?

Bei jedem Slot-relevanten Ereignis (Item gewechselt, Hand getauscht, Inventar-Klick, Login) wird eine Tick später geprüft, welches Item aktuell im konfigurierten `slot` liegt. Ist ein passendes Item aktiv, startet ein wiederkehrender Task (alle `reapply_ticks`), der bei erfüllten `conditions` die konfigurierten Potion-Effekte erneuert, Attribut-Modifikatoren transient auf den Spieler anwendet und optional ein Ambient-Partikel spawnt. Sind die Bedingungen zwischenzeitlich nicht erfüllt, werden Effekte/Modifikatoren entfernt, der Task läuft aber weiter und prüft erneut. Verlässt das Item den Slot oder der Spieler disconnected, wird vollständig deaktiviert (inkl. `deactivate_sound`).

## Wann einsetzen?

- Ausrüstungssets mit passiven Boni (Ringe, Amulette, Rüstungsteile)
- Zustandsabhängige Buffs (Notfall-Regeneration bei niedriger HP, Sprint-Boost)
- Biom- oder tageszeitabhängige Ausrüstung (z. B. Nacht-Regeneration)

## Konfiguration

> Kein einzelner Pflicht-Key – schon ein leerer `Mechanics.passive_effect:`-Block aktiviert die Mechanic (dann ohne Effekte, nur ggf. Sounds/Partikel).

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.passive_effect.slot` | `String` | `"mainhand"` | Slot, in dem das Item aktiv sein muss: `mainhand`, `offhand`, `armor_head`, `armor_chest`, `armor_legs`, `armor_feet`, oder `any` (= mainhand ODER offhand) |
| `Mechanics.passive_effect.reapply_ticks` | `int` | `40` | Intervall in Ticks, in dem Effekte erneuert und Bedingungen neu geprüft werden |
| `Mechanics.passive_effect.potion_effects` | `List<Map>` | `[]` | Liste der Potion-Effekte, s. Tabelle unten |
| `Mechanics.passive_effect.attribute_modifiers` | `List<Map>` | `[]` | Liste der Attribut-Modifikatoren, s. Tabelle unten |
| `Mechanics.passive_effect.conditions.on_sneak` | `bool` | `false` | Nur aktiv während der Spieler schleicht |
| `Mechanics.passive_effect.conditions.on_sprint` | `bool` | `false` | Nur aktiv während der Spieler sprintet |
| `Mechanics.passive_effect.conditions.health_below` | `int` | `100` | Nur aktiv, wenn die HP in Prozent ≤ diesem Wert liegt |
| `Mechanics.passive_effect.conditions.biomes` | `List<String>` | `[]` (kein Filter) | Nur aktiv in diesen Biomen (Registry-Key oder Legacy-Enum-Name) |
| `Mechanics.passive_effect.conditions.world_time` | `String` | `"any"` | Nur aktiv bei `day`, `night` oder `any` |
| `Mechanics.passive_effect.ambient_particle` | `String` | keiner | Partikel, das bei jedem Tick am Spieler gespawnt wird |
| `Mechanics.passive_effect.activate_sound` | `String` | keiner | Sound beim Aktivieren |
| `Mechanics.passive_effect.deactivate_sound` | `String` | keiner | Sound beim Deaktivieren |

**Felder pro `potion_effects`-Eintrag:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `type` | `String` | – | **(Pflicht)** PotionEffectType |
| `amplifier` | `int` | `0` | Effektstufe (0 = Stufe I) |

**Felder pro `attribute_modifiers`-Eintrag:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `attribute` | `String` | – | **(Pflicht)** Attribut, z. B. `GENERIC_MAX_HEALTH` (Legacy) oder `max_health` (Registry-Key) |
| `operation` | `String` | `ADD_NUMBER` | `AttributeModifier.Operation` (`ADD_NUMBER`, `ADD_SCALAR`, `MULTIPLY_SCALAR_1`) |
| `amount` | `double` | `0.0` | Wert des Modifikators |

## Beispiel

```yaml
GUARDIAN_CHESTPLATE:
  displayname: "<aqua>Guardian Chestplate"
  Mechanics:
    passive_effect:
      slot: armor_chest
      reapply_ticks: 60
      attribute_modifiers:
        - attribute: GENERIC_MAX_HEALTH
          operation: ADD_NUMBER
          amount: 8.0

CRISIS_AMULET:
  displayname: "<dark_red>Crisis Amulet"
  Mechanics:
    passive_effect:
      slot: offhand
      reapply_ticks: 40
      potion_effects:
        - type: REGENERATION
          amplifier: 2
      conditions:
        health_below: 30
        world_time: night
      activate_sound: ENTITY_EXPERIENCE_ORB_PICKUP
```

## Hinweise & Besonderheiten

- Attribut-Modifikatoren sind transient (`addTransientModifier`) und werden nie in die Spielerdatei geschrieben – sie verschwinden automatisch beim Logout/Neustart, auch ohne sauberes Entfernen.
- Die Bedingungsprüfung läuft im `reapply_ticks`-Takt, nicht in Echtzeit – bei z. B. 40 Ticks (2 s) reagiert die Mechanic entsprechend verzögert auf Sneak/Sprint/HP-Änderungen.
- `slot: any` deckt nur `mainhand`/`offhand` ab, keine Rüstungsslots.
- Wechselt ein Spieler auf ein anderes `passive_effect`-Item im selben Slot, wird das alte sauber deaktiviert, bevor das neue aktiviert wird.
