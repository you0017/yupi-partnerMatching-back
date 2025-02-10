package com.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupao.model.domain.UserTeam;
import com.yupao.service.UserTeamService;
import com.yupao.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 0.0
* @description 针对表【user_team(用户_队伍)】的数据库操作Service实现
* @createDate 2025-01-03 15:08:11
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




