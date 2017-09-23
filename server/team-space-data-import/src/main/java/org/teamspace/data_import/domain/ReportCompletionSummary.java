package org.teamspace.data_import.domain;

import lombok.*;
import org.teamspace.auth.domain.User;
import org.teamspace.groups.domain.Group;
import org.teamspace.membership.domain.Membership;

import java.util.List;

/**
 * Created by shpilb on 23/09/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportCompletionSummary {
    private Long jobId;
    private String date;
    private List<User> users;
    private List<Group> groups;
    private List<Membership> memberships;
}
