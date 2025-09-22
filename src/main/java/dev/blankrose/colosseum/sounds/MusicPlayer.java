package dev.blankrose.colosseum.sounds;

import dev.blankrose.colosseum.Colosseum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Objects;
import java.util.function.Supplier;

public class MusicPlayer {
    public static class Track extends AbstractTickableSoundInstance {
        protected Track(SoundEvent sound) {
            super(sound, SoundSource.RECORDS, RandomSource.create(sound.hashCode()));
            this.looping = true;
        }

        @Override
        public void tick() {
            if (!active) {
                volume -= 0.025f;
                if (volume <= 0.05f) {
                    stop();
                    playing = null;
                }
            } else if (volume < 1.0f) {
                volume += 0.025f;
            }
        }

        @Override
        public boolean canPlaySound() {
            return playing == this;
        }
    }

    private static Track playing = null;
    private static SoundEvent target = null;
    private static boolean active = false;

    public static void select(Supplier<SoundEvent> new_target) {
        if (Objects.nonNull(target) && Objects.nonNull(new_target)
            && target.getLocation().equals(new_target.get().getLocation())) {
            return;
        } else if (Objects.isNull(new_target)) {
            stop();
            target = null;
        } else {
            final boolean was_playing = Objects.nonNull(playing);
            stop();
            target = new_target.get();
            if (was_playing) {
                play();
            }
        }
    }

    public static void play() {
        active = true;
        Minecraft.getInstance().getMusicManager().stopPlaying();
        if (Objects.nonNull(playing)
            || Objects.isNull(target)) {
            return;
        }
        playing = new Track(target);
        Minecraft.getInstance().getSoundManager().play(playing);
    }

    public static void stop() {
        if (Objects.nonNull(playing)) {
            active = false;
        }
    }
}
