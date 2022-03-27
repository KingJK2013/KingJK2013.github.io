package me.rhin.openciv.server.game.religion.bonus.type.founder;

import me.rhin.openciv.server.Server;
import me.rhin.openciv.server.game.city.City;
import me.rhin.openciv.server.game.religion.PlayerReligion;
import me.rhin.openciv.server.game.religion.bonus.ReligionBonus;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.stat.Stat;

/**
 * Church Property --- +2 Gold for every city with the religion as a majority
 * 
 * @author Ryan
 *
 */
public class ChurchPropertyBonus extends ReligionBonus implements Listener {

	public ChurchPropertyBonus() {
		Server.getInstance().getEventManager().addListener(this);
	}

	@Override
	public void onAssigned() {
		player.getReligion().getStatLine().addValue(Stat.GOLD_GAIN,
				player.getReligion().getFollowerCities().size() * 2);

		player.updateOwnedStatlines(false);
	}

	@EventHandler
	public void onCityLooseMajorityReligion(City city, PlayerReligion oldReligion) {

		if (player == null)
			return;

		if (oldReligion == null)
			return;

		if (!oldReligion.getPlayer().equals(player))
			return;

		player.getReligion().getStatLine().subValue(Stat.GOLD_GAIN, 2);
		player.updateOwnedStatlines(false);
	}

	@EventHandler
	public void onCityGainMajorityReligion(City city, PlayerReligion newReligion) {
		if (player == null)
			return;

		if (!newReligion.getPlayer().equals(player))
			return;

		player.getReligion().getStatLine().addValue(Stat.GOLD_GAIN, 2);
		player.updateOwnedStatlines(false);
	}

}
