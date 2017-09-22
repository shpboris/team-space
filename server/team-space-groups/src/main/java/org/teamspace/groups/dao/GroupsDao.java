package org.teamspace.groups.dao;

import org.apache.ibatis.annotations.*;
import org.teamspace.groups.domain.Group;
import org.teamspace.persistence.common.Dao;
import org.teamspace.persistence.common.TableCreator;

import java.util.List;

/**
 * Created by shpilb on 11/04/2017.
 */
@Dao
public interface GroupsDao {
    @Insert("INSERT INTO GROUPS (ID, NAME) VALUES(#{id}, #{name})")
    int create(Group group);

    @Select("SELECT * FROM GROUPS")
    @Results(id = "groupResult", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "name", column = "NAME")
    })
    List<Group> findAll();

    @Select("SELECT * FROM GROUPS WHERE ID = #{id}")
    @ResultMap("groupResult")
    Group findOne(Integer id);

    @Select("SELECT * FROM GROUPS WHERE NAME = #{name}")
    @ResultMap("groupResult")
    Group findOneByName(@Param("name") String name);

    @Update("UPDATE GROUPS SET NAME = #{name} WHERE ID = #{id}")
    int update(Group group);

    @Delete("DELETE FROM GROUPS WHERE ID = #{id}")
    int delete(Group group);

    @TableCreator
    @Update("CREATE TABLE IF NOT EXISTS GROUPS(ID INT AUTO_INCREMENT PRIMARY KEY NOT NULL, " +
            "NAME VARCHAR(50) NOT NULL)")
    void createTable();
}
