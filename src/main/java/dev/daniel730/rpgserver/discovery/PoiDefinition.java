package dev.daniel730.rpgserver.discovery;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class PoiDefinition {

    private final String id;
    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final double radius;

    public PoiDefinition(String id, String name, String worldName, double x, double y, double z, double radius) {
        this.id = id;
        this.name = name == null || name.isBlank() ? id : name;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius <= 0 ? 8 : radius;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return radius;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        double dx = location.getX() - x;
        double dy = location.getY() - y;
        double dz = location.getZ() - z;
        return (dx * dx + dy * dy + dz * dz) <= radius * radius;
    }
}
