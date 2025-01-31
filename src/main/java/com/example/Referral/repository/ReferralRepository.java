package com.example.Referral.repository;
import com.example.Referral.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface ReferralRepository extends Neo4jRepository<UserNode, Long> {

    //построить полное дерево
    @Query("MATCH (user:UserNode {userId: $userId})-[:REFERRED*]->(descendants) RETURN user, descendants")
    List<UserNode> getFullTree();

    // Найти всех потомков для заданного пользователя
    @Query("MATCH (user:UserNode {userId: $userId})-[:REFERRED*]->(descendants) RETURN descendants")
    List<UserNode> getDescs(Long userId);

    // Найти всех предков для заданного пользователя
    @Query("MATCH (ancestors)-[:REFERRED*]->(user:UserNode {userId: $userId}) RETURN ancestors")
    List<UserNode> getAncs(Long userId);

    // Найти пользователя по реферальному коду
    @Query("MATCH (user:UserNode {refId: $refId}) RETURN user")
    UserNode getUserByRefId(String refId);

    //Найти предка для потомка с UID
    @Query("MATCH (ancestor:UserNode)-[:REFERRED]->(descendant:UserNode {refId: $value}) RETURN ancestor LIMIT 1")
    UserNode getAncByUidDesc(Long value);
}

