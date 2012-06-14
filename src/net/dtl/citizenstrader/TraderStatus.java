package net.dtl.citizenstrader;

import org.bukkit.inventory.Inventory;

import net.citizensnpcs.api.npc.NPC;
import net.dtl.citizenstrader.containers.StockItem;
import net.dtl.citizenstrader.tradertypes.Trader.TraderType;

public class TraderStatus {
	
	public enum Status {
		PLAYER_SELL, PLAYER_BUY, PLAYER_SELL_AMOUT, PLAYER_MANAGE_SELL, PLAYER_MANAGE_SELL_AMOUT, PLAYER_MANAGE_PRICE, PLAYER_MANAGE_BUY,
	}
	
	private NPC trader;
	private Status status;
	private Inventory inventory;
	private TraderType type;
	private net.dtl.citizenstrader.containers.StockItem itemSelected;
	private boolean lastInv = false;
	private int lastSlot = -1;
	
	public TraderStatus(NPC t) {
		trader = t;
		status = Status.PLAYER_SELL;
		inventory = null;
		itemSelected = null;
	}
	public TraderStatus(NPC t,Status s) {
		trader = t;
		status = s;
		inventory = null;
		itemSelected = null;
	}
	
	public void setTraderType(TraderType t) {
		type = t;
	}
	public TraderType getTraderType() {
		return type;
	}
	public void setStatus(Status s) {
		status = s;
	}
	public Status getStatus() {
		return status;
	}
	public void setLastInv(boolean l) {
		lastInv = l;
	}
	public boolean getLastInv() {
		return lastInv;
	}
	public void setLastSlot(int slot) {
		lastSlot = slot;
	}
	public int getLastSlot() {
		return lastSlot;
	}
	
	public void setInventory(Inventory i) {
		inventory = i;
	}
	public Inventory getInventory() {
		return inventory;
	}
	
	public void setStockItem(StockItem si) {
		itemSelected = si;
	}
	public StockItem getStockItem() {
		return itemSelected;
	}
	
	public NPC getTrader() {
		return trader;
	}
	
	
}
