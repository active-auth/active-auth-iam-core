# 定位资源

与 RBAC 风格的传统认证框架（如 Spring Security 或者 Shiro）不同，一个 PBAC 认证中心并不清楚地维护一个资源的从属关系，
因此资源定位符通常用于在没有维护资源从属关系的认证中心中描述一个唯一的资源。

本项目采用的资源定位符是一个拥有丰富字段内容的字符串，使用冒号`:`分隔，可以清晰地描述资源所属的系统、子系统、微服务、数据中心和所属用户，
亦可以表示一些简单的目录风格的资源层级关系。

## 资源定位符

一个资源定位符形如

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

## 定位符中合法的空字段

字段 `region` 和 `account-id` 允许为空，这是一个合法案例：

> arn:activecloud-cn:oss:::my-website-static-media

一个存储桶全局名字空间内的命名唯一，或者系统资源拥有者不能显式包含在定位符中，这样一来您可以保留字段 `region` 和 `account-id` 为空来描述一个资源。

## 定位符中合法的通配符

通配符只能在限定范围内使用，您只能用在 `resource/path/and/id` 字段的最后一级目录的整个匹配，
通配符不可以匹配部分字符串，不可以用在中间级别的目录，更不可以使用在其他字段。

这是合法案例：

> arn:activecloud-cn:oss:::\*
>
> arn:activecloud-cn:oss:::my-website-static-media/\*
>
> arn:activecloud-cn:oss:::my-website-static-media/some-dir/\*

下面的都是不合法的案例：

> × 通配符用于部分字符串的匹配
>
> arn:activecloud-cn:oss:::my-website-\*
>
> arn:activecloud-cn:oss:::*media
>
> arn:activecloud-cn:oss:::my*media
>
> × 通配符用在了中间级别的目录
>
> arn:activecloud-cn:oss:::my-website-static-media/\*/a-sub-dir
>
> × 通配符重复
>
> arn:activecloud-cn:oss:::my-website-static-media/**
>
> × 通配符出现在了其他不允许的字段
>
> arn:activecloud-cn:*:::my-website-static-media
