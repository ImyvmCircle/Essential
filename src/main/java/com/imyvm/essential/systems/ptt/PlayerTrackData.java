package com.imyvm.essential.systems.ptt;

import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class PlayerTrackData implements NbtPersistent {
    @NbtPersistentValue private final TrackData continuous = new TrackData();
    @NbtPersistentValue private final TrackData day = new TrackData();
    @NbtPersistentValue private final TrackData week = new TrackData();
    @NbtPersistentValue private final TrackData month = new TrackData();
    @NbtPersistentValue private final TrackData year = new TrackData();
    @NbtPersistentValue private final TrackData total = new TrackData();

    public TrackData getContinuous() {
        return this.continuous;
    }

    public TrackData getDay() {
        return this.day;
    }

    public TrackData getWeek() {
        return this.week;
    }

    public TrackData getMonth() {
        return this.month;
    }

    public TrackData getYear() {
        return this.year;
    }

    public TrackData getTotal() {
        return this.total;
    }
}
