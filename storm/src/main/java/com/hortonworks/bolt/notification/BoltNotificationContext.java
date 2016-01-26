package com.hortonworks.bolt.notification;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import com.hortonworks.iotas.notification.common.DefaultNotificationContext;
import com.hortonworks.iotas.notification.common.NotifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Notification context implementation specific to Bolt that tracks and acks or fails
 * the tuple.
 */
public class BoltNotificationContext extends DefaultNotificationContext {
    private final OutputCollector collector;
    private final ConcurrentHashMap<String, Tuple> tupleMap;
    private static final Logger LOG = LoggerFactory.getLogger(BoltNotificationContext.class);

    public BoltNotificationContext(OutputCollector collector, NotifierConfig config) {
        super(config);
        this.collector = collector;
        this.tupleMap = new ConcurrentHashMap<>();
    }

    void track(String notificationId, Tuple tuple) {
        LOG.debug("Tracking tuple {}, notification id {}", tuple, notificationId);
        tupleMap.putIfAbsent(notificationId, tuple);
    }

    @Override
    public void ack(String notificationId) {
        Tuple tuple = tupleMap.remove(notificationId);
        if(tuple != null) {
            LOG.debug("Acking tuple {}, notification id {}", tuple, notificationId);
            collector.ack(tuple);
        } else {
            throw new RuntimeException("Tracked tuple not found for notification id " + notificationId);
        }
    }

    @Override
    public void fail(String notificationId) {
        Tuple tuple = tupleMap.remove(notificationId);
        if(tuple != null) {
            LOG.debug("Failing tuple {}, notification id {}", tuple, notificationId);
            collector.fail(tuple);
        } else {
            throw new RuntimeException("Tracked tuple not found for notification id " + notificationId);
        }
    }

    @Override
    public String toString() {
        return "BoltNotificationContext{} " + super.toString();
    }
}