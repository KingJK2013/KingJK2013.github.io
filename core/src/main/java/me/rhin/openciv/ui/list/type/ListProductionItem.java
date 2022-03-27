package me.rhin.openciv.ui.list.type;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.game.city.City;
import me.rhin.openciv.game.production.ProductionItem;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.shared.packet.type.CityStatUpdatePacket;
import me.rhin.openciv.shared.stat.Stat;
import me.rhin.openciv.ui.label.CustomLabel;
import me.rhin.openciv.ui.list.ContainerList;
import me.rhin.openciv.ui.list.ListObject;
import me.rhin.openciv.ui.window.type.ItemInfoWindow;

public class ListProductionItem extends ListObject implements Listener {

	private City city;
	private ProductionItem productionItem;
	private CustomLabel itemNameLabel;
	private CustomLabel itemTurnCostLabel;
	private CustomLabel productionModifierLabel;
	private Sprite itemIconSprite;
	private Sprite backgroundSprite;
	private Sprite hoveredBackgroundSprite;

	public ListProductionItem(final City city, final ProductionItem productionItem, ContainerList containerList,
			float width, float height) {
		super(width, height, containerList, "ProductionItem");

		this.city = city;
		this.productionItem = productionItem;
		this.backgroundSprite = TextureEnum.UI_GRAY.sprite();
		this.backgroundSprite.setSize(width, height);

		this.hoveredBackgroundSprite = TextureEnum.UI_DARK_GRAY.sprite();
		this.hoveredBackgroundSprite.setSize(width, height);

		itemIconSprite = productionItem.getTexture().sprite();
		itemIconSprite.setSize(32, 32);

		this.itemNameLabel = new CustomLabel(productionItem.getName());
		itemNameLabel.setSize(width, height);
		itemNameLabel.setAlignment(Align.topLeft);

		if (productionItem.getFaithCost() == -1) {
			this.itemTurnCostLabel = new CustomLabel((int) Math
					.ceil((productionItem.getProductionCost() / city.getStatLine().getStatValue(Stat.PRODUCTION_GAIN)))
					+ " Turns");
		} else {
			this.itemTurnCostLabel = new CustomLabel((int) productionItem.getFaithCost() + " Faith");
		}

		itemTurnCostLabel.setSize(width, height);
		itemTurnCostLabel.setAlignment(Align.bottomLeft);

		this.productionModifierLabel = new CustomLabel(
				"(+" + (int) (100 * Math.abs(productionItem.getProductionModifier())) + "%)");
		productionModifierLabel.setColor(Color.GREEN);
		productionModifierLabel.setSize(width, height);
		productionModifierLabel.setAlignment(Align.center);

		Civilization.getInstance().getEventManager().addListener(this);
	}

	@Override
	protected void onClicked(InputEvent event) {
		// city.getProducibleItemManager().requestSetProductionItem(productionItem);
		if (!Civilization.getInstance().getWindowManager().allowsInput())
			return;

		Civilization.getInstance().getWindowManager().addWindow(new ItemInfoWindow(city, productionItem));
	}

	@EventHandler
	public void onCityStatUpdate(CityStatUpdatePacket packet) {
		if (productionItem.getProductionCost() < 0)
			return;

		itemTurnCostLabel.setText((int) Math
				.ceil((productionItem.getProductionCost() / city.getStatLine().getStatValue(Stat.PRODUCTION_GAIN)))
				+ " Turns");

		this.productionModifierLabel
				.setText("(+" + (int) (100 * Math.abs(productionItem.getProductionModifier())) + "%)");
		productionModifierLabel.setSize(getWidth(), getHeight());
		productionModifierLabel.setAlignment(Align.center);
	}

	@Override
	public void clearListeners() {
		super.clearListeners();
		Civilization.getInstance().getEventManager().removeListener(this);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (hovered)
			hoveredBackgroundSprite.draw(batch);
		else
			backgroundSprite.draw(batch);
		itemIconSprite.draw(batch);
		itemNameLabel.draw(batch, parentAlpha);
		itemTurnCostLabel.draw(batch, parentAlpha);
		if (Math.abs(productionItem.getProductionModifier()) > 0)
			productionModifierLabel.draw(batch, parentAlpha);

		super.draw(batch, parentAlpha);
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		backgroundSprite.setPosition(x, y);
		hoveredBackgroundSprite.setPosition(x, y);
		itemIconSprite.setPosition(x + getWidth() - itemIconSprite.getWidth(), y);
		itemNameLabel.setPosition(x, y);
		itemTurnCostLabel.setPosition(x, y);
		productionModifierLabel.setPosition(x, y);
	}

	public ProductionItem getProductionItem() {
		return productionItem;
	}

	@Override
	public int compareTo(ListObject listObj) {
		ListProductionItem listProductionItem = (ListProductionItem) listObj;

		int productionItemCost = (int) productionItem.getProductionCost();

		if (productionItem.getFaithCost() > 0)
			productionItemCost = 10000000;

		int listProductionItemCost = (int) listProductionItem.getProductionItem().getProductionCost();

		if (listProductionItem.getProductionItem().getFaithCost() > 0)
			listProductionItemCost = 10000000;

		return Integer.compare(listProductionItemCost, productionItemCost);
	}
}
