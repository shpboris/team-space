package org.teamspace.groups.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.groups.dao.GroupsDao;
import org.teamspace.groups.domain.Group;
import org.teamspace.groups.service.GroupsService;

import java.util.List;

import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

/**
 * Created by shpilb on 22/09/2017.
 */
@Service
public class GroupsServiceImpl implements GroupsService{

    @Autowired
    private GroupsDao usersDao;

    @Override
    public List<Group> findAll() {
        return usersDao.findAll();
    }

    @Override
    public Group findOne(Integer id) {
        return usersDao.findOne(id);
    }

    @Override
    public Group findOneByName(String name) {
        return usersDao.findOneByName(name);
    }

    @Override
    @Transactional(TX_MANAGER)
    public Group create(Group group) {
        usersDao.create(group);
        return usersDao.findOneByName(group.getName());
    }

    @Override
    @Transactional(TX_MANAGER)
    public Group update(Group group) {
        usersDao.update(group);
        return usersDao.findOneByName(group.getName());
    }

    @Override
    @Transactional(TX_MANAGER)
    public void delete(Group group) {
        usersDao.delete(group);
    }
}
