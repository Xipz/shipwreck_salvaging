package com.shipwrecksalvaging;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class ShipwreckSalvagingOverlay extends Overlay
{
    private static final int SALVAGE_RANGE = 7;
    private static final double SALVAGE_RANGE_NORTH_EXTRA = 0.5; // Extra half tile on north
    private static final double SALVAGE_RANGE_EAST_EXTRA = 0.5; // Extra half tile on east
    private static final int SHIPWRECK_SIZE = 2;
    private static final int DOT_SIZE = 8; // Size of the dot in pixels

    private final Client client;
    private final ShipwreckSalvagingPlugin plugin;
    private final ShipwreckSalvagingConfig config;

    @Inject
    private ShipwreckSalvagingOverlay(Client client, ShipwreckSalvagingPlugin plugin, ShipwreckSalvagingConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Force re-check for NPCs periodically to prevent disappearing markers
        if (plugin.getTrackedNpcs().isEmpty() && client.getGameState() == net.runelite.api.GameState.LOGGED_IN)
        {
            for (NPC npc : client.getNpcs())
            {
                if (npc.getId() == 15186 || npc.getId() == 15187 || npc.getId() == 15188 ||
                        npc.getId() == 15189 || npc.getId() == 15190)
                {
                    plugin.getTrackedNpcs().add(npc);
                }
            }
        }

        // First, calculate which tiles are in overlap zones
        java.util.Map<WorldPoint, Integer> tileOverlapCount = new java.util.HashMap<>();

        for (GameObject shipwreck : plugin.getActiveShipwrecks())
        {
            if (!plugin.isShipwreckEnabled(shipwreck))
            {
                continue;
            }

            if (plugin.isShipwreckDepleted(shipwreck))
            {
                continue;
            }

            if (!config.showSalvageRange())
            {
                continue;
            }

            WorldPoint shipwreckLocation = shipwreck.getWorldLocation();
            int minX = shipwreckLocation.getX() - SALVAGE_RANGE;
            int maxX = shipwreckLocation.getX() + SHIPWRECK_SIZE - 1 + SALVAGE_RANGE + 1; // +1 for the extra half tile on east
            int minY = shipwreckLocation.getY() - SALVAGE_RANGE;
            int maxY = shipwreckLocation.getY() + SHIPWRECK_SIZE - 1 + SALVAGE_RANGE + 1; // +1 for the extra half tile on north
            int plane = shipwreckLocation.getPlane();

            for (int x = minX; x <= maxX; x++)
            {
                for (int y = minY; y <= maxY; y++)
                {
                    WorldPoint tilePoint = new WorldPoint(x, y, plane);
                    tileOverlapCount.put(tilePoint, tileOverlapCount.getOrDefault(tilePoint, 0) + 1);
                }
            }
        }

        // Render the shipwrecks
        for (GameObject shipwreck : plugin.getActiveShipwrecks())
        {
            if (!plugin.isShipwreckEnabled(shipwreck))
            {
                continue;
            }

            WorldPoint shipwreckLocation = shipwreck.getWorldLocation();
            boolean isDepleted = plugin.isShipwreckDepleted(shipwreck);

            if (isDepleted && config.highlightShipwreck())
            {
                renderShipwreckHighlight(graphics, shipwreck);
            }

            if (!isDepleted && config.showSalvageRange())
            {
                renderSalvageRange(graphics, shipwreckLocation, tileOverlapCount);
            }
        }

        // Render NPC tile markers
        for (NPC npc : plugin.getTrackedNpcs())
        {
            if (config.showNpcMarker())
            {
                renderNpcTileMarker(graphics, npc);
            }
        }

        return null;
    }

    private void renderShipwreckHighlight(Graphics2D graphics, GameObject shipwreck)
    {
        Color color = config.shipwreckColor();
        LocalPoint localPoint = shipwreck.getLocalLocation();
        if (localPoint != null)
        {
            Polygon polygon = Perspective.getCanvasTileAreaPoly(client, localPoint, SHIPWRECK_SIZE);
            if (polygon != null)
            {
                graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
                graphics.fillPolygon(polygon);
                graphics.setColor(color);
                graphics.setStroke(new BasicStroke(2));
                graphics.drawPolygon(polygon);
            }
        }
    }

    private void renderSalvageRange(Graphics2D graphics, WorldPoint shipwreckLocation, java.util.Map<WorldPoint, Integer> tileOverlapCount)
    {
        // Calculate the range boundaries with extra half tile on north and east
        int minX = shipwreckLocation.getX() - SALVAGE_RANGE;
        int maxX = shipwreckLocation.getX() + SHIPWRECK_SIZE - 1 + SALVAGE_RANGE + 1; // +1 for the extra half tile on east
        int minY = shipwreckLocation.getY() - SALVAGE_RANGE;
        int maxY = shipwreckLocation.getY() + SHIPWRECK_SIZE - 1 + SALVAGE_RANGE + 1; // +1 for the extra half tile on north

        int plane = shipwreckLocation.getPlane();

        Color fillColor = new Color(
                config.tileFillColor().getRed(),
                config.tileFillColor().getGreen(),
                config.tileFillColor().getBlue(),
                config.fillOpacity()
        );
        Color borderColor = config.tileBorderColor();
        int borderWidth = config.tileBorderWidth();

        Color overlapFillColor = new Color(
                config.overlapFillColor().getRed(),
                config.overlapFillColor().getGreen(),
                config.overlapFillColor().getBlue(),
                config.overlapFillColor().getAlpha()
        );
        Color overlapBorderColor = config.overlapBorderColor();

        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                WorldPoint tilePoint = new WorldPoint(x, y, plane);

                boolean isOverlap = config.showOverlap() && tileOverlapCount.getOrDefault(tilePoint, 0) > 1;

                boolean isWestEdge = (x == minX);
                boolean isEastEdge = (x == maxX);
                boolean isSouthEdge = (y == minY);
                boolean isNorthEdge = (y == maxY);

                Color tileFill = isOverlap ? overlapFillColor : fillColor;
                Color tileBorder = isOverlap ? overlapBorderColor : borderColor;

                renderTileWithSelectiveBorder(graphics, tilePoint, tileFill, tileBorder, borderWidth,
                        isWestEdge, isEastEdge, isSouthEdge, isNorthEdge);
            }
        }
    }

    private void renderTileWithSelectiveBorder(Graphics2D graphics, WorldPoint worldPoint,
                                               Color fillColor, Color borderColor, int borderWidth,
                                               boolean drawWest, boolean drawEast, boolean drawSouth, boolean drawNorth)
    {
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
        if (localPoint == null)
        {
            return;
        }

        Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
        if (tilePoly == null)
        {
            return;
        }

        graphics.setColor(fillColor);
        graphics.fillPolygon(tilePoly);

        graphics.setColor(borderColor);
        graphics.setStroke(new BasicStroke(borderWidth));

        if (drawSouth)
        {
            graphics.drawLine(tilePoly.xpoints[0], tilePoly.ypoints[0],
                    tilePoly.xpoints[1], tilePoly.ypoints[1]);
        }
        if (drawEast)
        {
            graphics.drawLine(tilePoly.xpoints[1], tilePoly.ypoints[1],
                    tilePoly.xpoints[2], tilePoly.ypoints[2]);
        }
        if (drawNorth)
        {
            graphics.drawLine(tilePoly.xpoints[2], tilePoly.ypoints[2],
                    tilePoly.xpoints[3], tilePoly.ypoints[3]);
        }
        if (drawWest)
        {
            graphics.drawLine(tilePoly.xpoints[3], tilePoly.ypoints[3],
                    tilePoly.xpoints[0], tilePoly.ypoints[0]);
        }
    }

    private void renderHalfTileWithSelectiveBorder(Graphics2D graphics, WorldPoint worldPoint,
                                                   Color fillColor, Color borderColor, int borderWidth,
                                                   boolean drawWest, boolean drawEast, boolean drawSouth, boolean drawNorth,
                                                   boolean isEastHalf, boolean isNorthHalf)
    {
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
        if (localPoint == null)
        {
            return;
        }

        Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
        if (tilePoly == null)
        {
            return;
        }

        // Create a polygon for half the tile
        Polygon halfPoly = new Polygon();

        if (isEastHalf && isNorthHalf)
        {
            // Northeast corner - show only northeast quarter
            int midX = (tilePoly.xpoints[1] + tilePoly.xpoints[3]) / 2;
            int midY = (tilePoly.ypoints[1] + tilePoly.ypoints[3]) / 2;
            halfPoly.addPoint(midX, midY); // Center
            halfPoly.addPoint(tilePoly.xpoints[2], tilePoly.ypoints[2]); // NE corner
            halfPoly.addPoint((tilePoly.xpoints[2] + tilePoly.xpoints[3]) / 2, (tilePoly.ypoints[2] + tilePoly.ypoints[3]) / 2); // North mid
        }
        else if (isEastHalf)
        {
            // East half-tile
            halfPoly.addPoint((tilePoly.xpoints[0] + tilePoly.xpoints[1]) / 2, (tilePoly.ypoints[0] + tilePoly.ypoints[1]) / 2); // South mid
            halfPoly.addPoint(tilePoly.xpoints[1], tilePoly.ypoints[1]); // SE
            halfPoly.addPoint(tilePoly.xpoints[2], tilePoly.ypoints[2]); // NE
            halfPoly.addPoint((tilePoly.xpoints[2] + tilePoly.xpoints[3]) / 2, (tilePoly.ypoints[2] + tilePoly.ypoints[3]) / 2); // North mid
        }
        else if (isNorthHalf)
        {
            // North half-tile
            halfPoly.addPoint((tilePoly.xpoints[0] + tilePoly.xpoints[3]) / 2, (tilePoly.ypoints[0] + tilePoly.ypoints[3]) / 2); // West mid
            halfPoly.addPoint((tilePoly.xpoints[1] + tilePoly.xpoints[2]) / 2, (tilePoly.ypoints[1] + tilePoly.ypoints[2]) / 2); // East mid
            halfPoly.addPoint(tilePoly.xpoints[2], tilePoly.ypoints[2]); // NE
            halfPoly.addPoint(tilePoly.xpoints[3], tilePoly.ypoints[3]); // NW
        }

        // Draw fill
        graphics.setColor(fillColor);
        graphics.fillPolygon(halfPoly);

        // Draw borders
        graphics.setColor(borderColor);
        graphics.setStroke(new BasicStroke(borderWidth));

        if (isEastHalf && isNorthHalf)
        {
            // Northeast corner borders
            if (drawNorth)
            {
                graphics.drawLine(halfPoly.xpoints[1], halfPoly.ypoints[1], halfPoly.xpoints[2], halfPoly.ypoints[2]);
            }
            if (drawEast)
            {
                graphics.drawLine(halfPoly.xpoints[2], halfPoly.ypoints[2], halfPoly.xpoints[0], halfPoly.ypoints[0]);
            }
        }
        else if (isEastHalf)
        {
            if (drawSouth)
            {
                graphics.drawLine(halfPoly.xpoints[0], halfPoly.ypoints[0], halfPoly.xpoints[1], halfPoly.ypoints[1]);
            }
            if (drawEast)
            {
                graphics.drawLine(halfPoly.xpoints[1], halfPoly.ypoints[1], halfPoly.xpoints[2], halfPoly.ypoints[2]);
            }
            if (drawNorth)
            {
                graphics.drawLine(halfPoly.xpoints[2], halfPoly.ypoints[2], halfPoly.xpoints[3], halfPoly.ypoints[3]);
            }
        }
        else if (isNorthHalf)
        {
            if (drawWest)
            {
                graphics.drawLine(halfPoly.xpoints[3], halfPoly.ypoints[3], halfPoly.xpoints[0], halfPoly.ypoints[0]);
            }
            if (drawNorth)
            {
                graphics.drawLine(halfPoly.xpoints[2], halfPoly.ypoints[2], halfPoly.xpoints[3], halfPoly.ypoints[3]);
            }
            if (drawEast)
            {
                graphics.drawLine(halfPoly.xpoints[1], halfPoly.ypoints[1], halfPoly.xpoints[2], halfPoly.ypoints[2]);
            }
        }
    }

    private void renderNpcTileMarker(Graphics2D graphics, NPC npc)
    {
        WorldPoint npcWorldPoint = npc.getWorldLocation();
        if (npcWorldPoint == null)
        {
            return;
        }

        LocalPoint localPoint = LocalPoint.fromWorld(client, npcWorldPoint);
        if (localPoint == null)
        {
            return;
        }

        Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);
        if (tilePoly == null)
        {
            return;
        }

        int centerX, centerY;

        // Check if this is the 2x2 NPC (ID 15187)
        if (plugin.isLargeNpc(npc))
        {
            // For 2x2 NPC (15187), place on the center of the east edge of the southwest tile
            // The NPC's world location is the southwest tile, so we use that directly
            // East edge is between point 1 (SE) and point 2 (NE)
            centerX = (tilePoly.xpoints[1] + tilePoly.xpoints[2]) / 2;
            centerY = (tilePoly.ypoints[1] + tilePoly.ypoints[2]) / 2;
        }
        else if (plugin.isCenterNpc(npc))
        {
            // For center NPC (15186), place in the absolute center of the tile
            centerX = (tilePoly.xpoints[0] + tilePoly.xpoints[1] + tilePoly.xpoints[2] + tilePoly.xpoints[3]) / 4;
            centerY = (tilePoly.ypoints[0] + tilePoly.ypoints[1] + tilePoly.ypoints[2] + tilePoly.ypoints[3]) / 4;
        }
        else
        {
            // For other 1x1 NPCs (15188, 15189, 15190), use the center of the south edge
            // South edge is between point 0 (SW) and point 1 (SE)
            centerX = (tilePoly.xpoints[0] + tilePoly.xpoints[1]) / 2;
            centerY = (tilePoly.ypoints[0] + tilePoly.ypoints[1]) / 2;
        }

        // Draw the dot
        Color dotColor = config.npcMarkerColor();
        graphics.setColor(dotColor);
        graphics.fillOval(centerX - DOT_SIZE / 2, centerY - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);

        // Optional: Add a border to the dot for better visibility
        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(1));
        graphics.drawOval(centerX - DOT_SIZE / 2, centerY - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
    }
}