package me.rhin.openciv.server.game.options;

public enum GameOptionType {
	MAP_LENGTH(3),
	TURN_LENGTH_OFFSET(-1),
	SHOW_OBSERVED_TILES(0),
	BARBARIAN_AMOUNT(4),
	CITY_STATE_AMOUNT(6);

	private int defaultValue;

	GameOptionType(int defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Integer getDefaultValue() {
		return defaultValue;
	}
}
