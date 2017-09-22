package org.teamspace.membership.domain;

import lombok.*;
import org.teamspace.auth.domain.User;
import org.teamspace.groups.domain.Group;

/**
 * Created by shpilb on 22/09/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullMembership {
    private Integer id;
    private User user;
    private Group group;
}
