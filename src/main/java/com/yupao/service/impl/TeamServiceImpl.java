package com.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupao.common.ErrorCode;
import com.yupao.exception.BusinessException;
import com.yupao.model.domain.Team;
import com.yupao.model.domain.User;
import com.yupao.model.domain.UserTeam;
import com.yupao.model.dto.TeamQuery;
import com.yupao.model.enums.TeamStatusEnum;
import com.yupao.model.request.TeamJoinRequest;
import com.yupao.model.request.TeamQuitRequest;
import com.yupao.model.request.TeamUpdateRequest;
import com.yupao.model.vo.TeamUserVO;
import com.yupao.model.vo.UserVO;
import com.yupao.service.TeamService;
import com.yupao.mapper.TeamMapper;
import com.yupao.service.UserService;
import com.yupao.service.UserTeamService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author 0.0
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2025-01-03 15:07:41
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {
    private final TeamMapper teamMapper;
    private final UserTeamService userTeamService;
    private final UserService userService;

    @Override
    public long addTeam(Team team, User loginUser) {
        team.setUserId(loginUser.getId());
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        String name = team.getName();
        if (name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称过长");
        }
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (status < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum) && (StringUtils.isBlank(password) || password.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
        }
        if (new Date().after(team.getExpireTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "过期");
        }
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId, team.getUserId());
        if (this.count(queryWrapper) >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多五个队伍");
        }
        if (!this.save(team) || team.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(new Date());
        if (!userTeamService.save(userTeam) || userTeam.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入失败");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeamWithUser(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        Long userid = teamQuery.getUserId();
        queryWrapper.eq(userid != null && userid > 0, Team::getUserId, userid);
        List<Long> idList = teamQuery.getIdList();
        if (CollectionUtils.isNotEmpty(idList)){
            queryWrapper.in(Team::getId, idList);
        }
        String description = teamQuery.getDescription();
        queryWrapper.like(StringUtils.isNotBlank(description), Team::getDescription, description);

        Integer status = teamQuery.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (enumByValue == null) {
            enumByValue = TeamStatusEnum.PUBLIC;
        }
        if (!isAdmin && !TeamStatusEnum.PUBLIC.equals(enumByValue)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        queryWrapper.eq(status != null && status >= 0, Team::getStatus, status);

        String name = teamQuery.getName();
        queryWrapper.like(StringUtils.isNotBlank(name), Team::getName, name);
        Integer maxNum = teamQuery.getMaxNum();
        queryWrapper.eq(maxNum != null && maxNum > 0, Team::getMaxNum, maxNum);
        Long id = teamQuery.getId();
        queryWrapper.eq(id != null && id > 0, Team::getId, id);

        queryWrapper.and(StringUtils.isNotBlank(teamQuery.getSearchText()),(wrapper) -> {wrapper.like(Team::getDescription,teamQuery.getSearchText()).or().like(Team::getName,teamQuery.getSearchText());});
        queryWrapper.and((wrapper) -> {wrapper.isNull(Team::getExpireTime).or().gt(Team::getExpireTime, new Date());});

        //查询队伍列表
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }

        List<TeamUserVO> teamListVO = new ArrayList<>();
        //关联查询创建人
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null){
                continue;
            }
            User user = userService.getById(userId);
            //脱敏
            User safetyUser = userService.getSafetyUser(user);
            TeamUserVO teamUserVO = new TeamUserVO();
            UserVO userVO = new UserVO();
            try {
                BeanUtils.copyProperties(userVO, safetyUser);
                BeanUtils.copyProperties(teamUserVO, team);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            teamUserVO.setCreateUser(userVO);
            teamListVO.add(teamUserVO);
        }
        return teamListVO;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (teamUpdateRequest.getId() == null || teamUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(teamUpdateRequest.getId());
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //加密房间要有密码
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (enumByValue != null && enumByValue.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }

        Team updateTeam = new Team();
        try {
            BeanUtils.copyProperties(updateTeam, teamUpdateRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.updateById(updateTeam);
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (team.getExpireTime()!=null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
        List<UserTeam> list = userTeamService.list(userTeamLambdaQueryWrapper);
        long hasJoinNum = list.size();
        if (hasJoinNum > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入五个队伍");
        }
        for (UserTeam userTeam : list) {
            if (teamId.equals(userTeam.getTeamId())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
            }
        }
        //已加入队伍人数 >= 队伍最大人数
        long hasJoinTeamNum = countTeamUserByTeamId(teamId);
        if (hasJoinTeamNum >= team.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        return userTeamService.save(userTeam);
    }

    @Override
    public Boolean quitTeam(Long teamId, User loginUser) {
        if (teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        long userId = loginUser.getId();
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId).eq(UserTeam::getTeamId, teamId);
        long count = userTeamService.count(userTeamLambdaQueryWrapper);
        if (count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        Long teamHasJoinNum = countTeamUserByTeamId(teamId);
        if (teamHasJoinNum == 1){
            //队伍中只有一个人，解散队伍
            this.removeById(teamId);
            userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
            return userTeamService.remove(userTeamLambdaQueryWrapper);
        } else {
            //队伍中还有多个人，退出队伍
            //是否是队长
            if (team.getId().equals(userId)){
                //将队伍转移给最早加入的用户
                //查询已加入队伍的所有用户和加入时间
                userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
                userTeamLambdaQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamLambdaQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam userTeam = userTeamList.get(1);
                //更新队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(userTeam.getUserId());
                boolean b = this.updateById(updateTeam);
                if (!b){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
                }
                userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
                userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
                userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId);
            }
            return userTeamService.remove(userTeamLambdaQueryWrapper);
        }
    }

    @Override
    public boolean deleteTeam(Long id, User loginUser) {
        //校验队伍是否存在
        Team team = this.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //校验是否是队长
        if (!team.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权解散");
        }
        //移除所有关联信息
        userTeamService.remove(new LambdaQueryWrapper<UserTeam>().eq(UserTeam::getTeamId, id));
        this.removeById(id);
        return true;
    }

    private Long countTeamUserByTeamId(Long teamId){
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId);
        return userTeamService.count(userTeamLambdaQueryWrapper);
    }
}




