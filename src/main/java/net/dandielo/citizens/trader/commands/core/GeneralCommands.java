package net.dandielo.citizens.trader.commands.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.npc.NPC;
import net.dandielo.citizens.trader.CitizensTrader;
import net.dandielo.citizens.trader.commands.Command;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.types.Banker;
import net.dandielo.citizens.trader.types.Trader;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class GeneralCommands {
	
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	@Command(
	name = "trader",
	syntax = "",
	perm = "dtl.trader.commands",
	desc = "shows the current selected traders information",
	npc = false)
	public void trader(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		if ( npc == null )
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
		}
		else
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
			locale.sendMessage(sender, "key-value", "key", "#type", "value", "#" + npc.getType().toString() + "-trader");
			locale.sendMessage(sender, "key-value", "key", "#owner", "value", npc.getConfig().getOwner());
			if ( npc.getWallet() != null )
			locale.sendMessage(sender, "key-value", "key", "#wallet", "value", npc.getWallet().getType().toString());
		//	if ( npc.getStock().getPatterns() != null )
		//	locale.sendMessage(sender, "key-value", "key", "#pattern", "value", npc.getStock().getPattern().getName());
			
		}
	}
	
	@Command(
	name = "trader",
	syntax = "reload",
	perm = "dtl.trader.commands.reload",
	desc = "reloads the locale and the config file",
	npc = false)
	public void traderReload(CitizensTrader plugin, CommandSender sender, Trader npc, Map<String, String> args)
	{
		//reload the general config file
		plugin.reloadConfig();
		//reload the locale
		locale.load();
		//reload the item config file
		plugin.getItemConfig().reloadConfig();
	}
	
	@Command(
	name = "banker",
	syntax = "",
	perm = "dtl.banker.commands",
	desc = "shows the current selected bankers information",
	npc = false)
	public void banker(CitizensTrader plugin, CommandSender sender, Banker npc, Map<String, String> args)
	{
		if ( npc == null )
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion());
		}
		else
		{
			locale.sendMessage(sender, "plugin-command-message", "version", plugin.getDescription().getVersion(), "name", plugin.getName());
			locale.sendMessage(sender, "key-value", "key", "#type", "value", "#" + npc.getType().toString() + "-banker");		
		}
	}
	
	@Command(
	name = "banker",
	syntax = "reload",
	perm = "dtl.banker.commands.reload",
	desc = "reloads the locale and the config file",
	npc = false)
	public void bankerReload(CitizensTrader plugin, CommandSender sender, Banker npc, Map<String, String> args)
	{
		//reload the general config file
		plugin.reloadConfig();
		//reload the locale
		locale.load();
		//reload the item config file
		plugin.getItemConfig().reloadConfig();
	}
	
	//Type help command
	@Command(
	name = "banker",
	syntax = "help (page)",
	desc = "allows to get information about all banker commands",
	perm = "dtl.banker.commands.help",
	npc = false)
	public void bankerHelp(CitizensTrader plugin, CommandSender sender, NPC npc, Map<String, String> args)
	{
		List<Command> cmds = commands.get("banker");
		
		if ( cmds == null )
			sender.sendMessage(ChatColor.RED + "No commands are registered for this type");

		//Getting the page
		int page = 1;
		try
		{
			if ( args.containsKey("page") )
				page = Integer.parseInt(args.get("page"));
		} 
		catch (NumberFormatException e)
		{
			page = 1;
		}
		int overall = ( cmds.size() / 4 ) + (cmds.size() % 4 == 0 ? 0 : 1);
		
		if ( page < 1 || page > overall )
			page = 1;
		
		
		sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.YELLOW + "Banker commands, page: " + ChatColor.AQUA + page + ChatColor.YELLOW + "/" + ChatColor.DARK_AQUA + overall + ChatColor.GOLD + " ==");
		sender.sendMessage("");
		
		int i = 0;
		for ( Command cmd : cmds )
		{
			if ( i >= (page-1)*4 && i < (page)*4 )
			{
				sender.sendMessage(nameAndSyntax(cmd));
				sender.sendMessage(perm(cmd));
				sender.sendMessage(description(cmd));
				if ( !cmd.usage().isEmpty() )
					sender.sendMessage(usage(cmd));
				sender.sendMessage(npc(cmd));
				sender.sendMessage("");
			}
			++i;
		}
	}
	
	
	//Commands description holder
	private static Map<String, List<Command>> commands = new HashMap<String, List<Command>>(); 
	
	public static void registerCommandInfo(String type, Command command)
	{
		List<Command> list = commands.get(type);
		if ( list == null )
			list = new ArrayList<Command>();
		list.add(command);
		commands.put(type, list);
	}
	
	//Type help command
	@Command(
	name = "trader",
	syntax = "help {args}",
	desc = "allows to get information about all trader commands",
	perm = "dtl.trader.commands.help",
	npc = false)
	public void traderHelp(CitizensTrader plugin, CommandSender sender, NPC npc, Map<String, String> args)
	{
		List<Command> cmds = commands.get("trader");
		
		if ( cmds == null )
			sender.sendMessage(ChatColor.RED + "No commands are registered for this type");

		//Getting the page
		int page = 1;
		try
		{
			if ( args.containsKey("free") )
				page = Integer.parseInt(args.get("free"));
		} 
		catch (NumberFormatException e)
		{
			page = 1;
		}
		int overall = ( cmds.size() / 4 ) + (cmds.size() % 4 == 0 ? 0 : 1);
		
		if ( page < 1 || page > overall )
			page = 1;
		
		
		sender.sendMessage(ChatColor.GOLD + "== " + ChatColor.YELLOW + "Trader commands, page: " + ChatColor.AQUA + page + ChatColor.YELLOW + "/" + ChatColor.DARK_AQUA + overall + ChatColor.GOLD + " ==");
		sender.sendMessage("");
		
		int i = 0;
		for ( Command cmd : cmds )
		{
			if ( i >= (page-1)*4 && i < (page)*4 )
			{
				sender.sendMessage(nameAndSyntax(cmd));
				if ( args.containsKey("perm") )
					sender.sendMessage(perm(cmd));
				if ( args.containsKey("desc") )
				{
					sender.sendMessage(npc(cmd));
					sender.sendMessage(description(cmd));
				}
				if ( args.containsKey("usage") )
					if ( !cmd.usage().isEmpty() )
						sender.sendMessage(usage(cmd));
				sender.sendMessage("");
			}
			++i;
		}
	}
	
	private static String npc(Command cmd)
	{
		return ChatColor.GOLD + "Npc required: " + ChatColor.YELLOW + String.valueOf(cmd.npc());
	}
	
	private static String perm(Command cmd)
	{
		return ChatColor.GOLD + "Permission: " + ChatColor.YELLOW + cmd.perm();
	}

	private static String usage(Command cmd) {
		return cmd.usage().isEmpty() ? "" : ChatColor.GOLD + "Usage: " + ChatColor.YELLOW + cmd.usage();
	}
	
	private static String description(Command cmd) {
		return ChatColor.GOLD + "Description: " + ChatColor.YELLOW + ( cmd.desc().isEmpty() ? ChatColor.RED + "none" : cmd.desc() );
	}

	private static String nameAndSyntax(Command cmd)
	{
		return ChatColor.GOLD + "Command: " + ChatColor.YELLOW + cmd.name() + " " + cmd.syntax();
	}
}
