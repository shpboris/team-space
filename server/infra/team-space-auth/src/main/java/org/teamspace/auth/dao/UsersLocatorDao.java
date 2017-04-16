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
			@Result(property = "password", column = "PASSWORD")
	})
	User findUserByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
