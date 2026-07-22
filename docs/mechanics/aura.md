# Aura

> Lässt ein Item (Werkzeug oder Rüstungsteil) permanent Partikel um den Spieler herum erzeugen, solange er es hält oder trägt.

| | |
|---|---|
| **Config-Key** | `Mechanics.aura` |
| **Gilt für** | Item (Werkzeug in der Hand oder Rüstungsteil) |
| **Listener-Klasse** | Kein Event-Listener – wird von `ParticleEffectManager` als wiederkehrender Async-Task verarbeitet |
| **Toggle/Sneak-Verhalten** | Keins – passiv aktiv, solange das Item gehalten/getragen wird |

## Was macht sie?

Ein wiederkehrender Task prüft für jeden Online-Spieler das Item in der Haupthand sowie alle vier Rüstungsteile. Trägt/hält der Spieler ein Item mit `Mechanics.aura`, wird je nach `type` eines von vier vordefinierten Partikelmustern um den Spieler gespawnt: `simple` (einfache Partikelwolke am Standort), `ring` (Ring um den Spieler), `helix` (Doppelhelix-Spirale nach oben) oder `heart` (Herzform vor dem Spieler, an Blickrichtung ausgerichtet). Bei `type: custom` wird stattdessen die Formel aus `custom` ausgewertet, die für x/y/z jeweils einen mathematischen Ausdruck (inkl. Variablen `x`, `y`, `z`, `yaw`, `pitch`, `angle`, `angle2`, `Math_PI`) enthalten muss. Ein Spieler kann mehrere Auren gleichzeitig haben (Werkzeug + mehrere Rüstungsteile), diese werden alle unabhängig voneinander gerendert.

## Wann einsetzen?

- Premium-/Event-Werkzeuge oder -Rüstungen, die durch permanente Partikel optisch aufgewertet werden
- Ränge, Booster-Items oder Kill-Rewards mit sichtbarem "Ich trage etwas Besonderes"-Effekt
- Individuelle Partikelformen für spezielle Items über die `custom`-Formel (z. B. eigene geometrische Muster)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.aura.particle` | `String` (Bukkit `Particle`) | – | **(Pflicht)** Partikeltyp, z. B. `FLAME`, `HEART`, `END_ROD` |
| `Mechanics.aura.type` | `String` | – | **(Pflicht)** Eines von `simple`, `ring`, `helix`, `heart`, `custom` |
| `Mechanics.aura.custom` | `String` | `null` | Nur bei `type: custom` nötig: drei durch Komma getrennte Formeln für x, y, z |

> Sowohl `particle` als auch `type` müssen gesetzt sein, sonst wird die Mechanic für dieses Item gar nicht geladen.

## Beispiel

```yaml
ring_of_flames:
  material: DIAMOND_HELMET
  Mechanics:
    aura:
      particle: FLAME
      type: ring
```

```yaml
custom_aura_item:
  material: STICK
  Mechanics:
    aura:
      particle: END_ROD
      type: custom
      custom: "sin(angle)*2,cos(angle2),cos(angle)*2"
```

## Hinweise & Besonderheiten

- Abgrenzung zu ähnlich benannten Mechaniken: **`aura`** hängt am Spieler (Item in Hand/Rüstung), **`block_aura`** hängt an einem platzierten Custom Block/Furniture, **`particle_aura`** ist eine eigenständige, deutlich komplexere MMO-Mechanic mit mehreren Partikel-"Layern" (siehe `ParticleAuraMechanic`) – alle drei sind unabhängig voneinander und nicht austauschbar.
- Das Wiederholungsintervall (Tick-Rate) wird nicht pro Item, sondern global über `config.yml` (`aura_mechanic_delay`, Default `5` Ticks) gesteuert und läuft asynchron für alle Spieler gemeinsam.
- Ist die `custom`-Formel fehlerhaft (nicht genau drei durch Komma getrennte Teile oder ungültige Syntax), wird der komplette Aura-Task serverweit gestoppt und muss per `/nexoaddon reload` neu gestartet werden – bei Nutzung von `custom` sorgfältig testen.
- Die Partikel werden rein clientseitig sichtbar per `spawnParticle` erzeugt, es gibt keine Kollision oder Gameplay-Wirkung, nur visuelles Feedback.
