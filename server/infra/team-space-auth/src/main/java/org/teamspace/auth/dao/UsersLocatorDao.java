package org.teamspace.auth.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.auth.domain.User;
import org.teamspace.persistence.common.Dao;

@Dao
public interface UsersLocatorDao {

	@Select("SELECT * FROM USERS WHERE USERNAME = #{username} AND PASSWORD = #{password}")
	@Results({
			@Result(property = "id", column = "ID"),
			@Result(property = "username", column = "USERNAME"),
			@Result(property = "password", column = "PASSWORD"),
			@Result(property = "firstName", column = "FIRST_NAME"),
			@Result(property = "lastName", column = "LAST_NAME"),
			@Result(property = "role", column = "ROLE")
	})
	User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
