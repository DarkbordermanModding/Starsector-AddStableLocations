package mod.addstablelocations.utilities;

public class CostPanelItem {
    public String item_id;
    public int amount;
    public boolean consumed;

    public CostPanelItem(String item_id, int amount, boolean consumed){
        this.item_id = item_id;
        this.amount = amount;
        this.consumed = consumed;
    }
}
