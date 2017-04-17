package org.teamspace.users.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.auth.domain.User;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.util.List;

/**
 * Created by shpilb on 11/04/2017.
 */
@Dao
public interface UsersDao {
    @Insert("INSERT INTO USERS (ID,USERNAME,PASSWORD, FIRST_NAME, LAST_NAME, ROLE) VALUES(#{id}, #{username}, #{password}, #{firstName}, #{lastName}, #{role})")
    void create(User user);

    @Select("SELECT * FROM USERS")
    @Results({
            @Result(property = "id", column = "ID"),
            @Result(property = "username", column = "USERNAME"),
            @Result(property = "password", column = "PASSWORD"),
            @Result(property = "firstName", column = "FIRST_NAME"),
            @Result(property = "lastName", column = "LAST_NAME"),
            @Result(property = "role", column = "ROLE")
    })
    List<User> findAll();

    @Select("SELECT * FROM USERS WHERE ID = #{id}")
    @Results({
            @Result(property = "id", column = "ID"),
            @Result(property = "username", column = "USERNAME"),
            @Result(property = "password", column = "PASSWORD"),
            @Result(property = "firstName", column = "FIRST_NAME"),
            @Result(property = "lastName", column = "LAST_NAME"),
            @Result(property = "role", column = "ROLE")
    })
    User findOne(Integer id);

    @Select("SELECT * FROM USERS WHERE USERNAME = #{username}")
    @Results({
            @Result(property = "id", column = "ID"),
            @Result(property = "username", column = "USERNAME"),
            @Result(property = "password", column = "PASSWORD"),
            @Result(property = "firstName", column = "FIRST_NAME"),
            @Result(property = "lastName", column = "LAST_NAME"),
            @Result(property = "role", column = "ROLE")
    })
    User findOneByUsername(@Param("username") String username);

    @Update("UPDATE USERS SET USERNAME = #{username}, PASSWORD = #{password}, FIRST_NAME = #{firstName}," +
            "LAST_NAME = #{lastName},ROLE = #{role} WHERE ID = #{id}")
    int update(User user);

    @Delete("DELETE FROM USERS WHERE ID = #{id}")
    int delete(User user);

    @TableCreator
    @Update("CREATE TABLE IF NOT EXISTS USERS(ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
            "USERNAME VARCHAR(50) NOT NULL, PASSWORD VARCHAR(50) NOT NULL, FIRST_NAME VARCHAR(50) NOT NULL, " +
            "LAST_NAME VARCHAR(50) NOT NULL, ROLE VARCHAR(50) NOT NULL)")
    void createTable();
}
