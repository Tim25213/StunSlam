package com.tim25213.shieldbreaker.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tim25213.shieldbreaker.ShieldBreaker;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShieldBreakerConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("shieldbreaker.json");

	public boolean enabled = true;
	public Mode mode = Mode.MANUAL;
	public boolean spearEnabled = false;
	public boolean counterLeft = true;
	public boolean counterRight = false;
	public int counterX = 4;
	public int counterY = 4;
	public int counterSize = 5;

	public static ShieldBreakerConfig load() {
		if (Files.exists(FILE)) {
			try {
				ShieldBreakerConfig loaded = GSON.fromJson(Files.readString(FILE), ShieldBreakerConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException | RuntimeException failure) {
				ShieldBreaker.LOGGER.warn("Falling back to defaults, could not read {}", FILE, failure);
			}
		}
		return new ShieldBreakerConfig();
	}

	public void save() {
		try {
			Files.createDirectories(FILE.getParent());
			Files.writeString(FILE, GSON.toJson(this));
		} catch (IOException failure) {
			ShieldBreaker.LOGGER.warn("Could not write {}", FILE, failure);
		}
	}

	public float counterScale() {
		return 0.5f + (counterSize - 1) * (1.5f / 9.0f);
	}
}
