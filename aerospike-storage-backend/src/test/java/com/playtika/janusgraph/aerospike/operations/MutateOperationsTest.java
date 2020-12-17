package com.playtika.janusgraph.aerospike.operations;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Value;
import com.aerospike.client.async.NioEventLoops;
import com.aerospike.client.reactor.AerospikeReactorClient;
import com.aerospike.client.reactor.IAerospikeReactorClient;
import com.playtika.janusgraph.aerospike.AerospikePolicyProvider;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import static com.playtika.janusgraph.aerospike.AerospikeTestUtils.AEROSPIKE_PROPERTIES;
import static com.playtika.janusgraph.aerospike.AerospikeTestUtils.getAerospikeClient;
import static com.playtika.janusgraph.aerospike.AerospikeTestUtils.getAerospikeConfiguration;
import static com.playtika.janusgraph.aerospike.AerospikeTestUtils.getAerospikeContainer;
import static java.util.Collections.singletonMap;

public class MutateOperationsTest {

    @ClassRule
    public static GenericContainer container = getAerospikeContainer();

    public static final String STORE_NAME = "testStore";
    public static final Value KEY = Value.get("testKey");
    public static final Value COLUMN_NAME = Value.get("column_name");
    public static final Value COLUMN_VALUE = Value.get(new byte[]{1, 2, 3});

    private NioEventLoops eventLoops = new NioEventLoops();
    private AerospikeClient client = getAerospikeClient(getAerospikeContainer(), eventLoops);
    private IAerospikeReactorClient reactorClient = new AerospikeReactorClient(client, eventLoops);

    private MutateOperations mutateOperations = new BasicMutateOperations(
            new AerospikeOperations("test", AEROSPIKE_PROPERTIES.getNamespace(),
                    client, reactorClient,
                    new AerospikePolicyProvider(getAerospikeConfiguration(container))));


    @Test
    public void shouldDeleteKeyIdempotentlyIfWal()  {
        //when
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, COLUMN_VALUE), true).block();

        //then
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, Value.NULL), true).block();

        //expect
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, Value.NULL), true).block();
    }

    @Test(expected = AerospikeException.class)
    public void shouldFailOnDeleteIfNotWal()  {
        //when
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, COLUMN_VALUE), false).block();

        //then
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, Value.NULL), false).block();

        //expect
        mutateOperations.mutate(STORE_NAME, KEY,
                singletonMap(COLUMN_NAME, Value.NULL), false).block();
    }

}
