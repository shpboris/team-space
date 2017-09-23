package org.teamspace.data_import.service;

import org.teamspace.auth.domain.User;
import org.teamspace.groups.domain.Group;
import org.teamspace.membership.domain.Membership;

import java.util.List;

/**
 * Created by shpilb on 23/09/2017.
 */
public interface DatabaseImportService {
    public void importData(List<User> users, List<Group> groups,
                           List<Membership> memberships, boolean isDeleteExistingData);
}
