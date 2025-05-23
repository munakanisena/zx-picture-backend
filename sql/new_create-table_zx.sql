-- 创建数据库
CREATE DATABASE IF NOT EXISTS zx_picture CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换库
USE zx_picture;

-- 用户信息表
CREATE TABLE IF NOT EXISTS user_info
(
    id              bigint          unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    varchar(50)     NOT NULL COMMENT '登录名及昵称',
    password   varchar(100)    NOT NULL COMMENT '密码',
    user_email      varchar(50)     NOT NULL COMMENT '用户邮箱',
    user_phone      varchar(15)              DEFAULT NULL COMMENT '用户手机号',
    user_avatar     varchar(512)             DEFAULT NULL COMMENT '用户头像',
    user_sex        tinyint(3)      unsigned DEFAULT 2 COMMENT '用户性别;0-男 1-女 2-保密',
    user_profile    varchar(512)             DEFAULT NULL COMMENT '用户简介',
    birthday        date                     DEFAULT NULL COMMENT '出生日期',
    vip_number      bigint          unsigned DEFAULT NULL COMMENT '会员编号',
    user_role       varchar(20)              DEFAULT 'user' COMMENT '用户角色（USER-普通用户, ADMIN-管理员）',
    is_vip          tinyint(3)      unsigned DEFAULT 0 COMMENT '是否为会员;0-否 1-是', -- 通过 定时任务实现数据一致
    is_disabled     tinyint(3)     unsigned NOT NULL DEFAULT 0 COMMENT '是否禁用（0-正常, 1-禁用）',
    is_delete       tinyint(3)     unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time       datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY pk_id (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_user_email (user_email),
    UNIQUE KEY uk_user_phone (user_phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表';

-- 用户会员表
CREATE TABLE IF NOT EXISTS user_vip(
   id              bigint    unsigned   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   vip_code        varchar(20)              DEFAULT NULL COMMENT '会员兑换码',
   user_id         bigint    unsigned   DEFAULT NULL COMMENT '用户id',
   vip_number      bigint    unsigned   DEFAULT NULL COMMENT '会员编号',
   vip_expire_time datetime             NOT NULL     COMMENT '会员过期时间',
   is_used         tinyint(3)  unsigned    DEFAULT 0 COMMENT '是否使用 0:未使用 1:已使用',
   edit_time       datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
   create_time     datetime       NOT NULL  DEFAULT  CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time     datetime       NOT NULL  DEFAULT  CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (id),
   UNIQUE KEY pk_user_id(id),
   UNIQUE KEY uk_user_id(user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户会员表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_picture_collect(
   id              bigint    unsigned   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   user_id         bigint    unsigned   NOT NULL COMMENT '用户id',
   picture_id      bigint    unsigned   NOT NULL COMMENT '图片id',
   edit_time       datetime       NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
   create_time     datetime  NOT NULL  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time     datetime  NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (id),
   UNIQUE KEY pk_id (id),
   UNIQUE KEY uk_user_id (user_id,picture_id) -- 保证用户和图片组合唯一
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏表';

-- 用户点赞 使用redis实现

-- 图片信息表
CREATE TABLE IF NOT EXISTS picture_info(
   id           bigint          unsigned     NOT NULL      AUTO_INCREMENT COMMENT '主键ID',
   origin_url          varchar(512)                 NOT NULL     COMMENT '原图片地址(下载时使用)',
   origin_size       BIGINT          NOT NULL COMMENT '原图大小（单位: B）',
   origin_format     VARCHAR(20)     NULL     DEFAULT NULL COMMENT '原图格式',
   origin_width      INT             NULL     DEFAULT NULL COMMENT '原图宽度',
   origin_height     INT             NULL     DEFAULT NULL COMMENT '原图高度',
   origin_scale      decimal         NULL     DEFAULT NULL COMMENT '原图比例（宽高比）',
   origin_color      VARCHAR(20)     NULL     DEFAULT NULL COMMENT '原图主色调',
   pic_name          VARCHAR(256)    NOT NULL COMMENT '图片名称（展示）',
   pic_desc          VARCHAR(256)    NULL     DEFAULT NULL COMMENT '图片描述（展示）',
   compress_url           VARCHAR(512)    NOT NULL COMMENT '图片地址（展示时使用, 压缩图地址）',
   compress_format   VARCHAR(20)     NULL     DEFAULT NULL COMMENT '压缩图格式',
   thumbnail_url  varchar(512)               NOT NULL           COMMENT '缩略图 url(可能用也可能不用)',
   category_id       BIGINT          NULL     DEFAULT NULL COMMENT '分类 ID',
   tags         varchar(512)                       DEFAULT NULL COMMENT '标签（JSON 数组）',
   user_id      bigint          unsigned     NOT NULL            COMMENT '创建用户 id',
   space_id     bigint          unsigned     NOT NULL DEFAULT 0  COMMENT '所属空间 ID（0-表示公共空间）',
   review_status  tinyint(3)    unsigned  NOT NULL DEFAULT 0      COMMENT '0为待审核; 1为审核通过; 2为审核失败',
   review_message varchar(512)  DEFAULT NULL                     COMMENT '审核信息',
   reviewer_id   bigint         unsigned   DEFAULT NULL          COMMENT '审核人id',
   review_time    datetime      DEFAULT NULL                     COMMENT '审核时间',
   is_delete     tinyint(3)     unsigned NOT NULL DEFAULT 0      COMMENT '是否删除（0-正常, 1-删除）',
   edit_time     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
   create_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   update_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   PRIMARY KEY (id),
   UNIQUE key pk_id (id),
   INDEX idx_pic_name (pic_name),                 -- 提升基于图片名称的查询性能
   INDEX idx_pic_desc (pic_desc), -- 用于模糊搜索图片简介
   INDEX idx_category_id (category_id),         -- 提升基于分类的查询性能
   INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
   INDEX idx_user_id (user_id),           -- 提升基于用户 ID 的查询性能
   INDEX idx_space_id(space_id)           -- 提升基于空间 ID 的查询性能
)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_unicode_ci COMMENT = '图片信息表';

-- 图片分类表
CREATE TABLE IF NOT EXISTS category
(
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    name        VARCHAR(128)    NOT NULL COMMENT '分类名称',
    parent_id   BIGINT UNSIGNED          DEFAULT 0 COMMENT '父分类 ID（0-表示顶层分类）',
    use_num     INT                      DEFAULT 0 NOT NULL COMMENT '使用数量',
    user_id     BIGINT          NOT NULL COMMENT '创建用户 ID',
    is_delete   TINYINT         NOT NULL DEFAULT 0 COMMENT '是否删除（0-正常, 1-删除）',
    edit_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '分类表';


-- 空间信息表 todo 需要修改
CREATE TABLE IF NOT EXISTS space_info
(
    id         bigint   unsigned AUTO_INCREMENT COMMENT 'id' ,
    user_id     bigint    unsigned       NOT NULL COMMENT '创建用户 id',
    space_name  varchar(128)             NOT NULL COMMENT '空间名称',
    space_level int               DEFAULT 0       COMMENT '空间级别：0-普通版 1-专业版 2-旗舰版',
    max_size    bigint    unsigned    NOT NULL    DEFAULT 0          COMMENT '空间图片的最大总大小',
    max_count   bigint    unsigned    NOT NULL  DEFAULT 0           COMMENT '空间图片的最大数量',
    total_size  bigint    unsigned    NOT NULL   DEFAULT 0          COMMENT '当前空间下图片的总大小',
    total_count bigint    unsigned    NOT NULL   DEFAULT 0          COMMENT '当前空间下的图片数量',
    is_delete     tinyint(3)     unsigned NOT NULL DEFAULT 0      COMMENT '是否删除（0-正常, 1-删除）',
    edit_time     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
    create_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引设计
    PRIMARY KEY (id),
    UNIQUE KEY pk_id(id),
    INDEX idx_user_id (user_id),
    -- 提升基于用户的查询效率
    INDEX idx_space_name (space_name),  -- 提升基于空间名称的查询效率
    INDEX idx_space_level (space_level) -- 提升按空间级别查询的效率
)ENGINE = InnoDB
 DEFAULT CHARSET = utf8mb4
 COLLATE = utf8mb4_unicode_ci COMMENT = '空间信息表';