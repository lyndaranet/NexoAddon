# Decay

> Lässt einen Nexo-Custom-Block nach einer Wartezeit verfallen (verschwinden), wenn er nicht über gleichartige Blöcke mit einem "Basis"-Material verbunden ist.

| | |
|---|---|
| **Config-Key** | `Mechanics.custom_block.decay` |
| **Gilt für** | Custom Block |
| **Listener-Klasse** | `Decay.DecayListener` (löst über `BlockUtil.startDecay` aus) |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv (global per Server-Flag ein-/ausgeschaltet, sobald irgendein Item Decay nutzt) |

## Was macht sie?

Wird irgendwo im Umkreis von 10 Blöcken ein Block platziert oder abgebaut (Vanilla oder Nexo-Custom-Block), scannt die Mechanic alle Custom-Blocks in diesem Bereich. Für jeden gefundenen Custom-Block mit Decay-Konfiguration startet ein Timer, der alle `time` Sekunden prüft, ob der Block über eine Kette gleichartiger Custom-Blocks (innerhalb von `radius` Schritten, per A*-Suche über die 6 Nachbarrichtungen) mit einem Block aus `base` (Vanilla-Material oder Nexo-ID) verbunden ist. Ist er verbunden, stoppt der Timer und der Block bleibt bestehen. Ist er **nicht** verbunden, wird mit Wahrscheinlichkeit `chance` der Block entfernt (`NexoBlocks.remove`); ist er nicht verbunden und der Zufallswurf schlägt fehl, läuft der Timer beim nächsten Intervall erneut.

## Wann einsetzen?

- Baumaterialien, die wie echtes Holz/Gestein eine Verbindung zu einem tragenden Fundament brauchen, sonst verfallen sie (z. B. schwebende Deko-Blöcke)
- Ranken-/Wucherungs-Blöcke, die nur in der Nähe einer Basis (z. B. Erde) bestehen bleiben sollen
- Baufreiheit einschränken, damit Spieler Custom-Blöcke nicht frei im Raum "schweben" lassen können

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.custom_block.decay.base` | `List<String>` | – | **(Pflicht)** Vanilla-Materials oder Nexo-Block-IDs, die als "Basis" gelten, an die der Block angebunden sein muss |
| `Mechanics.custom_block.decay.time` | `int` | `5` | **(Pflicht)** Intervall in Sekunden, nach dem die Verbindungsprüfung erneut läuft |
| `Mechanics.custom_block.decay.chance` | `double` | `0.3` | **(Pflicht)** Wahrscheinlichkeit (0.0–1.0), dass der Block bei fehlender Verbindung tatsächlich entfernt wird |
| `Mechanics.custom_block.decay.radius` | `int` | `5` | **(Pflicht)** Maximale Suchtiefe (Anzahl Schritte) der Verbindungssuche zu einem Basis-Block |

> Alle vier Keys müssen gesetzt sein, damit die Mechanic überhaupt lädt.

## Beispiel

```yaml
schwebende_ranke:
  material: NOTE_BLOCK
  Mechanics:
    custom_block:
      decay:
        base:
          - DIRT
          - GRASS_BLOCK
        time: 5
        chance: 0.3
        radius: 5
```

## Hinweise & Besonderheiten

- Der Scan-Radius beim Auslösen (10 Blöcke um jeden platzierten/abgebauten Block) ist im Code fest verdrahtet und unterscheidet sich von `radius`, welches nur für die Verbindungssuche pro Block gilt.
- Die Mechanic wird global nur aktiv, sobald mindestens ein Item/Block auf dem Server `decay` konfiguriert hat (interner Server-Flag `isDecay`).
- Ausgelöst wird der Scan durch **jeden** Block-Platzieren/-Abbauen in der Nähe, nicht nur durch Interaktion mit dem Decay-Block selbst – das kann bei Baustellen mit viel Aktivität häufige Scans verursachen.
- Die Verbindungssuche erlaubt nur Fortbewegung über Blöcke desselben Custom-Block-Typs wie der Ausgangsblock oder über Basis-Blöcke – gemischte Ketten unterschiedlicher Decay-Blöcke werden nicht verbunden erkannt.
