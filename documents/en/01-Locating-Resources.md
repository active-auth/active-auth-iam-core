# Locating Resources

Unlike traditional RBAC-style authentication frameworks (such as Spring Security or Shiro), a PBAC certification center does not clearly maintain the affiliation of a resource,
therefore resource locator is usually used to describe a unique resource in a certification center that does not maintain resource ownership.

The resource locator used in this project is a string with rich field content, separated by a colon `:`, which can clearly describe the system, subsystem, microservice, data center, and user to which the resource belongs.
It can also express some simple resource hierarchical relationships in catalog style.

## Resource Locator

A resource locator looks like

> prefix:partition:service:region:account-id:resource/paths/and/id

Here is an example:

> arn:activecloud-cn:ecs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

| field name | example | explaining |
| ---- | ---- | ---- |
| prefix | arn | Your cloud platform name. <br> Example shows that locator named Activecloud Resource Name `ARN`. |
| partition | activecloud-cn | Your cloud platform partition, used to distinguish different commercial operating area. <br> Example shows an area operating in Mainland China to adapt to local legal compliance requirements, named `activecloud-cn`. |
| service | ecs | Your subsystem/cloud-service name, usually using when you have different service such as Virtual Hosting, Object Storage, Virtual Network, AI, Big Data services etc. <br> Example shows its a Elastic Compute Service(`ECS`). |
| region | cn-north-3 | Your subsystem region/datacenter code. <br> Example shows that resource is located in QingDao, Shandong, China(`cn-north-3`) |
| account-id | 7611 | Principal ID. Example shows that resource belongs to user `7611`. |
| resource/paths/and/id | volume/vol-8678eY3109N946oVsq | Resource paths and resource id. <br> Example shows that resource is a `volume`, id: `vol-8678eY3109N946oVsq`. |

## Legal Empty Field in Locator

Field `region` and `account-id` is allowed to be empty. Here is a legal example:

> arn:activecloud-cn:oss:::my-website-static-media

A storage bucket is named uniquely in a global namespace, or any other system resource owner of whom can't be included in
the locator, in that case you can keep field `region` and `account-id` empty to describe a resource.

## Legal Wildcard in Locator

Wildcards can only be used within a limited range. You can only use them in the entire match of the last level of the directory in the `resource/path/and/id` field.
Wildcards cannot match partial strings, be used in intermediate-level directories, or be used in other fields.

These are legal cases:

> arn:activecloud-cn:oss:::\*
>
> arn:activecloud-cn:oss:::my-website-static-media/\*
>
> arn:activecloud-cn:oss:::my-website-static-media/some-dir/\*

The following are illegal cases:

> × Wildcards are used to match partial strings
>
> arn:activecloud-cn:oss:::my-website-\*
>
> arn:activecloud-cn:oss:::*media
>
> arn:activecloud-cn:oss:::my*media
>
> × Wildcards are used in middle-level directories
>
> arn:activecloud-cn:oss:::my-website-static-media/\*/a-sub-dir
>
> × Repeating wildcards
>
> arn:activecloud-cn:oss:::my-website-static-media/**
>
> × Wildcards appears in other fields that aren't allowed.
>
> arn:activecloud-cn:*:::my-website-static-media
