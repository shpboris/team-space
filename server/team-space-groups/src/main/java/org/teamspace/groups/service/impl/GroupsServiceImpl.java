package org.teamspace.groups.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.groups.dao.GroupsDao;
import org.teamspace.groups.domain.Group;
import org.teamspace.groups.service.GroupsService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

/**
 * Created by shpilb on 22/09/2017.
 */
@Service
public class GroupsServiceImpl implements GroupsService{

    @Autowired
    private GroupsDao groupsDao;

    @Override
    public List<Group> findAll() {
        return groupsDao.findAll();
    }

    @Override
    public Group findOne(Integer id) {
        return groupsDao.findOne(id);
    }

    @Override
    public Group findOneByName(String name) {
        return groupsDao.findOneByName(name);
    }

    @Override
    @Transactional(TX_MANAGER)
    public Group create(Group group) {
        groupsDao.create(group);
        return groupsDao.findOneByName(group.getName());
    }

    @Override
    @Transactional(TX_MANAGER)
    public Group update(Group group) {
        groupsDao.update(group);
        return groupsDao.findOneByName(group.getName());
    }

    @Override
    @Transactional(TX_MANAGER)
    public void delete(Group group) {
        groupsDao.delete(group);
    }

    @Override
    @Transactional(TX_MANAGER)
    public List<Group> importGroups(List<Group> groups) {
        List<Group> createdGroupsList = new ArrayList<>();
        Map<String, Group> existingGroups = findAll()
                .stream().collect(Collectors.toMap(Group::getName, Function.identity()));
        for(Group currGroup : groups){
            if(existingGroups.get(currGroup.getName()) != null){
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
            Group createdGroup = create(currGroup);
            createdGroupsList.add(createdGroup);
        }
        return createdGroupsList;
    }

    @Override
    @Transactional(TX_MANAGER)
    public void deleteAllGroups(){
        groupsDao.deleteAll();
    }


}
