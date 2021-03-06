package org.teamspace.membership.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.membership.domain.Membership;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.util.List;

/**
 * Created by shpilb on 11/04/2017.
 */
@Dao
public interface MembershipsDao {

    String MEMBERSHIPS_BASIC_SELECT =
            "SELECT " +
                    "U.ID AS USER_ID, " +
                    "U.USERNAME, " +
                    "U.PASSWORD, " +
                    "U.FIRST_NAME, " +
                    "U.LAST_NAME, " +
                    "U.ROLE, " +
                    "M.ID AS MEMBERSHIP_ID, " +
                    "G.ID AS GROUP_ID, " +
                    "G.NAME AS GROUP_NAME " +
            "FROM " +
                    "USERS U " +
                    "JOIN MEMBERSHIPS M ON U.ID = M.USER_ID " +
                    "JOIN GROUPS G ON M.GROUP_ID = G.ID";

    String MEMBERSHIPS_BY_USERS_SELECT =
            "SELECT " +
                    "U.ID AS USER_ID, " +
                    "U.USERNAME, " +
                    "U.PASSWORD, " +
                    "U.FIRST_NAME, " +
                    "U.LAST_NAME, " +
                    "U.ROLE, " +
                    "M.ID AS MEMBERSHIP_ID, " +
                    "G.ID AS GROUP_ID, " +
                    "G.NAME AS GROUP_NAME " +
                    "FROM " +
                    "USERS U " +
                    "LEFT JOIN MEMBERSHIPS M ON U.ID = M.USER_ID " +
                    "LEFT JOIN GROUPS G ON M.GROUP_ID = G.ID";

    String MEMBERSHIPS_BY_GROUPS_SELECT =
            "SELECT " +
                    "U.ID AS USER_ID, " +
                    "U.USERNAME, " +
                    "U.PASSWORD, " +
                    "U.FIRST_NAME, " +
                    "U.LAST_NAME, " +
                    "U.ROLE, " +
                    "M.ID AS MEMBERSHIP_ID, " +
                    "G.ID AS GROUP_ID, " +
                    "G.NAME AS GROUP_NAME " +
                    "FROM " +
                    "GROUPS G " +
                    "LEFT JOIN MEMBERSHIPS M ON G.ID = M.GROUP_ID " +
                    "LEFT JOIN USERS U ON M.USER_ID = U.ID";

    String MEMBERSHIPS_RAW_SELECT = "SELECT ID, USER_ID, GROUP_ID FROM MEMBERSHIPS";

    @Insert("INSERT INTO MEMBERSHIPS (ID, USER_ID, GROUP_ID) VALUES(#{id}, #{user.id}, #{group.id})")
    int create(Membership membership);

    @Select(MEMBERSHIPS_BASIC_SELECT)
    @Results(id = "membershipResult", value = {
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
    List<Membership> findAll();

    @Select(MEMBERSHIPS_BY_USERS_SELECT)
    @ResultMap("membershipResult")
    List<Membership> findAllGroupedByUsers();

    @Select(MEMBERSHIPS_BY_GROUPS_SELECT)
    @ResultMap("membershipResult")
    List<Membership> findAllGroupedByGroups();

    @Select(MEMBERSHIPS_RAW_SELECT)
    @Results(id = "membershipRawResult", value = {
            @Result(property = "id", column = "MEMBERSHIP_ID"),
            @Result(property = "user.id", column = "USER_ID"),
            @Result(property = "group.id", column = "GROUP_ID")
    })
    List<Membership> findAllRaw();

    @Select(MEMBERSHIPS_BASIC_SELECT + " WHERE M.ID = #{id}")
    @ResultMap("membershipResult")
    Membership findOne(Integer id);

    @Select(MEMBERSHIPS_BASIC_SELECT + " WHERE USER_ID = #{userId} AND GROUP_ID = #{groupId}")
    @ResultMap("membershipResult")
    Membership findOneByUserIdAndGroupId(@Param("userId") Integer userId, @Param("groupId") Integer groupId);

    @Delete("DELETE FROM MEMBERSHIPS WHERE ID = #{id}")
    int delete(Membership membership);

    @Delete("DELETE FROM MEMBERSHIPS")
    int deleteAll();

    @TableCreator(creationOrder = 2)
    @Update("CREATE TABLE IF NOT EXISTS MEMBERSHIPS(" +
            "ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
            "USER_ID INT, " +
            "GROUP_ID INT, " +
            "CONSTRAINT USER_GROUP_UC UNIQUE (USER_ID, GROUP_ID), " +
            "FOREIGN KEY (USER_ID) REFERENCES USERS (ID) " +
                " ON DELETE CASCADE " +
                " ON UPDATE CASCADE, " +
            "FOREIGN KEY (GROUP_ID) REFERENCES GROUPS (ID) " +
                " ON DELETE CASCADE " +
                " ON UPDATE CASCADE)"
            )
    void createTable();
}
