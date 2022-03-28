package me.rhin.openciv.ui.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.NextTurnPacket;
import me.rhin.openciv.shared.packet.type.PlayerStatUpdatePacket;
import me.rhin.openciv.shared.packet.type.TurnTimeLeftPacket;
import me.rhin.openciv.shared.stat.Stat;
import me.rhin.openciv.shared.stat.StatLine;
import me.rhin.openciv.ui.label.CustomLabel;
import me.rhin.openciv.ui.window.type.statinfo.type.GoldStatInfoWindow;

public class StatusBar extends Actor implements Listener {

	// FIXME: Dispose these listeners properly.

	private Sprite sprite;

	private CustomLabel scienceDescLabel, heritageDescLabel, goldDescLabel, faithDescLabel, tradeDescLabel;
	private Sprite scienceIcon, heritageIcon, goldIcon, faithIcon, tradeIcon;
	private CustomLabel scienceLabel, hertiageLabel, goldLabel, faithLabel, tradeLabel;

	private CustomLabel turnsLabel;

	public StatusBar(float x, float y, float width, float height) {
		this.setPosition(x, y);
		this.setSize(width, height);
		this.sprite = TextureEnum.UI_STATUSBAR.sprite();

		this.scienceDescLabel = new CustomLabel("Science:");
		this.scienceIcon = TextureEnum.ICON_SCIENCE.sprite();
		scienceIcon.setSize(16, 16);
		this.scienceLabel = new CustomLabel("0");

		this.heritageDescLabel = new CustomLabel("Heritage:");
		this.heritageIcon = TextureEnum.ICON_HERITAGE.sprite();
		heritageIcon.setSize(16, 16);
		this.hertiageLabel = new CustomLabel("0");

		this.goldDescLabel = new CustomLabel("Gold:");
		this.goldIcon = TextureEnum.ICON_GOLD.sprite();
		goldIcon.setSize(16, 16);
		this.goldLabel = new CustomLabel("0");

		this.faithDescLabel = new CustomLabel("Faith:");
		this.faithIcon = TextureEnum.ICON_FAITH.sprite();
		faithIcon.setSize(16, 16);
		this.faithLabel = new CustomLabel("0");

		this.tradeDescLabel = new CustomLabel("Trade:");
		this.tradeIcon = TextureEnum.ICON_BARREL.sprite();
		tradeIcon.setSize(16, 16);
		this.tradeLabel = new CustomLabel("0/0");

		this.turnsLabel = new CustomLabel("Turns: 0 ");

		updatePositions();

		Civilization.getInstance().getEventManager().addListener(this);
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		if (sprite != null) {
			sprite.setSize(width, height);
			updatePositions();
		}
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		if (sprite != null) {
			sprite.setPosition(x, y);
			updatePositions();
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		sprite.draw(batch);

		scienceDescLabel.draw(batch, parentAlpha);
		heritageDescLabel.draw(batch, parentAlpha);
		goldDescLabel.draw(batch, parentAlpha);
		faithDescLabel.draw(batch, parentAlpha);
		tradeDescLabel.draw(batch, parentAlpha);

		scienceIcon.draw(batch);
		heritageIcon.draw(batch);
		goldIcon.draw(batch);
		faithIcon.draw(batch);
		tradeIcon.draw(batch);

		scienceLabel.draw(batch, parentAlpha);
		hertiageLabel.draw(batch, parentAlpha);
		goldLabel.draw(batch, parentAlpha);
		faithLabel.draw(batch, parentAlpha);
		tradeLabel.draw(batch, parentAlpha);

		turnsLabel.draw(batch, parentAlpha);
	}

	@EventHandler
	public void onPlayerStatUpdate(PlayerStatUpdatePacket packet) {
		// NOTE: WE use runnable here since we get weird libgdx sprite bugs when we
		// update the statline.
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {

				StatLine statLine = StatLine.fromPacket(packet);

				scienceLabel.setText("+" + statLine.getStatValue(Stat.SCIENCE_GAIN));

				float currentGold = (float) (Math.floor(statLine.getStatValue(Stat.GOLD) * 100F) / 100F);
				float gainedGold = (float) (Math.floor(statLine.getStatValue(Stat.GOLD_GAIN) * 100F) / 100F);

				goldLabel.setText("" + currentGold + "(" + (gainedGold < 0 ? "" : "+") + gainedGold + ")");

				hertiageLabel.setText("+" + (int) statLine.getStatValue(Stat.HERITAGE_GAIN) + "");

				float gainedFaith = statLine.getStatValue(Stat.FAITH_GAIN);

				faithLabel.setText("" + statLine.getStatValue(Stat.FAITH) + "(" + (gainedFaith < 0 ? "" : "+")
						+ gainedFaith + ")");

				tradeLabel.setText((int) statLine.getStatValue(Stat.TRADE_ROUTE_AMOUNT) + "/"
						+ (int) statLine.getStatValue(Stat.MAX_TRADE_ROUTES));

				updatePositions();
			}
		});
	}

	@EventHandler
	public void onNextTurn(NextTurnPacket packet) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				int turnTime = Civilization.getInstance().getGame().getTurnTime();

				// TODO: Do this better. We assume high numbers mean no limit.
				if (turnTime > 100000) {
					turnsLabel.setText("Turns: " + Civilization.getInstance().getGame().getTurn() + " ");
				} else
					turnsLabel.setText("Turns: " + Civilization.getInstance().getGame().getTurn() + "("
							+ Civilization.getInstance().getGame().getTurnTime() + "s)");
				updatePositions();
			}
		});
	}

	@EventHandler
	public void onTurnTimeLeft(TurnTimeLeftPacket packet) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				// TODO: Do this better. We assume high numbers mean no limit.
				if (packet.getTime() > 100000) {
					turnsLabel.setText("Turns: " + Civilization.getInstance().getGame().getTurn() + " ");
				} else
					turnsLabel.setText(
							"Turns: " + Civilization.getInstance().getGame().getTurn() + "(" + packet.getTime() + "s)");
				updatePositions();
			}
		});
	}

	@EventHandler
	public void onMouseHovered(float mouseX, float mouseY) {
		// Display gold info window
		if (mouseX > goldDescLabel.getX() && mouseX < (goldLabel.getX() + goldLabel.getWidth()))
			if (mouseY > goldDescLabel.getY() && mouseY < goldDescLabel.getY() + goldDescLabel.getHeight()) {
				Civilization.getInstance().getWindowManager()
						.addWindow(new GoldStatInfoWindow(mouseX + 15, mouseY - 150));
			}
	}

	private void updatePositions() {
		float x = getX();
		float y = getY();
		float originX = x + 3;

		scienceDescLabel.setPosition(originX, y + scienceDescLabel.getHeight() / 2);

		originX += scienceDescLabel.getWidth() + 5;
		scienceIcon.setPosition(originX, y + 1);

		originX += scienceIcon.getWidth() + 5;
		scienceLabel.setPosition(originX, y + scienceLabel.getHeight() / 2);

		originX += scienceLabel.getWidth() + 15;
		heritageDescLabel.setPosition(originX, y + heritageDescLabel.getHeight() / 2);

		originX += heritageDescLabel.getWidth() + 5;
		heritageIcon.setPosition(originX, y + 1);

		originX += heritageIcon.getWidth() + 5;
		hertiageLabel.setPosition(originX, y + hertiageLabel.getHeight() / 2);

		originX += hertiageLabel.getWidth() + 15;
		goldDescLabel.setPosition(originX, y + goldDescLabel.getHeight() / 2);

		originX += goldDescLabel.getWidth() + 5;
		goldIcon.setPosition(originX, y + 2);

		originX += goldIcon.getWidth() + 5;
		goldLabel.setPosition(originX, y + goldLabel.getHeight() / 2);

		originX += goldLabel.getWidth() + 15;
		faithDescLabel.setPosition(originX, y + faithDescLabel.getHeight() / 2);

		originX += faithDescLabel.getWidth() + 5;
		faithIcon.setPosition(originX, y + 1);

		originX += faithIcon.getWidth() + 5;
		faithLabel.setPosition(originX, y + faithLabel.getHeight() / 2);

		originX += faithLabel.getWidth() + 15;
		tradeDescLabel.setPosition(originX, y + tradeDescLabel.getHeight() / 2);

		originX += tradeDescLabel.getWidth() + 5;
		tradeIcon.setPosition(originX, y + 1);

		originX += tradeIcon.getWidth() + 5;
		tradeLabel.setPosition(originX, y + scienceLabel.getHeight() / 2);

		turnsLabel.setPosition(getWidth() - turnsLabel.getWidth() - 2, y + 5);
	}
}
