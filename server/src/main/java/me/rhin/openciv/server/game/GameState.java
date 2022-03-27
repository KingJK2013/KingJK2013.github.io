package me.rhin.openciv.server.game;

import java.util.ArrayList;

import org.java_websocket.WebSocket;

import com.badlogic.gdx.utils.Json;

import me.rhin.openciv.server.Server;
import me.rhin.openciv.server.game.ai.AIPlayer;
import me.rhin.openciv.server.game.civilization.type.Barbarians;
import me.rhin.openciv.server.scenarios.Scenario;
import me.rhin.openciv.server.scenarios.ScenarioList;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.SendChatMessagePacket;

public abstract class GameState implements Listener {

	protected ArrayList<Player> players;
	protected ArrayList<AIPlayer> aiPlayers;
	private ScenarioList scenarioList;
	private ArrayList<Scenario> gameScenarios;

	public GameState() {
		players = Server.getInstance().getPlayers();
		aiPlayers = Server.getInstance().getAIPlayers();
		scenarioList = new ScenarioList();
		gameScenarios = new ArrayList<>();
		Server.getInstance().getEventManager().addListener(this);
	}

	@EventHandler
	public void onSendChatMessage(WebSocket conn, SendChatMessagePacket packet) {
		packet.setPlayerName(getPlayerByConn(conn).getName());

		if (packet.getMessage().startsWith("/")) {
			Server.getInstance().getCommandProcessor().proccessCommand(conn, packet, packet.getMessage().substring(1));
			return;
		}

		Json json = new Json();
		for (Player player : players) {
			player.sendPacket(json.toJson(packet));
		}
	}

	public abstract void onStateBegin();

	public abstract void onStateEnd();

	public abstract void stop();

	public abstract String toString();

	public AIPlayer getBarbarianPlayer() {
		for (AIPlayer aiPlayer : aiPlayers)
			if (aiPlayer.getCiv() instanceof Barbarians)
				return aiPlayer;

		return null;
	}

	public ArrayList<AIPlayer> getAIPlayers() {
		return aiPlayers;
	}

	public Player getPlayerByConn(WebSocket conn) {
		return Server.getInstance().getPlayerByConn(conn);
	}

	public ScenarioList getScenarioList() {
		return scenarioList;
	}

	public void addScenario(Scenario scenario) {
		gameScenarios.add(scenario);
		scenario.toggle();
	}

	public ArrayList<Scenario> getEnabledScenarios() {
		return gameScenarios;
	}
}
