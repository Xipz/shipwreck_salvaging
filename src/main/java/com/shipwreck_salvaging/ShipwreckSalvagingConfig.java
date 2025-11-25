package com.shipwrecksalvaging;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("shipwrecksalvaging")
public interface ShipwreckSalvagingConfig extends Config
{
    // Salvage Range Section - Position 0
    @ConfigSection(
            name = "Salvage Range",
            description = "Settings for the salvage range overlay",
            position = 0
    )
    String salvageRangeSection = "salvageRange";

    @ConfigItem(
            keyName = "showSalvageRange",
            name = "Show Salvage Range",
            description = "Highlights the tiles within salvage range of shipwrecks",
            section = salvageRangeSection,
            position = 0
    )
    default boolean showSalvageRange()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
            keyName = "tileFillColor",
            name = "Fill Color",
            description = "Color to fill the salvage range tiles",
            section = salvageRangeSection,
            position = 1
    )
    default Color tileFillColor()
    {
        return new Color(0, 255, 255, 50);
    }

    @ConfigItem(
            keyName = "tileBorderColor",
            name = "Border Color",
            description = "Color of the tile borders",
            section = salvageRangeSection,
            position = 2
    )
    default Color tileBorderColor()
    {
        return new Color(0, 255, 255, 255);
    }

    @Range(min = 1, max = 5)
    @ConfigItem(
            keyName = "tileBorderWidth",
            name = "Border Width",
            description = "Width of the tile borders in pixels",
            section = salvageRangeSection,
            position = 3
    )
    default int tileBorderWidth()
    {
        return 2;
    }

    @Range(min = 0, max = 255)
    @ConfigItem(
            keyName = "fillOpacity",
            name = "Fill Opacity",
            description = "Opacity of the tile fill (0-255)",
            section = salvageRangeSection,
            position = 4
    )
    default int fillOpacity()
    {
        return 50;
    }

    // Salvage Overlap Section - Position 1
    @ConfigSection(
            name = "Salvage Overlap",
            description = "Settings for overlapping salvage ranges",
            position = 1
    )
    String salvageOverlapSection = "salvageOverlap";

    @ConfigItem(
            keyName = "showOverlap",
            name = "Show Overlap",
            description = "Highlights tiles where multiple salvage ranges overlap",
            section = salvageOverlapSection,
            position = 0
    )
    default boolean showOverlap()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
            keyName = "overlapFillColor",
            name = "Overlap Fill Color",
            description = "Color to fill overlapping salvage range tiles",
            section = salvageOverlapSection,
            position = 1
    )
    default Color overlapFillColor()
    {
        return new Color(0, 255, 0, 80);
    }

    @ConfigItem(
            keyName = "overlapBorderColor",
            name = "Overlap Border Color",
            description = "Color of the overlap tile borders",
            section = salvageOverlapSection,
            position = 2
    )
    default Color overlapBorderColor()
    {
        return new Color(0, 255, 0, 255);
    }

    // Depleted Shipwreck Section - Position 2
    @ConfigSection(
            name = "Depleted Shipwreck",
            description = "Settings for depleted shipwrecks",
            position = 2
    )
    String depletedShipwreckSection = "depletedShipwreck";

    @ConfigItem(
            keyName = "highlightShipwreck",
            name = "Highlight Depleted Shipwreck",
            description = "Highlights depleted shipwrecks",
            section = depletedShipwreckSection,
            position = 0
    )
    default boolean highlightShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "shipwreckColor",
            name = "Shipwreck Color",
            description = "Color to highlight depleted shipwrecks",
            section = depletedShipwreckSection,
            position = 1
    )
    default Color shipwreckColor()
    {
        return new Color(255, 0, 0, 255);
    }

    // NPC Marker Section - Position 3
    @ConfigSection(
            name = "Pivot Point",
            description = "Settings for pivot point markers",
            position = 3
    )
    String npcMarkerSection = "npcMarker";

    @ConfigItem(
            keyName = "showNpcMarker",
            name = "Show Pivot Point",
            description = "Shows a dot on the pivot point location",
            section = npcMarkerSection,
            position = 0
    )
    default boolean showNpcMarker()
    {
        return true;
    }

    @ConfigItem(
            keyName = "npcMarkerColor",
            name = "Pivot Point Color",
            description = "Color of the pivot point marker dot",
            section = npcMarkerSection,
            position = 1
    )
    default Color npcMarkerColor()
    {
        return new Color(255, 255, 0, 255); // Yellow by default
    }

    // Toggle Shipwrecks Section - Position 4 (Last)
    @ConfigSection(
            name = "Toggle Shipwrecks",
            description = "Enable or disable specific shipwreck types",
            position = 4,
            closedByDefault = false
    )
    String toggleShipwrecksSection = "toggleShipwrecks";

    @ConfigItem(
            keyName = "showSmallShipwreck",
            name = "Small Shipwreck",
            description = "Show highlights for Small Shipwrecks",
            section = toggleShipwrecksSection,
            position = 0
    )
    default boolean showSmallShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showFishermanShipwreck",
            name = "Fisherman's Shipwreck",
            description = "Show highlights for Fisherman's Shipwrecks",
            section = toggleShipwrecksSection,
            position = 1
    )
    default boolean showFishermanShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showBarracudaShipwreck",
            name = "Barracuda Shipwreck",
            description = "Show highlights for Barracuda Shipwrecks",
            section = toggleShipwrecksSection,
            position = 2
    )
    default boolean showBarracudaShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showLargeShipwreck",
            name = "Large Shipwreck",
            description = "Show highlights for Large Shipwrecks",
            section = toggleShipwrecksSection,
            position = 3
    )
    default boolean showLargeShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showPirateShipwreck",
            name = "Pirate Shipwreck",
            description = "Show highlights for Pirate Shipwrecks",
            section = toggleShipwrecksSection,
            position = 4
    )
    default boolean showPirateShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showMercenaryShipwreck",
            name = "Mercenary Shipwreck",
            description = "Show highlights for Mercenary Shipwrecks",
            section = toggleShipwrecksSection,
            position = 5
    )
    default boolean showMercenaryShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showFremennikShipwreck",
            name = "Fremennik Shipwreck",
            description = "Show highlights for Fremennik Shipwrecks",
            section = toggleShipwrecksSection,
            position = 6
    )
    default boolean showFremennikShipwreck()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showMerchantShipwreck",
            name = "Merchant Shipwreck",
            description = "Show highlights for Merchant Shipwrecks",
            section = toggleShipwrecksSection,
            position = 7
    )
    default boolean showMerchantShipwreck()
    {
        return true;
    }
}