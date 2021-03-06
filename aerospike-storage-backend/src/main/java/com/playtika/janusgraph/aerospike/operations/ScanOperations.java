package com.playtika.janusgraph.aerospike.operations;

import org.janusgraph.diskstorage.keycolumnvalue.KeyIterator;
import org.janusgraph.diskstorage.keycolumnvalue.SliceQuery;
import org.janusgraph.diskstorage.keycolumnvalue.StoreTransaction;

public interface ScanOperations {

    KeyIterator getKeys(String storeName, SliceQuery query, StoreTransaction txh);

}
