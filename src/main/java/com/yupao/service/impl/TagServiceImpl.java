package com.yupao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupao.mapper.TagMapper;
import com.yupao.service.TagService;
import com.yupao.model.domain.Tag;
import org.springframework.stereotype.Service;

/**
* @author 0.0
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-12-20 11:29:48
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




