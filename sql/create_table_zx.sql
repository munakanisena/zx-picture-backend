-- 创建数据库
CREATE DATABASE IF NOT EXISTS zx_picture CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换库
USE zx_picture;

-- 创建表  tab+shift 全局退格
CREATE TABLE IF NOT EXISTS tb_user
(
  id           BIGINT UNSIGNED AUTO_INCREMENT NOT NULL comment 'id',
  userAccount  varchar(256)                           not null COMMENT '账号',
  userPassword varchar(512)                           not null COMMENT '密码',
  userName     varchar(256)                           null COMMENT '用户昵称',
  userAvatar   varchar(1024)                          null COMMENT '用户头像',
  userProfile  varchar(512)                           null COMMENT '用户简介',
  userRole     varchar(256) default 'user'            not null COMMENT '用户角色：user/admin',
  editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
  createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
  updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
  isDelete     tinyint      default 0                 not null comment '是否删除',

  PRIMARY KEY (id),
  UNIQUE KEY uk_userAccount (userAccount),
  INDEX idx_userName (userName)
) COMMENT '用户' collate = utf8mb4_unicode_ci;

-- 图片表
CREATE TABLE IF NOT EXISTS tb_picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                       null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 涉及用户添加图片 需要对其进行审核 要添加picture字段

alter table tb_picture
    -- 添加新字段
    add column reviewStatus  int           not null default 0 comment '0为待审核; 1为审核通过; 2为审核失败',
    add column reviewMessage varchar(512)  null               comment '审核信息',
    add column reviewId      bigint        null               comment '审核人id',
    add column reviewTime    datetime      null               comment '审核时间';
-- 添加索引
create index idx_reviewStatus on tb_picture (reviewStatus);

-- 添加缩放图URL
alter table tb_picture
    add column thumbnailUrl varchar(512)   NULL comment '缩略图 url';

-- 空间表
CREATE TABLE IF NOT EXISTS tb_space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;

-- 添加新列 与图库空间进行关联
ALTER TABLE tb_picture
    ADD COLUMN spaceId  bigint  null comment '空间 id（为空表示公共空间）';

-- 创建索引
CREATE INDEX idx_spaceId ON tb_picture (spaceId);

-- 优化图片搜索 增加主色调
ALTER TABLE tb_picture
    ADD COLUMN picColor varchar(16) null comment '图片主色调';

-- 划分空间类型 (私有空间 和团队空间)
ALTER TABLE tb_space
    ADD COLUMN spaceType int default 0 not null comment '空间类型: 0为私有空间，1为团队空间';
-- 添加索引
CREATE INDEX idx_spaceType on tb_space(spaceType);

-- 团队空间用户表
CREATE TABLE IF NOT EXISTS tb_space_user
(
    id              bigint          auto_increment          comment 'id' primary key ,
    spaceId         bigint          not null                comment '空间Id',
    userId          bigint          not null                comment '用户id',
    spaceRole       varchar(128)    default 'viewer' null   comment '空间角色：viewer/editor/admin',
    createTime      datetime        default CURRENT_TIMESTAMP NOT NULL comment '创建时间',
    updateTie       datetime        default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL comment '更新时间',
    -- 索引设计
    unique key  uk_spaceId_userId (spaceId,userId), -- 唯一索引，用户在一个空间中只能有一个角色
    index idx_spaceId (spaceId),
    index idx_userId  (userId)
)comment '团队用户关联' collate =utf8mb4_unicode_ci;

