# AutoCatch

> Automatisiert das Angeln: Die Angel schlägt bei Biss selbstständig an und wirft sich optional automatisch neu aus.

| | |
|---|---|
| **Config-Key** | `Mechanics.autocatch` |
| **Gilt für** | Item (muss vom Grundmaterial `FISHING_ROD` sein) |
| **Listener-Klasse** | `AutoCatch.AutoCatchListener` |
| **Toggle/Sneak-Verhalten** | Mit `toggable: true` per Linksklick auf einen (nicht interagierbaren) Block an-/ausschaltbar; mit `toggable: false` immer aktiv |

## Was macht sie?

Beim Angeln mit einer Rute, die diese Mechanic besitzt, überwacht die Mechanic das `PlayerFishEvent`. Sobald ein Fisch anbeißt (`BITE`) oder gefangen wird (`CAUGHT_FISH`), wird nach einer kurzen Verzögerung (10 bzw. 20 Ticks) automatisch ein Rechtsklick mit der Rute simuliert – der Spieler holt also nicht mehr manuell ein. Mit `recast: true` wirft die Rute danach normal neu aus (regulärer neuer Wurf). Mit `recast: false` wird die Angel eingeholt und die Rute wirft zwar technisch neu aus, der Angelhaken wird danach aber wieder exakt an die vorherige Position teleportiert – die Rute bleibt also praktisch an derselben Stelle liegen. Ist `toggable: true`, kann der Spieler mit einem Linksklick auf einen Block (der selbst keine Interaktion auslöst) die Funktion pro Item-Stack an- und ausschalten.

## Wann einsetzen?

- AFK-/Idle-Angeln als Premium-Feature für besondere Ruten
- Angel-Werkzeuge, die Spieler bei Bedarf per Klick ein-/ausschalten können, um wahlweise manuell oder automatisch zu fischen
- Kombination mit Loot-Modifikationen anderer Plugins, da reguläre `PlayerFishEvent`-Logik weiterhin greift

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.autocatch.toggable` | `bool` | `false` | Ob der Spieler AutoCatch per Linksklick auf einen Block an-/ausschalten kann. `false` = Mechanic ist immer aktiv, solange die Rute gehalten wird |
| `Mechanics.autocatch.recast` | `bool` | `true` | `true` = nach dem automatischen Einholen wird normal neu ausgeworfen; `false` = die Rute wird eingeholt und der Haken danach wieder an die exakt gleiche Stelle gesetzt |

> Der Abschnitt `Mechanics.autocatch` muss lediglich existieren (auch leer), damit die Mechanic geladen wird – beide Werte haben Defaults.

## Beispiel

```yaml
auto_fishing_rod:
  material: FISHING_ROD
  Mechanics:
    autocatch:
      toggable: true
      recast: true
```

## Hinweise & Besonderheiten

- Funktioniert nur, wenn das Item als Grundmaterial tatsächlich `FISHING_ROD` ist – auch bei Nexo-Custom-Ruten muss das Basismaterial eine Angel sein.
- Der Toggle-Status wird per PersistentDataContainer direkt im Item gespeichert, gilt also pro Item-Stack.
- Die Umschalt-Meldungen (`messages.autocatch.enabled` / `.disabled`) und der Klick-Sound (`ui.button.click`) kommen aus `config.yml`, nicht aus dieser Mechanic-Konfiguration.
- Die Simulation des Rutenwurfs nutzt interne Server-Reflection (NMS); bei Server-/Versionswechseln kann sich das Verhalten in Einzelfällen ändern.
