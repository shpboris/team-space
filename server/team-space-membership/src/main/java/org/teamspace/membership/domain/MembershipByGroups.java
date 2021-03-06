package org.teamspace.membership.domain;

import lombok.*;
import org.teamspace.auth.domain.User;
import org.teamspace.groups.domain.Group;

import java.util.List;

/**
 * Created by shpilb on 15/09/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembershipByGroups {
    private Group group;
    private List<User> users;
}
