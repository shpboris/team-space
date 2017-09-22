package org.teamspace.membership.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.membership.domain.*;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.util.List;

/**
 * Created by shpilb on 11/04/2017.
 */
@Dao
public interface MembershipsDao {

    @Insert("INSERT INTO MEMBERSHIPS (ID, USER_ID, GROUP_ID) VALUES(#{id}, #{userId}, #{groupId})")
    int create(Membership membership);

    @Select("SELECT U.ID AS USER_ID, U.USERNAME, U.PASSWORD, U.FIRST_NAME, U.LAST_NAME, U.ROLE, " +
            "M.ID AS MEMBERSHIP_ID, G.ID AS GROUP_ID, G.NAME AS GROUP_NAME" +
            "  FROM USERS U JOIN MEMBERSHIPS M ON U.ID = M.USER_ID JOIN GROUPS G ON " +
            "M.GROUP_ID = G.ID")
    @Results(id = "fullMembershipResult", value = {
            @Result(property = "id", column = "MEMBERSHIP_ID"),
            @Result(property = "user.id", column = "USER_ID"),
            @Result(property = "user.username", column = "USERNAME"),
            @Result(property = "user.password", column = "PASSWORD"),
            @Result(property = "user.firstName", column = "FIRST_NAME"),
            @Result(property = "user.lastName", column = "LAST_NAME"),
            @Result(property = "user.role", column = "ROLE"),
            @Result(property = "group.id", column = "GROUP_ID"),
            @Result(property = "group.name", column = "GROUP_NAME")
    })
    List<FullMembership> findAllWithUsersGroupsData();

    @Select("SELECT * FROM MEMBERSHIPS")
    @Results(id = "membershipResult", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "userId", column = "USER_ID"),
            @Result(property = "groupId", column = "GROUP_ID")
    })
    List<Membership> findAll();

    @Select("SELECT * FROM MEMBERSHIPS WHERE ID = #{id}")
    @ResultMap("membershipResult")
    Membership findOne(Integer id);

    @Select("SELECT * FROM MEMBERSHIPS WHERE USER_ID = #{userId} AND GROUP_ID = #{groupId}")
    @ResultMap("membershipResult")
    Membership findOneByUserIdAndGroupId(@Param("userId") Integer userId, @Param("groupId") Integer groupId);

    @Delete("DELETE FROM MEMBERSHIPS WHERE ID = #{id}")
    int delete(Membership membership);

    @TableCreator
    @Update("CREATE TABLE IF NOT EXISTS MEMBERSHIPS(ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
            "USER_ID INT, GROUP_ID INT)")
    void createTable();
}
