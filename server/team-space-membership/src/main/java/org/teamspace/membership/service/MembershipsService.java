package org.teamspace.membership.service;

import org.teamspace.membership.domain.Membership;

import java.util.List;

/**
 * Created by shpilb on 22/09/2017.
 */
public interface MembershipsService {
    List<Membership> findAll();
    Membership findOne(Integer id);
    Membership findOneByUserIdAndGroupId(Integer userId, Integer groupId);
    Membership create(Membership Membership);
    void delete(Membership Membership);
}
