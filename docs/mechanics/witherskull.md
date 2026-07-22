# Wither Skull

> Schießt per Klick einen Witherkopf in Blickrichtung –
> wahlweise als normaler oder aufgeladener (blauer) Schädel.
> Gedacht für Angriffs-Stäbe/-Waffen mit Cooldown.

|                            |                                                                               |
|----------------------------|-------------------------------------------------------------------------------|
| **Config-Key**             | `Mechanics.witherskull`                                                       |
| **Gilt für**               | Item                                                                          |
| **Listener-Klasse**        | `WitherSkullMechanic.WitherSkullMechanicListener`                             |
| **Toggle/Sneak-Verhalten** | Auslöser per `trigger` (`right_click`, `shift_right_click` oder `left_click`) |

## Was macht sie?

Beim konfigurierten Trigger (Standard: Rechtsklick) schießt
die Mechanic einen Vanilla-`WitherSkull` aus den Augen des
Spielers in dessen Blickrichtung. Der Spieler wird als
Schütze gesetzt, der Schädel verhält sich also wie ein vom
Wither abgefeuertes Geschoss (Explosion + Wither-Effekt beim
Einschlag). Über `charged: true` wird der Schädel
aufgeladen (blau, stärkere Explosion). Die
Schuss-Geschwindigkeit steuert `velocity`. Jeder Schuss ist
durch einen per-Spieler-`cooldown` (in Sekunden) begrenzt;
ist er noch aktiv, erscheint eine Actionbar-Meldung mit der
Restzeit statt eines Schusses.

## Wann einsetzen?

- Angriffs-Zauberstäbe/-Waffen, die Witherkopf-Projektile
  verschießen
- Boss- oder Event-Items mit begrenzt einsetzbarer
  Fernkampf-Fähigkeit
- Aufgeladene Variante (`charged: true`) für stärkere,
  block-zerstörende Geschosse

## Konfiguration

| Key                              | Typ              | Default       | Beschreibung                                                   |
|----------------------------------|------------------|---------------|----------------------------------------------------------------|
| `Mechanics.witherskull.trigger`  | `String`         | `right_click` | Auslöser: `right_click`, `shift_right_click` oder `left_click` |
| `Mechanics.witherskull.charged`  | `bool`           | `false`       | `true` = aufgeladener (blauer) Schädel mit stärkerer Explosion |
| `Mechanics.witherskull.cooldown` | `Zeit`           | `0`           | Cooldown pro Spieler; blanke Zahl = Sekunden, oder mit Einheit `10s`, `5min`, `2h`, `500ms` |
| `Mechanics.witherskull.velocity` | `double`         | `1.5`         | Schuss-Geschwindigkeit des Schädels                            |
| `Mechanics.witherskull.sound`    | `String`         | –             | Optionaler Sound beim Abschuss, z. B. `ENTITY_WITHER_SHOOT`    |

> Nur die Keys, die der Loader tatsächlich liest (
`ItemConfigUtil.java`, `loadWitherSkullMechanic`). Ohne
`Mechanics.witherskull`-Abschnitt lädt die Mechanic gar
> nicht.

## Beispiel

```yaml
WITHER_STAB:
  displayname: "<dark_gray>Witherstab"
  Mechanics:
    witherskull:
      trigger: right_click
      charged: false
      cooldown: 12
      velocity: 1.5
      sound: ENTITY_WITHER_SHOOT
```

## Hinweise & Besonderheiten

- Der abgefeuerte `WitherSkull` erzeugt beim Einschlag eine
  echte Explosion und den Wither-Effekt – Griefing-Schutz
  hängt vom Server/Region-Plugin ab, nicht von dieser
  Mechanic.
- Der Cooldown ersetzt den früher üblichen `delay`-Wert. Eine
  blanke Zahl wird als **Sekunden** interpretiert; alternativ
  mit Einheit `500ms`, `10s`, `5min`, `2h`, `1d` – gilt für
  jede Mechanik mit `cooldown`.
- `velocity` skaliert nur die Anfangsgeschwindigkeit; der
  Schädel behält seine Vanilla-Flugbahn/Beschleunigung.
- Bei aktivem Cooldown wird kein Schuss abgegeben,
  stattdessen erscheint eine Actionbar-Meldung mit der
  verbleibenden Restzeit.
