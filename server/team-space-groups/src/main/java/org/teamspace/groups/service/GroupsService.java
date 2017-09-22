package org.teamspace.groups.service;

import org.springframework.stereotype.Service;
import org.teamspace.groups.domain.Group;

import java.util.List;

/**
 * Created by shpilb on 22/09/2017.
 */
@Service
public interface GroupsService {
    List<Group> findAll();
    Group findOne(Integer id);
    Group findOneByName(String name);
    Group create(Group group);
    Group update(Group group);
    void delete(Group group);
}
