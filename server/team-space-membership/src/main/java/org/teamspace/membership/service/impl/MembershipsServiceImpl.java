package org.teamspace.membership.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.auth.domain.User;
import org.teamspace.groups.domain.Group;
import org.teamspace.membership.dao.MembershipsDao;
import org.teamspace.membership.domain.*;
import org.teamspace.membership.service.MembershipsService;
import org.teamspace.persistence.common.utils.PersistenceUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

import static java.util.stream.Collectors.*;
import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

/**
 * Created by shpilb on 22/09/2017.
 */
@Service
public class MembershipsServiceImpl implements MembershipsService{

    @Autowired
    private MembershipsDao membershipsDao;

    @Override
    public List<Membership> findAll() {
        return membershipsDao.findAll();
    }

    @Override
    public List<MembershipByUsers> findAllGroupedByUsers() {
        List<Membership> memberships = membershipsDao.findAllGroupedByUsers();
        List<MembershipByUsers> membershipByUsersList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(memberships)){
            Map<User, List<Group>> userGroups = memberships
                    .stream()
                    .collect(groupingBy(membership -> membership.getUser(),
                            mapping(membership -> membership.getGroup(), toList())));

            userGroups.keySet()
                    .stream()
                    .forEach(user -> {
                        membershipByUsersList.add(new MembershipByUsers(user,
                                PersistenceUtils.normalizeJoinedList(userGroups.get(user))));
                    });
        }
        return membershipByUsersList;
    }

    @Override
    public List<MembershipByGroups> findAllGroupedByGroups() {
        List<Membership> memberships = membershipsDao.findAllGroupedByGroups();
        List<MembershipByGroups> membershipByGroupsList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(memberships)){
            Map<Group, List<User>> groupUsers = memberships
                    .stream()
                    .collect(groupingBy(membership -> membership.getGroup(),
                            mapping(membership -> membership.getUser(), toList())));

            groupUsers.keySet()
                    .stream()
                    .forEach(group -> membershipByGroupsList.add(new MembershipByGroups(group,
                            PersistenceUtils.normalizeJoinedList(groupUsers.get(group)))));
        }
        return membershipByGroupsList;
    }

    @Override
    public List<Membership> findAllRaw() {
        return membershipsDao.findAllRaw();
    }

    @Override
    public Membership findOne(Integer id) {
        return membershipsDao.findOne(id);
    }

    @Override
    public Membership findOneByUserIdAndGroupId(Integer userId, Integer groupId) {
        return membershipsDao.findOneByUserIdAndGroupId(userId, groupId);
    }

    @Override
    @Transactional(TX_MANAGER)
    public Membership create(Membership membership) {
        membershipsDao.create(membership);
        return findOneByUserIdAndGroupId(membership.getUser().getId(), membership.getGroup().getId());
    }

    @Override
    @Transactional(TX_MANAGER)
    public void delete(Membership membership) {
        membershipsDao.delete(membership);
    }

    @Override
    @Transactional(TX_MANAGER)
    public List<Membership> importMemberships(List<Membership> memberships) {
        List<Membership> createdMembershipsList = new ArrayList<>();
        List<Membership> existingMemberships = findAll();
        for(Membership currMembership : memberships){
            boolean isMembershipExists = existingMemberships.stream().anyMatch(existingMembership -> {
                if(existingMembership.getUser().getUsername().equals(currMembership.getUser().getUsername()) &&
                        existingMembership.getGroup().getName().equals(currMembership.getGroup().getName())){
                    return true;
                }
                return false;
            });

            if(isMembershipExists){
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
            Membership createdMembership = create(currMembership);
            createdMembershipsList.add(createdMembership);
        }
        return createdMembershipsList;
    }

    @Override
    @Transactional(TX_MANAGER)
    public void deleteAllMemberships(){
        membershipsDao.deleteAll();
    }

}
