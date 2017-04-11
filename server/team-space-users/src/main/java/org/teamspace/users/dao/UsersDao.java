package org.teamspace.users.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.auth.domain.User;
import org.teamspace.persistence.common.CustomMapper;

/**
 * Created by shpilb on 11/04/2017.
 */
@CustomMapper
public interface UsersDao {
    @Insert("INSERT INTO USERS (ID,USERNAME,PASSWORD) VALUES(#{id}, #{username}, #{password})")
    void insertUser(User user);

    @Select("SELECT * FROM USERS WHERE ID = #{id}")
    @Results({
            @Result(property = "id", column = "ID"),
            @Result(property = "username", column = "USERNAME"),
            @Result(property = "password", column = "PASSWORD")
    })
    User findUserById(Integer id);

    @Update("CREATE TABLE IF NOT EXISTS USERS(ID INT PRIMARY KEY NOT NULL,USERNAME VARCHAR(50) NOT NULL, PASSWORD VARCHAR(50) NOT NULL)")
    void createTable();
}
