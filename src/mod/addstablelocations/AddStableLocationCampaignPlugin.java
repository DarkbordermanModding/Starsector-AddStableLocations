package mod.addstablelocations;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import mod.addstablelocations.utilities.JSONUtils;


public class AddStableLocationCampaignPlugin extends BaseCampaignPlugin{

    // make it safe to remove from game
    public boolean isTransient(){return true;}

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget instanceof PlanetAPI) {
            PlanetAPI planet = (PlanetAPI)interactionTarget;
            StarSystemAPI system = planet.getStarSystem();
            if (system != null && planet == system.getStar()){
                if(!planet.getSpec().isBlackHole()){
                    if (JSONUtils.loadBoolean(JSONUtils.getModConfig(), "AllowStar") == true){
                        return new PluginPick<InteractionDialogPlugin>(new StarDialogPluginImpl(), PickPriority.CORE_GENERAL);
                    }
                }else{
                    if (JSONUtils.loadBoolean(JSONUtils.getModConfig(), "AllowBlackHole") == true){
                        return new PluginPick<InteractionDialogPlugin>(new BlackHoleDialogPluginImpl(), PickPriority.CORE_GENERAL);
                    }
                }
            }
        }
        return null;
    }
}
