package com.example.Referral.repository;
import com.example.Referral.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferralRepository extends Neo4jRepository<UserNode, Long> {

    // создание пользователя
    @Query("CREATE (newUser:UserNode {userId: $userId, referralCode: $referralCode})")
    void saveWithoutReferral(
            @Param("userId") Long userId,
            @Param("referralCode") String referralCode
    );


    // Для пользователей c реферальным кодом
    @Query("MATCH (referrer:UserNode {referralCode: $invitedByCode}) " +
            "CREATE (newUser:UserNode {userId: $userId, referralCode: $referralCode}) " + // referrerCode не храним!
            "CREATE (newUser)-[:REFERRED_BY]->(referrer) " +
            "RETURN newUser")
    UserNode saveWithReferral(
            @Param("userId") Long userId,
            @Param("referralCode") String referralCode,
            @Param("invitedByCode") String invitedByCode // Используется только для поиска реферера
    );


    @Query("MATCH path = (User:UserNode {userId: $userId})-[:REFERRED_BY*1..6]->(:UserNode) " +
    "WITH path " +
    "ORDER BY length(path) DESC " +
    "LIMIT 1 " +
    "RETURN [node IN nodes(path)[1..] | node] AS ancestors")
    List<UserNode> getParentsForUserByUID (@Param("userId") Long userId);

    //построить полное дерево
    @Query("MATCH (user:UserNode {userId: $userId})-[:REFERRED*]->(descendants) RETURN user, descendants")
    List<UserNode> getFullTree();

    // Найти всех потомков для заданного пользователя
    @Query("MATCH (user:UserNode {userId: $userId})-[:REFERRED*]->(descendants) RETURN descendants")
    List<UserNode> getAllChildren(Long userId);

    // Найти всех предков для заданного пользователя
    @Query("MATCH (ancestors)-[:REFERRED*]->(user:UserNode {userId: $userId}) RETURN ancestors")
    List<UserNode> getAncs(Long userId);

    //Найти предка для потомка с UID
    @Query("MATCH (u:UserNode {referralCode: $referralCode}) RETURN u LIMIT 1")
    UserNode findUserByReferralCode(String referralCode);
}

