package net.dandielo.citizens.trader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.TraderTrait.EType;
import net.dandielo.citizens.trader.events.TraderOpenEvent;
import net.dandielo.citizens.trader.events.TraderTransactionEvent;
import net.dandielo.citizens.trader.events.TraderTransactionEvent.TransactionResult;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.managers.PermissionsManager;
import net.dandielo.citizens.trader.objects.NBTTagEditor;
import net.dandielo.citizens.trader.types.Banker;
import net.dandielo.citizens.trader.types.MarketTrader;
import net.dandielo.citizens.trader.types.MoneyBanker;
import net.dandielo.citizens.trader.types.PlayerTrader;
import net.dandielo.citizens.trader.types.PrivateBanker;
import net.dandielo.citizens.trader.types.ServerTrader;
import net.dandielo.citizens.trader.types.Trader;
import net.dandielo.citizens.trader.types.tNPC;
import net.dandielo.citizens.trader.types.Trader.TraderStatus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class NpcManager implements Listener {
	//trader configs
	protected static ItemsConfig config = CitizensTrader.getInstance().getItemConfig();
	
	//managers
	protected static LocaleManager locale = CitizensTrader.getLocaleManager();
	private PermissionsManager permManager = CitizensTrader.getPermissionsManager();
	private Timer timer;
	
	//EconomyNpc
	private HashMap<String,tNPC> playerInteraction;
	private List<NPC> isEconomyNpc;
	
	public NpcManager() 
	{
		//initialize playerInteraction
		playerInteraction = new HashMap<String, tNPC>();
		//initialize the economyNpcList
		isEconomyNpc = new ArrayList<NPC>();
		initTimer();
	}
	
	public tNPC tNPC(CommandSender sender)
	{
		if ( sender instanceof Player )
			return playerInteraction.get(sender.getName());
		return null;
	}
	
	public void end() {
		timer.cancel();
	}
	
	public void initTimer() {
		if ( timer != null ) 
		{
			timer.cancel();
		}
		
		timer = new Timer("DtlDescription-Cleaner");
	}
	
	//check Npc
	public boolean isEconomyNpc(NPC npc) {
		return this.isEconomyNpc.contains(npc);
	}
	
	public List<NPC> getTraders(EType type)
	{
		List<NPC> traders =  new ArrayList<NPC>();
		for ( NPC npc : isEconomyNpc )
		{
			if ( npc.getTrait(TraderTrait.class).getType().equals(type) )
				traders.add(npc);
		}
		return traders;
	}
	
	public Trader traderByName(String name, Player player)
	{
		for ( NPC npc : isEconomyNpc )
		{
			TraderTrait trait = npc.getTrait(TraderTrait.class);
			if ( npc.getName().equals(name) )
				if ( trait.getType().equals(EType.SERVER_TRADER) )
					return new ServerTrader(trait, npc, player);
				else if ( trait.getType().equals(EType.MARKET_TRADER) )
					return new MarketTrader(trait, npc, player);
				else if ( trait.getType().equals(EType.PLAYER_TRADER) )
					return new PlayerTrader(trait, npc, player);
		}
		return null;
	}
	//Interaction
	public tNPC getInteractionNpc(String player) {
		if ( playerInteraction.containsKey(player) )
			return playerInteraction.get(player);
		return null;
	}
	//Interaction
	public void addInteractionNpc(String player, tNPC npc) {
		playerInteraction.put(player, npc);
	}
	//Interaction
	public void removeInteractionNpc(String player) {
		playerInteraction.remove(player);
	}
	
	protected void addEconomyNpc(NPC npc) {
		if ( !isEconomyNpc(npc) ) {
			this.isEconomyNpc.add(npc);
		}
	}	
	
	//cancel opening all other inventories when in mm
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		tNPC economyNpc = playerInteraction.get(event.getPlayer().getName());
		
		if ( economyNpc == null )
			return;
		
		if ( !economyNpc.locked() )
			return;

		if ( economyNpc.getInventory().getTitle().equals(event.getInventory().getTitle()) )
			return;
			
		if ( event.getInventory().getType().equals(InventoryType.PLAYER)
				|| event.getInventory().getType().equals(InventoryType.CRAFTING) )
			return;
		
		event.setCancelled(true);
		locale.sendMessage((Player)event.getPlayer(), "error-managermode-enabled");
	}
	
	
	//Events Handling!
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		//player licked the inventory
		if ( !( event.getWhoClicked() instanceof Player ) )
			return;
		
		//get the player
		Player p = (Player) event.getWhoClicked();
		
		//if creative inventory is open ignore this event
		if ( p.getGameMode().equals(GameMode.CREATIVE) 
				&&  event.getView().getType().equals(InventoryType.PLAYER) )
			return;
		
		//if normal inventory is open ignore this event
		if ( (p.getGameMode().equals(GameMode.SURVIVAL) 
				|| p.getGameMode().equals(GameMode.ADVENTURE) )
				&& event.getView().getType().equals(InventoryType.CRAFTING) )
			return;
		
		//Get the economy npc
		tNPC economyNpc = playerInteraction.get(p.getName());
		
		if ( economyNpc == null )
			return;
		
		
		//Npc Manager-mode
		if ( economyNpc.locked() )
		{ 
			economyNpc.managerMode(event);
			return;
		}
		if ( economyNpc instanceof Banker )
		{
			if ( ((Banker)economyNpc).getStatus().settings() ) 
			{
				economyNpc.settingsMode(event);
				return;			
			}
		}
		economyNpc.simpleMode(event);
	}
	
	
	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		
		
		tNPC economyNpc = playerInteraction.get(player.getName());
		//System.out.print(event.getPlayer().getInventory().getSize());
		
		if ( economyNpc == null )
			return;
		
		if ( economyNpc.locked() )
		{
			
			if ( economyNpc instanceof Trader )
				if ( ((Trader)economyNpc).getTraderStatus().equals(TraderStatus.MANAGE_SELL_AMOUNT) )
				{
					//cast to trader type, (it's save)
					((Trader)economyNpc).saveManagedAmounts();
					((Trader)economyNpc).switchInventory(TraderStatus.MANAGE_SELL);
					
					//reset the traders status
					((Trader)economyNpc).reset(TraderStatus.MANAGE);
				}
			
			return;
		}
		
		
		//remove the interaction
		playerInteraction.remove(player.getName());
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (  playerInteraction.get(player.getName()) == null )
					NBTTagEditor.removeDescriptions(event.getPlayer().getInventory());
			}
		};
		timer.schedule(task, 1000);
			
	}
	
	//block item dropping while managing
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = (Player) event.getPlayer();
		
		//get the trader we are trading with
		tNPC economyNpc = playerInteraction.get(player.getName());
		
		
		if ( economyNpc == null )
			return;
		
		
		if ( !economyNpc.locked() )
			return;
		
		
		//got permission?
		if ( this.permManager.has(player, "dtl.trader.bypass.drop-managing") )
			return;


		event.setCancelled(true);
	}
	
	
	//On npc-despawn
	@EventHandler
	public void onNPCDespawn(NPCDespawnEvent event)
	{
	}
	
	public HashSet<String> tempOpening = new HashSet<String>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onNPCRightCLick(NPCRightClickEvent event) {		
		if ( !isEconomyNpc(event.getNPC()) )
			return;
		

		final Player player = event.getClicker();
		
		if ( event.isCancelled() && !(player.isSneaking() && (player.isOp() || this.permManager.has(player, "dtl.trader.denizen.bypass")) ) )
			return;

		if ( tempOpening.contains(player.getName()) )
			return;
		
		tempOpening.add(player.getName());
		
		TimerTask task = new TimerTask()
		{
			@Override
			public void run() {
				tempOpening.remove(player.getName());
			}
		};
		
		timer.schedule(task, 1000);
		
		//used variables
		final String playerName = player.getName();
		NPC npc = event.getNPC();
		
		//EconomyNpc
		tNPC economyNpc = playerInteraction.get(playerName);


		//trader character
		TraderTrait characterTrait = npc.getTrait(TraderTrait.class);
		
		switch( characterTrait.getType() )
		{
			case SERVER_TRADER:
			{
				if ( economyNpc != null )
				{
					if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
					{
						locale.sendMessage(player, "error-nopermission");
						//player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
						
						tNPC newNpc = new ServerTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					
					}
				}
				else
				{
					tNPC newNpc = new ServerTrader(characterTrait, npc, player);
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
					
				}
				return;
			}
			case PLAYER_TRADER:
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
				{
					locale.sendMessage(player, "error-nopermission");
					//player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
						
						tNPC newNpc = new PlayerTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					}
				}
				else
				{
					tNPC newNpc = new PlayerTrader(characterTrait, npc, player);
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
				}
				return;
			}
			case MARKET_TRADER:
			{
				if ( !permManager.has(player, "dtl.trader.types." + characterTrait.getType().toString() ) )
				{
					locale.sendMessage(player, "error-nopermission");
					//player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					if ( economyNpc.getNpcId() == npc.getId() )
					{
						economyNpc.onRightClick(player, characterTrait, npc);
						
						if ( !economyNpc.locked() )
							playerInteraction.remove(playerName);
					}
					else
					{
						locale.sendMessage(player, "managermode-disabled", "npc", npc.getFullName());
						
						tNPC newNpc = new MarketTrader(characterTrait, npc, player);
						((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
						playerInteraction.put(playerName, newNpc);
						
						if ( !newNpc.onRightClick(player, characterTrait, npc) )
							playerInteraction.remove(playerName);
					}
				}
				else
				{
					tNPC newNpc = new MarketTrader(characterTrait, npc, player);
					((Trader)newNpc).switchInventory(Trader.getStartStatus(player));
					playerInteraction.put(playerName, newNpc);
					
					if ( !newNpc.onRightClick(player, characterTrait, npc) )
						playerInteraction.remove(playerName);
				}
				return;
			}
			case PRIVATE_BANKER:
			{

				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					
					player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
					
					tNPC newNpc = new PrivateBanker(npc, characterTrait.getBankTrait(), playerName);
					playerInteraction.put(playerName, newNpc);
				//	player.sendMessage( locale.getLocaleString("bank-deposit-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getDepositFee())) );
				//	player.sendMessage( locale.getLocaleString("bank-withdraw-fee").replace("{fee}", new DecimalFormat("#.##").format(banker.getWithdrawFee())) );
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				else
				{

					if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
					{
						locale.sendMessage(player, "error-nopermission");
					//	player.sendMessage( locale.getLocaleString("lacks-permissions") );
						return;
					}
					
					tNPC newNpc = new PrivateBanker(npc, characterTrait.getBankTrait(), playerName);
					
					playerInteraction.put(playerName, newNpc);
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			case MONEY_BANKER:
			{
				if ( !permManager.has(player, "dtl.banker.types." + characterTrait.getType().toString() ) )
				{
					locale.sendMessage(player, "error-nopermission");
				//	player.sendMessage( locale.getLocaleString("lacks-permissions") );
					return;
				}
				
				if ( economyNpc != null )
				{
					{
						player.sendMessage(ChatColor.AQUA + economyNpc.getNpc().getFullName() + ChatColor.RED + " exited the manager mode");
						
						tNPC newNpc = new MoneyBanker(npc, characterTrait, playerName);
						playerInteraction.put(playerName, newNpc);
						
						newNpc.onRightClick(player, characterTrait, npc);
						
					}
				}
				else
				{
					tNPC newNpc = new MoneyBanker(npc, characterTrait, playerName);
					playerInteraction.put(playerName, newNpc);
					
					newNpc.onRightClick(player, characterTrait, npc);
					
				}
				return;
			}
			default:
			{
				
			}
					
		}
		
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event)
	{
		NBTTagEditor.removeDescriptions(event.getPlayer().getInventory());
	}

}
