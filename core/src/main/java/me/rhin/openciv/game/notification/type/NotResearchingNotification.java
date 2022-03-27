package me.rhin.openciv.game.notification.type;

import com.badlogic.gdx.graphics.g2d.Sprite;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.game.notification.AbstractNotification;
import me.rhin.openciv.game.notification.NotificationPriority;
import me.rhin.openciv.game.research.Technology;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.packet.type.SetCityOwnerPacket;
import me.rhin.openciv.ui.window.type.ResearchWindow;

public class NotResearchingNotification extends AbstractNotification {

	public NotResearchingNotification() {
		Civilization.getInstance().getEventManager().addListener(this);
	}

	@Override
	public void act() {
		// TODO: Prompt the player that they need to settle a city.
		if (Civilization.getInstance().getGame().getPlayer().getOwnedCities().size() < 1)
			return;

		Civilization.getInstance().getWindowManager().toggleWindow(new ResearchWindow());
	}

	@EventHandler
	public void onPickResearch(Technology tech) {
		Civilization.getInstance().getGame().getNotificationHanlder().removeNotification(this);
	}

	@EventHandler
	public void onSetCityOwner(SetCityOwnerPacket packet) {
		if (Civilization.getInstance().getGame().getPlayer().getOwnedCities().size() < 1)
			Civilization.getInstance().getGame().getNotificationHanlder().removeNotification(this);
	}

	@Override
	public String getName() {
		return "Not researching notification";
	}

	@Override
	public Sprite getIcon() {
		return TextureEnum.ICON_SCIENCE.sprite();
	}

	@Override
	public String getText() {
		return "You can research\na new technology.";
	}

	@Override
	public NotificationPriority getPriorityLevel() {
		return NotificationPriority.HIGH;
	}
}
