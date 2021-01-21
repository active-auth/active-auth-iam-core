# Active Auth

一个开源的 PBAC 云平台认证 & 授权中心。

[English](../en/README.md) | [简体中文](README.md)

## 功能

（README.md 文档和 wiki 页面将随第一个版本发布，尽请期待。）

### 定位资源

本项目采用的资源定位器是一个拥有丰富字段内容的字符串，使用冒号`:`分隔，可以清晰地描述资源所属的系统、子系统、微服务、数据中心和所属用户， 亦可以表示一些简单的目录风格的资源层级关系。

这是一个例子：

> arn:activecloud-cn:ecs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

更多请参考：[01 Locating Resources](01-Locating-Resources.md)

### 描述权限策略

权限策略是 PBAC 模式的认证框架或认证中心的核心概念。权限策略通常类似于自然语言中的谓宾短语，描述的是：“允许或拒绝对**某资源**进行**某操作**”。

权限策略的创建者将它授予其他人，则整个 PBAC 趋于完整，即描述成：“**谁**允许或拒绝**谁**对**某资源**进行**某操作**”。

例如下面的策略描述：

```json
{
  "name": "Allowing43ToListAndDeleteMyBooks",
  "effect": "ALLOW",
  "actions": [
    "bookshelf:ListBooks",
    "bookshelf:DeleteBooks"
  ],
  "resources": [
    "arn:cloudapp:bookshelf::31:bought-book/*",
    "arn:cloudapp:bookshelf::31:shoppping-cart/*"
  ]
}
```

更多请参考：02 Describing Authority Policies（撰写中）

## 维护者

[Okeyja Teung](https://github.com/Okeyja)

## 赞助者

没有赞助者。