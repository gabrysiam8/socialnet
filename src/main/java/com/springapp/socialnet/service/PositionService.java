package com.springapp.socialnet.service;

import com.springapp.socialnet.model.Position;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Values;
import org.springframework.stereotype.Service;

@Service
public class PositionService {

    private final Driver driver;

    public PositionService(Driver driver) {
        this.driver = driver;
    }

    public void addPosition(int compId, Position position) {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String statement = "MATCH (c:Company) WHERE ID(c)={compId} MERGE (p:Position { name: {posName} }) MERGE (c)-[r:LOOKS_FOR]->(p)";
                tx.run(statement, Values.parameters(
                        "compId", compId,
                        "posName", position.getName()));
                tx.success();
            }
        }
    }
}
