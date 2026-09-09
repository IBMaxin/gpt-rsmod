package com.elvarg.game.content.combat.method.impl.npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

public class ZulrahCombatMethod extends CombatMethod {

	private static final int MAX_ATTACKS_PER_FORM = 3;
	private CombatType currentForm = CombatType.MAGIC;
	private int attacksThisForm = 0;
	private static final Graphic MAGIC_PROJECTILE_GFX = new Graphic(264);
	private static final Graphic RANGE_PROJECTILE_GFX = new Graphic(260);
	private static final Projectile MAGIC_PROJECTILE = new Projectile(263, 31, 35, 40, 80);
	private static final Projectile RANGE_PROJECTILE = new Projectile(261, 31, 35, 40, 80);
	private static final int TOXIC_CLOUD_DELAY = 3;

	public ZulrahCombatMethod() {
		super();
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		return true;
	}

	@Override
		public PendingHit[] hits(Mobile character, Mobile target) {
			// Zulrah handles damage manually via projectiles + delayed tasks, so return null.
			return null;
		}

	@Override
	public void start(Mobile character, Mobile target) {
		if (!character.isNpc() || !target.isPlayer())
			return;
		NPC npc = character.getAsNpc();
		npc.performAnimation(new Animation(npc.getAttackAnim()));
		
		if (attacksThisForm >= MAX_ATTACKS_PER_FORM) {
			switchForm(character, target);
			attacksThisForm = 0;
		}

		if (currentForm == CombatType.MAGIC) {
			applyMagicAttack(npc, target);
		} else {
			applyRangeAttack(npc, target);
		}

		attacksThisForm++;
		npc.getTimers().register(TimerKey.COMBAT_ATTACK, 5);
	}

	private void switchForm(Mobile character, Mobile target) {
			currentForm = (currentForm == CombatType.MAGIC) ? CombatType.RANGED : CombatType.MAGIC;
			// Apply toxic cloud when switching forms (target may be the player)
			if (target != null && target.isPlayer()) {
				applyToxicCloud(character.getAsNpc(), target.getLocation());
			}
		}

	private void applyMagicAttack(NPC npc, Mobile target) {
		Location targetPos = target.getLocation();
		List<Location> attackPositions = new ArrayList<>();
		attackPositions.add(targetPos);
		for (int i = 0; i < 2; i++) {
			attackPositions.add(new Location((targetPos.getX() - 1) + Misc.getRandom(3),
					(targetPos.getY() - 1) + Misc.getRandom(3)));
		}
		for (Location pos : attackPositions) {
			Projectile.sendProjectile(npc, pos, MAGIC_PROJECTILE);
		}
		TaskManager.submit(new Task(4) {
			@Override
			public void execute() {
				for (Location pos : attackPositions) {
					target.getAsPlayer().getPacketSender().sendGlobalGraphic(MAGIC_PROJECTILE_GFX, pos);
					for (Player player : npc.getPlayersWithinDistance(10)) {
						if (player.getLocation().equals(pos)) {
							player.getCombat().getHitQueue()
									.addPendingDamage(new HitDamage(Misc.getRandom(25), HitMask.RED));
						}
					}
				}
				finished(npc, target);
				stop();
			}
		});
	}

	private void applyRangeAttack(NPC npc, Mobile target) {
		Location targetPos = target.getLocation();
		List<Location> attackPositions = new ArrayList<>();
		attackPositions.add(targetPos);
		for (int i = 0; i < 2; i++) {
			attackPositions.add(new Location((targetPos.getX() - 1) + Misc.getRandom(3),
					(targetPos.getY() - 1) + Misc.getRandom(3)));
		}
		for (Location pos : attackPositions) {
			Projectile.sendProjectile(npc, pos, RANGE_PROJECTILE);
		}
		TaskManager.submit(new Task(4) {
			@Override
			public void execute() {
				for (Location pos : attackPositions) {
					target.getAsPlayer().getPacketSender().sendGlobalGraphic(RANGE_PROJECTILE_GFX, pos);
					for (Player player : npc.getPlayersWithinDistance(10)) {
						if (player.getLocation().equals(pos)) {
							player.getCombat().getHitQueue()
									.addPendingDamage(new HitDamage(Misc.getRandom(15), HitMask.RED));
						}
					}
				}
				finished(npc, target);
				stop();
			}
		});
	}

	private void applyToxicCloud(NPC npc, Location playerLocation) {
		TaskManager.submit(new Task(TOXIC_CLOUD_DELAY) {
			@Override
			public void execute() {
				applyCloudDamage(npc, playerLocation);
				stop();
			}
		});
	}

	private void applyCloudDamage(NPC npc, Location playerLocation) {
		for (Player player : npc.getPlayersWithinDistance(3)) {
			if (player.getLocation().getDistance(playerLocation) <= 2) {
				player.getCombat().getHitQueue()
						.addPendingDamage(new HitDamage(Misc.getRandom(15), HitMask.RED));
			}
		}
	}

	@Override
	public int attackDistance(Mobile character) {
		return 7;
	}
	
	@Override
	public CombatType type() {
		return currentForm;
	}
}