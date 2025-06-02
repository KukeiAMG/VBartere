package com.example.Referral.repository;
import com.example.Referral.DTO.ParentChainDTO;
import com.example.Referral.DTO.ReferralLevelCount;
import com.example.Referral.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
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

    /**Находит всех потомков до 15 уровней глубины
     * Группирует их по уровню (длина пути)
     * Считает количество пользователей на каждом уровне
     * Возвращает список пар "уровень - количество"*/
    @Query("MATCH path = (user:UserNode {userId: $userId})<-[:REFERRED_BY*1..15]-(descendant) " +
            "WHERE descendant IS NOT NULL " +
            "WITH length(path) AS level, count(descendant) AS count " +
            "RETURN level, count " +
            "ORDER BY level")
    List<ReferralLevelCount> getChildrenCountByLevel(@Param("userId") Long userId);

//    @Query("MATCH path = (user:UserNode {userId: $userId})<-[:REFERRED_BY*1..15]-(ancestor) " +
//            "WITH ancestor, length(path) AS level " +
//            "RETURN ancestor.userId AS userId, level " +
//            "ORDER BY level")

    @Query("MATCH path = (user:UserNode {userId: $userId})-[:REFERRED_BY*1..15]->(ancestor) "
            + "RETURN ancestor as userNode, length(path) as level "
            + "ORDER BY level")
    List<ParentChainDTO> getParentChainWithLevels(@Param("userId") Long userId);
}

