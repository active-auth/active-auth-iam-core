# Active Auth

一个开源的 PBAC 云平台认证&授权中心。

[English](README.md) | [简体中文](README-zh_Hans.md)

## 功能

（README.md 文档和 wiki 页面将随第一个版本发布，尽请期待。）

### 定位资源

#### 定位器风格

一个资源定位器形如

> prefix:partition:service:region:account-id:resource/paths/and/id

这是一个例子：

> arn:activecloud-cn:ecs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

| 字段名称 | 例子 | 释义 |
| ---- | ---- | ---- |
| prefix | arn | 您的云平台名称 <br> 例子展示的是您的云平台资源命名为 Activecloud Resource Name `ARN`。 |
| partition | activecloud-cn | 您的云平台分区, 用来区分不同的商业运营区。<br> 例子展示的是一个在中国大陆地区适应合规运营的分区，命名为 `activecloud-cn`。|
| service | ecs | 您的子系统/云服务名称，通常如果您有诸如虚拟机、对象存储、虚拟网络、AI、大数据等不同服务时用以区分使用。<br> 例子展示的是弹性云计算服务 Elastic Compute Service(`ECS`). |
| region | cn-north-3 | 您的子系统所在区域或数据中心的短代码。<br> 例子展示的是您的云资源在中国山东青岛(`cn-north-3`)。 |
| account-id | 7611 | 主体 ID。 例子展示的是资源属于用户 `7611`。|
| resource/paths/and/id | volume/vol-8678eY3109N946oVsq | 资源路径和资源 ID。 <br> 例子展示的是这个资源是一个存储卷 `volume`, ID 是：`vol-8678eY3109N946oVsq`。|

#### 定位器中合法的空字段

字段 `region` 和 `account-id` 允许为空，这是一个合法案例：

> arn:activecloud-cn:oss:::my-website-static-media

一个存储桶全局名字空间内的命名唯一，或者系统资源拥有者不能显式包含在定位器中，这样一来您可以保留字段 `region` 和 `account-id` 为空来描述一个资源。

## 维护者

[Okeyja Teung](https://github.com/Okeyja)

## 赞助者

没有赞助者。