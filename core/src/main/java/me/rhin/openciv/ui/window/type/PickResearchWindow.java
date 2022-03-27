package me.rhin.openciv.ui.window.type;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Align;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.game.research.Technology;
import me.rhin.openciv.game.research.Unlockable;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.stat.Stat;
import me.rhin.openciv.ui.background.ColoredBackground;
import me.rhin.openciv.ui.button.CustomButton;
import me.rhin.openciv.ui.button.type.CloseWindowButton;
import me.rhin.openciv.ui.label.CustomLabel;
import me.rhin.openciv.ui.window.AbstractWindow;

public class PickResearchWindow extends AbstractWindow {

	private Technology tech;
	private ColoredBackground coloredBackground;
	private CustomLabel titleLabel;
	private ColoredBackground icon;
	private ArrayList<CustomButton> descButtons;
	private ArrayList<CustomLabel> descLabels;
	private CustomLabel descTitleLabel;
	private CustomLabel turnsLabel;
	private CustomButton pickResearchButton;
	private CloseWindowButton closeWindowButton;

	public PickResearchWindow(Technology tech) {
		super.setBounds(viewport.getWorldWidth() / 2 - 300 / 2, viewport.getWorldHeight() / 2 - 350 / 2, 300, 350);
		this.tech = tech;

		this.descButtons = new ArrayList<>();
		this.descLabels = new ArrayList<>();

		this.coloredBackground = new ColoredBackground(TextureEnum.UI_POPUP_BOX_A.sprite(), 0, 0, getWidth(),
				getHeight());
		addActor(coloredBackground);

		this.titleLabel = new CustomLabel("Research " + tech.getName(), Align.center, 0, getHeight() - 20, getWidth(),
				15);
		addActor(titleLabel);

		this.icon = new ColoredBackground(tech.getIcon(), getWidth() / 2 - 32 / 2, getHeight() - 55, 32, 32);
		addActor(icon);

		this.descTitleLabel = new CustomLabel("Enables The Following:", 15, getHeight() - 80, getWidth(), 15);
		addActor(descTitleLabel);

		this.pickResearchButton = new CustomButton("Research", 0, 5, 100, 35);
		pickResearchButton.onClick(() -> {
			if (tech.hasResearchedRequiredTechs()) {

				// Reset the tech queue if the player manually changes the tech.
				Civilization.getInstance().getGame().getPlayer().getResearchTree().clearTechQueue();

				tech.research();

			} else {
				Civilization.getInstance().getGame().getPlayer().getResearchTree()
						.setTechQueue(tech.getRequiedTechsQueue());

				// TODO: Reference tech leafs to represent a queued tech.
			}
		});
		addActor(pickResearchButton);

		this.closeWindowButton = new CloseWindowButton(this.getClass(), "Cancel", getWidth() - 100, 5, 100, 35);
		addActor(closeWindowButton);

		int index = 0;
		if (tech.getUnlockables() != null)
			for (Unlockable unlockable : tech.getUnlockables()) {
				CustomButton button = new CustomButton(TextureEnum.UI_BUTTON_ICON, TextureEnum.UI_BUTTON_ICON_HOVERED,
						unlockable.getTexture(), 5 + (50 * index), getHeight() - 135, 48, 48, 32, 32);
				button.onClick(() -> {
					Civilization.getInstance().getWindowManager().addWindow(new ItemInfoWindow(unlockable));
				});
				descButtons.add(button);
				addActor(button);
				index++;
			}

		// FIXME: Handle multiple lines...
		index = 0;
		for (String text : tech.getDesc()) {
			CustomLabel descLabel = new CustomLabel(text);
			descLabel.setWrap(true);
			descLabel.setWidth(getWidth() - 10);
			descLabel.setAlignment(Align.left);
			descLabel.pack();
			descLabel.setPosition(6, (getHeight() - 155) - descLabel.getHeight());
			descLabel.setWidth(getWidth() - 10);

			descLabels.add(descLabel);
			addActor(descLabel);
			index++;
		}

		int turns = (int) Math.ceil(tech.getScienceCost()
				/ Civilization.getInstance().getGame().getPlayer().getStatLine().getStatValue(Stat.SCIENCE_GAIN));

		if (tech.hasResearchedRequiredTechs()) {
			this.turnsLabel = new CustomLabel(turns + " Turns", Align.center, 0, 50, getWidth(), 15);

		} else {
			this.turnsLabel = new CustomLabel("Queue " + (tech.getRequiedTechsQueue().size() - 1) + " Technologies",
					Align.center, 0, 50, getWidth(), 15);

			ColoredBackground clockIcon = new ColoredBackground(TextureEnum.ICON_CLOCK.sprite(),
					pickResearchButton.getX() + pickResearchButton.getWidth() + 4, pickResearchButton.getY() + 1, 32,
					32);
			addActor(clockIcon);
		}

		addActor(turnsLabel);
	}

	@EventHandler
	public void onResize(int width, int height) {
		super.setPosition(width / 2 - 300 / 2, height / 2 - 350 / 2);
	}

	@Override
	public boolean disablesInput() {
		return true;
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
	public boolean closesGameDisplayWindows() {
		return false;
	}

	@Override
	public boolean isGameDisplayWindow() {
		return false;
	}

}
