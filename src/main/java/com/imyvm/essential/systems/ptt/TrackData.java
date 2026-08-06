package com.imyvm.essential.systems.ptt;

import com.imyvm.hoki.nbt.NbtEnumValue;
import com.imyvm.hoki.nbt.NbtPersistent;
import com.imyvm.hoki.nbt.NbtPersistentValue;

public class TrackData implements NbtPersistent {
    @NbtPersistentValue private long duration;
    @NbtPersistentValue private int periodId;
    @NbtPersistentValue private final NbtEnumValue<Status> status = new NbtEnumValue<>(Status.class);
    @NbtPersistentValue private PendingBonus pendingBonus;

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

    public PendingBonus getPendingBonus() {
        return this.pendingBonus;
    }

    public PendingBonus replacePendingBonus(PendingBonus newPendingBonus) {
        PendingBonus previous = this.pendingBonus;
        this.pendingBonus = newPendingBonus;
        this.setStatus(Status.PENDING);
        return previous;
    }

    public PendingBonus finishPendingBonus(Status terminalStatus) {
        if (terminalStatus != Status.EXPIRED && terminalStatus != Status.OBTAINED)
            throw new IllegalArgumentException("Pending bonus must finish as expired or obtained");

        PendingBonus finished = this.pendingBonus;
        this.pendingBonus = null;
        if (finished != null && finished.getPeriodId() == this.periodId)
            this.setStatus(terminalStatus);
        return finished;
    }

    public boolean repairLegacyPendingStatus() {
        if (this.getStatus() != Status.PENDING || this.pendingBonus != null)
            return false;

        this.setStatus(Status.NOT_MEET);
        return true;
    }

    public PendingBonus clearInvalidPendingBonus() {
        PendingBonus invalid = this.pendingBonus;
        this.pendingBonus = null;
        if (invalid != null && invalid.getPeriodId() == this.periodId && this.getStatus() == Status.PENDING)
            this.setStatus(Status.NOT_MEET);
        return invalid;
    }

    public enum Status {
        NOT_MEET,
        PENDING,
        EXPIRED,
        OBTAINED,
    }
}
