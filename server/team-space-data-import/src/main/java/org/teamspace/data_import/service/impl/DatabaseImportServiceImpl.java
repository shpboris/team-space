package org.teamspace.data_import.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.auth.domain.User;
import org.teamspace.data_import.service.DatabaseImportService;
import org.teamspace.groups.domain.Group;
import org.teamspace.groups.service.GroupsService;
import org.teamspace.membership.domain.Membership;
import org.teamspace.membership.service.MembershipsService;
import org.teamspace.users.service.UsersService;

import java.util.List;

import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

/**
 * Created by shpilb on 23/09/2017.
 */
@Service
public class DatabaseImportServiceImpl implements DatabaseImportService {

    @Autowired
    private UsersService usersService;

    @Autowired
    private GroupsService groupsService;

    @Autowired
    private MembershipsService membershipsService;

    @Override
    @Transactional(TX_MANAGER)
    public void importData(List<User> users, List<Group> groups,
                           List<Membership> memberships, boolean isDeleteExistingData) {
        if(isDeleteExistingData){
            deleteExistingData();
        }
        importData(users, groups, memberships);

    }

    private void importData(List<User> users, List<Group> groups,
                            List<Membership> memberships){
        usersService.importUsers(users);
        groupsService.importGroups(groups);
        membershipsService.importMemberships(memberships);
    }

    private void deleteExistingData(){
        membershipsService.deleteAllMemberships();
        usersService.deleteNonAdminUsers();
        groupsService.deleteAllGroups();
    }
}
