package com.jcca.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description int型自增序列
 * @ClassName IntIdGenerator
 * @Date 2022/4/28 12:00
 * @Author hanwone
 * @Since 2.0.0.1
 */
public class IntIdGenerator {
    private int sequence = 0;
    private int laterSequence = 0;
    private int lastTimestamp = -1;
    private boolean isAdvance = false;
    private final MinuteCounter counter = new MinuteCounter();

    public synchronized int nextId() {
        int timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new IllegalArgumentException("Clock moved backwards.");
        }
        if (timestamp > counter.get()) {
            counter.set(timestamp);
            isAdvance = false;
        }

        // 2022-02-02分钟数
        int twepoch = 27395520;
        long seqBits = 7L;
        if (lastTimestamp == timestamp || isAdvance) {
            int sequenceMask = ~(-1 << seqBits);
            if (!isAdvance) {
                sequence = (sequence + 1) & sequenceMask;
            }

            if (sequence == 0) {
                isAdvance = true;
                int nextTimestamp = counter.get();
                if (laterSequence == 0) {
                    nextTimestamp = counter.incrementAndGet();
                }
                int nextId = (nextTimestamp - twepoch) << seqBits | laterSequence;
                laterSequence = (laterSequence + 1) & sequenceMask;
                return nextId;
            }
        } else {
            sequence = 0;
            laterSequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp - twepoch) << seqBits | sequence;
    }

    private static int timeGen() {
        String s = String.valueOf(System.currentTimeMillis() / 1000 / 60);
        return Integer.parseInt(s);
    }
}

class MinuteCounter {
    private final static int MASK = 0x7FFFFFFF;
    private final AtomicInteger atom;

    public MinuteCounter() {
        atom = new AtomicInteger(0);
    }

    public int incrementAndGet() {
        return atom.incrementAndGet() & MASK;
    }

    public int get() {
        return atom.get() & MASK;
    }

    public void set(int newValue) {
        atom.set(newValue & MASK);
    }
}