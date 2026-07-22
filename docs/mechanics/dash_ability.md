# Dash Ability

> Beschleunigt den Spieler in eine konfigurierbare Richtung mit Dash-/Gleit-/Flug- oder Blink-Modus, Phasen-Effekten, Invincibility-Fenster und optionalem Einschlag.

| | |
|---|---|
| **Config-Key** | `Mechanics.dash.mode` (Pflicht-Key, unterscheidet diese Mechanic von der einfachen `Dash`-Mechanic) |
| **Gilt für** | Item |
| **Listener-Klasse** | `DashMechanic.DashMechanicListener` |
| **Toggle/Sneak-Verhalten** | Auslösung per `trigger` (Rechts-/Shift-Rechtsklick oder `double_jump`); während des Dashs ignoriert die Mechanic weitere Auslöser |

## Was macht sie?

Beim Auslösen wird der Spieler in eine Richtung (`direction`: Blick, horizontal, vorwärts, aufwärts) beschleunigt, gesteuert über `mode`: `dash` (Geschwindigkeits-Impuls + verzögerter Einschlag als Landungs-Näherung), `glide` (verleiht für `duration` Ticks Flug), `flight` (Impuls + Flug für `duration` Ticks), `blink_dash` (teleportiert schrittweise jeden Tick, durchquert dabei nicht-solide Blöcke und bricht sofort ab, sobald der nächste Schritt in einem soliden Block läge). Während der aktiven Phase können `phase_effects` (Dauer in **Ticks**, nicht Sekunden), eine Invincibility-Zeit, Leuchten und eine Partikelspur (`phase_particles`) laufen. Beim Aktivieren wirken zusätzlich `activate_effects`/`activate_self_damage` sowie Partikel/Sound; endet die Phase, wird bei gesetztem `impact_radius` ein Flächenschaden mit Effekten/Partikeln/Sound ausgelöst und der ursprüngliche Flugstatus exakt wiederhergestellt. Statt eines einfachen Cooldowns kann `charges` ein unabhängiges Ladungssystem mit eigenem Aufladeintervall (`charge_recharge_seconds`) aktivieren.

## Wann einsetzen?

- Mobilitäts-Skills für Nahkämpfer (Sturmklingen-Dash mit Einschlagschaden am Ziel)
- Doppelsprung-Gleitfähigkeiten mit mehreren Ladungen (Adlerflügel)
- Kurzzeit-Unverwundbarkeits-Dashs, die durch dünne Wände/Gitter blinzeln (Geisterschritt)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.dash.trigger` | `String` | `right_click` | `right_click`, `shift_right_click` oder `double_jump` (fängt den vanilla Doppelsprung-Flugtoggle ab) |
| `Mechanics.dash.mode` | `String` | `flight` | **(Pflicht, aktiviert diese Mechanic)** `dash`, `glide`, `flight`, `blink_dash` |
| `Mechanics.dash.direction` | `String` | `look` | `look`, `horizontal`, `forward`, `up` |
| `Mechanics.dash.cooldown` | `int` (Sekunden) | `0` | Nur relevant, wenn `charges <= 1` |
| `Mechanics.dash.duration` | `int` (Ticks) | `40` | Dauer der Dash-/Gleit-/Flugphase |
| `Mechanics.dash.speed` | `double` | `2.5` | Geschwindigkeit des Impulses bzw. Blink-Distanz |
| `Mechanics.dash.charges` / `charge_recharge_seconds` | `int` | `1` / `8` | Ladungssystem statt einfachem Cooldown, wenn `charges > 1` |
| `Mechanics.dash.phase_effects` | `List<PhaseEffect>` | `[]` | Effekte während der Phase: `type`, `amplifier`, `duration_ticks` (**Ticks**, nicht Sekunden!) |
| `Mechanics.dash.phase_invincibility_ticks` | `int` | `0` | Unverwundbarkeitsfenster (`noDamageTicks`) beim Aktivieren |
| `Mechanics.dash.glow_during_dash` | `bool` | `false` | Spieler leuchtet während der Phase |
| `Mechanics.dash.phase_particles` | `List<TrailEntry>` | `[]` | Partikelspur während der gesamten Phase |
| `Mechanics.dash.trail_interval_ticks` | `int` | `1` | Intervall der Partikelspur |
| `Mechanics.dash.activate_effects` / `activate_self_damage` | – | `[]`/`0.0` | Wirkung auf den Caster beim Aktivieren |
| `Mechanics.dash.impact_radius` / `impact_damage` | `double` | `0.0` | Flächenschaden, wenn die Phase endet |
| `Mechanics.dash.impact_delay_ticks` | `int` | `10` | Nur `mode: dash`: Verzögerung bis zum Landungs-Einschlag |
| `Mechanics.dash.impact_effects` / `impact_particles` / `impact_sound` | – | `[]`/`[]`/– | Wirkung/Feedback beim Einschlag |
| `Mechanics.dash.conditions.require_sneaking` / `require_sprinting` / `require_on_ground` / `require_in_air` | `bool` | `false` | Nutzungsbedingungen zur Bewegung |
| `Mechanics.dash.conditions.require_health_below` / `require_permission` | `int`/`String` | `100`/`""` | Weitere Nutzungsbedingungen |
| `Mechanics.dash.activate_particles` / `activate_sound` | – | `[]`/– | Feedback beim Aktivieren |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadDashAbilityMechanic`). Der Abschnitt wird nur geladen, wenn `Mechanics.dash.mode` gesetzt ist.

## Beispiel

```yaml
STURMKLINGEN:
  displayname: "<aqua>Sturmklingen"
  Mechanics:
    dash:
      trigger: right_click
      mode: dash
      direction: horizontal
      charges: 2
      charge_recharge_seconds: 6
      speed: 3.0
      impact_radius: 3.0
      impact_damage: 3.0
      impact_delay_ticks: 8
      impact_effects:
        - type: SLOW
          amplifier: 1
          duration: 2
      impact_particles:
        - particle: CLOUD
          count: 20
      impact_sound: ENTITY_PLAYER_ATTACK_SWEEP
      conditions:
        require_on_ground: true
      phase_particles:
        - particle: CRIT
          count: 4
      activate_sound: ENTITY_PLAYER_ATTACK_STRONG
```

## Hinweise & Besonderheiten

- **Wichtig:** `Mechanics.dash` kann zwei sich gegenseitig ausschließende Mechaniken enthalten – diese hier (MMO-Fähigkeit mit Modi/Charges, erkennbar am Pflicht-Key `mode`) oder die einfache Dash-Mechanic mit dem Pflicht-Key `power` (siehe `dash.md`). Der Loader entscheidet anhand des vorhandenen Keys, welche Mechanic geladen wird; **beide können nicht gleichzeitig in derselben Item-Config aktiv sein**.
- `phase_effects` verwenden `duration_ticks` (Ticks), während die meisten anderen Effekt-Listen im Plugin `duration` in Sekunden nutzen – Verwechslungsgefahr.
- `double_jump` fängt das vanilla Doppelsprung-Flugtoggle ab; funktioniert nur in Survival/Adventure, Creative/Spectator bleiben unberührt.
- `blink_dash` bricht sofort ab (und löst dabei den Einschlag aus), sobald das nächste Schrittsegment in einem soliden Block läge.
- Beim Ende der Phase wird der ursprüngliche Flugstatus (erlaubtes Fliegen / aktives Fliegen) exakt wiederhergestellt.
- `charges > 1` ersetzt das klassische `cooldown`-Feld vollständig durch ein unabhängiges Ladungssystem.
