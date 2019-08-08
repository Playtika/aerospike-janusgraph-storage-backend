package com.playtika.janusgraph.aerospike;

import com.playtika.janusgraph.aerospike.operations.BasicOperations;
import com.playtika.janusgraph.aerospike.operations.FlakingLockOperations;
import com.playtika.janusgraph.aerospike.operations.FlakingMutateOperations;
import com.playtika.janusgraph.aerospike.operations.FlakingWriteAheadLogManager;
import com.playtika.janusgraph.aerospike.operations.LockOperations;
import com.playtika.janusgraph.aerospike.operations.MutateOperations;
import com.playtika.janusgraph.aerospike.transaction.TransactionalOperations;
import com.playtika.janusgraph.aerospike.transaction.WriteAheadLogManager;
import com.playtika.janusgraph.aerospike.utils.FixedClock;
import org.janusgraph.diskstorage.configuration.Configuration;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class FlakingAerospikeStoreManager extends AerospikeStoreManager {

    public static final AtomicBoolean failsAcquire = new AtomicBoolean(false);
    public static final AtomicBoolean failsUnlock = new AtomicBoolean(false);
    public static final AtomicBoolean failsMutate = new AtomicBoolean(false);
    public static final AtomicBoolean failsDeleteTransaction = new AtomicBoolean(false);

    public static final AtomicLong time = new AtomicLong();

    public FlakingAerospikeStoreManager(Configuration configuration) {
        super(configuration);
    }

    protected BasicOperations initOperations(Configuration configuration) {
        return new FlakingOperations(configuration);
    }

    public static void fixAll(){
        failsAcquire.set(false);
        failsUnlock.set(false);
        failsMutate.set(false);
        failsDeleteTransaction.set(false);
    }

    public static void breakAll(){
        failsAcquire.set(true);
        failsUnlock.set(true);
        failsMutate.set(true);
        failsDeleteTransaction.set(true);
    }

    private static class FlakingOperations extends BasicOperations {

        public FlakingOperations(Configuration configuration) {
            super(configuration);
        }

        protected TransactionalOperations initTransactionalOperations(
                Supplier<WriteAheadLogManager> writeAheadLogManager,
                Supplier<LockOperations> lockOperations,
                Supplier<MutateOperations> mutateOperations){
            return super.initTransactionalOperations(
                    () -> new FlakingWriteAheadLogManager(writeAheadLogManager.get(), failsDeleteTransaction),
                    () -> new FlakingLockOperations(lockOperations.get(), failsUnlock, failsAcquire),
                    () -> new FlakingMutateOperations(mutateOperations.get(), failsMutate)
            );
        }

        @Override
        protected Clock getClock(){
            return new FixedClock(time);
        }
    }
}
