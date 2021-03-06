package net.dandielo.citizens.trader.cititrader;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.parts.TraderStockPart;
import net.dandielo.citizens.trader.types.Trader;

public class ShopTrait extends Trait {
	private TraderTrait trait;

	public ShopTrait() {
		super("shop"); 
	}

	@Override
	public void onAttach()
	{
		if ( !npc.hasTrait(TraderTrait.class) )
			npc.addTrait(TraderTrait.class);
		trait = npc.getTrait(TraderTrait.class);
	}
	
	@Override
	public void load(DataKey data)
	{
		TraderStockPart stock = trait.getStock();
		
		//load selling prices
        for (DataKey priceKey : data.getRelative("prices").getIntegerSubKeys())
        {
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));

            double price = priceKey.getDouble("price");
            int stacksize = priceKey.getInt("stack", 1);

            StockItem item = Trader.toStockItem(k);
            if ( item != null )
            {
            	item.setPatternPrice(false);
            	item.setRawPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setRawPrice(price);
            		item.setAmount(stacksize);
            	}
    			stock.addItem("sell", item);
            }
        }
        
        //load buy prices
        for (DataKey priceKey : data.getRelative("buyprices").getIntegerSubKeys()) 
        {
            ItemStack k = ItemStorage.loadItemStack(priceKey.getRelative("item"));

            // Assume once that if there is an item, that it is real.
            if (k == null) {
                int test = priceKey.getInt("item.id");
                Material mat = Material.getMaterial(test);
                priceKey.setString("item.id", mat.name());
                k = ItemStorage.loadItemStack((priceKey.getRelative("item")));
            }

            double price = priceKey.getDouble("price");
            int stacksize = priceKey.getInt("stack", 1);

            StockItem item = Trader.toStockItem(k);
            if ( item != null )
            {
            	item.setPatternPrice(false);
            	item.setRawPrice(price);
            	if ( stacksize > 1 )
            	{
            		item.setStackPrice(true);
            		item.setAmount(stacksize);
            	}
    			stock.addItem("buy", item);
            }
        }
		npc.removeTrait(ShopTrait.class);
	}
	
}
