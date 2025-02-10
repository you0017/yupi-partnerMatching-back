package com.yupao.service;

import com.yupao.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupao.model.domain.User;
import com.yupao.model.dto.TeamQuery;
import com.yupao.model.request.TeamJoinRequest;
import com.yupao.model.request.TeamQuitRequest;
import com.yupao.model.request.TeamUpdateRequest;
import com.yupao.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 0.0
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-01-03 15:07:41
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    List<TeamUserVO> listTeamWithUser(TeamQuery teamQuery, boolean isAdmin);

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    Boolean quitTeam(Long teamId, User loginUser);

    boolean deleteTeam(Long teamId, User loginUser);
}
