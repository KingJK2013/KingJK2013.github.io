package me.rhin.openciv.server.game.heritage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.badlogic.gdx.utils.Json;

import me.rhin.openciv.server.Server;
import me.rhin.openciv.server.game.AbstractPlayer;
import me.rhin.openciv.server.game.heritage.type.all.CapitalDefenseHeritage;
import me.rhin.openciv.server.game.heritage.type.all.CapitalExpansionHeritage;
import me.rhin.openciv.server.game.heritage.type.all.StateWorshipHeritage;
import me.rhin.openciv.server.game.heritage.type.all.TaxesHeritage;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.CompleteHeritagePacket;
import me.rhin.openciv.shared.stat.Stat;

public class HeritageTree implements Listener {

	private LinkedHashMap<Class<? extends Heritage>, Heritage> values;
	private Heritage heritageStudying;
	private AbstractPlayer player;

	public HeritageTree(AbstractPlayer player) {
		this.player = player;
		this.values = new LinkedHashMap<>();

		Server.getInstance().getEventManager().addListener(this);
	}

	@EventHandler
	public void onNextTurn() {
		if (heritageStudying == null || !player.hasConnection())
			return;

		heritageStudying.applyHeritage(player.getStatLine().getStatValue(Stat.HERITAGE_GAIN));

		if (heritageStudying.getAppliedHeritage() >= heritageStudying.getCost()) {
			heritageStudying.setStudied(true);

			CompleteHeritagePacket packet = new CompleteHeritagePacket();
			packet.setName(heritageStudying.getClass().getName()
					.substring(heritageStudying.getClass().getName().indexOf("type.") + 5));

			Json json = new Json();
			player.sendPacket(json.toJson(packet));

			heritageStudying = null;
		}
	}

	public void initHeritage() {
		// Our default heritage values
		addHeritage(new CapitalExpansionHeritage(player));
		addHeritage(new StateWorshipHeritage(player));
		addHeritage(new TaxesHeritage(player));
		addHeritage(new CapitalDefenseHeritage(player));
	}

	public void addHeritage(Heritage heritage) {
		values.put(heritage.getClass(), heritage);
	}

	public List<Heritage> getAllHeritage() {
		// FIXME: This seems dumb
		return new ArrayList<>(values.values());
	}

	public <T extends Heritage> boolean hasStudied(Class<T> heritageClass) {

		if (values.get(heritageClass) == null)
			return false;

		return values.get(heritageClass).isStudied();
	}

	public Heritage getHeritage(Class<? extends Heritage> clazz) {
		return values.get(clazz);
	}

	public void studyHeritage(String className) {
		try {
			heritageStudying = values.get(Class.forName("me.rhin.openciv.server.game.heritage.type." + className));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Heritage> getStudiedHeritage() {

		ArrayList<Heritage> studiedHeritage = new ArrayList<>();

		for (Class<? extends Heritage> heritageClass : values.keySet()) {
			if (hasStudied(heritageClass))
				studiedHeritage.add(values.get(heritageClass));
		}

		return studiedHeritage;
	}
}
