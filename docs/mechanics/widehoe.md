# WideHoe

> Pflügt beim Rechtsklick mit einer Hacke nicht nur einen Block, sondern ein ganzes quadratisches Areal auf einmal um.

| | |
|---|---|
| **Config-Key** | `Mechanics.widehoe` |
| **Gilt für** | Item (Werkzeug, i. d. R. Hacke) |
| **Listener-Klasse** | `WideHoe.WideHoeListener` |
| **Toggle/Sneak-Verhalten** | Optional per Rechtsklick (Luft oder nicht-pflügbarer Block) an-/ausschaltbar, wenn `switchable: true` |

## Was macht sie?

Klickt der Spieler mit einem WideHoe-Werkzeug einen pflügbaren Block (Dirt-Varianten, optional auch Grasblock/Myzel/Podzol) an, bricht die Mechanic das Vanilla-Interact-Event ab und pflügt stattdessen im nächsten Tick alle pflügbaren Blöcke in einem zentrierten `radius x radius`-Quadrat um den angeklickten Block zu Ackerland um. Blöcke mit einem soliden Block direkt darüber werden übersprungen, Schutz-Plugins werden pro Block respektiert. Ist `durability_cost` gesetzt, wird für jeden erfolgreich gepflügten Block (inkl. des ursprünglich angeklickten) Haltbarkeit vom Werkzeug abgezogen; bricht das Werkzeug dabei, stoppt der Vorgang sofort.

## Wann einsetzen?

- Landwirtschafts-Werkzeuge im Shop, die große Felder in einem Klick vorbereiten
- Ressourcen-Hacken mit skalierender Haltbarkeitskosten (`durability_cost`) je nach Areal-Größe
- Kombination mit Toggle (`switchable: true`), damit Spieler bei Bedarf auch einzelne Blöcke normal pflügen können

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.widehoe` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.widehoe.radius` | `int` | `3` | Kantenlänge des quadratischen Pflüg-Areals um den angeklickten Block. Gerade Werte werden automatisch um 1 erhöht, damit das Quadrat zentriert werden kann (z. B. `4` → `5`) |
| `Mechanics.widehoe.switchable` | `bool` | `false` | Ob der Spieler die Wide-Pflüg-Funktion per Rechtsklick (Luft/nicht-pflügbarer Block) an-/ausschalten kann |
| `Mechanics.widehoe.till_grass` | `bool` | `true` | Ob zusätzlich zu Dirt-Varianten auch Grasblock, Myzel und Podzol gepflügt werden |
| `Mechanics.widehoe.durability_cost` | `int` | `1` | Haltbarkeitskosten pro erfolgreich gepflügtem Block (inkl. des angeklickten Blocks). `0` = keine Haltbarkeitskosten |

## Beispiel

```yaml
diamond_hoe_3x3:
  material: DIAMOND_HOE
  Mechanics:
    widehoe:
      radius: 3
      switchable: true
      till_grass: true
      durability_cost: 1
```

## Hinweise & Besonderheiten

- Der Toggle-Status wird direkt im Item (PersistentDataContainer) gespeichert, gilt also pro Item-Stack, nicht global pro Spieler. Ohne vorherige Interaktion gilt die Mechanic als eingeschaltet.
- Die Vanilla-Pflüg-Aktion wird für den angeklickten Block vollständig unterdrückt (`event.setCancelled(true)`); die gesamte Haltbarkeitskostenberechnung – auch für den ursprünglich angeklickten Block – läuft ausschließlich über `durability_cost` dieser Mechanic, nicht über die Vanilla-Logik.
- Blöcke, über denen ein solider, nicht-luftiger Block liegt, werden übersprungen (verhindert das Pflügen "unter" Gebäuden).
- Respektiert Schutz-Plugins über `ProtectionLib` pro Einzelblock: geschützte Blöcke im Areal werden übersprungen, der Rest des Areals wird trotzdem bearbeitet.
- Beim Umschalten wird eine Actionbar-Nachricht gesendet, deren Texte in `config.yml` unter `messages.widehoe.enabled` / `messages.widehoe.disabled` konfiguriert werden – nicht in der Mechanic-Konfiguration selbst.
