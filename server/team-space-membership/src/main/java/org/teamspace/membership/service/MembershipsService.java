package org.teamspace.membership.service;

import org.teamspace.membership.domain.*;

import java.util.List;

/**
 * Created by shpilb on 22/09/2017.
 */
public interface MembershipsService {
    List<Membership> findAll();
    List<MembershipByUsers> findAllGroupedByUsers();
    List<MembershipByGroups> findAllGroupedByGroups();
    List<Membership> findAllRaw();
    Membership findOne(Integer id);
    Membership findOneByUserIdAndGroupId(Integer userId, Integer groupId);
    Membership create(Membership Membership);
    void delete(Membership Membership);
    List<Membership> importMemberships(List<Membership> memberships);
    void deleteAllMemberships();
}
