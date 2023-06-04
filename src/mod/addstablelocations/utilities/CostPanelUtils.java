package mod.addstablelocations.utilities;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;

public class CostPanelUtils {
    public static boolean setCostPanel(InteractionDialogAPI dialog, ArrayList<CostPanelItem> costPanelItems, String costTitle){
        boolean canAfford = true;
        boolean costTitlePrinted = false;
        boolean result = true;
        ArrayList<Object> displays = new ArrayList<>();

        if(costTitle == null){
            costTitle = "";
        }
        // Due to display limit, the display will cut each 3 display items(9 length array)
        for(CostPanelItem costPanelItem: costPanelItems){
            displays.add(costPanelItem.item_id);
            displays.add(costPanelItem.amount);
            displays.add(costPanelItem.consumed);
            if(displays.size() == 9){
                if(!costTitlePrinted){
                    result = dialog.getTextPanel().addCostPanel(costTitle, displays.toArray());
                    costTitlePrinted = true;
                }
                else{
                    result = dialog.getTextPanel().addCostPanel("", displays.toArray());
                }
                if(result == false){
                    canAfford = false;
                }
                displays.clear();
            }
        }
        if(!displays.isEmpty()){
            if(!costTitlePrinted){
                result = dialog.getTextPanel().addCostPanel(costTitle, displays.toArray());
            }else{
                result = dialog.getTextPanel().addCostPanel("", displays.toArray());
            }
        }
        if(result == false){
            canAfford = false;
        }
        return canAfford;
    }

    public static ArrayList<CostPanelItem> getItemCosts(boolean isConsumed){
        JSONObject ModConfig = JSONUtils.getModConfig();
        JSONObject resources;
        if(isConsumed){
            resources = JSONUtils.loadJSON(ModConfig, "ConsumedResources");
        }else{
            resources = JSONUtils.loadJSON(ModConfig, "RequiredResources");
        }
        ArrayList<CostPanelItem> costPanelItems = new ArrayList<>();
        Iterator<String> keys = resources.keys();

        while(keys.hasNext()){
            String key = keys.next();
            costPanelItems.add(
                new CostPanelItem(key, JSONUtils.loadInt(resources, key), isConsumed)
            );
        }
        return costPanelItems;
    }

    public static void removeCosts(InteractionDialogAPI dialog, ArrayList<CostPanelItem> resources){
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        for(CostPanelItem resource: resources){
            cargo.removeCommodity(resource.item_id, resource.amount);
            AddRemoveCommodity.addCommodityLossText(resource.item_id, resource.amount, dialog.getTextPanel());
        }
    }
}
