package org.teamspace.membership.domain;

import lombok.*;

/**
 * Created by shpilb on 15/09/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Membership {
    private Integer id;
    private Integer userId;
    private Integer groupId;
}
