package mod.addstablelocations;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.util.Misc;

import mod.addstablelocations.utilities.CostPanelItem;
import mod.addstablelocations.utilities.CostPanelUtils;
import mod.addstablelocations.utilities.JSONUtils;


public class StarDialogPluginImpl implements InteractionDialogPlugin {
    public static String ADDED_KEY = "$core_starAddedStable";
    private static enum OptionId {
        INIT,
        ADD_STABLE_CONFIRM,
        ADD_STABLE_DESCRIBE,
        ADD_STABLE_NEVER_MIND,
        LEAVE,
    }

    public Map<String, MemoryAPI> getMemoryMap() {return null;}
    public Object getContext() {return null;}
    public void advance(float amount) {}
    public void optionMousedOver(String optionText, Object optionData) {}
    public void backFromEngagement(EngagementResultAPI result) {}

    private InteractionDialogAPI dialog;
    private PlanetAPI planet;
    private TextPanelAPI textPanel;
    private OptionPanelAPI options;
    private CampaignFleetAPI playerFleet;

    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        playerFleet = Global.getSector().getPlayerFleet();
        textPanel = dialog.getTextPanel();
        options = dialog.getOptionPanel();
        VisualPanelAPI visual = dialog.getVisualPanel();
        planet = (PlanetAPI) dialog.getInteractionTarget();
        visual.setVisualFade(0.25f, 0.25f);
        if (planet.getCustomInteractionDialogImageVisual() != null) {
            visual.showImageVisual(planet.getCustomInteractionDialogImageVisual());
        } else {
            if (!Global.getSettings().getBoolean("3dPlanetBGInInteractionDialog")) {
                visual.showPlanetInfo(planet);
            }
        }
        dialog.setOptionOnEscape("Leave", OptionId.LEAVE);
        optionSelected(null, OptionId.INIT);
    }
    public void optionSelected(String text, Object optionData) {
        OptionId option = (OptionId) optionData;
        switch (option) {
            case INIT: {
                boolean didAlready = planet.getMemoryWithoutUpdate().getBoolean(ADDED_KEY);
                addText(getString("approach"));
                if (didAlready) {
                    addText("The star's corona exhibits fluctuations indicative of recent antimatter application.");
                }
                Description desc = Global.getSettings().getDescription(planet.getCustomDescriptionId(), Type.CUSTOM);
                if (desc != null && desc.hasText3()) {
                    addText(desc.getText3());
                }
                createInitialOptions();
                break;
            }
            case ADD_STABLE_CONFIRM: {
                StarSystemAPI system = planet.getStarSystem();
                if (system != null) {
                    CostPanelUtils.removeCosts(dialog, CostPanelUtils.getItemCosts(true));
                    StarSystemGenerator.addStableLocations(system, 1);
                    planet.getMemoryWithoutUpdate().set(ADDED_KEY, true);
                    addText("Preparations are made, and you give the go-ahead. " +
                            "A few tense minutes later, the chief engineer reports success. " +
                            "The resulting stable location won't last for millennia, like " +
                            "naturally-occurring ones - but it'll do for your purposes.");
                }
                createInitialOptions();
                break;
            }
            case ADD_STABLE_DESCRIBE: {
                JSONObject ModConfig = JSONUtils.getModConfig();

                addText(
                    "The procedure requires spreading prodigious amounts of antimatter in the star's corona, " +
                    "according to calculations far beyond the ability of anything on the right side of the " +
                    "treaty that ended the Second AI War."
                );

                ArrayList<CostPanelItem> costPanelItems = new ArrayList<>();
                costPanelItems.addAll(CostPanelUtils.getItemCosts(false));
                costPanelItems.addAll(CostPanelUtils.getItemCosts(true));

                boolean canAfford = CostPanelUtils.setCostPanel(dialog, costPanelItems, "Resources required (available)");
                options.clearOptions();

                int num = Misc.getNumStableLocations(planet.getStarSystem());
                int maxStableLocationCount = JSONUtils.loadInt(ModConfig, "MaxStableLocationCount");

                if (num >= maxStableLocationCount){
                    options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
                    options.setEnabled(OptionId.ADD_STABLE_CONFIRM, false);
                    String reason = "This procedure can not performed in a star system that already has numerous stable locations.";
                    addText(reason);
                    options.setTooltip(OptionId.ADD_STABLE_CONFIRM, reason);
                }else{
                    if(!canAfford){
                        options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
                        options.setEnabled(OptionId.ADD_STABLE_CONFIRM, false);
                        String reason = "You do not have the necessary resources to carry out this procedure.";
                        addText(reason);
                        options.setTooltip(OptionId.ADD_STABLE_CONFIRM, reason);
                    }else{
                        if (JSONUtils.loadBoolean(ModConfig, "RequireStoryPoint")){
                            addText("Normally, this procedure can only be performed in a star system without any " +
                            "stable locations. However, your chief engineer suggests an unorthodox workaround.");
                            options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
                            SetStoryOption.set(dialog, JSONUtils.loadInt(ModConfig, "RequireStoryPointAmount"),
                                OptionId.ADD_STABLE_CONFIRM, "createStableLocation", Sounds.STORY_POINT_SPEND_TECHNOLOGY,
                                "Created additional stable location in " + planet.getStarSystem().getNameWithLowercaseType() + "");
                        } else{
                            options.addOption("Proceed with the operation", OptionId.ADD_STABLE_CONFIRM, null);
                            options.setEnabled(OptionId.ADD_STABLE_CONFIRM, true);
                        }
                    }
                }
                options.addOption("Never mind", OptionId.ADD_STABLE_NEVER_MIND, null);
                break;
            }
            case ADD_STABLE_NEVER_MIND: {
                createInitialOptions();
                break;
            }
            case LEAVE: {
                Global.getSector().setPaused(false);
                dialog.dismiss();
                break;
            }
        }
    }

    private void createInitialOptions(){
        options.clearOptions();
        options.addOption("Consider inducing a resonance cascade in the star's hyperfield, creating a stable location", OptionId.ADD_STABLE_DESCRIBE, null);
        options.addOption("Leave", OptionId.LEAVE, null);
        options.setShortcut(OptionId.LEAVE, Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    // Copied from PlanetInteractionDialogPluginImpl
    private void addText(String text) {
        textPanel.addParagraph(text);
    }

    // Copied from PlanetInteractionDialogPluginImpl
    private String getString(String id) {
        String str = Global.getSettings().getString("planetInteractionDialog", id);

        String fleetOrShip = "fleet";
        if (playerFleet.getFleetData().getMembersListCopy().size() == 1) {
            fleetOrShip = "ship";
            if (playerFleet.getFleetData().getMembersListCopy().get(0).isFighterWing()) {
                fleetOrShip = "fighter wing";
            }
        }
        str = str.replaceAll("\\$fleetOrShip", fleetOrShip);
        str = str.replaceAll("\\$planetName", planet.getName());
        return str;
    }
}
