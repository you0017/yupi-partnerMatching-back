delete from `user` where id>=8;
alter table `user` auto_increment=8;
show create table `user`;

-- auto-generated definition
create table team
(
    id bigint auto_increment primary key,
    name      varchar(256) not null comment '队伍名称',
    description  varchar(1024) null comment '描述',
    maxNum int default 1 not null comment '最大人数',
    expire_time   datetime null comment '过期时间',
    userId bigint comment '用户id',
    status int default 0 not null comment '0-公开，1-私有，2-加密',
    password varchar(256) null comment '密码',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete tinyint default 0 not null comment '是否删除'
)
    comment '队伍';

-- auto-generated definition
create table user_team
(
    id bigint auto_increment primary key,
    user_id bigint not null comment '用户id',
    team_id bigint not null comment '队伍id',
    join_time datetime default CURRENT_TIMESTAMP null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete tinyint default 0 not null comment '是否删除'
)
    comment '用户_队伍';
