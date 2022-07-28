package com.imyvm.essential.systems.ptt;

import com.imyvm.hoki.nbt.NbtEnumValue;
import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class TrackData implements NbtPersistent {
    @NbtPersistentValue private long duration;
    @NbtPersistentValue private int periodId;
    @NbtPersistentValue private final NbtEnumValue<Status> status = new NbtEnumValue<>(Status.class);

    public TrackData(int duration, int periodId, Status status) {
        this.duration = duration;
        this.periodId = periodId;
        this.setStatus(status);
    }

    public TrackData() {
        this(0, 0, Status.NOT_MEET);
    }

    public void update(long msSinceLastUpdate, int periodId) {
        if (this.periodId != periodId) {
            this.periodId = periodId;
            this.duration = 0;
            this.setStatus(Status.NOT_MEET);
        }
        this.duration += msSinceLastUpdate;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getPeriodId() {
        return this.periodId;
    }

    public Status getStatus() {
        return this.status.get();
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }

    public enum Status {
        NOT_MEET,
        PENDING,
        EXPIRED,
        OBTAINED,
    }
}
