-- 创建数据库
CREATE DATABASE IF NOT EXISTS zx_picture CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换库
USE zx_picture;

-- 用户信息表
CREATE TABLE IF NOT EXISTS user_info
(
    id           bigint unsigned     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name         varchar(50)         NOT NULL COMMENT '登录名及昵称',
    password     varchar(100)        NOT NULL COMMENT '密码',
    email        varchar(50)         NOT NULL COMMENT '用户邮箱',
    phone        varchar(15)                  DEFAULT NULL COMMENT '用户手机号',
    avatar       varchar(512)                 DEFAULT NULL COMMENT '用户头像',
    introduction varchar(512)                 DEFAULT NULL COMMENT '用户简介',
    role         varchar(20)                  DEFAULT 'user' COMMENT '用户角色（USER-普通用户, ADMIN-管理员）',
    vip_number   bigint unsigned              DEFAULT NULL COMMENT '会员编号',
    is_vip       tinyint(3) unsigned          DEFAULT 0 COMMENT '是否为会员;0-否 1-是',
    is_disabled  tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否禁用（0-正常, 1-禁用）',
    is_delete    tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time    datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time  datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    UNIQUE KEY uk_username (name),
    UNIQUE KEY uk_user_email (email),
    UNIQUE KEY uk_user_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表';

-- 用户会员表
CREATE TABLE IF NOT EXISTS user_vip
(
    id              bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    vip_code        varchar(20)              DEFAULT NULL COMMENT '会员兑换码',
    user_id         bigint unsigned          DEFAULT NULL COMMENT '用户ID',
    vip_number      bigint unsigned          DEFAULT NULL COMMENT '会员编号',
    vip_expire_time datetime        NOT NULL COMMENT '会员过期时间',
    is_used         tinyint(3) unsigned      DEFAULT 0 COMMENT '是否使用 0:未使用 1:已使用',
    edit_time       datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time     datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_user_id (id),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户会员表';


-- 图片信息表    点赞 收藏数量等数据先存入redis 通过定时任务同步到数据库
CREATE TABLE IF NOT EXISTS picture_info
(
    id               bigint unsigned     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    origin_url       varchar(512)        NOT NULL COMMENT '原图片地址(下载时使用)',
    origin_size      bigint              NOT NULL COMMENT '原图大小（单位: B）',
    origin_format    varchar(20)         NULL     DEFAULT NULL COMMENT '原图格式',
    origin_width     int                 NULL     DEFAULT NULL COMMENT '原图宽度',
    origin_height    int                 NULL     DEFAULT NULL COMMENT '原图高度',
    origin_scale     double              NULL     DEFAULT NULL COMMENT '原图比例（宽高比）',
    origin_color     varchar(20)         NULL     DEFAULT NULL COMMENT '原图主色调',
    origin_path      varchar(256)        NULL     DEFAULT NULL COMMENT '原图资源路径（对象键）',
    pic_name         varchar(256)        NOT NULL COMMENT '图片名称（展示）',
    pic_desc         varchar(256)        NULL     DEFAULT NULL COMMENT '图片描述（展示）',
    compress_url     varchar(512)        NULL     DEFAULT NULL COMMENT '图片地址（展示时使用, 压缩图地址）',
    compress_format  varchar(20)         NULL     DEFAULT NULL COMMENT '压缩图格式',
    compress_path    varchar(256)        NULL     DEFAULT NULL COMMENT '压缩图资源路径（对象键）',
    thumbnail_url    varchar(512)        NULL     DEFAULT NULL COMMENT '缩略图 url(可能用也可能不用)',
    thumbnail_path   varchar(256)        NULL     DEFAULT NULL COMMENT '缩略图资源路径（对象键）',
    category_id      bigint              NULL     DEFAULT NULL COMMENT '分类 ID',
    tags             varchar(512)                 DEFAULT NULL COMMENT '标签（JSON 数组）',
    user_id          bigint unsigned     NOT NULL COMMENT '创建用户 ID',
    space_id         bigint unsigned     NOT NULL DEFAULT 0 COMMENT '所属空间 ID（0-表示公共空间）',
    review_status    tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '0为待审核; 1为审核通过; 2为审核失败',
    review_message   varchar(512)                 DEFAULT NULL COMMENT '审核信息',
    reviewer_id      bigint unsigned              DEFAULT NULL COMMENT '审核人id',
    review_time      datetime                     DEFAULT NULL COMMENT '审核时间',
    like_quantity    int                 NOT NULL DEFAULT 0 COMMENT '点赞数量',
    collect_quantity int                 NOT NULL DEFAULT 0 COMMENT '收藏数量',
    is_delete        tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time        datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time      datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    INDEX idx_pic_name (pic_name),          -- 提升基于图片名称的查询性能
    INDEX idx_pic_desc (pic_desc),          -- 用于模糊搜索图片简介
    INDEX idx_category_id (category_id),    -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                  -- 提升基于标签的查询性能
    INDEX idx_user_id (user_id),            -- 提升基于用户 ID 的查询性能
    INDEX idx_space_id (space_id),          -- 提升基于空间 ID 的查询性能
    INDEX idx_review_status (review_status) -- 提升基于审核状态的查询性能
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '图片信息表';


-- 图片交互表
CREATE TABLE IF NOT EXISTS picture_interaction
(
    user_id            bigint unsigned     NOT NULL COMMENT '用户 ID',
    picture_id         bigint unsigned     NOT NULL COMMENT '图片 ID',
    interaction_type   tinyint(3) unsigned NOT NULL COMMENT '交互类型（0-点赞, 1-收藏）',
    interaction_status tinyint(3) unsigned NOT NULL COMMENT '交互状态（0-未互动, 1-已互动）',
    is_delete          tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time          datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time        datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id, picture_id, interaction_type),
    UNIQUE KEY pk_id (user_id, picture_id, interaction_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '图片交互表';

-- 图片分类表
CREATE TABLE IF NOT EXISTS picture_category
(
    id          bigint unsigned     NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    name        varchar(128)        NOT NULL COMMENT '分类名称',
    parent_id   bigint unsigned              DEFAULT 0 COMMENT '父分类 ID（0-表示顶层分类）',
    use_num     int unsigned                 DEFAULT 0 NOT NULL COMMENT '使用数量',
    user_id     bigint unsigned     NOT NULL COMMENT '创建用户 ID',
    is_delete   tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    INDEX idx_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '分类表';


-- 空间信息表
CREATE TABLE IF NOT EXISTS space_info
(
    id          bigint unsigned AUTO_INCREMENT COMMENT '主键 ID',
    user_id     bigint unsigned     NOT NULL COMMENT '创建用户 ID',
    space_name  varchar(128)        NOT NULL COMMENT '空间名称',
    space_type  tinyint(3)          NOT NULL DEFAULT 1 COMMENT '空间类型:1-私人空间,2-团队空间',
    space_level int                          DEFAULT 0 COMMENT '空间级别:0-普通版 1-专业版 2-旗舰版',
    max_size    bigint unsigned     NOT NULL DEFAULT 0 COMMENT '空间图片的最大总大小(单位:KB)',
    max_count   bigint unsigned     NOT NULL DEFAULT 0 COMMENT '空间图片的最大数量',
    used_size   bigint unsigned     NOT NULL DEFAULT 0 COMMENT '当前空间下已使用的空间(单位:KB)',
    used_count  bigint unsigned     NOT NULL DEFAULT 0 COMMENT '当前空间下已使用的的图片数量',
    is_delete   tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引设计
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    INDEX idx_user_id (user_id),
    -- 提升基于用户的查询效率
    INDEX idx_space_type (space_type),  -- 提升基于空间类型的查询效率
    INDEX idx_space_name (space_name),  -- 提升基于空间名称的查询效率
    INDEX idx_space_level (space_level) -- 提升按空间级别查询的效率
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '空间信息表';

-- 团队空间用户表
CREATE TABLE IF NOT EXISTS space_user
(
    id          bigint unsigned AUTO_INCREMENT COMMENT '主键ID',
    space_id    bigint unsigned     NOT NULL COMMENT '空间ID',
    user_id     bigint unsigned     NOT NULL COMMENT '用户ID',
    space_role  varchar(24)                  DEFAULT 'viewer' NULL COMMENT '空间角色(viewer-访问,editor-编辑,admin-管理)',
    edit_time   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    UNIQUE KEY uk_space_id_user_id (space_id, user_id), -- 唯一索引，用户在一个空间中只能有一个角色
    index idx_space_id (space_id),
    index idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '团队用户关联';