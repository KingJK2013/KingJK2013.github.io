package me.rhin.openciv.server.game.diplomacy;

import java.util.ArrayList;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.utils.Json;

import me.rhin.openciv.server.Server;
import me.rhin.openciv.server.events.type.ServerDeclareWarEvent;
import me.rhin.openciv.server.game.AbstractPlayer;
import me.rhin.openciv.server.game.Player;
import me.rhin.openciv.server.game.civilization.type.Barbarians;
import me.rhin.openciv.server.game.diplomacy.actions.DiplomaticAction;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.Packet;
import me.rhin.openciv.shared.packet.type.DeclareWarPacket;
import me.rhin.openciv.shared.packet.type.DiscoveredPlayerPacket;

public class Diplomacy implements Listener {

	private final Logger LOGGER = LoggerFactory.getLogger(Diplomacy.class);

	private AbstractPlayer player;

	private ArrayList<Packet> queuedPackets;
	private ArrayList<AbstractPlayer> discoveredPlayers;
	private ArrayList<AbstractPlayer> enemies;
	private ArrayList<AbstractPlayer> allies;
	private ArrayList<DiplomaticAction> diplomaticActions;

	public Diplomacy(AbstractPlayer player) {
		this.player = player;

		this.discoveredPlayers = new ArrayList<>();
		this.enemies = new ArrayList<>();
		this.allies = new ArrayList<>();
		this.diplomaticActions = new ArrayList<>();
		this.queuedPackets = new ArrayList<>();

		Server.getInstance().getEventManager().addListener(this);
	}

	@EventHandler
	public void onDeclareWar(WebSocket conn, DeclareWarPacket packet) {

		AbstractPlayer attacker = Server.getInstance().getPlayerByConn(conn);

		if (!attacker.equals(player))
			return;

		AbstractPlayer defender = null;

		for (AbstractPlayer player : Server.getInstance().getAbstractPlayers()) {
			if (player.getName().equals(packet.getDefender()))
				defender = player;
		}

		declareWar(defender);

		Json json = new Json();
		for (Player player : Server.getInstance().getPlayers()) {
			player.sendPacket(json.toJson(packet));
		}
	}

	@Override
	public String toString() {
		String output = "";
		for (AbstractPlayer player : enemies) {
			output += "WAR: " + player.getName() + "\n";
		}

		return output;
	}

	public void declareWar(AbstractPlayer targetPlayer) {
		LOGGER.info(player.getName() + " declared war on " + targetPlayer.getName());

		addEnemy(targetPlayer);
		targetPlayer.getDiplomacy().addEnemy(player);

		Server.getInstance().getEventManager().fireEvent(new ServerDeclareWarEvent(player, targetPlayer));
	}

	public void declarWarAll() {
		for (AbstractPlayer otherPlayer : Server.getInstance().getAbstractPlayers()) {
			if (player.equals(otherPlayer))
				continue;

			declareWar(otherPlayer);
		}
	}

	public void addEnemy(AbstractPlayer otherPlayer) {
		enemies.add(otherPlayer);
	}

	public boolean atWar(AbstractPlayer otherPlayer) {
		return enemies.contains(otherPlayer);
	}

	public boolean inWar() {
		for (AbstractPlayer enemy : enemies) {
			if (enemy instanceof AbstractPlayer && !(enemy.getCiv() instanceof Barbarians))
				return true;
		}
		return false;
	}

	public ArrayList<AbstractPlayer> getDiscoveredPlayers() {
		return discoveredPlayers;
	}

	public ArrayList<AbstractPlayer> getEnemies() {
		return enemies;
	}

	public void addDiscoveredPlayer(AbstractPlayer discoveredPlayer) {
		discoveredPlayers.add(discoveredPlayer);

		DiscoveredPlayerPacket packet = new DiscoveredPlayerPacket();
		packet.setPlayers(player.getName(), discoveredPlayer.getName());

		if (!player.isLoaded()) {
			queuedPackets.add(packet);
			return;
		}

		Json json = new Json();
		for (Player player : Server.getInstance().getPlayers()) {
			player.sendPacket(json.toJson(packet));
		}
	}

	public void sendQueuedPackets() {
		Json json = new Json();

		for (Packet packet : queuedPackets) {
			for (Player player : Server.getInstance().getPlayers()) {
				player.sendPacket(json.toJson(packet));
			}
		}
	}

}
