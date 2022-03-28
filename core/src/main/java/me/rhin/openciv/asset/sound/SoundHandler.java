package me.rhin.openciv.asset.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import me.rhin.openciv.Civilization;
import me.rhin.openciv.asset.SoundEnum;
import me.rhin.openciv.asset.SoundEnum.SoundType;
import me.rhin.openciv.options.OptionType;
import me.rhin.openciv.shared.listener.EventHandler;
import me.rhin.openciv.shared.listener.Listener;
import me.rhin.openciv.ui.screen.AbstractScreen;
import me.rhin.openciv.ui.screen.ScreenEnum;

public class SoundHandler implements Listener {

	private HashMap<SoundType, ArrayList<MusicWrapper>> musicMap;
	private HashMap<SoundEnum, Sound> effectMap;

	private MusicWrapper currentMusic;
	private MusicWrapper currentAmbience;

	public SoundHandler() {

		this.musicMap = new HashMap<>();
		this.effectMap = new HashMap<>();
	}

	@EventHandler
	public void onSetScreen(ScreenEnum prevScreenEnum, ScreenEnum screenEnum) {
		if (screenEnum == ScreenEnum.TITLE && prevScreenEnum == ScreenEnum.IN_GAME) {
			// Stop playing ambient music & ingame music
			currentAmbience.stop();
			currentAmbience = null;
		}
	}

	public void loadSounds() {

		for (SoundEnum soundEnum : SoundEnum.values()) {

			String soundPath = "sound/" + soundEnum.getSoundType().name().toLowerCase() + "/"
					+ soundEnum.name().toLowerCase() + ".ogg";

			switch (soundEnum.getSoundType()) {
			case EFFECT:
				Sound effect = Civilization.getInstance().getAssetHandler().get(soundPath, Sound.class);
				effectMap.put(soundEnum, effect);
				break;
			default:
				Music music = Civilization.getInstance().getAssetHandler().get(soundPath, Music.class);
				addMusic(soundEnum.getSoundType(), new MusicWrapper(music, soundEnum.getVolume()));
				break;
			}

		}
	}

	private void addMusic(SoundType soundType, MusicWrapper musicWrapper) {
		if (!musicMap.containsKey(soundType))
			musicMap.put(soundType, new ArrayList<>());

		musicMap.get(soundType).add(musicWrapper);
	}

	public void playEffect(SoundEnum soundEnum) {

		if (soundEnum.getSoundType() != SoundType.EFFECT) {
			// TODO: Throw error
			return;
		}

		Sound sound = effectMap.get(soundEnum);

		long id = sound.play();

		sound.setVolume(id, Civilization.getInstance().getGameOptions().getInt(OptionType.EFFECTS_VOLUME) / 100F);
	}

	public void playTrackBySoundtype(SoundType soundType) {
		playTrackBySoundtype(Civilization.getInstance().getCurrentScreen(), soundType);
	}

	public void playTrackBySoundtype(AbstractScreen screen, SoundType soundType) {

		// if (screen instanceof TitleScreen && currentMusic != null)
		// return;

		ArrayList<MusicWrapper> musicTrack = musicMap.get(soundType);
		Random rnd = new Random();

		MusicWrapper rndSound = musicTrack.get(rnd.nextInt(musicTrack.size()));

		if (soundType == SoundType.AMBIENCE) {
			if (currentAmbience != null)
				currentAmbience.stop();
			currentAmbience = rndSound;

			rndSound.setVolume(Civilization.getInstance().getGameOptions().getInt(OptionType.AMBIENCE_VOLUME) / 100F);
		} else {
			if (currentMusic != null)
				currentMusic.stop();
			currentMusic = rndSound;

			rndSound.setVolume(Civilization.getInstance().getGameOptions().getInt(OptionType.MUSIC_VOLUME) / 100F);
		}

		rndSound.play();

		rndSound.setOnCompletionListener(new Music.OnCompletionListener() {
			@Override
			public void onCompletion(Music music) {
				playTrackBySoundtype(soundType);
			}
		});
	}

	public MusicWrapper getCurrentMusic() {
		return currentMusic;
	}

	public MusicWrapper getCurrentAmbience() {
		return currentAmbience;
	}
}
