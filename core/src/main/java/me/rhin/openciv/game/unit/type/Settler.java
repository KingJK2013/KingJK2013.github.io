package me.rhin.openciv.game.unit.type;

import java.util.Arrays;
import java.util.List;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.TextureEnum;
import me.rhin.openciv.events.type.UnitActEvent;
import me.rhin.openciv.game.city.City;
import me.rhin.openciv.game.map.tile.Tile;
import me.rhin.openciv.game.map.tile.TileType.TileProperty;
import me.rhin.openciv.game.unit.Unit;
import me.rhin.openciv.game.unit.UnitItem;
import me.rhin.openciv.game.unit.UnitParameter;
import me.rhin.openciv.game.unit.actions.AbstractAction;
import me.rhin.openciv.shared.packet.type.SettleCityPacket;

public class Settler extends UnitItem {

	public Settler(City city) {
		super(city);
	}

	public static class SettlerUnit extends Unit {
		public SettlerUnit(UnitParameter unitParameter) {
			super(unitParameter, TextureEnum.UNIT_SETTLER);

			customActions.add(new SettleAction(this));
		}

		@Override
		public float getMovementCost(Tile prevTile, Tile tile) {
			if (tile.containsTileProperty(TileProperty.WATER))
				return 1000000;
			else
				return tile.getMovementCost(prevTile);
		}

		@Override
		public boolean isUnitCapturable() {
			return true;
		}

		@Override
		public List<UnitType> getUnitTypes() {
			return Arrays.asList(UnitType.SUPPORT);
		}

		@Override
		public boolean canUpgrade() {
			return false;
		}
	}

	public static class SettleAction extends AbstractAction {

		public SettleAction(Unit unit) {
			super(unit);
		}

		@Override
		public boolean act(float delta) {
			unit.getPlayerOwner().unselectUnit();
			SettleCityPacket packet = new SettleCityPacket();
			packet.setLocation(unit.getStandingTile().getGridX(), unit.getStandingTile().getGridY());
			Civilization.getInstance().getNetworkManager().sendPacket(packet);
			// unit.removeAction(this);

			Civilization.getInstance().getEventManager().fireEvent(new UnitActEvent(unit));

			unit.removeAction(this);
			return true;
		}

		// Problem: We need to find a way to update this.
		@Override
		public boolean canAct() {
			if (unit.getCurrentMovement() < 1) {
				return false;
			}

			for (City city : Civilization.getInstance().getGame().getCities()) {
				float distX = Math.abs(unit.getTile().getGridX() - city.getTile().getGridX());
				float distY = Math.abs(unit.getTile().getGridY() - city.getTile().getGridY());

				if (distX + distY < 4)
					return false;
			}

			for (Tile tile : unit.getStandingTile().getAdjTiles())
				for (Tile adjTile : tile.getAdjTiles())
					if (adjTile.getTerritory() != null)
						return false;

			return true;
		}

		@Override
		public String getName() {
			return "Settle City";
		}

		@Override
		public TextureEnum getSprite() {
			return TextureEnum.ICON_SETTLE;
		}
	}

	@Override
	protected float getUnitProductionCost() {
		return 80;
	}

	@Override
	public float getGoldCost() {
		return 250;
	}

	@Override
	public boolean meetsProductionRequirements() {
		return true;
	}

	@Override
	public String getName() {
		return "Settler";
	}

	@Override
	public TextureEnum getTexture() {
		return TextureEnum.UNIT_SETTLER;
	}

	@Override
	public List<String> getDesc() {
		return Arrays.asList("Can settle new cities. Can be captured by enemy units.");
	}

	@Override
	public List<UnitType> getUnitItemTypes() {
		return Arrays.asList(UnitType.SUPPORT);
	}
}
