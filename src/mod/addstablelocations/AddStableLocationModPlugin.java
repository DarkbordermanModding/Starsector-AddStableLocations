package mod.addstablelocations;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;


public class AddStableLocationModPlugin extends BaseModPlugin {

    public void onGameLoad(boolean newGame) {
        Global.getSector().registerPlugin(new AddStableLocationCampaignPlugin());
    }
}
