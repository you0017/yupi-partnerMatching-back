package com.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupao.common.BaseResponse;
import com.yupao.common.DeleteRequest;
import com.yupao.common.ErrorCode;
import com.yupao.common.ResultUtils;
import com.yupao.exception.BusinessException;
import com.yupao.model.domain.Team;
import com.yupao.model.domain.User;
import com.yupao.model.domain.UserTeam;
import com.yupao.model.dto.TeamQuery;
import com.yupao.model.request.TeamAddRequest;
import com.yupao.model.request.TeamJoinRequest;
import com.yupao.model.request.TeamQuitRequest;
import com.yupao.model.request.TeamUpdateRequest;
import com.yupao.model.vo.TeamUserVO;
import com.yupao.service.TeamService;
import com.yupao.service.UserService;
import com.yupao.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final UserService userService;
    private final UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team, teamAddRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        teamService.addTeam(team, userService.getLoginUser(request));
        return ResultUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (teamService.deleteTeam(deleteRequest.getId(),userService.getLoginUser(request))) {
            return ResultUtils.success(true);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "解散失败");
        }
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (teamService.updateTeam(teamUpdateRequest, userService.getLoginUser(request))) {
            return ResultUtils.success(true);
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "获取失败");
        }
        return ResultUtils.success(team);
    }
    /**
     * 获取队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeam(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamUserVOS = teamService.listTeamWithUser(teamQuery, isAdmin);
        //判断当前用户是否加入队伍
        List<Long> teamIdList = teamUserVOS.stream().map(TeamUserVO::getId).toList();
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            lambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            lambdaQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> list = userTeamService.list(lambdaQueryWrapper);
            teamUserVOS.forEach(teamUserVO -> {
                list.forEach(userTeam -> {
                    if (userTeam.getTeamId().equals(teamUserVO.getId())){
                        teamUserVO.setHasJoin(true);
                    }
                });
            });
        }catch (Exception e){

        }
        return ResultUtils.success(teamUserVOS);
    }
    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @PostMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeam(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean isAdmin = userService.isAdmin(loginUser);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamUserVOS = teamService.listTeamWithUser(teamQuery, isAdmin);
        return ResultUtils.success(teamUserVOS);
    }
    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @PostMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeam(@RequestBody TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        LambdaQueryWrapper<UserTeam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
        List<UserTeam> list = userTeamService.list(lambdaQueryWrapper);
        //取出不重复的队伍id
        Map<Long, List<UserTeam>> collect = list.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamQuery.setIdList(new ArrayList<>(collect.keySet()));
        List<TeamUserVO> teamUserVOS = teamService.listTeamWithUser(teamQuery, true);
        return ResultUtils.success(teamUserVOS);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(@RequestBody TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(teamQuery, team);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>(team);
        return ResultUtils.success(teamService.page(page, queryWrapper));
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(teamService.joinTeam(teamJoinRequest, userService.getLoginUser(request)));
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(teamService.quitTeam(deleteRequest.getId(), userService.getLoginUser(request)));
    }

}
