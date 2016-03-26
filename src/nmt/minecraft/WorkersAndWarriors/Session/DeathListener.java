/**
 * 
 */
package nmt.minecraft.WorkersAndWarriors.Session;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import nmt.minecraft.WorkersAndWarriors.WorkersAndWarriorsPlugin;
import nmt.minecraft.WorkersAndWarriors.Config.PluginConfiguration;
import nmt.minecraft.WorkersAndWarriors.Scheduling.Scheduler;
import nmt.minecraft.WorkersAndWarriors.Session.GameSession.State;
import nmt.minecraft.WorkersAndWarriors.Team.Team;
import nmt.minecraft.WorkersAndWarriors.Team.WWPlayer.WWPlayer;

/**
 * This class handles all player deaths within WorkersAndWarriors.
 * @author williamfong
 *
 */
public class DeathListener implements Listener {
	
	private GameSession session;
	
	public DeathListener(GameSession session) {
		this.session = session;
		Bukkit.getPluginManager().registerEvents(this, WorkersAndWarriorsPlugin.plugin);
	}

	/**
	 * This method determines if the damage event is appropriate for<br />
	 * a player entity and checks if the damage is fatal and if the player<br />
	 * is even in the session
	 * @param e The damage event
	 */
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		// Check EntityType
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player p = (Player) e.getEntity();
		
		if (this.session == null) {
			// Listener was not correctly instantiated
			return;
		}
		
		//Check to see if session is active
		if (this.session.getState() != State.RUNNING) {
			// The player's session is currently not running
			// TODO may require additional behavior
			return;
		}
		
		// Check to see if damage is 'fatal'
		if (p.getHealth() - e.getDamage() < 1) {
			this.handleDeath(p, e);
			
			// If the killer was a Player entity, notify them
			if (e.getDamager() instanceof Player) {
				msgKiller((Player) e.getDamager(), e);
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 10, 1);
				((Player) e.getDamager()).playSound(p.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 10, 1);
			}
			
			// For gameplay, play audio for players
			
		}	
	}
	
	/**
	 * This method handles the death event.
	 * @param p
	 */
	private void handleDeath(Player p, EntityDamageByEntityEvent e) {
		// Player's should NOT 'die' by Minecraft definition, we 
		// want to handle death by our own rules
		e.setCancelled(true);
		
		//set player to intermediate state
		p.setGameMode(GameMode.SPECTATOR);
		
		
		// Obtain WW player for respawn behavior
		WWPlayer wPlayer = this.session.getPlayer(p);
		Team wTeam = this.session.getTeam(p);
		Respawn respawn = new Respawn(wPlayer, wTeam);
		respawn.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 40, 4));
		Scheduler.getScheduler().schedule(respawn, null, PluginConfiguration.config.getRespawnCooldown());
		
	}
	
	/**
	 * This method messages the killer
	 * TODO If additional behavior is required: e.g player score/stats, this method
	 * should be encapsulated with a larger method.
	 * @param killer The Player killer
	 * @param e The EntityDamageByEntityEvent
	 */
	private void msgKiller(Player killer, EntityDamageByEntityEvent e) {
		// Get victim name
		Player victim = (Player) e.getEntity();
		String vName = victim.getDisplayName();
		killer.sendMessage("Eliminated: " + vName);
	
	}
}
