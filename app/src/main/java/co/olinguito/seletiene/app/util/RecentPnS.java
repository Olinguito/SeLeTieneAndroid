package co.olinguito.seletiene.app.util;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.JSONObject;

public class RecentPnS extends CircularFifoQueue<JSONObject> {

    public static final int MAX_SIZE = 10;

    public RecentPnS() {
        super(MAX_SIZE);
    }
}
