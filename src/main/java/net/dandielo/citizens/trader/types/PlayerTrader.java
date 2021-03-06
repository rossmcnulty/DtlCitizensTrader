package net.dandielo.citizens.trader.types;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.TraderTrait;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.events.TraderOpenEvent;
import net.dandielo.citizens.trader.limits.Limits;
import net.dandielo.citizens.trader.limits.Limits.Limit;
import net.dandielo.citizens.trader.objects.NBTTagEditor;
import net.dandielo.citizens.trader.objects.StockItem;
import net.dandielo.citizens.trader.parts.TraderStockPart;

public class PlayerTrader extends Trader {

	public PlayerTrader(TraderTrait trait, NPC npc, Player player) {
		super(trait, npc, player);
	}

	@Override
	public void simpleMode(InventoryClickEvent event) 
	{
		DecimalFormat f = new DecimalFormat("#.##");
		int slot = event.getSlot();
		
		if ( slot < 0 )
		{
			event.setCancelled(true);
			return;
		}
		
		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		
		if ( top ) {
			
			if ( event.isShiftClick() )
			{
				((Player)event.getWhoClicked()).sendMessage(ChatColor.GOLD + "You can't shift click this, Sorry");
				event.setCancelled(true);
				return;
			}
			
			if ( isManagementSlot(slot, 1) ) {
				
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) )
				{
					
					switchInventory(TraderStatus.SELL);		
					
				}
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
				{
					
					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.SELL);	
					//	locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:sell") );
					}
						
					
				}
				else 
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) ) 
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						locale.sendMessage(player, "error-nopermission");
				//		player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
					}
					else
					{
						switchInventory(TraderStatus.BUY);	
					//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-tab","transaction:buy") );
					}	
				}
			} 
			else
			//is slot management
			if ( equalsTraderStatus(TraderStatus.SELL) ) 
			{

				
				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
				{
					
					if ( getSelectedItem().hasMultipleAmounts() 
							&& permissionsManager.has(player, "dtl.trader.options.sell-amounts"))
					{

						switchInventory(getSelectedItem());
						setTraderStatus(TraderStatus.SELL_AMOUNT);
						
					} 
					else
					{

						double price = getSelectedItem().getPrice(0);
						if ( !checkLimits() )
						{
							locale.sendMessage(player, "trader-transaction-failed-limit");
						//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
						}
						else
						if ( !inventoryHasPlace(0) )
						{
							locale.sendMessage(player, "trader-transaction-failed-inventory");
						//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
						}
						else
						if ( !buyTransaction(price) )
						{
							locale.sendMessage(player, "trader-transaction-failed-money");
						//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
						}
						else
						{
							locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
						//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount() ).replace("{price}", f.format(getSelectedItem().getPrice()) ) );


							addSelectedToInventory(0);


							updateLimits();
							
							//logging
							log("buy", 
								getSelectedItem().getItemStack().getTypeId(),
								getSelectedItem().getItemStack().getData().getData(), 
								getSelectedItem().getAmount(), 
								price );
							
							//sending a message to the traders owner
							this.messageOwner("bought", player.getName(), getSelectedItem(), 0);
							
						}
						
					}
					
				}
				
			}
			else 
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
			{
				
				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) 
				{
					
				//	if ( getClickedSlot() == slot ) 
				//	{ 
					double price = getSelectedItem().getPrice(slot);
					if ( !checkLimits(slot) )
					{
						locale.sendMessage(player, "trader-transaction-failed-limit");
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
					}
					else
					if ( !inventoryHasPlace(slot) )
					{
						locale.sendMessage(player, "trader-transaction-failed-inventory");
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
					}
					else
					if ( !buyTransaction(price) ) 
					{
						locale.sendMessage(player, "trader-transaction-failed-money");
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#bought", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount(slot) ).replace("{price}", f.format(getSelectedItem().getPrice(slot)) ) );
						
						//
						addSelectedToInventory(slot);

						//
						updateLimits(slot);
						switchInventory(getSelectedItem());
						
						//logging
						log("buy", 
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount(slot), 
							price );
						
						//sending a message to the traders owner
						this.messageOwner("bought", player.getName(), getSelectedItem(), slot);
						
					} 
				}
			} 
			else 
			if ( equalsTraderStatus(TraderStatus.BUY) ) 
			{
				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() )
				{	
				//	locale.sendMessage(player, "");
				//	player.sendMessage( localeManager.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getSelectedItem().getPrice()) ) );
				//	player.sendMessage( localeManager.getLocaleString("item-buy-limit").replace("{limit}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()).replace("{amount}", "" + getSelectedItem().getLimitSystem().getGlobalAmount()) );
				
				}
			}
			setInventoryClicked(true);
		} 
		else
		{
			if ( equalsTraderStatus(TraderStatus.BUY) )
			{
				
				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() ) 
				{
					
					double price = getPrice(player, "buy");
					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
					if ( !checkBuyLimits(scale) )
					{
						locale.sendMessage(player, "trader-transaction-failed-limit");
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit"));
					}
					else
					if ( !sellTransaction(price*scale,event.getCurrentItem()) )
					{
						locale.sendMessage(player, "trader-transaction-failed-money");
					//	player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money"));
					}
					else
					{
						locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
					//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );


						updateBuyLimits(scale);

						removeFromInventory(event.getCurrentItem(),event);
						
						//logging
						log("sell",  
							getSelectedItem().getItemStack().getTypeId(),
							getSelectedItem().getItemStack().getData().getData(), 
							getSelectedItem().getAmount()*scale, 
							price*scale );

						//sending a message to the traders owner
						this.messageOwner("sold", player.getName(), getSelectedItem(), 0);
						
					} 

				}
			} 
			else 
			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) )
			{ 
				//p.sendMessage( locale.getLocaleString("amount-exception") );
				event.setCancelled(true);
				return;
			}
			else 
			if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() )
			{

				double price = getPrice(player, "buy");
				int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount();
				if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:permission") );
				}
				else
				if ( !checkBuyLimits(scale) )
				{
					locale.sendMessage(player, "trader-transaction-failed-limit");
				//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit") );
				}
				else
				if ( !sellTransaction(price*scale,event.getCurrentItem()) )
				{
					locale.sendMessage(player, "trader-transaction-failed-money");
				//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money") );
				}
				else
				{
					locale.sendMessage(player, "trader-transaction-success", "action", "#sold", "amount", String.valueOf(getSelectedItem().getAmount()), "price", f.format(price));
				//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );
					
					updateBuyLimits(scale);
					
					removeFromInventory(event.getCurrentItem(),event);
					
					//logging
					log("sell", 
						getSelectedItem().getItemStack().getTypeId(),
						getSelectedItem().getItemStack().getData().getData(), 
						getSelectedItem().getAmount()*scale, 
						price*scale );

					//sending a message to the traders owner
					this.messageOwner("sold", player.getName(), getSelectedItem(), 0);
				
				}
			}
			setInventoryClicked(false);
		}
		event.setCancelled(true);
	}
	
	

	@Override
	public void managerMode(InventoryClickEvent event) {

		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
		//Player p = (Player) event.getWhoClicked();
		DecimalFormat f = new DecimalFormat("#.##");
		
		int clickedSlot = event.getSlot();
		
		if ( clickedSlot < 0 )
		{
			event.setCursor(null);
			switchInventory(getBasicManageModeByWool());
			return;
		}
		
		if ( top ) 
		{
			setInventoryClicked(true);
			
			if ( isManagementSlot(clickedSlot, 3) ) 
			{
				//is white wool clicked
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(6)) )
				{
					
					//close any management mode, switch to the default buy/sell management
					if ( isSellModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_SELL);
					if ( isBuyModeByWool() )
						this.setTraderStatus(TraderStatus.MANAGE_BUY);

					switchInventory(getBasicManageModeByWool(), "manage");
					
					getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(2) );//new ItemStack(Material.WOOL,1,(short)0,(byte)15));
					getInventory().setItem(getInventory().getSize() - 3, ( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? itemsConfig.getItemManagement(4) : itemsConfig.getItemManagement(3) ) );//new ItemStack(Material.WOOL,1,(short)0,(byte)( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? 11 : 12 ) ));
					
					//send message
					locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
				//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
				}
				else
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(2)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.managing.price") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:price") );
					}
					else
					{
						//switch to price setting mode
						setTraderStatus(TraderStatus.MANAGE_PRICE);
						switchInventory(getBasicManageModeByWool(), "price");
						

						getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(6));
						getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
						

						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-price");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:price") );
					}
				}
				else
				// Only for buy system!
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(3)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.managing.buy-limits") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
					}
					else
					{
					//	switchManageInventory("limit", TraderStatus.MANAGE_LIMIT_GLOBAL);
						setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
						switchInventory(getBasicManageModeByWool(), "glimit");
						
						
						
						getInventory().setItem(getInventory().getSize() - 2, new ItemStack(Material.WOOL,1,(byte)0));
						getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
						
						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#buy-limit");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:buy-limit") );
					
					}
				}
				else
				// add a nice support to this system
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy") );
					}
					else
					{
						switchInventory(TraderStatus.MANAGE_BUY);
						
						getInventory().setItem(getInventory().getSize() - 1, itemsConfig.getItemManagement(0));
						getInventory().setItem(getInventory().getSize() - 3, itemsConfig.getItemManagement(3));
					
	
						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:buy") );
						
					}
				}
				else
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
				{
					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:sell") );
					}
					else
					{
						//switch to sell mode
						//status switching included in Inventory switch
						switchInventory(TraderStatus.MANAGE_SELL);
						
						
						
						getInventory().setItem(getInventory().getSize() - 1, itemsConfig.getItemManagement(1));
						getInventory().setItem(getInventory().getSize() - 3, itemsConfig.getItemManagement(4));
					//	getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
	
						//send message
						locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
					//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:sell") );
						
					}
				}
				else
				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) )	//unsupported wool data value
				{
					
					this.saveManagedAmounts();
					switchInventory(TraderStatus.MANAGE_SELL);
					//this.switchInventory(TraderStatus.MANAGE_SELL);
					
					getInventory().setItem(getInventory().getSize() - 1, itemsConfig.getItemManagement(1));
					

					//send message
					locale.sendMessage(player, "trader-manage-toggle", "mode", "#manage-stock");
				//	player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "manage:stock", "entity:player") );
				}
				
				//cancel the event, so no1 can take up wools and end
				event.setCancelled(true);
				return;
			}
			//items management 
			else
			{
				//shift click handling
				if ( event.isShiftClick() )
				{
					
					//we don't like shift click in the upper inventory ;)
					event.setCancelled(true);
					
					if ( isSellModeByWool() )
					{
						
						//but any shift click will remove an item from the traders stock ;> 
						//and return all the remaining amount to the player, (if he has enough space)
						if ( selectItem(clickedSlot, TraderStatus.MANAGE_SELL ).hasSelectedItem() ) 
						{
							
							if ( event.isLeftClick() )
							{
								
								//get the amount left in the stock
								int leftAmount = getSelectedItem().getLimits().get("global").getLimit() - limits.getLimit(this, "global", getSelectedItem()).getAmount();
								
								//check if the player has enough space
								if ( inventoryHasPlaceAmount(leftAmount) )
								{
								
									//remove that item from stock room
									if ( isBuyModeByWool() )
										getStock().removeItem("buy", clickedSlot);
									if ( isSellModeByWool() )
										getStock().removeItem("sell", clickedSlot);
									
									
									//add the remaining amount to the player
									addAmountToInventory(leftAmount);
									
									
									getInventory().setItem(clickedSlot, new ItemStack(0));
									
									
									//clear the selecton and message the player
									selectItem(null);

									//send message
									locale.sendMessage(player, "trader-stock-item-remove");
									locale.sendMessage(player, "trader-player-recover", "amount", String.valueOf(leftAmount));
								}
							} 
							else
							//if right clicked open the multiple amounts tab
							{
								if ( permissionsManager.has(player, "dtl.trader.managing.multiple-amounts") )
								{
									//inventory and status update
									switchInventory(getSelectedItem());
									setTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT); 
								}
								else
									locale.sendMessage(player, "error-nopermission");
							}
							
						}
						
					}
					//buy mode acts in another way
					else if ( isBuyModeByWool() )
					{
						
						if ( selectItem(clickedSlot, TraderStatus.MANAGE_BUY ).hasSelectedItem() ) 
						{
							//get the amount left in the stock
							int stockedAmount = limits.getLimit(this, "global", getSelectedItem()).getAmount();// getSelectedItem().getLimits().getGlobalAmount();
							
							//check if the player has enough space
							if ( inventoryHasPlaceAmount(stockedAmount) )
							{
							
								if ( event.isLeftClick() )
								{
									//remove that item from stock room
									if ( isBuyModeByWool() )
										getStock().removeItem("buy", clickedSlot);
									if ( isSellModeByWool() )
										getStock().removeItem("sell", clickedSlot);
									
									//clear the inventory
									getInventory().setItem(clickedSlot, new ItemStack(0));

									//send a remove message
									locale.sendMessage(player, "trader-stock-item-remove");
									locale.sendMessage(player, "trader-player-recover", "amount", String.valueOf(stockedAmount));
								}
								else
								{
									//send a item got amount message
									locale.sendMessage(player, "trader-player-recover", "amount", String.valueOf(stockedAmount));
									
									//reset the amount
									limits.getLimit(this, "global", getSelectedItem()).setAmount(0);
									//getSelectedItem().getLimits().getLimit("global").setA(0);
								}
									
								
								//add the remaining amount to the player
								addAmountToInventory(stockedAmount);
								
								
								//clear the selection and message the player
								selectItem(null);
	
							}
							
						}
						
					}
					return;
					
				}
				//sell management
				if ( equalsTraderStatus(getBasicManageModeByWool()) )
				{
					
					//if an item is right-clicked
					if ( event.isRightClick() ) 
					{
						if ( selectItem(event.getSlot(), getTraderStatus()).hasSelectedItem() )
						{
							if ( !permissionsManager.has(player, "dtl.trader.managing.stack-price") )
							{
								locale.sendMessage(player, "error-nopermission");
								selectItem(null);
								event.setCancelled(true);
								return;
							}
							//if it has the stack price change it back to "per-item" price
							if ( getSelectedItem().stackPrice() ) 
							{
								getSelectedItem().setStackPrice(false);
								locale.sendMessage(player, "key-value", "key", "#stack-price", "value", "#disabled");
							} 
							//change the price to a stack-price
							else
							{
								getSelectedItem().setStackPrice(true);
								locale.sendMessage(player, "key-value", "key", "#stack-price", "value", "#disabled");
							}
							
							
							NBTTagEditor.removeDescription(event.getCurrentItem());
							TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getManageLore(getSelectedItem(), getTraderStatus().name(), player));
							
						}
						
						//reset the selection
						selectItem(null);
						
						//cancel the event
						event.setCancelled(true);
						return;
					}
					event.setCancelled(true);
					locale.sendMessage(player, "trader-stock-invalid-action");
					return;
					
				}
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) )
				{
					
					//check the cursor (if nothing is held just show the items price)
					if ( event.getCursor().getType().equals(Material.AIR) ) {
						
						//select the item to get the information from, and show the price
						if ( selectItem(event.getSlot(), getBasicManageModeByWool()).hasSelectedItem() ) 
							locale.sendMessage(player, "key-value", "key", "#price", "value", f.format(getSelectedItem().getRawPrice()));
						
						
					} 
					//if some thing is held change the items price
					else
					{
							
						//select the item if it exists
						if ( selectItem(event.getSlot(), getBasicManageModeByWool()).hasSelectedItem() ) 
						{
							
							//if it's right clicked the lower the price, else rise it
							if ( event.isRightClick() ) 
								getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
							else
								getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
							

							NBTTagEditor.removeDescription(event.getCurrentItem());
							TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPriceLore(getSelectedItem(), 0, getBasicManageModeByWool().toString(), null, player));
							
							
							//show the new price
							locale.sendMessage(player, "key-change", "key", "#price", "value", f.format(getSelectedItem().getRawPrice()));
							
							
						}
						
						
					}
					
					//reset the selection
					selectItem(null);
					
					event.setCancelled(true);
					
					
				}
				else 
				//global limit as "item limit"
				if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) )
				{
					//show limits
					if ( event.getCursor().getType().equals(Material.AIR) )
					{
						
						//select item which limit will be shown up
						if ( selectItem(clickedSlot, getBasicManageModeByWool()).hasSelectedItem() ) 
						{
							locale.sendMessage(player, "key-value", "key", "#buy-limit", "value", String.valueOf(getSelectedItem().getLimits().limit("global")));
						}
						
						
					} 
					//change limits
					else 
					{
						
						//select the item
						if ( selectItem(clickedSlot, getBasicManageModeByWool()).hasSelectedItem() ) 
						{

							if ( getSelectedItem().getLimits().get("global") == null )
								getSelectedItem().getLimits().set("global", new Limit(0,-1));
							
							if ( event.isRightClick() ) 
								getSelectedItem().getLimits().get("global").changeLimit(-calculateLimit(event.getCursor()));
							else
								getSelectedItem().getLimits().get("global").changeLimit(calculateLimit(event.getCursor()));
							
							NBTTagEditor.removeDescription(event.getCurrentItem());
							TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getLimitLore(getSelectedItem(), getTraderStatus().name(), player));

							locale.sendMessage(player, "key-change", "key", "#buy-limit", "value", String.valueOf(getSelectedItem().getLimits().limit("global")));
						
						}

					}
					
					//reset the selected item
					selectItem(null);
					
					//cancel the event
					event.setCancelled(true);
				}
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) )
				{
					
				}
				//currently unsupported
				else 
				if ( equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
				{
					event.setCancelled(true);
					
					//left = +
					if ( event.isLeftClick() )
					{
						
						//add a new item in that slot (it will be either rearranged)
						if ( event.getCurrentItem().getType().equals(Material.AIR) )
						{
							ItemStack clonedStack = getSelectedItem().getItemStack().clone();

							getInventory().setItem(clickedSlot, clonedStack);
							event.setCancelled(false);

							locale.sendMessage(player, "trader-stock-item-add");
						//	player.sendMessage( localeManager.getLocaleString("xxx-item", "action:added") );

						}
						else
						{
							int addAmount = event.getCursor().getAmount();
							
							//if the cursor is empty
							if ( event.getCursor().getTypeId() == 0 )
								addAmount = 1;
							
							
							int oldAmount = event.getCurrentItem().getAmount();
							
							//add the amount
							if ( event.getCurrentItem().getMaxStackSize() < oldAmount + addAmount )
								event.getCurrentItem().setAmount(event.getCurrentItem().getMaxStackSize());
							else
								event.getCurrentItem().setAmount(oldAmount+addAmount);

							locale.sendMessage(player, "trader-stock-item-update");
						//	player.sendMessage( localeManager.getLocaleString("xxx-item", "action:updated") );
							
						}
						
						
					}
					//right = you know... -.-
					else
					{
						if ( event.getCurrentItem().getTypeId() == 0 )
						{
							return;
						}
						
						
						//get amount info
						int removeAmount = event.getCursor().getAmount();
						
						//if the cursor is empty
						if ( event.getCursor().getTypeId() == 0 )
							removeAmount = 1;
						
						int oldAmount = event.getCurrentItem().getAmount();
						
						//decrease the amount, or delete the item
						if ( oldAmount - removeAmount <= 0 )
						{
							locale.sendMessage(player, "trader-stock-item-remove");
						//	player.sendMessage( localeManager.getLocaleString("xxx-item", "action:removed") );
							event.setCurrentItem(new ItemStack(Material.AIR, 0));
						}
						else
						{
							locale.sendMessage(player, "trader-stock-item-update");
						//	player.sendMessage( localeManager.getLocaleString("xxx-item", "action:updated") );
							event.getCurrentItem().setAmount(oldAmount-removeAmount);
						}
					}
					
				 
				} 

			}
			
		}
		//bottom inventory management
		else 
		{
			if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE)
					|| equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL)
					|| equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
			{
				if ( event.isShiftClick() )
					event.setCancelled(true);
				
				
				return;
			}
			
			//cancel the event, bottom always canceled
			event.setCancelled(true);
			
			if ( hasSelectedItem() )
			{
				if ( event.getCursor().getTypeId() != 0 )
				{
					event.setCursor(null);
					selectItem(null);
					switchInventory(getBasicManageModeByWool());
				}
			
			}
				
			//if an item is left-clicked
			if ( event.isLeftClick() && event.getCurrentItem().getTypeId() != 0 )
			{
				//save the amount 
				int backUpAmount = event.getCurrentItem().getAmount();
				
				
				//get the item information
				ItemStack itemToAdd = event.getCurrentItem();
				itemToAdd.setAmount(1);
				
				
				this.selectItem(itemToAdd, getBasicManageModeByWool(), false, false);
				
				
				if ( hasSelectedItem() )
				{
					
					//message the player
					locale.sendMessage(player, "trader-player-item-invalid");
				//	player.sendMessage( localeManager.getLocaleString("item-in-stock") );
					
					
					//reset the selection and set the clicked inventory (false = bottom)
					itemToAdd.setAmount(backUpAmount);
					selectItem(null);
					setInventoryClicked(false);
					return;
				}
				
				
				//get the first empty item slot
				int firstEmpty = getInventory().firstEmpty();
				
				
				
				//just to be sure nothing will be out of the inventory range (-3 for managing)
				if ( firstEmpty >= 0 && firstEmpty < getInventory().getSize() - 3 )
				{
					
					
					//set the item to the inventory
					getInventory().setItem(firstEmpty, itemToAdd.clone());

					
					//change the item into the stock type
					StockItem stockItem = toStockItem(itemToAdd.clone());

					
					//pattern disabled always
					stockItem.setAsPatternItem(false);
					stockItem.setPatternPrice(false);
					
					
					//set the stock items slot
					stockItem.setSlot(firstEmpty);

					
					//set the limit system to 0/0/-2 (player empty configuration)
					Limits limitSystem = stockItem.getLimits();
					limitSystem.set("global", new Limit(0, -2));
					
					
					//put it into the stock list
					if ( isSellModeByWool() )
						getStock().addItem("sell", stockItem);
					if ( isBuyModeByWool() )
						getStock().addItem("buy", stockItem);
					
					
					itemToAdd.setAmount(backUpAmount);
					
					//send message
					locale.sendMessage(player, "trader-stock-item-add");
				}
				
				
			}
			else
			//if we are right clicking an item we will add the stock amount the trader will sell
			if ( event.getCurrentItem().getTypeId() != 0 )
			{
				//if it's not shift clicked it has no effect ;P
				if ( !event.isShiftClick() )
				{
					
					//reset the selection and set the clicked inventory (false = bottom)
					selectItem(null);
					setInventoryClicked(false);
					return;
				}

				if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) )
					return;

				
				
				//get the item we want to add
				ItemStack itemToAdd = event.getCurrentItem();
				
				
				//get the item if it exists in the inventory
				this.selectItem(itemToAdd, getBasicManageModeByWool(), false, false);
				
				
				//if it exist allow the event to occur (let the item disappear)
				if ( hasSelectedItem() ) 
				{
					
					//let the item disappear
					event.setCancelled(false);
					
					
					//get the items limit system
					Limits limitSystem = getSelectedItem().getLimits();
					
					
					//timeout set to no timeout checks (-2000 = it will never reset)
					limitSystem.get("global").setTimeout(-2000);
					
					
					int getItemsLeft = limitSystem.get("global").getLimit() - limits.getLimit(this, "global", getSelectedItem()).getAmount();
					if ( getItemsLeft < 0 )
						getItemsLeft = 0;
					
					//set the new limit (how many items can players buy)
					limitSystem.get("global").setLimit(getItemsLeft + itemToAdd.getAmount());

					//send message
					locale.sendMessage(player, "trader-player-add-amount");
				//	player.sendMessage( localeManager.getLocaleString("item-added-selling").replace("{amount}", itemToAdd.getAmount() + "").replace( ( itemToAdd.getAmount() != 1 ? "{ending}" : "{none}"), "s" ) );
					
					
					//set the amount to 0 to push it but don't change the top items amount 
					itemToAdd.setAmount(0);
					event.setCurrentItem(itemToAdd);
					
					
					//reset the amount
					limits.getLimit(this, "global", getSelectedItem()).setAmount(0);
					//limitSystem.setGlobalAmount(0);
				
					
					//reset
					selectItem(null);
				}
				else
				{
					//that item isn't in the stock
				//	player.sendMessage( localeManager.getLocaleString("item-not-in-stock") );
					locale.sendMessage(player, "trader-player-item-invalid");
					
				}
				
				
			}
			
			
		}
		
		setInventoryClicked(false);
	}
	
	public void messageOwner(String action, String buyer, StockItem item, int slot)
	{
		Player player = Bukkit.getPlayer(getConfig().getOwner());
		playerLog(getConfig().getOwner(), buyer, action, item, slot);
		
		if ( player == null )
			return;
	//	player.sendMessage( localeManager.getLocaleString("xxx-transaction-xxx-item-log", "entity:name", "transaction:"+action).replace("{name}", buyer).replace("{item}", item.getItemStack().getType().name().toLowerCase()).replace("{amount}", ""+item.getAmount(slot)) );
		
	}

	@Override
	public boolean onRightClick(Player player, TraderTrait trait, NPC npc) {
		
		if ( player.getGameMode().equals(GameMode.CREATIVE) 
				&& !permissionsManager.has(player, "dtl.trader.bypass.creative") )
		{
			locale.sendMessage(player, "error-nopermission-creative");
		//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-creative") );
			return false;
		}
		
		if ( player.getItemInHand().getTypeId() == itemsConfig.getManageWand().getTypeId() )
		{
			
			if ( !permissionsManager.has(player, "dtl.trader.bypass.managing") 
				&& !player.isOp() )
			{
				if ( !permissionsManager.has(player, "dtl.trader.options.manage") )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
				if ( !trait.getConfig().getOwner().equals(player.getName()) )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
					return false;
				}
			}
			
			if ( getTraderStatus().isManaging() )
			{
				switchInventory( getStartStatus(player) );
				locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
				//player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
				return true;
			}	

			locale.sendMessage(player, "managermode-enabled", "npc", npc.getFullName());
			//player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
			switchInventory(getManageStartStatus(player) );
			return true;
		}
		
		//DEscriptions for player items
		NBTTagEditor.removeDescriptions(player.getInventory());
		if ( !getTraderStatus().isManaging() )
			loadDescriptions(player, player.getInventory());

		player.openInventory(getInventory());
		return true;
	}

	@Override
	public EType getType() {
		return EType.PLAYER_TRADER;
	}


}
