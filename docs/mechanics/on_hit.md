# On Hit

> Verpasst dem getroffenen Ziel bei einem Nahkampftreffer automatisch konfigurierte Statuseffekte.

| | |
|---|---|
| **Config-Key** | `Mechanics.on_hit` |
| **Gilt für** | Item (Waffe, Hauptvhand) |
| **Listener-Klasse** | `OnHitMechanic.OnHitMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – wirkt bei jedem Treffer automatisch, solange kein Cooldown aktiv ist |

`on_hit` ist Teil des generischen MMO-Ability-Baukastens (Cooldown + Effects als wiederverwendbare Bausteine), reagiert hier aber direkt und ohne eigenen Trigger-Typ auf jeden Nahkampftreffer – anders als z. B. `consumable`, das explizite Trigger wie `right_click` kennt.

## Was macht sie?

Schlägt ein Spieler mit einem `on_hit`-Item einen lebenden Entity, erhält das Ziel die konfigurierten Potion-Effekte, sofern `ProtectionLib` das Interagieren an der Zielposition erlaubt und kein Cooldown aktiv ist. Optional wird zusätzlich ein Partikeleffekt am Ziel abgespielt. Ein Cooldown pro Spieler (nicht pro Waffe) verhindert Effekt-Spam bei schnellen Angriffsserien – während er aktiv ist, wird weiterhin normaler Schaden verursacht, nur ohne Zusatzeffekte.

## Wann einsetzen?

- Gift-/Elementar-Waffen mit Debuff auf den Gegner
- PvP-Waffen mit kurzzeitigem Slow/Weakness-Effekt
- Boss- oder Event-Loot mit charakteristischem Statuseffekt

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.on_hit.effects` | `List<Map>` | `[]` | **(Pflicht)** Liste der Potion-Effekte; ohne mindestens einen gültigen Eintrag lädt die Mechanic nicht |
| `Mechanics.on_hit.cooldown` | `int` | `0` | Cooldown in Sekunden pro Spieler zwischen zwei Effekt-Anwendungen (0 = kein Cooldown) |
| `Mechanics.on_hit.particles` | `String` | keiner | Optionaler Partikel-Effekt am Ziel bei Treffer |

**Felder pro `effects`-Eintrag:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `type` | `String` | – | **(Pflicht)** PotionEffectType (Legacy-Name oder Registry-Key) |
| `amplifier` | `int` | `0` | Effektstufe (0 = Stufe I) |
| `duration` | `int` | `1` | Dauer in Sekunden |

## Beispiel

```yaml
VENOMOUS_DAGGER:
  displayname: "<red>Venomous Dagger"
  Mechanics:
    on_hit:
      cooldown: 8
      particles: VILLAGER_ANGRY
      effects:
        - type: POISON
          amplifier: 1
          duration: 3
        - type: SLOWNESS
          amplifier: 0
          duration: 2
```

## Hinweise & Besonderheiten

- Der Cooldown ist rein pro Spieler und gilt global über alle `on_hit`-Waffen hinweg, die dieser Spieler führt – nicht pro Waffe oder Item-Stack.
- Während der Cooldown aktiv ist, wird weiterhin normaler Schaden zugefügt; nur die Zusatzeffekte entfallen.
- `ProtectionLib.canInteract` wird geprüft: In geschützten Bereichen (WorldGuard/GriefPrevention/PlotSquared) werden keine Effekte angewendet, der Schaden bleibt jedoch bestehen.
- Reagiert ausschließlich auf `EntityDamageByEntityEvent` durch direkten Spieler-Nahkampf – Projektil-Mechaniken wie `bow`/`projectile` lösen `on_hit` nicht aus.
