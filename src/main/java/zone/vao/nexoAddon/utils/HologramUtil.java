package zone.vao.nexoAddon.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import zone.vao.nexoAddon.NexoAddon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramUtil {

  private static final Map<UUID, ArmorStand> holograms = new HashMap<>();

  public static void displayProgressBar(Entity entity, double progress, Player player) {
    if (entity == null || progress < 0.0 || progress > 1.0) return;

    NexoAddon.getInstance().foliaLib.getScheduler().runAsync(startDisplay -> {
      World world = entity.getWorld();
      Location entityLocation = entity.getLocation().clone();
      Location hologramLocation = entityLocation.add(0, 0.5, 0);

      Component progressBar = getProgressBar(progress, 10);

      NexoAddon.getInstance().foliaLib.getScheduler().runNextTick(nextTick -> {
        if (player != null && holograms.containsKey(player.getUniqueId())) {
          ArmorStand existingHologram = holograms.get(player.getUniqueId());
          existingHologram.remove();
          holograms.remove(player.getUniqueId());
        }

        ArmorStand hologram = world.spawn(hologramLocation, ArmorStand.class, stand -> {
          stand.customName(progressBar);
          stand.setCustomNameVisible(true);
          stand.setGravity(false);
          stand.setInvisible(true);
          stand.setMarker(true);
          stand.setSmall(true);
          if (player != null) {
            stand.setVisibleByDefault(false);
            player.showEntity(NexoAddon.getInstance(), stand);
          }
        });

        if (player != null)
          holograms.put(player.getUniqueId(), hologram);

        NexoAddon.getInstance().foliaLib.getScheduler().runLater(() -> {
          hologram.remove();
          if (player != null)
            holograms.remove(player.getUniqueId());
        }, 60);
      });
    });
  }

  private static Component getProgressBar(double progress, int length) {
    int filledLength = (int) (progress * length);
    int emptyLength = length - filledLength;

    Component filledPart = Component.text("█".repeat(filledLength)).color(NamedTextColor.DARK_GREEN);
    Component emptyPart = Component.text("█".repeat(emptyLength)).color(NamedTextColor.RED);

    return filledPart.append(emptyPart);
  }

}
