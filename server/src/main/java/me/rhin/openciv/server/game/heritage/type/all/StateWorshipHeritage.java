package me.rhin.openciv.server.game.heritage.type.all;

import org.java_websocket.WebSocket;

import me.rhin.openciv.server.Server;
import me.rhin.openciv.server.game.AbstractPlayer;
import me.rhin.openciv.server.game.city.City;
import me.rhin.openciv.server.game.city.building.type.Monument;
import me.rhin.openciv.server.game.heritage.Heritage;
import me.rhin.openciv.server.game.map.tile.Tile;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.SettleCityPacket;

public class StateWorshipHeritage extends Heritage implements Listener {

	public StateWorshipHeritage(AbstractPlayer player) {
		super(player);

		Server.getInstance().getEventManager().addListener(this);
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return "State Worship";
	}

	@Override
	public float getCost() {
		return 20;
	}

	@Override
	protected void onStudied() {
		// Add monument to all cities & future cities
		for (City city : player.getOwnedCities())
			if (!city.containsBuilding(Monument.class))
				city.addBuilding(new Monument(city));
	}

	@EventHandler
	public void onSettleCity(WebSocket conn, SettleCityPacket packet) {
		if (!isStudied())
			return;

		Tile tile = Server.getInstance().getMap().getTiles()[packet.getGridX()][packet.getGridY()];
		City city = tile.getCity();

		if (!city.getPlayerOwner().equals(player))
			return;

		city.addBuilding(new Monument(city));
	}
}
