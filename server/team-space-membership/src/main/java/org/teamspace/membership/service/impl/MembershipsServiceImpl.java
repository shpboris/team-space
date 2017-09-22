package org.teamspace.membership.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.membership.dao.MembershipsDao;
import org.teamspace.membership.domain.FullMembership;
import org.teamspace.membership.domain.Membership;
import org.teamspace.membership.service.MembershipsService;

import java.util.List;

import static org.teamspace.persistence.common.CommonConstants.TX_MANAGER;

/**
 * Created by shpilb on 22/09/2017.
 */
@Service
public class MembershipsServiceImpl implements MembershipsService{

    @Autowired
    private MembershipsDao membershipsDao;

    @Override
    public List<FullMembership> findAllWithUsersGroupsData() {
        return membershipsDao.findAllWithUsersGroupsData();
    }

    @Override
    public List<Membership> findAll() {
        return membershipsDao.findAll();
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
        return membershipsDao
                .findOneByUserIdAndGroupId(membership.getUserId(), membership.getGroupId());
    }

    @Override
    @Transactional(TX_MANAGER)
    public void delete(Membership membership) {
        membershipsDao.delete(membership);
    }

}
