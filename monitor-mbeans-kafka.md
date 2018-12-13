

#Kafka-emitted metrics

#####Name,	MBean Name,	Description,	Metric-Type

- **UnderReplicatedPartitions**	_kafka.server:type=ReplicaManager, name=UnderReplicatedPartitions_	Number of unreplicated partitions	Resource: Availability

- **IsrShrinksPerSec/IsrExpandsPerSec**	kafka.server:type=ReplicaManager, name=IsrShrinksPerSec kafka.server:type=ReplicaManager,name=IsrExpandsPerSec	Rate at which the pool of in-sync replicas (ISRs) shrinks/expands	Resource: Availability

- **ActiveControllerCount**	kafka.controller:type=KafkaController, name=ActiveControllerCount	Number of active controllers in cluster	Resource: Error

- **OfflinePartitionsCount**	kafka.controller:type=KafkaController, name=OfflinePartitionsCount	Number of offline partitions	Resource: Availability

- **LeaderElectionRateAndTimeMs**	kafka.controller:type=ControllerStats, name=LeaderElectionRateAndTimeMs	Leader election rate and latency	Other

- **UncleanLeaderElectionsPerSec**	kafka.controller:type=ControllerStats, name=UncleanLeaderElectionsPerSec	Number of "unclean" elections per second	Resource: Error

- **TotalTimeMs**	kafka.network:type=RequestMetrics, name=TotalTimeMs,request={Produce|FetchConsumer|FetchFollower}	Total time (in ms) to serve the specified request (Produce/Fetch)	Work: Performance

- **PurgatorySize**	kafka.server:type=ProducerRequestPurgatory,name=PurgatorySize kafka.server:type=FetchRequestPurgatory,name=PurgatorySize	Number of requests waiting in producer purgatory Number of requests waiting in fetch purgatory	Other

- **BytesInPerSec BytesOutPerSec**	kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec	Aggregate incoming/outgoing byte rate	Work: Throughput



**UnderReplicatedPartitions**: In a healthy cluster, the number of in sync replicas (ISRs) should be exactly equal to the total number of replicas. If partition replicas fall too far behind their leaders, the follower partition is removed from the ISR pool, and you should see a corresponding increase in IsrShrinksPerSec. Since Kafka’s high-availability guarantees cannot be met without replication, investigation is certainly warranted should this metric value exceed zero for extended time periods.

**IsrShrinksPerSec/IsrExpandsPerSec**: The number of in-sync replicas (ISRs) for a particular partition should remain fairly static, the only exceptions are when you are expanding your broker cluster or removing partitions. In order to maintain high availability, a healthy Kafka cluster requires a minimum number of ISRs for failover. A replica could be removed from the ISR pool for a couple of reasons: it is too far behind the leader’s offset (user-configurable by setting the replica.lag.max.messages configuration parameter), or it has not contacted the leader for some time (configurable with the replica.socket.timeout.ms parameter). No matter the reason, an increase in IsrShrinksPerSec without a corresponding increase in IsrExpandsPerSec shortly thereafter is cause for concern and requires user intervention.The Kafka documentation provides a wealth of information on the user-configurable parameters for brokers.

**ActiveControllerCount**: The first node to boot in a Kafka cluster automatically becomes the controller, and there can be only one. The controller in a Kafka cluster is responsible for maintaining the list of partition leaders, and coordinating leadership transitions (in the event a partition leader becomes unavailable). If it becomes necessary to replace the controller, a new controller is randomly chosen by ZooKeeper from the pool of brokers. In general, it is not possible for this value to be greater than one, but you should definitely alert on a value of zero that lasts for more than a short period (< 1s) of time.

**OfflinePartitionsCount** (controller only): This metric reports the number of partitions without an active leader. Because all read and write operations are only performed on partition leaders, a non-zero value for this metric should be alerted on to prevent service interruptions. Any partition without an active leader will be completely inaccessible, and both consumers and producers of that partition will be blocked until a leader becomes available.

**LeaderElectionRateAndTimeMs**: When a partition leader dies, an election for a new leader is triggered. A partition leader is considered “dead” if it fails to maintain its session with ZooKeeper. Unlike ZooKeeper’s Zab, Kafka does not employ a majority-consensus algorithm for leadership election. Instead, Kafka’s quorum is composed of the set of all in-sync replicas (ISRs) for a particular partition. Replicas are considered in-sync if they are caught-up to the leader, which means that any replica in the ISR can be promoted to the leader.

**LeaderElectionRateAndTimeMs** reports the rate of leader elections (per second) and the total time the cluster went without a leader (in milliseconds). Although not as bad as UncleanLeaderElectionsPerSec, you will want to keep an eye on this metric. As mentioned above, a leader election is triggered when contact with the current leader is lost, which could translate to an offline broker.



