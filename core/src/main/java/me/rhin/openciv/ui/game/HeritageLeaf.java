package me.rhin.openciv.ui.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.game.heritage.Heritage;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.CompleteHeritagePacket;
import me.rhin.openciv.ui.background.ColoredBackground;
import me.rhin.openciv.ui.label.CustomLabel;
import me.rhin.openciv.ui.window.type.PickHeritageWindow;

public class HeritageLeaf extends Group implements Comparable<HeritageLeaf>, Listener {

	// FIXME: Dispose these listeners properly

	private Heritage heritage;
	private ColoredBackground background;
	private ColoredBackground icon;
	private ColoredBackground heritageIcon;
	private CustomLabel heritageNameLabel;
	private Vector2 backVector;
	private Vector2 frontVector;
	private boolean positionUpdated;

	public HeritageLeaf(final Heritage heritage, float x, float y, float width, float height) {
		this.heritage = heritage;
		this.setTouchable(Touchable.enabled);
		this.setBounds(x, y, width, height);

		Sprite sprite = null;

		if (heritage.isStudied())
			sprite = TextureEnum.UI_GREEN.sprite();
		else if (heritage.ableToStudy())
			sprite = TextureEnum.UI_YELLOW.sprite();
		else
			sprite = TextureEnum.UI_RED.sprite();

		this.background = new ColoredBackground(sprite, 0, 0, width, height);
		addActor(background);

		this.icon = new ColoredBackground(heritage.getIcon(), 2, 6, 32, 32);
		addActor(icon);

		this.heritageIcon = new ColoredBackground(TextureEnum.ICON_HERITAGE.sprite(), width / 2 - 16 / 2, height - 18,
				16, 16);

		if (heritage.isStudying())
			addActor(heritageIcon);

		// FIXME: Setting the label to a height > 0 causes click input isses.
		this.heritageNameLabel = new CustomLabel(heritage.getName(), icon.getX() + icon.getWidth() + 3, height - 25,
				width, 0);
		addActor(heritageNameLabel);

		this.backVector = new Vector2(x, y + height / 2);
		this.frontVector = new Vector2(x + width, y + height / 2);

		addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {

				if (!Civilization.getInstance().getWindowManager().allowsInput(event.getListenerActor())
						|| !heritage.ableToStudy() || heritage.isStudied()) {
					return;
				}

				Civilization.getInstance().getWindowManager().addWindow(new PickHeritageWindow(heritage));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			}

		});

		Civilization.getInstance().getEventManager().addListener(this);
	}

	@EventHandler
	public void onCompleteHeritage(CompleteHeritagePacket packet) {

		if (!Civilization.getInstance().getGame().getPlayer().getHeritageTree()
				.getHeritageFromClassName(packet.getHeritageName()).equals(heritage)) {
			// Determine if others can be studied.

			if (heritage.ableToStudy() && !heritage.isStudied()) {
				background.setSprite(TextureEnum.UI_YELLOW.sprite());
				background.setBounds(0, 0, getWidth(), getHeight());
			} else if (!heritage.ableToStudy() && !heritage.isStudied()) {
				background.setSprite(TextureEnum.UI_RED.sprite());
				background.setBounds(0, 0, getWidth(), getHeight());
			}

			return;
		}

		background.setSprite(TextureEnum.UI_GREEN.sprite());
		background.setBounds(0, 0, getWidth(), getHeight());
		removeActor(heritageIcon);
	}

	@Override
	public int compareTo(HeritageLeaf otherLeaf) {
		return heritage.getLevel() - otherLeaf.getHeritage().getLevel();
	}

	public void setStudying(boolean studying) {
		if (studying)
			addActor(heritageIcon);
		else
			removeActor(heritageIcon);
	}

	public Heritage getHeritage() {
		return heritage;
	}
}
