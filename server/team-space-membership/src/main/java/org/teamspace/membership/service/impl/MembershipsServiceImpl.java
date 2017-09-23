package org.teamspace.membership.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.teamspace.membership.dao.MembershipsDao;
import org.teamspace.membership.domain.Membership;
import org.teamspace.membership.service.MembershipsService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
