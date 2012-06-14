package net.dtl.citizenstrader.traits;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.dtl.citizenstrader.TraderStatus.Status;
import net.dtl.citizenstrader.containers.StockItem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

public class InventoryTrait extends Trait implements InventoryHolder {
	
	private List<StockItem> sellStock = new ArrayList<StockItem>();					//What the trader sells the player
	private List<StockItem> buyStock = new ArrayList<StockItem>();					//What the trader buys from the player 
	private int size;
	private boolean firstSave;
	
	public InventoryTrait() {
		this(54); 
	}
	
	private InventoryTrait(int stockSize){
	//	if ( this.getName() == null ) {
	//		this.setName("inv");
	//	}
		size = stockSize;
        if( size <= 0 || size > 54 ){
        	throw new IllegalArgumentException("Size must be between 1 and 54");}
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(DataKey data) throws NPCLoadException {
		if ( data.keyExists("sell") ) {
			for ( String item :  (List<String>) data.getRaw("sell") ) {
				sellStock.add(new StockItem(item));
			}
		}

		if ( data.keyExists("buy") ) {
			for ( String item :  (List<String>) data.getRaw("buy") ) 
				buyStock.add(new StockItem(item));
		}
	}
	
	
	@Override
	public void save(DataKey data) {
		System.out.print(data);
		
        List<String> sellList = new ArrayList<String>();
		if ( !sellStock.isEmpty() )
	        for ( StockItem item : sellStock )
	            	sellList.add(item.toString());
        
		List<String> buyList = new ArrayList<String>();
		if ( !buyStock.isEmpty() )
			for ( StockItem item : buyStock )
				buyList.add(item.toString());

		data.setRaw("sell", sellList);
		data.setRaw("buy", buyList);
		
	}
	
	//Returning the displayInventory
	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this,size,"sellstockroom");

        for ( StockItem item : sellStock )
            inv.addItem(item.getItemStack());
        
		return inv;
	}
	
	@SuppressWarnings("unused")
	public Inventory inventoryView(Inventory view,Status s) {

		int i = 0;
		if ( s.equals(Status.PLAYER_SELL) ) {
			for( StockItem item : sellStock ){
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            //if ( view.contains(chk) == false ) {
	                view.setItem( item.getSlot() ,chk);
	            //}
	            i++;
	        }
            if ( !buyStock.isEmpty() )
            	view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));//3
		} else if ( s.equals(Status.PLAYER_BUY ) ) {
			for( StockItem item : buyStock ) {
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            //if ( view.contains(chk) == false ) {
	            	if ( item.getSlot() < 0 )
	            		item.setSlot(view.firstEmpty());
	                view.setItem( item.getSlot() ,chk);
	            //}
	            i++;
	        }
            view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)3));//3
		} else if ( s.equals(Status.PLAYER_MANAGE_SELL ) ) {
			for( StockItem item : sellStock ) {
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            //if ( view.contains(chk) == false ) {
	            	if ( item.getSlot() < 0 )
	            		item.setSlot(view.firstEmpty());
	                view.setItem( item.getSlot() ,chk);
	            //}
	            i++;
	        }
            view.setItem(view.getSize()-2, new ItemStack(Material.WOOL,1));//3
            view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));//3
		} else if ( s.equals(Status.PLAYER_MANAGE_BUY ) ) {
			for( StockItem item : buyStock ) {
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            //if ( view.contains(chk) == false ) {
	            	if ( item.getSlot() < 0 )
	            		item.setSlot(view.firstEmpty());
	                view.setItem( item.getSlot() ,chk);
	            //}
	            i++;
	        }
            view.setItem(view.getSize()-2, new ItemStack(Material.WOOL,1));//3
            view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)3));//3
		} else if ( s.equals(Status.PLAYER_MANAGE_PRICE ) ) {
			for( StockItem item : sellStock ) {
	            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
	            chk.addEnchantments(item.getItemStack().getEnchantments());
	            //if ( view.contains(chk) == false ) {
	            	if ( item.getSlot() < 0 )
	            		item.setSlot(view.firstEmpty());
	                view.setItem( item.getSlot() ,chk);
	            //}
	            i++;
	        }
            view.setItem(view.getSize()-2, new ItemStack(Material.WOOL,1,(short)0,(byte)15));//3
		}
		
		return view;
	}
	@SuppressWarnings("unused")
	public Inventory inventoryView(int size, String name) {
		Inventory view = Bukkit.createInventory(this, size, name);
		
		int i = 0;
		for( StockItem item : sellStock ){
            ItemStack chk = new ItemStack(item.getItemStack().getType(),item.getItemStack().getAmount(),item.getItemStack().getDurability());
            chk.addEnchantments(item.getItemStack().getEnchantments());
            //if ( view.contains(chk) == false ) {
            	if ( item.getSlot() < 0 )
            		item.setSlot(view.firstEmpty());
                view.setItem(item.getSlot(),chk);
            //}
            i++;
        }

        if ( !buyStock.isEmpty() )
        	view.setItem(view.getSize()-1, new ItemStack(Material.WOOL,1,(short)0,(byte)5));//3
        
		return view;
	}
	
	public StockItem itemForSell(int slot) {
		for ( StockItem item : sellStock )
			if ( item.getSlot() == slot )
				return item;
		return null;
	}

	public StockItem wantItemBuy(int slot) {
		for ( StockItem item : buyStock )
			if ( item.getSlot() == slot )
				return item;
		return null;
	}
	
	public StockItem wantItemBuy(ItemStack itemStack) {
		for ( StockItem item : buyStock )
			if ( item.getItemStack().equals(itemStack) && item.getItemStack().getEnchantments().equals(itemStack.getEnchantments()) )
				return item;
		return null;
	}
	
	public static void setInventoryWith(Inventory inv,StockItem si) {
		int i = 0;
		for ( Integer amount : si.getAmouts() ) {
			ItemStack is = si.getItemStack().clone();
			is.setAmount(amount);
			inv.setItem(i++,is);
		}
		inv.setItem(inv.getSize()-1,new ItemStack(Material.WOOL,1,(short)0,(byte)14));
	}
	
	public void addItem(boolean sell,String data) {
		if ( sell )
			sellStock.add(new StockItem(data));
		else
			buyStock.add(new StockItem(data));
	}
	public void addItem(boolean sell,StockItem si) {
		if ( sell )
			sellStock.add(si);
		else
			buyStock.add(si);
	}
	public void removeItem(boolean sell,int slot) {
		if ( sell ) {
			for ( StockItem item : sellStock )
				if ( item.getSlot() == slot ) {
					sellStock.remove(item);
					return;
				}
		} else 
			for ( StockItem item : buyStock )
				if ( item.getSlot() == slot ) {
					buyStock.remove(item);
					return;
				}
	}
	public void saveNewAmouts(Inventory inv,StockItem si) {
		si.getAmouts().clear();
		for ( ItemStack is : inv.getContents() ) {
			if ( is != null ) {
				si.addAmout(is.getAmount());
			}
		}
		if ( si.getAmouts().size() > 1 )
			si.getAmouts().remove(si.getAmouts().size()-1);
	}
	
}
