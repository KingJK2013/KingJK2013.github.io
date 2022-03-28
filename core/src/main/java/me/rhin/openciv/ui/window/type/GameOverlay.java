package me.rhin.openciv.ui.window.type;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.game.map.tile.Tile;
import me.rhin.openciv.game.map.tile.Tile.TileTypeWrapper;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.packet.type.TileStatlinePacket;
import me.rhin.openciv.shared.stat.Stat;
import me.rhin.openciv.shared.stat.StatLine;
import me.rhin.openciv.ui.background.ColoredBackground;
import me.rhin.openciv.ui.game.StatusBar;
import me.rhin.openciv.ui.label.CustomLabel;
import me.rhin.openciv.ui.window.AbstractWindow;

public class GameOverlay extends AbstractWindow {

	public static final int HEIGHT = 20;

	private CustomLabel fpsLabel;
	private CustomLabel tileNameLabel;
	private StatusBar statusBar;
	private Tile hoveredTile;
	private ArrayList<ColoredBackground> statIcons;
	private ArrayList<CustomLabel> statLabels;
	private CustomLabel geographyNameLabel;

	public GameOverlay() {
		this.statusBar = new StatusBar(0, viewport.getWorldHeight() - HEIGHT, viewport.getWorldWidth(), HEIGHT);
		addActor(statusBar);

		this.fpsLabel = new CustomLabel("FPS: 60.0");
		fpsLabel.setPosition(viewport.getWorldWidth() - fpsLabel.getWidth() - 4,
				viewport.getWorldHeight() - fpsLabel.getHeight() - statusBar.getHeight() - 5);
		this.addActor(fpsLabel);

		this.tileNameLabel = new CustomLabel("Grass");
		tileNameLabel.setBounds(2, 2, 0, 15); // FIXME: Setting the width to 0 is a workaround
		this.addActor(tileNameLabel);

		this.geographyNameLabel = new CustomLabel("N/A");
		geographyNameLabel.setBounds(2, 16, 0, 15); // FIXME: Setting the width to 0 is a workaround
		this.addActor(geographyNameLabel);

		this.statIcons = new ArrayList<>();
		this.statLabels = new ArrayList<>();
	}

	@EventHandler
	public void onResize(int width, int height) {
		statusBar.setSize(width, HEIGHT);
		statusBar.setPosition(0, height - HEIGHT);

		fpsLabel.setPosition(width - fpsLabel.getWidth() - 4,
				height - fpsLabel.getHeight() - statusBar.getHeight() - 5);
	}

	@EventHandler
	public void onRecieveTileStatline(TileStatlinePacket packet) {
		clearTileStatInfo();

		geographyNameLabel.setText(packet.getGeographyName());

		StatLine statline = StatLine.fromPacket(packet);

		ArrayList<Stat> statList = new ArrayList<>();
		statList.addAll(statline.getStatValues().keySet());

		// Put morale at the last part of the set.
		if (statList.contains(Stat.MORALE_TILE)) {
			statList.remove(Stat.MORALE_TILE);
			statList.add(Stat.MORALE_TILE);
		}

		float addedWidth = 0;
		for (Stat stat : statList) {
			if (stat == Stat.MAINTENANCE)
				continue;

			String name = null;
			if (stat.name().contains("_")) {
				name = stat.name().substring(0, stat.name().indexOf('_')).toUpperCase();
			} else {
				name = stat.name().toUpperCase();
			}

			float tileNameWidthOffset = tileNameLabel.getX() + tileNameLabel.getWidth() + 6;

			CustomLabel label = new CustomLabel("+" + (int) statline.getStatValue(stat));
			label.setPosition(tileNameWidthOffset + addedWidth, 2);
			addedWidth += label.getWidth();
			addActor(label);
			statLabels.add(label);

			Sprite sprite = TextureEnum.valueOf("ICON_" + name).sprite();
			ColoredBackground statIcon = new ColoredBackground(sprite, tileNameWidthOffset + addedWidth, 0, 16, 16);
			addedWidth += statIcon.getWidth();
			addActor(statIcon);
			statIcons.add(statIcon);
		}
	}

	@Override
	public boolean disablesInput() {
		return false;
	}

	@Override
	public boolean disablesCameraMovement() {
		return false;
	}

	@Override
	public boolean closesOtherWindows() {
		return false;
	}

	@Override
	public boolean isGameDisplayWindow() {
		return false;
	}

	@Override
	public boolean closesGameDisplayWindows() {
		return false;
	}

	public void setFPSText(String text) {
		fpsLabel.setText(text);
		fpsLabel.setPosition(viewport.getWorldWidth() - fpsLabel.getWidth() - 4,
				viewport.getWorldHeight() - fpsLabel.getHeight() - statusBar.getHeight() - 5);
	}

	public float getTopbarHeight() {
		return statusBar.getHeight();
	}

	public void setHoveredTile(Tile tile) {
		if (!tile.isDiscovered() && Civilization.SHOW_FOG) {
			tileNameLabel.setText("Undiscovered");
			geographyNameLabel.setText("");
			// tileNameLabel.setText("[" + tile.getGridX() + "," + tile.getGridY() + "]
			// Undiscovered");
			// tileNameLabel.setSize(0, 15);
			clearTileStatInfo();
			return;
		}

		if (tile.equals(hoveredTile))
			return;

		clearTileStatInfo();

		this.hoveredTile = tile;

		// [grass,copper]
		String tileName = "[" + tile.getGridX() + "," + tile.getGridY() + "] ";
		// String tileName = "";
		int index = 0;
		for (TileTypeWrapper typeWrapper : tile.getTileTypeWrappers()) {

			tileName += typeWrapper.getTileType().getName();

			if (index < tile.getTileTypeWrappers().size() - 1) {
				tileName += ", ";
			}
			index++;
		}

		tileNameLabel.setText(tileName);
		// tileNameLabel.setSize(0, 15);

		// TODO: Make this more optimized.
		// Only send a request after 100ms hovered on tile

		TileStatlinePacket packet = new TileStatlinePacket();
		packet.setTile(tile.getGridX(), tile.getGridY());
		Civilization.getInstance().getNetworkManager().sendPacket(packet);
	}

	private void clearTileStatInfo() {
		for (ColoredBackground statIcon : new ArrayList<>(statIcons)) {
			statIcon.addAction(Actions.removeActor());
		}

		for (CustomLabel label : new ArrayList<>(statLabels)) {
			label.addAction(Actions.removeActor());
		}

		statIcons.clear();
		statLabels.clear();
	}
}
