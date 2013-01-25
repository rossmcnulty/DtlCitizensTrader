package net.dtl.citizens.trader.denizen.commands;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.dtl.citizens.trader.CitizensTrader;
import net.dtl.citizens.trader.TraderCharacterTrait;
import net.dtl.citizens.trader.denizen.AbstractDenizenCommand;
import net.dtl.citizens.trader.events.TraderTransactionEvent;
import net.dtl.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dtl.citizens.trader.managers.LocaleManager;
import net.dtl.citizens.trader.types.ServerTrader;
import net.dtl.citizens.trader.types.Trader;
import net.dtl.citizens.trader.types.Trader.TraderStatus;

public class TransactionCommand extends AbstractDenizenCommand {

	/** 
	 * TRANSACTION ({SELL}|BUY|OPEN|CLOSE) [ITEM:#(:#)] (QTY:#) 
	 * 
	 * 
	 * Arguments: [] - Required, () - Optional 
	 * [POTION_EFFECT] Uses bukkit enum for specifying the potion effect to use.
	 *   
	 * Example Usage:
	 * 
	 */

	// Initialize variables 
	
	LocaleManager locale = CitizensTrader.getLocaleManager();
	
	public TransactionCommand()
	{
		this.activate().as("TRANSACTION").withOptions("({SELL}|BUY) [ITEM:item_name(:#)] (QTY:#)", 1);
		CitizensTrader.info("Registered denizen " + ChatColor.YELLOW + TransactionCommand.class.getSimpleName());
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		Trader trader = null;
		ItemStack item = null;
		String action = "SELL";
		int qty = 1;
		
		trader = new ServerTrader(scriptEntry.getNPC().getCitizen().getTrait(TraderCharacterTrait.class), scriptEntry.getNPC().getCitizen(), scriptEntry.getPlayer());
		CitizensTrader.getNpcEcoManager().addInteractionNpc(scriptEntry.getPlayer().getName(), trader);

		for (String arg : scriptEntry.getArguments())
		{
            if (aH.matchesItem(arg)) {
            	item = aH.getItemFrom(arg);
				dB.echoDebug("...set ITEM: '%s'", item.getType().name());
                continue;
             
            }   else if (aH.matchesArg("SELL, BUY", arg)) {
            	action = aH.getStringFrom(arg);
				dB.echoDebug("...set ACTION: '%s'", action);
				continue;

            }	else if (aH.matchesQuantity(arg)) {
            	qty = aH.getIntegerFrom(arg);
				dB.echoDebug("...set QTY: '%s'", String.valueOf(qty));
				continue;

			}   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

		// Check for null fields from 'required' arguments
		if ( item == null ) 
			throw new InvalidArgumentsException("Must specify a valid 'Animation SCRIPT'.");
		
		scriptEntry.addObject("item", item);
		scriptEntry.addObject("trader", trader);
		scriptEntry.addObject("action", action);
		scriptEntry.addObject("qty", qty);
	}


	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		
		String action = (String) scriptEntry.getObject("action");
		Trader trader = (Trader) scriptEntry.getObject("trader");
		ItemStack item = (ItemStack) scriptEntry.getObject("item");
		int qty = (Integer) scriptEntry.getObject("qty");
		Player player = scriptEntry.getPlayer();
		
		if ( action.equals("SELL") )
		{
			trader.selectItem(item, TraderStatus.SELL, false, false);
			
			if ( !trader.hasSelectedItem() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), Trader.toStockItem(item), -1.0, TransactionResult.FAIL_ITEM));
				return;
			}
			
			double price = trader.getPrice(player, "sell")*qty;
			
			if ( !trader.getSelectedItem().getLimitSystem().checkLimit(player.getName(), 0, qty) )// !trader.checkLimits() )
			{
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
				player.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
			}
			else
			if ( !trader.inventoryHasPlaceAmount(qty) )
			{
				player.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
			}
			else
			if ( !trader.buyTransaction(price) )
			{
				player.sendMessage(locale.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
			}
			else
			{ 
				//TODO add debug mode
				player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + trader.getSelectedItem().getAmount() ).replace("{price}", new DecimalFormat("#.##").format(price) ) );

				trader.addAmountToInventory(qty);//.addSelectedToInventory(0);

				trader.updateBuyLimits(qty);
				
				//call event Denizen Transaction Trigger
				Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(trader, trader.getNpc(), player, trader.getTraderStatus(), trader.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
				
				//logging
				trader.log("buy", 
					trader.getSelectedItem().getItemStack().getTypeId(),
					trader.getSelectedItem().getItemStack().getData().getData(), 
					trader.getSelectedItem().getAmount()*qty, 
					price );
				
			}
		}
	}	

}
