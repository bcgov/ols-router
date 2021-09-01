# Overview
1. export data from another cassandra cluster.
2. import the data into your cassandra cluster.

## Export
1. connect to the terminal of one of the cassandra nodes.
2. start the `cqlsh`
3. set the context to the `bgeo` keyspace
4. run the following query the relevant table
   ```SQL
   COPY bgeo_configuration_parameters
     TO '/tmp/bgeo_configuration_parameters.csv'
   WITH HEADER = TRUE AND DELIMITER = '^';
   ```
   This exports the data to CSV in the pod.

# Copy the CSV export to the new cluster
1. copy the data locally
   ```bash
   oc cp cassandra-0/tmp/bgeo_configuration_parameters.csv . -n ${NS}
   ```
2. Copy the data to a cassandra node in the desired cluster
   ```bash
   oc cp bgeo_configuration_parameters.csv cassandra-0/tmp/ -n ${OTHER_NS}
   ```

## Import
1. connect to the terminal of one of the cassandra nodes.
2. start the `cqlsh`
3. set the context to the `bgeo` keyspace
4. run the following query the relevant table
   ```SQL
   COPY bgeo_configuration_parameters
   FROM '/tmp/bgeo_configuration_parameters.csv'
   WITH HEADER = TRUE AND DELIMITER = '^';
   ```
   This imports the data to CSV in the pod.
