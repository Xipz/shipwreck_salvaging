package com.shipwrecksalvaging;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "Shipwreck Salvaging",
        description = "Highlights the salvage range around shipwrecks",
        tags = {"sailing", "shipwreck", "salvaging", "overlay"}
)
public class ShipwreckSalvagingPlugin extends Plugin
{
    // Shipwreck IDs
    private static final Set<Integer> SMALL_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60464);
    private static final Set<Integer> SMALL_SHIPWRECK_DEPLETED_IDS = Set.of(60465);

    private static final Set<Integer> FISHERMAN_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60466);
    private static final Set<Integer> FISHERMAN_SHIPWRECK_DEPLETED_IDS = Set.of(60467);

    private static final Set<Integer> BARRACUDA_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60468);
    private static final Set<Integer> BARRACUDA_SHIPWRECK_DEPLETED_IDS = Set.of(60469);

    private static final Set<Integer> LARGE_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60470);
    private static final Set<Integer> LARGE_SHIPWRECK_DEPLETED_IDS = Set.of(60471);

    private static final Set<Integer> PIRATE_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60472);
    private static final Set<Integer> PIRATE_SHIPWRECK_DEPLETED_IDS = Set.of(60473);

    private static final Set<Integer> MERCENARY_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60474);
    private static final Set<Integer> MERCENARY_SHIPWRECK_DEPLETED_IDS = Set.of(60475);

    private static final Set<Integer> FREMENNIK_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60476);
    private static final Set<Integer> FREMENNIK_SHIPWRECK_DEPLETED_IDS = Set.of(60477);

    private static final Set<Integer> MERCHANT_SHIPWRECK_SALVAGEABLE_IDS = Set.of(60478);
    private static final Set<Integer> MERCHANT_SHIPWRECK_DEPLETED_IDS = Set.of(60479);

    // NPC IDs to track
    private static final Set<Integer> TARGET_NPC_IDS = Set.of(15186, 15187, 15188, 15189, 15190);
    private static final int LARGE_NPC_ID = 15187; // 2x2 NPC that needs special handling
    private static final int CENTER_NPC_ID = 15186; // NPC that needs center placement

    @Inject
    private Client client;

    @Inject
    private ShipwreckSalvagingConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ShipwreckSalvagingOverlay overlay;

    private final Set<GameObject> activeShipwrecks = new HashSet<>();
    private final Set<NPC> trackedNpcs = new HashSet<>();

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        // Scan for existing NPCs when plugin starts
        if (client.getGameState() == net.runelite.api.GameState.LOGGED_IN)
        {
            for (NPC npc : client.getNpcs())
            {
                if (TARGET_NPC_IDS.contains(npc.getId()))
                {
                    trackedNpcs.add(npc);
                }
            }
        }
        log.info("Shipwreck Salvaging started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        activeShipwrecks.clear();
        trackedNpcs.clear();
        log.info("Shipwreck Salvaging stopped!");
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();
        if (isShipwreck(gameObject) && isShipwreckEnabled(gameObject))
        {
            activeShipwrecks.add(gameObject);
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        GameObject gameObject = event.getGameObject();
        activeShipwrecks.remove(gameObject);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        NPC npc = event.getNpc();
        if (TARGET_NPC_IDS.contains(npc.getId()))
        {
            trackedNpcs.add(npc);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        NPC npc = event.getNpc();
        trackedNpcs.remove(npc);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState().equals(net.runelite.api.GameState.LOADING))
        {
            activeShipwrecks.clear();
            // Don't clear trackedNpcs here - let them persist
        }
        else if (event.getGameState().equals(net.runelite.api.GameState.LOGGED_IN))
        {
            // Re-scan for NPCs after loading to pick up any new ones
            for (NPC npc : client.getNpcs())
            {
                if (TARGET_NPC_IDS.contains(npc.getId()))
                {
                    trackedNpcs.add(npc);
                }
            }
        }
    }

    public Set<GameObject> getActiveShipwrecks()
    {
        return activeShipwrecks;
    }

    public Set<NPC> getTrackedNpcs()
    {
        return trackedNpcs;
    }

    public boolean isLargeNpc(NPC npc)
    {
        return npc.getId() == LARGE_NPC_ID;
    }

    public boolean isCenterNpc(NPC npc)
    {
        return npc.getId() == CENTER_NPC_ID;
    }

    public boolean isShipwreckDepleted(GameObject gameObject)
    {
        int id = gameObject.getId();
        return SMALL_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                FISHERMAN_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                BARRACUDA_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                LARGE_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                PIRATE_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                MERCENARY_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                FREMENNIK_SHIPWRECK_DEPLETED_IDS.contains(id) ||
                MERCHANT_SHIPWRECK_DEPLETED_IDS.contains(id);
    }

    public boolean isShipwreckSalvageable(GameObject gameObject)
    {
        int id = gameObject.getId();
        return SMALL_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                FISHERMAN_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                BARRACUDA_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                LARGE_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                PIRATE_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                MERCENARY_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                FREMENNIK_SHIPWRECK_SALVAGEABLE_IDS.contains(id) ||
                MERCHANT_SHIPWRECK_SALVAGEABLE_IDS.contains(id);
    }

    private boolean isShipwreck(GameObject gameObject)
    {
        return isShipwreckSalvageable(gameObject) || isShipwreckDepleted(gameObject);
    }

    public boolean isShipwreckEnabled(GameObject gameObject)
    {
        int id = gameObject.getId();

        if (SMALL_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || SMALL_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showSmallShipwreck();
        if (FISHERMAN_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || FISHERMAN_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showFishermanShipwreck();
        if (BARRACUDA_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || BARRACUDA_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showBarracudaShipwreck();
        if (LARGE_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || LARGE_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showLargeShipwreck();
        if (PIRATE_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || PIRATE_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showPirateShipwreck();
        if (MERCENARY_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || MERCENARY_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showMercenaryShipwreck();
        if (FREMENNIK_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || FREMENNIK_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showFremennikShipwreck();
        if (MERCHANT_SHIPWRECK_SALVAGEABLE_IDS.contains(id) || MERCHANT_SHIPWRECK_DEPLETED_IDS.contains(id))
            return config.showMerchantShipwreck();

        return true;
    }

    @Provides
    ShipwreckSalvagingConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ShipwreckSalvagingConfig.class);
    }
}