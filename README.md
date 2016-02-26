# Juju Real World Cqrs
The sample shows you how to implement a [Real World Cqrs sample](https://github.com/rucka/RealWorldCqrs) using [juju](https://github.com/brokersquare/juju) reactive cqrs library.

The sample demonstrates the following [juju](https://github.com/brokersquare/juju) features:

- Implement aggregates and sagas
- Exchange messages between aggregates with location trasparency
- How it's simple move domain from local to cluster
- Send commands to a rest api
- Persist aggregates domain events to kafka
- Consume events from Spark and produce denormalized views to elastic search
- Monitor metrics with Grafana and views with Kibana
- Run the application and all the infrastructure components (kafka, Elasticsearch, Kibana, Grafana) with docker compose

